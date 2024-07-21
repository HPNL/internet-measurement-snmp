package com.intenert_measurement.snmp.collector;

import com.intenert_measurement.snmp.Configuration;
import com.intenert_measurement.snmp.util.HostSnmpConnectionInfo;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class CollectorUtil {

    public static Map<String, Object> collect(HostSnmpConnectionInfo host, List<String> oids, boolean isTable) {
        try {
            // Create TransportMapping and Listen
            TransportMapping transport = new DefaultUdpTransportMapping();
            transport.listen();

            // Create Target Address object
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(host.getRCommunity()));
            target.setVersion(Configuration.SNMP_VERSION);
            target.setAddress(new UdpAddress(host.getIp() + "/" + host.getPort()));
            target.setRetries(2);
            target.setTimeout(host.getTimeout());

            // Create the PDU object
            PDU pdu = new PDU();
            pdu.addAll(oids.stream().map(x -> new VariableBinding(new OID(x))).collect(Collectors.toList()));
            pdu.setType(PDU.GET);

            // Create Snmp object for sending data to Agent
            try (Snmp snmp = new Snmp(transport)) {
                log.info("Sending Request to Host {}", host.getAddress());

                if (isTable) {
                    return getTableAsStrings(snmp, target, oids);
                }
                ResponseEvent response = snmp.get(pdu, target);
                // Process Agent Response
                if (response != null) {
                    log.info("Got Response from Host {}", host.getAddress());
                    PDU responsePDU = response.getResponse();
                    if (responsePDU != null) {
                        int errorStatus = responsePDU.getErrorStatus();
                        if (errorStatus != PDU.noError) {
                            int errorIndex = responsePDU.getErrorIndex();
                            String errorStatusText = responsePDU.getErrorStatusText();
                            log.info("Error: Request Failed");
                            log.info("Error Status = " + errorStatus);
                            log.info("Error Index = " + errorIndex);
                            log.info("Error Status Text = " + errorStatusText);
                        } else {
                            return readMetric(host, responsePDU);
                        }
                    } else {
                        log.info("Error: Response PDU is null from host {}", host.getAddress());
                    }
                } else {
                    log.info("Error: Host timeout... from host {}", host.getAddress());
                }
            }
        } catch (Exception e) {
            log.error("Could not collectSingleOID snmp from host {}", host.getAddress());
        }
        return new HashMap<>();
    }

    private static Map<String, Object> readMetric(HostSnmpConnectionInfo host, PDU responsePDU) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<? extends VariableBinding> variableBindings = responsePDU.getVariableBindings();
            for (VariableBinding variableBinding : variableBindings) {
                try {
                    result.put(variableBinding.getOid().toDottedString(), variableBinding.getVariable().clone());
                } catch (Exception ignore) {
                }
            }
        } catch (Exception e) {
            log.error("Could not read SNMP response from host {}", host.getAddress());
        }
        return result;
    }

    private static Map<String, Object> getTableAsStrings(Snmp snmp, Target target, List<String> oids) {
        TableUtils tUtils = new TableUtils(snmp, new DefaultPDUFactory());
        OID[] oidObj = oids.stream().map(OID::new).toArray(OID[]::new);
        List<TableEvent> events = tUtils.getTable(target, oidObj, null, null);

        Map<String, Object> list = new HashMap<>();
        for (TableEvent event : events) {
            if (event.isError()) {
                throw new RuntimeException(event.getErrorMessage());
            }
            for (VariableBinding vb : event.getColumns()) {
                if (vb != null) {
                    list.put(vb.getOid().toDottedString(), vb.getVariable());
                }
            }
        }
        return list;
    }

    private static Number castToNumber(Object value) {
        if (value instanceof TimeTicks) {
            return ((TimeTicks) value).toMilliseconds();
        }
        if (Number.class.isAssignableFrom(value.getClass())) {
            return (Number) value;
        }
        log.error("Not supported value type {}", value.getClass().getSimpleName());
        return null;
    }
}

package com.intenert_measurement.snmp;

import com.intenert_measurement.snmp.util.HostSnmpConnectionInfo;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class CollectorUtil {

    public static Map<String, Number> collect(HostSnmpConnectionInfo host, List<String> oids) {
        try {
            // Create TransportMapping and Listen
            TransportMapping transport = new DefaultUdpTransportMapping();
            transport.listen();

            // Create Target Address object
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(host.getRCommunity()));
            target.setVersion(Configuration.SNMP_VERSION);
            target.setAddress(new UdpAddress(host.getIp() + "/" + host.getPort()));
            target.setRetries(1);
            target.setTimeout(host.getTimeout());

            // Create the PDU object
            PDU pdu = new PDU();
            pdu.addAll(oids.stream().map(x -> new VariableBinding(new OID(x))).collect(Collectors.toList()));
            pdu.setType(PDU.GET);

            // Create Snmp object for sending data to Agent
            try (Snmp snmp = new Snmp(transport)) {
                log.info("Sending Request to Host {}", host.getAddress());
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
            log.error("Could not collect snmp from host {}", host.getAddress());
        }
        return new HashMap<>();
    }

    private static Map<String, Number> readMetric(HostSnmpConnectionInfo host, PDU responsePDU) {
        Map<String, Number> result = new HashMap<>();
        try {
            List<? extends VariableBinding> variableBindings = responsePDU.getVariableBindings();
            for (VariableBinding variableBinding : variableBindings) {
                try {
                    result.put(variableBinding.getOid().toDottedString(), castToNumber(variableBinding.getVariable().clone()));
                } catch (Exception ignore) {
                }
            }
        } catch (Exception e) {
            log.error("Could not read SNMP response from host {}", host.getAddress());
        }
        return result;
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

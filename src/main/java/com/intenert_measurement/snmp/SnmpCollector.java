package com.intenert_measurement.snmp;

import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SnmpCollector {

    // OID of MIB RFC 1213; Scalar Object = .iso.org.dod.internet.mgmt.mib-2.system.sysDescr.0
    private static final String Sys_UPTIME_OID = ".1.3.6.1.2.1.1.3.0";  // ends with 0 for scalar object
    private static final int SNMP_VERSION = SnmpConstants.version2c;
    private static final long COLLECTOR_PERIOD = TimeUnit.SECONDS.toMillis(300);

    public final List<Metric> hostCollectedMetrics = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        long appStartTime = System.currentTimeMillis();
        log.info("-----------------------------------------------------------------------------");
        log.info("Start SNMP Collector to Measure Failure Metrics: MTTR, MTBF, MTTF\n");

        List<HostSnmpConnectionInfo> hosts = new ArrayList<>();

        hosts.add(new HostSnmpConnectionInfo("vm1", "127.0.0.1", 161, 100, "public"));
        hosts.add(new HostSnmpConnectionInfo("vm2", "127.0.0.1", 1162, 100, "public"));

        SnmpCollector collector = new SnmpCollector();

        long currentTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis() + COLLECTOR_PERIOD;
        while (currentTime <= endTime) {
            log.info("-----------------------------------------------------------------------------");
            log.info("Collector metrics from hosts: {}\n", new Date());
            for (HostSnmpConnectionInfo host : hosts) {
                collector.hostCollectedMetrics.add(collector.collect(host));
            }
            Thread.sleep(TimeUnit.SECONDS.toMillis(1)); // wait to do next collection
            currentTime = System.currentTimeMillis();
        }

        log.info("-----------------------------------------------------------------------------");
        log.info("Snmp Application Has Been Finished with execution time {} second", (System.currentTimeMillis() - appStartTime) / 1000);

        log.info("--------------------------------- Draw Charts --------------------------------------------");
        ChartUtil.saveAndShowResults(collector.hostCollectedMetrics, true);
    }

    public Metric collect(HostSnmpConnectionInfo host) {
        try {
            // Create TransportMapping and Listen
            TransportMapping transport = new DefaultUdpTransportMapping();
            transport.listen();

            // Create Target Address object
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(host.getRCommunity()));
            target.setVersion(SNMP_VERSION);
            target.setAddress(new UdpAddress(host.getIp() + "/" + host.getPort()));
            target.setRetries(1);
            target.setTimeout(host.getTimeout());

            // Create the PDU object
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(Sys_UPTIME_OID)));
            pdu.setType(PDU.GET);
            pdu.setRequestID(new Integer32(1));

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
        return new Metric(null, MetricType.UPTIME, host);
    }

    public Metric readMetric(HostSnmpConnectionInfo host, PDU responsePDU) {
        try {
            List<VariableBinding> uptimeVar = responsePDU.get(Sys_UPTIME_OID);
            if (uptimeVar.size() == 1) {
                long uptime = uptimeVar.get(0).getVariable().toLong();
                return new Metric(uptime, MetricType.UPTIME, host);
            }
        } catch (Exception e) {
            log.error("Could not read SNMP response from host {}", host.getAddress());
        }
        return new Metric(null, MetricType.UPTIME, host);
    }

}

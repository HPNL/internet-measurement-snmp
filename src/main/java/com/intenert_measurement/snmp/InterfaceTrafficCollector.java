package com.intenert_measurement.snmp;

import com.intenert_measurement.snmp.metric.Metric;
import com.intenert_measurement.snmp.util.HostSnmpConnectionInfo;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.smi.Integer32;

import java.util.*;

@Slf4j
public class InterfaceTrafficCollector {

    public static void main(String[] args) throws Exception {

        List<HostSnmpConnectionInfo> hosts = new ArrayList<>();
        Map<Integer, HostSnmpConnectionInfo> hostIndices = new HashMap<>();
        Map<HostSnmpConnectionInfo, List<Metric>> hostTraffic = new HashMap<>();

        hosts.add(new HostSnmpConnectionInfo("vm1", "10.168.130.216", 161, 100, "public"));
        //        hosts.add(new HostSnmpConnectionInfo("vm2", "127.0.0.1", 1162, 100, "public"));

        SnmpCollector snmpCollector = SnmpCollector.ofTable(Arrays.asList(Configuration.IP_ADDR_TABLE_IP, Configuration.IP_ADDR_TABLE_INDEX));
        Map<String, Object> metrics = snmpCollector.collectTable(hosts);

        // find host if index
        for (HostSnmpConnectionInfo host : hosts) {
            String hostIp = metrics.get(Configuration.IP_ADDR_TABLE_IP + "." + host.getIp()).toString();
            Integer hostIfIndex = ((Integer32) metrics.get(Configuration.IP_ADDR_TABLE_INDEX + "." + host.getIp())).getValue();
            hostIndices.put(hostIfIndex, host);
        }

        // read if table values
        for (Map.Entry<Integer, HostSnmpConnectionInfo> entry : hostIndices.entrySet()) {
            snmpCollector = new SnmpCollector(Arrays.asList(
                    Configuration.IF_IN_OCTETS + "." + entry.getKey(),
                    Configuration.IF_IN_UCAST_PKTS + "." + entry.getKey(),
                    Configuration.IF_IN_NUCAST_PKTS + "." + entry.getKey(),
                    Configuration.IF_OUT_OCTETS + "." + entry.getKey(),
                    Configuration.IF_OUT_UCAST_PKTS + "." + entry.getKey(),
                    Configuration.IF_OUT_NUCAST_PKTS + "." + entry.getKey()
            )
            );
            List<Metric> inOctValues = snmpCollector.collectSingleOID(Collections.singletonList(entry.getValue()));
            hostTraffic.put(entry.getValue(), inOctValues);

        }
        log.info("--------------------------------- Draw Charts --------------------------------------------");
        //        ChartUtil.saveAndShowResults(metrics, true);
    }
}

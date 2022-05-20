package com.intenert_measurement.snmp.ifnet;

import com.intenert_measurement.snmp.Configuration;
import com.intenert_measurement.snmp.collector.SnmpCollector;
import com.intenert_measurement.snmp.metric.Metric;
import com.intenert_measurement.snmp.util.HostSnmpConnectionInfo;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.smi.Integer32;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class InterfaceTrafficCollector {

    public static void main(String[] args) throws Exception {

        InterfaceTrafficCollector trafficCollector = new InterfaceTrafficCollector();
        List<HostSnmpConnectionInfo> hosts = new ArrayList<>();
        hosts.add(new HostSnmpConnectionInfo("vm1", "10.168.130.216", 161, 100, "public"));
        //        hosts.add(new HostSnmpConnectionInfo("vm2", "127.0.0.1", 1162, 100, "public"));

        //        trafficCollector.collectHostInterface(hosts);

        trafficCollector.collectHostAllInterface(hosts);
    }

    private void collectHostInterface(List<HostSnmpConnectionInfo> hosts) throws Exception {
        Map<Integer, HostSnmpConnectionInfo> hostIndices = new HashMap<>();
        Map<HostSnmpConnectionInfo, List<Metric>> hostTraffic = new HashMap<>();

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

    private void collectHostAllInterface(List<HostSnmpConnectionInfo> hosts) throws Exception {
        Map<Integer, HostSnmpConnectionInfo> hostIndices = new HashMap<>();
        Map<HostSnmpConnectionInfo, List<InterfaceMetrics>> hostTraffic = new HashMap<>();

        SnmpCollector snmpCollector = SnmpCollector.ofTable(Arrays.asList(Configuration.IP_ADDR_TABLE_IP, Configuration.IP_ADDR_TABLE_INDEX));

        // find host if index
        for (HostSnmpConnectionInfo host : hosts) {
            Map<String, Object> metrics = snmpCollector.collectTable(Collections.singletonList(host));
            //            Map<String, String> ipList = metrics.entrySet().stream().filter(x -> x.getValue() instanceof IpAddress).collect(Collectors.toMap(
            //                    Map.Entry::getKey,
            //                    y -> y.getValue().toString()
            //            ));
            Map<String, String> indexList = metrics.entrySet().stream().filter(x -> x.getValue() instanceof Integer32).collect(Collectors.toMap(
                    Map.Entry::getKey,
                    y -> y.getValue().toString()
            ));
            hostIndices.put(null, host);

            List<InterfaceMetrics> interfaceMetrics = new ArrayList<>();
            for (Map.Entry<String, String> entry : indexList.entrySet()) {
                String ip = entry.getKey().replace(Configuration.IP_ADDR_TABLE_INDEX + ".", "");
                String index = entry.getValue();
                // read if table values
                snmpCollector = new SnmpCollector(Arrays.asList(
                        Configuration.IF_IN_OCTETS + "." + index,
                        Configuration.IF_IN_UCAST_PKTS + "." + index,
                        Configuration.IF_IN_NUCAST_PKTS + "." + index,
                        Configuration.IF_OUT_OCTETS + "." + index,
                        Configuration.IF_OUT_UCAST_PKTS + "." + index,
                        Configuration.IF_OUT_NUCAST_PKTS + "." + index
                ));
                interfaceMetrics.add(new InterfaceMetrics(ip, index, snmpCollector.collectSingleOID(Collections.singletonList(host))));
            }
            hostTraffic.put(host, interfaceMetrics);
        }
        log.info("--------------------------------- Draw Charts --------------------------------------------");
        //        ChartUtil.saveAndShowResults(metrics, true);
    }

}

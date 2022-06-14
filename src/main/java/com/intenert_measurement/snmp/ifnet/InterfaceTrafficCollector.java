package com.intenert_measurement.snmp.ifnet;

import com.intenert_measurement.snmp.Configuration;
import com.intenert_measurement.snmp.chart.ChartUtil;
import com.intenert_measurement.snmp.collector.SnmpCollector;
import com.intenert_measurement.snmp.metric.Metric;
import com.intenert_measurement.snmp.metric.MetricType;
import com.intenert_measurement.snmp.util.HostSnmpConnectionInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.snmp4j.smi.Integer32;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class InterfaceTrafficCollector {

    public static void main(String[] args) throws Exception {

        // args as ip port
        if (ArrayUtils.isEmpty(args) || args.length % 2 != 0) {
            System.out.println("Enter input as list of ip port of your hosts" +
                    "e,.g. 10.0.0.1 161 10.0.0.2 162 10.0.0.2 163");
            System.exit(1);
        }

        // create host list
        List<List<String>> subSets = ListUtils.partition(Arrays.asList(args), 2);
        List<HostSnmpConnectionInfo> hosts = new ArrayList<>();
        for (List<String> ipPortPair : subSets) {
            String ip = ipPortPair.get(0);
            String port = ipPortPair.get(1);
            hosts.add(new HostSnmpConnectionInfo(ip + ":" + port, ip, NumberUtils.toInt(port), 100, "public"));
        }

        // collect host traffic
        InterfaceTrafficCollector trafficCollector = new InterfaceTrafficCollector();
        Map<HostSnmpConnectionInfo, List<InterfaceMetrics>> ifTraffics = trafficCollector.collectHostAllInterface(hosts, (x) -> x != null && x.toLowerCase().startsWith("switch"));

        for (Map.Entry<HostSnmpConnectionInfo, List<InterfaceMetrics>> entry : ifTraffics.entrySet()) {
            for (InterfaceMetrics interfaceMetrics : entry.getValue()) {
                ChartUtil.saveAndShowResults(interfaceMetrics.getMetrics(), true);
            }
        }
        System.out.println("Finish collector");
    }

    private Map<HostSnmpConnectionInfo, List<Metric>> collectHostInterface(List<HostSnmpConnectionInfo> hosts) throws Exception {
        Map<Integer, HostSnmpConnectionInfo> hostIndices = new HashMap<>();
        Map<HostSnmpConnectionInfo, List<Metric>> hostTraffic = new HashMap<>();

        SnmpCollector snmpCollector = SnmpCollector.ofTable(new HashMap<String, MetricType>() {
            {
                put(Configuration.IP_ADDR_TABLE_IP, MetricType.IF_IP);
                put(Configuration.IP_ADDR_TABLE_INDEX, MetricType.IF_INDEX);
            }
        });
        Map<String, Object> metrics = snmpCollector.collectTable(hosts);

        // find host if index
        for (HostSnmpConnectionInfo host : hosts) {
            String hostIp = metrics.get(Configuration.IP_ADDR_TABLE_IP + "." + host.getIp()).toString();
            Integer hostIfIndex = ((Integer32) metrics.get(Configuration.IP_ADDR_TABLE_INDEX + "." + host.getIp())).getValue();
            hostIndices.put(hostIfIndex, host);
        }

        // read if table values
        for (Map.Entry<Integer, HostSnmpConnectionInfo> entry : hostIndices.entrySet()) {
            snmpCollector = new SnmpCollector(
                    new HashMap<String, MetricType>() {
                        {
                            put(Configuration.IF_IN_OCTETS + "." + entry.getKey(), MetricType.IF_IN_TRAFFIC);
                            put(Configuration.IF_OUT_OCTETS + "." + entry.getKey(), MetricType.IF_OUT_TRAFFIC);
                            //put(Configuration.IF_IN_UCAST_PKTS + "." + entry.getKey(), MetricType.IF);
                            //put(Configuration.IF_IN_NUCAST_PKTS + "." + entry.getKey(), MetricType.IF);
                            //put(Configuration.IF_OUT_UCAST_PKTS + "." + entry.getKey(), MetricType.IF);
                            //put(Configuration.IF_OUT_NUCAST_PKTS + "." + entry.getKey(),MetricType.IF);
                        }
                    }
            );
            List<Metric> inOctValues = snmpCollector.collectSingleOID(Collections.singletonList(entry.getValue()));
            hostTraffic.put(entry.getValue(), inOctValues);

        }
        return hostTraffic;
    }

    private Map<HostSnmpConnectionInfo, List<InterfaceMetrics>> collectHostAllInterface(
            List<HostSnmpConnectionInfo> hosts,
            Function<String, Boolean> interfaceNameFilter
    ) throws Exception {
        Map<Integer, HostSnmpConnectionInfo> hostIndices = new HashMap<>();
        Map<HostSnmpConnectionInfo, List<InterfaceMetrics>> hostTraffic = new HashMap<>();

        SnmpCollector snmpCollector = SnmpCollector.ofTable(
                new HashMap<String, MetricType>() {
                    {
                        put(Configuration.IF_NAME, MetricType.IF_NAME);
                        put(Configuration.IF_INDEX, MetricType.IF_INDEX);
                    }
                }
        );
        // find host if index
        for (HostSnmpConnectionInfo host : hosts) {
            Map<String, Object> metrics = snmpCollector.collectTable(Collections.singletonList(host));
            Map<String, String> indexList = new HashMap<>();
            for (String index : metrics.keySet().stream().filter(x -> x.startsWith(Configuration.IF_INDEX)).collect(Collectors.toSet())) {
                String ifName = String.valueOf(metrics.get(Configuration.IF_NAME + "." + metrics.get(index)));
                if (interfaceNameFilter.apply(ifName)) {
                    indexList.put(ifName, String.valueOf(metrics.get(index)));
                }
            }
            hostIndices.put(null, host);

            List<InterfaceMetrics> interfaceMetrics = new ArrayList<>();
            for (Map.Entry<String, String> entry : indexList.entrySet()) {
                String ifName = entry.getKey();
                String index = entry.getValue();
                // read if table values
                snmpCollector = new SnmpCollector(new HashMap<String, MetricType>() {
                    {
                        put(Configuration.IF_IN_OCTETS + "." + index, MetricType.IF_IN_TRAFFIC);
                        put(Configuration.IF_OUT_OCTETS + "." + index, MetricType.IF_OUT_TRAFFIC);
                        //put(Configuration.IF_IN_UCAST_PKTS + "." + entry.getKey(), MetricType.IF);
                        //put(Configuration.IF_IN_NUCAST_PKTS + "." + entry.getKey(), MetricType.IF);
                        //put(Configuration.IF_OUT_UCAST_PKTS + "." + entry.getKey(), MetricType.IF);
                        //put(Configuration.IF_OUT_NUCAST_PKTS + "." + entry.getKey(),MetricType.IF);
                    }
                }
                );
                interfaceMetrics.add(new InterfaceMetrics(ifName, index, snmpCollector.collectSingleOID(Collections.singletonList(host))));
            }
            hostTraffic.put(host, interfaceMetrics);
        }
        return hostTraffic;
    }

}

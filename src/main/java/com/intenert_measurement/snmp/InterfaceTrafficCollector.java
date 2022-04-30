package com.intenert_measurement.snmp;

import com.intenert_measurement.snmp.chart.ChartUtil;
import com.intenert_measurement.snmp.metric.Metric;
import com.intenert_measurement.snmp.util.HostSnmpConnectionInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class InterfaceTrafficCollector {

    public static void main(String[] args) throws Exception {

        List<HostSnmpConnectionInfo> hosts = new ArrayList<>();

        hosts.add(new HostSnmpConnectionInfo("vm1", "127.0.0.1", 161, 100, "public"));
        hosts.add(new HostSnmpConnectionInfo("vm2", "127.0.0.1", 1162, 100, "public"));

        SnmpCollector snmpCollector = new SnmpCollector(Arrays.asList(Configuration.IF_IN_NUCAST_PKTS, Configuration.IF_IN_UCAST_PKTS));
        List<Metric> metrics = snmpCollector.collect(hosts);

        log.info("--------------------------------- Draw Charts --------------------------------------------");
        ChartUtil.saveAndShowResults(metrics, true);
    }
}

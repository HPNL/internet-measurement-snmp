package com.intenert_measurement.snmp;

import com.intenert_measurement.snmp.chart.ChartUtil;
import com.intenert_measurement.snmp.metric.Metric;
import com.intenert_measurement.snmp.metric.MetricType;
import com.intenert_measurement.snmp.util.HostSnmpConnectionInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class FailureMetricCollector {

    public static void main(String[] args) throws Exception {

        List<HostSnmpConnectionInfo> hosts = new ArrayList<>();

        hosts.add(new HostSnmpConnectionInfo("vm1", "127.0.0.1", 161, 100, "public"));
        hosts.add(new HostSnmpConnectionInfo("vm2", "127.0.0.1", 1162, 100, "public"));

        SnmpCollector snmpCollector = new SnmpCollector(Collections.singletonList(Configuration.Sys_UPTIME_OID));
        List<Metric> metrics = snmpCollector.collectSingleOID(hosts);
        metrics.stream().peek(x -> x.setType(MetricType.UPTIME));

        log.info("--------------------------------- Draw Charts --------------------------------------------");
        ChartUtil.saveAndShowResults(metrics, true);
    }
}

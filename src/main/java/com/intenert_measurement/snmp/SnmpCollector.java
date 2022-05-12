package com.intenert_measurement.snmp;

import com.intenert_measurement.snmp.metric.Metric;
import com.intenert_measurement.snmp.util.HostSnmpConnectionInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SnmpCollector {

    private final List<String> oids;
    private final boolean isTable;

    public SnmpCollector(List<String> oids) {
        this.oids = oids;
        this.isTable = false;
    }

    public static SnmpCollector ofTable(List<String> oids) {
        return new SnmpCollector(oids, true);
    }

    public List<Metric> collectSingleOID(List<HostSnmpConnectionInfo> hosts) throws Exception {

        final List<Metric> hostCollectedMetrics = new ArrayList<>();

        long appStartTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis() + Configuration.COLLECTOR_PERIOD;
        while (currentTime <= endTime) {
            log.info("-----------------------------------------------------------------------------");
            log.info("Collector metrics from hosts: {}\n", new Date());
            for (HostSnmpConnectionInfo host : hosts) {
                Map<String, Object> result = CollectorUtil.collect(host, oids, isTable);
                hostCollectedMetrics.addAll(result.values().stream().map(number -> new Metric(number, null, new Date(), host)).collect(Collectors.toList()));
            }
            Thread.sleep(TimeUnit.SECONDS.toMillis(1)); // wait to do next collection
            currentTime = System.currentTimeMillis();
        }
        log.info("-----------------------------------------------------------------------------");
        log.info("Snmp Collector has been finished with execution time {} second", (System.currentTimeMillis() - appStartTime) / 1000);

        return hostCollectedMetrics;
    }

    public Map<String, Object> collectTable(List<HostSnmpConnectionInfo> hosts) throws Exception {
        Map<String, Object> result = new HashMap<>();
        long appStartTime = System.currentTimeMillis();
        log.info("-----------------------------------------------------------------------------");
        log.info("Start SNMP Collector to Measure Failure Metrics: MTTR, MTBF, MTTF\n");

        long currentTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis() + Configuration.COLLECTOR_PERIOD;
        while (currentTime <= endTime) {
            log.info("-----------------------------------------------------------------------------");
            log.info("Collector metrics from hosts: {}\n", new Date());
            for (HostSnmpConnectionInfo host : hosts) {
                result.putAll(CollectorUtil.collect(host, oids, isTable));
            }
            Thread.sleep(TimeUnit.SECONDS.toMillis(1)); // wait to do next collection
            currentTime = System.currentTimeMillis();
        }
        log.info("-----------------------------------------------------------------------------");
        log.info("Snmp Collector has been finished with execution time {} second", (System.currentTimeMillis() - appStartTime) / 1000);

        return result;
    }
}

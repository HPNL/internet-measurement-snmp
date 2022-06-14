package com.intenert_measurement.snmp.collector;

import com.intenert_measurement.snmp.Configuration;
import com.intenert_measurement.snmp.metric.Metric;
import com.intenert_measurement.snmp.metric.MetricType;
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

    private final Map<String, MetricType> oids;
    private final boolean isTable;

    public SnmpCollector(Map<String, MetricType> oids) {
        this.oids = oids;
        this.isTable = false;
    }

    public static SnmpCollector ofTable(Map<String, MetricType> oids) {
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
                Map<String, Object> result = CollectorUtil.collect(host, new ArrayList<>(oids.keySet()), isTable);
                hostCollectedMetrics.addAll(result.entrySet().stream().map(x -> new Metric(
                        x.getKey(),
                        x.getValue(),
                        oids.get(x.getKey()),
                        new Date(),
                        host
                )).collect(Collectors.toList()));
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
                result.putAll(CollectorUtil.collect(host, new ArrayList<>(oids.keySet()), isTable));
            }
            Thread.sleep(TimeUnit.SECONDS.toMillis(1)); // wait to do next collection
            currentTime = System.currentTimeMillis();
        }
        log.info("-----------------------------------------------------------------------------");
        log.info("Snmp Collector has been finished with execution time {} second", (System.currentTimeMillis() - appStartTime) / 1000);

        return result;
    }
}

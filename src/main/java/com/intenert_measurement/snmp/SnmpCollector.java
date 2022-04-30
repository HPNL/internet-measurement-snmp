package com.intenert_measurement.snmp;

import com.intenert_measurement.snmp.metric.Metric;
import com.intenert_measurement.snmp.util.HostSnmpConnectionInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class SnmpCollector {

    private final List<String> oids;

    public SnmpCollector(List<String> oids) {
        this.oids = oids;
    }

    private final List<Metric> hostCollectedMetrics = new ArrayList<>();

    public List<Metric> collect(List<HostSnmpConnectionInfo> hosts) throws Exception {

        long appStartTime = System.currentTimeMillis();
        log.info("-----------------------------------------------------------------------------");
        log.info("Start SNMP Collector to Measure Failure Metrics: MTTR, MTBF, MTTF\n");

        long currentTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis() + Configuration.COLLECTOR_PERIOD;
        while (currentTime <= endTime) {
            log.info("-----------------------------------------------------------------------------");
            log.info("Collector metrics from hosts: {}\n", new Date());
            for (HostSnmpConnectionInfo host : hosts) {
                Map<String, Number> result = CollectorUtil.collect(host, oids);
                hostCollectedMetrics.addAll(result.values().stream().map(number -> new Metric(number, null, new Date(), host)).collect(Collectors.toList()));
            }
            Thread.sleep(TimeUnit.SECONDS.toMillis(1)); // wait to do next collection
            currentTime = System.currentTimeMillis();
        }
        log.info("-----------------------------------------------------------------------------");
        log.info("Snmp Application Has Been Finished with execution time {} second", (System.currentTimeMillis() - appStartTime) / 1000);

        return hostCollectedMetrics;
    }
}

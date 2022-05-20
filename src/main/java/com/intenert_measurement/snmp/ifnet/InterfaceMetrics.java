package com.intenert_measurement.snmp.ifnet;

import com.intenert_measurement.snmp.metric.Metric;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class InterfaceMetrics {
    private String ip;
    private String index;
    private List<Metric> metrics;
}

package com.intenert_measurement.snmp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
public class Metric {

    private Number value;
    private MetricType type;
    private Date timestamp;
    private HostSnmpConnectionInfo host;

    public Metric(Long value, MetricType type, HostSnmpConnectionInfo host) {
        this.value = value;
        this.host = host;
        this.type = type;
        this.timestamp = new Date();
    }

    public int getAvailability() {
        return value != null ? 100 : 0;
    }

    public Metric clone() {
        return new Metric(this.value, this.type, this.timestamp, this.host);
    }
}


package com.intenert_measurement.snmp.metric;

import com.intenert_measurement.snmp.util.HostSnmpConnectionInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
public class Metric {

    private String oid;
    private Object value;
    private MetricType type;
    private Date timestamp;
    private HostSnmpConnectionInfo host;

    public Metric(String oid, Object value, MetricType type, HostSnmpConnectionInfo host) {
        this.oid = oid;
        this.value = value;
        this.host = host;
        this.type = type;
        this.timestamp = new Date();
    }

    public int getAvailability() {
        return value != null ? 100 : 0;
    }

    public Metric clone() {
        return new Metric(this.oid, this.value, this.type, this.timestamp, this.host);
    }

    public Number toNumber() {
        try {
            return NumberUtils.toDouble(String.valueOf(value));
        } catch (Exception ignore) {
        }
        return null;
    }
}


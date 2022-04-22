package com.intenert_measurement.snmp.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class HostSnmpConnectionInfo {

    private String name;
    private String ip;
    private int port;
    private int timeout;
    private String rCommunity;

    public String getAddress() {
        return ip + ":" + port;
    }
}

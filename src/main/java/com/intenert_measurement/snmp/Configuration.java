package com.intenert_measurement.snmp;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.snmp4j.mp.SnmpConstants;

import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Configuration {

    public static final String Sys_UPTIME_OID = ".1.3.6.1.2.1.1.3.0";
    public static final int SNMP_VERSION = SnmpConstants.version2c;

    public static final long COLLECTOR_PERIOD = TimeUnit.SECONDS.toMillis(10);

    public static final String IF_INDEX = "1.3.6.1.2.1.2.2.1.1";
    public static final String IFX_TABLE_INDEX = "1.3.6.1.2.1.31.1.1.1.1";
    public static final String IP_ADDR_TABLE_IP = "1.3.6.1.2.1.4.20.1.1";
    public static final String IP_ADDR_TABLE_INDEX = "1.3.6.1.2.1.4.20.1.2";
    public static final String IF_IN_OCTETS = "1.3.6.1.2.1.2.2.1.10";
    public static final String IF_IN_UCAST_PKTS = "1.3.6.1.2.1.2.2.1.12";
    public static final String IF_IN_NUCAST_PKTS = "1.3.6.1.2.1.2.2.1.12";
}

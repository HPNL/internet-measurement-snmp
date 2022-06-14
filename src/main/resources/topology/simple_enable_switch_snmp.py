#!/usr/bin/python
from mininet.net import Mininet
from mininet.node import Controller
from mininet.cli import CLI
from mininet.link import Intf
from mininet.log import setLogLevel, info
import os


def simple_enable_switch_snmp():
    net = Mininet(topo=None)
    info("*** Adding controller\n")
    c0 = net.addController(name="c0")
    info("*** Add switches\n")
    s1 = net.addSwitch("switch1", ip="10.0.10.0/24")

    info("*** Add hosts\n")
    h1 = net.addHost("h1", ip="10.0.0.1/24")
    h2 = net.addHost("h2", ip="10.0.0.2/24")

    # we report only an example for each network level
    info("*** Add links\n")
    net.addLink(h1, s1, bw=100)
    net.addLink(h2, s1, bw=110)

    info("*** Starting network\n")
    net.start()
    enableSTP()
    info("*** Starting SNMP agent in switch {0}\n", s1)
    s1.cmd("/usr/sbin/snmpd - Lsd - Lf/dev/null - u snmp - I - smux - p/var/run/snmpd.pid - c/etc/snmp/snmpd.conf")

    net.pingAll()
    CLI(net)
    net.stop()


# in this function we enable the spanning tree algorithm for all switches
def enableSTP():
    cmd = "ovs-vsctl set Bridge s1 stp_enable=true"
    os.system(cmd)
    print(cmd)
    # for x in range(1, 5):
    #     cmd = "ovs-vsctl set Bridge %s stp_enable=true" % ("s1" + str(x))
    #     os.system(cmd)
    #     print(cmd)
    #     cmd = "ovs-vsctl set Bridge %s stp_enable=true" % ("s2" + str(x))
    #     os.system(cmd)
    #     print(cmd)


# with this function we start the application
if __name__ == "__main__":
    setLogLevel("info")
    simple_enable_switch_snmp()

from mininet.topo import Topo
from mininet.net import Mininet
from mininet.log import setLogLevel, info
from mininet.cli import CLI
import networkx as nx


class CustomTopology(Topo):
    def __init__(self, hosts, links):
        Topo.__init__(self)

        self.myNet = nx.DiGraph()
        for h in hosts:
            hName = "h{}".format(h)
            self.myNet.add_node(h, name=hName, eths=list(), ips=list(), macs=list())
            mh = self.addHost(hName, ip=None)

        for l in links:
            h1Name = self.myNet.nodes[l[0]]["name"]
            h2Name = self.myNet.nodes[l[1]]["name"]
            h1EthNum = len(self.myNet.nodes[l[0]]["eths"])
            h2EthNum = len(self.myNet.nodes[l[1]]["eths"])
            eth1Name = "{}-eth{}".format(h1Name, h1EthNum)
            eth2Name = "{}-eth{}".format(h2Name, h2EthNum)
            ip1, mac1 = self.generateIpMac(l[0], h1EthNum + 1)
            ip2, mac2 = self.generateIpMac(l[1], h2EthNum + 1)
            self.myNet.add_edge(l[0], l[1], eth1=eth1Name, eth2=eth2Name, ip1=ip1, ip2=ip2, mac1=mac1, mac2=mac2)
            self.myNet.add_edge(l[1], l[0], eth1=eth2Name, eth2=eth1Name, ip1=ip2, ip2=ip1, mac1=mac2, mac2=mac1)
            self.myNet.nodes[l[0]]["eths"].append(eth1Name)
            self.myNet.nodes[l[1]]["eths"].append(eth2Name)
            self.myNet.nodes[l[0]]["ips"].append(ip1)
            self.myNet.nodes[l[1]]["ips"].append(ip2)
            self.myNet.nodes[l[0]]["macs"].append(mac1)
            self.myNet.nodes[l[1]]["macs"].append(mac2)
            self.addLink(h1Name, h2Name,
                         params1={'ip': "{}/24".format(ip1)},
                         params2={'ip': "{}/24".format(ip2)})

    def generateIpMac(self, hostId, ethId):
        hostIdHex = hex(hostId)[2:]
        hostIdHex = hostIdHex if len(hostIdHex) == 2 else '0' + hostIdHex
        ethIdHex = hex(ethId)[2:]
        ethIdHex = ethIdHex if len(ethIdHex) == 2 else '0' + ethIdHex
        ip = "10.0.{}.{}".format(hostIdHex, ethIdHex)
        mac = "00:00:00:00:{}:{}".format(hostIdHex, ethIdHex)
        return ip, mac


def run(hosts, links, paths):
    SNMP_START_CMD = "/usr/sbin/snmpd -Lsd -Lf /dev/null -u snmp -I -smux -p /var/run/snmpd.pid -c /etc/snmp/snmpd.conf"
    topo = CustomTopology(hosts, links)
    net = Mininet(topo)
    net.start()

    for e in topo.myNet.edges():
        h = e[0]
        hName = "h{}".format(h)
        eth = topo.myNet[e[0]][e[1]]["eth1"]
        mac = topo.myNet[e[0]][e[1]]["mac1"]
        macCmd = "ip link set dev {} address {}".format(eth, mac)
        print(h, macCmd)
        info(net[hName].cmd(macCmd))

    for i in range(len(paths)):
        path1 = paths[i]
        path2 = path1[::-1]
        for path in [path1, path2]:
            node1 = path[-2]
            node2 = path[-1]
            dstIp = topo.myNet[node1][node2]["ip2"]
            for j in range(len(path) - 1):
                l1 = path[j]
                l2 = path[j + 1]
                viaIp = topo.myNet[l1][l2]["ip1"]
                viaEth = topo.myNet[l1][l2]["eth1"]
                staticRoute = "ip route add {} via {} dev {}".format(dstIp, viaIp, viaEth)
                l1Name = topo.myNet.nodes[l1]["name"]
                print(l1Name, staticRoute)
                info(net[l1Name].cmd(staticRoute))
                viaMac = topo.myNet[l1][l2]["mac2"]
                staticArp = "arp -i {} -s {} {}".format(viaEth, dstIp, viaMac)
                print(l1Name, staticArp)
                info(net[l1Name].cmd(staticArp))

    for h in hosts:
        net["h{}".format(h)].cmd('sysctl net.ipv4.ip_forward=1')
        net["h{}".format(h)].cmd(SNMP_START_CMD)

    # net.pingAll()
    CLI(net)
    net.stop()


if __name__ == "__main__":
    setLogLevel('info')

    #       H1
    #       L1
    # H2 L2 H3 L3 H4
    #       L4
    #       H5

    hosts = [1, 2, 3, 4, 5]
    links = [
        [1, 3],
        [2, 3],
        [3, 4],
        [3, 5]
    ]
    paths = [
        [1, 3, 2],
        [1, 3],
        [1, 3, 4],
        [1, 3, 5]
    ]

    run(hosts, links, paths)

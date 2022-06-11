from ipaddress import ip_address
from turtle import position
from mininet.topo import Topo
from mininet.net import Mininet
from mininet.node import Node
from mininet.log import setLogLevel, info
from mininet.cli import CLI
import numpy as np


class LinuxRouter(Node):
    def config(self, **params):
        super(LinuxRouter, self).config(**params)
        self.cmd('sysctl net.ipv4.ip_forward=1')

    def terminate(self):
        self.cmd('sysctl net.ipv4.ip_forward=0')
        super(LinuxRouter, self).terminate()


class CustomTopology(Topo):

    def __init__(self):
        # Initialize topology
        Topo.__init__(self)

        scale = 10
        positions = {1: scale * np.array([0.79460614, 0.51128642]),
                     2: scale * np.array([0.90050535, -0.04279148]),
                     3: scale * np.array([0.1813443, 0.49869866]),
                     4: scale * np.array([0.52567252, -0.66597264]),
                     5: scale * np.array([0.24121387, 0.15728566])
                     }

        shortest_path = [[1, 5, 2], [1, 5, 3], [1, 5, 4], [2, 5, 1], [2, 5, 3], [2, 5, 4], [3, 5, 1], [3, 5, 2], [3, 5, 4], [4, 5, 1], [4, 5, 2], [4, 5, 3]]

        # Adding hosts
        h1 = self.addHost('h1', cls=LinuxRouter, position=positions[1], ip=None)
        h2 = self.addHost('h2', cls=LinuxRouter, position=positions[2], ip=None)
        h3 = self.addHost('h3', cls=LinuxRouter, position=positions[3], ip=None)
        h4 = self.addHost('h4', cls=LinuxRouter, position=positions[4], ip=None)
        s5 = self.addSwitch('s5', cls=LinuxRouter, position=positions[5], ip=None)

        # Adding links between hosts
        self.addLink(h1, s5, bw=110, params1={'ip': '10.0.1.2/24'}, params2={'ip': '10.0.1.1/24'})
        self.addLink(h2, s5, bw=160, params1={'ip': '10.0.2.2/24'}, params2={'ip': '10.0.2.1/24'})
        self.addLink(h3, s5, bw=280, params1={'ip': '10.0.3.2/24'}, params2={'ip': '10.0.3.1/24'})
        self.addLink(h4, s5, bw=60, params1={'ip': '10.0.4.2/24'}, params2={'ip': '10.0.4.1/24'})
        self.addLink(s5, h1, bw=100, params1={'ip': '10.0.1.1/24'}, params2={'ip': '10.0.1.2/24'})
        self.addLink(s5, h2, bw=200, params1={'ip': '10.0.2.1/24'}, params2={'ip': '10.0.2.2/24'})
        self.addLink(s5, h3, bw=240, params1={'ip': '10.0.3.1/24'}, params2={'ip': '10.0.3.2/24'})
        self.addLink(s5, h4, bw=60, params1={'ip': '10.0.4.1/24'}, params2={'ip': '10.0.4.2/24'})


def run():
    topo = CustomTopology()
    net = Mininet(topo)
    net.start()

    net.pingAll()

    # info('*** Add routing for reaching networks that are not directly connected \n')

    # [1, 5, 2]
    info(net['h1'].cmd('ip route add 10.0.1.1 via 10.0.1.2 dev h1-eth0'))
    info(net['s5'].cmd('ip route add 10.0.2.2 via 10.0.2.1 dev s5-eth2'))

    # [1, 5, 3]
    info(net['h1'].cmd('ip route add 10.0.1.1 via 10.0.1.2 dev h1-eth0'))
    info(net['s5'].cmd('ip route add 10.0.3.2 via 10.0.3.1 dev s5-eth3'))
    # [1, 5, 4]
    info(net['h1'].cmd('ip route add 10.0.1.1 via 10.0.1.2 dev h1-eth0'))
    info(net['s5'].cmd('ip route add 10.0.4.2 via 10.0.4.1 dev s5-eth4'))
    # [2, 5, 1]
    info(net['h2'].cmd('ip route add 10.0.2.1 via 10.0.2.2 dev h2-eth0'))
    info(net['s5'].cmd('ip route add 10.0.1.2 via 10.0.1.1 dev s5-eth1'))
    # [2, 5,3]
    info(net['h2'].cmd('ip route add 10.0.2.1 via 10.0.2.2 dev h2-eth0'))
    info(net['s5'].cmd('ip route add 10.0.3.2 via 10.0.3.1 dev s5-eth3'))
    # [2, 5, 4]
    info(net['h2'].cmd('ip route add 10.0.2.1 via 10.0.2.2 dev h2-eth0'))
    info(net['s5'].cmd('ip route add 10.0.4.2 via 10.0.4.1 dev s5-eth4'))
    # [3, 5, 1]
    info(net['h3'].cmd('ip route add 10.0.3.1 via 10.0.3.2 dev h3-eth0'))
    info(net['s5'].cmd('ip route add 10.0.1.2 via 10.0.1.1 dev s5-eth1'))
    # [3, 5, 2]
    info(net['h3'].cmd('ip route add 10.0.3.1 via 10.0.3.2 dev h3-eth0'))
    info(net['s5'].cmd('ip route add 10.0.2.2 via 10.0.2.1 dev s5-eth2'))
    # 3, 5, 4]
    info(net['h3'].cmd('ip route add 10.0.3.1 via 10.0.3.2 dev h3-eth0'))
    info(net['s5'].cmd('ip route add 10.0.4.2 via 10.0.4.1 dev s5-eth4'))
    # [4, 5, 1]
    info(net['h4'].cmd('ip route add 10.0.4.1 via 10.0.4.2 dev h4-eth0'))
    info(net['s5'].cmd('ip route add 10.0.1.2 via 10.0.1.1 dev s5-eth1'))
    # [4, 5, 2]
    info(net['h4'].cmd('ip route add 10.0.4.1 via 10.0.4.2 dev h4-eth0'))
    info(net['s5'].cmd('ip route add 10.0.2.2 via 10.0.2.1 dev s5-eth2'))
    # [4, 5, 3]]
    info(net['h4'].cmd('ip route add 10.0.4.1 via 10.0.4.2 dev h4-eth0'))
    info(net['s5'].cmd('ip route add 10.0.3.2 via 10.0.3.1 dev s5-eth3'))

    CLI(net)
    net.stop()


if _name_ == "__main__":
    setLogLevel('info')
    run()

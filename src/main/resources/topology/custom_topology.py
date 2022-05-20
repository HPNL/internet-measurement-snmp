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
                     2: scale * np.array([ 0.90050535, -0.04279148]),
                     3: scale * np.array([0.1813443 , 0.49869866]),
                     4: scale * np.array([ 0.52567252, -0.66597264]),
                     5: scale * np.array([0.24121387, 0.15728566]),
                     6: scale * np.array([-0.42900197,  0.29992649]),
                     7: scale * np.array([0.52016318, 1.        ]),
                     8: scale * np.array([-0.92774328,  0.34664684]),
                     9: scale * np.array([0.44551648, 0.57269363]),
                     10: scale * np.array([-0.42442404, -0.07174852]),
                     11: scale * np.array([ 0.06260727, -0.96158746]),
                     12: scale * np.array([-0.45761849, -0.62463164]),
                     13: scale * np.array([-0.76996238, -0.22206278]),
                     14: scale * np.array([-0.66287894, -0.79774318])}

        shortest_path = [[1, 2], [1, 3], [1, 2, 4], [1, 2, 4, 5], [1, 3, 6], [1, 2, 4, 5, 7], [1, 3, 6, 8], [1, 9], [1, 9, 10], [1, 2, 4, 11], [1, 9, 10, 12], [1, 3, 6, 13], [1, 9, 10, 14], [2, 3], [2, 4], [2, 4, 5], [2, 3, 6], [2, 4, 5, 7], [2, 3, 6, 8], [2, 4, 5, 7, 9], [2, 4, 5, 7, 9, 10], [2, 4, 11], [2, 4, 11, 12], [2, 3, 6, 13], [2, 4, 11, 14], [3, 2, 4], [3, 2, 4, 5], [3, 6], [3, 2, 4, 5, 7], [3, 6, 8], [3, 2, 4, 5, 7, 9], [3, 6, 8, 10], [3, 2, 4, 11], [3, 2, 4, 11, 12], [3, 6, 13], [3, 6, 13, 14], [4, 5], [4, 5, 6], [4, 5, 7], [4, 5, 6, 8], [4, 5, 7, 9], [4, 5, 7, 9, 10], [4, 11], [4, 11, 12], [4, 5, 6, 13], [4, 11, 14], [5, 6], [5, 7], [5, 6, 8], [5, 7, 9], [5, 7, 9, 10], [5, 4, 11], [5, 4, 11, 12], [5, 6, 13], [5, 7, 9, 10, 14], [6, 5, 7], [6, 8], [6, 5, 7, 9], [6, 8, 10], [6, 13, 14, 11], [6, 13, 12], [6, 13], [6, 13, 14], [7, 5, 6, 8], [7, 9], [7, 9, 10], [7, 5, 4, 11], [7, 9, 10, 12], [7, 5, 6, 13], [7, 9, 10, 14], [8, 10, 9], [8, 10], [8, 10, 12, 11], [8, 10, 12], [8, 6, 13], [8, 10, 14], [9, 10], [9, 10, 12, 11], [9, 10, 12], [9, 10, 14, 13], [9, 10, 14], [10, 12, 11], [10, 12], [10, 14, 13], [10, 14], [11, 12], [11, 14, 13], [11, 14], [12, 13], [12, 11, 14], [13, 14]]

        # Adding hosts
        h1 = self.addHost('h1', cls=LinuxRouter, position = positions[1], ip=None)
        h2 = self.addHost('h2', cls=LinuxRouter, position = positions[2], ip=None)
        h3 = self.addHost('h3', cls=LinuxRouter, position = positions[3], ip=None)
        h4 = self.addHost('h4', cls=LinuxRouter, position = positions[4], ip=None)
        h5 = self.addHost('h5', cls=LinuxRouter, position = positions[5], ip=None)
        h6 = self.addHost('h6', cls=LinuxRouter, position = positions[6], ip=None)
        h7 = self.addHost('h7', cls=LinuxRouter, position = positions[7], ip=None)
        h8 = self.addHost('h8', cls=LinuxRouter, position = positions[8], ip=None)
        h9 = self.addHost('h9', cls=LinuxRouter, position = positions[9], ip=None)
        h10 = self.addHost('h10', cls=LinuxRouter, position = positions[10], ip=None)
        h11 = self.addHost('h11', cls=LinuxRouter, position = positions[11], ip=None)
        h12 = self.addHost('h12', cls=LinuxRouter, position = positions[12], ip=None)
        h13 = self.addHost('h13', cls=LinuxRouter, position = positions[13], ip=None)
        h14 = self.addHost('h14', cls=LinuxRouter, position = positions[14], ip=None)

        # Adding links between hosts
        self.addLink(h1, h2, bw=110, params1={ 'ip' : '10.0.0.1/24' }, params2={ 'ip' : '10.0.0.2/24' })
        self.addLink(h1, h3, bw=160, params1={ 'ip' : '10.0.1.1/24' }, params2={ 'ip' : '10.0.1.2/24' })
        self.addLink(h1, h9, bw=280, params1={ 'ip' : '10.0.9.1/24' }, params2={ 'ip' : '10.0.9.2/24' })
        self.addLink(h2, h3, bw=60, params1={ 'ip' : '10.0.3.1/24' }, params2={ 'ip' : '10.0.3.2/24' })
        self.addLink(h2, h4, bw=100, params1={ 'ip' : '10.0.5.1/24' }, params2={ 'ip' : '10.0.5.2/24' })
        self.addLink(h3, h6, bw=200, params1={ 'ip' : '10.0.4.1/24' }, params2={ 'ip' : '10.0.4.2/24' })
        self.addLink(h4, h11, bw=240,params1={ 'ip' : '10.0.7.1/24' }, params2={ 'ip' : '10.0.7.2/24' })
        self.addLink(h4, h5, bw=60, params1={ 'ip' : '10.0.6.1/24' }, params2={ 'ip' : '10.0.6.2/24' })
        self.addLink(h5, h6, bw=110, params1={ 'ip' : '10.0.8.1/24' }, params2={ 'ip' : '10.0.8.2/24' })
        self.addLink(h5, h7, bw=80, params1={ 'ip' : '10.0.10.1/24' }, params2={ 'ip' : '10.0.10.2/24' })
        self.addLink(h6, h8, bw=120, params1={ 'ip' : '10.0.17.2/24' }, params2={ 'ip' : '10.0.17.1/24' })
        self.addLink(h6, h13, bw=200,params1={ 'ip' : '10.0.16.2/24' }, params2={ 'ip' : '10.0.16.1/24' })
        self.addLink(h7, h9, bw=120, params1={ 'ip' : '10.0.11.2/24' }, params2={ 'ip' : '10.0.11.1/24' })
        self.addLink(h8, h10, bw=140, params1={ 'ip' : '10.0.15.2/24' }, params2={ 'ip' : '10.0.15.1/24' })
        self.addLink(h9, h10, bw=90, params1={ 'ip' : '10.0.12.1/24' }, params2={ 'ip' : '10.0.12.2/24' })
        self.addLink(h10, h12, bw=120, params1={ 'ip' : '10.0.14.1/24' }, params2={ 'ip' : '10.0.14.2/24' })
        self.addLink(h10, h14, bw=100, params1={ 'ip' : '10.0.13.1/24' }, params2={ 'ip' : '10.0.13.2/24' })
        self.addLink(h11, h12, bw=80, params1={ 'ip' : '10.0.20.2/24' }, params2={ 'ip' : '10.0.20.1/24' })
        self.addLink(h11, h14, bw=100, params1={ 'ip' : '10.0.21.2/24' }, params2={ 'ip' : '10.0.21.1/24' })
        self.addLink(h12, h13, bw=150, params1={ 'ip' : '10.0.19.2/24' }, params2={ 'ip' : '10.0.19.1/24' })
        self.addLink(h13, h14, bw=90, params1={ 'ip' : '10.0.18.1/24' }, params2={ 'ip' : '10.0.18.2/24' })

def run():
    topo = CustomTopology()
    net = Mininet(topo)
    net.start()

    net.pingAll()

    # info('*** Add routing for reaching networks that are not directly connected \n')

    # [1, 2, 4]
    #info(net['h1'].cmd('ip route add 10.0.5.2 via 10.0.0.1 dev h1-eth0'))
    #info(net['h4'].cmd('ip route add 10.0.0.1 via 10.0.5.2 dev h4-eth0'))
    # [1, 2, 4, 5]
    #info(net['h1'].cmd('ip route add 10.0.6.2 via 10.0.0.1 dev h1-eth0'))
    #info(net['h2'].cmd('ip route add 10.0.6.2 via 10.0.5.1 dev h2-eth0'))
    #info(net['h4'].cmd('ip route add 10.0.6.2 via 10.0.6.1 dev h4-eth0'))
    # [1, 3, 6]
    #info(net['h1'].cmd('ip route add 10.0.4.2 via 10.0.1.1 dev h1-eth0'))
    #info(net['h3'].cmd('ip route add 10.0.4.2 via 10.0.4.1 dev h3-eth0'))
    # [1, 2, 4, 5, 7]
    #info(net['h1'].cmd('ip route add 10.0.10.2 via 10.0.0.1 dev h1-eth0'))
    #info(net['h2'].cmd('ip route add 10.0.10.2 via 10.0.5.1 dev h2-eth0'))
    #info(net['h4'].cmd('ip route add 10.0.10.2 via 10.0.6.1 dev h4-eth2'))
    #info(net['h5'].cmd('ip route add 10.0.10.2 via 10.0.10.1 dev h5-eth2'))
    # [1, 3, 6, 8]
    #info(net['h1'].cmd('ip route add to 10.0.17.1 via 10.0.0.1 dev h1-eth1'))
    #info(net['h3'].cmd('ip route add to 10.0.17.1 via 10.0.4.1 dev h3-eth2'))
    #info(net['h6'].cmd('ip route add to 10.0.17.1 via 10.0.17.2 dev h6-eth1'))
    # [1, 9, 10]
    #info(net['h1'].cmd('ip route add to 10.0.12.2 via 10.0.9.1 dev h1-eth2'))
    #info(net['h9'].cmd('ip route add to 10.0.12.2 via 10.0.12.1 dev h9-eth2'))
    # [1, 2, 4, 11]
    #info(net['h1'].cmd('ip route add to 10.0.7.2 via 10.0.0.1 dev h1-eth0'))
    #info(net['h2'].cmd('ip route add to 10.0.7.2 via 10.0.5.1 dev h2-eth0'))
    #info(net['h4'].cmd('ip route add to 10.0.7.2 via 10.0.7.1 dev h4-eth1'))
    # [1, 9, 10, 12]
    #info(net['h1'].cmd('ip route add to 10.0.14.2 via 10.0.9.1 dev h1-eth2'))
    #info(net['h9'].cmd('ip route add to 10.0.14.2 via 10.0.12.1 dev h9-eth1'))
    #info(net['h10'].cmd('ip route add to 10.0.14.2 via 10.0.14.1 dev h10-eth2'))
    # [1, 3, 6, 13]
    #info(net['h1'].cmd('ip route add to 10.0.16.1 via 10.0.1.1 dev h1-eth1'))
    #info(net['h3'].cmd('ip route add to 10.0.16.1 via 10.0.4.1 dev h3-eth2'))
    #info(net['h6'].cmd('ip route add to 10.0.16.1 via 10.0.16.2 dev h6-eth2'))
    # [1, 9, 10, 14]
    #info(net['h1'].cmd('ip route add to 10.0.13.2 via 10.0.9.1 dev h1-eth2'))
    #info(net['h9'].cmd('ip route add to 10.0.13.2 via 10.0.12.1 dev h9-eth1'))
    #info(net['h10'].cmd('ip route add to 10.0.13.2 via 10.0.13.1 dev h10-eth1'))


    # [2, 4, 5]
    # [2, 3, 6]
    # [2, 4, 5, 7]
    # [2, 3, 6, 8]
    # [2, 4, 5, 7, 9]
    # [2, 4, 5, 7, 9, 10]
    # [2, 4, 11]
    # [2, 4, 11, 12]
    # [2, 3, 6, 13]
    # [2, 4, 11, 14]
    # [3, 2, 4]
    # [3, 2, 4, 5]
    # [3, 2, 4, 5, 7]
    # [3, 6, 8]
    # [3, 2, 4, 5, 7, 9]
    # [3, 6, 8, 10]
    # [3, 2, 4, 11]
    # [3, 2, 4, 11, 12]
    # [3, 6, 13]
    # [3, 6, 13, 14]
    # [4, 5, 6]
    # [4, 5, 7]
    # [4, 5, 6, 8]
    # [4, 5, 7, 9]
    # [4, 5, 7, 9, 10]
    # [4, 11, 12]
    # [4, 5, 6, 13]
    # [4, 11, 14]
    # [5, 6, 8]
    # [5, 7, 9]
    # [5, 7, 9, 10]
    # [5, 4, 11]
    # [5, 4, 11, 12]
    # [5, 6, 13]
    # [5, 7, 9, 10, 14]
    # [6, 5, 7]
    # [6, 5, 7, 9]
    # [6, 8, 10]
    # [6, 13, 14, 11]
    # [6, 13, 12]
    # [6, 13]
    # [6, 13, 14]
    # [7, 5, 6, 8]
    # [7, 9, 10]
    # [7, 5, 4, 11]
    # [7, 9, 10, 12]
    # [7, 5, 6, 13]
    # [7, 9, 10, 14]
    # [8, 10, 9]
    # [8, 10, 12, 11]
    # [8, 10, 12]
    # [8, 6, 13]
    # [8, 10, 14]
    # [9, 10, 12, 11]
    # [9, 10, 12]
    # [9, 10, 14, 13]
    # [9, 10, 14]
    # [10, 12, 11]
    # [10, 14, 13]
    # [11, 14, 13]
    # [12, 11, 14]

    CLI(net)
    net.stop()

if __name__ == "__main__":
    setLogLevel('info')
    run()

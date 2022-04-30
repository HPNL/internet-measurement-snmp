import numpy as np
from scapy.all import *

MIO = 5
ITERATION = 100
s = np.random.poisson(MIO, ITERATION)
sock = conf.L2socket()
pe=Ether()/IP(dst="10.13.37.218")/ICMP()
data = pe.build()
while True:
    pe.send(data)

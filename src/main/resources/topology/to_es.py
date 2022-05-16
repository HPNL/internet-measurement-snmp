import numpy as np
import matplotlib.pyplot as plt
import networkx as nx
from scipy.linalg import khatri_rao
from sympy import numbered_symbols
import csv

G = nx.Graph()
node_names = np.arange(1, 15)
G.add_nodes_from(node_names)

G.add_edge(1, 2, weight=110)
G.add_edge(1, 3, weight=160)
G.add_edge(1, 9, weight=280)
G.add_edge(2, 3, weight=60)
G.add_edge(2, 4, weight=100)
G.add_edge(3, 6, weight=200)
G.add_edge(4, 5, weight=60)
G.add_edge(4, 11, weight=240)
G.add_edge(5, 6, weight=110)
G.add_edge(5, 7, weight=80)
G.add_edge(6, 8, weight=120)
G.add_edge(6, 13, weight=200)
G.add_edge(7, 9, weight=120)
G.add_edge(8, 10, weight=140)
G.add_edge(9, 10, weight=90)
G.add_edge(10, 12, weight=120)
G.add_edge(10, 14, weight=100)
G.add_edge(11, 12, weight=80)
G.add_edge(11, 14, weight=100)
G.add_edge(12, 13, weight=150)
G.add_edge(13, 14, weight=90)

labels = nx.get_edge_attributes(G, 'weight')
pos = nx.spring_layout(G)
nx.draw(G, pos, with_labels=True)
nx.draw_networkx_edge_labels(G, pos, edge_labels=labels)
plt.show()

node_list = list(G.nodes)
od_list = []
for i in range(len(node_list)):
    for j in range(len(node_list)):
        if (node_list[i] < node_list[j]):
            od_list.append((node_list[i], node_list[j]))
M = len(G.edges)
L = len(od_list)

# shortest_paths = nx.shortest_path(G,weight="weight")
shortest_paths = []
for i in range(L):
    od = od_list[i]
    origin = od[0]
    dest = od[1]
    paths = list(nx.shortest_simple_paths(G, origin, dest, weight="weight"))
    shortest_paths.append(paths[0])

with open('shortest_path.csv', 'w') as f:
    writer = csv.writer(f)
    writer.writerows(shortest_paths)

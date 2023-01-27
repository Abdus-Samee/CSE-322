# python3 1805021.py areas.txt

import sys
import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv(sys.argv[1], header=None, delimiter=',', names=["first", "second", "third", "fourth"], skiprows=1)
#print(df["second"])

areas = [250, 500, 750, 1000, 1250]
nodes = [20, 40, 60, 80, 100]
flows = [10, 20, 30, 40, 50]

name = sys.argv[1][:-4]
l = globals()[name]
data = df["first"].values.flatten().tolist()
# print(data)
plt.plot(l, data, color="purple", label="area", marker="o")
plt.title("Throughput vs " + name.capitalize())
plt.xlabel(name.capitalize())
plt.ylabel("Throughput(bits/s)")
plt.xticks(l, [str(x) for x in l])
plt.yticks(data, [str(round(x, 6)) for x in data], rotation=45)
plt.show()

data = df["second"].values.flatten().tolist()
# print(data)
plt.plot(l, data, color="red", label="area", marker="o")
plt.title("Delay vs " + name.capitalize())
plt.xlabel(name.capitalize())
plt.ylabel("Delay(s)")
plt.xticks(l, [str(x) for x in l])
plt.yticks(data, [str(round(x, 6)) for x in data], rotation=45)
plt.show()

data = df["third"].values.flatten().tolist()
# print(data)
plt.plot(l, data, color="blue", label="area", marker="o")
plt.title("Delivery Ratio vs " + name.capitalize())
plt.xlabel(name.capitalize())
plt.ylabel("Delivery Ratio")
plt.xticks(l, [str(x) for x in l])
plt.yticks(data, [str(round(x, 6)) for x in data], rotation=45)
plt.show()

data = df["fourth"].values.flatten().tolist()
# print(data)
plt.plot(l, data, color="green", label="area", marker="o")
plt.title("Drop Ratio vs " + name.capitalize())
plt.xlabel(name.capitalize())
plt.ylabel("Drop Ratio")
plt.xticks(l, [str(x) for x in l])
plt.yticks(data, [str(round(x, 6)) for x in data], rotation=45)
plt.show()

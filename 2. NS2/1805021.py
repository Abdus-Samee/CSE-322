# python3 1805021.py area.txt

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
plt.plot(l, data, color="red", label="area", marker="o")
plt.title("Throughput vs " + name.capitalize())
plt.legend()
plt.xlabel("Area")
plt.ylabel("Throughput(bits/s)")
plt.show()

data = df["second"].values.flatten().tolist()
# print(data)
plt.plot(l, data, color="red", label="area", marker="o")
plt.title("Delay vs " + name.capitalize())
plt.legend()
plt.xlabel("Area")
plt.ylabel("Delay(s)")
plt.show()

data = df["third"].values.flatten().tolist()
# print(data)
plt.plot(l, data, color="red", label="area", marker="o")
plt.title("Delivery Ratio vs " + name.capitalize())
plt.legend()
plt.xlabel("Area")
plt.ylabel("Delivery Ratio")
plt.show()

data = df["fourth"].values.flatten().tolist()
# print(data)
plt.plot(l, data, color="red", label="area", marker="o")
plt.title("Drop Ratio vs " + name.capitalize())
plt.legend()
plt.xlabel("Area")
plt.ylabel("Drop Ratio")
plt.show()

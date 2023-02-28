# python3 1805021.py nodes.txt mod_nodes.txt

import sys
import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv(sys.argv[1], header=None, delimiter=',', names=["first", "second", "third", "fourth", "fifth", "sixth"], skiprows=1)
df2 = pd.read_csv(sys.argv[2], header=None, delimiter=',', names=["fi", "s", "t", "fo", "fif", "si"], skiprows=1)
#print(df["second"])

nodes = [20, 40, 60, 80, 100]
flows = [10, 20, 30, 40, 50]
rate = [100, 200, 300, 400, 500]
tx = [250, 500, 750, 1000, 1250]


name = sys.argv[1][:-4]
l = globals()[name]
data = df["first"].values.flatten().tolist()
data2 = df2["fi"].values.flatten().tolist()

# print(data)
plt.plot(l, data, color="purple", label="Usual Config", marker="o")
plt.plot(l, data2, color="red", label="Modified Config", marker="o")
plt.title("Throughput vs " + name.capitalize())
plt.xlabel(name.capitalize())
plt.ylabel("Throughput(bits/s)")
plt.legend(loc="upper right")
plt.xticks(l, [str(x) for x in l])
plt.yticks(data, [str(round(x, 6)) for x in data], rotation=45)
for x,y in zip(l, data2):
    label = "{:.2f}".format(y)
    plt.annotate(label, # this is the text
        (x,y), # these are the coordinates to position the label
        textcoords="offset points", # how to position the text
        xytext=(0,10), # distance from text to points (x,y)
        ha='center') # horizontal alignment can be left, right or center
plt.show()

data = df["second"].values.flatten().tolist()
data2 = df2["s"].values.flatten().tolist()
# print(data)
plt.plot(l, data, color="brown", label="Usual Config", marker="o")
plt.plot(l, data2, color="red", label="Modified Config", marker="o")
plt.title("Delay vs " + name.capitalize())
plt.xlabel(name.capitalize())
plt.ylabel("Delay(s)")
plt.legend(loc="upper right")
plt.xticks(l, [str(x) for x in l])
plt.yticks(data, [str(round(x, 6)) for x in data], rotation=45)
for x,y in zip(l, data2):
    label = "{:.2f}".format(y)
    plt.annotate(label, # this is the text
        (x,y), # these are the coordinates to position the label
        textcoords="offset points", # how to position the text
        xytext=(0,10), # distance from text to points (x,y)
        ha='center') # horizontal alignment can be left, right or center
plt.show()

data = df["third"].values.flatten().tolist()
data2 = df2["t"].values.flatten().tolist()
# print(data)
plt.plot(l, data, color="blue", label="Usual Config", marker="o")
plt.plot(l, data2, color="red", label="Modified Config", marker="o")
plt.title("Delivery Ratio vs " + name.capitalize())
plt.xlabel(name.capitalize())
plt.ylabel("Delivery Ratio")
plt.legend(loc="upper right")
plt.xticks(l, [str(x) for x in l])
plt.yticks(data, [str(round(x, 6)) for x in data], rotation=45)
for x,y in zip(l, data2):
    label = "{:.2f}".format(y)
    plt.annotate(label, # this is the text
        (x,y), # these are the coordinates to position the label
        textcoords="offset points", # how to position the text
        xytext=(0,10), # distance from text to points (x,y)
        ha='center') # horizontal alignment can be left, right or center
plt.show()

data = df["fourth"].values.flatten().tolist()
data2 = df2["fo"].values.flatten().tolist()
# print(data)
plt.plot(l, data, color="green", label="Usual Config", marker="o")
plt.plot(l, data2, color="red", label="Modified Config", marker="o")
plt.title("Drop Ratio vs " + name.capitalize())
plt.xlabel(name.capitalize())
plt.ylabel("Drop Ratio")
plt.xticks(l, [str(x) for x in l])
plt.yticks(data, [str(round(x, 6)) for x in data], rotation=45)
for x,y in zip(l, data2):
    label = "{:.2f}".format(y)
    plt.annotate(label, # this is the text
        (x,y), # these are the coordinates to position the label
        textcoords="offset points", # how to position the text
        xytext=(0,10), # distance from text to points (x,y)
        ha='center') # horizontal alignment can be left, right or center
plt.show()

data = df["fifth"].values.flatten().tolist()
data2 = df2["fif"].values.flatten().tolist()
# print(data)
plt.plot(l, data, color="orange", label="Usual Config", marker="o")
plt.plot(l, data2, color="red", label="Modified Config", marker="o")
plt.title("Energy/packet vs " + name.capitalize())
plt.xlabel(name.capitalize())
plt.ylabel("Joules/packet")
plt.legend(loc="upper right")
plt.xticks(l, [str(x) for x in l])
plt.yticks(data, [str(round(x, 6)) for x in data], rotation=45)
for x,y in zip(l, data2):
    label = "{:.2f}".format(y)
    plt.annotate(label, # this is the text
        (x,y), # these are the coordinates to position the label
        textcoords="offset points", # how to position the text
        xytext=(0,10), # distance from text to points (x,y)
        ha='center') # horizontal alignment can be left, right or center
plt.show()

# data = df["sixth"].values.flatten().tolist()
# data2 = df2["si"].values.flatten().tolist()
# # print(data)
# plt.plot(l, data, color="brown", label="Usual Config", marker="o")
# plt.plot(l, data2, color="red", label="Modified Config", marker="o")
# plt.title("Energy/byte vs " + name.capitalize())
# plt.xlabel(name.capitalize())
# plt.ylabel("Energy/byte")
# plt.legend(loc="upper right")
# plt.xticks(l, [str(x) for x in l])
# plt.yticks(data, [str(round(x, 6)) for x in data], rotation=45)
# plt.show()

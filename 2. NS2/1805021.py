import sys
import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv(sys.argv[1], header=None, delimiter=',', names=["first", "second", "third", "fourth", "fifth"], skiprows=1)
print(df["second"])

data = df.values.tolist()
print(data)

if sys.argv[1] == "area.txt":
    plt.plot(data, [[250, 500, 750, 1000, 1250]], color="red", label="area", marker="o")
    plt.title("Area Graph")
    plt.legend()
    plt.xlabel("Values")
    plt.ylabel("Data")
    plt.show()

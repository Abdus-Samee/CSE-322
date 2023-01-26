import sys
import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv(sys.argv[1], header=None, delimiter=',', names=["first", "second", "third", "fourth"])
print(df["second"])

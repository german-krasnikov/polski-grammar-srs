import sys, json, time, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import flip_rive_perf_driver as pd

pd.ADB = os.environ["ADB"]
N = int(sys.argv[1]) if len(sys.argv) > 1 else 15
CHECKPOINT_EVERY = 5

readings = []
readings.append({"cycle": 0, **pd.parse_mem(pd.meminfo())})
for i in range(1, N + 1):
    pd.step(f"[leak cycle {i}/{N}]")
    pd.do_one_rating_swipe("right" if i % 2 else "left")
    if i % CHECKPOINT_EVERY == 0 or i == N:
        readings.append({"cycle": i, **pd.parse_mem(pd.meminfo())})

print(json.dumps(readings, indent=2))

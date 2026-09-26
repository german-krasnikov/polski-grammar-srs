import subprocess, re, sys, time, json, os

ADB = os.environ.get("ADB", "adb")
PKG = "dev.polski.grammarmatrix"
SCRATCH = os.path.dirname(os.path.abspath(__file__))
XML = os.path.join(SCRATCH, "_w.xml")


def sh(*args, timeout=20):
    return subprocess.run([ADB] + list(args), capture_output=True, text=True, timeout=timeout).stdout


def dump():
    sh("shell", "uiautomator", "dump", "/sdcard/window.xml")
    sh("pull", "/sdcard/window.xml", XML)
    return open(XML, encoding="utf-8").read()


def find_text_bounds(data, text):
    for m in re.finditer(r"<node ([^>]*)/?>", data):
        attrs = m.group(1)
        t = re.search(r'text="([^"]*)"', attrs)
        if t and text in t.group(1):
            b = re.search(r"bounds=\"\[(\d+),(\d+)\]\[(\d+),(\d+)\]\"", attrs)
            return tuple(map(int, b.groups()))
    return None


def center(b):
    x1, y1, x2, y2 = b
    return (x1 + x2) // 2, (y1 + y2) // 2


def tap(x, y):
    sh("shell", "input", "tap", str(x), str(y))


def swipe(x1, y1, x2, y2, dur_ms=250):
    sh("shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(dur_ms))


def face_of(data):
    """'front' (question face), 'back' (revealed answer face), or None (e.g.
    chain-complete/no-due interstitial, or the introPending skill-intro screen
    which has no flip card at all)."""
    if find_text_bounds(data, "Перейти к заданию") is not None:
        return None  # introPending: no AndroidFlipCard yet
    if find_text_bounds(data, "Эталон") is not None:
        return "back"
    if find_text_bounds(data, "ИСХОДНОЕ ПРЕДЛОЖЕНИЕ") is not None:
        return "front"
    return None


def safe_point(data, face):
    """A point over plain, non-interactive label/sentence text well inside the
    gesture zone -- never over a button, chip or text field."""
    if face == "back":
        label = find_text_bounds(data, "Эталон")
        return (540, label[1] + 200) if label else None
    if face == "front":
        label = find_text_bounds(data, "ИСХОДНОЕ ПРЕДЛОЖЕНИЕ")
        return (540, label[1] + 200) if label else None
    return None


def step(action_desc):
    print(f"  -> {action_desc}", file=sys.stderr)


def skip_interstitial_if_any(data):
    """The fixed practice chain (5 steps) or the due-review queue can run out
    mid-batch, landing on a ChainComplete/NoDue info card instead of a
    question/answer face. Switch to the (much larger) spaced-repetition
    schedule to keep a steady supply of real due exercises."""
    for _ in range(4):  # chain-complete -> schedule -> exhausted -> chain -> ... can nest
        advanced = False
        for label in ("Перейти к заданию", "К повторениям", "Потренировать цепочку"):
            b = find_text_bounds(data, label)
            if b:
                step(f"interstitial: tap '{label}'")
                x, y = center(b)
                tap(x, y)
                time.sleep(0.7)
                data = dump()
                advanced = True
                break
        if not advanced:
            break
    return data


def scroll_to_top():
    sh("shell", "input", "swipe", "540", "700", "540", "2100", "300")
    time.sleep(0.4)


def reveal_if_needed():
    """Taps 'Показать ответ' wherever it currently is in the (plain, non-lazy)
    verticalScroll column, scrolling to find it if needed. Tapping it is a
    harmless no-op once already Revealed (TrainingStore.reveal() only acts
    from CardPhase.Question). Off-screen semantics nodes are pruned from the
    accessibility tree, so the *back* face (whose content sits higher up, and
    which never has this button at all) must be recognized at the top of the
    scroll before any downward search is attempted -- searching down while
    already on the back face just scrolls its own content out of view."""
    data = skip_interstitial_if_any(dump())
    scroll_to_top()
    data = dump()
    if face_of(data) == "back":
        return data  # already revealed and showing the answer face
    btn = find_text_bounds(data, "Показать ответ")
    tries = 0
    while not btn and tries < 3:
        step("scroll down to find Показать ответ")
        sh("shell", "input", "swipe", "540", "1900", "540", "700", "300")
        time.sleep(0.4)
        data = dump()
        btn = find_text_bounds(data, "Показать ответ")
        tries += 1
    if btn:
        step("tap Показать ответ")
        x, y = center(btn)
        tap(x, y)
        time.sleep(0.7)
        scroll_to_top()
        data = dump()
    return data


def do_one_flip():
    """One tap-to-flip on the card, from whatever face it is currently on."""
    data = reveal_if_needed()
    face = face_of(data)
    if face is None:
        raise RuntimeError(f"unrecognized card face; dump saved at {XML}")
    pt = safe_point(data, face)
    if not pt:
        raise RuntimeError("could not locate a safe tap point")
    step(f"tap-flip on {face} face at {pt}")
    tap(*pt)
    time.sleep(0.7)


def do_one_rating_swipe(direction="right"):
    """Reveals if needed, ensures the back face is showing, then swipes to rate."""
    data = reveal_if_needed()
    face = face_of(data)
    if face == "front":
        pt = safe_point(data, "front")
        step(f"tap-flip front->back at {pt}")
        tap(*pt)
        time.sleep(0.7)
        data = dump()
        face = face_of(data)
    if face != "back":
        raise RuntimeError(f"expected back face to rate, got {face!r}; dump at {XML}")
    pt = safe_point(data, "back")
    if not pt:
        raise RuntimeError("could not locate a safe swipe point")
    x, y = pt
    if direction == "right":
        step(f"swipe RIGHT (Good) at y={y}")
        swipe(x - 350, y, x + 350, y, 250)
    else:
        step(f"swipe LEFT (Again) at y={y}")
        swipe(x + 350, y, x - 350, y, 250)
    time.sleep(0.9)


def gfx_reset():
    sh("shell", "dumpsys", "gfxinfo", PKG, "reset")


def gfx_stats():
    return sh("shell", "dumpsys", "gfxinfo", PKG)


def meminfo():
    return sh("shell", "dumpsys", "meminfo", PKG)


def parse_gfx(text):
    out = {}
    for key, pat in [
        ("total_frames", r"Total frames rendered:\s*(\d+)"),
        ("janky_pct", r"Janky frames:\s*\d+\s*\(([\d.]+)%\)"),
        ("janky_legacy_pct", r"Janky frames \(legacy\):\s*\d+\s*\(([\d.]+)%\)"),
        ("p50_ms", r"50th percentile:\s*(\d+)ms"),
        ("p90_ms", r"90th percentile:\s*(\d+)ms"),
        ("p95_ms", r"95th percentile:\s*(\d+)ms"),
        ("p99_ms", r"99th percentile:\s*(\d+)ms"),
        ("missed_vsync", r"Number Missed Vsync:\s*(\d+)"),
        ("high_input_latency", r"Number High input latency:\s*(\d+)"),
        ("slow_ui_thread", r"Number Slow UI thread:\s*(\d+)"),
        ("deadline_missed", r"Number Frame deadline missed:\s*(\d+)"),
    ]:
        m = re.search(pat, text)
        if m:
            out[key] = m.group(1)
    return out


def parse_mem(text):
    m = re.search(r"TOTAL\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)", text)
    if m:
        return {"pss_total_kb": int(m.group(1)), "rss_total_kb": int(m.group(5))}
    return {}


def run_variant(name, n_flips=5, n_rates=5):
    result = {"variant": name}
    gfx_reset()
    mem_before = parse_mem(meminfo())
    t0 = time.time()
    for i in range(n_flips):
        step(f"[flip {i+1}/{n_flips}]")
        do_one_flip()
    t1 = time.time()
    for i in range(n_rates):
        step(f"[rate-swipe {i+1}/{n_rates}]")
        do_one_rating_swipe("right")
    t2 = time.time()
    gfx_after = parse_gfx(gfx_stats())
    mem_after = parse_mem(meminfo())
    result.update({
        "flip_wall_s": round(t1 - t0, 2),
        "rate_wall_s": round(t2 - t1, 2),
        "gfx": gfx_after,
        "mem_before": mem_before,
        "mem_after": mem_after,
    })
    return result


def run_leak_cycles(n):
    mem_before = parse_mem(meminfo())
    for i in range(n):
        step(f"[leak cycle {i+1}/{n}]")
        do_one_rating_swipe("right" if i % 2 == 0 else "left")
    mem_after = parse_mem(meminfo())
    return {"cycles": n, "mem_before": mem_before, "mem_after": mem_after}


if __name__ == "__main__":
    mode = sys.argv[1]
    if mode == "leak":
        n = int(sys.argv[2])
        print(json.dumps(run_leak_cycles(n), indent=2, ensure_ascii=False))
    else:
        n_flips = int(sys.argv[2]) if len(sys.argv) > 2 else 5
        n_rates = int(sys.argv[3]) if len(sys.argv) > 3 else 5
        res = run_variant(mode, n_flips, n_rates)
        print(json.dumps(res, indent=2, ensure_ascii=False))

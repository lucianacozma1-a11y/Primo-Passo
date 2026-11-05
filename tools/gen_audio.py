"""Genera gli audio delle lezioni con una voce neurale italiana (edge-tts) e un'anteprima con musica."""
import asyncio, json, os, re, subprocess, sys, math, wave, struct, random

import edge_tts

VOICE = os.environ.get("VOICE", "it-IT-IsabellaNeural")
RATE = os.environ.get("RATE", "-8%")
SRC = "app/src/main/java/it/primopasso/Program.kt"
OUT = "app/src/main/assets/audio"
TMP = "/tmp/voce"
os.makedirs(OUT, exist_ok=True)
os.makedirs(TMP, exist_ok=True)
os.makedirs("samples", exist_ok=True)

LIT = r'("(?:\\.|[^"\\])*")'
pat = re.compile(r'Lesson\((\d+),\s*(\d+),\s*' + r',\s*'.join([LIT] * 4), re.S)
src = open(SRC, encoding="utf-8").read()
lessons = []
for m in pat.finditer(src):
    lid = int(m.group(1))
    title, body, key, todo = (json.loads(m.group(i)) for i in range(3, 7))
    lessons.append((lid, title, body, key, todo))
print("lezioni trovate:", len(lessons))
assert len(lessons) >= 20


def sh(*args):
    subprocess.run(args, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


async def tts(text, path, voice=VOICE):
    for attempt in range(4):
        try:
            await edge_tts.Communicate(text, voice, rate=RATE, pitch="-2Hz").save(path)
            return
        except Exception as e:
            print("riprovo", e)
            await asyncio.sleep(3)
    raise RuntimeError("tts fallito")


def silence(sec, path):
    sh("ffmpeg", "-y", "-f", "lavfi", "-i", "anullsrc=r=24000:cl=mono", "-t", str(sec), "-c:a", "libmp3lame", "-b:a", "48k", path)


async def build(parts, out_path, voice=VOICE):
    silence(1.0, f"{TMP}/s_start.mp3")
    silence(0.9, f"{TMP}/s_mid.mp3")
    files = [f"{TMP}/s_start.mp3"]
    for i, p in enumerate(parts):
        f = f"{TMP}/p{i}.mp3"
        await tts(p, f, voice)
        files += [f, f"{TMP}/s_mid.mp3"]
    with open(f"{TMP}/list.txt", "w") as fh:
        for f in files:
            fh.write(f"file '{f}'\n")
    sh("ffmpeg", "-y", "-f", "concat", "-safe", "0", "-i", f"{TMP}/list.txt",
       "-af", "loudnorm=I=-18:TP=-2", "-ac", "1", "-ar", "24000", "-c:a", "libmp3lame", "-b:a", "48k", out_path)


def music_wav(path, seconds):
    """Stessa musica dolce dell'app: accordi lenti e campanelle lontane."""
    rate = 22050
    n = rate * seconds
    buf = [0.0] * n
    chords = [(130.81, 196.0, 261.63, 329.63), (110.0, 164.81, 220.0, 261.63), (87.31, 174.61, 220.0, 261.63), (98.0, 146.83, 196.0, 246.94)]
    seg = rate * 8
    for i in range(n):
        c = (i // seg) % 4
        k = i % seg
        t = i / rate
        w = 0.5 * (1 - math.cos(2 * math.pi * k / seg))
        nc = (c + 1) % 4
        for idx, ww in ((c, 1.0), (nc, 0.0)):
            pass
        v = 0.0
        for j, f in enumerate(chords[c]):
            a = 0.32 if j == 0 else 0.22
            v += a * math.sin(2 * math.pi * f * t)
        buf[i] = v * (0.35 + 0.65 * w)
    rnd = random.Random(7)
    pos = rate * 2
    bells = [523.25, 587.33, 659.25, 783.99, 880.0]
    while pos < n:
        f = rnd.choice(bells)
        for k in range(rate * 4):
            if pos + k >= n:
                break
            t = k / rate
            buf[pos + k] += 0.10 * math.exp(-1.4 * t) * (1 - math.exp(-60 * t)) * math.sin(2 * math.pi * f * t)
        pos += rate * (3 + rnd.randint(0, 3))
    mx = max(abs(x) for x in buf) or 1
    with wave.open(path, "w") as wf:
        wf.setnchannels(1); wf.setsampwidth(2); wf.setframerate(rate)
        wf.writeframes(b"".join(struct.pack("<h", int(x / mx * 0.5 * 32767)) for x in buf))


async def main():
    only_samples = os.environ.get("ONLY_SAMPLES") == "1"
    l1 = lessons[0]
    first_par = l1[2].split("\n\n")[0]
    for v in ["it-IT-IsabellaNeural", "it-IT-ElsaNeural", "it-IT-GiuseppeMultilingualNeural", "it-IT-DiegoNeural"]:
        try:
            await build([f"Lezione 1. {l1[1]}.", first_par], f"samples/voce_{v.split('-')[2]}.mp3", v)
            print("campione", v)
        except Exception as e:
            print("voce non disponibile", v, e)
    if only_samples:
        return
    for lid, title, body, key, todo in lessons:
        parts = [f"Lezione {lid}. {title}."] + [p for p in body.split("\n\n")] + [f"Il concetto chiave. {key}", f"Per oggi. {todo}"]
        await build(parts, f"{OUT}/lesson_{lid}.mp3")
        print("lezione", lid, "ok")
    # anteprima con musica di sottofondo
    music_wav(f"{TMP}/music.wav", 64)
    sh("ffmpeg", "-y", "-i", f"{OUT}/lesson_1.mp3", "-stream_loop", "-1", "-i", f"{TMP}/music.wav",
       "-filter_complex", "[1:a]volume=0.22,afade=t=in:d=3[m];[0:a][m]amix=inputs=2:duration=first:dropout_transition=0,afade=t=out:st=0:d=0[o]",
       "-map", "[o]", "-ac", "1", "-c:a", "libmp3lame", "-b:a", "64k", "samples/lezione1_con_musica.mp3")


asyncio.run(main())

# NJ01 P8 / Node feasibility probe (bounded, experimental)

Status: **Kit A NO-GO in the current product checkout; no writer or wire format selected.** The native B prototype proves a narrower run-row transport can be tested under both minimum supported Node versions, but complete `.measure` bytes, full report semantics, capacity limits and the physical writer remain unfrozen/unimplemented. This is not an NJ01 acceptance result.

Inputs: product `biome/full-port-2026@b9bab47`, frozen semantic ledger `Plans/Biome/NJ01PerformanceWriter.cases` at that commit; compiler `main@7cf5977799600591d1a329742ae08ac6944c3b72`, existing `@biome-dsl/kit` `readData`/`formatDataValue`. The standalone experiment is `scripts/nj01/probe-glass-run-rows.mjs`; production `Plans/Kotlin/archive/scripts/benchmark-glass-rollout.mjs` is untouched. No JSON is written by the probe.

## Delivery and semantics

In a `git archive` clean product checkout, `npm ci --ignore-scripts --engine-strict` passed on Node 20.19.0 and 22.12.0. A local Kit tarball produced by `npm pack ./packages/kit --ignore-scripts` had SHA-256 `e35bbf13e1cce3cb73de9c1beefac916de9bbd914ae755d83b9cdf242f3be433` (53,996 bytes per `npm pack --dry-run`). It was created from the developer compiler worktree, whose `dist` is ignored/untracked; therefore the **source commit alone does not pin this tarball**. The tarball is not checked into the product and no product dependency is pinned.

Using Node 20.19.0, npm 11.3.0 and `--engine-strict --legacy-peer-deps`, installing that tarball returned `EBADENGINE`: Kit declares `node >=22`. Using Node 22.12.0 with the same options, installation succeeded, but `import { readData } from '@biome-dsl/kit'` failed `ERR_MODULE_NOT_FOUND` because the packed `exports` points to `./src/index.ts`, while the package `files` list contains `dist` only. The `publishConfig.exports` for `dist` did not replace the local tarball manifest. A direct absolute import of the installed `dist/data-values.js` works in this experiment, but is **not** a supported clean-checkout package entrypoint or product delivery solution. The first `npm install --no-save --package-lock=false` attempts also failed inside npm Arborist (`edgesOut` null); `npm ci` followed by `--legacy-peer-deps` isolated the actual Kit package failures above. These npm attempts are not counted as successful delivery.

Raw `formatDataValue(-0)` emits `0`, and `readData` then returns positive zero; the P02 `Object.is` check fails for one of seven finite probes. It emits U+0085 raw inside a quoted string, violating P03's no raw record line break/control requirement. A project wrapper could encode these cases, but its bytes and cost need separate review; the existing Kit pair does not meet equal guarantees as supplied. `readData` also returns a numeric value without its source spelling; old-byte preservation belongs to Git provenance rather than this reader.

The prototype's B side has a provisional line/column representation solely to exercise the comparison. It explicitly preserves negative zero, the other six P02 numeric probes, `null` versus empty text, Unicode including U+0085, and escapes controls. Its shared run validation rejects zero/nonfinite intervals, unpaired surrogates, missing fields, bad indices/order, duplicate runs, incorrect derived percentile/count and truncated/inconsistent row counts. It round-trips the **chromium four-run subset** of P01 and 1,000 complete runs/30,000 ordered intervals at the P04 floor. A direct Kit `dist` import round-trips the same run rows except the separately demonstrated P02/P03 failures. Neither side tests the full P01 engine headers, two-engine summaries, date/URLs/profile, N02/N05/N08/N11/N15/N17/N18 or the future full writer. The preliminary text layout is not an approved byte oracle or production API.

## Measured cost (diagnostic, not full-writer P8 proof)

Each sample ran in a separate Node process with three warmups and 20 encode/decode repetitions. The table gives the median of three sequential process samples for each runtime/size; `encode` and `decode` are mean milliseconds within that process. Sampled heap increase is the largest `heapUsed` observed **after a complete encode+decode iteration**, minus `heapUsed` after GC before the loop; it is not a continuous peak or a resource ceiling. RSS is the final process RSS. Raw 36 lines are in `NJ01P8BenchmarkRaw.txt`. Kit figures import the packed internal `dist/data-values.js` by absolute path despite the failed public import, and do not include a repair wrapper for P02/P03, so they cannot justify selecting A. Both variants use the same 13 run fields and 30 intervals per run, but their timing arms have unequal per-row validation work: the native writer and reader validate each row in addition to shared document validation, while the Kit arm only uses the shared document validation. The ratios are diagnostic and cannot select an implementation; the native format is only provisional.

| Node | Runs | Variant | Bytes | Encode ms | Decode ms | Sampled heap increase MiB | Final RSS MiB |
| --- | ---: | --- | ---: | ---: | ---: | ---: | ---: |
| 20.19.0 | 10 | Kit internal | 2,894 | 0.373 | 1.383 | 0.94 | 45.61 |
| 20.19.0 | 10 | Native prototype | 1,620 | 0.420 | 0.460 | 0.94 | 43.16 |
| 20.19.0 | 100 | Kit internal | 28,852 | 1.346 | 3.900 | 4.75 | 63.84 |
| 20.19.0 | 100 | Native prototype | 16,058 | 1.699 | 2.175 | 2.37 | 58.68 |
| 20.19.0 | 1,000 | Kit internal | 290,205 | 6.698 | 28.463 | 31.93 | 99.28 |
| 20.19.0 | 1,000 | Native prototype | 162,211 | 7.799 | 11.002 | 24.70 | 107.87 |
| 22.12.0 | 10 | Kit internal | 2,894 | 0.248 | 1.506 | 0.95 | 51.46 |
| 22.12.0 | 10 | Native prototype | 1,620 | 0.610 | 0.800 | 0.94 | 51.54 |
| 22.12.0 | 100 | Kit internal | 28,852 | 1.496 | 4.145 | 4.37 | 69.75 |
| 22.12.0 | 100 | Native prototype | 16,058 | 1.429 | 1.966 | 2.17 | 60.91 |
| 22.12.0 | 1,000 | Kit internal | 290,205 | 6.714 | 27.719 | 38.38 | 112.97 |
| 22.12.0 | 1,000 | Native prototype | 162,211 | 7.486 | 10.598 | 22.17 | 104.16 |

Separate seven-process startup samples (Python `perf_counter` around `node --input-type=module -e ...`) gave median wall milliseconds: Node 20.19.0 native baseline 84.61, Kit internal import 108.82; Node 22.12.0 native 136.28, Kit internal import 153.59. For each Node version, all seven native launches ran before all seven Kit launches; the order was neither alternated nor randomized. These are process startup/import observations with a shared machine and disk cache, not application launch proof or a causal startup comparison. The native prototype is 214 lines **including test vectors, self-checks, benchmark and reporting**; it has no new dependency. Kit's `dist/data-values.js` alone is 5,063 bytes, but working package delivery and a semantic wrapper would add cost. Neither experiment measures future review effort or establishes a >=2× margin below a declared limit because N17/P23 limits are not yet frozen.

## Reproduction and handoff

From the clean product `git archive`, with exact Node binaries (installed via `npx --yes node@20.19.0 --version` and `npx --yes node@22.12.0 --version`), the delivery experiment ran `npm ci --ignore-scripts --engine-strict`, then `npm install --ignore-scripts --no-save --engine-strict --legacy-peer-deps <pinned-tarball>` and public import. The compiler tarball command ran from `/Users/german/Work/Python/biome-dsl/implementations/typescript`. The benchmark ran from the isolated product worktree:

```text
<node20-or-node22> --expose-gc scripts/nj01/probe-glass-run-rows.mjs native 10|100|1000
<node20-or-node22> --expose-gc scripts/nj01/probe-glass-run-rows.mjs kit 10|100|1000 <absolute-installed-dist/data-values.js>
```

Selected/executed: 36/36 benchmark processes, each with the embedded P01-chromium/P02/P03/P04-row and listed negative self-checks; zero process skips or failures. The 7/7 startup launches per Node/variant also exited successfully. `npm ci` 2/2 PASS; Kit package install Node20 FAIL expected `EBADENGINE`, Node22 PASS; public import Node22 FAIL `ERR_MODULE_NOT_FOUND`. No full benchmark browser run, actual `.measure` output, host/device execution, full P01/N01–N19 acceptance, strict resource ceiling or independent tester/reviewer was run in this developer probe.

Next handoff: independent tester should add exact byte and negative oracles for a chosen text representation before any production printer; architecture should decide whether Kit packaging and Node20 support are worth changing or whether to adopt a small native helper. The current data favors testing B but does **not** choose it. Any selected format must then be measured at full-report scale under explicit P23 bounds and reviewed against P8/P11/P15/P17/P23 before switching the first writer.

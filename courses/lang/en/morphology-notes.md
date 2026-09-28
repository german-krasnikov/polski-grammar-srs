# en lexicon — authoring notes (not read by any runtime/validator)

Plans/Kotlin/EnRuPackPlan.md §1.3 principle 5: orthography/inflection rules live here as notes for
whoever adds the next lexeme by hand; `lexicon.json` itself stores only the finished, literal
surface strings (same decision as pl — a runtime table, not a runtime rule engine).

## Regular verb endings (materialize into `present3sg`/`past`/`pastParticiple`/`ing`)

- 3rd person singular present: `+s` (`walk→walks`), `+es` after sibilants (`watch→watches`),
  `y→ies` after a consonant (`study→studies`, none in this lexicon yet).
- Regular past / past participle: `+ed` (`walk→walked`), `+d` after a silent final `e`
  (`like→liked`), `y→ied` after a consonant (`study→studied`, none yet).
- Consonant doubling before `-ed`/`-ing` on a stressed short vowel + single consonant
  (`stop→stopped/stopping`) — none of the 10 verbs in this lexicon trigger it yet; the next author
  adding one must double the final consonant in both `past`/`pastParticiple` and `ing`, not just one.
- `-ing`: `+ing` (`walk→walking`), drop a silent final `e` first (`like→liking`, `have→having`).
- Irregular verbs (`see/saw/seen`, `go/went/gone`) are simply a different literal string in the same
  4 fields — no `irregular` flag, exactly EnRuPackPlan.md §1.3's "неправильная форма — просто другая
  строка в той же таблице".

## Why `be` and `will` don't fit the plain 4-field shape

- `be` is the only verb whose **present** varies by more than 3sg/non-3sg (`am`/`is`/`are`, all
  three distinct) and whose **past** varies by number (`was`/`were`), not just tense. It keeps
  `present3sg`/`past`/`pastParticiple`/`ing` (is/was/been/being, i.e. the singular forms) and adds
  `presentSg1` (`am`), `presentPl` (`are`), `pastPl` (`were`) as extra fields — real English facts,
  not invented ones; a future consumer that only reads the 4 baseline fields still gets a correct
  singular sentence.
- `will` is a modal auxiliary: no `-s`/`-ed`/`-ing`/participle exists for it in Modern English (only
  `present3sg: "will"`, used invariantly for every person/number). No `past`/`pastParticiple`/`ing`
  fields are added — that would document forms that don't exist. (`would` exists as a separate,
  distinct modal, out of scope: none of the 16 en:* skills need a conditional/reported-future form.)

## Irregular plural nouns

- `child→children` is the only irregular plural in this lexicon (matches EnRuPackPlan.md §1.3's own
  example) — again just a different literal `forms.pl` string, no rule.
- `wife→wives`, `woman→women` are also irregular (real English facts) — noted here so a future
  reviewer doesn't "fix" them into `wifes`/`womans`.

## No gender, no case declension

- Adjectives are invariant (`forms.invariant`, one string) — English adjectives never agree with the
  noun they modify.
- Possessive determiners (`my/your/his/her/its/our/their`) are invariant per lexeme too — unlike pl's
  `mój/moja/moje`, none of them change by the noun's gender or number. `your` covers both 2nd-person
  singular and plural (no `yourPlural` lexeme, unlike pl's separate `twój`/`wasz`) since English uses
  the identical word for both — a genuine collapse, not a placeholder.

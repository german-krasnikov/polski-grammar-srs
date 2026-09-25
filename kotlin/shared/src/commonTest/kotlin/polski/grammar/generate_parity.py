#!/usr/bin/env python3
"""Generate static Kotlin P01 dictionaries and exact-case assertions from grammar.json.

Run from any directory: python3 kotlin/shared/src/commonTest/kotlin/polski/grammar/generate_parity.py --check
The --check mode fails if the current 1979-case fixture no longer matches generated Kotlin.
"""
import json, pathlib, os, sys
os.chdir(pathlib.Path(__file__).resolve().parents[7])
CHECK = '--check' in sys.argv
root=pathlib.Path('kotlin/shared/src/commonMain/kotlin/polski/data');root.mkdir(parents=True,exist_ok=True)
cases=json.load(open('tests/fixtures/kotlin-parity/grammar.json'))['cases']
def q(s):return json.dumps(s,ensure_ascii=False)
def write(name, body):
 target = root/name
 content = 'package polski.data\n\nimport polski.model.*\n\n'+body
 if CHECK:
  if target.read_text(encoding='utf8') != content: raise SystemExit(f'Stale generated dictionary: {target}')
 else: target.write_text(content,encoding='utf8')
ns={}
for x in cases:
 if x['id'].startswith('G-NOUN-'):
  i=x['input'];e=x['expected'];n=ns.setdefault(i['nounId'],{'lemma':e['lemma'],'meaning':e['meaning'],'gender':e['gender'],'forms':{}});n['forms'][(i['number'],i['gramCase'])]=e['form']
order=['nom','gen','dat','acc','inst','loc','voc']
def formmap(forms,n):return 'caseForms('+', '.join(q(forms[(n,c)]) for c in order)+')'
rows=[]
for id,n in ns.items():rows.append(f'    Noun({q(id)}, {q(n["lemma"])}, {q(n["meaning"])}, Gender.fromId({q(n["gender"])}), mapOf(NumberGram.SG to {formmap(n["forms"],"sg")}, NumberGram.PL to {formmap(n["forms"],"pl")}))')
write('Nouns.kt','private fun caseForms(vararg values: String): Map<GramCase, String> = GramCase.entries.zip(values).toMap()\n\nval nouns: List<Noun> = listOf(\n'+',\n'.join(rows)+'\n)\n\nfun nounById(id: String): Noun = nouns.firstOrNull { it.id == id } ?: error("Unknown noun $id")\n')
ad={}
for x in cases:
 if x['id'].startswith('G-ADJ-'):
  i=x['input'];e=x['expected'];a=ad.setdefault(i['adjectiveId'],{'lemma':e['lemma'],'meaning':e['meaning'],'forms':{}});a['forms'][(i['number'],i['gender'],i['gramCase'])]=e['form']
rows=[]
for id,a in ad.items():
 nums=[]
 for n in ['sg','pl']:
  gs=[]
  for g in ['m-personal','m-animate','m-inanimate','f','n']:
   gs.append('Gender.fromId('+q(g)+') to caseForms('+', '.join(q(a['forms'][(n,g,c)]) for c in order)+')')
  nums.append('NumberGram.'+n.upper()+' to mapOf('+', '.join(gs)+')')
 rows.append(f'    Adjective({q(id)}, {q(a["lemma"])}, {q(a["meaning"])}, mapOf('+', '.join(nums)+'))')
write('Adjectives.kt','private fun caseForms(vararg values: String): Map<GramCase, String> = GramCase.entries.zip(values).toMap()\n\nval adjectives: List<Adjective> = listOf(\n'+',\n'.join(rows)+'\n)\n\nfun adjectiveById(id: String): Adjective = adjectives.firstOrNull { it.id == id } ?: error("Unknown adjective $id")\n')
vs={};stems={'have':['miał','miała','miało','mieli','miały'],'see':['widział','widziała','widziało','widzieli','widziały'],'like':['lubił','lubiła','lubiło','lubili','lubiły'],'buy':['kupował','kupowała','kupowało','kupowali','kupowały'],'read':['czytał','czytała','czytało','czytali','czytały'],'talk':['mówił','mówiła','mówiło','mówili','mówiły'],'go':['szedł','szła','szło','szli','szły'],'be':['był','była','było','byli','były'],'buyDone':['kupił','kupiła','kupiło','kupili','kupiły'],'do':['robił','robiła','robiło','robili','robiły'],'doDone':['zrobił','zrobiła','zrobiło','zrobili','zrobiły']}
for x in cases:
 if x['id'].startswith('G-VERB-') and not x['id'].endswith('present-rejected'):
  i=x['input'];e=x['expected'];v=vs.setdefault(i['verbId'],{'meta':e,'present':{}})
  if i['tense']=='present' or (e['aspect']=='perfective' and i['tense']=='future'):v['present'][(i['number'],i['person'])]=e['form']
rows=[]
for id,v in vs.items():
 e=v['meta']; ps=[]
 for n in ['sg','pl']:
  ps.append('NumberGram.'+n.upper()+' to mapOf('+', '.join('Person.'+['FIRST','SECOND','THIRD'][p-1]+' to '+q(v['present'][(n,p)]) for p in [1,2,3])+')')
 stem=', '.join(q(s) for s in stems[id]);pair=q(e['perfectivePair']) if e['perfectivePair'] else 'null'
 rows.append(f'    Verb({q(id)}, {q(e["lemma"])}, {q(e["meaning"])}, Aspect.{e["aspect"].upper()}, mapOf('+', '.join(ps)+f'), PastStem({stem}), FutureType.{e["futureType"].upper()}, {pair})')
write('Verbs.kt','val verbs: List<Verb> = listOf(\n'+',\n'.join(rows)+'\n)\n\nfun verbById(id: String): Verb = verbs.firstOrNull { it.id == id } ?: error("Unknown verb $id")\n')
pron=[x for x in cases if x['id'].startswith('G-PRON-')]
rows=[]
for x in pron:rows.append('    '+q(x['input']['pronounId'])+' to mapOf('+', '.join('GramCase.'+c.upper()+' to '+q(x['expected'][c]) for c in order)+')')
write('Pronouns.kt','val personalPronouns: Map<String, Map<GramCase, String>> = linkedMapOf(\n'+',\n'.join(rows)+'\n)\n\ndata class Possessive(val id: PossessiveId, val label: String)\n\nval possessives: List<Possessive> = listOf(\n'+',\n'.join('    Possessive(PossessiveId.fromId('+q(x['input']['owner'])+'), '+q(x['expected']['label'])+')' for x in [next(y for y in cases if y['id'].startswith('G-POSS-'+id+'-')) for id in ['my','your','his','her','our','yourPlural','their']])+'\n)\n')
skills=[x['expected'] for x in cases if x['id'].startswith('S-')]
rows=[]
for s in skills:
 pre='listOf('+', '.join(q(p) for p in s['prerequisites'])+')'
 rows.append('    Skill('+', '.join(q(s[k]) for k in ['id','title','group','level','formula','theory','hint'])+', '+pre+')')
write('Skills.kt','val skills: List<Skill> = listOf(\n'+',\n'.join(rows)+'\n)\n\nfun skillById(id: String): Skill = skills.firstOrNull { it.id == id } ?: error("Unknown skill $id")\n')

cases=json.load(open('tests/fixtures/kotlin-parity/grammar.json'))['cases']
def q(s):return json.dumps(s,ensure_ascii=False)
def C(s):return 'GramCase.fromId('+q(s)+')'
def N(s):return 'NumberGram.fromId('+q(s)+')'
def G(s):return 'Gender.fromId('+q(s)+')'
def P(s):return 'PossessiveId.fromId('+q(s)+')'
def check(actual,expected,id):return f'        assertEquals({q(expected)}, {actual}, {q(id)})'
lines=['package polski.grammar','','import kotlin.test.Test','import kotlin.test.assertEquals','import kotlin.test.assertFailsWith','import polski.data.*','import polski.model.*','','class GrammarParityTest {']
for part in range((len(cases)+99)//100):
 lines.append('    @Test');lines.append(f'    fun corpusPart{part+1}() {{')
 for x in cases[part*100:(part+1)*100]:
  id=x['id'];i=x['input'];e=x['expected']
  if id.startswith('G-NOUN-'):
   exp=e['lemma']+'|'+e['meaning']+'|'+e['gender']+'|'+e['form']
   act=f'run {{ val noun = nounById({q(i["nounId"])}); listOf(noun.lemma, noun.meaning, noun.gender.id, nounForm(noun.id, {C(i["gramCase"])}, {N(i["number"])})).joinToString("|") }}'
   lines.append(check(act,exp,id))
  elif id.startswith('G-ADJ-'):
   exp=e['lemma']+'|'+e['meaning']+'|'+e['form'];act=f'run {{ val a = adjectiveById({q(i["adjectiveId"])}); listOf(a.lemma, a.meaning, adjectiveForm(a.id, {G(i["gender"])}, {C(i["gramCase"])}, {N(i["number"])})).joinToString("|") }}';lines.append(check(act,exp,id))
  elif id.startswith('G-VERB-'):
   arg=f'{q(i["verbId"])}, Tense.fromId({q(i["tense"])}), Person.fromId({i["person"]}), {N(i["number"])}, {G(i["gender"])}'
   if 'error' in e:lines.append(f'        assertEquals({q(e["error"]["message"])}, assertFailsWith<IllegalStateException> {{ verbForm({arg}) }}.message, {q(id)})')
   else:
    exp='|'.join(str(e[k]) if e[k]!=None else '' for k in ['lemma','meaning','aspect','futureType','perfectivePair','form'])
    act=f'run {{ val v = verbById({q(i["verbId"])}); listOf(v.lemma, v.meaning, v.aspect.id, v.futureType.id, v.perfectivePair.orEmpty(), verbForm({arg})).joinToString("|") }}'
    lines.append(check(act,exp,id))
  elif id.startswith('G-PRON-'):
   exp='|'.join(e[c] for c in ['nom','gen','dat','acc','inst','loc','voc']);act='GramCase.entries.joinToString("|") { personalPronouns.getValue('+q(i['pronounId'])+').getValue(it) }';lines.append(check(act,exp,id))
  elif id.startswith('G-POSS-'):
   exp=e['label']+'|'+e['form'];act=f'possessives.first {{ it.id == {P(i["owner"])} }}.label + "|" + possessiveForm({P(i["owner"])}, {G(i["gender"])}, {N(i["number"])}, {C(i["gramCase"])})';lines.append(check(act,exp,id))
  elif id.startswith('G-PHRASE-'):
   lines.append(check(f'nounPhrase({q(i["nounId"])}, {C(i["gramCase"])}, {N(i["number"])}, {q(i["adjectiveId"])}, {P(i["possessive"])})',e,id))
  elif id.startswith('G-SENTENCE-'):
   lines.append(check(f'caseSentence(SentenceSeed({q(i["seed"]["nounId"])}, {q(i["seed"]["adjectiveId"])}), {C(i["gramCase"])}, {P(i["owner"])}, {N(i["number"])})',e,id))
  elif id.startswith('S-'):
   exp='|'.join(e[k] for k in ['id','title','group','level','formula','theory','hint'])+'|'+','.join(e['prerequisites'])
   act='run { val s = skillById('+q(i['skillId'])+'); listOf(s.id,s.title,s.group,s.level,s.formula,s.theory,s.hint,s.prerequisites.joinToString(",")).joinToString("|") }';lines.append(check(act,exp,id))
 lines.append('    }');lines.append('')
lines.append('}')
p=pathlib.Path('kotlin/shared/src/commonTest/kotlin/polski/grammar/GrammarParityTest.kt');content='\n'.join(lines)+'\n'
if CHECK:
 if p.read_text(encoding='utf8') != content: raise SystemExit(f'Stale generated fixture assertions: {p}')
else: p.write_text(content,encoding='utf8')

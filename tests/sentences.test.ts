import {describe,it,expect,vi,afterEach} from 'vitest';
import {generateChain,generateForSkill,sentenceSeeds} from '../src/training/generator';
import {possessiveForm} from '../src/data/pronouns';
import {nounPhrase} from '../src/grammar/engine';
import {recordReview} from '../src/progress/review';
import {freshProgress,loadProgress,saveProgress,localDay} from '../src/progress/storage';
import {skills} from '../src/training/skills';

describe('sentence transformation cards',()=>{
 it('keeps the exact wife sentence through five connected transformations',()=>{
  const chain=generateChain({nounId:'wife',adjectiveId:'beautiful'});
  expect(chain[0].source).toBe('To jest moja piękna żona.');
  expect(chain.map(c=>c.expected)).toEqual([
   'Widzę moją piękną żonę.','Widziałem moją piękną żonę.',
   'Nie widziałem mojej pięknej żony.','Nie widziałem ich pięknej żony.',
   'Mówię o ich pięknej żonie.',
  ]);
  for(let i=1;i<chain.length;i++)expect(chain[i].source).toBe(chain[i-1].expected);
 });
 it('reuses the same words while handling masculine -a and neuter agreement',()=>{
  const colleague=generateChain({nounId:'friendM',adjectiveId:'good'});
  expect(colleague[0].expected).toBe('Widzę mojego dobrego kolegę.');
  expect(colleague[2].expected).toBe('Nie widziałem mojego dobrego kolegi.');
  expect(colleague[4].expected).toBe('Mówię o ich dobrym koledze.');
  expect(generateChain({nounId:'child',adjectiveId:'small'})[0].primarySkill).toBe('case.acc.n');
 });
 it('uses complete source sentences for every skill and every chain',()=>{
  for(const skill of skills)for(let i=0;i<25;i++){
   const ex=generateForSkill(skill.id);
   expect(ex.source).toMatch(/^[A-ZĄĆĘŁŃÓŚŹŻ].+[.!?]$/u);
   expect(ex.source).not.toContain(' + ');
   expect(ex.source.split(' ').length).toBeGreaterThanOrEqual(3);
   expect(ex.expected).not.toBe(ex.source);
   expect(ex.changes.length).toBeGreaterThan(0);
  }
  for(const seed of sentenceSeeds)for(const ex of generateChain(seed))expect(skills.some(s=>s.id===ex.primarySkill)).toBe(true);
 });
 it('changes the whole nominative subject through past and future',()=>{
  const seed={nounId:'wife',adjectiveId:'beautiful'};
  const past=generateForSkill('verb.past',seed),future=generateForSkill('verb.future',seed);
  expect(past.source).toBe('Moja piękna żona idzie do domu.');
  expect(past.expected).toBe('Moja piękna żona szła do domu.');
  expect(future.expected).toBe('Moja piękna żona będzie iść do domu.');
  expect(future.accepted).toContain('Moja piękna żona będzie szła do domu.');
 });
 it('distinguishes personal and nonpersonal plural inside full sentences',()=>{
  expect(generateForSkill('sentence.plural',{nounId:'friendM',adjectiveId:'good'}).expected).toBe('Widzę moich dobrych kolegów.');
  expect(generateForSkill('sentence.plural',{nounId:'book',adjectiveId:'new'}).expected).toBe('Widzę moje nowe książki.');
 });
});

describe('possessive matrix',()=>{
 it.each([
  ['your','m-personal','pl','nom','twoi'],['your','f','sg','acc','twoją'],
  ['our','m-personal','pl','nom','nasi'],['our','f','sg','inst','naszą'],
  ['yourPlural','m-personal','pl','nom','wasi'],['yourPlural','n','sg','gen','waszego'],
 ] as const)('declines %s/%s/%s/%s', (id,g,n,c,expected)=>expect(possessiveForm(id,g,n,c)).toBe(expected));
 it('keeps his, her and their unchanged across all cases',()=>{
  for(const [id,expected] of [['his','jego'],['her','jej'],['their','ich']] as const)
   for(const c of ['nom','gen','dat','acc','inst','loc','voc'] as const)
    expect(possessiveForm(id,'f','sg',c)).toBe(expected);
  expect(nounPhrase('wife','gen',{adjectiveId:'beautiful',possessive:'their'})).toBe('ich pięknej żony');
 });
});

describe('Anki self-assessment and persisted scheduling',()=>{
 afterEach(()=>vi.unstubAllGlobals());
 it('counts oral recall by grade and written recall by the actual checked answer',()=>{
  const p=freshProgress(),id='verb.past';
  const oral=recordReview(p,id,'good');
  expect(oral.stats[id]).toEqual({reviews:1,correct:1,mistakes:0,streak:1});
  expect(oral.cards.find(c=>c.skillId===id)!.card.reps).toBe(1);
  const wrong=recordReview(oral,id,'again');
  expect(wrong.stats[id]).toEqual({reviews:2,correct:1,mistakes:1,streak:0});
  const typed=recordReview(wrong,id,'good',false);
  expect(typed.stats[id].correct).toBe(1);
  expect(typed.totalReviews).toBe(3);
  expect(p.totalReviews).toBe(0);
 });
 it('preserves old progress and adds new grammar skills when reloading',()=>{
  const store=new Map<string,string>();
  vi.stubGlobal('localStorage',{getItem:(k:string)=>store.get(k)??null,setItem:(k:string,v:string)=>store.set(k,v)});
  const p=recordReview(freshProgress(),'verb.past','easy');
  p.cards=p.cards.filter(c=>!c.skillId.startsWith('sentence.'));
  delete p.stats['sentence.question'];delete p.stats['sentence.plural'];
  saveProgress(p);
  const loaded=loadProgress();
  expect(loaded.totalReviews).toBe(1);
  expect(loaded.stats['verb.past'].correct).toBe(1);
  expect(loaded.cards).toHaveLength(skills.length);
  expect(loaded.stats['sentence.question'].reviews).toBe(0);
 });
 it('starts the daily counter on the local calendar day',()=>{
  const p=freshProgress();p.lastDay='2000-01-01';p.reviewsToday=99;
  const next=recordReview(p,'verb.past','good');
  expect(next.reviewsToday).toBe(1);expect(next.lastDay).toBe(localDay());
 });
});

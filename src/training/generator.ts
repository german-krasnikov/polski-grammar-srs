import type {Exercise,GramCase,NumberGram,PossessiveId} from '../types';
import {nounById} from '../data/nouns';
import {personalPronouns} from '../data/pronouns';
import {courseCaseSentencePrefixes,courseExerciseCopy,courseSentenceSeeds,renderCoursePattern} from '../data/course';
import {capitalize,nounPhrase,verbForm} from '../grammar/engine';

export interface SentenceSeed {nounId:string;adjectiveId:string}
export const sentenceSeeds:SentenceSeed[]=courseSentenceSeeds;
const pick=<T,>(items:T[]):T=>items[Math.floor(Math.random()*items.length)];
export const phrase=(seed:SentenceSeed,c:GramCase,possessive:PossessiveId='my',number:NumberGram='sg')=>nounPhrase(seed.nounId,c,{adjectiveId:seed.adjectiveId,possessive,number});
export function caseSentence(seed:SentenceSeed,c:GramCase,possessive:PossessiveId='my',number:NumberGram='sg'){
 const p=phrase(seed,c,possessive,number);
 if(c==='voc')return `${capitalize(p)}!`;
 const prefix=c==='nom'?(number==='sg'?courseCaseSentencePrefixes.nomSg:courseCaseSentencePrefixes.nomPl):courseCaseSentencePrefixes[c];
 return `${prefix} ${p}.`;
}
function card(skill:string,seed:SentenceSeed,source:string,prompt:string,expected:string,explanation:string,changes:Exercise['changes'],options:Partial<Exercise>={}):Exercise{
 return {id:Math.random().toString(36).slice(2),primarySkill:skill,source,prompt,expected,explanation,changes,nounId:seed.nounId,adjectiveId:seed.adjectiveId,possessive:'my',number:'sg',tags:[skill],...options};
}
export function generateForSkill(skillId:string,preferred?:SentenceSeed):Exercise{
 let candidates=sentenceSeeds;
 if(skillId==='case.acc.f')candidates=candidates.filter(s=>nounById(s.nounId).gender==='f');
 if(skillId==='case.acc.m')candidates=candidates.filter(s=>nounById(s.nounId).gender.startsWith('m-'));
 if(skillId==='case.acc.n')candidates=candidates.filter(s=>nounById(s.nounId).gender==='n');
 if(skillId.startsWith('verb.')||skillId==='case.inst'||skillId==='case.dat'||skillId==='pronouns')candidates=candidates.filter(s=>['wife','husband','friendM','son','child'].includes(s.nounId));
 const seed=preferred&&candidates.some(s=>s.nounId===preferred.nounId)?preferred:pick(candidates);
 const noun=nounById(seed.nounId),nom=phrase(seed,'nom'),acc=phrase(seed,'acc'),gen=phrase(seed,'gen');
 const change=(from:string,to:string,reason:string)=>[{from,to,reason}];
 if(skillId==='case.acc.f'||skillId==='case.acc.m'||skillId==='case.acc.n')return card(skillId,seed,caseSentence(seed,'nom'),courseExerciseCopy.accPrompt,renderCoursePattern('seenAcc',{acc}),courseExerciseCopy.accExplanation,change(nom,acc,noun.lemma.endsWith('a')&&noun.gender==='m-personal'?courseExerciseCopy.accReasonPersonalA:noun.gender==='m-inanimate'||noun.gender==='n'?courseExerciseCopy.accReasonUnchanged:courseExerciseCopy.accReasonChanged),{tags:['acc',noun.gender]});
 if(skillId==='case.gen.neg')return card(skillId,seed,renderCoursePattern('seenAcc',{acc}),courseExerciseCopy.genNegPrompt,renderCoursePattern('seenGenNeg',{gen}),courseExerciseCopy.genNegExplanation,change(acc,gen,courseExerciseCopy.genNegReason),{tags:['gen','negation']});
 const caseDrills:Record<string,{c:GramCase;start:string;task:string}>={
  'case.inst':{c:'inst',start:courseExerciseCopy.instStart,task:courseExerciseCopy.instPrompt},
  'case.loc':{c:'loc',start:courseExerciseCopy.locStart,task:courseExerciseCopy.locPrompt},
  'case.dat':{c:'dat',start:courseExerciseCopy.datStart,task:courseExerciseCopy.datPrompt},
 };
 const drill=caseDrills[skillId];
 if(drill){const target=phrase(seed,drill.c);return card(skillId,seed,renderCoursePattern('seenAcc',{acc}),drill.task,renderCoursePattern('caseDrill',{start:drill.start,target}),courseExerciseCopy.caseDrillExplanation,change(acc,target,drill.c==='inst'?courseExerciseCopy.instReason:drill.c==='loc'?courseExerciseCopy.locReason:courseExerciseCopy.datReason),{tags:[drill.c]});}
 if(skillId==='agreement.my'){
  const owner=pick(['your','his','her','our','yourPlural','their'] as const);
  const labels={your:courseExerciseCopy.ownerYour,his:courseExerciseCopy.ownerHis,her:courseExerciseCopy.ownerHer,our:courseExerciseCopy.ownerOur,yourPlural:courseExerciseCopy.ownerYourPlural,their:courseExerciseCopy.ownerTheir};
  const target=phrase(seed,'acc',owner);
  return card(skillId,seed,renderCoursePattern('seenAcc',{acc}),`${courseExerciseCopy.ownerPromptPrefix}${labels[owner]}${courseExerciseCopy.ownerPromptSuffix}`,renderCoursePattern('seenAcc',{acc:target}),courseExerciseCopy.ownerExplanation,change(acc,target,courseExerciseCopy.ownerReason),{possessive:owner,tags:['agreement','acc']});
 }
 if(skillId.startsWith('verb.')){
  const tense=skillId.split('.')[1] as 'present'|'past'|'future',fromTense=tense==='present'?'past':'present';
  const from=verbForm('go',fromTense,3,'sg',noun.gender),to=verbForm('go',tense,3,'sg',noun.gender);
  const source=renderCoursePattern('verbSentence',{nom:capitalize(nom),verb:from}),expected=renderCoursePattern('verbSentence',{nom:capitalize(nom),verb:to});
  const accepted=tense==='future'?[renderCoursePattern('verbFutureAccepted',{nom:capitalize(nom),past:verbForm('go','past',3,'sg',noun.gender)})]:[];
  return card(skillId,seed,source,tense==='past'?courseExerciseCopy.verbPastPrompt:tense==='future'?courseExerciseCopy.verbFuturePrompt:courseExerciseCopy.verbPresentPrompt,expected,courseExerciseCopy.verbExplanation,change(from,to,courseExerciseCopy.verbReason),{accepted,tags:['verb',tense,'nom']});
 }
 if(skillId==='aspect')return card(skillId,seed,courseExerciseCopy.aspectSource,courseExerciseCopy.aspectPrompt,courseExerciseCopy.aspectExpected,courseExerciseCopy.aspectExplanation,change(courseExerciseCopy.aspectFrom,courseExerciseCopy.aspectTo,courseExerciseCopy.aspectReason),{nounId:'book',adjectiveId:'new',accepted:[courseExerciseCopy.aspectAccepted],tags:['aspect','past']});
 if(skillId==='pronouns'){
  const key=noun.gender==='f'?'ona':noun.gender==='n'?'ono':'on';
  return card(skillId,seed,renderCoursePattern('seenAcc',{acc}),courseExerciseCopy.pronounPrompt,renderCoursePattern('seenAcc',{acc:personalPronouns[key].acc}),courseExerciseCopy.pronounExplanation,change(acc,personalPronouns[key].acc,courseExerciseCopy.pronounReason),{tags:['pronoun','acc']});
 }
 if(skillId==='sentence.question')return card(skillId,seed,renderCoursePattern('questionSource',{acc}),courseExerciseCopy.questionPrompt,renderCoursePattern('questionExpected',{acc}),courseExerciseCopy.questionExplanation,change(courseExerciseCopy.questionChangeFrom,courseExerciseCopy.questionChangeTo,courseExerciseCopy.questionReason),{tags:['question','acc']});
 if(skillId==='sentence.plural')return card(skillId,seed,renderCoursePattern('seenAcc',{acc}),courseExerciseCopy.pluralPrompt,renderCoursePattern('seenAcc',{acc:phrase(seed,'acc','my','pl')}),courseExerciseCopy.pluralExplanation,change(acc,phrase(seed,'acc','my','pl'),courseExerciseCopy.pluralReason),{number:'pl',tags:['plural','acc']});
 if(skillId==='mixed')return card(skillId,seed,renderCoursePattern('seenAcc',{acc}),courseExerciseCopy.mixedPrompt,renderCoursePattern('mixedMale',{gen}),courseExerciseCopy.mixedExplanation,[{from:courseExerciseCopy.mixedChangeFrom,to:courseExerciseCopy.mixedChangeTo,reason:courseExerciseCopy.mixedVerbReason},{from:acc,to:gen,reason:courseExerciseCopy.mixedNounReason}],{accepted:[renderCoursePattern('mixedFemale',{gen})],tags:['mixed','gen','past']});
 throw new Error(`Unknown skill ${skillId}`);
}

export function generateChain(seed:SentenceSeed=sentenceSeeds[0]):Exercise[]{
 const nom=phrase(seed,'nom'),acc=phrase(seed,'acc'),gen=phrase(seed,'gen'),theirGen=phrase(seed,'gen','their'),theirLoc=phrase(seed,'loc','their');
 const make=(skill:string,source:string,prompt:string,expected:string,from:string,to:string,reason:string,options:Partial<Exercise>={})=>card(skill,seed,source,prompt,expected,reason,[{from,to,reason}],options);
 return [
  make(nounById(seed.nounId).gender==='f'?'case.acc.f':nounById(seed.nounId).gender==='n'?'case.acc.n':'case.acc.m',caseSentence(seed,'nom'),courseExerciseCopy.chainAccPrompt,renderCoursePattern('seenAcc',{acc}),nom,acc,courseExerciseCopy.chainAccReason,{tags:['acc']}),
  make('verb.past',renderCoursePattern('seenAcc',{acc}),courseExerciseCopy.chainPastPrompt,renderCoursePattern('chainPast',{acc}),courseExerciseCopy.chainPastChangeFrom,courseExerciseCopy.chainPastChangeTo,courseExerciseCopy.chainPastReason,{tags:['past','acc']}),
  make('case.gen.neg',renderCoursePattern('chainPast',{acc}),courseExerciseCopy.chainNegPrompt,renderCoursePattern('chainNeg',{gen}),acc,gen,courseExerciseCopy.chainNegReason,{tags:['gen','negation']}),
  make('agreement.my',renderCoursePattern('chainNeg',{gen}),courseExerciseCopy.chainOwnerPrompt,renderCoursePattern('chainOwner',{theirGen}),gen,theirGen,courseExerciseCopy.chainOwnerReason,{possessive:'their',tags:['gen','agreement']}),
  make('case.loc',renderCoursePattern('chainOwner',{theirGen}),courseExerciseCopy.chainLocPrompt,renderCoursePattern('chainLoc',{theirLoc}),theirGen,theirLoc,courseExerciseCopy.chainLocReason,{possessive:'their',tags:['loc']}),
 ];
}

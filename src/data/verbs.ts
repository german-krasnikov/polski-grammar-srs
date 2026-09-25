import type { Verb,NumberGram,Person,Gender } from '../types';
import { courseFutureAuxiliary, courseVerbs } from './course';
export const verbs: Verb[] = courseVerbs;
export const verbById=(id:string)=>{const v=verbs.find(x=>x.id===id);if(!v)throw new Error(`Unknown verb ${id}`);return v};
export function conjugate(verb:Verb,tense:'present'|'past'|'future',person:Person,num:NumberGram,speakerGender:Gender='m-personal'){
 if(tense==='present'){if(verb.aspect==='perfective')throw new Error('Perfective verbs have no present tense');if(!verb.present)throw new Error('No present');return verb.present[num][person]}
 if(tense==='future'&&verb.id===courseFutureAuxiliary.verbId)return courseFutureAuxiliary.forms[num][person];
 if(tense==='future'&&verb.futureType==='present'){if(!verb.present)throw new Error('No future');return verb.present[num][person]}
 const shortSuffix=speakerGender==='f'||speakerGender==='n';
 const suffix=num==='sg'?(person===1?(shortSuffix?'m':'em'):person===2?(shortSuffix?'ś':'eś'):''):(person===1?'śmy':person===2?'ście':'');
 const base=num==='pl'?(speakerGender==='m-personal'?verb.pastStem.mp:verb.pastStem.np):(speakerGender==='f'?verb.pastStem.f:speakerGender==='n'?verb.pastStem.n??verb.pastStem.m:verb.pastStem.m);
 if(tense==='past') return base+suffix;
 return `${courseFutureAuxiliary.forms[num][person]} ${verb.lemma}`;
}

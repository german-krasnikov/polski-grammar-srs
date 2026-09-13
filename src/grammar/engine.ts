import type { Gender,GramCase,NumberGram,Person,Tense,PossessiveId } from '../types';import {nounById} from '../data/nouns';import {adjectiveById} from '../data/adjectives';import {possessiveForm} from '../data/pronouns';import {verbById,conjugate} from '../data/verbs';
export const nounForm=(id:string,c:GramCase,n:NumberGram='sg')=>nounById(id).forms[n][c];
export const adjectiveForm=(id:string,g:Gender,c:GramCase,n:NumberGram='sg')=>{const forms=adjectiveById(id).forms[n][g];if(!forms)throw new Error(`No adjective form ${id}/${g}`);return forms[c]};
export function nounPhrase(nounId:string,c:GramCase,opts:{number?:NumberGram;adjectiveId?:string;possessive?:boolean|PossessiveId}={}){const noun=nounById(nounId);const n=opts.number??'sg';const parts:string[]=[];if(opts.possessive)parts.push(possessiveForm(opts.possessive===true?'my':opts.possessive,noun.gender,n,c));if(opts.adjectiveId)parts.push(adjectiveForm(opts.adjectiveId,noun.gender,c,n));parts.push(nounForm(nounId,c,n));return parts.join(' ')}
export const verbForm=(id:string,t:Tense,p:Person,n:NumberGram,g:Gender='m-personal')=>conjugate(verbById(id),t,p,n,g);
export const normalize=(s:string)=>s.normalize('NFC').toLocaleLowerCase('pl-PL').trim().replace(/[.!?]+$/,'').replace(/\s+/g,' ');
export const capitalize=(s:string)=>s.charAt(0).toLocaleUpperCase('pl-PL')+s.slice(1);

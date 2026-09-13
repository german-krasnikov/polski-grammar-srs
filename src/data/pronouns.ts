import type { GramCase,Gender,NumberGram,PossessiveId } from '../types';
export const personalPronouns={
 ja:{nom:'ja',gen:'mnie',dat:'mi',acc:'mnie',inst:'mną',loc:'mnie',voc:'ja'},
 ty:{nom:'ty',gen:'ciebie',dat:'ci',acc:'ciebie',inst:'tobą',loc:'tobie',voc:'ty'},
 on:{nom:'on',gen:'go',dat:'mu',acc:'go',inst:'nim',loc:'nim',voc:'on'},
 ona:{nom:'ona',gen:'jej',dat:'jej',acc:'ją',inst:'nią',loc:'niej',voc:'ona'},
 ono:{nom:'ono',gen:'go',dat:'mu',acc:'je',inst:'nim',loc:'nim',voc:'ono'},
 my:{nom:'my',gen:'nas',dat:'nam',acc:'nas',inst:'nami',loc:'nas',voc:'my'},
 wy:{nom:'wy',gen:'was',dat:'wam',acc:'was',inst:'wami',loc:'was',voc:'wy'},
 oni:{nom:'oni',gen:'ich',dat:'im',acc:'ich',inst:'nimi',loc:'nich',voc:'oni'},
 one:{nom:'one',gen:'ich',dat:'im',acc:'je',inst:'nimi',loc:'nich',voc:'one'}
} as const;
const mySg:Record<Gender,Record<GramCase,string>>={
 'm-personal':{nom:'mój',gen:'mojego',dat:'mojemu',acc:'mojego',inst:'moim',loc:'moim',voc:'mój'},'m-animate':{nom:'mój',gen:'mojego',dat:'mojemu',acc:'mojego',inst:'moim',loc:'moim',voc:'mój'},'m-inanimate':{nom:'mój',gen:'mojego',dat:'mojemu',acc:'mój',inst:'moim',loc:'moim',voc:'mój'},f:{nom:'moja',gen:'mojej',dat:'mojej',acc:'moją',inst:'moją',loc:'mojej',voc:'moja'},n:{nom:'moje',gen:'mojego',dat:'mojemu',acc:'moje',inst:'moim',loc:'moim',voc:'moje'}};
export function possessiveMy(g:Gender,n:NumberGram,c:GramCase){if(n==='sg')return mySg[g][c];const mp=g==='m-personal';return c==='nom'||c==='voc'||(c==='acc'&&!mp)?(mp?'moi':'moje'):c==='dat'?'moim':c==='inst'?'moimi':'moich'}
export const possessives:{id:PossessiveId;label:string}[]=[{id:'my',label:'mój — мой'},{id:'your',label:'twój — твой'},{id:'his',label:'jego — его'},{id:'her',label:'jej — её'},{id:'our',label:'nasz — наш'},{id:'yourPlural',label:'wasz — ваш'},{id:'their',label:'ich — их'}];
export function possessiveForm(id:PossessiveId,g:Gender,n:NumberGram,c:GramCase):string{
 if(id==='his')return 'jego';if(id==='her')return 'jej';if(id==='their')return 'ich';
 const form=possessiveMy(g,n,c);if(id==='my')return form;
 if(id==='your')return form==='mój'?'twój':form.replace(/^mo/,'two');
 const forms:Record<string,string>={mój:'nasz',moja:'nasza',moje:'nasze',mojego:'naszego',mojej:'naszej',mojemu:'naszemu',moją:'naszą',moim:'naszym',moi:'nasi',moich:'naszych',moimi:'naszymi'};
 return id==='our'?forms[form]:forms[form].replace(/^nas/,'was');
}

import type { Verb,NumberGram,Person,Gender } from '../types';
const V=(id:string,lemma:string,meaning:string,present:string[],past:string[],futureType:Verb['futureType']='compound',aspect:Verb['aspect']='imperfective',pair?:string):Verb=>({id,lemma,meaning,aspect,futureType,perfectivePair:pair,present:present.length?{sg:{1:present[0],2:present[1],3:present[2]},pl:{1:present[3],2:present[4],3:present[5]}}:undefined,pastStem:{m:past[0],f:past[1],n:past[2],mp:past[3],np:past[4]}});
export const verbs:Verb[]=[
V('have','mieć','иметь',['mam','masz','ma','mamy','macie','mają'],['miał','miała','miało','mieli','miały']),
V('see','widzieć','видеть',['widzę','widzisz','widzi','widzimy','widzicie','widzą'],['widział','widziała','widziało','widzieli','widziały']),
V('like','lubić','любить',['lubię','lubisz','lubi','lubimy','lubicie','lubią'],['lubił','lubiła','lubiło','lubili','lubiły']),
V('buy','kupować','покупать',['kupuję','kupujesz','kupuje','kupujemy','kupujecie','kupują'],['kupował','kupowała','kupowało','kupowali','kupowały'],'compound','imperfective','buyDone'),
V('read','czytać','читать',['czytam','czytasz','czyta','czytamy','czytacie','czytają'],['czytał','czytała','czytało','czytali','czytały']),
V('talk','mówić','говорить',['mówię','mówisz','mówi','mówimy','mówicie','mówią'],['mówił','mówiła','mówiło','mówili','mówiły']),
V('go','iść','идти',['idę','idziesz','idzie','idziemy','idziecie','idą'],['szedł','szła','szło','szli','szły']),
V('be','być','быть',['jestem','jesteś','jest','jesteśmy','jesteście','są'],['był','była','było','byli','były']),
V('buyDone','kupić','купить',['kupię','kupisz','kupi','kupimy','kupicie','kupią'],['kupił','kupiła','kupiło','kupili','kupiły'],'present','perfective'),
V('do','robić','делать',['robię','robisz','robi','robimy','robicie','robią'],['robił','robiła','robiło','robili','robiły'],'compound','imperfective','doDone'),
V('doDone','zrobić','сделать',['zrobię','zrobisz','zrobi','zrobimy','zrobicie','zrobią'],['zrobił','zrobiła','zrobiło','zrobili','zrobiły'],'present','perfective')
];
export const verbById=(id:string)=>{const v=verbs.find(x=>x.id===id);if(!v)throw new Error(`Unknown verb ${id}`);return v};
const bycFuture={sg:{1:'będę',2:'będziesz',3:'będzie'},pl:{1:'będziemy',2:'będziecie',3:'będą'}} as const;
export function conjugate(verb:Verb,tense:'present'|'past'|'future',person:Person,num:NumberGram,speakerGender:Gender='m-personal'){
 if(tense==='present'){if(verb.aspect==='perfective')throw new Error('Perfective verbs have no present tense');if(!verb.present)throw new Error('No present');return verb.present[num][person]}
 if(tense==='future'&&verb.id==='be')return bycFuture[num][person];
 if(tense==='future'&&verb.futureType==='present'){if(!verb.present)throw new Error('No future');return verb.present[num][person]}
 const shortSuffix=speakerGender==='f'||speakerGender==='n';
 const suffix=num==='sg'?(person===1?(shortSuffix?'m':'em'):person===2?(shortSuffix?'ś':'eś'):''):(person===1?'śmy':person===2?'ście':'');
 const base=num==='pl'?(speakerGender==='m-personal'?verb.pastStem.mp:verb.pastStem.np):(speakerGender==='f'?verb.pastStem.f:speakerGender==='n'?verb.pastStem.n??verb.pastStem.m:verb.pastStem.m);
 if(tense==='past') return base+suffix;
 return `${bycFuture[num][person]} ${verb.lemma}`;
}

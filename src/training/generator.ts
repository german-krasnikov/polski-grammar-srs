import type {Exercise,GramCase,NumberGram,PossessiveId} from '../types';
import {nounById} from '../data/nouns';
import {personalPronouns} from '../data/pronouns';
import {capitalize,nounPhrase,verbForm} from '../grammar/engine';

export interface SentenceSeed {nounId:string;adjectiveId:string}
export const sentenceSeeds:SentenceSeed[]=[
 {nounId:'wife',adjectiveId:'beautiful'},{nounId:'husband',adjectiveId:'good'},
 {nounId:'friendM',adjectiveId:'good'},{nounId:'son',adjectiveId:'small'},
 {nounId:'dog',adjectiveId:'good'},{nounId:'cat',adjectiveId:'small'},
 {nounId:'book',adjectiveId:'new'},{nounId:'car',adjectiveId:'new'},
 {nounId:'house',adjectiveId:'new'},{nounId:'phone',adjectiveId:'new'},
 {nounId:'child',adjectiveId:'small'},{nounId:'window',adjectiveId:'new'},
];
const pick=<T,>(items:T[]):T=>items[Math.floor(Math.random()*items.length)];
export const phrase=(seed:SentenceSeed,c:GramCase,possessive:PossessiveId='my',number:NumberGram='sg')=>nounPhrase(seed.nounId,c,{adjectiveId:seed.adjectiveId,possessive,number});
export function caseSentence(seed:SentenceSeed,c:GramCase,possessive:PossessiveId='my',number:NumberGram='sg'){
 const p=phrase(seed,c,possessive,number);
 const prefixes:Record<GramCase,string>={nom:number==='sg'?'To jest':'To są',gen:'Nie widzę',dat:'Przyglądam się',acc:'Widzę',inst:'Zachwycam się',loc:'Mówię o',voc:''};
 return c==='voc'?`${capitalize(p)}!`:`${prefixes[c]} ${p}.`;
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
 if(skillId==='case.acc.f'||skillId==='case.acc.m'||skillId==='case.acc.n')return card(skillId,seed,caseSentence(seed,'nom'),'Скажи, что ты видишь этого человека или этот предмет. Начни с «Widzę…».',`Widzę ${acc}.`,'Widzę требует Biernik. Согласуй притяжательное местоимение, прилагательное и существительное.',change(nom,acc,noun.lemma.endsWith('a')&&noun.gender==='m-personal'?'kolega → kolegę; местоимение и прилагательное — mojego dobrego.':noun.gender==='m-inanimate'||noun.gender==='n'?'Biernik совпадает с Mianownikiem: группа сохраняет базовую форму.':'Изменяется вся группа слов.'),{tags:['acc',noun.gender]});
 if(skillId==='case.gen.neg')return card(skillId,seed,`Widzę ${acc}.`,'Сделай всё предложение отрицательным.',`Nie widzę ${gen}.`,'Отрицание widzę переводит прямой объект из Biernika в Dopełniacz.',change(acc,gen,'Biernik → Dopełniacz'),{tags:['gen','negation']});
 const caseDrills:Record<string,{c:GramCase;start:string;task:string}>={
  'case.inst':{c:'inst',start:'Idę z',task:'Скажи, что идёшь вместе с этим человеком. Начни с «Idę z…».'},
  'case.loc':{c:'loc',start:'Mówię o',task:'Теперь скажи, что говоришь об этом. Начни с «Mówię o…».'},
  'case.dat':{c:'dat',start:'Daję prezent',task:'Скажи, что даришь этому человеку подарок. Начни с «Daję prezent…».'},
 };
 const drill=caseDrills[skillId];
 if(drill){const target=phrase(seed,drill.c);return card(skillId,seed,`Widzę ${acc}.`,drill.task,`${drill.start} ${target}.`,'Новая конструкция задаёт падеж всей группы: местоимение + прилагательное + существительное.',change(acc,target,drill.c==='inst'?'z kim? czym? — Narzędnik':drill.c==='loc'?'o kim? czym? — Miejscownik':'komu? czemu? — Celownik'),{tags:[drill.c]});}
 if(skillId==='agreement.my'){
  const owner=pick(['your','his','her','our','yourPlural','their'] as const);
  const labels={your:'твой / твоя',his:'его',her:'её',our:'наш / наша',yourPlural:'ваш / ваша',their:'их'};
  const target=phrase(seed,'acc',owner);
  return card(skillId,seed,`Widzę ${acc}.`,`Замени «мой / моя» на «${labels[owner]}». Сохрани время и смысл предложения.`,`Widzę ${target}.`,'mój, twój, nasz, wasz согласуются с предметом обладания. jego, jej, ich не склоняются.',change(acc,target,'Меняем владельца, сохраняем Biernik.'),{possessive:owner,tags:['agreement','acc']});
 }
 if(skillId.startsWith('verb.')){
  const tense=skillId.split('.')[1] as 'present'|'past'|'future',fromTense=tense==='present'?'past':'present';
  const from=verbForm('go',fromTense,3,'sg',noun.gender),to=verbForm('go',tense,3,'sg',noun.gender);
  const source=`${capitalize(nom)} ${from} do domu.`,expected=`${capitalize(nom)} ${to} do domu.`;
  const accepted=tense==='future'?[`${capitalize(nom)} będzie ${verbForm('go','past',3,'sg',noun.gender)} do domu.`]:[];
  return card(skillId,seed,source,tense==='past'?'Перенеси всё предложение в прошедшее время.':tense==='future'?'Перенеси всё предложение в будущее время. Сохрани несовершенный вид.':'Перенеси всё предложение в настоящее время.',expected,'Подлежащее остаётся в Mianowniku. В прошедшем времени глагол согласуется с ним по роду.',change(from,to,'Меняется время глагола, остальные слова сохраняются.'),{accepted,tags:['verb',tense,'nom']});
 }
 if(skillId==='aspect')return card(skillId,seed,'Dzisiaj kupuję nową książkę.','Скажи, что вчера уже купил эту книгу: завершённый результат. Начни с «Wczoraj…».','Wczoraj kupiłem nową książkę.','Процесс kupować → завершённый результат kupić. Форма kupiłam также правильная.',change('kupuję','kupiłem / kupiłam','Совершенный вид + прошедшее время.'),{nounId:'book',adjectiveId:'new',accepted:['Wczoraj kupiłam nową książkę.'],tags:['aspect','past']});
 if(skillId==='pronouns'){
  const key=noun.gender==='f'?'ona':noun.gender==='n'?'ono':'on';
  return card(skillId,seed,`Widzę ${acc}.`,'Замени всю группу после «Widzę» одним личным местоимением.',`Widzę ${personalPronouns[key].acc}.`,'Местоимение заменяет всю группу слов и остаётся в Bierniku.',change(acc,personalPronouns[key].acc,'кого? что?'),{tags:['pronoun','acc']});
 }
 if(skillId==='sentence.question')return card(skillId,seed,`Widzisz ${acc}.`,'Сделай вопрос, на который можно ответить «да» или «нет». Начни с «Czy…».',`Czy widzisz ${acc}?`,'Czy превращает утверждение в общий вопрос. Падеж объекта сохраняется.',change('Widzisz','Czy widzisz','Добавь вопросительное czy.'),{tags:['question','acc']});
 if(skillId==='sentence.plural')return card(skillId,seed,`Widzę ${acc}.`,'Поставь всю группу после «Widzę» во множественное число.',`Widzę ${phrase(seed,'acc','my','pl')}.`,'Во множественном числе мужские личные формы отличаются от остальных: moich dobrych kolegów, но moje nowe książki.',change(acc,phrase(seed,'acc','my','pl'),'Меняются число и согласование всей группы.'),{number:'pl',tags:['plural','acc']});
 if(skillId==='mixed')return card(skillId,seed,`Widzę ${acc}.`,'Скажи «ты не видел…» в прошедшем времени. Сохрани «мой / моя».',`Nie widziałeś ${gen}.`,'TY + прошедшее время + отрицание. Отрицание требует Dopełniacza всей группы слов.',[{from:'Widzę',to:'Nie widziałeś / Nie widziałaś',reason:'Лицо + время + отрицание'},{from:acc,to:gen,reason:'Biernik → Dopełniacz'}],{accepted:[`Nie widziałaś ${gen}.`],tags:['mixed','gen','past']});
 throw new Error(`Unknown skill ${skillId}`);
}

export function generateChain(seed:SentenceSeed=sentenceSeeds[0]):Exercise[]{
 const nom=phrase(seed,'nom'),acc=phrase(seed,'acc'),gen=phrase(seed,'gen'),theirGen=phrase(seed,'gen','their'),theirLoc=phrase(seed,'loc','their');
 const make=(skill:string,source:string,prompt:string,expected:string,from:string,to:string,reason:string,options:Partial<Exercise>={})=>card(skill,seed,source,prompt,expected,reason,[{from,to,reason}],options);
 return [
  make(nounById(seed.nounId).gender==='f'?'case.acc.f':nounById(seed.nounId).gender==='n'?'case.acc.n':'case.acc.m',`To jest ${nom}.`,'Скажи, что видишь это. Начни с «Widzę…».',`Widzę ${acc}.`,nom,acc,'Widzę → Biernik.',{tags:['acc']}),
  make('verb.past',`Widzę ${acc}.`,'Перенеси предложение в прошедшее время. Говори от мужского лица.',`Widziałem ${acc}.`,'Widzę','Widziałem','Меняется только время глагола.',{tags:['past','acc']}),
  make('case.gen.neg',`Widziałem ${acc}.`,'Теперь сделай это предложение отрицательным.',`Nie widziałem ${gen}.`,acc,gen,'Отрицание → Biernik меняется на Dopełniacz.',{tags:['gen','negation']}),
  make('agreement.my',`Nie widziałem ${gen}.`,'Замени «мой / моя» на «их».',`Nie widziałem ${theirGen}.`,gen,theirGen,'ich не склоняется; прилагательное и существительное остаются в Dopełniaczu.',{possessive:'their',tags:['gen','agreement']}),
  make('case.loc',`Nie widziałem ${theirGen}.`,'Теперь скажи, что говоришь об этом. Начни с «Mówię o…».',`Mówię o ${theirLoc}.`,theirGen,theirLoc,'o → Miejscownik. Сохрани владельца «их».',{possessive:'their',tags:['loc']}),
 ];
}

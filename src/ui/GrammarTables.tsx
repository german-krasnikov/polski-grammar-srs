import {useState} from 'react';
import type {Gender,GramCase,NumberGram,Person,PossessiveId} from '../types';
import {nouns,nounById} from '../data/nouns';
import {adjectives} from '../data/adjectives';
import {personalPronouns,possessives,possessiveForm} from '../data/pronouns';
import {verbs} from '../data/verbs';
import {nounPhrase,verbForm} from '../grammar/engine';
import {caseSentence} from '../training/generator';

export const caseRows:{id:GramCase;pl:string;ru:string;question:string;trigger:string;skill?:string}[]=[
 {id:'nom',pl:'Mianownik',ru:'Именительный',question:'kto? co?',trigger:'To jest…'},
 {id:'gen',pl:'Dopełniacz',ru:'Родительный',question:'kogo? czego?',trigger:'nie widzę / nie mam / bez / do',skill:'case.gen.neg'},
 {id:'dat',pl:'Celownik',ru:'Дательный',question:'komu? czemu?',trigger:'daję / pomagam',skill:'case.dat'},
 {id:'acc',pl:'Biernik',ru:'Винительный',question:'kogo? co?',trigger:'widzę / mam / lubię',skill:'case.acc.f'},
 {id:'inst',pl:'Narzędnik',ru:'Творительный',question:'kim? czym?',trigger:'z (с) / jestem',skill:'case.inst'},
 {id:'loc',pl:'Miejscownik',ru:'Предложный',question:'o kim? o czym?',trigger:'o / w / na (место)',skill:'case.loc'},
 {id:'voc',pl:'Wołacz',ru:'Обращение',question:'обращаемся к кому-то',trigger:'Żono! Kolego!'},
];
export const genderNames:Record<Gender,string>={'m-personal':'мужской · человек','m-animate':'мужской · животное','m-inanimate':'мужской · предмет',f:'женский',n:'средний'};

export function CaseReference({nounId,adjectiveId,owner='my',number='sg',active=[]}:{nounId:string;adjectiveId:string;owner?:PossessiveId;number?:NumberGram;active?:string[]}){
 const noun=nounById(nounId);
 return <><p className="reference-type">{noun.lemma} · {genderNames[noun.gender]} · {number==='sg'?'ед. ч.':'мн. ч.'}</p><div className="table-scroll"><table className="compact-table"><thead><tr><th>Падеж</th><th>Вся группа слов</th></tr></thead><tbody>{caseRows.map(c=><tr key={c.id} className={active.includes(c.id)?'highlight-row':''}><th scope="row">{c.pl}<small>{c.ru}</small></th><td lang="pl">{nounPhrase(nounId,c.id,{adjectiveId,possessive:owner,number})}</td></tr>)}</tbody></table></div><p className="muted small">Местоимение и прилагательное согласуются с существительным. jego, jej, ich не изменяются.</p></>;
}

export default function GrammarTables({onTrain}:{onTrain:(skill:string,nounId?:string,adjectiveId?:string)=>void}){
 const [section,setSection]=useState<'map'|'cases'|'verbs'|'pronouns'>('map');
 const [nounId,setNounId]=useState('wife'),[adjectiveId,setAdjectiveId]=useState('beautiful');
 const [number,setNumber]=useState<NumberGram>('sg'),[owner,setOwner]=useState<PossessiveId>('my');
 const [verbId,setVerbId]=useState('do'),[feminine,setFeminine]=useState(false);
 const seed={nounId,adjectiveId};
 const subjects:{label:string;p:Person;n:NumberGram;g:Gender}[]=[
  {label:'ja — я',p:1,n:'sg',g:feminine?'f':'m-personal'},
  {label:'ty — ты',p:2,n:'sg',g:feminine?'f':'m-personal'},
  {label:'on — он',p:3,n:'sg',g:'m-personal'},{label:'ona — она',p:3,n:'sg',g:'f'},{label:'ono — оно',p:3,n:'sg',g:'n'},
  {label:'my — мы',p:1,n:'pl',g:feminine?'f':'m-personal'},{label:'wy — вы',p:2,n:'pl',g:feminine?'f':'m-personal'},
  {label:'oni — мужская личная группа',p:3,n:'pl',g:'m-personal'},{label:'one — остальные',p:3,n:'pl',g:'f'},
 ];
 return <main className="matrix-page">
  <div className="page-heading"><div><h2>Грамматическая матрица</h2><p>Один небольшой словарь. Видно, что меняется при каждой операции.</p></div></div>
  <div className="subnav" aria-label="Разделы матрицы">{([['map','Карта системы'],['cases','Падежи и окончания'],['verbs','Времена и лица'],['pronouns','Местоимения']] as const).map(([id,label])=><button key={id} className={section===id?'active':''} aria-pressed={section===id} onClick={()=>setSection(id)}>{label}</button>)}</div>
  {section==='map'&&<>
   <section className="card matrix-section"><h3>Сначала конструкция, затем формы</h3><div className="rule-pipeline"><div><small>01 · Смысл</small><strong>Что хочу сказать?</strong><span>Вижу / не вижу / говорю о…</span></div><b aria-hidden="true">→</b><div><small>02 · Операция</small><strong>Какой падеж нужен?</strong><span>widzę → Biernik</span></div><b aria-hidden="true">→</b><div><small>03 · Согласование</small><strong>Меняю всю группу</strong><span>moją + piękną + żonę</span></div></div>
   <div className="system-grid"><article><h4>Существительное</h4><p>Род × число × падеж</p><code>żona → żonę → żony</code></article><article><h4>Прилагательное и владелец</h4><p>Копируют род, число и падеж</p><code>moja piękna → moją piękną</code></article><article><h4>Глагол</h4><p>Лицо × число × время × вид</p><code>widzę → widziałem → będę widzieć</code></article><article><h4>Модификаторы</h4><p>Отрицание · вопрос · владелец</p><code>Widzę… → Nie widzę… → Czy widzę…?</code></article></div>
   </section>
   <section className="card matrix-section"><h3>Одна мысль, пять преобразований</h3><div className="table-scroll"><table><thead><tr><th>Операция</th><th>Целое предложение</th><th>Что изменилось</th></tr></thead><tbody>
    <tr><th>База</th><td lang="pl">Widzę moją piękną żonę.</td><td>Biernik всей группы</td></tr>
    <tr><th>Прошедшее</th><td lang="pl">Widziałem moją piękną żonę.</td><td>widzę → widziałem</td></tr>
    <tr><th>Отрицание</th><td lang="pl">Nie widziałem mojej pięknej żony.</td><td>вся группа: Biernik → Dopełniacz</td></tr>
    <tr><th>Их вместо моей</th><td lang="pl">Nie widziałem ich pięknej żony.</td><td>mojej → ich; ich не склоняется</td></tr>
    <tr><th>Говорю о…</th><td lang="pl">Mówię o ich pięknej żonie.</td><td>o → Miejscownik</td></tr>
   </tbody></table></div><button onClick={()=>onTrain('chain','wife','beautiful')}>Тренировать эту цепочку</button></section>
   <section className="card matrix-section"><h3>Мужской Biernik: дерево решений</h3><p>Для единственного числа сначала определи тип существительного.</p><div className="decision-grid"><article><span>Человек</span><h4>mąż / kolega</h4><p lang="pl">Widzę mojego dobrego męża.<br/>Widzę mojego dobrego kolegę.</p><small>На согласную: как Gen. На -a: -ę у существительного, но -ego у местоимения и прилагательного.</small></article><article><span>Животное</span><h4>pies / kot</h4><p lang="pl">Widzę mojego dobrego psa.</p><small>Винительный как родительный: pies → psa.</small></article><article><span>Предмет</span><h4>dom / samochód</h4><p lang="pl">Widzę mój nowy samochód.</p><small>Винительный как именительный: группа сохраняет базовую форму.</small></article></div><button onClick={()=>onTrain('case.acc.m','friendM','good')}>Тренировать мужской род</button></section>
   <section className="card matrix-section"><h3>Опора на русский: что переносится, а что проверить</h3><div className="table-scroll"><table><thead><tr><th>Русская опора</th><th>Польская конструкция</th><th>Проверка</th></tr></thead><tbody><tr><td>вижу кого? что?</td><td>Widzę moją żonę.</td><td>Логика винительного знакома; польские окончания нужно менять во всей группе.</td></tr><tr><td>с моей женой</td><td>z moją żoną</td><td>Польское женское -ą соответствует здесь творительному; это же окончание есть у прилагательного в Bierniku.</td></tr><tr><td>говорю о жене</td><td>mówię o żonie</td><td>Местный падеж требует предлога; żona → żonie.</td></tr><tr><td>мой / его / их</td><td>moją żonę / jego żonę / ich żonę</td><td>jego, jej, ich не склоняются. Формы mojego и mojej зависят от предмета обладания.</td></tr></tbody></table></div></section>
  </>}
  {section==='cases'&&<>
   <section className="card matrix-section"><h3>Все семь падежей на одной группе слов</h3><div className="table-controls"><label>Эталонное слово<select value={nounId} onChange={e=>setNounId(e.target.value)}>{nouns.map(n=><option key={n.id} value={n.id}>{n.lemma} — {n.meaning}</option>)}</select></label><label>Прилагательное<select value={adjectiveId} onChange={e=>setAdjectiveId(e.target.value)}>{adjectives.map(a=><option key={a.id} value={a.id}>{a.lemma} — {a.meaning}</option>)}</select></label><label>Владелец<select value={owner} onChange={e=>setOwner(e.target.value as PossessiveId)}>{possessives.map(p=><option key={p.id} value={p.id}>{p.label}</option>)}</select></label><label>Число<select value={number} onChange={e=>setNumber(e.target.value as NumberGram)}><option value="sg">Единственное</option><option value="pl">Множественное</option></select></label></div>
   <div className="table-scroll"><table><thead><tr><th>Падеж · русская опора</th><th>Вопрос / конструкция</th><th>Местоимение + прилагательное + существительное</th><th>Целое предложение</th></tr></thead><tbody>{caseRows.map(c=><tr key={c.id}><th scope="row">{c.pl}<small>{c.ru}</small></th><td>{c.question}<small>{c.trigger}</small></td><td lang="pl" className="polish-form">{nounPhrase(nounId,c.id,{adjectiveId,possessive:owner,number})}</td><td lang="pl">{caseSentence(seed,c.id,owner,number)}</td></tr>)}</tbody></table></div><p className="muted">Wołacz показан как форма обращения; с неодушевлёнными словами обычно используется только стилистически. «Zachwycam się…» = «Восхищаюсь…» (Narzędnik), «Przyglądam się…» = «Присматриваюсь к…» (Celownik).</p>
   <button onClick={()=>onTrain('case.gen.neg',nounId,adjectiveId)}>Тренировать отрицание с этим словом</button></section>
   <section className="card matrix-section"><h3>Сравнение типов склонения · {number==='sg'?'единственное':'множественное'} число</h3><p>Читай по строке, чтобы сравнить типы. По столбцу — чтобы увидеть все формы одного слова.</p><div className="table-scroll"><table className="comparison-table"><thead><tr><th>Падеж</th>{['husband','friendM','dog','house','wife','book','child'].map(id=><th key={id}>{nounById(id).lemma}<small>{genderNames[nounById(id).gender]}</small></th>)}</tr></thead><tbody>{caseRows.map(c=><tr key={c.id}><th scope="row">{c.pl}</th>{['husband','friendM','dog','house','wife','book','child'].map(id=><td key={id} lang="pl">{nounById(id).forms[number][c.id]}</td>)}</tr>)}</tbody></table></div></section>
  </>}
  {section==='verbs'&&<>
   <section className="card matrix-section"><h3>Лицо × число × время</h3><div className="table-controls"><label>Глагол<select value={verbId} onChange={e=>setVerbId(e.target.value)}>{verbs.filter(v=>v.aspect==='imperfective').map(v=><option key={v.id} value={v.id}>{v.lemma} — {v.meaning}</option>)}</select></label><label>Род для ja / ty / my / wy<select value={feminine?'f':'m'} onChange={e=>setFeminine(e.target.value==='f')}><option value="m">Мужской / мужская личная группа</option><option value="f">Женский / женская группа</option></select></label></div><div className="table-scroll"><table><thead><tr><th>Кто</th><th>Teraz · сейчас</th><th>Przeszłość · прошлое</th><th>Przyszłość · будущее</th></tr></thead><tbody>{subjects.map(s=><tr key={s.label}><th>{s.label}</th>{(['present','past','future'] as const).map(t=><td key={t} lang="pl">{verbForm(verbId,t,s.p,s.n,s.g)}</td>)}</tr>)}</tbody></table></div><p>Составное будущее: będę + инфинитив (będę robić) или форма на -ł с родом и числом (będę robił / robiła). У być: będę, без второго глагола.</p></section>
   <section className="card matrix-section"><h3>Время меняется, предложение остаётся целым</h3><div className="table-scroll"><table><thead><tr><th>Операция</th><th>Предложение</th></tr></thead><tbody><tr><th>Сейчас</th><td lang="pl">Moja piękna żona idzie do domu.</td></tr><tr><th>Прошедшее</th><td lang="pl">Moja piękna żona szła do domu.</td></tr><tr><th>Будущее · процесс</th><td lang="pl">Moja piękna żona będzie szła do domu.</td></tr><tr><th>Отрицание</th><td lang="pl">Moja piękna żona nie idzie do domu.</td></tr><tr><th>Вопрос</th><td lang="pl">Czy moja piękna żona idzie do domu?</td></tr></tbody></table></div><button onClick={()=>onTrain('verb.past','wife','beautiful')}>Тренировать времена предложениями</button></section>
   <section className="card matrix-section"><h3>Вид: процесс или результат</h3><div className="table-scroll"><table><thead><tr><th>Смысл</th><th>Настоящее</th><th>Прошедшее</th><th>Будущее</th></tr></thead><tbody><tr><th>robić · процесс</th><td>robię</td><td>robiłem / robiłam</td><td>będę robić</td></tr><tr><th>zrobić · результат</th><td>Нет настоящего времени</td><td>zrobiłem / zrobiłam</td><td>zrobię</td></tr><tr><th>kupować · процесс</th><td>kupuję</td><td>kupowałem / kupowałam</td><td>będę kupować</td></tr><tr><th>kupić · результат</th><td>Нет настоящего времени</td><td>kupiłem / kupiłam</td><td>kupię</td></tr></tbody></table></div></section>
  </>}
  {section==='pronouns'&&<>
   <section className="card matrix-section"><h3>Личные местоимения в конструкциях</h3><p>Вместо отдельной формы запоминай её место в предложении. После предлогов у местоимений третьего лица появляется n-.</p><div className="table-scroll"><table><thead><tr><th>Кто</th><th>Nie widzę…<small>Dopełniacz</small></th><th>Daję prezent…<small>Celownik</small></th><th>Widzę…<small>Biernik</small></th><th>Idę z…<small>Narzędnik</small></th><th>Mówię o…<small>Miejscownik</small></th></tr></thead><tbody>{Object.entries(personalPronouns).map(([id,p])=><tr key={id}><th>{id}</th><td>{p.gen}</td><td>{p.dat}</td><td>{p.acc}</td><td>{id==='ja'?'ze mną':`z ${p.inst}`}</td><td>o {p.loc}</td></tr>)}</tbody></table></div><p className="muted">Здесь обычные безударные формы. Для ударения или противопоставления: mnie, tobie, jego, jemu. После предлога: do niego, do niej, do nich, dla mnie, dla ciebie.</p></section>
   <section className="card matrix-section"><h3>Владелец меняется независимо от падежа</h3><div className="table-scroll"><table><thead><tr><th>Кому принадлежит</th><th>Mianownik</th><th>Widzę… · Biernik</th><th>Nie widzę… · Dopełniacz</th><th>Правило</th></tr></thead><tbody>{possessives.map(p=><tr key={p.id}><th>{p.label}</th>{(['nom','acc','gen'] as const).map(c=><td key={c}>{possessiveForm(p.id,'f','sg',c)} {c==='nom'?'piękna żona':c==='acc'?'piękną żonę':'pięknej żony'}</td>)}<td>{['his','her','their'].includes(p.id)?'Владелец не склоняется':'Согласуется с żona'}</td></tr>)}</tbody></table></div><button onClick={()=>onTrain('agreement.my','wife','beautiful')}>Тренировать смену владельца</button></section>
  </>}
 </main>;
}

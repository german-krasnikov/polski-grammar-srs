import {Fragment,useState} from 'react';
import type {Gender,GramCase,NumberGram,Person,PossessiveId} from '../types';
import {nouns,nounById} from '../data/nouns';
import {adjectives} from '../data/adjectives';
import {possessives} from '../data/pronouns';
import {coursePronounTeaching,coursePronounContextValue,courseMatrixIntroduction,courseContextHelp,courseMaleAccIntro,courseAspectNoPresent} from '../data/course';
import {verbs} from '../data/verbs';
import {nounPhrase,verbForm} from '../grammar/engine';
import {caseSentence} from '../training/generator';
import {courseCaseRows,courseCaseTeaching,courseComparisonNounIds,courseGenderNames,courseReferenceChain,courseReferencePipeline,courseReferenceSystemCards,courseReferenceTenses,courseReferenceAspects,courseMaleAccRows,courseRussianSupport,courseVerbTeaching} from '../data/course';
import {FormContrast} from './FormContrast';

export const caseRows=courseCaseRows;
export const genderNames=courseGenderNames;

export function CaseReference({nounId,adjectiveId,owner='my',number='sg',active=[]}:{nounId:string;adjectiveId:string;owner?:PossessiveId;number?:NumberGram;active?:string[]}){
 const noun=nounById(nounId);
 const base=nounPhrase(nounId,'nom',{adjectiveId,possessive:owner,number});
 return <><p className="reference-type">{noun.lemma} · {genderNames[noun.gender]} · {number==='sg'?'ед. ч.':'мн. ч.'}</p><div className="table-scroll"><table className="compact-table"><thead><tr><th>Падеж</th><th>Базовая форма → нужная форма</th></tr></thead><tbody>{caseRows.map(c=><tr key={c.id} className={active.includes(c.id)?'highlight-row':''}><th scope="row">{c.pl}<small>{c.ru}</small></th><td lang="pl"><FormContrast from={base} to={nounPhrase(nounId,c.id,{adjectiveId,possessive:owner,number})}/></td></tr>)}</tbody></table></div><p className="muted small">{courseContextHelp.react}</p></>;
}

export default function GrammarTables({onTrain}:{onTrain:(skill:string,nounId?:string,adjectiveId?:string)=>void}){
 const [section,setSection]=useState<'map'|'cases'|'verbs'|'pronouns'>('map');
 const [nounId,setNounId]=useState('wife'),[adjectiveId,setAdjectiveId]=useState('beautiful');
 const [number,setNumber]=useState<NumberGram>('sg'),[owner,setOwner]=useState<PossessiveId>('my');
 const [verbId,setVerbId]=useState('do'),[feminine,setFeminine]=useState(false);
 const seed={nounId,adjectiveId};
 const subjects:{id:string;label:string;p:Person;n:NumberGram;g:Gender}[]=courseVerbTeaching.subjects.map(subject=>({
  id:subject.id,label:subject.label.full,p:subject.person as Person,n:subject.number as NumberGram,
  g:(subject.genderMode==='selected'?(feminine?'f':'m-personal'):subject.fixedGender) as Gender,
 }));
 return <main className="matrix-page">
  <div className="page-heading"><div><h2>Грамматическая матрица</h2><p>{courseMatrixIntroduction}</p></div></div>
  <div className="subnav" aria-label="Разделы матрицы">{([['map','Карта системы'],['cases','Падежи и окончания'],['verbs','Времена и лица'],['pronouns','Местоимения']] as const).map(([id,label])=><button key={id} className={section===id?'active':''} aria-pressed={section===id} onClick={()=>setSection(id)}>{label}</button>)}</div>
  {section==='map'&&<>
   <section className="card matrix-section"><h3>{courseReferencePipeline.title}</h3><div className="rule-pipeline">{courseReferencePipeline.steps.map((step,index)=><Fragment key={step.id}>{index>0&&<b aria-hidden="true">→</b>}<div><small>{step.label}</small><strong>{step.question}</strong><span>{step.example}</span></div></Fragment>)}</div>
   <div className="system-grid">{courseReferenceSystemCards.map(card=><article key={card.id}><h4>{card.title}</h4><p>{card.explanation}</p><code>{card.example}</code></article>)}</div>
   </section>
   <section className="card matrix-section"><h3>Одна мысль, пять преобразований</h3><div className="table-scroll"><table><thead><tr><th>Операция</th><th>Целое предложение</th><th>Что изменилось</th></tr></thead><tbody>
    {courseReferenceChain.map(row=><tr key={row.label}><th>{row.label}</th><td lang="pl"><FormContrast from={row.from} to={row.to}/></td><td>{row.change}</td></tr>)}
   </tbody></table></div><button onClick={()=>onTrain('chain','wife','beautiful')}>Тренировать эту цепочку</button></section>
   <section className="card matrix-section"><h3>Мужской Biernik: дерево решений</h3><p>{courseMaleAccIntro}</p><div className="decision-grid">{courseMaleAccRows.map(row=><article key={row.id}><span>{row.label}</span><h4>{row.title}</h4>{row.examples.map(example=><p key={example.to} lang="pl"><FormContrast from={example.from} to={example.to}/><br/>{example.sentence}</p>)}<small>{row.rule}</small></article>)}</div><button onClick={()=>onTrain('case.acc.m','friendM','good')}>Тренировать мужской род</button></section>
   <section className="card matrix-section"><h3>{courseRussianSupport.title.full}</h3><div className="table-scroll"><table><thead><tr>{courseRussianSupport.columns.map(column=><th key={column}>{column}</th>)}</tr></thead><tbody>{courseRussianSupport.rows.map(row=><tr key={row.id}><td>{row.cue}</td><td>{row.react.construction}<div className="support-comparisons">{row.comparisons.map((pair,index)=><FormContrast key={index} {...pair}/>)}</div></td><td>{row.react.check}</td></tr>)}</tbody></table></div></section>
  </>}
  {section==='cases'&&<>
   <section className="card matrix-section"><h3>Все семь падежей на одной группе слов</h3><div className="table-controls"><label>Эталонное слово<select value={nounId} onChange={e=>setNounId(e.target.value)}>{nouns.map(n=><option key={n.id} value={n.id}>{n.lemma} — {n.meaning}</option>)}</select></label><label>Прилагательное<select value={adjectiveId} onChange={e=>setAdjectiveId(e.target.value)}>{adjectives.map(a=><option key={a.id} value={a.id}>{a.lemma} — {a.meaning}</option>)}</select></label><label>Владелец<select value={owner} onChange={e=>setOwner(e.target.value as PossessiveId)}>{possessives.map(p=><option key={p.id} value={p.id}>{p.label}</option>)}</select></label><label>Число<select value={number} onChange={e=>setNumber(e.target.value as NumberGram)}><option value="sg">Единственное</option><option value="pl">Множественное</option></select></label></div>
   <div className="table-scroll"><table><thead><tr><th>Падеж · русская опора</th><th>Вопрос / конструкция</th><th>База → форма</th><th>Целое предложение</th></tr></thead><tbody>{caseRows.map(c=><tr key={c.id}><th scope="row">{c.pl}<small>{c.ru}</small></th><td>{c.question}<small>{c.trigger}</small></td><td lang="pl" className="polish-form"><FormContrast from={nounPhrase(nounId,'nom',{adjectiveId,possessive:owner,number})} to={nounPhrase(nounId,c.id,{adjectiveId,possessive:owner,number})}/></td><td lang="pl"><FormContrast from={caseSentence(seed,'nom',owner,number)} to={caseSentence(seed,c.id,owner,number)}/></td></tr>)}</tbody></table></div><p className="muted">{courseCaseTeaching.caseNote.react}</p>
   <button onClick={()=>onTrain('case.gen.neg',nounId,adjectiveId)}>Тренировать отрицание с этим словом</button></section>
   <section className="card matrix-section"><h3>Сравнение типов склонения · {number==='sg'?'единственное':'множественное'} число</h3><p>{courseCaseTeaching.comparisonReadingHint}</p><div className="table-scroll"><table className="comparison-table"><thead><tr><th>Падеж</th>{courseComparisonNounIds.map(id=><th key={id}>{nounById(id).lemma}<small>{genderNames[nounById(id).gender]}</small></th>)}</tr></thead><tbody>{caseRows.map(c=><tr key={c.id}><th scope="row">{c.pl}</th>{courseComparisonNounIds.map(id=><td key={id} lang="pl"><FormContrast from={nounById(id).forms[number].nom} to={nounById(id).forms[number][c.id]}/></td>)}</tr>)}</tbody></table></div></section>
  </>}
  {section==='verbs'&&<>
   <section className="card matrix-section"><h3>Лицо × число × время</h3><div className="table-controls"><label>Глагол<select value={verbId} onChange={e=>setVerbId(e.target.value)}>{verbs.filter(v=>v.aspect==='imperfective').map(v=><option key={v.id} value={v.id}>{v.lemma} — {v.meaning}</option>)}</select></label><label>{courseVerbTeaching.genderControlLabel.full}<select value={feminine?'f':'m'} onChange={e=>setFeminine(e.target.value==='f')}>{courseVerbTeaching.genderOptions.map(option=><option key={option.id} value={option.id}>{option.label.full}</option>)}</select></label></div><div className="table-scroll"><table><thead><tr><th>Кто</th>{(['present','past','future'] as const).map(t=><th key={t}>{courseVerbTeaching.tenseLabels[t].full}</th>)}</tr></thead><tbody>{subjects.map(s=><tr key={s.id}><th>{s.label}</th>{(['present','past','future'] as const).map(t=><td key={t} lang="pl"><FormContrast from={verbs.find(v=>v.id===verbId)!.lemma} to={verbForm(verbId,t,s.p,s.n,s.g)}/></td>)}</tr>)}</tbody></table></div><p>{courseVerbTeaching.futureExplanation.react}</p></section>
   <section className="card matrix-section"><h3>Время меняется, предложение остаётся целым</h3><div className="table-scroll"><table><thead><tr><th>Операция</th><th>Предложение</th></tr></thead><tbody>{courseReferenceTenses.map(row=><tr key={row.label}><th>{row.label}</th><td lang="pl"><FormContrast from={row.from} to={row.to}/></td></tr>)}</tbody></table></div><button onClick={()=>onTrain('verb.past','wife','beautiful')}>Тренировать времена предложениями</button></section>
   <section className="card matrix-section"><h3>Вид: процесс или результат</h3><div className="table-scroll"><table><thead><tr><th>Смысл</th><th>Настоящее</th><th>Прошедшее</th><th>Будущее</th></tr></thead><tbody>{courseReferenceAspects.map(row=><tr key={row.from}><th>{row.label}</th>{([row.present,row.past,row.future] as const).map((form,index)=><td key={index} lang={form?'pl':undefined}>{form?<FormContrast from={row.from} to={form}/>: courseAspectNoPresent.compact}</td>)}</tr>)}</tbody></table></div></section>
  </>}
  {section==='pronouns'&&<>
   <section className="card matrix-section"><h3>{coursePronounTeaching.personal.title}</h3><p>{coursePronounTeaching.personal.intro.react}</p><div className="table-scroll"><table><thead><tr><th>Кто</th>{coursePronounTeaching.contexts.map(context=><th key={context.id}>{context.cue.full}<small>{context.caseName}</small></th>)}</tr></thead><tbody>{coursePronounTeaching.personal.pronounIds.map(id=><tr key={id}><th>{id}</th>{coursePronounTeaching.contexts.map(context=><td key={context.id}><FormContrast from={id} to={coursePronounContextValue(id,context)}/></td>)}</tr>)}</tbody></table></div><p className="muted">{coursePronounTeaching.personal.footer.react}</p></section>
   <section className="card matrix-section"><h3>{coursePronounTeaching.possessive.title}</h3><div className="table-scroll"><table><thead><tr><th>Кому принадлежит</th>{coursePronounTeaching.possessive.demo.cases.map(row=><th key={row.id}>{row.caption.web}</th>)}<th>Правило</th></tr></thead><tbody>{possessives.map(p=><tr key={p.id}><th>{p.label}</th>{coursePronounTeaching.possessive.demo.cases.map(row=><td key={row.id}><FormContrast from={nounPhrase(coursePronounTeaching.possessive.demo.nounId,'nom',{adjectiveId:coursePronounTeaching.possessive.demo.adjectiveId,possessive:p.id,number:'sg'})} to={nounPhrase(coursePronounTeaching.possessive.demo.nounId,row.id as GramCase,{adjectiveId:coursePronounTeaching.possessive.demo.adjectiveId,possessive:p.id,number:'sg'})}/></td>)}<td>{coursePronounTeaching.possessive.demo.invariableOwnerIds.includes(p.id as 'his'|'her'|'their')?coursePronounTeaching.possessive.demo.rule.invariable:coursePronounTeaching.possessive.demo.rule.variable}</td></tr>)}</tbody></table></div><button onClick={()=>onTrain('agreement.my',coursePronounTeaching.possessive.demo.nounId,coursePronounTeaching.possessive.demo.adjectiveId)}>Тренировать смену владельца</button></section>
  </>}
 </main>;
}

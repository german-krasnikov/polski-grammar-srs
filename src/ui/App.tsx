import {useEffect,useMemo,useRef,useState} from 'react';
import type {Exercise,RatingName} from '../types';
import {skills,skillById} from '../training/skills';
import {generateForSkill,generateChain,sentenceSeeds,type SentenceSeed} from '../training/generator';
import {evaluate} from '../training/evaluator';
import {isDue,preview} from '../srs/scheduler';
import {exportProgress,freshProgress,loadProgress,saveProgress,localDay} from '../progress/storage';
import {recordReview} from '../progress/review';
import {nextSkillId} from '../training/queue';
import {nounById} from '../data/nouns';
import GrammarTables,{CaseReference} from './GrammarTables';
import {changeHighlightParts, sentenceHighlightParts} from './endingHighlight';
import {courseChainPresentation,coursePresentations,type ExplanationMethod} from '../data/course';
import {FormContrast} from './FormContrast';
import VocabularyView from './VocabularyView';
import './style.css';

const grades:{id:RatingName;label:string;hint:string}[]=[{id:'again',label:'Повторить',hint:'Ошибка или не уверен'},{id:'good',label:'Вспомнил',hint:'Воспроизвёл сам'}];
const dueText=(d:Date)=>{const m=Math.max(1,Math.round((d.getTime()-Date.now())/60000));return m<60?`${m} мин`:m<2880?`${Math.round(m/60)} ч`:`${Math.round(m/1440)} дн`};
type Mode='chain'|'schedule'|'focused';
export default function App(){
 const [progress,setProgress]=useState(loadProgress);
 const [mode,setMode]=useState<Mode>('chain');
 const [chain,setChain]=useState(()=>generateChain());
 const [chainStep,setChainStep]=useState(0),[chainComplete,setChainComplete]=useState(false);
 const [seedIndex,setSeedIndex]=useState(0);
 const [exercise,setExercise]=useState<Exercise>(()=>chain[0]);
 const [tab,setTab]=useState<'train'|'words'|'matrix'|'progress'>('train');
 const [answer,setAnswer]=useState(''),[revealed,setRevealed]=useState(false),[answerMode,setAnswerMode]=useState<'oral'|'typed'>('oral');
 const [introPending,setIntroPending]=useState(()=>progress.stats[chain[0].primarySkill]?.reviews===0);
 const [showReference,setShowReference]=useState(false),[showSkills,setShowSkills]=useState(false);
 const [explanationMethod,setExplanationMethod]=useState<ExplanationMethod>(()=>{
  try{return localStorage.getItem('polski-explanation-method-v1')==='situations'?'situations':'logic'}catch{return 'logic'}
 });
 const [message,setMessage]=useState('');
 const rated=useRef<string|null>(null),revealButton=useRef<HTMLButtonElement>(null);
 const [,tick]=useState(0);
 useEffect(()=>{const timer=setInterval(()=>tick(n=>n+1),30000);return()=>clearInterval(timer)},[]);
 const skill=skillById(exercise.primarySkill),sc=progress.cards.find(c=>c.skillId===exercise.primarySkill)!;
 const presentation=coursePresentations[skill.id];
 const methodContent=presentation.methods[explanationMethod];
 useEffect(()=>{try{localStorage.setItem('polski-explanation-method-v1',explanationMethod)}catch{/* preference remains in memory */}},[explanationMethod]);
 useEffect(()=>{
  if(tab==='train'&&!introPending&&!revealed&&!chainComplete)revealButton.current?.focus();
 },[exercise.id,introPending,revealed,chainComplete,tab]);
 const intervals=useMemo(()=>preview(sc),[sc]);
 const dueCount=progress.cards.filter(isDue).length;
 const today=progress.lastDay===localDay()?progress.reviewsToday:0;
 const evaluation=useMemo(()=>evaluate(answer,exercise),[answer,exercise]);
 const clearAnswer=()=>{setAnswer('');setRevealed(false);rated.current=null;};
 const showExercise=(ex:Exercise,currentProgress=progress)=>{setExercise(ex);setIntroPending(currentProgress.stats[ex.primarySkill]?.reviews===0);clearAnswer();};
 function startChain(index=seedIndex,currentProgress=progress){const cards=generateChain(sentenceSeeds[index]);setSeedIndex(index);setChain(cards);setChainStep(0);setChainComplete(false);setMode('chain');showExercise(cards[0],currentProgress);setTab('train');}
 function automatic(){setMode('schedule');setChainComplete(false);showExercise(generateForSkill(nextSkillId(progress)));setTab('train');}
 function choose(id:string,nounId?:string,adjectiveId?:string){
  if(id==='chain'){startChain(Math.max(0,sentenceSeeds.findIndex(s=>s.nounId===nounId)));return;}
  setMode('focused');setChainComplete(false);showExercise(generateForSkill(id,nounId&&adjectiveId?{nounId,adjectiveId}:undefined));setTab('train');
 }
 function persist(np:typeof progress){setProgress(np);try{saveProgress(np);setMessage('')}catch{setMessage('Браузер не смог сохранить прогресс. Экспортируй JSON перед закрытием.')}}
 function rate(r:RatingName){
  if(!revealed||rated.current===exercise.id)return;
  rated.current=exercise.id;
  const np=recordReview(progress,exercise.primarySkill,r,answerMode==='typed'?evaluation.correct:undefined);
  persist(np);
  if(mode==='chain'){
   if(chainStep+1===chain.length){setChainComplete(true);return;}
   setChainStep(chainStep+1);showExercise(chain[chainStep+1],np);
  }else showExercise(generateForSkill(mode==='focused'?exercise.primarySkill:nextSkillId(np)),np);
 }
 useEffect(()=>{
  function keyboard(e:KeyboardEvent){
   const target=e.target as HTMLElement;
   if(tab!=='train'||chainComplete||(mode==='schedule'&&dueCount===0)||e.repeat||e.altKey||e.ctrlKey||e.metaKey)return;
   if(['INPUT','TEXTAREA','SELECT','BUTTON','A'].includes(target.tagName))return;
   if(e.code==='Space'){e.preventDefault();if(introPending)setIntroPending(false);else if(!revealed)setRevealed(true);}
   if(revealed&&['1','2'].includes(e.key)){e.preventDefault();rate(grades[Number(e.key)-1].id);}
  }
  window.addEventListener('keydown',keyboard);return()=>window.removeEventListener('keydown',keyboard);
 });
 function download(){const blob=new Blob([exportProgress(progress)],{type:'application/json'});const url=URL.createObjectURL(blob),a=document.createElement('a');a.href=url;a.download='polski-srs-progress.json';a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);}
 function reset(){if(confirm('Сбросить весь прогресс?')){const fresh=freshProgress();persist(fresh);startChain(0,fresh);}}
 return <div className="app">
  <header className="top"><div><h1>POLSKI <span>Grammar Matrix</span></h1><p>Предложение → преобразование → новое предложение</p></div><div className="topstats"><div><b>{dueCount}</b><span>к повторению</span></div><div><b>{today}</b><span>сегодня</span></div></div></header>
  <nav aria-label="Основные разделы"><button className={tab==='train'?'active':''} aria-pressed={tab==='train'} onClick={()=>setTab('train')}>Карточки</button><button className={tab==='words'?'active':''} aria-pressed={tab==='words'} onClick={()=>setTab('words')}>Слова</button><button className={tab==='matrix'?'active':''} aria-pressed={tab==='matrix'} onClick={()=>setTab('matrix')}>Таблицы и схема</button><button className={tab==='progress'?'active':''} aria-pressed={tab==='progress'} onClick={()=>setTab('progress')}>Прогресс</button></nav>
  {message&&<p role="alert" className="notice">{message}</p>}
  {tab==='train'&&<main className="study-page">
   <div className="study-toolbar"><div className="subnav" aria-label="Режим тренировки"><button className={mode==='chain'?'active':''} aria-pressed={mode==='chain'} onClick={()=>startChain()}>Цепочка предложений</button><button className={mode==='schedule'?'active':''} onClick={automatic} aria-pressed={mode==='schedule'}>По расписанию <span>{dueCount}</span></button><button className={mode==='focused'?'active':''} aria-expanded={showSkills} onClick={()=>setShowSkills(!showSkills)}>Отдельный навык</button></div><div className="study-options"><label>Подача<select aria-label="Подача объяснений" value={explanationMethod} onChange={e=>setExplanationMethod(e.target.value as ExplanationMethod)}><option value="logic">Схемы и логика</option><option value="situations">Живые ситуации</option></select></label><button className="reference-toggle" disabled={introPending} aria-expanded={showReference&&!introPending} onClick={()=>setShowReference(!showReference)}>{showReference&&!introPending?'Скрыть таблицу':'Таблица под рукой'}</button></div></div>
   {showSkills&&<section className="skill-picker" aria-label="Выбор навыка">{skills.map(s=><button key={s.id} className={s.id===exercise.primarySkill&&mode==='focused'?'active':''} onClick={()=>{choose(s.id);setShowSkills(false)}}>{s.title}<small>{s.level}</small></button>)}</section>}
   {mode==='chain'&&<div className="chain-header"><label>Один набор слов<select aria-label="Слова для цепочки" value={seedIndex} onChange={e=>startChain(Number(e.target.value))}>{sentenceSeeds.map((s,i)=><option key={s.nounId} value={i}>{nounById(s.nounId).lemma} — {nounById(s.nounId).meaning}</option>)}</select></label><ol aria-label="Шаги цепочки">{courseChainPresentation.steps.map((step,i)=><li key={step.id} className={chainComplete||i<chainStep?'done':i===chainStep?'current':''} aria-current={!chainComplete&&i===chainStep?'step':undefined}><span>{i+1}</span>{step.label}</li>)}</ol></div>}
   <div className={'study-layout '+(showReference&&!introPending?'with-reference':'')}>
    <section className="flashcard card" aria-label="Учебная карточка">
     {chainComplete?<div className="session-complete"><span className="eyebrow">{courseChainPresentation.completion.reactEyebrow}</span><h2>{courseChainPresentation.completion.title}</h2><p>{courseChainPresentation.completion.reactBody}</p><div className="chain-review">{chain.map((c,i)=><div key={c.id}><small>{i+1} · {skillById(c.primarySkill).title}</small><p lang="pl">{c.expected}</p></div>)}</div><div className="actions"><button className="primary" onClick={()=>startChain((seedIndex+1)%sentenceSeeds.length)}>Следующий набор слов</button><button onClick={automatic}>К повторениям по расписанию</button></div></div>:mode==='schedule'&&dueCount===0?<div className="session-complete"><h2>Повторения на сейчас завершены</h2><p>Следующее: {new Date(sc.card.due).toLocaleString('ru-RU')}.</p><button onClick={()=>startChain()}>Потренировать цепочку</button></div>:<>
      <div className="card-meta"><span>{mode==='chain'?`Цепочка · ${chainStep+1} / ${chain.length}`:mode==='schedule'?'Повторение по расписанию':'Тренировка навыка'}</span><span>{skill.level} · {skill.title}</span></div>
      {introPending?<div className="card-front method-introduce"><span className="eyebrow">{explanationMethod==='situations'?'Сцена и намерение':'Признаки и операция'}</span><p className="source-sentence" lang="pl">{sentenceHighlightParts(exercise.source,exercise.changes,'before').map((part,i)=>part.isChanged?<span className="change-before" key={i}>{part.text}</span>:part.text)}</p><p>{methodContent.introduce}</p><button className="primary" onClick={()=>setIntroPending(false)}>Перейти к заданию</button></div>:<>
      <div className="card-front"><span className="eyebrow">Исходное предложение</span><p className="source-sentence" lang="pl">{sentenceHighlightParts(exercise.source,exercise.changes,'before').map((part,i)=>part.isChanged?<span className="change-before" key={i}>{part.text}</span>:part.text)}</p><div className="operation"><span>{explanationMethod==='situations'?'Ситуация':'Преобразуй'}</span><h2>{exercise.prompt}</h2><p className="method-retrieve">{methodContent.retrieve}</p><small className="method-lead">{methodContent.promptLead}</small></div></div>
      {!revealed&&<div className="answer-area"><div className="answer-mode" aria-label="Как отвечать"><button className={answerMode==='oral'?'active':''} aria-pressed={answerMode==='oral'} onClick={()=>setAnswerMode('oral')}>Ответ вслух / про себя</button><button className={answerMode==='typed'?'active':''} aria-pressed={answerMode==='typed'} onClick={()=>setAnswerMode('typed')}>Напечатать ответ</button></div>{answerMode==='typed'?<textarea aria-label="Ответ по-польски" value={answer} onChange={e=>setAnswer(e.target.value)} placeholder="Напиши целое предложение…" onKeyDown={e=>{if(e.key==='Enter'&&!e.shiftKey){e.preventDefault();setRevealed(true)}}}/>:<p className="muted">Произнеси целое предложение, затем переверни карточку.</p>}<button ref={revealButton} className="primary reveal-button" onClick={()=>setRevealed(true)}>{answerMode==='typed'?'Проверить и показать ответ':'Показать ответ'}<kbd>Пробел</kbd></button></div>}
      {revealed&&<div className="card-back" aria-live="polite"><span className="eyebrow">Обратная сторона · эталон</span><p className="answer-sentence" lang="pl">{sentenceHighlightParts(exercise.expected,exercise.changes,'after').map((part,i)=>part.isChanged?<span className="change-after" key={i}>{part.text}</span>:part.text)}</p>{exercise.accepted?.length?<p className="accepted">Также: <span lang="pl">{exercise.accepted.join(' / ')}</span></p>:null}{answerMode==='typed'&&<div className={evaluation.correct?'typed-result correct':'typed-result incorrect'}><strong>{evaluation.correct?'Совпадает с правильным вариантом':'Сравни свой ответ с эталоном'}</strong><p lang="pl">{answer||'Ответ не введён'}</p></div>}<div className="method-feedback"><h3>{explanationMethod==='situations'?'Сравни смысл и форму':'Разбор изменений'}</h3><p>{methodContent.feedback}</p>{explanationMethod==='situations'&&<p>{exercise.explanation}</p>}</div><div className="change-list"><h3>Что изменилось</h3>{exercise.changes.map((c,i)=><div key={i}><div className="change-pair"><span lang="pl">{changeHighlightParts(c.from,c.to,'before').map((part,j)=>part.isChanged?<span className="change-before" key={j}>{part.text}</span>:part.text)}</span><b aria-hidden="true">→</b><strong lang="pl">{changeHighlightParts(c.from,c.to,'after').map((part,j)=>part.isChanged?<span className={part.isEnding?"ending-highlight change-after":"change-after"} key={j}>{part.text}</span>:part.text)}</strong></div><p>{c.reason}</p></div>)}</div><section className="rule-focus" aria-label="Ключевое правило"><small>ЗАПОМНИ</small><strong>{skill.formula}</strong><p>{methodContent.introduction}</p>{explanationMethod==='logic'&&<p>{exercise.explanation}</p>}<div className="rule-contrast" lang="pl"><FormContrast from={presentation.focus.before} to={presentation.focus.after}/></div></section><div className="rating-label">Когда повторить?</div><p className="method-review">{methodContent.review}</p><div className="ratings">{grades.map((g,i)=><button key={g.id} onClick={()=>rate(g.id)} className={'rating-'+g.id}><small>{g.hint}</small><b><kbd>{i+1}</kbd> {g.label}</b><span>{dueText(intervals[g.id])}</span></button>)}</div><p className="muted small">Оценка планирует следующее повторение навыка. В цепочке следующая карточка продолжает то же предложение.</p></div>}
      </>}
     </>}
    </section>
    {showReference&&!introPending&&<aside className="card reference-panel"><div className="reference-heading"><h3>Таблица этого предложения</h3><span>Можно подсматривать</span></div><CaseReference nounId={exercise.nounId} adjectiveId={exercise.adjectiveId} owner={exercise.possessive} number={exercise.number} active={exercise.tags}/><button onClick={()=>setTab('matrix')}>Все таблицы и схема</button></aside>}
   </div>
   <div className="study-help"><span><kbd>Пробел</kbd> {introPending?'перейти к заданию':'показать ответ'} · <kbd>1–2</kbd> оценить</span><span>Один навык повторяется на разных предложениях.</span></div>
  </main>}
  {tab==='words'&&<main className="vocabulary-page"><VocabularyView/></main>}
  {tab==='matrix'&&<GrammarTables onTrain={choose}/>}
  {tab==='progress'&&<main className="progress-page"><section className="card matrix-section"><h2>Прогресс</h2><div className="bigstats"><div><b>{progress.totalReviews}</b><span>всего карточек</span></div><div><b>{today}</b><span>сегодня</span></div><div><b>{dueCount}</b><span>к повторению</span></div></div><p>При ответе вслух «Повторить» считается ошибкой, «Вспомнил» — успешным воспоминанием. При печати точность определяется проверкой текста, а время следующего повтора выбираешь сам.</p><div className="actions"><button onClick={download}>Экспорт JSON</button><button onClick={reset}>Сбросить прогресс</button></div></section><section className="card matrix-section"><div className="table-scroll"><table><thead><tr><th>Навык</th><th>Повторений</th><th>Точность</th><th>Следующее повторение</th></tr></thead><tbody>{skills.map(s=>{const st=progress.stats[s.id],c=progress.cards.find(c=>c.skillId===s.id)!;return <tr key={s.id}><th><button className="text-button" onClick={()=>choose(s.id)}>{s.title}</button></th><td>{st.reviews}</td><td>{st.reviews?`${Math.round(st.correct/st.reviews*100)}%`:'—'}</td><td>{isDue(c)?'Сейчас':new Date(c.card.due).toLocaleString('ru-RU')}</td></tr>})}</tbody></table></div></section></main>}
  <footer>Прогресс сохраняется в этом браузере. Интервальные повторения — FSRS.</footer>
 </div>;
}

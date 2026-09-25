// @vitest-environment jsdom
import {afterEach,beforeEach,describe,expect,it} from 'vitest';
import {cleanup,render,screen,fireEvent} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from '../src/ui/App';
import {freshProgress,saveProgress} from '../src/progress/storage';

beforeEach(()=>localStorage.clear());
afterEach(cleanup);
const readStored=()=>JSON.parse(localStorage.getItem('polski-grammar-srs-v1')!);
const hasText=(selector:string,value:string,scope:ParentNode=document)=>
 Array.from(scope.querySelectorAll(selector)).some(node=>node.textContent===value);
const advanceIntro=async(user:ReturnType<typeof userEvent.setup>)=>{
 const button=screen.queryByRole('button',{name:'Перейти к заданию'});
 if(button)await user.click(button);
};

describe('Anki sentence experience',()=>{
 it('hides the answer, reveals it, and continues all five linked cards with saved grades',async()=>{
 const user=userEvent.setup();render(<App/>);
  await advanceIntro(user);
  expect(hasText('.source-sentence','To jest moja piękna żona.')).toBe(true);
  expect(hasText('.answer-sentence','Widzę moją piękną żonę.')).toBe(false);
  const expected=['Widzę moją piękną żonę.','Widziałem moją piękną żonę.','Nie widziałem mojej pięknej żony.','Nie widziałem ich pięknej żony.','Mówię o ich pięknej żonie.'];
  for(let i=0;i<5;i++){
   await advanceIntro(user);
   await user.click(screen.getByRole('button',{name:/Показать ответ/}));
   expect(hasText('.answer-sentence',expected[i])).toBe(true);
   await user.click(screen.getByRole('button',{name:/2 Вспомнил/}));
   expect(readStored().totalReviews).toBe(i+1);
   if(i<4){await advanceIntro(user);expect(hasText('.source-sentence',expected[i])).toBe(true);}
  }
  expect(screen.getByRole('heading',{name:'Цепочка завершена'})).toBeTruthy();
  expect(readStored().stats['verb.past'].correct).toBe(1);
  expect(screen.queryByRole('button',{name:/2 Вспомнил/})).toBeNull();
 });
 it('supports keyboard reveal/rating without rating twice',()=>{
 render(<App/>);
 fireEvent.keyDown(document.body,{key:' ',code:'Space'});
 fireEvent.keyDown(document.body,{key:' ',code:'Space'});
  expect(hasText('.answer-sentence','Widzę moją piękną żonę.')).toBe(true);
  expect(screen.getAllByRole('button',{name:/Повторить|Вспомнил/})).toHaveLength(2);
  fireEvent.keyDown(document.body,{key:'3',code:'Digit3'});
  fireEvent.keyDown(document.body,{key:'4',code:'Digit4'});
  expect(readStored()).toBeNull();
  fireEvent.keyDown(document.body,{key:'2',code:'Digit2'});
  fireEvent.keyDown(document.body,{key:'2',code:'Digit2'});
  expect(readStored().totalReviews).toBe(1);
  expect(hasText('.answer-sentence','Widziałem moją piękną żonę.')).toBe(false);
 });
 it('allows typing a whole sentence and locks it after checking',async()=>{
  const user=userEvent.setup();render(<App/>);
  await advanceIntro(user);
  await user.click(screen.getByRole('button',{name:'Напечатать ответ'}));
  await user.type(screen.getByRole('textbox',{name:'Ответ по-польски'}),'Widzę moją piękną żonę.');
  await user.keyboard('{Enter}');
  expect(screen.getByText('Совпадает с правильным вариантом')).toBeTruthy();
  expect(screen.queryByRole('textbox')).toBeNull();
  await user.click(screen.getByRole('button',{name:/2 Вспомнил/}));
  expect(readStored().stats['case.acc.f'].correct).toBe(1);
 });
 it('retains the current card while opening its case table and the full grammar map',async()=>{
  const user=userEvent.setup();render(<App/>);
  await advanceIntro(user);
  await user.click(screen.getByRole('button',{name:'Таблица под рукой'}));
  expect(screen.getByRole('table')).toBeTruthy();
  expect(hasText('.form-contrast-after','mojej pięknej żony')).toBe(true);
  await user.click(screen.getByRole('button',{name:'Таблицы и схема'}));
  expect(screen.getByRole('heading',{name:'Мужской Biernik: дерево решений'})).toBeTruthy();
  await user.click(screen.getByRole('button',{name:'Падежи и окончания'}));
  await user.selectOptions(screen.getByLabelText('Эталонное слово'),'friendM');
  await user.selectOptions(screen.getByLabelText('Прилагательное'),'good');
  await user.selectOptions(screen.getByLabelText('Владелец'),'their');
  await user.selectOptions(screen.getByLabelText('Число'),'pl');
  expect(hasText('.form-contrast-after','Widzę ich dobrych kolegów.')).toBe(true);
  expect(hasText('.form-contrast-after','Mówię o ich dobrych kolegach.')).toBe(true);
  await user.click(screen.getByRole('button',{name:'Карточки'}));
  expect(hasText('.source-sentence','To jest moja piękna żona.')).toBe(true);
  expect(screen.queryByText('ОБРАТНАЯ СТОРОНА · ЭТАЛОН')).toBeNull();
 });
 it('shows verb and pronoun matrices and launches a sentence drill from a table',async()=>{
  const user=userEvent.setup();render(<App/>);
  await user.click(screen.getByRole('button',{name:'Таблицы и схема'}));
  await user.click(screen.getByRole('button',{name:'Времена и лица'}));
  await user.selectOptions(screen.getByLabelText('Глагол'),'go');
  const table=screen.getAllByRole('table')[0];
  expect(hasText('.form-contrast-after','szedłem',table)).toBe(true);
  await user.selectOptions(screen.getByLabelText('Род для ja / ty / my / wy'),'f');
  expect(hasText('.form-contrast-after','szłam',table)).toBe(true);
  await user.click(screen.getByRole('button',{name:'Местоимения'}));
  expect(hasText('.form-contrast-after','ze mną')).toBe(true);
  expect(hasText('.form-contrast-after','ich piękną żonę')).toBe(true);
  await user.click(screen.getByRole('button',{name:'Тренировать смену владельца'}));
  await advanceIntro(user);
  expect(hasText('.source-sentence','Widzę moją piękną żonę.')).toBe(true);
  expect(screen.getByRole('heading',{name:/Замени «мой/})).toBeTruthy();
 });
 it('honours the due queue and persists across remounts',async()=>{
  const p=freshProgress();p.cards.forEach(c=>c.card.due='2099-01-01T00:00:00.000Z');saveProgress(p);
  const user=userEvent.setup();const app=render(<App/>);
  await user.click(screen.getByRole('button',{name:/По расписанию/}));
  expect(screen.getByRole('heading',{name:'Повторения на сейчас завершены'})).toBeTruthy();
  await user.click(screen.getByRole('button',{name:'Потренировать цепочку'}));
  await advanceIntro(user);
  await user.click(screen.getByRole('button',{name:/Показать ответ/}));
  await user.click(screen.getByRole('button',{name:/1 Повторить/}));
  app.unmount();render(<App/>);
  await user.click(screen.getByRole('button',{name:'Прогресс'}));
  expect(readStored().totalReviews).toBe(1);
  expect(readStored().stats['case.acc.f'].mistakes).toBe(1);
 });
});

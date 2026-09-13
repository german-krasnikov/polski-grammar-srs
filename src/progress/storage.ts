import type {Progress,SkillStats} from '../types';import {skills} from '../training/skills';import {newSkillCard} from '../srs/scheduler';
const KEY='polski-grammar-srs-v1';export const localDay=(date=new Date())=>`${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;const day=localDay;const emptyStats=():SkillStats=>({reviews:0,correct:0,streak:0,mistakes:0});
export function freshProgress():Progress{return {version:1,cards:skills.map(s=>newSkillCard(s.id)),stats:Object.fromEntries(skills.map(s=>[s.id,emptyStats()])),reviewsToday:0,lastDay:day(),totalReviews:0}}
export function loadProgress():Progress{try{const raw=localStorage.getItem(KEY);if(!raw)return freshProgress();const p=JSON.parse(raw) as Progress;if(p.lastDay!==day()){p.reviewsToday=0;p.lastDay=day()}for(const s of skills){if(!p.cards.some(c=>c.skillId===s.id))p.cards.push(newSkillCard(s.id));if(!p.stats[s.id])p.stats[s.id]=emptyStats()}return p}catch{return freshProgress()}}
export const saveProgress=(p:Progress)=>localStorage.setItem(KEY,JSON.stringify(p));export function resetProgress(){localStorage.removeItem(KEY)}
export function exportProgress(p:Progress){return JSON.stringify(p,null,2)}
export function importProgress(raw:string):Progress{const p=JSON.parse(raw) as Progress;if(!p.cards||!p.stats)throw new Error('Invalid progress file');return p}

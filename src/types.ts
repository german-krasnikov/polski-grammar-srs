import type { Card } from 'ts-fsrs';
export type GramCase='nom'|'gen'|'dat'|'acc'|'inst'|'loc'|'voc';
export type NumberGram='sg'|'pl';
export type Gender='m-personal'|'m-animate'|'m-inanimate'|'f'|'n';
export type Person=1|2|3;
export type Tense='present'|'past'|'future';
export type RatingName='again'|'hard'|'good'|'easy';
export type PossessiveId='my'|'your'|'his'|'her'|'our'|'yourPlural'|'their';
export interface Noun {id:string; lemma:string; meaning:string; gender:Gender; forms:Record<NumberGram,Record<GramCase,string>>}
export interface Adjective {id:string; lemma:string; meaning:string; forms:Record<NumberGram,Partial<Record<Gender,Record<GramCase,string>>>>}
export interface Verb {id:string; lemma:string; meaning:string; aspect:'imperfective'|'perfective'; present?:Record<NumberGram,Record<Person,string>>; pastStem:{m:string;f:string;n?:string;mp:string;np:string}; futureType:'compound'|'present'; perfectivePair?:string}
export interface Skill {id:string; title:string; group:string; level:'A1'|'A2'|'B1'|'B2'; formula:string; theory:string; hint:string; prerequisites:string[]}
export interface Exercise {id:string; primarySkill:string; source:string; prompt:string; expected:string; accepted?:string[]; explanation:string; tags:string[]; nounId:string; adjectiveId:string; possessive:PossessiveId; number:NumberGram; changes:{from:string;to:string;reason:string}[]}
export interface StoredCard {skillId:string; card:SerializedCard}
export type SerializedCard=Omit<Card,'due'|'last_review'> & {due:string; last_review?:string};
export interface SkillStats {reviews:number; correct:number; streak:number; mistakes:number}
export interface Progress {version:number; cards:StoredCard[]; stats:Record<string,SkillStats>; reviewsToday:number; lastDay:string; totalReviews:number}

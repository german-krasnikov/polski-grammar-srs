import {createEmptyCard,fsrs,Rating,type Card} from 'ts-fsrs';import type {RatingName,SerializedCard,StoredCard} from '../types';
const scheduler=fsrs({request_retention:0.9,maximum_interval:3650,enable_fuzz:true,enable_short_term:true,learning_steps:['1m','10m'],relearning_steps:['10m']});
const map={again:Rating.Again,hard:Rating.Hard,good:Rating.Good,easy:Rating.Easy} as const satisfies Record<RatingName,Rating>;
export const serialize=(c:Card):SerializedCard=>({...c,due:c.due.toISOString(),last_review:c.last_review?.toISOString()});
export const deserialize=(c:SerializedCard):Card=>({...c,due:new Date(c.due),last_review:c.last_review?new Date(c.last_review):undefined});
export const newSkillCard=(skillId:string):StoredCard=>({skillId,card:serialize(createEmptyCard())});
export function preview(sc:StoredCard){const r=scheduler.repeat(deserialize(sc.card),new Date());return {again:r[Rating.Again].card.due,hard:r[Rating.Hard].card.due,good:r[Rating.Good].card.due,easy:r[Rating.Easy].card.due}}
export function review(sc:StoredCard,r:RatingName):StoredCard{const result=scheduler.next(deserialize(sc.card),new Date(),map[r]);return {skillId:sc.skillId,card:serialize(result.card)}}
export const isDue=(sc:StoredCard)=>new Date(sc.card.due).getTime()<=Date.now();

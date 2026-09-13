import type {Progress,RatingName} from '../types';
import {review} from '../srs/scheduler';
import {localDay} from './storage';

export function recordReview(progress:Progress,skillId:string,rating:RatingName,typedCorrect?:boolean):Progress {
 const correct=typedCorrect??rating!=='again';
 const old=progress.stats[skillId];
 return {...progress,
  cards:progress.cards.map(c=>c.skillId===skillId?review(c,rating):c),
  stats:{...progress.stats,[skillId]:{reviews:old.reviews+1,correct:old.correct+(correct?1:0),mistakes:old.mistakes+(correct?0:1),streak:correct?old.streak+1:0}},
  reviewsToday:(progress.lastDay===localDay()?progress.reviewsToday:0)+1,
  lastDay:localDay(),totalReviews:progress.totalReviews+1,
 };
}

import type { Exercise } from '../types';import {normalize} from '../grammar/engine';
export interface Evaluation{correct:boolean;normalized:string;expected:string;distance:number}
function lev(a:string,b:string){const m=Array.from({length:a.length+1},()=>Array(b.length+1).fill(0));for(let i=0;i<=a.length;i++)m[i][0]=i;for(let j=0;j<=b.length;j++)m[0][j]=j;for(let i=1;i<=a.length;i++)for(let j=1;j<=b.length;j++)m[i][j]=Math.min(m[i-1][j]+1,m[i][j-1]+1,m[i-1][j-1]+(a[i-1]===b[j-1]?0:1));return m[a.length][b.length]}
export function evaluate(answer:string,ex:Exercise):Evaluation{const a=normalize(answer),variants=[ex.expected,...(ex.accepted??[])].map(normalize),best=Math.min(...variants.map(v=>lev(a,v)));return {correct:variants.includes(a),normalized:a,expected:ex.expected,distance:best}}

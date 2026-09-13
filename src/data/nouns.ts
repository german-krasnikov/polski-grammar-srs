import type { Noun } from '../types';
const N=(id:string,lemma:string,meaning:string,gender:Noun['gender'],sg:string[],pl:string[]):Noun=>({id,lemma,meaning,gender,forms:{sg:{nom:sg[0],gen:sg[1],dat:sg[2],acc:sg[3],inst:sg[4],loc:sg[5],voc:sg[6]},pl:{nom:pl[0],gen:pl[1],dat:pl[2],acc:pl[3],inst:pl[4],loc:pl[5],voc:pl[6]}}});
export const nouns:Noun[]=[
N('wife','żona','жена','f',['żona','żony','żonie','żonę','żoną','żonie','żono'],['żony','żon','żonom','żony','żonami','żonach','żony']),
N('woman','kobieta','женщина','f',['kobieta','kobiety','kobiecie','kobietę','kobietą','kobiecie','kobieto'],['kobiety','kobiet','kobietom','kobiety','kobietami','kobietach','kobiety']),
N('book','książka','книга','f',['książka','książki','książce','książkę','książką','książce','książko'],['książki','książek','książkom','książki','książkami','książkach','książki']),
N('work','praca','работа','f',['praca','pracy','pracy','pracę','pracą','pracy','praco'],['prace','prac','pracom','prace','pracami','pracach','prace']),
N('husband','mąż','муж','m-personal',['mąż','męża','mężowi','męża','mężem','mężu','mężu'],['mężowie','mężów','mężom','mężów','mężami','mężach','mężowie']),
N('friendM','kolega','коллега','m-personal',['kolega','kolegi','koledze','kolegę','kolegą','koledze','kolego'],['koledzy','kolegów','kolegom','kolegów','kolegami','kolegach','koledzy']),
N('son','syn','сын','m-personal',['syn','syna','synowi','syna','synem','synu','synu'],['synowie','synów','synom','synów','synami','synach','synowie']),
N('dog','pies','собака','m-animate',['pies','psa','psu','psa','psem','psie','psie'],['psy','psów','psom','psy','psami','psach','psy']),
N('cat','kot','кот','m-animate',['kot','kota','kotu','kota','kotem','kocie','kocie'],['koty','kotów','kotom','koty','kotami','kotach','koty']),
N('car','samochód','машина','m-inanimate',['samochód','samochodu','samochodowi','samochód','samochodem','samochodzie','samochodzie'],['samochody','samochodów','samochodom','samochody','samochodami','samochodach','samochody']),
N('house','dom','дом','m-inanimate',['dom','domu','domowi','dom','domem','domu','domu'],['domy','domów','domom','domy','domami','domach','domy']),
N('phone','telefon','телефон','m-inanimate',['telefon','telefonu','telefonowi','telefon','telefonem','telefonie','telefonie'],['telefony','telefonów','telefonom','telefony','telefonami','telefonach','telefony']),
N('child','dziecko','ребёнок','n',['dziecko','dziecka','dziecku','dziecko','dzieckiem','dziecku','dziecko'],['dzieci','dzieci','dzieciom','dzieci','dziećmi','dzieciach','dzieci']),
N('window','okno','окно','n',['okno','okna','oknu','okno','oknem','oknie','okno'],['okna','okien','oknom','okna','oknami','oknach','okna'])
];
export const nounById=(id:string)=>{const n=nouns.find(x=>x.id===id);if(!n)throw new Error(`Unknown noun ${id}`);return n};

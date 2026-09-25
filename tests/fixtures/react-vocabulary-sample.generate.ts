import { writeFileSync } from 'node:fs';
import { freshVocabulary, reviewWord, setWordSelected } from '../../src/vocabulary/storage';

const selected = setWordSelected(freshVocabulary(), 'noun.wife', true);
const reviewed = reviewWord(selected, 'noun.wife', 'ru-pl', 'good');
const key = 'pl-ru:vocabulary:ru-pl:noun.wife';
reviewed.cards[key].due = '2099-01-01T00:00:00.000Z';
reviewed.cards[key].last_review = '2026-09-23T12:00:00.000Z';
writeFileSync('tests/fixtures/react-vocabulary-sample.json', JSON.stringify(reviewed, null, 2) + '\n');

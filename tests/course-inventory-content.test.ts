import { describe, expect, it } from 'vitest';
import course from '../courses/pl-ru/course.json';
import frequency from '../courses/pl-ru/frequency-top1000.json';
import { validateCoursePack } from '../scripts/validate-course.mjs';

type NewContent = {
  reference: {
    matrixIntroduction: string;
    contextHelp: {react: string; compact: string};
    maleAccIntro: string;
    aspectNoPresent: {compact: string; ios: string};
    webCaseCompositionHeader: string;
  };
  vocabulary: {instructions: {react: string; web: string; native: string; ios: string}; unavailableLabel: string};
};

describe('remaining authored course copy', () => {
  it('retains the exact published host variants in the course pack', () => {
    const authored = course as unknown as NewContent;
    expect(authored.reference.matrixIntroduction).toBe('Один небольшой словарь. Видно, что меняется при каждой операции.');
    expect(authored.reference.contextHelp.react).toBe('Красным показана старая часть, золотым — новая. Местоимение и прилагательное согласуются с существительным.');
    expect(authored.reference.contextHelp.compact).toBe('Местоимение и прилагательное согласуются с существительным. jego, jej, ich не изменяются.');
    expect(authored.reference.maleAccIntro).toBe('Для единственного числа сначала определи тип существительного.');
    expect(authored.reference.aspectNoPresent).toEqual({compact: 'Нет настоящего времени', ios: 'Настоящее: нет настоящего времени'});
    expect(authored.reference.webCaseCompositionHeader).toBe('Местоимение + прилагательное + существительное');
    const reviewNotice = ' Исходные 32 карточки прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR.';
    expect(authored.vocabulary.instructions).toEqual({
      react: 'Частотный ранг не равен уровню CEFR. Слова без проверенного перевода и примера видны в списке, но пока не добавляются в тренировки.' + reviewNotice,
      web: 'Частотный ранг не равен уровню CEFR. Слова без проверенного перевода и примера пока не добавляются в тренировки.' + reviewNotice,
      native: 'Частотный ранг не равен уровню CEFR. Без проверенного перевода и примера слово пока недоступно.' + reviewNotice,
      ios: 'Частотный ранг не равен уровню CEFR. Непроверенные слова пока недоступны.' + reviewNotice,
    });
    expect(authored.vocabulary.unavailableLabel).toBe('Перевод и пример ещё не проверены');
  });

  it('links the Web case composition header to the existing plural formula', () => {
    const changed = structuredClone(course);
    changed.reference.webCaseCompositionHeader = 'Не связанная учебная схема';
    expect(() => validateCoursePack(changed, frequency)).toThrow(/reference\/webCaseCompositionHeader/);
  });
});

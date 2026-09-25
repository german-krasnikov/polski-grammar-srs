import { useRef, useState } from 'react';
import { courseVocabularyInstructions, courseVocabularyUnavailableLabel } from '../data/course';
import { dueWordIds, exportVocabulary, loadVocabulary, mergeVocabulary, reviewWord, saveVocabulary, setWordSelected, validateVocabulary, type StudyDirection, type VocabularyDocument } from '../vocabulary/storage';
import { frequencyTop, validateVocabularyItem, vocabularyById, vocabularyItems, type VocabularyItem, type WordLevel } from '../vocabulary/catalog';

type ListFilter = 'A1' | 'A2' | 'B1' | '100' | '500' | '1000' | 'mine';
const blank = { lemma: '', translation: '', form: '', example: '', level: '—' as WordLevel };

function download(name: string, text: string) {
  const url = URL.createObjectURL(new Blob([text], { type: 'application/json' }));
  const link = document.createElement('a');
  link.href = url; link.download = name; link.click();
  window.setTimeout(() => URL.revokeObjectURL(url), 1000);
}

export default function VocabularyView() {
  const [loaded] = useState(loadVocabulary);
  const [recoveryActive, setRecoveryActive] = useState(!!loaded.recoveryRaw);
  const [document, setDocument] = useState<VocabularyDocument>(loaded.document);
  const [error, setError] = useState(loaded.error ?? '');
  const [direction, setDirection] = useState<StudyDirection>('ru-pl');
  const [filter, setFilter] = useState<ListFilter>('A1');
  const [showCatalog, setShowCatalog] = useState(true);
  const [revealed, setRevealed] = useState(false);
  const [draft, setDraft] = useState('');
  const [typed, setTyped] = useState(false);
  const [editing, setEditing] = useState<string | null>(null);
  const [form, setForm] = useState(blank);
  const fileInput = useRef<HTMLInputElement>(null);
  const touchStart = useRef<{ x: number; y: number } | null>(null);
  const items = [...vocabularyItems, ...document.custom];
  const readyByLemma = new Map(vocabularyItems.map(item => [item.lemma, item]));
  const due = dueWordIds(document, direction);
  const current = due.length ? vocabularyById(due[0], document.custom) : undefined;
  const isFrequency = filter === '100' || filter === '500' || filter === '1000';
  const rows = isFrequency ? frequencyTop(Number(filter) as 100 | 500 | 1000).map(rank => ({ rank: rank.rank, lemma: rank.lemma, item: readyByLemma.get(rank.lemma) }))
    : items.filter(item => filter === 'mine' ? item.custom : item.level === filter)
      .map(item => ({ rank: item.frequencyRank, lemma: item.lemma, item }));
  const coverage = isFrequency ? `Готово ${rows.filter(row => row.item && !row.item.custom).length}/${rows.length} · недоступно ${rows.filter(row => !row.item || row.item.custom).length}` : null;

  function commit(next: VocabularyDocument): boolean {
    if (recoveryActive) { setError('Сначала сохрани повреждённый JSON и импортируй исправленную копию.'); return false; }
    try { saveVocabulary(next); setDocument(next); setError(''); return true; }
    catch (failure) { setError(failure instanceof Error ? failure.message : 'Не удалось сохранить словарь'); return false; }
  }
  function select(id: string, checked: boolean) { commit(setWordSelected(document, id, checked)); }
  function rate(rating: 'again' | 'good') {
    if (!revealed || !current) return;
    if (commit(reviewWord(document, current.id, direction, rating))) {
      setRevealed(false); setDraft('');
    }
  }
  function saveOwn() {
    try {
      const id = editing ?? `user.${crypto.randomUUID()}`;
      const item = validateVocabularyItem({ ...form, id, frequencyRank: null, custom: true });
      if (items.some(other => other.id !== id && other.lemma.toLocaleLowerCase('pl') === item.lemma.toLocaleLowerCase('pl'))) {
        throw new Error('Это польское слово уже есть в словаре.');
      }
      const custom = editing ? document.custom.map(old => old.id === id ? item : old) : [...document.custom, item];
      if (commit({ ...document, custom })) { setEditing(null); setForm(blank); }
    } catch (failure) { setError(failure instanceof Error ? failure.message : 'Не удалось сохранить слово'); }
  }
  function removeOwn(id: string) {
    if (!window.confirm('Удалить своё слово из каталога? История повторений останется в экспортируемых данных.')) return;
    if (commit({ ...document, custom: document.custom.filter(item => item.id !== id), selectedIds: document.selectedIds.filter(selected => selected !== id) })) {
      setEditing(null); setForm(blank);
    }
  }
  async function importFile(file?: File) {
    if (!file) return;
    try {
      const imported = JSON.parse(await file.text());
      const merged = recoveryActive ? validateVocabulary(imported) : mergeVocabulary(document, imported);
      saveVocabulary(merged); setDocument(merged); setRecoveryActive(false); setError('');
    }
    catch (failure) { setError(failure instanceof Error ? failure.message : 'Не удалось импортировать словарь'); }
    if (fileInput.current) fileInput.current.value = '';
  }

  return <section className="vocabulary-view" aria-label="Тренировка слов">
    <div className="vocabulary-heading"><div><h2>Слова и выражения</h2><p>Отмечай слова в каталоге. Узнавание и воспроизведение повторяются по отдельным расписаниям.</p></div><label>Направление<select aria-label="Направление карточки" value={direction} onChange={event => { setDirection(event.target.value as StudyDirection); setRevealed(false); setDraft(''); }}><option value="ru-pl">Русский → польский</option><option value="pl-ru">Польский → русский</option></select></label></div>
    {error && <p role="alert" className="notice">{error}</p>}
    {recoveryActive && loaded.recoveryRaw && <button onClick={() => download('vocabulary-recovery.json', loaded.recoveryRaw!)}>Сохранить исходный JSON</button>}
    <div className="vocabulary-layout">
      <section className="card vocabulary-card" aria-label="Карточка слова" onTouchStart={event => {
        if (revealed) touchStart.current = { x: event.changedTouches[0].clientX, y: event.changedTouches[0].clientY };
      }} onTouchEnd={event => {
        if (!revealed || !touchStart.current) return;
        const dx = event.changedTouches[0].clientX - touchStart.current.x;
        const dy = event.changedTouches[0].clientY - touchStart.current.y;
        touchStart.current = null;
        if (Math.abs(dx) >= 75 && Math.abs(dx) > Math.abs(dy) * 1.25) rate(dx < 0 ? 'again' : 'good');
      }}>
        {current ? <><span className="eyebrow">{direction === 'ru-pl' ? 'Вспомни по-польски' : 'Вспомни по-русски'}</span><p className="vocabulary-prompt" lang={direction === 'pl-ru' ? 'pl' : 'ru'}>{direction === 'ru-pl' ? current.translation : current.lemma}</p>
          {!revealed ? <><div className="answer-mode"><button aria-pressed={!typed} onClick={() => setTyped(false)}>Ответ вслух / про себя</button><button aria-pressed={typed} onClick={() => setTyped(true)}>Напечатать ответ</button></div>{typed && <textarea aria-label="Ответ на карточку слова" value={draft} onChange={event => setDraft(event.target.value)} onKeyDown={event => { if (event.key === 'Enter' && !event.shiftKey && !event.nativeEvent.isComposing) { event.preventDefault(); setRevealed(true); } }}/>}<button className="primary reveal-button" onClick={() => setRevealed(true)}>Показать ответ</button></>
            : <><div className="vocabulary-answer"><span className="eyebrow">Эталон · {direction === 'ru-pl' ? 'польский' : 'русский'}</span><p lang={direction === 'ru-pl' ? 'pl' : 'ru'}>{direction === 'ru-pl' ? current.lemma : current.translation}</p><dl><div><dt>Перевод</dt><dd>{current.translation}</dd></div><div><dt>Форма</dt><dd lang="pl">{current.form}</dd></div><div><dt>В предложении</dt><dd lang="pl">{current.example}</dd></div></dl>{typed && <p>Твой ответ: <strong>{draft || 'не введён'}</strong>. Сравни сам и выбери оценку.</p>}</div><div className="ratings"><button onClick={() => rate('again')}><b>Повторить</b><small>Ошибка или не уверен</small></button><button onClick={() => rate('good')}><b>Вспомнил</b><small>Воспроизвёл сам</small></button></div></>}
        </> : <><h3>{document.selectedIds.length ? 'На сейчас всё повторено' : 'Выбери слова для тренировки'}</h3><p>Отметь готовые карточки в каталоге. История каждого направления сохраняется отдельно.</p></>}
      </section>
      <section className="card vocabulary-catalog"><div className="catalog-toolbar"><h3>Мой словарь · {document.selectedIds.length}</h3><button onClick={() => setShowCatalog(!showCatalog)} aria-expanded={showCatalog}>{showCatalog ? 'Скрыть каталог' : 'Открыть каталог'}</button></div>
        {showCatalog && <><label>Подборка<select aria-label="Подборка слов" value={filter} onChange={event => setFilter(event.target.value as ListFilter)}><option value="A1">A1 · готовые карточки</option><option value="A2">A2 · готовые карточки</option><option value="B1">B1 · готовые карточки</option><option value="100">Топ-100 по частоте</option><option value="500">Топ-500 по частоте</option><option value="1000">Топ-1000 по частоте</option><option value="mine">Мои слова</option></select></label><p className="muted small">{courseVocabularyInstructions.react}</p>{coverage && <p className="muted small" role="status">{coverage}</p>}<div className="catalog-list">{rows.map(row => <label key={row.rank ?? row.item?.id ?? row.lemma} className="catalog-row"><input type="checkbox" checked={!!row.item && document.selectedIds.includes(row.item.id)} disabled={!row.item || recoveryActive} onChange={event => row.item && select(row.item.id, event.target.checked)}/><span><b lang="pl">{row.lemma}</b><small>{row.item ? row.item.translation : courseVocabularyUnavailableLabel}</small></span>{row.rank && <small>№ {row.rank}</small>}{row.item?.custom && <button type="button" onClick={event => { event.preventDefault(); setEditing(row.item!.id); setForm({ lemma: row.item!.lemma, translation: row.item!.translation, form: row.item!.form, example: row.item!.example, level: row.item!.level }); }}>Изменить</button>}</label>)}</div>
          <div className="vocabulary-editor"><h4>{editing ? 'Изменить своё слово' : 'Добавить своё слово'}</h4><div className="editor-fields"><label>Польское слово<input value={form.lemma} onChange={event => setForm({ ...form, lemma: event.target.value })}/></label><label>Перевод<input value={form.translation} onChange={event => setForm({ ...form, translation: event.target.value })}/></label><label>Форма<input value={form.form} onChange={event => setForm({ ...form, form: event.target.value })}/></label><label>Пример в предложении<input value={form.example} onChange={event => setForm({ ...form, example: event.target.value })}/></label><label>Уровень<select value={form.level} onChange={event => setForm({ ...form, level: event.target.value as WordLevel })}>{['—', 'A1', 'A2', 'B1', 'B2', 'C1', 'C2'].map(level => <option key={level} value={level}>{level}</option>)}</select></label></div><div className="actions"><button onClick={saveOwn}>{editing ? 'Сохранить изменения' : 'Добавить слово'}</button>{editing && <><button onClick={() => { setEditing(null); setForm(blank); }}>Отмена</button><button onClick={() => removeOwn(editing)}>Удалить слово</button></>}</div></div>
          <div className="actions"><button onClick={() => download('polski-vocabulary-pl-ru.json', exportVocabulary(document))}>Экспорт словаря JSON</button><button onClick={() => fileInput.current?.click()}>Импортировать JSON</button><input ref={fileInput} type="file" accept="application/json,.json" hidden onChange={event => void importFile(event.target.files?.[0])}/></div></>}
      </section>
    </div>
    <p className="muted small">Частотные ранги и counts: <a href="https://github.com/KubaCiolo/leksjo-dane/blob/01782aa92cc842d0d3199079eba47ecbf05879e1/dane/nkjp-frekwencja.csv" target="_blank" rel="noreferrer">Leksjo / NKJP, CC BY 4.0</a>. Изменения: взяты первые 1000 лемм, рангов и counts.</p>
  </section>;
}

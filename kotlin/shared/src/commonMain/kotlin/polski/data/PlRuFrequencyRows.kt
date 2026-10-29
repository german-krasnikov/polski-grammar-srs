package polski.data

import polski.data.rows.plru.frequency.FREQUENCY_ROW_ROWS

/** R1.8a: the pl-ru word frequencies in rank order, from the generated rows. Nothing but the pack field reads them yet. */
internal val generatedPlRuFrequency: List<FrequencyItem> = FREQUENCY_ROW_ROWS.map { FrequencyItem(it.rank, it.lemma, it.count) }

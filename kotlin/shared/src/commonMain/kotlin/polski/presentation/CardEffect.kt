package polski.presentation

import polski.srs.Rating

/** Non-domain visual cue a host may play (e.g. via Rive) after a rating dispatch. Never stored in [AppUiState]. */
enum class CardEffect { None, Remembered, Again }

/** Pure mapping from a rating to its visual effect; binary ratings only (see [polski.srs.Rating]). */
fun cardEffectFor(rating: Rating): CardEffect = if (rating == Rating.Good) CardEffect.Remembered else CardEffect.Again

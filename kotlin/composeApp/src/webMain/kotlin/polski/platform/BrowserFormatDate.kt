package polski.platform

/** Display-only local browser date; scheduling still uses absolute instants. */
internal expect fun browserFormatDate(epochMilliseconds: Double): String

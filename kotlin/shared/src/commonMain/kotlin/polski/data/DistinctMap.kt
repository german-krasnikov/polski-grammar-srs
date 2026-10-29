package polski.data

/** A map from pairs; a key written twice is refused, not overwritten (a map built with `toMap()` keeps the last one and says nothing). */
internal fun <K, V> List<Pair<K, V>>.toDistinctMap(what: String): Map<K, V> = toMap().also {
    require(it.size == size) { "$what: ${groupingBy { pair -> pair.first }.eachCount().filterValues { n -> n > 1 }.keys} written twice" }
}

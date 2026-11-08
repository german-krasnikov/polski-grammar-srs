package polski.data

/** Validates authored UI labels when a typed pack is first read. */
internal fun uiCopyRowsToMap(rows: Iterable<Pair<String, String>>): Map<String, String> {
    val result = linkedMapOf<String, String>()
    for ((key, text) in rows) {
        require(key.isNotBlank()) { "UI copy key must not be blank" }
        require(text.isNotBlank()) { "UI copy text must not be blank: $key" }
        require(key !in result) { "Duplicate UI copy key: $key" }
        result[key] = text
    }
    return result.toMap()
}

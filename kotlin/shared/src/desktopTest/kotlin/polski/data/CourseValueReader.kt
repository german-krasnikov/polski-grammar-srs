package polski.data

import java.io.File

/**
 * NJ03 B3 pilot: reads CourseValueSkill from courses-biome/pl-ru/skills.core-data.
 * JVM-only (desktopTest). Production path wires through GeneratedCourseJson after B5.
 */

private object BiomeParser {
    fun record(text: String): Map<String, Any?> {
        val t = text.trim()
        require(t.startsWith("{") && t.endsWith("}")) { "expected record: $text" }
        return recBody(t, 1).first
    }

    private fun value(s: String, i: Int): Pair<Any?, Int> {
        var p = ws(s, i)
        return when {
            s[p] == '"'                         -> str(s, p)
            s[p] == '{'                         -> { val (r, e) = recBody(s, p + 1); r to e }
            s[p] == '['                         -> { val (l, e) = lstBody(s, p + 1); l to e }
            s.startsWith("null", p)             -> null to p + 4
            s.startsWith("true", p)             -> true to p + 4
            s.startsWith("false", p)            -> false to p + 5
            s[p] == '-' || s[p].isDigit()       -> num(s, p)
            else                                -> bare(s, p)
        }
    }

    private fun recBody(s: String, start: Int): Pair<Map<String, Any?>, Int> {
        val m = mutableMapOf<String, Any?>(); var i = ws(s, start)
        while (i < s.length && s[i] != '}') {
            var e = i; while (e < s.length && (s[e].isLetterOrDigit() || s[e] in "._-")) e++
            val k = s.substring(i, e); i = ws(s, e)
            require(s[i] == ':'); i = ws(s, i + 1)
            val (v, n) = value(s, i); m[k] = v; i = ws(s, n)
        }
        return m to i + 1
    }

    private fun lstBody(s: String, start: Int): Pair<List<Any?>, Int> {
        val l = mutableListOf<Any?>(); var i = ws(s, start)
        while (i < s.length && s[i] != ']') { val (v, n) = value(s, i); l.add(v); i = ws(s, n) }
        return l to i + 1
    }

    private fun str(s: String, start: Int): Pair<String, Int> {
        var i = start + 1; val sb = StringBuilder()
        while (i < s.length && s[i] != '"') {
            if (s[i] == '\\') { i++; sb.append(when (s[i]) { 'n' -> '\n'; 't' -> '\t'; else -> s[i] }) }
            else sb.append(s[i]); i++
        }
        return sb.toString() to i + 1
    }

    private fun num(s: String, start: Int): Pair<Any, Int> {
        var i = start; if (s[i] == '-') i++
        while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
        val raw = s.substring(start, i)
        return (if ('.' in raw) raw.toDouble() else raw.toLong()) to i
    }

    private fun bare(s: String, start: Int): Pair<String, Int> {
        var i = start; while (i < s.length && (s[i].isLetterOrDigit() || s[i] in "._:-")) i++
        return s.substring(start, i) to i
    }

    private fun ws(s: String, i: Int): Int { var p = i; while (p < s.length && s[p] in " \t\r\n") p++; return p }
}

internal data class CourseValueSkill(
    val id: String,
    val title: String,
    val group: String,
    val level: String,
    val prerequisites: List<String>,
)

internal fun courseValueSkills(): List<CourseValueSkill> {
    val root = generateSequence(File(".").absoluteFile) { it.parentFile }
        .first { it.resolve("courses-biome").isDirectory }
    val lines = root.resolve("courses-biome/pl-ru/skills.core-data").readLines()
    return lines.filter { it.startsWith("SKILL ") }.map { line ->
        @Suppress("UNCHECKED_CAST")
        val rec = BiomeParser.record(line.removePrefix("SKILL ")) as Map<String, Any?>
        CourseValueSkill(
            id            = rec["id"] as String,
            title         = rec["title"] as String,
            group         = rec["group"] as String,
            level         = rec["level"] as String,
            prerequisites = (rec["prerequisites"] as List<*>).map { it as String },
        )
    }
}

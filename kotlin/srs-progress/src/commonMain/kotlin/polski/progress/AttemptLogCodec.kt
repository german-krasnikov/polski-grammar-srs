package polski.progress

import polski.textwire.DecodeResult as WireDecodeResult
import polski.textwire.PolskiTextCodec
import polski.textwire.WireNode

sealed interface AttemptLogDecodeResult {
    data class Valid(val document: AttemptLogDocument) : AttemptLogDecodeResult
    data class Invalid(val reason: String) : AttemptLogDecodeResult
    data class Unsupported(val version: Int) : AttemptLogDecodeResult
}

/**
 * P1 blueprint §P1.1: one more POLSKI-TEXT wire kind (`"attempts"`, schema version 1), parallel to
 * `ProgressCodec`'s `"progress"` kind — reuses the same `PolskiTextCodec`/`WireNode` infrastructure
 * rather than duplicating it. `AttemptLog` has no legacy (pre-text-wire) predecessor — unlike
 * `Progress`, there is no React-authored JSON to stay compatible with — so unlike `ProgressCodec`
 * this codec has only one wire path (`encodeText`/`decodeText`), not a second strict-JSON
 * legacy reader.
 */
object AttemptLogCodec {

    private const val KIND = "attempts"
    private const val SCHEMA_VERSION = 1

    fun encodeText(document: AttemptLogDocument): ByteArray {
        val log = document.log
        val events = WireNode.WireList(log.events.map(::encodeEvent))
        val root = WireNode.WireMap(listOf(
            "version" to WireNode.WireI64(log.version.toLong()),
            "events" to events,
        ))
        return PolskiTextCodec.encode(root, KIND, SCHEMA_VERSION).bytes
    }

    fun decodeText(bytes: ByteArray): AttemptLogDecodeResult {
        val wire = PolskiTextCodec.decode(bytes)
        if (wire is WireDecodeResult.Err) return AttemptLogDecodeResult.Invalid(wire.message)
        val ok = wire as WireDecodeResult.Ok
        if (ok.kind != KIND) return AttemptLogDecodeResult.Invalid("wrong kind: ${ok.kind}")
        if (ok.schemaVersion != SCHEMA_VERSION) return AttemptLogDecodeResult.Unsupported(ok.schemaVersion)
        val m = ok.root.children.toMap()
        val version = (m["version"] as? WireNode.WireI64)?.value?.toInt()
            ?: return AttemptLogDecodeResult.Invalid("missing version")
        val eventsNode = m["events"] as? WireNode.WireList
            ?: return AttemptLogDecodeResult.Invalid("missing events")
        val events = ArrayList<AttemptEvent>(eventsNode.items.size)
        for ((index, item) in eventsNode.items.withIndex()) {
            val map = item as? WireNode.WireMap ?: return AttemptLogDecodeResult.Invalid("invalid event at $index")
            events += decodeEvent(map, index) ?: return AttemptLogDecodeResult.Invalid("invalid event at $index")
        }
        return AttemptLogDecodeResult.Valid(AttemptLogDocument(AttemptLog(version, events.toList())))
    }

    private fun encodeEvent(e: AttemptEvent): WireNode.WireMap = WireNode.WireMap(listOf(
        "schemaVersion" to WireNode.WireI64(e.schemaVersion.toLong()),
        "attemptId" to WireNode.WireString(e.attemptId),
        "sessionId" to WireNode.WireString(e.sessionId),
        "skillId" to WireNode.WireString(e.skillId),
        "seedKey" to WireNode.WireString(e.seedKey),
        "kind" to WireNode.WireString(e.kind.name),
        "at" to WireNode.WireString(e.at),
        "localDay" to WireNode.WireString(e.localDay),
        "activeMillis" to WireNode.WireI64(e.activeMillis),
        "answerMode" to WireNode.WireString(e.answerMode),
        "helpShown" to WireNode.WireBool(e.helpShown),
        "disclosed" to WireNode.WireBool(e.disclosed),
        "correct" to (e.correct?.let { WireNode.WireBool(it) } ?: WireNode.WireNull),
        "correctnessBasis" to WireNode.WireString(e.correctnessBasis.name),
        "selfRating" to WireNode.WireString(e.selfRating),
        "correctionOf" to (e.correctionOf?.let { WireNode.WireString(it) } ?: WireNode.WireNull),
        "sessionKind" to WireNode.WireString(e.sessionKind),
        "sessionSource" to WireNode.WireString(e.sessionSource),
        "experimentVariant" to (e.experimentVariant?.let { WireNode.WireString(it) } ?: WireNode.WireNull),
        "rubricVersion" to WireNode.WireI64(e.rubricVersion.toLong()),
        "contentVersion" to (e.contentVersion?.let { WireNode.WireString(it) } ?: WireNode.WireNull),
        "scheduledNextDue" to (e.scheduledNextDue?.let { WireNode.WireString(it) } ?: WireNode.WireNull),
    ))

    private fun decodeEvent(m: WireNode.WireMap, index: Int): AttemptEvent? {
        val c = m.children.toMap()
        val kind = (c["kind"] as? WireNode.WireString)?.value?.let { raw -> AttemptKind.entries.firstOrNull { it.name == raw } } ?: return null
        val basis = (c["correctnessBasis"] as? WireNode.WireString)?.value?.let { raw -> CorrectnessBasis.entries.firstOrNull { it.name == raw } } ?: return null
        return AttemptEvent(
            schemaVersion = (c["schemaVersion"] as? WireNode.WireI64)?.value?.toInt() ?: return null,
            attemptId = (c["attemptId"] as? WireNode.WireString)?.value?.takeIf { it.isNotBlank() } ?: return null,
            sessionId = (c["sessionId"] as? WireNode.WireString)?.value?.takeIf { it.isNotBlank() } ?: return null,
            skillId = (c["skillId"] as? WireNode.WireString)?.value?.takeIf { it.isNotBlank() } ?: return null,
            seedKey = (c["seedKey"] as? WireNode.WireString)?.value ?: return null,
            kind = kind,
            at = (c["at"] as? WireNode.WireString)?.value ?: return null,
            localDay = (c["localDay"] as? WireNode.WireString)?.value?.takeIf(ProgressCodec::validDay) ?: return null,
            activeMillis = (c["activeMillis"] as? WireNode.WireI64)?.value ?: return null,
            answerMode = (c["answerMode"] as? WireNode.WireString)?.value ?: return null,
            helpShown = (c["helpShown"] as? WireNode.WireBool)?.value ?: return null,
            disclosed = (c["disclosed"] as? WireNode.WireBool)?.value ?: return null,
            correct = when (val node = c["correct"]) {
                is WireNode.WireBool -> node.value
                WireNode.WireNull -> null
                else -> return null
            },
            correctnessBasis = basis,
            selfRating = (c["selfRating"] as? WireNode.WireString)?.value ?: return null,
            correctionOf = when (val node = c["correctionOf"]) {
                is WireNode.WireString -> node.value
                WireNode.WireNull, null -> null
                else -> return null
            },
            sessionKind = (c["sessionKind"] as? WireNode.WireString)?.value ?: return null,
            sessionSource = (c["sessionSource"] as? WireNode.WireString)?.value ?: return null,
            experimentVariant = when (val node = c["experimentVariant"]) {
                is WireNode.WireString -> node.value
                WireNode.WireNull, null -> null
                else -> return null
            },
            rubricVersion = (c["rubricVersion"] as? WireNode.WireI64)?.value?.toInt() ?: return null,
            contentVersion = when (val node = c["contentVersion"]) {
                is WireNode.WireString -> node.value
                WireNode.WireNull, null -> null
                else -> return null
            },
            scheduledNextDue = when (val node = c["scheduledNextDue"]) {
                is WireNode.WireString -> node.value
                WireNode.WireNull, null -> null
                else -> return null
            },
        )
    }
}

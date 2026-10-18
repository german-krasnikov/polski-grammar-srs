// Generated from "polski-text-codec.editmode"; do not edit.
package polski.textwire

import org.junit.jupiter.api.Test
import biome.plan.PlanRun

class PolskiTextCodecExamples {
    @Test
    fun EmptyMap() {
        Host.open("PolskiTextCodec/emptyMap").use { run ->
            run.fixture("codec", mapOf<String, Any?>("preset" to "empty", "kind" to "progress", "schema" to 2.0))
            run.action("roundtrip", mapOf<String, Any?>())
            check(PlanRun.cmp("eq", run.query("ok"), true)) { "ASSERT at line 9" }
        }
    }

    @Test
    fun StringValue() {
        Host.open("PolskiTextCodec/stringValue").use { run ->
            run.fixture("codec", mapOf<String, Any?>("preset" to "hello", "kind" to "progress", "schema" to 2.0))
            run.action("roundtrip", mapOf<String, Any?>())
            check(PlanRun.cmp("eq", run.query("ok"), true)) { "ASSERT at line 9" }
        }
    }

    @Test
    fun NullNode() {
        Host.open("PolskiTextCodec/nullNode").use { run ->
            run.fixture("codec", mapOf<String, Any?>("preset" to "withNull", "kind" to "progress", "schema" to 2.0))
            run.action("roundtrip", mapOf<String, Any?>())
            check(PlanRun.cmp("eq", run.query("ok"), true)) { "ASSERT at line 9" }
        }
    }

    @Test
    fun WrongHeader() {
        Host.open("PolskiTextCodec/wrongHeader").use { run ->
            run.fixture("codec", mapOf<String, Any?>("preset" to "badHeader", "kind" to "progress", "schema" to 2.0))
            run.action("roundtrip", mapOf<String, Any?>())
            check(PlanRun.cmp("eq", run.query("ok"), false)) { "ASSERT at line 9" }
        }
    }

    @Test
    fun BadDigest() {
        Host.open("PolskiTextCodec/badDigest").use { run ->
            run.fixture("codec", mapOf<String, Any?>("preset" to "badDigest", "kind" to "progress", "schema" to 2.0))
            run.action("roundtrip", mapOf<String, Any?>())
            check(PlanRun.cmp("eq", run.query("ok"), false)) { "ASSERT at line 9" }
        }
    }
}

package polski.pack

import kotlin.test.Test
import kotlin.test.assertEquals

class CoursePackSourceTest {
    @Test fun embeddedSourceLoadsExactlyTheJsonItWasGivenById() {
        val source: CoursePackSource = EmbeddedCoursePackSource("pl-ru", """{"schemaVersion":1}""")
        assertEquals("pl-ru", source.id)
        assertEquals("""{"schemaVersion":1}""", source.load())
    }

    @Test fun manifestPackIdsMirrorTheDiscoveredSources() {
        val sources = listOf(EmbeddedCoursePackSource("pl-ru", "{}"))
        val manifest = PackManifest(sources.map { it.id })
        assertEquals(listOf("pl-ru"), manifest.packIds)
    }
}

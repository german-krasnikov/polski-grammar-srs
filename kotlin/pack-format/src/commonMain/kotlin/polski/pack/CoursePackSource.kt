package polski.pack

/**
 * One course pack's raw JSON, addressed by [id] (the pack directory name under `courses/`, e.g.
 * `pl-ru`), without interpreting its schema — deciding v1 vs. a future v2 shape is the caller's
 * job (`CoursePackLoader`, not built yet — UniversalCorePlan.md §6 keeps v1 readable as-is).
 * [EmbeddedCoursePackSource] is the only implementation today: `:shared`'s
 * `generateCoursePackSource` task (`kotlin/shared/build.gradle.kts`) scans the `courses` directory
 * for subdirectories with a `course.json` and inlines each match's bytes as a Kotlin string
 * literal at build time.
 * UniversalCorePlan.md §4.2 reserves a later, non-embedded `DownloadedPackSource` beside it —
 * nothing here changes for that.
 */
interface CoursePackSource {
    val id: String
    fun load(): String
}

/** [json] is embedded verbatim by the build task; decoding it is the caller's job. */
class EmbeddedCoursePackSource(override val id: String, private val json: String) : CoursePackSource {
    override fun load(): String = json
}

/**
 * Which pack ids the build discovered under `courses/` — mirrors the generated
 * [EmbeddedCoursePackSource.id]s 1:1 (UniversalCorePlan.md §4.2 "manifest списка id").
 */
data class PackManifest(val packIds: List<String>)

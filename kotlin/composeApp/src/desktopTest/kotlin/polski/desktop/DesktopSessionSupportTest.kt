package polski.desktop

import java.nio.file.Files
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.data.activeCoursePackId
import polski.data.selectCoursePack
import polski.presentation.TimeCapture
import polski.presentation.TimeSource
import polski.presentation.TrainingStore
import polski.srs.FsrsScheduler
import polski.training.PlExerciseEngine

/**
 * EnRuAcceptance-2026-09-28.md §7 item 2 parity (ADR-37), JVM Compose Desktop preview lane:
 * unlike `MacSession`/`IosSession`, `Main.kt`'s `DesktopSession` built `TrainingStore` directly, so
 * a Settings switch to a pack that [polski.data.usableCourseSelections] accepts (parses) but whose
 * engine still can't build a session (en-ru's `forms.generated.json` is verb-only today —
 * `TrainingStore`'s own `exerciseEngine.generateChain()` init property throws) crashed the JVM
 * preview instead of rolling back the way the native macOS/iOS hosts already do.
 * [buildTrainingStoreOrRollback] is the same runCatching-rollback recipe, extracted so it is
 * directly testable without a real `Window`.
 */
class DesktopSessionSupportTest {
    @Test
    fun rollsBackToTheLastGoodPackWhenTheActivePackCannotBuildASession() {
        val goodPackId = activeCoursePackId // "pl-ru" — always registered and buildable
        val directory = Files.createTempDirectory("polski-desktop-session-support-")
        try {
            selectCoursePack("en-ru")
            try {
                var attempts = 0
                val scheduler = FsrsScheduler()
                val repository = DesktopProgressRepository(directory, scheduler)
                val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
                val store = buildTrainingStoreOrRollback(goodPackId) {
                    attempts++
                    TrainingStore(
                        repository, scheduler,
                        PlExerciseEngine(RandomSource { Random.nextDouble() }, ExerciseIdFactory { "t-$attempts" }),
                        TimeSource { TimeCapture(kotlin.time.Clock.System.now(), "2026-09-28") },
                        scope,
                    )
                }
                assertEquals(2, attempts, "should try the requested pack once, then retry with the rolled-back one")
                assertEquals(goodPackId, activeCoursePackId, "the shared active pack must be rolled back, not left broken")
                assertFalse(store.state.value.exercise!!.primarySkill.startsWith("en:"),
                    "the built store must actually serve the rolled-back pack's own (pl) skills")
                store.close()
            } finally { selectCoursePack(goodPackId) }
        } finally { directory.toFile().deleteRecursively() }
    }

    @Test
    fun buildsNormallyWhenTheActivePackAlreadyWorks() {
        val goodPackId = activeCoursePackId
        val directory = Files.createTempDirectory("polski-desktop-session-support-")
        try {
            var attempts = 0
            val scheduler = FsrsScheduler()
            val repository = DesktopProgressRepository(directory, scheduler)
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
            val store = buildTrainingStoreOrRollback(goodPackId) {
                attempts++
                TrainingStore(
                    repository, scheduler,
                    PlExerciseEngine(RandomSource { Random.nextDouble() }, ExerciseIdFactory { "t-$attempts" }),
                    TimeSource { TimeCapture(kotlin.time.Clock.System.now(), "2026-09-28") },
                    scope,
                )
            }
            assertEquals(1, attempts, "a pack that already builds must never be retried")
            store.close()
        } finally { directory.toFile().deleteRecursively() }
    }
}

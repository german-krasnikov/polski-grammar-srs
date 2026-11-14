package polski.platform

import kotlinx.browser.window
import polski.mission.MissionRepository

/** M9 blueprint: web-only MVP persistence — one flat localStorage key, same `loadRaw`/`saveRaw`
 *  shape [polski.vocabulary.VocabularyRepository] already uses, directly against the browser
 *  (no cross-host `RawKeyValueStore` port needed yet; missions are web-only in this pass). */
internal class WebMissionRepository : MissionRepository {
    companion object {
        const val KEY = "polski-missions-pl-ru-v1"
    }

    override suspend fun loadRaw(): String? = window.localStorage.getItem(KEY)
    override suspend fun saveRaw(value: String) {
        window.localStorage.setItem(KEY, value)
    }
}

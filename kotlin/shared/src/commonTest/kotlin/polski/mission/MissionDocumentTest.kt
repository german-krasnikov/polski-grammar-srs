package polski.mission

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import polski.mission.data.MISSION_CHOICE_ROW_ROWS
import polski.mission.data.MISSION_NODE_ROW_ROWS
import polski.mission.data.MISSION_ROW_ROWS
import polski.mission.data.MissionNodeRowOutcome

/** M9 blueprint AC1/AC2: the real authored pl-ru missions (`courses-biome/pl-ru/missions.core-data`
 *  + `mission-nodes.core-data` + `mission-choices.core-data`) form a reachable DAG with at least
 *  one goal-reached ending each — a content-authoring guard, not a synthetic fixture. */
class MissionDocumentTest {
    @Test fun atLeastOneMissionIsAuthored() {
        assertTrue(MISSION_ROW_ROWS.isNotEmpty())
    }

    @Test fun everyChoicesNextNodeResolvesToARealNodeInTheSameMission() {
        MISSION_ROW_ROWS.forEach { mission ->
            val nodeIdsInMission = MISSION_NODE_ROW_ROWS.filter { it.missionRef == mission.missionId }.map { it.nodeId }.toSet()
            val choices = MISSION_CHOICE_ROW_ROWS.filter { it.nodeRef in nodeIdsInMission }
            choices.forEach { choice ->
                val next = choice.nextNodeRef
                if (next != null) assertTrue(next in nodeIdsInMission, "${choice.choiceId}'s nextNodeRef must be a node of ${mission.missionId}")
            }
        }
    }

    @Test fun everyMissionHasAPathFromItsStartNodeToAGoalReachedTerminal() {
        MISSION_ROW_ROWS.forEach { mission ->
            val nodes = MISSION_NODE_ROW_ROWS.filter { it.missionRef == mission.missionId }
            val startNode = nodes.first()
            val reachable = mutableSetOf<String>()
            val queue = mutableListOf(startNode.nodeId)
            while (queue.isNotEmpty()) {
                val nodeId = queue.removeAt(0)
                if (!reachable.add(nodeId)) continue
                MISSION_CHOICE_ROW_ROWS.filter { it.nodeRef == nodeId }.mapNotNull { it.nextNodeRef }.forEach { queue.add(it) }
            }
            val goalReached = nodes.any { it.nodeId in reachable && it.isTerminal && it.outcome == MissionNodeRowOutcome.GOAL_REACHED }
            assertTrue(goalReached, "Mission ${mission.missionId} has no reachable goal_reached ending")
        }
    }

    @Test fun missionCodecRoundTripsThroughEncodeAndDecode() {
        val document = MissionDocument(progress = mapOf(
            "book-shop" to MissionProgressEntry("goal_reached", "2026-11-14T12:00:00Z"),
        ))
        val encoded = MissionCodec.encode(document)
        assertEquals(document, MissionCodec.decode(encoded))
    }

    @Test fun missionCodecDefaultsToEmptyProgressWhenDecodingAMinimalDocument() {
        val decoded = MissionCodec.decode("""{"schemaVersion":1,"progress":{}}""")
        assertEquals(MissionDocument(), decoded)
    }
}

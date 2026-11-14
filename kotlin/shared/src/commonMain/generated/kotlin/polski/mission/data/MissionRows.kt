// Generated from dsl/missions.data-contract
// source-sha256=47ec629442288af86c63b731c1550cd02666ed1754d9f51b00d71d03df518702
// Edit the source, then run biome generate.

package polski.mission.data

enum class MissionNodeRowOutcome(val tag: String) {
    GOAL_REACHED("goal_reached"),
    DEAD_END("dead_end"),
    NONE("none"),
}

data class MissionRow(
    val missionId: String,
    val title: String,
    val goal: String,
    val level: String,
    val skillTag: String? = null,
)

data class MissionNodeRow(
    val nodeId: String,
    val missionRef: String,
    val situation: String,
    val prompt: String,
    val isTerminal: Boolean,
    val outcome: MissionNodeRowOutcome? = null,
)

data class MissionChoiceRow(
    val choiceId: String,
    val nodeRef: String,
    val label: String,
    val expectedForm: String,
    val acceptedVariant: String? = null,
    val meaningAdvancesGoal: Boolean,
    val nextNodeRef: String? = null,
)

val MISSION_ROW_ROWS: List<MissionRow> = listOf(
    MissionRow(
        missionId = "book-shop",
        title = "В книжном магазине",
        goal = "Узнать, есть ли нужная книга, и что-нибудь купить",
        level = "A1",
        skillTag = "case.acc.f",
    ),
    MissionRow(
        missionId = "ask-way",
        title = "Jak dojść do stacji?",
        goal = "Узнать дорогу до станции и поблагодарить",
        level = "A1",
    ),
)

val MISSION_NODE_ROW_ROWS: List<MissionNodeRow> = listOf(
    MissionNodeRow(
        nodeId = "book-shop-ask",
        missionRef = "book-shop",
        situation = "Jesteś w księgarni. Chcesz wiedzieć, czy jest książka „Pan Tadeusz”.",
        prompt = "Spytaj sprzedawcę, czy ma tę książkę.",
        isTerminal = false,
    ),
    MissionNodeRow(
        nodeId = "book-shop-alt",
        missionRef = "book-shop",
        situation = "Sprzedawca mówi: „Niestety, nie mamy tej książki. Mamy inną — Lalka”.",
        prompt = "Zapytaj, czy mogą polecić inną książkę.",
        isTerminal = false,
    ),
    MissionNodeRow(
        nodeId = "book-shop-buy",
        missionRef = "book-shop",
        situation = "Sprzedawca podaje książkę „Lalka”.",
        prompt = "Powiedz, że kupisz tę książkę.",
        isTerminal = true,
        outcome = MissionNodeRowOutcome.GOAL_REACHED,
    ),
    MissionNodeRow(
        nodeId = "book-shop-leave",
        missionRef = "book-shop",
        situation = "Wychodzisz z księgarni bez książki.",
        prompt = "Rozmowa się skończyła.",
        isTerminal = true,
        outcome = MissionNodeRowOutcome.DEAD_END,
    ),
    MissionNodeRow(
        nodeId = "ask-way-start",
        missionRef = "ask-way",
        situation = "Jesteś na ulicy i nie wiesz, jak dojść do stacji.",
        prompt = "Spytaj przechodnia o drogę.",
        isTerminal = false,
    ),
    MissionNodeRow(
        nodeId = "ask-way-thanks",
        missionRef = "ask-way",
        situation = "Przechodzień tłumaczy drogę.",
        prompt = "Podziękuj za pomoc.",
        isTerminal = true,
        outcome = MissionNodeRowOutcome.GOAL_REACHED,
    ),
    MissionNodeRow(
        nodeId = "ask-way-giveup",
        missionRef = "ask-way",
        situation = "Nie pytasz nikogo i błądzisz dalej.",
        prompt = "Rozmowa się nie zaczęła.",
        isTerminal = true,
        outcome = MissionNodeRowOutcome.DEAD_END,
    ),
)

val MISSION_CHOICE_ROW_ROWS: List<MissionChoiceRow> = listOf(
    MissionChoiceRow(
        choiceId = "book-shop-ask-yes",
        nodeRef = "book-shop-ask",
        label = "Спросить про книгу",
        expectedForm = "Czy masz książkę „Pan Tadeusz”?",
        acceptedVariant = "Czy macie książkę „Pan Tadeusz”?",
        meaningAdvancesGoal = true,
        nextNodeRef = "book-shop-alt",
    ),
    MissionChoiceRow(
        choiceId = "book-shop-ask-no",
        nodeRef = "book-shop-ask",
        label = "Промолчать и уйти",
        expectedForm = "Nie, dziękuję.",
        meaningAdvancesGoal = false,
        nextNodeRef = "book-shop-leave",
    ),
    MissionChoiceRow(
        choiceId = "book-shop-alt-yes",
        nodeRef = "book-shop-alt",
        label = "Спросить про другую книгу",
        expectedForm = "Czy możecie polecić inną książkę?",
        meaningAdvancesGoal = true,
        nextNodeRef = "book-shop-buy",
    ),
    MissionChoiceRow(
        choiceId = "book-shop-alt-no",
        nodeRef = "book-shop-alt",
        label = "Отказаться и уйти",
        expectedForm = "Nie, dziękuję, do widzenia.",
        meaningAdvancesGoal = false,
        nextNodeRef = "book-shop-leave",
    ),
    MissionChoiceRow(
        choiceId = "ask-way-ask",
        nodeRef = "ask-way-start",
        label = "Спросить дорогу",
        expectedForm = "Jak dojść do stacji?",
        meaningAdvancesGoal = true,
        nextNodeRef = "ask-way-thanks",
    ),
    MissionChoiceRow(
        choiceId = "ask-way-skip",
        nodeRef = "ask-way-start",
        label = "Не спрашивать и идти дальше",
        expectedForm = "Pójdę dalej sam.",
        meaningAdvancesGoal = false,
        nextNodeRef = "ask-way-giveup",
    ),
)

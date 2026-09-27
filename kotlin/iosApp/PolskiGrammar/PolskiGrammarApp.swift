import SwiftUI
import UniformTypeIdentifiers
import PolskiShared

typealias Record = [String: Any]

private enum StudyStyle {
    static let ink = Color(red: 0.08, green: 0.19, blue: 0.22)
    static let amber = Color(red: 0.98, green: 0.72, blue: 0.38)
    static let canvas = Color(uiColor: .systemGroupedBackground)
}

private struct StudyHero: View {
    let due: Int
    let today: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Label("POLSKI · GRAMMAR MATRIX", systemImage: "sparkle")
                .font(.caption.weight(.bold)).tracking(1.2)
                .foregroundStyle(StudyStyle.amber)
            Text("Говори целыми предложениями")
                .font(.system(.title3, design: .rounded, weight: .bold))
                .foregroundStyle(.white)
                .fixedSize(horizontal: false, vertical: true)
            HStack(spacing: 8) {
                heroStat("К повторению", due)
                heroStat("Сегодня", today)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(18)
        .background(
            LinearGradient(colors: [StudyStyle.ink, Color(red: 0.15, green: 0.35, blue: 0.37)],
                           startPoint: .topLeading, endPoint: .bottomTrailing),
            in: RoundedRectangle(cornerRadius: 26, style: .continuous)
        )
        .accessibilityElement(children: .contain)
    }

    private func heroStat(_ title: String, _ value: Int) -> some View {
        HStack(alignment: .firstTextBaseline, spacing: 5) {
            Text("\(value)").font(.headline.weight(.bold)).monospacedDigit()
            Text(title).font(.caption)
        }
        .foregroundStyle(.white)
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(10)
        .background(.white.opacity(0.12), in: RoundedRectangle(cornerRadius: 14))
    }
}

/// C1: shown above every tab whenever `TrainingStore.state.error` is set (e.g. a progress save
/// failure), regardless of `loadStatus`, with the export action always reachable.
private struct ErrorBanner: View {
    let message: String
    let onExport: () -> Void

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: "exclamationmark.triangle.fill").foregroundStyle(.red)
            VStack(alignment: .leading, spacing: 6) {
                Text(message).font(.footnote).fontWeight(.semibold)
                Button("Экспортировать JSON", action: onExport)
                    .font(.footnote)
                    .accessibilityIdentifier("errorBannerExport")
            }
            Spacer(minLength: 0)
        }
        .padding(12)
        .background(Color.red.opacity(0.12), in: RoundedRectangle(cornerRadius: 12))
        .padding(.horizontal, 12)
        .padding(.top, 6)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("trainingErrorBanner")
    }
}

extension Dictionary where Key == String, Value == Any {
    func string(_ key: String) -> String { self[key] as? String ?? "" }
    func int(_ key: String) -> Int { (self[key] as? NSNumber)?.intValue ?? 0 }
    func int64(_ key: String) -> Int64? { (self[key] as? NSNumber)?.int64Value }
    func bool(_ key: String) -> Bool { (self[key] as? Bool) ?? false }
    /// Like [bool] but missing-key means enabled, not disabled — for `animationsEnabled`, whose own
    /// decode default is `true` (see `UserPreferences.kt`), so an empty `preferences` snapshot
    /// during the first frame of `AppModel.init` never reads as animations-off.
    func bool(_ key: String, default defaultValue: Bool) -> Bool { (self[key] as? Bool) ?? defaultValue }
    func record(_ key: String) -> Record { self[key] as? Record ?? [:] }
    func rows(_ key: String) -> [Record] { self[key] as? [Record] ?? [] }
    func strings(_ key: String) -> [String] { self[key] as? [String] ?? [] }
}

/// StylesBlueprint.md §2/S1: one of the 4 presentation styles, as offered to the picker/quick
/// switch. `label`/`description` prefer the recipe's own text (`"styles"` in the preferences
/// snapshot, sourced from `StyleRegistry` — empty until pl-ru content merges) and fall back to
/// this file's own copy only when the recipe doesn't provide one yet.
private struct StyleOption: Identifiable {
    let id: String
    let label: String
    let description: String
}

private let styleOrder = ["RuleFirst", "SituationFirst", "NativeContrast", "MinimalTheory"]
private let styleFallbackLabel: [String: String] = [
    "RuleFirst": "Через правило", "SituationFirst": "Через ситуацию",
    "NativeContrast": "Через сравнение с родным", "MinimalTheory": "Минимум теории",
]
private let styleFallbackDescription: [String: String] = [
    "RuleFirst": "Формула и схема, потом упражнение.",
    "SituationFirst": "Короткая сцена, потом упражнение.",
    "NativeContrast": "Родной язык рядом с изучаемым — где сходится, где отличается.",
    "MinimalTheory": "Минимум объяснений, больше примеров.",
]

private func styleOptions(_ preferences: Record) -> [StyleOption] {
    let recipes = preferences.rows("styles")
    let ids = recipes.isEmpty ? styleOrder : recipes.map { $0.string("id") }
    return ids.map { id in
        let row = recipes.first { $0.string("id") == id } ?? [:]
        let label = row.string("label"), description = row.string("description")
        return StyleOption(id: id, label: label.isEmpty ? (styleFallbackLabel[id] ?? id) : label,
            description: description.isEmpty ? (styleFallbackDescription[id] ?? "") : description)
    }
}

struct ProgressFile: FileDocument {
    static var readableContentTypes: [UTType] { [.json] }
    var text: String
    init(text: String) { self.text = text }
    init(configuration: ReadConfiguration) throws {
        text = String(data: configuration.file.regularFileContents ?? Data(), encoding: .utf8) ?? ""
    }
    func fileWrapper(configuration: WriteConfiguration) throws -> FileWrapper {
        FileWrapper(regularFileWithContents: Data(text.utf8))
    }
}

/// A one-shot Rive rating cue (FlipCardRivePlan.md FC-01/FC-17/FC-20). `id` is a monotonic counter,
/// not the rating name alone, so `.onChange(of:)` fires again even when the same rating repeats
/// back to back (e.g. two "Good" ratings in a row) rather than being coalesced as an unchanged value.
struct CardEffectEvent: Equatable {
    let id: Int
    let name: String
}

final class AppModel: ObservableObject {
    @Published var state: Record = [:]
    @Published var vocabularyState: Record = [:]
    @Published var notice: String?
    @Published var preferences: Record = [:]
    @Published var exportFile: ProgressFile?
    @Published var exportEffectId: Int64?
    @Published var resetEffectId: Int64?
    @Published var focusEffectId: Int64?
    @Published var cardEffect: CardEffectEvent?
    /// D3: the vocabulary card's own decorative Rive rating cue — mirrors [cardEffect] exactly,
    /// but kept as a separate published property so a training rating never re-triggers the
    /// vocabulary overlay (and vice versa) purely because `CardEffectEvent` is `Equatable`.
    @Published var vocabularyCardEffect: CardEffectEvent?
    var focusExerciseId: String?
    private let session = IosSession(defaults: .standard)
    private let vocabulary = IosVocabularySession(defaults: .standard)
    private let preferencesSession = IosPreferencesSession(defaults: .standard)
    private var lastSnapshot = ""
    private var claimedEffects = Set<Int64>()
    private var vocabularyRefreshTimer: Timer?
    private var effectCounter = 0
    private var vocabularyEffectCounter = 0

    init() {
        session.onState = { [weak self] json in
            DispatchQueue.main.async { self?.receive(json) }
        }
        // FC-01/17/20: fired once per accepted rating, never stored in `state` — a decorative cue
        // for RiveEffectOverlay, not domain data. `cardEffectFor`'s Again/Remembered mapping is
        // computed in IosSession (Kotlin), the exact point where AppAction.Rate is dispatched, so
        // this bridge only ever forwards a plain effect name string.
        session.onEffect = { [weak self] name in
            DispatchQueue.main.async {
                guard let self else { return }
                self.effectCounter += 1
                self.cardEffect = CardEffectEvent(id: self.effectCounter, name: name)
            }
        }
        vocabulary.onState = { [weak self] json in
            DispatchQueue.main.async { self?.receiveVocabulary(json) }
        }
        // D3: mirrors `session.onEffect` above for the vocabulary card's own Rive rating cue.
        vocabulary.onEffect = { [weak self] name in
            DispatchQueue.main.async {
                guard let self else { return }
                self.vocabularyEffectCounter += 1
                self.vocabularyCardEffect = CardEffectEvent(id: self.vocabularyEffectCounter, name: name)
            }
        }
        preferencesSession.onState = { [weak self] json in
            DispatchQueue.main.async { self?.receivePreferences(json) }
        }
        receive(session.currentSnapshot())
        receiveVocabulary(vocabulary.currentSnapshot())
        receivePreferences(preferencesSession.currentSnapshot())
        if preferences.string("status") == "Ready" {
            send("styleId", preferences.string("styleId"))
            send("answerMode", preferences.string("answerMode"))
        }
        // Vocabulary due status only changes when time passes; grammar training refreshes on
        // scenePhase already. Mirrors the 30s tick the desktop/Android hosts use for grammar.
        vocabularyRefreshTimer = Timer.scheduledTimer(withTimeInterval: 30, repeats: true) { [weak self] _ in
            self?.sendVocabulary("refresh")
        }
    }

    deinit {
        vocabularyRefreshTimer?.invalidate()
        session.onState = nil
        session.close()
        vocabulary.onState = nil
        vocabulary.close()
        preferencesSession.onState = nil
    }

    func send(_ command: String, _ value: String = "") { session.dispatch(command: command, value: value) }
    func sendVocabulary(_ command: String, _ value: String = "") {
        vocabulary.dispatch(command: command, value: value)
    }
    func exportVocabulary() -> ProgressFile? {
        vocabulary.exportJson().map(ProgressFile.init(text:))
    }
    func setPreference(_ field: String, _ value: String) {
        if let error = preferencesSession.set(field: field, value: value) { notice = error; return }
        if field == "styleId" { send("styleId", value) }
        if field == "answerMode" { send("answerMode", value) }
    }
    func exportPreferences() -> ProgressFile? {
        preferencesSession.exportJson().map(ProgressFile.init(text:))
    }
    func importPreferencesFile(_ result: Result<URL, Error>) {
        do {
            let url = try result.get()
            let access = url.startAccessingSecurityScopedResource()
            defer { if access { url.stopAccessingSecurityScopedResource() } }
            let handle = try FileHandle(forReadingFrom: url)
            defer { try? handle.close() }
            let data = try handle.read(upToCount: 1_000_001) ?? Data()
            guard data.count <= 1_000_000, let raw = String(data: data, encoding: .utf8) else {
                notice = "Файл должен быть UTF-8 JSON не больше 1 МБ"
                return
            }
            notice = preferencesSession.importJson(raw: raw) ?? "Настройки импортированы"
            receivePreferences(preferencesSession.currentSnapshot())
            if preferences.string("status") == "Ready" {
                send("styleId", preferences.string("styleId"))
                send("answerMode", preferences.string("answerMode"))
            }
        } catch { notice = error.localizedDescription }
    }
    func importVocabularyFile(_ result: Result<URL, Error>) {
        do {
            let url = try result.get()
            let access = url.startAccessingSecurityScopedResource()
            defer { if access { url.stopAccessingSecurityScopedResource() } }
            let handle = try FileHandle(forReadingFrom: url)
            defer { try? handle.close() }
            let data = try handle.read(upToCount: 10_000_001) ?? Data()
            guard data.count <= 10_000_000, let raw = String(data: data, encoding: .utf8) else {
                notice = "Файл должен быть UTF-8 JSON не больше 10 МБ"
                return
            }
            vocabulary.importJson(raw: raw) { [weak self] success in
                DispatchQueue.main.async { self?.notice = success.boolValue ? "Словарь импортирован" :
                    (self?.vocabularyState.string("error") ?? "Импорт словаря не удался") }
            }
        } catch { notice = error.localizedDescription }
    }
    func saveVocabularyWord(_ word: Record, editingId: String?, completion: @escaping (Bool) -> Void) {
        vocabulary.saveCustom(lemma: word.string("lemma"), translation: word.string("translation"),
            form: word.string("form"), example: word.string("example"), level: word.string("level"),
            editingId: editingId, completion: { result in completion(result.boolValue) })
    }
    func acknowledge(_ id: Int64, completed: Bool = true) {
        session.acknowledgeEffect(id: id, outcome: completed ? "completed" : "skipped")
    }
    func decideReset(_ confirmed: Bool) {
        guard let id = resetEffectId else { return }
        resetEffectId = nil
        session.decideReset(id: id, confirmed: confirmed)
    }
    func finishExport(_ result: Result<URL, Error>) {
        guard let id = exportEffectId else { return }
        exportEffectId = nil
        exportFile = nil
        switch result {
        case .success: acknowledge(id); notice = "Прогресс экспортирован"
        case .failure(let error): acknowledge(id, completed: false); notice = error.localizedDescription
        }
    }
    func importFile(_ result: Result<URL, Error>) {
        do {
            let url = try result.get()
            let access = url.startAccessingSecurityScopedResource()
            defer { if access { url.stopAccessingSecurityScopedResource() } }
            let handle = try FileHandle(forReadingFrom: url)
            defer { try? handle.close() }
            let data = try handle.read(upToCount: 10_000_001) ?? Data()
            guard data.count <= 10_000_000, let raw = String(data: data, encoding: .utf8) else {
                notice = "Файл должен быть UTF-8 JSON не больше 10 МБ"
                return
            }
            session.importJson(raw: raw) { [weak self] error in
                DispatchQueue.main.async {
                    if error == nil {
                        self?.claimedEffects.removeAll()
                        self?.focusEffectId = nil
                        self?.resetEffectId = nil
                        self?.exportEffectId = nil
                        self?.lastSnapshot = ""
                        if let snapshot = self?.session.currentSnapshot() { self?.receive(snapshot) }
                    }
                    self?.notice = error ?? "Прогресс импортирован"
                }
            }
        } catch { notice = error.localizedDescription }
    }

    private func receive(_ json: String) {
        guard json != lastSnapshot, let data = json.data(using: .utf8),
              let parsed = (try? JSONSerialization.jsonObject(with: data)) as? Record else { return }
        lastSnapshot = json
        let previousPhase = state.string("phase")
        let previousExerciseId = state.record("exercise").string("id")
        state = parsed
        reconcileAnswerMode(previousPhase: previousPhase, previousExerciseId: previousExerciseId)
        for effect in parsed.rows("effects") {
            guard let id = effect.int64("id"), claimedEffects.insert(id).inserted else { continue }
            switch effect.string("kind") {
            case "export": exportFile = ProgressFile(text: effect.string("json")); exportEffectId = id
            case "reset": resetEffectId = id
            case "focus": focusExerciseId = effect.string("exerciseId"); focusEffectId = id
            default: acknowledge(id, completed: false)
            }
        }
    }

    /// M10: a preferred answerMode set while a card is Revealed is dropped by the store (it only
    /// accepts SetAnswerMode in CardPhase.Question); re-apply it once the next Question appears,
    /// the same way the desktop host's `applyPendingAnswerMode` reacts to phase/exerciseId changes.
    private func reconcileAnswerMode(previousPhase: String, previousExerciseId: String) {
        let phase = state.string("phase")
        guard phase == "Question" else { return }
        let exerciseId = state.record("exercise").string("id")
        guard phase != previousPhase || exerciseId != previousExerciseId else { return }
        let desired = preferences.string("answerMode")
        guard !desired.isEmpty, desired != state.string("answerMode") else { return }
        send("answerMode", desired)
    }
    private func receiveVocabulary(_ json: String) {
        guard let data = json.data(using: .utf8),
              let parsed = (try? JSONSerialization.jsonObject(with: data)) as? Record else { return }
        vocabularyState = parsed
    }
    private func receivePreferences(_ json: String) {
        guard let data = json.data(using: .utf8),
              let parsed = (try? JSONSerialization.jsonObject(with: data)) as? Record else { return }
        preferences = parsed
    }
}

@main
struct PolskiGrammarApp: App {
    @StateObject private var model = AppModel()
    @Environment(\.scenePhase) private var scenePhase
    @State private var importing = false
    @State private var exporting = false
    @State private var importingVocabulary = false
    @State private var exportingVocabulary = false
    @State private var vocabularyFile: ProgressFile?
    @State private var showingSettings = false
    @State private var importingPreferences = false
    @State private var exportingPreferences = false
    @State private var preferencesFile: ProgressFile?
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    // D4: direction the *next* tab switch should slide in from — updated in `onChange(of:)`
    // below, strictly before the switch's own body re-renders with the new `tab`, so the
    // transition this drives is always keyed by the switch that is actually happening.
    @State private var pagingEdge: Edge = .trailing
    private let tabOrder = ["Training", "Matrix", "Progress", "Vocabulary"]
    private let tabBarItems = [
        TabBarItem(tag: "Training", title: "Тренировка", icon: "square.stack"),
        TabBarItem(tag: "Matrix", title: "Матрица", icon: "tablecells"),
        TabBarItem(tag: "Progress", title: "Прогресс", icon: "chart.bar"),
        TabBarItem(tag: "Vocabulary", title: "Слова", icon: "character.book.closed"),
    ]

    var body: some Scene {
        WindowGroup {
            VStack(spacing: 0) {
                // C1: a save failure (state.error) must stay visible above every tab, regardless
                // of loadStatus, with the export action reachable so no reviewed progress is lost.
                if let error = model.state["error"] as? String {
                    ErrorBanner(message: error) { model.send("export") }
                }
                // D4: a custom paging container, not `TabView` — `TabView` swaps tab content
                // instantly with no transition slot, so there is no way to make old/new content
                // slide together from it. Only the *active* tab's subtree is mounted (matching
                // `TabView`'s own accessibility behavior: an inactive tab's elements must not be
                // independently discoverable), so `.transition` on the `if` below sees a real
                // insertion/removal, not just a value change, for exactly one tab at a time.
                ZStack {
                    ForEach(tabOrder, id: \.self) { tab in
                        if tab == model.state.string("tab") {
                            tabContent(tab)
                                .transition(.asymmetric(
                                    insertion: .move(edge: pagingEdge),
                                    removal: .move(edge: pagingEdge == .trailing ? .leading : .trailing)))
                        }
                    }
                }
                .animation(motionActive ? .easeOut(duration: 0.3) : nil, value: model.state.string("tab"))
                .onChange(of: model.state.string("tab")) { old, new in
                    let oldIndex = tabOrder.firstIndex(of: old) ?? 0
                    let newIndex = tabOrder.firstIndex(of: new) ?? 0
                    pagingEdge = newIndex >= oldIndex ? .trailing : .leading
                }
                Divider()
                PagingTabBar(selected: model.state.string("tab"), items: tabBarItems) { model.send("tab", $0) }
            }
            .fileImporter(isPresented: $importing, allowedContentTypes: [.json]) { model.importFile($0) }
            .fileExporter(isPresented: $exporting, document: model.exportFile,
                          contentType: .json, defaultFilename: "polski-srs-progress") { model.finishExport($0) }
            .fileImporter(isPresented: $importingVocabulary, allowedContentTypes: [.json]) { model.importVocabularyFile($0) }
            .fileExporter(isPresented: $exportingVocabulary, document: vocabularyFile,
                          contentType: .json, defaultFilename: "polski-vocabulary-pl-ru") { _ in vocabularyFile = nil }
            .fileImporter(isPresented: $importingPreferences, allowedContentTypes: [.json]) { model.importPreferencesFile($0) }
            .fileExporter(isPresented: $exportingPreferences, document: preferencesFile,
                          contentType: .json, defaultFilename: "polski-preferences-v2") { result in
                if case let .failure(error) = result { model.notice = error.localizedDescription }
                preferencesFile = nil
            }
            .sheet(isPresented: $showingSettings) {
                NavigationStack {
                    IosSettingsView(model: model, importing: $importingPreferences,
                        exporting: $exportingPreferences, exportFile: $preferencesFile)
                }
            }
            .onChange(of: model.exportEffectId) { _, id in if id != nil { exporting = true } }
            .alert("Сбросить весь прогресс?", isPresented: Binding(
                get: { model.resetEffectId != nil },
                set: { if !$0 && model.resetEffectId != nil { model.decideReset(false) } }
            )) {
                Button("Сбросить", role: .destructive) { model.decideReset(true) }
                Button("Отмена", role: .cancel) { model.decideReset(false) }
            } message: { Text("Все оценки и расписание повторений будут удалены.") }
            .alert("Сообщение", isPresented: Binding(
                get: { model.notice != nil }, set: { if !$0 { model.notice = nil } }
            )) { Button("ОК") { model.notice = nil } } message: { Text(model.notice ?? "") }
            .onChange(of: scenePhase) { _, phase in
                if phase == .active { model.send("refresh"); model.sendVocabulary("refresh") }
            }
            .tint(Color(uiColor: .systemTeal))
            .preferredColorScheme(colorScheme)
        }
    }

    /// D4/D5: system Reduce Motion, the app's own `Motion.Reduced` setting, or the "Анимации"
    /// master switch — same gate shape as `TrainingView.cardMotionReduced`, applied here to the
    /// tab-paging transition.
    private var motionActive: Bool {
        !reduceMotion && model.preferences.string("motion") != "Reduced"
            && model.preferences.bool("animationsEnabled", default: true)
    }

    @ViewBuilder
    private func tabContent(_ tab: String) -> some View {
        switch tab {
        case "Training":
            NavigationStack { TrainingView(model: model).toolbar { settingsToolbar } }
        case "Matrix":
            NavigationStack { MatrixView(model: model).toolbar { settingsToolbar } }
        case "Progress":
            NavigationStack { ProgressView(model: model, importing: $importing).toolbar { settingsToolbar } }
        default:
            NavigationStack { VocabularyView(model: model, importing: $importingVocabulary,
                exporting: $exportingVocabulary, exportFile: $vocabularyFile).toolbar { settingsToolbar } }
        }
    }

    private var settingsToolbar: some ToolbarContent {
        ToolbarItem(placement: .topBarTrailing) {
            Button { showingSettings = true } label: { Label("Настройки", systemImage: "gearshape") }
                .accessibilityIdentifier("openSettings")
        }
    }

    private var colorScheme: ColorScheme? {
        switch model.preferences.string("appearance") {
        case "Light": .light
        case "Dark": .dark
        default: nil
        }
    }
}

func formattedDate(_ milliseconds: Int64?) -> String {
    guard let milliseconds else { return "—" }
    return Date(timeIntervalSince1970: TimeInterval(milliseconds) / 1000).formatted(date: .abbreviated, time: .shortened)
}

private struct IosSettingsView: View {
    @ObservedObject var model: AppModel
    @Binding var importing: Bool
    @Binding var exporting: Bool
    @Binding var exportFile: ProgressFile?
    @Environment(\.dismiss) private var dismiss
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    #if DEBUG
    @AppStorage("polski.debug.riveDisabled") private var riveDisabledForMeasurement = false
    #endif

    var body: some View {
        Form {
            if model.preferences.string("status") == "RecoveryRequired" {
                Section("Восстановление") {
                    Text(model.preferences.string("error")).foregroundStyle(.red)
                    Text("Экспортируйте исходные настройки перед импортом проверенной копии.")
                }
            } else {
                Section("Обучение") {
                    // UC-10/S1: 4 styles replace the old Logic/Situations selector — the picker's
                    // own selected-value row shows the label, the caption below shows its
                    // description (both prefer StyleRegistry's recipe text, see [styleOptions]).
                    Picker("Стиль объяснений", selection: Binding(
                        get: { model.preferences.string("styleId") },
                        set: { model.setPreference("styleId", $0) }
                    )) {
                        ForEach(styleOptions(model.preferences)) { option in
                            Text(option.label).tag(option.id)
                        }
                    }
                    .accessibilityIdentifier("settingsStylePicker")
                    if let description = styleOptions(model.preferences)
                        .first(where: { $0.id == model.preferences.string("styleId") })?.description, !description.isEmpty {
                        Text(description).font(.footnote).foregroundStyle(.secondary)
                    }
                    // StylesBlueprint.md §2/ST-03: native-contrast never crashes or shows an empty
                    // block on a skill without an authored L1 parallel — it silently falls back to
                    // rule-first for that skill. This hint just makes the (already-safe) fallback
                    // visible instead of surprising, using the currently focused card as the sample.
                    if model.preferences.string("styleId") == "NativeContrast"
                        && model.state.record("styleBlocks").bool("nativeContrastFallback") {
                        Text("Для текущего навыка пока нет пары для сравнения с родным — карточка показывается как «через правило».")
                            .font(.footnote).foregroundStyle(.secondary)
                    }
                    Picker("Ответ", selection: Binding(
                        get: { model.preferences.string("answerMode") },
                        set: { model.setPreference("answerMode", $0) }
                    )) {
                        Text("Вслух").tag("Oral")
                        Text("Напечатать").tag("Typed")
                    }
                    // Distinct from the training screen's own "Ответ" picker (FlashCardView),
                    // which stays mounted under this sheet and shares the same label — a
                    // label-based query can't tell them apart. See testAnswerModeChosenInSettingsSurvivesAppRestart.
                    .accessibilityIdentifier("settingsAnswerModePicker")
                }
                Section("Внешний вид") {
                    Picker("Тема", selection: Binding(
                        get: { model.preferences.string("appearance") },
                        set: { model.setPreference("appearance", $0) }
                    )) {
                        Text("Системная").tag("System")
                        Text("Светлая").tag("Light")
                        Text("Тёмная").tag("Dark")
                    }
                    Picker("Движение", selection: Binding(
                        get: { model.preferences.string("motion") },
                        set: { model.setPreference("motion", $0) }
                    )) {
                        Text("Системное").tag("System")
                        Text("Сокращённое").tag("Reduced")
                    }
                    Text(reduceMotion ? "Система сокращает движение" : "Системное движение активно")
                        .font(.footnote).foregroundStyle(.secondary)
                    // D5: master switch — off means no card/tab/reveal animation and, per
                    // `cardMotionReduced`/`motionActive` above, RiveViewModel is never created.
                    Toggle("Анимации", isOn: Binding(
                        get: { model.preferences.bool("animationsEnabled", default: true) },
                        set: { model.setPreference("animationsEnabled", $0 ? "true" : "false") }
                    ))
                }
            }
            Section("Данные настроек") {
                Button("Экспортировать настройки JSON") {
                    exportFile = model.exportPreferences()
                    exporting = exportFile != nil
                }
                Button("Импортировать настройки JSON") { importing = true }
            }
            #if DEBUG
            // FlipCardRivePlan.md §5: a debug-only measurement toggle for variant B (native flip,
            // Rive effects off), layered on top of the real Motion.Reduced gate — never shown in a
            // release build, and hidden behind a long-press even in debug so it never reads as a
            // real user-facing setting.
            Section {
                Text("Тестовая версия")
                    .font(.footnote).foregroundStyle(.secondary)
                    .onLongPressGesture { riveDisabledForMeasurement.toggle() }
                if riveDisabledForMeasurement {
                    Text("Замер: Rive-эффекты отключены (вариант B)")
                        .font(.footnote).foregroundStyle(.orange)
                }
            }
            #endif
        }
        .navigationTitle("Настройки")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .confirmationAction) { Button("Готово") { dismiss() } } }
    }
}

private struct ChoiceMenu: View {
    let title: String
    let selected: String
    let options: [Record]
    let choose: (String) -> Void
    var body: some View {
        Picker(title, selection: Binding(get: { selected }, set: choose)) {
            ForEach(options, id: \.selfHash) { item in
                Text(item.string("title")).tag(item.string("id"))
            }
        }
    }
}

private extension Dictionary where Key == String, Value == Any {
    var selfHash: String { string("id") }
}

private struct TrainingView: View {
    @ObservedObject var model: AppModel
    @FocusState private var answerFocused: Bool
    @AccessibilityFocusState private var revealButtonFocused: Bool
    @State private var localDraft = ""
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    // FlipCardRivePlan.md §5, variant B (flip only, Rive off) on top of the real Motion gate —
    // a debug-only measurement toggle, never user-facing; see IosSettingsView's hidden long-press.
    #if DEBUG
    @AppStorage("polski.debug.riveDisabled") private var riveDisabledForMeasurement = false
    #endif
    private var state: Record { model.state }
    private var card: Record { state.record("exercise") }

    var body: some View {
        Form {
            Section {
                StudyHero(due: state.int("dueCount"), today: state.int("todayCount"))
                    .listRowInsets(EdgeInsets())
                    .listRowBackground(Color.clear)
            }
            if state.string("loadStatus") != "Ready" {
                Section("Прогресс") {
                    Text(state.string("loadStatus") == "Loading" ? "Загружаем…" : "Нужна копия прогресса")
                    // state.error is shown by the global ErrorBanner above every tab (C1).
                    Text("Откройте «Прогресс» для импорта или экспорта JSON.")
                }
            } else {
                Section {
                    Menu("Режим: \(modeTitle)") {
                        Button("Цепочка предложений") { model.send("chain") }
                        Button("По расписанию") { model.send("schedule") }
                        Button("Отдельный навык") { model.send("skillPicker") }
                    }
                    if state.bool("showSkillPicker") {
                        ChoiceMenu(title: "Навык", selected: "", options: state.rows("skills")) { model.send("skill", $0) }
                    }
                    if state.string("mode") == "Chain" {
                        ChoiceMenu(title: "Набор слов", selected: "\(state.int("seedIndex"))", options: state.rows("seeds")) { model.send("seed", $0) }
                        Text("\(state.int("chainDisplayCount")) / \(state.int("chainCount")) · \(state.string("chainStepSummary"))")
                            .font(.footnote).foregroundStyle(.secondary)
                    }
                    // S1 compact quick switch — same 4 styles as Settings, same location the old
                    // Logic/Situations toggle held. Dispatches SetStyle only: never creates a
                    // review, never touches draft/frozenAnswer (TrainingStore.SetStyle, ST-04).
                    Picker("Стиль объяснений", selection: Binding(
                        get: { state.string("styleId") },
                        set: { model.setPreference("styleId", $0) }
                    )) {
                        ForEach(styleOptions(model.preferences)) { option in
                            Text(option.label).tag(option.id)
                        }
                    }
                    .accessibilityIdentifier("trainingStylePicker")
                }
                switch state.string("phase") {
                case "ChainComplete":
                    Section(state.string("chainCompletionTitle")) {
                        // FC2-10 (R3, Pick C): one-shot "Tada" celebration on this completion
                        // screen, never on the card itself, gated the same way the rating cue is.
                        if !riveEffectsSuppressed {
                            RiveChainCompleteOverlay()
                                .frame(height: 120)
                                .listRowInsets(EdgeInsets())
                                .listRowBackground(Color.clear)
                        }
                        ForEach(Array(state.strings("chainAnswers").enumerated()), id: \.offset) { index, answer in
                            Text("\(index + 1). \(answer)")
                        }
                        Button("Следующий набор слов") { model.send("seed", "\((state.int("seedIndex") + 1) % max(1, state.rows("seeds").count))") }
                        Button("К повторениям") { model.send("schedule") }
                    }
                case "NoDue":
                    Section("Повторения на сейчас завершены") {
                        Text("Следующее: \(formattedDate(state.int64("nextDue")))")
                        Button("Потренировать цепочку") { model.send("chain") }
                    }
                default:
                    if !card.isEmpty {
                        Section("\(card.string("skillLevel")) · \(card.string("skillTitle"))") {
                            ZStack {
                                // D1: no more native flip, so there is no flip-in-progress ring cue
                                // to drive here any more (that overlay's own removal is D3's job).
                                FlashCardView(
                                    model: model, state: state, card: card,
                                    localDraft: $localDraft, answerFocused: $answerFocused,
                                    reduceMotion: cardMotionReduced,
                                    revealButton: { title, expands in revealButton(title: title, expands: expands) }
                                )
                                RiveEffectOverlay(effect: model.cardEffect, reduceMotion: riveEffectsSuppressed)
                            }
                            .listRowInsets(EdgeInsets())
                            .listRowBackground(Color.clear)
                        }
                    }
                }
                Section {
                    let introducing = state.string("phase") == "Question" && state.bool("introPending")
                    Button(!introducing && state.bool("showReference") ? "Скрыть таблицу" : "Таблица под рукой") {
                        model.send("reference")
                    }
                    .disabled(introducing)
                    // D4/UX4-20..22: expand/collapse animated instead of the Form's default
                    // instant row insert/remove; content is fully removed while collapsed (not
                    // just visually shrunk), so there is nothing left over for VoiceOver/Tab to
                    // reach — no separate `inert` step needed, unlike the web host's height trick.
                    // `Group` (a real expression) is what `.transition` attaches to; the bare
                    // `if` above it is a ViewBuilder statement, not something modifiers chain onto.
                    if state.bool("showReference") && !introducing {
                        Group {
                            ForEach(Array(state.rows("referenceRows").enumerated()), id: \.offset) { _, row in
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(row.string("title")).font(.headline)
                                    NativeContrastPairView(pair: row.record("pair"))
                                }
                            }
                            Button("Все таблицы и схема") { model.send("tab", "Matrix") }
                        }
                        .transition(.opacity.combined(with: .move(edge: .top)))
                    }
                }
                .animation(cardMotionReduced ? nil : .easeOut(duration: 0.25), value: state.bool("showReference"))
            }
        }
        .toolbar {
            ToolbarItemGroup(placement: .keyboard) {
                if showsPinnedReveal {
                    Spacer()
                    revealButton(title: "Проверить ответ", expands: false)
                }
            }
        }
        .scrollDismissesKeyboard(.immediately)
        .navigationTitle("Тренировка")
        .navigationBarTitleDisplayMode(.inline)
        .frame(maxWidth: 850).frame(maxWidth: .infinity)
        .onAppear { localDraft = state.string("draft") }
        .onChange(of: card.string("id")) { _, _ in localDraft = state.string("draft") }
        .animation(cardMotionReduced ? nil : .default, value: state.string("phase"))
    }

    private var modeTitle: String {
        switch state.string("mode") {
        case "Schedule": "По расписанию"
        case "Focused": "Отдельный навык"
        default: "Цепочка предложений"
        }
    }

    private var showsPinnedReveal: Bool {
        state.string("loadStatus") == "Ready" && !card.isEmpty &&
            state.string("phase") == "Question" && !state.bool("introPending") &&
            state.string("answerMode") == "Typed" && answerFocused
    }

    /// FC-09/12/14/20's shared reduced-motion gate: system Reduce Motion or the app's own
    /// `Motion.Reduced` setting, the same pair already used by `.animation(...)` below — both the
    /// card flip and the Rive overlay must snap/skip together with everything else this gates.
    private var cardMotionReduced: Bool {
        reduceMotion || model.preferences.string("motion") == "Reduced"
            || !model.preferences.bool("animationsEnabled", default: true)
    }

    /// FC-20's Rive gate, plus the debug-only variant-B override (§5): the flip itself keeps
    /// animating in variant B — only the Rive trigger is suppressed — so this is deliberately
    /// separate from [cardMotionReduced], which the flip's own animation uses unchanged.
    private var riveEffectsSuppressed: Bool {
        #if DEBUG
        return cardMotionReduced || riveDisabledForMeasurement
        #else
        return cardMotionReduced
        #endif
    }

    private func revealButton(title: String, expands: Bool = true) -> some View {
        Button(title) {
            answerFocused = false
            model.send("reveal")
        }
        .accessibilityIdentifier("revealAnswer")
        .accessibilityFocused($revealButtonFocused)
        .buttonStyle(.borderedProminent)
        .controlSize(.large)
        .frame(maxWidth: expands ? .infinity : nil)
        .onAppear {
            // M11: only claim the FocusReveal effect while its exercise is still the one on
            // screen; a stale effect from an exercise the user already left is Skipped instead.
            guard let id = model.focusEffectId else { return }
            model.focusEffectId = nil
            if model.focusExerciseId == card.string("id") {
                model.focusExerciseId = nil
                revealButtonFocused = true
                model.acknowledge(id, completed: true)
            } else {
                model.acknowledge(id, completed: false)
            }
        }
    }

}

/// `ContrastHighlightPlan.md` §3: "Хосту запрещено иметь второй, «упрощённый» путь отрисовки" —
/// this used to color `before`/`after` with raw `.systemRed`/`.systemOrange` instead of the shared
/// `EmphasisBefore`/`EmphasisAfter` tokens `FlashCardView.swift`'s [EmphasisRole] already defines,
/// which made `after` warm orange rather than the contract's required cool accent (I2 correction,
/// `EmphasisUXAudit-2026-09-27.md` E1/E9 class). [styledParts] is that one shared view; every
/// `Cases`/`Verbs`/`Pronouns`/chain/system-map row below goes through it now, not a second copy.
private struct NativeContrastPairView: View {
    let pair: Record

    var body: some View {
        VStack(alignment: .leading, spacing: 3) {
            Text("Было").font(.caption)
            styledParts(pair.rows("beforeParts"), role: .before)
            Text("→").font(.caption)
            Text("Стало").font(.caption)
            styledParts(pair.rows("afterParts"), role: .after)
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Было: \(pair.string("from")); Стало: \(pair.string("to"))")
    }
}

private struct MatrixView: View {
    @ObservedObject var model: AppModel
    @State private var comparisonCase = "NOM"
    private var matrix: Record { model.state.record("matrix") }

    var body: some View {
        Form {
            Section {
                Text(matrix.string("matrixIntroduction"))
                Picker("Раздел", selection: Binding(get: { matrix.string("section") }, set: { model.send("matrixSection", $0) })) {
                    Text("Карта системы").tag("Map")
                    Text("Падежи и окончания").tag("Cases")
                    Text("Времена и лица").tag("Verbs")
                    Text("Местоимения").tag("Pronouns")
                }
            }
            switch matrix.string("section") {
            case "Cases": cases
            case "Verbs": verbs
            case "Pronouns": pronouns
            default: map
            }
        }
        .navigationTitle("Матрица")
        .navigationBarTitleDisplayMode(.inline)
        .frame(maxWidth: 850).frame(maxWidth: .infinity)
    }

    private func comparisonRow(_ row: Record, includeNote: Bool = false) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(row.string("label")).font(.headline)
            NativeContrastPairView(pair: row)
            if includeNote { Text(row.string("change")).font(.footnote) }
        }
        .accessibilityElement(children: .contain)
    }

    @ViewBuilder private var map: some View {
        Section(matrix.string("pipelineTitle")) {
            Text(matrix.string("pipelineSummary"))
            Text(matrix.string("pipelineExample"))
            ForEach(matrix.rows("systemCards"), id: \.selfHash) { card in
                VStack(alignment: .leading, spacing: 4) {
                    Text(card.string("title")).font(.headline)
                    Text(card.string("explanation"))
                    // C2/EmphasisUXAudit E6: the żona → żonę → żony chain used to show as one flat
                    // `example` string. Each authored `steps` arrow (structured pack data) now
                    // renders through the same NativeContrastPairView every other before/after pair
                    // in this app uses — these cards sit outside the exercise reveal gate (they are
                    // reference material, not an exercise's own answer), so showing every step's
                    // "Стало" here is the emphasis contract's stated exception, not an answer leak.
                    ForEach(Array(card.rows("steps").enumerated()), id: \.offset) { step in
                        NativeContrastPairView(pair: step.element)
                    }
                }
                .accessibilityElement(children: .contain)
            }
        }
        Section("Одна мысль, пять преобразований") {
            ForEach(Array(matrix.rows("chainRows").enumerated()), id: \.offset) { entry in
                comparisonRow(entry.element, includeNote: true)
            }
            Button("Тренировать эту цепочку") { model.send("skill", "chain") }
        }
        Section("Мужской Biernik") {
            ForEach(Array(matrix.rows("maleAccRows").enumerated()), id: \.offset) { row in
                VStack(alignment: .leading, spacing: 8) {
                    Text("\(row.element.string("label")) · \(row.element.string("title"))").font(.headline)
                    ForEach(Array(row.element.rows("examples").enumerated()), id: \.offset) { example in
                        comparisonRow(example.element, includeNote: true)
                    }
                    Text(row.element.string("rule")).font(.footnote)
                }
            }
            Button("Тренировать мужской род") { model.send("skill", "case.acc.m") }
        }
        Section(matrix.string("supportTitle")) {
            ForEach(Array(matrix.rows("supportRows").enumerated()), id: \.offset) { entry in
                Text(entry.element.string("line"))
                ForEach(Array(entry.element.rows("comparisons").enumerated()), id: \.offset) { pair in
                    NativeContrastPairView(pair: pair.element)
                }
            }
        }
    }

    @ViewBuilder private var cases: some View {
        Section("Выбери группу слов") {
            ChoiceMenu(title: "Слово", selected: matrix.string("nounId"), options: matrix.rows("nouns")) { model.send("matrixNoun", $0) }
            ChoiceMenu(title: "Прилагательное", selected: matrix.string("adjectiveId"), options: matrix.rows("adjectives")) { model.send("matrixAdjective", $0) }
            ChoiceMenu(title: "Владелец", selected: matrix.string("ownerId"), options: matrix.rows("owners")) { model.send("matrixOwner", $0) }
            ChoiceMenu(title: "Число", selected: matrix.string("numberId"), options: matrix.rows("numbers")) { model.send("matrixNumber", $0) }
        }
        ForEach(Array(matrix.rows("cases").enumerated()), id: \.offset) { _, row in
            Section(row.string("title")) {
                Text("\(row.string("question")) · \(row.string("trigger"))").font(.footnote).foregroundStyle(.secondary)
                NativeContrastPairView(pair: row.record("phrasePair"))
                NativeContrastPairView(pair: row.record("sentencePair"))
            }
        }
        Section {
            Text(matrix.string("caseNote"))
            Button("Тренировать отрицание") { model.send("skill", "case.gen.neg") }
        }
        Section("Сравнение типов склонения") {
            ChoiceMenu(title: "Падеж для сравнения", selected: comparisonCase, options: matrix.rows("comparisonCases")) { comparisonCase = $0 }
            ForEach(Array(matrix.rows("comparison").enumerated()), id: \.offset) { _, row in
                VStack(alignment: .leading) {
                    Text(row.string("title")).font(.headline)
                    NativeContrastPairView(pair: row.record("\(comparisonCase)pair"))
                }
            }
        }
    }

    @ViewBuilder private var verbs: some View {
        Section("Лицо × число × время") {
            ChoiceMenu(title: "Глагол", selected: matrix.string("verbId"), options: matrix.rows("verbs")) { model.send("matrixVerb", $0) }
            ChoiceMenu(title: matrix.string("verbGenderControlLabel"), selected: matrix.bool("feminineGroup") ? "f" : "m",
                       options: matrix.rows("verbGenderOptions")) { model.send("matrixGender", $0) }
            Text(matrix.string("verbFutureExplanation"))
        }
        ForEach(Array(matrix.rows("verbsRows").enumerated()), id: \.offset) { _, row in
            Section(row.string("title")) {
                ForEach(["present", "past", "future"], id: \.self) { tense in
                    Text(matrix.record("verbTenseLabels").string(tense)).font(.subheadline)
                    NativeContrastPairView(pair: row.record("\(tense)Pair"))
                }
            }
        }
        Section("Вид: процесс или результат") {
            ForEach(Array(matrix.rows("aspectRows").enumerated()), id: \.offset) { aspect in
                VStack(alignment: .leading, spacing: 10) {
                    Text(aspect.element.string("label")).font(.headline)
                    ForEach(Array(aspect.element.rows("entries").enumerated()), id: \.offset) { entry in
                        if entry.element.bool("available") {
                            comparisonRow(entry.element)
                        } else {
                            Text(matrix.string("aspectNoPresent"))
                        }
                    }
                }
            }
            Button("Тренировать времена") { model.send("skill", "verb.past") }
        }
        Section("Время меняется, предложение остаётся целым") {
            ForEach(Array(matrix.rows("tenseRows").enumerated()), id: \.offset) { entry in
                comparisonRow(entry.element)
            }
        }
    }

    @ViewBuilder private var pronouns: some View {
        Section { Text(matrix.string("pronounIntro")) }
        ForEach(Array(matrix.rows("pronouns").enumerated()), id: \.offset) { _, row in
            Section(row.string("title")) {
                ForEach(Array(matrix.rows("pronounContexts").enumerated()), id: \.offset) { _, context in
                    Text(context.string("iosCue")).font(.subheadline)
                    NativeContrastPairView(pair: row.record("\(context.string("id"))pair"))
                }
            }
        }
        Section { Text(matrix.string("pronounFooter")) }
        Section(matrix.string("possessiveTitle")) {
            ForEach(Array(matrix.rows("possessives").enumerated()), id: \.offset) { _, row in
                VStack(alignment: .leading) {
                    Text(row.string("title")).font(.headline)
                    ForEach(Array(matrix.rows("possessiveCases").enumerated()), id: \.offset) { _, demoCase in
                        Text(demoCase.string("iosCaption")).font(.subheadline)
                        NativeContrastPairView(pair: row.record("\(demoCase.string("id"))pair"))
                    }
                }.accessibilityElement(children: .combine)
            }
            Button("Тренировать смену владельца") { model.send("skill", "agreement.my") }
        }
    }
}

private struct ProgressView: View {
    @ObservedObject var model: AppModel
    @Binding var importing: Bool
    private var state: Record { model.state }

    var body: some View {
        Form {
            Section("Сводка") {
                LabeledContent("Всего повторений", value: "\(state.int("totalReviews"))")
                    .accessibilityElement(children: .combine)
                    .accessibilityIdentifier("totalReviews")
                LabeledContent("Сегодня", value: "\(state.int("todayCount"))")
                LabeledContent("К повторению", value: "\(state.int("dueCount"))")
                LabeledContent("Следующее", value: formattedDate(state.int64("nextDue")))
                // state.error is shown by the global ErrorBanner above every tab (C1).
            }
            Section("Навыки") {
                ForEach(state.rows("progress"), id: \.selfHash) { row in
                    Button {
                        model.send("skill", row.string("id"))
                    } label: {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(row.string("title")).font(.headline)
                            Text("Повторений: \(row.int("reviews")) · Точно: \(row.int("correct")) · Серия: \(row.int("streak"))")
                                .font(.footnote).foregroundStyle(.secondary)
                            Text("Следующее: \(formattedDate(row.int64("due")))")
                                .font(.footnote).foregroundStyle(.secondary)
                        }
                    }
                }
            }
            Section("Данные") {
                Button("Экспортировать JSON") { model.send("export") }
                Button("Импортировать JSON") { importing = true }
                Button("Сбросить прогресс", role: .destructive) { model.send("reset") }
            }
        }
        .navigationTitle("Прогресс")
        .navigationBarTitleDisplayMode(.inline)
        .frame(maxWidth: 850).frame(maxWidth: .infinity)
    }
}

private struct VocabularyView: View {
    @ObservedObject var model: AppModel
    @Binding var importing: Bool
    @Binding var exporting: Bool
    @Binding var exportFile: ProgressFile?
    @State private var editing: Record?
    @State private var deletingId: String?
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    private var state: Record { model.vocabularyState }
    // D5: system Reduce Motion OR the app's own `Motion.Reduced` gate the vocabulary flip too,
    // mirroring `TrainingView.cardMotionReduced`.
    private var cardMotionReduced: Bool {
        reduceMotion || model.preferences.string("motion") == "Reduced"
            || !model.preferences.bool("animationsEnabled", default: true)
    }

    var body: some View {
        Form {
            if let error = state["error"] as? String {
                Section { Text(error).foregroundStyle(.red) }
            }
            Section("Данные словаря") {
                Button("Импортировать словарь JSON") { importing = true }
                Button("Экспортировать словарь JSON") {
                    exportFile = model.exportVocabulary()
                    exporting = exportFile != nil
                }
                .disabled(state.string("loadStatus") == "Loading")
            }
            if state.string("loadStatus") != "Ready" {
                Section("Состояние") {
                    Text(state.string("loadStatus") == "RecoveryRequired"
                         ? "Экспортируйте исходный JSON и импортируйте исправленный словарь."
                         : "Словарь пока недоступен")
                }
            } else {
                Section("Направление") {
                    Picker("Учить", selection: Binding(
                        get: { state.string("direction") },
                        set: { model.sendVocabulary("direction", $0) }
                    )) {
                        Text("Русский → польский").tag("ru-pl")
                        Text("Польский → русский").tag("pl-ru")
                    }
                    .pickerStyle(.segmented)
                }
                Section("Карточка") {
                    if state.string("currentId").isEmpty {
                        Text(state.int("selectedCount") == 0 ? "Выберите слова для тренировки" : "На сейчас всё повторено")
                    } else {
                        ZStack {
                            VocabularyCardView(model: model, state: state, card: state.record("current"),
                                               reduceMotion: cardMotionReduced)
                            RiveEffectOverlay(effect: model.vocabularyCardEffect, reduceMotion: cardMotionReduced)
                        }
                        .listRowInsets(EdgeInsets())
                        .listRowBackground(Color.clear)
                    }
                }
                Section("Мой словарь · \(state.int("selectedCount"))") {
                    Picker("Подборка", selection: Binding(
                        get: { state.string("filter") },
                        set: { model.sendVocabulary("filter", $0) }
                    )) {
                        Text("A1").tag("A1"); Text("A2").tag("A2"); Text("B1").tag("B1")
                        Text("Топ 100").tag("100"); Text("Топ 500").tag("500")
                        Text("Топ 1000").tag("1000"); Text("Мои слова").tag("mine")
                    }
                    Text(state.string("instructions"))
                        .font(.footnote).foregroundStyle(.secondary)
                    if !state.string("coverage").isEmpty {
                        Text(state.string("coverage"))
                            .font(.footnote).foregroundStyle(.secondary)
                    }
                    Link("Leksjo / NKJP · CC BY 4.0 · изменения: первые 1000 лемм, рангов и counts",
                         destination: URL(string: "https://github.com/KubaCiolo/leksjo-dane/blob/01782aa92cc842d0d3199079eba47ecbf05879e1/dane/nkjp-frekwencja.csv")!)
                        .font(.footnote)
                    Button("Добавить своё слово") { editing = [:] }
                    ForEach(Array(state.rows("entries").enumerated()), id: \.offset) { _, entry in
                        HStack {
                            Button {
                                model.sendVocabulary(entry.bool("selected") ? "deselect" : "select", entry.string("id"))
                            } label: {
                                Image(systemName: entry.bool("selected") ? "checkmark.circle.fill" : "circle")
                            }
                            .disabled(!entry.bool("available") || state.bool("busy"))
                            .accessibilityLabel("\(entry.bool("selected") ? "Убрать" : "Добавить") \(entry.string("lemma"))")
                            .accessibilityIdentifier("vocabularySelect-\(entry.string("id"))")
                            VStack(alignment: .leading) {
                                Text(entry.string("lemma"))
                                Text(entry.bool("available") ? entry.string("translation") : state.string("unavailableLabel"))
                                    .font(.caption).foregroundStyle(.secondary)
                            }
                            Spacer()
                            if entry.bool("custom") {
                                Button("Изменить") { editing = entry }
                                    .font(.caption)
                            }
                        }
                    }
                }
            }
        }
        .navigationTitle("Слова")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(item: Binding(
            get: { editing.map(VocabularyEdit.init) },
            set: { editing = $0?.word }
        )) { item in
            VocabularyEditor(model: model, initial: item.word, onClose: { editing = nil },
                onDelete: { deletingId = $0; editing = nil })
        }
        .alert("Удалить своё слово?", isPresented: Binding(
            get: { deletingId != nil }, set: { if !$0 { deletingId = nil } }
        )) {
            Button("Удалить", role: .destructive) {
                if let id = deletingId { model.sendVocabulary("delete", id) }
                deletingId = nil
            }
            Button("Отмена", role: .cancel) { deletingId = nil }
        } message: { Text("Слово исчезнет из каталога, история оценок сохранится в JSON.") }
    }
}

private struct VocabularyEdit: Identifiable {
    let word: Record
    var id: String { word.string("id").isEmpty ? "new" : word.string("id") }
}

private struct VocabularyEditor: View {
    @ObservedObject var model: AppModel
    let initial: Record
    let onClose: () -> Void
    let onDelete: (String) -> Void
    @State private var lemma = ""
    @State private var translation = ""
    @State private var form = ""
    @State private var example = ""
    @State private var level = "—"

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    TextField("Польское слово", text: $lemma)
                    TextField("Перевод", text: $translation)
                    TextField("Форма", text: $form)
                    TextField("Пример в предложении", text: $example)
                    Picker("Уровень", selection: $level) {
                        ForEach(["—", "A1", "A2", "B1", "B2", "C1", "C2"], id: \.self) { Text($0).tag($0) }
                    }
                    if !initial.string("id").isEmpty {
                        Button("Удалить слово", role: .destructive) { onDelete(initial.string("id")) }
                    }
                }
            }
            .navigationTitle(initial.string("id").isEmpty ? "Новое слово" : "Изменить слово")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Отмена", action: onClose) }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Сохранить") {
                        model.saveVocabularyWord(["lemma": lemma, "translation": translation,
                            "form": form, "example": example, "level": level],
                            editingId: initial.string("id").isEmpty ? nil : initial.string("id")) { saved in
                            if saved { onClose() }
                        }
                    }
                }
            }
            .onAppear {
                lemma = initial.string("lemma"); translation = initial.string("translation")
                form = initial.string("form"); example = initial.string("example")
                level = initial.string("level").isEmpty ? "—" : initial.string("level")
            }
        }
    }
}

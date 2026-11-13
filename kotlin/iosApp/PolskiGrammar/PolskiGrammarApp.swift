import SwiftUI
import UniformTypeIdentifiers
import PolskiShared

typealias Record = [String: Any]

/// Reads the current pack's complete copy, then the seven legacy snapshot fields for old frameworks.
func uiText(_ state: Record, _ key: String, fallback: String) -> String {
    let semanticKey = key.hasPrefix("ui")
        ? String(key.dropFirst(2).prefix(1)).lowercased() + String(key.dropFirst(3)) : key
    if let value = (state["uiCopy"] as? Record)?[semanticKey] as? String { return value }
    return state[key] as? String ?? fallback
}

/// Validates authored placeholders, then inserts values once so user text is never parsed as copy.
func uiFormat(_ state: Record, _ key: String, fallback: String, values: [String: String]) -> String {
    let template = uiText(state, key, fallback: fallback)
    var fields: [(range: Range<String.Index>, name: String)] = []
    var cursor = template.startIndex
    var used = Set<String>()
    while let open = template[cursor...].firstIndex(of: "{") {
        assert(!template[cursor..<open].contains("}"), "Malformed UI copy template: \(key)")
        guard let close = template[open...].firstIndex(of: "}") else {
            assertionFailure("Unfilled UI copy template: \(key)")
            return template
        }
        let name = String(template[template.index(after: open)..<close])
        assert(values[name] != nil, "Unfilled UI copy template: \(key).\(name)")
        used.insert(name)
        let end = template.index(after: close)
        fields.append((open..<end, name))
        cursor = end
    }
    assert(!template[cursor...].contains("}"), "Malformed UI copy template: \(key)")
    for name in values.keys {
        precondition(used.contains(name), "Unknown UI copy placeholder: \(key).\(name)")
    }
    var result = ""
    cursor = template.startIndex
    for field in fields {
        result.append(contentsOf: template[cursor..<field.range.lowerBound])
        result.append(contentsOf: values[field.name] ?? String(template[field.range]))
        cursor = field.range.upperBound
    }
    result.append(contentsOf: template[cursor...])
    return result
}

private enum StudyStyle {
    static let ink = Color(red: 0.08, green: 0.19, blue: 0.22)
    static let amber = Color(red: 0.98, green: 0.72, blue: 0.38)
    static let canvas = Color(uiColor: .systemGroupedBackground)
}

private struct StudyHero: View {
    let copy: Record
    let due: Int
    let today: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Label("POLSKI · GRAMMAR MATRIX", systemImage: "sparkle")
                .font(.caption.weight(.bold)).tracking(1.2)
                .foregroundStyle(StudyStyle.amber)
            Text(uiText(copy, "heroSubtitle", fallback: "Говори целыми предложениями"))
                .font(.system(.title3, design: .rounded, weight: .bold))
                .foregroundStyle(.white)
                .fixedSize(horizontal: false, vertical: true)
            HStack(spacing: 8) {
                heroStat(uiText(copy, "dueStat", fallback: "К повторению"), due)
                heroStat(uiText(copy, "todayStat", fallback: "Сегодня"), today)
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
    let copy: Record
    let message: String
    let onExport: () -> Void

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: "exclamationmark.triangle.fill").foregroundStyle(.red)
            VStack(alignment: .leading, spacing: 6) {
                Text(message).font(.footnote).fontWeight(.semibold)
                Button(uiText(copy, "exportProgressJson", fallback: "Экспортировать JSON"), action: onExport)
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
    /// Like [rows], one level deeper — `Block.Examples.itemParts` (`StyleSnapshot.kt`'s
    /// `List<List<EndingPart>>`), one `[Record]` of `EndingPart` rows per example item.
    func rowsOfRows(_ key: String) -> [[Record]] { self[key] as? [[Record]] ?? [] }
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

// EN-22 (Plans/Kotlin/EnRuPackPlan.md §6): `coursePacks` (IosPreferencesSession.currentSnapshot)
// is the real, currently-registered (target, native) pairs — pl-ru and en-ru today, never a
// hardcoded literal pair here; only the *display* label for a known code is this file's own copy,
// the same fallback pattern `styleFallbackLabel` above already uses for a style without recipe text.
private func courseLanguageName(_ code: String, state: Record) -> String {
    uiText(state, "lang.\(code)", fallback: code)
}

// EnRuAcceptance-2026-09-28.md §7 item 4: the vocabulary screen's own 2 small Russian phrasings
// that need a language's name lowercase (mid-clause, e.g. "Русский → польский") or as the
// instrumental adverb ("Вспомни по-польски") — [courseLanguageName] above is capitalized (a
// Settings picker's own leading word), so neither reuses it. Same fallback-to-code pattern.
private func courseDirectionLabel(from: String, to: String, state: Record) -> String {
    let source = uiText(state, "langLower.\(from)", fallback: from)
    let target = uiText(state, "langLower.\(to)", fallback: to)
    return "\(source.prefix(1).uppercased() + source.dropFirst()) → \(target)"
}

/// The distinct values of [key] ("target"/"native") across every row of `preferences`'s
/// `coursePacks`, in first-seen order — the real option list for one of the two EN-22 pickers.
private func coursePackValues(_ preferences: Record, _ key: String) -> [String] {
    var seen: [String] = []
    for row in preferences.rows("coursePacks") {
        let value = row.string(key)
        if !seen.contains(value) { seen.append(value) }
    }
    return seen
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
        // EnRuAcceptance-2026-09-28.md §7 item 2 (iOS false-green fix): must run before any
        // bridge's `onState` is assigned below. `preferencesSession.onState`'s setter eagerly
        // calls `currentSnapshot()` (see its own KDoc), which reconciles the persisted
        // target/native against `activeCoursePackId` — while the active pack was still the
        // process-default pl-ru, that reconciliation always found a "mismatch" and immediately
        // self-corrected a genuinely persisted en-ru choice back to pl-ru before this method
        // (the only thing that actually switches the active pack) ever ran, so a real, working
        // en-ru pack silently reverted on every cold start. `session.onState`'s own setter also
        // triggers a rebuild (`IosSession.rebuildIfCourseSwitched`), so running this first makes
        // that first rebuild the real one, instead of a second, redundant one below.
        preferencesSession.reapplySavedCoursePack()
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
    @discardableResult
    func setPreference(_ field: String, _ value: String) -> Bool {
        if let error = preferencesSession.set(field: field, value: value) { notice = error; return false }
        if field == "target" || field == "native" { send("refresh") }
        if field == "styleId" { send("styleId", value) }
        if field == "answerMode" { send("answerMode", value) }
        return true
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
                notice = uiText(state, "importSettingsSizeLimit", fallback: "Файл должен быть UTF-8 JSON не больше 1 МБ")
                return
            }
            notice = preferencesSession.importJson(raw: raw)
                ?? uiText(state, "settingsImported", fallback: "Настройки импортированы")
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
                notice = uiText(state, "importDataSizeLimit", fallback: "Файл должен быть UTF-8 JSON не больше 10 МБ")
                return
            }
            vocabulary.importJson(raw: raw) { [weak self] success in
                DispatchQueue.main.async { [weak self] in
                    guard let self else { return }
                    let error = self.vocabularyState.string("error")
                    self.notice = success.boolValue
                        ? uiText(self.state, "vocabularyImported", fallback: "Словарь импортирован")
                        : (error.isEmpty ? uiText(self.state, "vocabularyImportFailed", fallback: "Импорт словаря не удался") : error)
                }
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
        case .success: acknowledge(id); notice = uiText(state, "progressExported", fallback: "Прогресс экспортирован")
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
                notice = uiText(state, "importDataSizeLimit", fallback: "Файл должен быть UTF-8 JSON не больше 10 МБ")
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
                    if let self {
                        self.notice = error ?? uiText(self.state, "progressImported", fallback: "Прогресс импортирован")
                    }
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
        // ADR-38 (EnRuAcceptance §7 item 2, blocker 2 correction): `packSwitchWarning` is a
        // one-shot field — present only on the snapshot right after a training bridge elsewhere
        // rolled back a pack switch that `set` had accepted. Surfacing it through the same
        // `notice` alert every other Settings error already uses is what makes that self-
        // correction visible instead of silent (see `IosPreferencesSession.reconcileWithActivePack`).
        if let warning = parsed["packSwitchWarning"] as? String, !warning.isEmpty { notice = warning }
    }
}

@main
struct PolskiGrammarApp: App {
    @StateObject private var model = AppModel()
    @StateObject private var nativeExam = NativeExamModel()
    @State private var examTabSelected = false
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
    private let tabOrder = ["Training", "Matrix", "Progress", "Vocabulary", "Exam"]
    private var readyExamFeedbackPreference: Bool? {
        guard model.preferences.string("status") == "Ready" else { return nil }
        return model.preferences.bool("immediateExamFeedbackEnabled", default: true)
    }
    private var selectedTab: String { examTabSelected ? "Exam" : model.state.string("tab") }
    private var tabBarItems: [TabBarItem] { [
        TabBarItem(tag: "Training", title: uiText(model.state, "trainingTab", fallback: "Тренировка"), icon: "square.stack"),
        TabBarItem(tag: "Matrix", title: uiText(model.state, "matrixNavTitle", fallback: "Матрица"), icon: "tablecells"),
        TabBarItem(tag: "Progress", title: uiText(model.state, "uiProgressTab", fallback: "Прогресс"), icon: "chart.bar"),
        TabBarItem(tag: "Vocabulary", title: uiText(model.state, "vocabularyTab", fallback: "Слова"), icon: "character.book.closed"),
        TabBarItem(tag: "Exam", title: uiText(model.state, "exam", fallback: "exam"), icon: "text.book.closed"),
    ] }

    var body: some Scene {
        WindowGroup {
            VStack(spacing: 0) {
                // C1: a save failure (state.error) must stay visible above every tab, regardless
                // of loadStatus, with the export action reachable so no reviewed progress is lost.
                if let error = model.state["error"] as? String {
                    ErrorBanner(copy: model.state, message: error) { model.send("export") }
                }
                // D4: a custom paging container, not `TabView` — `TabView` swaps tab content
                // instantly with no transition slot, so there is no way to make old/new content
                // slide together from it. Only the *active* tab's subtree is mounted (matching
                // `TabView`'s own accessibility behavior: an inactive tab's elements must not be
                // independently discoverable), so `.transition` on the `if` below sees a real
                // insertion/removal, not just a value change, for exactly one tab at a time.
                ZStack {
                    ForEach(tabOrder, id: \.self) { tab in
                        if tab == selectedTab {
                            tabContent(tab)
                                .transition(.asymmetric(
                                    insertion: .move(edge: pagingEdge),
                                    removal: .move(edge: pagingEdge == .trailing ? .leading : .trailing)))
                        }
                    }
                }
                .animation(motionActive ? .easeOut(duration: 0.3) : nil, value: selectedTab)
                .onChange(of: selectedTab) { old, new in
                    let oldIndex = tabOrder.firstIndex(of: old) ?? 0
                    let newIndex = tabOrder.firstIndex(of: new) ?? 0
                    pagingEdge = newIndex >= oldIndex ? .trailing : .leading
                }
                Divider()
                PagingTabBar(selected: selectedTab, items: tabBarItems) { tab in
                    if tab == "Exam" { examTabSelected = true }
                    else { examTabSelected = false; model.send("tab", tab) }
                }
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
                        exporting: $exportingPreferences, exportFile: $preferencesFile,
                        setExamFeedback: { nativeExam.setImmediateFeedbackEnabled($0) })
                }
            }
            .onChange(of: model.exportEffectId) { _, id in if id != nil { exporting = true } }
            .onChange(of: readyExamFeedbackPreference) { _, enabled in
                if let enabled { nativeExam.setImmediateFeedbackEnabled(enabled) }
            }
            .onAppear {
                if let enabled = readyExamFeedbackPreference { nativeExam.setImmediateFeedbackEnabled(enabled) }
            }
            .alert(uiText(model.state, "resetTitle", fallback: "Сбросить весь прогресс?"), isPresented: Binding(
                get: { model.resetEffectId != nil },
                set: { if !$0 && model.resetEffectId != nil { model.decideReset(false) } }
            )) {
                Button(uiText(model.state, "resetConfirm", fallback: "Сбросить"), role: .destructive) { model.decideReset(true) }
                Button(uiText(model.state, "cancel", fallback: "Отмена"), role: .cancel) { model.decideReset(false) }
            } message: { Text(uiText(model.state, "resetWarning", fallback: "Все оценки и расписание повторений будут удалены.")) }
            .alert(uiText(model.state, "noticeTitle", fallback: "Сообщение"), isPresented: Binding(
                get: { model.notice != nil }, set: { if !$0 { model.notice = nil } }
            )) { Button(uiText(model.state, "ok", fallback: "ОК")) { model.notice = nil } } message: { Text(model.notice ?? "") }
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
        case "Exam":
            NavigationStack { NativeExamView(model: nativeExam, copy: model.state).toolbar { settingsToolbar } }
        default:
            NavigationStack { VocabularyView(model: model, importing: $importingVocabulary,
                exporting: $exportingVocabulary, exportFile: $vocabularyFile).toolbar { settingsToolbar } }
        }
    }

    private var settingsToolbar: some ToolbarContent {
        ToolbarItem(placement: .topBarTrailing) {
            Button { showingSettings = true } label: { Label(uiText(model.state, "uiSettingsTab", fallback: "Настройки"), systemImage: "gearshape") }
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
    let setExamFeedback: (Bool) -> Void
    @Environment(\.dismiss) private var dismiss
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    #if DEBUG
    @AppStorage("polski.debug.riveDisabled") private var riveDisabledForMeasurement = false
    #endif

    var body: some View {
        Form {
            if model.preferences.string("status") == "RecoveryRequired" {
                Section(uiText(model.state, "recoverySection", fallback: "Восстановление")) {
                    Text(model.preferences.string("error")).foregroundStyle(.red)
                    Text(uiText(model.state, "recoveryAdvice", fallback: "Экспортируйте исходные настройки перед импортом проверенной копии."))
                }
            } else {
                Section(uiText(model.state, "courseSection", fallback: "Курс")) {
                    // EN-22: options come from `coursePacks` (every really-registered pack), not a
                    // hardcoded pl/en pair — picking a target keeps the current native unless that
                    // exact combination doesn't exist, in which case `model.setPreference` surfaces
                    // the real rejection as `notice` (see `set`'s "Неизвестная пара языков" error).
                    Picker(uiText(model.state, "targetLanguage", fallback: "Изучаемый язык"), selection: Binding(
                        get: { model.preferences.string("target") },
                        set: { model.setPreference("target", $0) }
                    )) {
                        ForEach(coursePackValues(model.preferences, "target"), id: \.self) { code in
                            Text(courseLanguageName(code, state: model.state)).tag(code)
                        }
                    }
                    .accessibilityIdentifier("settingsTargetPicker")
                    Picker(uiText(model.state, "nativeLanguage", fallback: "Родной язык"), selection: Binding(
                        get: { model.preferences.string("native") },
                        set: { model.setPreference("native", $0) }
                    )) {
                        ForEach(coursePackValues(model.preferences, "native"), id: \.self) { code in
                            Text(courseLanguageName(code, state: model.state)).tag(code)
                        }
                    }
                    .accessibilityIdentifier("settingsNativePicker")
                }
                Section(uiText(model.state, "learningSection", fallback: "Обучение")) {
                    Toggle(uiText(model.state, "examImmediateFeedbackSetting", fallback: "Сразу показывать ответ в тренировочном экзамене"), isOn: Binding(
                        get: { model.preferences.bool("immediateExamFeedbackEnabled", default: true) },
                        set: { enabled in
                            if model.setPreference("immediateExamFeedbackEnabled", enabled ? "true" : "false") {
                                setExamFeedback(enabled)
                            }
                        }
                    ))
                    .accessibilityIdentifier("settingsExamImmediateFeedback")
                    // UC-10/S1: 4 styles replace the old Logic/Situations selector — the picker's
                    // own selected-value row shows the label, the caption below shows its
                    // description (both prefer StyleRegistry's recipe text, see [styleOptions]).
                    Picker(uiText(model.state, "explanationStyle", fallback: "Стиль объяснений"), selection: Binding(
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
                        Text(uiText(model.state, "nativeContrastFallback", fallback: "Для текущего навыка пока нет пары для сравнения с родным — карточка показывается как «через правило»."))
                            .font(.footnote).foregroundStyle(.secondary)
                    }
                    Picker(uiText(model.state, "answerModeTitle", fallback: "Ответ"), selection: Binding(
                        get: { model.preferences.string("answerMode") },
                        set: { model.setPreference("answerMode", $0) }
                    )) {
                        Text(uiText(model.state, "oralMode", fallback: "Вслух")).tag("Oral")
                        Text(uiText(model.state, "typedMode", fallback: "Напечатать")).tag("Typed")
                    }
                    // Distinct from the training screen's own "Ответ" picker (FlashCardView),
                    // which stays mounted under this sheet and shares the same label — a
                    // label-based query can't tell them apart. See testAnswerModeChosenInSettingsSurvivesAppRestart.
                    .accessibilityIdentifier("settingsAnswerModePicker")
                }
                Section(uiText(model.state, "appearanceSection", fallback: "Внешний вид")) {
                    Picker(uiText(model.state, "themeTitle", fallback: "Тема"), selection: Binding(
                        get: { model.preferences.string("appearance") },
                        set: { model.setPreference("appearance", $0) }
                    )) {
                        Text(uiText(model.state, "themeSystem", fallback: "Системная")).tag("System")
                        Text(uiText(model.state, "themeLight", fallback: "Светлая")).tag("Light")
                        Text(uiText(model.state, "themeDark", fallback: "Тёмная")).tag("Dark")
                    }
                    Picker(uiText(model.state, "motionTitle", fallback: "Движение"), selection: Binding(
                        get: { model.preferences.string("motion") },
                        set: { model.setPreference("motion", $0) }
                    )) {
                        Text(uiText(model.state, "motionSystem", fallback: "Системное")).tag("System")
                        Text(uiText(model.state, "motionReduced", fallback: "Сокращённое")).tag("Reduced")
                    }
                    Text(reduceMotion ? uiText(model.state, "motionReducedStatus", fallback: "Система сокращает движение") : uiText(model.state, "motionActiveStatus", fallback: "Системное движение активно"))
                        .font(.footnote).foregroundStyle(.secondary)
                    // D5: master switch — off means no card/tab/reveal animation and, per
                    // `cardMotionReduced`/`motionActive` above, RiveViewModel is never created.
                    Toggle(uiText(model.state, "animationsTitle", fallback: "Анимации"), isOn: Binding(
                        get: { model.preferences.bool("animationsEnabled", default: true) },
                        set: { model.setPreference("animationsEnabled", $0 ? "true" : "false") }
                    ))
                }
            }
            Section(uiText(model.state, "settingsDataSection", fallback: "Данные настроек")) {
                Button(uiText(model.state, "exportSettingsJson", fallback: "Экспортировать настройки JSON")) {
                    exportFile = model.exportPreferences()
                    exporting = exportFile != nil
                }
                Button(uiText(model.state, "importSettingsJson", fallback: "Импортировать настройки JSON")) { importing = true }
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
        .navigationTitle(uiText(model.state, "uiSettingsTab", fallback: "Настройки"))
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .confirmationAction) { Button(uiText(model.state, "done", fallback: "Готово")) { dismiss() } } }
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
                StudyHero(copy: state, due: state.int("dueCount"), today: state.int("todayCount"))
                    .listRowInsets(EdgeInsets())
                    .listRowBackground(Color.clear)
            }
            if state.string("loadStatus") != "Ready" {
                Section(uiText(state, "uiProgressTab", fallback: "Прогресс")) {
                    Text(state.string("loadStatus") == "Loading" ? uiText(model.state, "loading", fallback: "Загружаем…") : uiText(model.state, "needsProgressCopy", fallback: "Нужна копия прогресса"))
                    // state.error is shown by the global ErrorBanner above every tab (C1).
                    Text(uiText(model.state, "openProgressForTransfer", fallback: "Откройте «Прогресс» для импорта или экспорта JSON."))
                }
            } else {
                Section {
                    Menu(uiFormat(state, "modeTemplate", fallback: "Режим: {mode}", values: ["mode": modeTitle])) {
                        Button(uiText(model.state, "sentenceChain", fallback: "Цепочка предложений")) { model.send("chain") }
                        Button(uiText(model.state, "scheduledReviews", fallback: "По расписанию")) { model.send("schedule") }
                        Button(uiText(model.state, "singleSkill", fallback: "Отдельный навык")) { model.send("skillPicker") }
                    }
                    if state.bool("showSkillPicker") {
                        ChoiceMenu(title: uiText(model.state, "skillTitle", fallback: "Навык"), selected: "", options: state.rows("skills")) { model.send("skill", $0) }
                    }
                    if state.string("mode") == "Chain" {
                        ChoiceMenu(title: uiText(model.state, "wordSet", fallback: "Набор слов"), selected: "\(state.int("seedIndex"))", options: state.rows("seeds")) { model.send("seed", $0) }
                        Text("\(state.int("chainDisplayCount")) / \(state.int("chainCount")) · \(state.string("chainStepSummary"))")
                            .font(.footnote).foregroundStyle(.secondary)
                    }
                    // S1 compact quick switch — same 4 styles as Settings, same location the old
                    // Logic/Situations toggle held. Dispatches SetStyle only: never creates a
                    // review, never touches draft/frozenAnswer (TrainingStore.SetStyle, ST-04).
                    Picker(uiText(model.state, "explanationStyle", fallback: "Стиль объяснений"), selection: Binding(
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
                        Button(uiText(model.state, "nextWordSet", fallback: "Следующий набор слов")) { model.send("seed", "\((state.int("seedIndex") + 1) % max(1, state.rows("seeds").count))") }
                        Button(uiText(model.state, "backToReviews", fallback: "К повторениям")) { model.send("schedule") }
                    }
                case "NoDue":
                    Section(uiText(model.state, "reviewsDone", fallback: "Повторения на сейчас завершены")) {
                        Text(uiFormat(state, "nextDueTemplate", fallback: "Следующее: {date}",
                            values: ["date": formattedDate(state.int64("nextDue"))]))
                        Button(uiText(model.state, "practiceChain", fallback: "Потренировать цепочку")) { model.send("chain") }
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
                    if state.bool("caseReferenceAvailable") {
                        Button(!introducing && state.bool("showReference") ? uiText(model.state, "hideTable", fallback: "Скрыть таблицу") : uiText(model.state, "tableAtHand", fallback: "Таблица под рукой")) {
                            model.send("reference")
                        }
                        .disabled(introducing)
                    }
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
                                    NativeContrastPairView(pair: row.record("pair"), copy: model.state)
                                }
                            }
                            Button(uiText(model.state, "allTablesAndSchema", fallback: "Все таблицы и схема")) { model.send("tab", "Matrix") }
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
                    revealButton(title: uiText(model.state, "checkAnswer", fallback: "Проверить ответ"), expands: false)
                }
            }
        }
        .scrollDismissesKeyboard(.immediately)
        .navigationTitle(uiText(model.state, "trainingTab", fallback: "Тренировка"))
        .navigationBarTitleDisplayMode(.inline)
        .frame(maxWidth: 850).frame(maxWidth: .infinity)
        .onAppear { localDraft = state.string("draft") }
        .onChange(of: card.string("id")) { _, _ in localDraft = state.string("draft") }
        .animation(cardMotionReduced ? nil : .default, value: state.string("phase"))
    }

    private var modeTitle: String {
        switch state.string("mode") {
        case "Schedule": uiText(model.state, "scheduledReviews", fallback: "По расписанию")
        case "Focused": uiText(model.state, "singleSkill", fallback: "Отдельный навык")
        default: uiText(model.state, "sentenceChain", fallback: "Цепочка предложений")
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
    let copy: Record

    var body: some View {
        VStack(alignment: .leading, spacing: 3) {
            Text(uiText(copy, "before", fallback: "Было")).font(.caption)
            styledParts(pair.rows("beforeParts"), role: .before)
            Text("→").font(.caption)
            Text(uiText(copy, "after", fallback: "Стало")).font(.caption)
            styledParts(pair.rows("afterParts"), role: .after)
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("\(uiText(copy, "before", fallback: "Было")): \(pair.string("from")); \(uiText(copy, "after", fallback: "Стало")): \(pair.string("to"))")
    }
}

private struct MatrixView: View {
    @ObservedObject var model: AppModel
    @State private var comparisonCase = "NOM"
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    // This task's "Лайфхаки" listing (EnRuPackPlan.md §4.3) is a `Раздел` option like the other
    // 4, but it has no counterpart in the shared `MatrixSection` enum (`AppUiState.kt`) — the
    // `lifehackGroups` data it reads is a top-level, section-independent snapshot field (always
    // fully computed, ADR-46), so there is nothing for a 5th bridged section to select. Kept as
    // pure host-local UI state instead of growing the shared enum for a selection Kotlin never
    // needs to know: non-nil overrides the bridge-driven `matrix.section` below; picking any of
    // the other 4 options clears it back to `nil` and dispatches to the bridge as before.
    @State private var localSection: String? = nil
    private var matrix: Record { model.state.record("matrix") }
    private var section: String { localSection ?? matrix.string("section") }

    var body: some View {
        Form {
            Section {
                Text(matrix.string("matrixIntroduction"))
                Picker(uiText(model.state, "matrixSection", fallback: "Раздел"), selection: Binding(get: { section }, set: { newValue in
                    if newValue == "Lifehacks" { localSection = "Lifehacks" } else {
                        localSection = nil
                        model.send("matrixSection", newValue)
                    }
                })) {
                    Text(uiText(model.state, "matrixMap", fallback: "Карта системы")).tag("Map")
                    Text(uiText(model.state, "casesAndEndings", fallback: "Падежи и окончания")).tag("Cases")
                    Text(uiText(model.state, "tensesAndPersons", fallback: "Времена и лица")).tag("Verbs")
                    Text(uiText(model.state, "pronouns", fallback: "Местоимения")).tag("Pronouns")
                    Text(uiText(model.state, "uiLifehacks", fallback: "Лайфхаки")).tag("Lifehacks")
                }
                .accessibilityIdentifier("matrixSectionPicker")
            }
            switch section {
            case "Cases": cases
            case "Verbs": verbs
            case "Pronouns": pronouns
            case "Lifehacks": lifehacks
            default: map
            }
        }
        .navigationTitle(uiText(model.state, "matrixNavTitle", fallback: "Матрица"))
        .navigationBarTitleDisplayMode(.inline)
        .frame(maxWidth: 850).frame(maxWidth: .infinity)
    }

    private func comparisonRow(_ row: Record, includeNote: Bool = false) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(row.string("label")).font(.headline)
            NativeContrastPairView(pair: row, copy: model.state)
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
                        NativeContrastPairView(pair: step.element, copy: model.state)
                    }
                }
                .accessibilityElement(children: .contain)
            }
        }
        Section("Одна мысль, пять преобразований") {
            ForEach(Array(matrix.rows("chainRows").enumerated()), id: \.offset) { entry in
                comparisonRow(entry.element, includeNote: true)
            }
            Button(uiText(model.state, "matrixTrainChain", fallback: "Тренировать эту цепочку")) { model.send("skill", "chain") }
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
            Button(uiText(model.state, "matrixTrainMasculine", fallback: "Тренировать мужской род")) { model.send("skill", "case.acc.m") }
        }
        Section(matrix.string("supportTitle")) {
            ForEach(Array(matrix.rows("supportRows").enumerated()), id: \.offset) { entry in
                Text(entry.element.string("line"))
                ForEach(Array(entry.element.rows("comparisons").enumerated()), id: \.offset) { pair in
                    NativeContrastPairView(pair: pair.element, copy: model.state)
                }
            }
        }
    }

    @ViewBuilder private var cases: some View {
        Section(uiText(model.state, "matrixChooseWordGroup", fallback: "Выбери группу слов")) {
            ChoiceMenu(title: uiText(model.state, "matrixWord", fallback: "Слово"), selected: matrix.string("nounId"), options: matrix.rows("nouns")) { model.send("matrixNoun", $0) }
            ChoiceMenu(title: uiText(model.state, "matrixAdjective", fallback: "Прилагательное"), selected: matrix.string("adjectiveId"), options: matrix.rows("adjectives")) { model.send("matrixAdjective", $0) }
            ChoiceMenu(title: uiText(model.state, "matrixOwner", fallback: "Владелец"), selected: matrix.string("ownerId"), options: matrix.rows("owners")) { model.send("matrixOwner", $0) }
            ChoiceMenu(title: uiText(model.state, "matrixNumber", fallback: "Число"), selected: matrix.string("numberId"), options: matrix.rows("numbers")) { model.send("matrixNumber", $0) }
        }
        ForEach(Array(matrix.rows("cases").enumerated()), id: \.offset) { _, row in
            Section(row.string("title")) {
                Text("\(row.string("question")) · \(row.string("trigger"))").font(.footnote).foregroundStyle(.secondary)
                NativeContrastPairView(pair: row.record("phrasePair"), copy: model.state)
                NativeContrastPairView(pair: row.record("sentencePair"), copy: model.state)
            }
        }
        Section {
            Text(matrix.string("caseNote"))
            Button(uiText(model.state, "matrixTrainNegation", fallback: "Тренировать отрицание")) { model.send("skill", "case.gen.neg") }
        }
        Section(uiText(model.state, "matrixDeclensionComparison", fallback: "Сравнение типов склонения")) {
            ChoiceMenu(title: uiText(model.state, "matrixCaseComparison", fallback: "Падеж для сравнения"), selected: comparisonCase, options: matrix.rows("comparisonCases")) { comparisonCase = $0 }
            ForEach(Array(matrix.rows("comparison").enumerated()), id: \.offset) { _, row in
                VStack(alignment: .leading) {
                    Text(row.string("title")).font(.headline)
                    NativeContrastPairView(pair: row.record("\(comparisonCase)pair"), copy: model.state)
                }
            }
        }
    }

    @ViewBuilder private var verbs: some View {
        if matrix.string("targetLanguage") == "pl" {
            Section("Лицо × число × время") {
                ChoiceMenu(title: uiText(model.state, "matrixVerb", fallback: "Глагол"), selected: matrix.string("verbId"), options: matrix.rows("verbs")) { model.send("matrixVerb", $0) }
                ChoiceMenu(title: matrix.string("verbGenderControlLabel"), selected: matrix.bool("feminineGroup") ? "f" : "m",
                           options: matrix.rows("verbGenderOptions")) { model.send("matrixGender", $0) }
                Text(matrix.string("verbFutureExplanation"))
            }
            ForEach(Array(matrix.rows("verbsRows").enumerated()), id: \.offset) { _, row in
                Section(row.string("title")) {
                    ForEach(["present", "past", "future"], id: \.self) { tense in
                        Text(matrix.record("verbTenseLabels").string(tense)).font(.subheadline)
                        NativeContrastPairView(pair: row.record("\(tense)Pair"), copy: model.state)
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
                Button(uiText(model.state, "matrixTrainTenses", fallback: "Тренировать времена")) { model.send("skill", "verb.past") }
            }
            Section("Время меняется, предложение остаётся целым") {
                ForEach(Array(matrix.rows("tenseRows").enumerated()), id: \.offset) { entry in
                    comparisonRow(entry.element)
                }
            }
        }
        if matrix.string("targetLanguage") == "en" {
            english
        }
    }

    // EN-24 (UC-09 part 2/2, ios lane, EnRuPackPlan.md §5 gap H / §6): mirrors the web host's
    // `renderEnglishVerbMatrix` (ADR-28) — the one live English matrix table (fixed example verb
    // "see", no selector — a minimum slice, not a full English matrix UI) plus a do-support table,
    // both fed by `IosSnapshot.kt`'s `enVerbForm` (shared with the web host). Deliberately not
    // `matrix.record("verbTenseLabels")` above — that map holds Polish glosses ("Teraz" etc.) for
    // pl's own tense column headers, which would be a real a11y/correctness bug on an English
    // table (the same class of bug ADR-28 fixed for `lang="pl"` on the web host).
    private var englishTenseLabel: [String: String] { [
        "present": uiText(model.state, "tense.present", fallback: "Настоящее"),
        "past": uiText(model.state, "tense.past", fallback: "Прошедшее"),
        "future": uiText(model.state, "tense.future", fallback: "Будущее"),
    ] }

    @ViewBuilder private var english: some View {
        Section("English: лицо × время (\"\(matrix.string("englishExampleLemma"))\")") {
            Text("Формы английского глагола по лицам и временам.")
        }
        ForEach(Array(matrix.rows("englishVerbsRows").enumerated()), id: \.offset) { _, row in
            Section(row.string("title")) {
                ForEach(["present", "past", "future"], id: \.self) { tense in
                    Text(englishTenseLabel[tense] ?? tense).font(.subheadline)
                    NativeContrastPairView(pair: row.record("\(tense)Pair"), copy: model.state)
                }
            }
        }
        Section("do-support: вопрос и отрицание") {
            Text("«do/does/did» встаёт перед подлежащим (Do you see…?) или перед «not» (I do not see…). У будущего своего do-support нет — вопрос и отрицание строятся через «will» само по себе.")
        }
        ForEach(Array(matrix.rows("englishDoSupportRows").enumerated()), id: \.offset) { _, row in
            Section(row.string("title")) {
                ForEach(["present", "past"], id: \.self) { tense in
                    Text(englishTenseLabel[tense] ?? tense).font(.subheadline)
                    NativeContrastPairView(pair: row.record("\(tense)Pair"), copy: model.state)
                }
                Text(englishTenseLabel["future"] ?? "future").font(.subheadline)
                Text("не нужен — только will").font(.footnote).foregroundStyle(.secondary)
            }
        }
    }

    @ViewBuilder private var pronouns: some View {
        Section { Text(matrix.string("pronounIntro")) }
        ForEach(Array(matrix.rows("pronouns").enumerated()), id: \.offset) { _, row in
            Section(row.string("title")) {
                ForEach(Array(matrix.rows("pronounContexts").enumerated()), id: \.offset) { _, context in
                    Text(context.string("iosCue")).font(.subheadline)
                    NativeContrastPairView(pair: row.record("\(context.string("id"))pair"), copy: model.state)
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
                        NativeContrastPairView(pair: row.record("\(demoCase.string("id"))pair"), copy: model.state)
                    }
                }.accessibilityElement(children: .combine)
            }
            Button(uiText(model.state, "matrixTrainOwnerChange", fallback: "Тренировать смену владельца")) { model.send("skill", "agreement.my") }
        }
    }

    /// EnRuPackPlan.md §4.3's pack-wide "Лайфхаки" listing, from `IosSnapshot.kt`'s top-level
    /// `lifehackGroups` (`LifehackProvider.listAll()`, ADR-46) — one `Form` `Section` per skill/
    /// topic that actually has an authored tip, already in curriculum order (real skill titles),
    /// each with its own collapsible entries (same [LifehackEntryView] the per-skill card back
    /// face uses, `FlashCardView.swift`). Works for whichever pack is active (pl-ru or en-ru,
    /// EN-22's picker) — this reads nothing pack-specific itself. Absent entirely (no empty
    /// section) when the active pack has no authored lifehacks at all yet.
    @ViewBuilder private var lifehacks: some View {
        let groups = model.state.rows("lifehackGroups")
        if groups.isEmpty {
            Section(uiText(model.state, "uiLifehacks", fallback: "Лайфхаки")) {
                Text(uiText(model.state, "noLifehacks", fallback: "Для этого набора лайфхаков пока нет.")).foregroundStyle(.secondary)
            }
        } else {
            ForEach(Array(groups.enumerated()), id: \.offset) { groupIndex, group in
                Section(group.string("title")) {
                    let hacks = group.rows("lifehacks")
                    ForEach(Array(hacks.enumerated()), id: \.offset) { index, hack in
                        LifehackEntryView(hack: hack, index: index, reduceMotion: reduceMotion, copy: model.state,
                            identifierPrefix: "matrixLifehack-\(groupIndex)")
                    }
                }
            }
        }
    }
}

private struct ProgressView: View {
    @ObservedObject var model: AppModel
    @Binding var importing: Bool
    private var state: Record { model.state }

    var body: some View {
        Form {
            Section(uiText(model.state, "summary", fallback: "Сводка")) {
                LabeledContent(uiText(model.state, "totalReviews", fallback: "Всего повторений"), value: "\(state.int("totalReviews"))")
                    .accessibilityElement(children: .combine)
                    .accessibilityIdentifier("totalReviews")
                LabeledContent(uiText(model.state, "todayStat", fallback: "Сегодня"), value: "\(state.int("todayCount"))")
                LabeledContent(uiText(model.state, "dueStat", fallback: "К повторению"), value: "\(state.int("dueCount"))")
                LabeledContent(uiText(model.state, "nextDueLabel", fallback: "Следующее"), value: formattedDate(state.int64("nextDue")))
                // state.error is shown by the global ErrorBanner above every tab (C1).
            }
            Section(uiText(model.state, "skills", fallback: "Навыки")) {
                ForEach(state.rows("progress"), id: \.selfHash) { row in
                    Button {
                        model.send("skill", row.string("id"))
                    } label: {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(row.string("title")).font(.headline)
                            Text(uiFormat(state, "reviewsCountTemplate",
                                fallback: "Повторений: {reviews} · Точно: {correct} · Серия: {streak}",
                                values: ["reviews": "\(row.int("reviews"))", "correct": "\(row.int("correct"))",
                                         "streak": "\(row.int("streak"))"]))
                                .font(.footnote).foregroundStyle(.secondary)
                            Text(uiFormat(state, "nextDueTemplate", fallback: "Следующее: {date}",
                                values: ["date": formattedDate(row.int64("due"))]))
                                .font(.footnote).foregroundStyle(.secondary)
                        }
                    }
                }
            }
            Section(uiText(model.state, "dataSection", fallback: "Данные")) {
                Button(uiText(model.state, "exportProgressJson", fallback: "Экспортировать JSON")) { model.send("export") }
                Button(uiText(model.state, "importProgressJson", fallback: "Импортировать JSON")) { importing = true }
                Button(uiText(model.state, "uiResetProgress", fallback: "Сбросить прогресс"), role: .destructive) { model.send("reset") }
            }
        }
        .navigationTitle(uiText(model.state, "uiProgressTab", fallback: "Прогресс"))
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
            Section(uiText(model.state, "vocabularyData", fallback: "Данные словаря")) {
                Button(uiText(model.state, "importVocabularyJson", fallback: "Импортировать словарь JSON")) { importing = true }
                Button(uiText(model.state, "exportVocabularyJson", fallback: "Экспортировать словарь JSON")) {
                    exportFile = model.exportVocabulary()
                    exporting = exportFile != nil
                }
                .disabled(state.string("loadStatus") == "Loading")
            }
            if state.string("loadStatus") != "Ready" {
                Section(uiText(model.state, "statusSection", fallback: "Состояние")) {
                    Text(state.string("loadStatus") == "RecoveryRequired"
                         ? uiText(model.state, "vocabularyRecoveryAdvice", fallback: "Экспортируйте исходный JSON и импортируйте исправленный словарь.")
                         : uiText(model.state, "vocabularyUnavailable", fallback: "Словарь пока недоступен"))
                }
            } else {
                Section(uiText(model.state, "directionSection", fallback: "Направление")) {
                    // EnRuAcceptance-2026-09-28.md §7 item 4: was a hardcoded `"ru-pl"/"pl-ru"`
                    // pair. Tags/labels read `state`'s own `target`/`native` (this bridge's own
                    // snapshot, `IosVocabularySession`'s — see its own comment), never
                    // `model.preferences`: that Settings-owned copy can briefly lag behind (or get
                    // self-corrected after) a pack switch this screen's own `state` already
                    // reflects, which would tag a segment with a pair `state.string("direction")`
                    // never actually carries — reproduced live, both segments then show unselected.
                    // pl-ru keeps the exact same 2 tags/labels it always had.
                    Picker(uiText(model.state, "studyDirection", fallback: "Учить"), selection: Binding(
                        get: { state.string("direction") },
                        set: { model.sendVocabulary("direction", $0) }
                    )) {
                        let target = state.string("target"), native = state.string("native")
                        Text(courseDirectionLabel(from: native, to: target, state: model.state)).tag("\(native)-\(target)")
                        Text(courseDirectionLabel(from: target, to: native, state: model.state)).tag("\(target)-\(native)")
                    }
                    .pickerStyle(.segmented)
                }
                Section(uiText(model.state, "vocabularyCard", fallback: "Карточка")) {
                    if state.string("currentId").isEmpty {
                        Text(state.int("selectedCount") == 0 ? uiText(model.state, "selectWords", fallback: "Выберите слова для тренировки") : uiText(model.state, "allReviewed", fallback: "На сейчас всё повторено"))
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
                Section(uiFormat(model.state, "myVocabularyTemplate", fallback: "Мой словарь · {count}",
                    values: ["count": "\(state.int("selectedCount"))"])) {
                    Picker(uiText(model.state, "vocabularyFilter", fallback: "Подборка"), selection: Binding(
                        get: { state.string("filter") },
                        set: { model.sendVocabulary("filter", $0) }
                    )) {
                        Text("A1").tag("A1"); Text("A2").tag("A2"); Text("B1").tag("B1")
                        Text(uiText(model.state, "vocabularyTop100", fallback: "Топ 100")).tag("100")
                        Text(uiText(model.state, "vocabularyTop500", fallback: "Топ 500")).tag("500")
                        Text(uiText(model.state, "vocabularyTop1000", fallback: "Топ 1000")).tag("1000")
                        Text(uiText(model.state, "myWords", fallback: "Мои слова")).tag("mine")
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
                    Button(uiText(model.state, "addOwnWord", fallback: "Добавить своё слово")) { editing = [:] }
                    ForEach(Array(state.rows("entries").enumerated()), id: \.offset) { _, entry in
                        HStack {
                            Button {
                                model.sendVocabulary(entry.bool("selected") ? "deselect" : "select", entry.string("id"))
                            } label: {
                                Image(systemName: entry.bool("selected") ? "checkmark.circle.fill" : "circle")
                                    .frame(minWidth: 44, minHeight: 44)
                                    .contentShape(Rectangle())
                            }
                            .disabled(!entry.bool("available") || state.bool("busy"))
                            .accessibilityLabel("\(entry.bool("selected") ? uiText(model.state, "remove", fallback: "Убрать") : uiText(model.state, "add", fallback: "Добавить")) \(entry.string("lemma"))")
                            .accessibilityIdentifier("vocabularySelect-\(entry.string("id"))")
                            VStack(alignment: .leading) {
                                Text(entry.string("lemma"))
                                Text(entry.bool("available") ? entry.string("translation") : state.string("unavailableLabel"))
                                    .font(.caption).foregroundStyle(.secondary)
                            }
                            Spacer()
                            if entry.bool("custom") {
                                Button(uiText(model.state, "edit", fallback: "Изменить")) { editing = entry }
                                    .font(.caption)
                            }
                        }
                    }
                }
            }
        }
        .navigationTitle(uiText(model.state, "vocabularyTab", fallback: "Слова"))
        .navigationBarTitleDisplayMode(.inline)
        .sheet(item: Binding(
            get: { editing.map(VocabularyEdit.init) },
            set: { editing = $0?.word }
        )) { item in
            VocabularyEditor(model: model, initial: item.word, onClose: { editing = nil },
                onDelete: { deletingId = $0; editing = nil })
        }
        .alert(uiText(model.state, "deleteWordTitle", fallback: "Удалить своё слово?"), isPresented: Binding(
            get: { deletingId != nil }, set: { if !$0 { deletingId = nil } }
        )) {
            Button(uiText(model.state, "delete", fallback: "Удалить"), role: .destructive) {
                if let id = deletingId { model.sendVocabulary("delete", id) }
                deletingId = nil
            }
            Button(uiText(model.state, "cancel", fallback: "Отмена"), role: .cancel) { deletingId = nil }
        } message: { Text(uiText(model.state, "deleteWordWarning", fallback: "Слово исчезнет из каталога, история оценок сохранится в JSON.")) }
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
                    TextField(uiText(model.state, "polishWord", fallback: "Польское слово"), text: $lemma)
                    TextField(uiText(model.state, "translation", fallback: "Перевод"), text: $translation)
                    TextField(uiText(model.state, "form", fallback: "Форма"), text: $form)
                    TextField(uiText(model.state, "exampleSentence", fallback: "Пример в предложении"), text: $example)
                    Picker(uiText(model.state, "level", fallback: "Уровень"), selection: $level) {
                        ForEach(["—", "A1", "A2", "B1", "B2", "C1", "C2"], id: \.self) { Text($0).tag($0) }
                    }
                    if !initial.string("id").isEmpty {
                        Button(uiText(model.state, "deleteWord", fallback: "Удалить слово"), role: .destructive) { onDelete(initial.string("id")) }
                    }
                }
            }
            .navigationTitle(initial.string("id").isEmpty ? uiText(model.state, "newWord", fallback: "Новое слово") : uiText(model.state, "editWord", fallback: "Изменить слово"))
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button(uiText(model.state, "cancel", fallback: "Отмена"), action: onClose) }
                ToolbarItem(placement: .confirmationAction) {
                    Button(uiText(model.state, "save", fallback: "Сохранить")) {
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

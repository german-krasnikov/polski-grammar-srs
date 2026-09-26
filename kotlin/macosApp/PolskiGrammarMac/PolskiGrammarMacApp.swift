import SwiftUI
import AppKit
import UniformTypeIdentifiers
import PolskiShared

struct TrainingSnapshot: Decodable {
    struct HighlightPart: Decodable {
        let text: String
        let changed: Bool
    }
    struct FormChange: Decodable {
        let from: String
        let to: String
        let reason: String
        let toParts: [HighlightPart]
    }
    struct Exercise: Decodable {
        let id: String
        let source: String
        let sourceParts: [HighlightPart]
        let prompt: String?
        let expected: String?
        let expectedParts: [HighlightPart]?
        let changes: [FormChange]?
        let formula: String?
        let explanation: String?
        let methodFeedback: String?
        let frozenAnswer: String?
        /// D3's rating-button interval preview (`MacSnapshot.kt`'s `intervals`), keyed by
        /// `Rating.name` — only `Again`/`Good` are ever shown (see `RatingIntervals`).
        let intervals: RatingIntervals?
    }
    struct Matrix: Decodable {
        struct ContrastPair: Decodable {
            let from: String
            let to: String
            let beforeParts: [HighlightPart]
            let afterParts: [HighlightPart]
        }
        struct CaseRow: Decodable {
            let title: String
            let question: String
            let trigger: String
            let phrasePair: ContrastPair
            let sentencePair: ContrastPair
        }
        struct SystemCard: Decodable { let id: String; let title: String; let explanation: String; let example: String }
        let section: String
        let matrixIntroduction: String
        let pipelineTitle: String
        let pipelineSummary: String
        let systemCards: [SystemCard]
        let cases: [CaseRow]
    }
    struct Skill: Decodable { let id: String; let title: String; let group: String; let reviews: Int; let correct: Int; let due: Int64 }
    /// M12: mirrors `UiEffect` — a host must claim each id once and acknowledge it so it leaves
    /// `pendingEffects`, or the array grows without bound and is re-sent in every snapshot.
    struct Effect: Decodable {
        let id: Int64
        let kind: String
        let exerciseId: String?
        let filename: String?
        let json: String?
        let prompt: String?
    }
    let schemaVersion: Int
    let loadStatus: String
    let tab: String
    let phase: String
    let answerMode: String
    let explanationMethod: String
    let draft: String
    let introPending: Bool
    let dueCount: Int
    let todayCount: Int
    let revision: Int64
    let savedRevision: Int64
    let totalReviews: Int
    let error: String?
    let now: Int64?
    let exercise: Exercise?
    let matrix: Matrix
    let progress: [Skill]
    let effects: [Effect]
}

/// D3's rating-button interval preview, shared by [TrainingSnapshot.Exercise] and
/// [VocabularySnapshot] — only `Again`/`Good` are ever surfaced as rating choices.
struct RatingIntervals: Decodable {
    let again: Int64?
    let good: Int64?
    enum CodingKeys: String, CodingKey { case again = "Again"; case good = "Good" }
}

struct VocabularySnapshot: Decodable {
    struct Item: Decodable { let id: String; let lemma: String; let translation: String; let form: String; let example: String; let level: String }
    struct Entry: Decodable { let id: String; let lemma: String; let translation: String; let level: String; let selected: Bool; let available: Bool }
    let loadStatus: String
    let direction: String
    let filter: String
    let currentId: String?
    let current: Item?
    let entries: [Entry]
    let instructions: String
    let coverage: String
    let revealed: Bool
    let typed: Bool
    let draft: String
    let busy: Bool
    let error: String?
    let selectedCount: Int
    let now: Int64?
    let intervals: RatingIntervals?
}

struct PreferencesSnapshot: Decodable {
    let schemaVersion: Int
    let status: String
    let method: String?
    let answerMode: String?
    let appearance: String?
    let motion: String?
    let error: String?
}

enum DocumentKind: String, CaseIterable, Identifiable {
    case progress, vocabulary, preferences
    var id: String { rawValue }
    var title: String {
        switch self { case .progress: "Прогресс"; case .vocabulary: "Словарь"; case .preferences: "Настройки" }
    }
    var filename: String { "\(rawValue)-\(self == .preferences ? "v2" : "v1").json" }
}

/// C1: shown above every tab whenever `TrainingSnapshot.error` is set (e.g. a progress save
/// failure), regardless of the selected tab, with the export action always reachable — mirrors
/// `PolskiGrammarApp.swift`'s `ErrorBanner`.
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

struct JSONDocument: FileDocument {
    static let readableContentTypes: [UTType] = [.json]
    var text: String
    init(text: String) { self.text = text }
    init(configuration: ReadConfiguration) throws {
        guard let data = configuration.file.regularFileContents,
              let text = String(data: data, encoding: .utf8) else { throw CocoaError(.fileReadCorruptFile) }
        self.text = text
    }
    func fileWrapper(configuration: WriteConfiguration) throws -> FileWrapper {
        FileWrapper(regularFileWithContents: Data(text.utf8))
    }
}

/// A one-shot Rive rating cue (`FlipCardRivePlan.md` FC-01/FC-17/FC-20). `id` is a monotonic
/// counter, not the rating name alone, so `.onChange(of:)` fires again even when the same rating
/// repeats back to back (e.g. two "Good" ratings in a row) rather than being coalesced as an
/// unchanged value. Mirrors `PolskiGrammarApp.swift`'s `CardEffectEvent`.
struct CardEffectEvent: Equatable {
    let id: Int
    let name: String
}

@MainActor
final class MacModel: ObservableObject {
    @Published private(set) var training: TrainingSnapshot?
    @Published private(set) var vocabulary: VocabularySnapshot?
    @Published private(set) var preferences: PreferencesSnapshot?
    @Published var error: String?
    @Published var resetEffectId: Int64?
    @Published var exportEffectId: Int64?
    @Published var exportDocument: JSONDocument?
    @Published var cardEffect: CardEffectEvent?
    @Published var selectedTab = "Training" {
        didSet { if oldValue != selectedTab { trainingSession.dispatch(command: "tab", value: selectedTab) } }
    }
    private let trainingSession: MacSession
    private let vocabularySession: MacVocabularySession
    private let preferencesSession: MacPreferencesSession
    private var claimedEffects = Set<Int64>()
    private var vocabularyRefreshTimer: Timer?
    private var activationObserver: NSObjectProtocol?
    private var effectCounter = 0

    init() {
        let root = ProcessInfo.processInfo.environment["POLSKI_MAC_DATA_DIR"]
            ?? FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
                .appendingPathComponent("Polski Grammar Matrix", isDirectory: true).path
        trainingSession = MacSession(directory: root)
        vocabularySession = MacVocabularySession(directory: root)
        preferencesSession = MacPreferencesSession(directory: root)
        trainingSession.onState = { [weak self] raw in Task { @MainActor in self?.consumeTraining(raw) } }
        // FC-01/17/20: fired once per accepted rating, never stored in `training` — a decorative
        // cue for RiveEffectOverlay, not domain data. `cardEffectFor`'s Again/Remembered mapping is
        // computed in MacSession (Kotlin), the exact point where AppAction.Rate is dispatched, so
        // this bridge only ever forwards a plain effect name string.
        trainingSession.onEffect = { [weak self] name in
            Task { @MainActor in
                guard let self else { return }
                self.effectCounter += 1
                self.cardEffect = CardEffectEvent(id: self.effectCounter, name: name)
            }
        }
        vocabularySession.onState = { [weak self] raw in Task { @MainActor in self?.consumeVocabulary(raw) } }
        // D3: same one-shot Rive cue as training's own `trainingSession.onEffect` above, sharing
        // `cardEffect`/`effectCounter` — the two cards are never on screen at once (tab-switched),
        // so one decorative overlay slot covers both hosts without a second published property.
        vocabularySession.onEffect = { [weak self] name in
            Task { @MainActor in
                guard let self else { return }
                self.effectCounter += 1
                self.cardEffect = CardEffectEvent(id: self.effectCounter, name: name)
            }
        }
        preferencesSession.onState = { [weak self] raw in Task { @MainActor in self?.consumePreferences(raw) } }
        consumeTraining(trainingSession.currentSnapshot())
        consumeVocabulary(vocabularySession.currentSnapshot())
        consumePreferences(preferencesSession.currentSnapshot())
        // M4: due status only changes when time passes; without a periodic nudge a word rated
        // Again stays hidden ("На сейчас всё повторено") until some other change happens to
        // trigger a snapshot. Mirrors the 30s tick `PolskiGrammarApp.swift` uses on iOS.
        vocabularyRefreshTimer = Timer.scheduledTimer(withTimeInterval: 30, repeats: true) { [weak self] _ in
            Task { @MainActor in self?.vocab("refresh") }
        }
        activationObserver = NotificationCenter.default.addObserver(
            forName: NSApplication.didBecomeActiveNotification, object: nil, queue: .main
        ) { [weak self] _ in Task { @MainActor in self?.vocab("refresh") } }
    }
    private func decode<T: Decodable>(_ type: T.Type, _ raw: String) -> T? {
        do { return try JSONDecoder().decode(type, from: Data(raw.utf8)) }
        catch { self.error = "Не удалось прочитать состояние: \(error.localizedDescription)"; return nil }
    }
    private func consumeTraining(_ raw: String) {
        if let next = decode(TrainingSnapshot.self, raw), next.schemaVersion == 1 {
            training = next
            if next.tab != selectedTab { selectedTab = next.tab }
            handleEffects(next.effects)
        }
    }
    /// M12: claims each effect id once so it is acknowledged and leaves `pendingEffects` instead
    /// of growing forever. `reset` opens the confirmation alert; `export` opens the save panel;
    /// every other kind (currently only `focus`) is acknowledged Skipped — macOS does not move
    /// VoiceOver focus for a reveal, matching the contract's fallback for a host that does not act.
    private func handleEffects(_ effects: [TrainingSnapshot.Effect]) {
        for effect in effects {
            guard claimedEffects.insert(effect.id).inserted else { continue }
            switch effect.kind {
            case "reset": resetEffectId = effect.id
            case "export":
                exportDocument = JSONDocument(text: effect.json ?? "")
                exportEffectId = effect.id
            default: trainingSession.acknowledgeEffect(id: effect.id, outcome: "skipped")
            }
        }
    }
    func decideReset(_ confirmed: Bool) {
        guard let id = resetEffectId else { return }
        resetEffectId = nil
        send("resetDecision", "\(id)|\(confirmed)")
    }
    func finishExport(_ result: Result<URL, Error>) {
        guard let id = exportEffectId else { return }
        exportEffectId = nil
        exportDocument = nil
        switch result {
        case .success: trainingSession.acknowledgeEffect(id: id, outcome: "completed")
        case .failure(let failure):
            trainingSession.acknowledgeEffect(id: id, outcome: "skipped")
            error = failure.localizedDescription
        }
    }
    private func consumeVocabulary(_ raw: String) { vocabulary = decode(VocabularySnapshot.self, raw) }
    private func consumePreferences(_ raw: String) {
        preferences = decode(PreferencesSnapshot.self, raw)
        if let method = preferences?.method { trainingSession.dispatch(command: "method", value: method) }
        if let mode = preferences?.answerMode { trainingSession.dispatch(command: "answerMode", value: mode) }
    }
    func send(_ command: String, _ value: String = "") { trainingSession.dispatch(command: command, value: value) }
    func vocab(_ command: String, _ value: String = "") { vocabularySession.dispatch(command: command, value: value) }
    func preference(_ key: String, _ value: String) {
        if let failure = preferencesSession.set(field: key, value: value) { error = failure }
    }
    func export(_ kind: DocumentKind) -> String? {
        switch kind {
        case .progress: trainingSession.exportJson()
        case .vocabulary: vocabularySession.exportJson()
        case .preferences: preferencesSession.exportJson()
        }
    }
    func importJSON(_ kind: DocumentKind, _ raw: String) {
        switch kind {
        case .progress: trainingSession.importJson(raw: raw) { [weak self] failure in
            Task { @MainActor in
                guard let self else { return }
                // A successful import replaces the store, whose effect id counter restarts at 1;
                // without clearing claimedEffects a reused id would look already-handled and its
                // effect would never surface (M12).
                if failure == nil {
                    self.claimedEffects.removeAll()
                    self.resetEffectId = nil
                    self.exportEffectId = nil
                    self.exportDocument = nil
                }
                self.error = failure
            }
        }
        case .vocabulary: vocabularySession.importJson(raw: raw) { [weak self] accepted in
            if !accepted.boolValue { Task { @MainActor in self?.error = "Словарь не принят: проверьте версию и содержание файла" } }
        }
        case .preferences: error = preferencesSession.importJson(raw: raw)
        }
    }
    func recovery(_ kind: DocumentKind) -> String? {
        switch kind {
        case .progress: trainingSession.recoveryRaw()
        case .vocabulary: vocabularySession.exportJson()
        case .preferences: preferencesSession.recoveryRaw()
        }
    }
    func close() {
        vocabularyRefreshTimer?.invalidate()
        if let activationObserver { NotificationCenter.default.removeObserver(activationObserver) }
        trainingSession.onState = nil
        trainingSession.onEffect = nil
        vocabularySession.onState = nil
        preferencesSession.onState = nil
        trainingSession.close()
        vocabularySession.close()
    }
    deinit {
        vocabularyRefreshTimer?.invalidate()
        if let activationObserver { NotificationCenter.default.removeObserver(activationObserver) }
        trainingSession.close(); vocabularySession.close()
    }
}

@main
struct PolskiGrammarMacApp: App {
    @StateObject private var model = MacModel()
    var body: some Scene {
        WindowGroup { MacRootView(model: model) }
            .defaultSize(width: 1120, height: 760)
        Settings { MacSettingsView(model: model).frame(width: 480).padding(20) }
    }
}

private struct MacRootView: View {
    @ObservedObject var model: MacModel
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    // D4: which way the detail pane slides, by sidebar-section order — a lower-indexed tab
    // (e.g. "Training") slides in from the leading edge, a higher-indexed one (e.g. "Progress")
    // from the trailing edge, mirroring the web reference's phone-like pager direction
    // (`TrainingWebApp.kt`'s route-order `nav-indicator`) adapted to a sidebar-driven detail pane.
    @State private var slideForward = true
    private let tabs: [(String, String, String)] = [
        ("Training", "Карточки", "rectangle.on.rectangle"),
        ("Matrix", "Матрица", "tablecells"),
        ("Vocabulary", "Словарь", "text.book.closed"),
        ("Progress", "Прогресс", "chart.bar")
    ]
    /// D5: same system-Reduce-Motion-or-app-Motion.Reduced gate the cards already use
    /// (`TrainingView.cardMotionReduced`), so the tab transition snaps together with everything
    /// else it gates.
    private var tabMotionReduced: Bool { reduceMotion || model.preferences?.motion == "Reduced" }
    private func tabIndex(_ tab: String) -> Int { tabs.firstIndex(where: { $0.0 == tab }) ?? 0 }
    private var tabTransition: AnyTransition {
        .asymmetric(
            insertion: .move(edge: slideForward ? .trailing : .leading).combined(with: .opacity),
            removal: .move(edge: slideForward ? .leading : .trailing).combined(with: .opacity)
        )
    }

    var body: some View {
        VStack(spacing: 0) {
            // C1: state.error (e.g. a progress save failure) must stay visible above every tab,
            // not only on Progress, with the export action reachable so no reviewed progress
            // is lost. Mirrors PolskiGrammarApp.swift's global ErrorBanner.
            if let error = model.training?.error {
                ErrorBanner(message: error) { model.send("export") }
            }
            NavigationSplitView {
                List(selection: $model.selectedTab) {
                    ForEach(tabs, id: \.0) { tab in
                        Label(tab.1, systemImage: tab.2)
                            .tag(tab.0)
                    }
                }
                .navigationTitle("Polski Grammar Matrix")
                .frame(minWidth: 165)
            } detail: {
                // D4: `.id(selectedTab)` + an asymmetric `.move`+`.opacity` transition, keyed to
                // the sidebar section order via `slideForward` (set by the `onChange` below before
                // the new content mounts) — a directional slide/crossfade instead of the old plain
                // opacity-only `.animation(value:)` crossfade. ~300ms with a Material-style
                // "emphasized" decelerate curve (`timingCurve(0.2, 0, 0, 1, ...)`), snapped under
                // `tabMotionReduced`.
                ZStack {
                    detailContent(model.selectedTab)
                        .id(model.selectedTab)
                        .transition(tabTransition)
                }
                .frame(minWidth: 320, minHeight: 420)
                .toolbar { SettingsLink { Label("Настройки", systemImage: "gearshape") } }
                .animation(tabMotionReduced ? nil : .timingCurve(0.2, 0, 0, 1, duration: 0.3), value: model.selectedTab)
                .onChange(of: model.selectedTab) { oldValue, newValue in
                    slideForward = tabIndex(newValue) >= tabIndex(oldValue)
                }
            }
        }
        .preferredColorScheme(colorScheme)
        .alert("Ошибка", isPresented: Binding(get: { model.error != nil }, set: { if !$0 { model.error = nil } })) {
            Button("ОК") { model.error = nil }
        } message: { Text(model.error ?? "") }
        // M12: ConfirmReset stays pending until the user decides; cancelling the alert (e.g. the
        // system dismiss gesture) must still resolve it as declined so it leaves pendingEffects.
        .alert("Сбросить весь прогресс?", isPresented: Binding(
            get: { model.resetEffectId != nil },
            set: { if !$0 && model.resetEffectId != nil { model.decideReset(false) } }
        )) {
            Button("Сбросить", role: .destructive) { model.decideReset(true) }
            Button("Отмена", role: .cancel) { model.decideReset(false) }
        } message: { Text("Все оценки и расписание повторений будут удалены.") }
        // M12: DownloadJson from the error banner's export action opens the save panel; success
        // or failure (including a user cancel) both acknowledge the effect.
        .fileExporter(isPresented: Binding(
            get: { model.exportEffectId != nil },
            set: { if !$0 && model.exportEffectId != nil { model.finishExport(.failure(CocoaError(.userCancelled))) } }
        ), document: model.exportDocument, contentType: .json, defaultFilename: "polski-srs-progress") { result in
            model.finishExport(result)
        }
    }
    private var colorScheme: ColorScheme? {
        switch model.preferences?.appearance {
        case "Light": .light
        case "Dark": .dark
        default: nil
        }
    }
    @ViewBuilder
    private func detailContent(_ tab: String) -> some View {
        switch tab {
        case "Matrix": MatrixView(model: model)
        case "Vocabulary": VocabularyView(model: model)
        case "Progress": ProgressViewNative(model: model)
        default: TrainingView(model: model)
        }
    }
}

private struct TrainingView: View {
    @ObservedObject var model: MacModel
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    // FlipCardRivePlan.md §5, variant B (flip only, Rive off) on top of the real Motion gate — a
    // debug-only measurement toggle, never user-facing; see MacSettingsView's hidden long-press.
    #if DEBUG
    @AppStorage("polski.debug.riveDisabled") private var riveDisabledForMeasurement = false
    #endif

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                header
                if let state = model.training {
                    if state.loadStatus != "Ready" {
                        recovery(state)
                    } else if let exercise = state.exercise {
                        // D3: attached at the call site (not inside `exerciseCard`/`MacFlashCardView`
                        // themselves) so the transform covers `contentCard`'s own background+border
                        // too — the *whole* panel follows the finger, matching the vocabulary card's
                        // already-whole-panel flip.
                        exerciseCard(state, exercise)
                            .swipeToRate(enabled: state.phase == "Revealed", reduceMotion: cardMotionReduced) { remembered in
                                model.send("rate", "\(exercise.id)|\(remembered ? "Good" : "Again")")
                            }
                    } else {
                        completion(state)
                    }
                    footer(state)
                } else {
                    SwiftUI.ProgressView("Загружаем прогресс")
                        .frame(maxWidth: .infinity, minHeight: 300)
                }
            }
            .frame(maxWidth: 680, alignment: .leading)
            .frame(maxWidth: .infinity)
            .padding(.horizontal, 32)
            .padding(.vertical, 38)
        }

        .navigationTitle("Занятие")
        .animation(cardMotionReduced ? nil : .easeInOut(duration: 0.12), value: model.training?.phase)
    }

    /// FC-09/12/14/20's shared reduced-motion gate: system Reduce Motion or the app's own
    /// `Motion.Reduced` setting, the same pair already used by `.animation(...)` above — both the
    /// card flip and the Rive overlay must snap/skip together with everything else this gates.
    private var cardMotionReduced: Bool { reduceMotion || model.preferences?.motion == "Reduced" }

    /// FC-20's Rive gate, plus the debug-only variant-B override (§5): the flip itself keeps
    /// animating in variant B — only the Rive trigger is suppressed — so this is deliberately
    /// separate from `cardMotionReduced`, which the flip's own animation uses unchanged.
    private var riveEffectsSuppressed: Bool {
        #if DEBUG
        return cardMotionReduced || riveDisabledForMeasurement
        #else
        return cardMotionReduced
        #endif
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text("Практика").font(.largeTitle.weight(.semibold))
            Text("Польский ↔ русский").font(.subheadline).foregroundStyle(.secondary)
        }
        .padding(16)
        .accessibilityElement(children: .combine)
    }

    private func recovery(_ state: TrainingSnapshot) -> some View {
        contentCard {
            VStack(alignment: .leading, spacing: 12) {
                Label("Прогресс требует восстановления", systemImage: "exclamationmark.triangle")
                    .font(.title3.weight(.semibold))
                Text("Исходный JSON сохранён. Откройте Прогресс, чтобы экспортировать его или импортировать проверенную копию.")
                Button("Открыть Прогресс") { model.selectedTab = "Progress" }
            }
        }
    }

    /// FC-06/12/13/14/17/20: the flip card (native `rotation3DEffect`, relocated swipe rating) and
    /// its decorative Rive overlay both extracted into their own files — see `MacFlashCardView.swift`
    /// (flip mechanics/gesture) and `RiveEffectOverlay.swift` (effects). `contentCard` is already a
    /// real standalone card surface on this host, so no `Form`/`List` row-splitting workaround is
    /// needed here, unlike iOS's `FlashCardView`.
    private func exerciseCard(_ state: TrainingSnapshot, _ exercise: TrainingSnapshot.Exercise) -> some View {
        contentCard {
            ZStack {
                MacFlashCardView(model: model, state: state, exercise: exercise, reduceMotion: cardMotionReduced)
                RiveEffectOverlay(effect: model.cardEffect, reduceMotion: riveEffectsSuppressed)
            }
        }
    }

    private func completion(_ state: TrainingSnapshot) -> some View {
        contentCard {
            VStack(alignment: .leading, spacing: 16) {
                Image(systemName: "checkmark.circle.fill")
                    .font(.largeTitle).foregroundStyle(.tint)
                Text(state.phase == "ChainComplete" ? "Цепочка завершена" : "Заданий сейчас нет")
                    .font(.title2.weight(.semibold))
                HStack {
                    Button("Следующая цепочка") { model.send("chain") }
                        .buttonStyle(.borderedProminent)
                    Button("Повторения") { model.send("schedule") }
                }.controlSize(.large)
            }
        }
    }

    private func footer(_ state: TrainingSnapshot) -> some View {
        HStack(spacing: 8) {
            Text("Сегодня \(state.todayCount)")
            Text("·")
            Text("К повторению \(state.dueCount)")
            Text("·")
            Text("Всего \(state.totalReviews)")
        }
        .font(.footnote)
        .foregroundStyle(.secondary)
        .accessibilityElement(children: .combine)
        .padding(.horizontal, 4)
    }

    private func contentCard<Content: View>(@ViewBuilder content: () -> Content) -> some View {
        content()
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(24)
            .background(Color(nsColor: .controlBackgroundColor), in: RoundedRectangle(cornerRadius: 18, style: .continuous))
            .overlay(RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(Color.primary.opacity(0.09), lineWidth: 1))
            .frame(maxWidth: .infinity, alignment: .leading)
    }

}

private struct MatrixView: View {
    @ObservedObject var model: MacModel
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                if let matrix = model.training?.matrix {
                    Text(matrix.matrixIntroduction).font(.title3).textSelection(.enabled)
                    Picker("Раздел", selection: Binding(get: { matrix.section }, set: { model.send("matrixSection", $0) })) {
                        Text("Карта").tag("Map")
                        Text("Падежи").tag("Cases")
                        Text("Глаголы").tag("Verbs")
                        Text("Местоимения").tag("Pronouns")
                    }.pickerStyle(.segmented)
                        .padding(3)

                    if matrix.section == "Cases" {
                        ForEach(matrix.cases, id: \.title) { row in
                            GroupBox(row.title) {
                                VStack(alignment: .leading, spacing: 9) {
                                    Text("\(row.question) · \(row.trigger)").foregroundStyle(.secondary)
                                    contrastPair(row.phrasePair).font(.headline)
                                    contrastPair(row.sentencePair)
                                }.frame(maxWidth: .infinity, alignment: .leading)
                            }
                        }
                    } else {
                        Text(matrix.pipelineTitle).font(.headline)
                        Text(matrix.pipelineSummary)
                        ForEach(matrix.systemCards, id: \.id) { card in
                            GroupBox(card.title) {
                                VStack(alignment: .leading) {
                                    Text(card.explanation)
                                    Text(card.example).font(.headline)
                                }.frame(maxWidth: .infinity, alignment: .leading)
                            }
                        }
                    }
                }
            }
            .frame(maxWidth: 860, alignment: .leading).padding(28)
        }
        .navigationTitle("Матрица")
    }

    private func contrastPair(_ pair: TrainingSnapshot.Matrix.ContrastPair) -> some View {
        VStack(alignment: .leading, spacing: 3) {
            highlightedText(pair.beforeParts, before: true)
                .accessibilityLabel("Было: \(pair.from)")
            (Text("→ ") + highlightedText(pair.afterParts, before: false))
                .accessibilityLabel("Стало: \(pair.to)")
        }
        .textSelection(.enabled)
    }

}

func highlightedText(_ parts: [TrainingSnapshot.HighlightPart], before: Bool) -> Text {
    parts.reduce(Text("")) { result, part in
        let fragment = Text(part.text)
        return result + (part.changed
            ? fragment.bold().foregroundColor(Color(nsColor: before ? .systemRed : .systemOrange)).underline()
            : fragment)
    }
}

private struct VocabularyView: View {
    @ObservedObject var model: MacModel
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    /// Same gate as `TrainingView.cardMotionReduced` (D5): system Reduce Motion or the app's own
    /// `Motion.Reduced` setting, so this card's flip snaps together with everything else it gates.
    private var cardMotionReduced: Bool { reduceMotion || model.preferences?.motion == "Reduced" }
    // D4: the animated collapsible for the entry catalog — mirrors the web reference's
    // "Скрыть/Открыть каталог" `.collapsible` (`VocabularyWeb.kt`'s `renderCatalog`), a real
    // SwiftUI conditional mount (not a height-0 CSS-grid trick, unneeded here) driven by an
    // explicit `withAnimation` in the toggle button so it animates height+opacity together.
    @State private var catalogVisible = true

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                if let state = model.vocabulary {
                    Text(state.instructions).foregroundStyle(.secondary)
                    if let error = state.error { Text(error).foregroundStyle(.red) }
                    Picker("Список", selection: Binding(get: { state.filter }, set: { model.vocab("filter", $0) })) {
                        ForEach(["A1", "A2", "B1", "100", "500", "1000", "mine"], id: \.self) { value in Text(value).tag(value) }
                    }.pickerStyle(.segmented)
                        .padding(3)

                    Text(state.coverage).foregroundStyle(.secondary)
                    if let item = state.current {
                        ZStack {
                            MacVocabularyCardView(model: model, state: state, item: item, reduceMotion: cardMotionReduced)
                            RiveEffectOverlay(effect: model.cardEffect, reduceMotion: cardMotionReduced)
                        }
                    }
                    Button(catalogVisible ? "Скрыть каталог" : "Открыть каталог") {
                        withAnimation(cardMotionReduced ? nil : .timingCurve(0.2, 0, 0, 1, duration: 0.3)) {
                            catalogVisible.toggle()
                        }
                    }
                    .accessibilityIdentifier("vocabularyCatalogToggle")
                    if catalogVisible {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Выбрано: \(state.selectedCount)")
                            ForEach(state.entries, id: \.lemma) { entry in
                                HStack {
                                    VStack(alignment: .leading) {
                                        Text(entry.lemma).font(.headline)
                                        Text(entry.translation).foregroundStyle(.secondary)
                                    }
                                    Spacer()
                                    if entry.available {
                                        Button(entry.selected ? "Убрать" : "Добавить") {
                                            model.vocab(entry.selected ? "deselect" : "select", entry.id)
                                        }
                                    } else { Text("Недоступно").foregroundStyle(.secondary) }
                                }
                                .padding(12)

                            }
                        }
                        .transition(.opacity.combined(with: .move(edge: .top)))
                    }
                } else { SwiftUI.ProgressView("Загружаем словарь") }
            }.frame(maxWidth: 760, alignment: .leading).padding(28)
        }.navigationTitle("Словарь")
    }
}

private struct ProgressViewNative: View {
    @ObservedObject var model: MacModel
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                if let state = model.training {
                    Text("Повторений: \(state.totalReviews)").font(.title2.bold())
                    Text("К повторению: \(state.dueCount) · Сегодня: \(state.todayCount)")
                    ForEach(state.progress, id: \.id) { skill in
                        HStack {
                            Text(skill.title)
                            Spacer()
                            Text("\(skill.correct)/\(skill.reviews)")
                        }
                        .padding(12)

                    }
                }
                Divider()
                DataControls(model: model)
                // M12: RequestReset produces a ConfirmReset effect the root alert now handles.
                Button("Сбросить прогресс", role: .destructive) { model.send("reset") }
            }.frame(maxWidth: 760, alignment: .leading).padding(28)
        }.navigationTitle("Прогресс")
    }
}

private struct MacSettingsView: View {
    @ObservedObject var model: MacModel
    @Environment(\.accessibilityReduceMotion) private var systemReduceMotion
    #if DEBUG
    @AppStorage("polski.debug.riveDisabled") private var riveDisabledForMeasurement = false
    #endif
    var body: some View {
        Form {
            if model.preferences?.status == "RecoveryRequired" {
                Text(model.preferences?.error ?? "Настройки требуют восстановления")
                    .foregroundStyle(.red)
            } else {
                Picker("Объяснение", selection: Binding(
                    get: { model.preferences?.method ?? "Logic" },
                    set: { model.preference("method", $0) }
                )) {
                    Text("Логика").tag("Logic")
                    Text("Ситуации").tag("Situations")
                }
                Picker("Внешний вид", selection: Binding(
                    get: { model.preferences?.appearance ?? "System" },
                    set: { model.preference("appearance", $0) }
                )) {
                    Text("Системная").tag("System")
                    Text("Светлая").tag("Light")
                    Text("Тёмная").tag("Dark")
                }
                Picker("Движение", selection: Binding(
                    get: { model.preferences?.motion ?? "System" },
                    set: { model.preference("motion", $0) }
                )) {
                    Text("Системное").tag("System")
                    Text("Сокращённое").tag("Reduced")
                }
                Text(systemReduceMotion ? "Система сокращает движение" : "Системное движение активно")
                    .foregroundStyle(.secondary)
            }
            Text("Напоминания пока недоступны").foregroundStyle(.secondary)
            DataControls(model: model)
            // FlipCardRivePlan.md §5: a debug-only measurement toggle for variant B (native flip,
            // Rive effects off), layered on top of the real Motion.Reduced gate — never shown in a
            // release build, and hidden behind a long-press on this existing footer text even in
            // debug so it never reads as a real user-facing setting (mirrors IosSettingsView).
            Text("Polski Grammar Matrix · польский ↔ русский").foregroundStyle(.secondary)
                #if DEBUG
                .onLongPressGesture { riveDisabledForMeasurement.toggle() }
                #endif
            #if DEBUG
            if riveDisabledForMeasurement {
                Text("Замер: Rive-эффекты отключены (вариант B)")
                    .font(.footnote).foregroundStyle(.orange)
            }
            #endif
        }
        .preferredColorScheme(settingsColorScheme)
    }

    private var settingsColorScheme: ColorScheme? {
        switch model.preferences?.appearance {
        case "Light": .light
        case "Dark": .dark
        default: nil
        }
    }
}

private struct DataControls: View {
    @ObservedObject var model: MacModel
    @State private var importing: DocumentKind?
    @State private var exporting: DocumentKind?
    @State private var document = JSONDocument(text: "")
    var body: some View {
        GroupBox("Данные JSON") {
            VStack(alignment: .leading, spacing: 12) {
                ForEach(DocumentKind.allCases) { kind in
                    HStack {
                        Text(kind.title).frame(width: 100, alignment: .leading)
                        Button("Импортировать…") { importing = kind }
                        Button("Экспортировать…") {
                            if let raw = model.export(kind) {
                                document = JSONDocument(text: raw)
                                exporting = kind
                            } else { model.error = "Документ ещё не готов к экспорту" }
                        }
                        if let raw = model.recovery(kind), model.training?.loadStatus != "Ready" || model.preferences?.status == "RecoveryRequired" {
                            Button("Экспорт исходного…") {
                                document = JSONDocument(text: raw)
                                exporting = kind
                            }
                        }
                    }
                }
                Text("Документы хранятся отдельно. Импорт проверяет формат и сохраняет резервную копию.")
                    .font(.caption).foregroundStyle(.secondary)
            }.frame(maxWidth: .infinity, alignment: .leading).padding(4)
        }
        .fileImporter(isPresented: Binding(get: { importing != nil }, set: { if !$0 { importing = nil } }), allowedContentTypes: [.json]) { result in
            let kind = importing
            importing = nil
            guard let kind else { return }
            do {
                let url = try result.get()
                let access = url.startAccessingSecurityScopedResource()
                defer { if access { url.stopAccessingSecurityScopedResource() } }
                let data = try Data(contentsOf: url)
                guard let raw = String(data: data, encoding: .utf8) else { throw CocoaError(.fileReadCorruptFile) }
                model.importJSON(kind, raw)
            } catch { model.error = error.localizedDescription }
        }
        .fileExporter(isPresented: Binding(get: { exporting != nil }, set: { if !$0 { exporting = nil } }), document: document, contentType: .json, defaultFilename: exporting?.filename ?? "data.json") { result in
            exporting = nil
            if case let .failure(error) = result { model.error = error.localizedDescription }
        }
    }
}

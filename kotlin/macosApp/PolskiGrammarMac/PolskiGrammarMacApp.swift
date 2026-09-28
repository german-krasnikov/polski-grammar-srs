import SwiftUI
import AppKit
import UniformTypeIdentifiers
import PolskiShared

struct TrainingSnapshot: Decodable {
    struct HighlightPart: Decodable {
        let text: String
        let changed: Bool
        /// "before"/"after", or absent — set only when [StyleSnapshot.kt]'s `endingPartsJson` has
        /// a non-null `EndingPart.side` (W3 correction, EmphasisUXAudit E7/S4): a style block's own
        /// prose can mix a Before-role fragment and an After-role fragment in one `parts` list, so
        /// each part must carry its own role rather than relying on one block-level default. Every
        /// other caller (exercise `sourceParts`/`toParts`/`beforeParts`/`afterParts`) never mixes
        /// roles within a list, so this stays absent there and `highlightedText`'s `before`
        /// parameter is still what renders them.
        let side: String?
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
        /// [example] is the flat `"żona → żonę → żony"` label, kept only for accessibility/VoiceOver
        /// (`Text` cannot describe a `Text`-chain's own accessibility label from its parts); the
        /// visible chain renders from [steps] (`MacSnapshot.kt`'s `card.steps.zipWithNext`), one
        /// [ContrastPair] per arrow, each carrying its own morpheme-level `before`/`after` parts —
        /// C2, EmphasisUXAudit E6.
        struct SystemCard: Decodable { let id: String; let title: String; let explanation: String; let example: String; let steps: [ContrastPair] }
        /// UC-09 part 2/2 (UniversalCorePlan.md §5.3.3, ADR-21): the generic wire shape for one
        /// `MatrixTableViewModel` ([MatrixTableSnapshot.kt]'s `toJson()`) — a row-axis × column-list
        /// table where each cell is either plain text or a "было → стало" [ContrastPair]. One struct
        /// and one renderer (`matrixTableView` below) now covers every table this screen shows,
        /// instead of a bespoke `Decodable`/view pair per section.
        struct Cell: Decodable { let value: String; let contrast: ContrastPair? }
        struct Row: Decodable { let header: String; let cells: [Cell] }
        struct Table: Decodable { let rowHeaderLabel: String; let columnHeaders: [String]; let rows: [Row] }
        let section: String
        let matrixIntroduction: String
        let pipelineTitle: String
        let pipelineSummary: String
        let systemCards: [SystemCard]
        let caseNote: String
        let casesTable: Table
        let comparisonTable: Table
        let verbFutureExplanation: String
        let verbsTable: Table
        /// EN-24 (`EnRuPackPlan.md` §6, macOS slice): the one live English matrix on this host —
        /// same `MatrixTableViewModel` wire shape as every pl table above, read from
        /// `forms.generated.json`(en) through `enMorphology`, not a mock.
        let enVerbsTable: Table
        let enDoSupportTable: Table
        let pronounIntro: String
        let pronounFooter: String
        let possessiveTitle: String
        let personalPronounsTable: Table
        let possessivesTable: Table
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
    /// UC-10 S1: the full 4-value style, read directly (no more Logic/Situations collapse) by
    /// the Settings picker and the training-card quick switch.
    let styleId: String
    /// `null` while there is no current exercise (matches `styleBlocksSnapshot` in `MacSnapshot.kt`).
    /// UC-10 S2: `frontBlocks`/`backBlocks` are always both present regardless of `phase` — the
    /// card keeps its front face mounted after reveal too, so it needs its own Front blocks even
    /// once `phase == "Revealed"` (see `MacFlashCardView`'s D1 expand-reveal).
    struct StyleBlocks: Decodable {
        let effectiveStyleId: String
        let nativeContrastAvailable: Bool
        let frontBlocks: [BlockJSON]
        let backBlocks: [BlockJSON]
        /// EN-21 (`EnRuPackPlan.md` §4.2/§4.3): pair-scoped L1-transfer tips for the active
        /// exercise's skill — deliberately not part of `backBlocks`, since a lifehack shows the
        /// same way for every style (see `MacLifehackView.swift`). Empty when the skill has none.
        let lifehacks: [LifehackJSON]
    }
    let styleBlocks: StyleBlocks?
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

/// UC-10 S2: one entry of `styleBlocks.frontBlocks`/`backBlocks`, mirroring the flat `kind`-tagged
/// shape `StyleSnapshot.kt`'s `blockToJson` writes — no sealed Kotlin type crosses the bridge, this
/// struct just reads `kind` and the fields that kind uses (see `MacStyleBlockView.swift`). `items`
/// is polymorphic on the Kotlin side (`[String]` for `examples`, `[{before,after,reason}]` for
/// `changes`), hence the manual `init(from:)` splitting it into two typed, mutually-exclusive
/// fields instead of one `Decodable`-synthesized property.
struct BlockJSON: Decodable {
    struct TableRow: Decodable { let label: String; let before: [TrainingSnapshot.HighlightPart]; let after: [TrainingSnapshot.HighlightPart] }
    /// [targetParts] highlights [target] (S4/M1, EmphasisUXAudit E7) — [native] stays plain prose,
    /// never highlighted, matching `Block.NativeParallelPair`'s own contract.
    struct NativeParallelPair: Decodable { let native: String; let target: String; let note: String; let matches: Bool; let targetParts: [TrainingSnapshot.HighlightPart] }
    struct ChangeItem: Decodable { let before: [TrainingSnapshot.HighlightPart]; let after: [TrainingSnapshot.HighlightPart]; let reason: String }

    let kind: String
    let text: String?
    let detail: String?
    let caption: String?
    let rows: [TableRow]?
    let pairs: [NativeParallelPair]?
    let collapsedLabel: String?
    let before: [TrainingSnapshot.HighlightPart]?
    let after: [TrainingSnapshot.HighlightPart]?
    let exampleItems: [String]?
    let changeItems: [ChangeItem]?
    /// S4/M1 (EmphasisUXAudit E7): highlights `text` for `formula`/`rule`/`scene`/`whyOnDemand`,
    /// the same explicit-pair parts `Block.kt` already attaches — see `MacStyleBlockView`.
    let parts: [TrainingSnapshot.HighlightPart]?
    /// `examples` only: parallel to `exampleItems` by index (`itemParts[i]` highlights `exampleItems[i]`).
    let itemParts: [[TrainingSnapshot.HighlightPart]]?

    private enum CodingKeys: String, CodingKey {
        case kind, text, detail, caption, rows, pairs, collapsedLabel, before, after, items, parts, itemParts
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        kind = try c.decode(String.self, forKey: .kind)
        text = try c.decodeIfPresent(String.self, forKey: .text)
        detail = try c.decodeIfPresent(String.self, forKey: .detail)
        caption = try c.decodeIfPresent(String.self, forKey: .caption)
        rows = try c.decodeIfPresent([TableRow].self, forKey: .rows)
        pairs = try c.decodeIfPresent([NativeParallelPair].self, forKey: .pairs)
        collapsedLabel = try c.decodeIfPresent(String.self, forKey: .collapsedLabel)
        before = try c.decodeIfPresent([TrainingSnapshot.HighlightPart].self, forKey: .before)
        after = try c.decodeIfPresent([TrainingSnapshot.HighlightPart].self, forKey: .after)
        parts = try c.decodeIfPresent([TrainingSnapshot.HighlightPart].self, forKey: .parts)
        itemParts = try c.decodeIfPresent([[TrainingSnapshot.HighlightPart]].self, forKey: .itemParts)
        if kind == "changes" {
            exampleItems = nil
            changeItems = try c.decodeIfPresent([ChangeItem].self, forKey: .items)
        } else {
            changeItems = nil
            exampleItems = try c.decodeIfPresent([String].self, forKey: .items)
        }
    }
}

/// EN-21: one `MacSnapshot.kt` `styleBlocksSnapshot`'s `lifehacks[]` entry — `status` is always
/// `"editorial"` today (`"community"` reserved, ADR-15); `url` is `nil` for a record with no
/// `source.url` (`courses/pairs/pl-ru/lifehacks.json`'s `case.inst` record, for one real example).
struct LifehackJSON: Decodable { let text: String; let citation: String; let url: String?; let status: String }

struct VocabularySnapshot: Decodable {
    struct Item: Decodable { let id: String; let lemma: String; let translation: String; let form: String; let example: String; let level: String }
    struct Entry: Decodable { let id: String; let lemma: String; let translation: String; let level: String; let selected: Bool; let available: Bool }
    /// EnRuAcceptance-2026-09-28.md §7 item 4: one direction the active pack's own [VocabularySnapshot.direction]
    /// can be set to — built from `polski.vocabulary.studyDirectionOptions`, never a hardcoded
    /// pl-ru 2-case list, so a second pack (en-ru) offers its own real "ru-en"/"en-ru" pair.
    struct DirectionOption: Decodable, Identifiable { let wire: String; let label: String; var id: String { wire } }
    let loadStatus: String
    let direction: String
    /// The active pack's own 2 [DirectionOption]s — the direction picker's real option list.
    let directionOptions: [DirectionOption]
    /// "Вспомни по-…" — which language [direction] asks the learner to produce, computed in
    /// Kotlin so this view never needs its own language-name table.
    let promptCaption: String
    /// Whether [direction] asks for the target word (`item.lemma`) as the revealed answer, rather
    /// than the native one (`item.translation`) — see `MacVocabularyCardView`.
    let recallTarget: Bool
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
    /// UC-10 S1: the 4 style recipes' native-language copy, resolved by `MacPreferencesSession`
    /// (recipe data first, a host-side Russian fallback while CONTENT's authoring is unmerged) —
    /// Settings and the training-card quick switch both render this, never a hardcoded 4-case list.
    struct StyleOption: Decodable, Identifiable {
        let id: String
        let label: String
        let description: String
    }
    /// EN-22: one pack `MacPreferencesSession.selectCoursePack` can actually switch to — pl-ru is
    /// the only real one today (`polski.data.packRegistry`'s own KDoc has why en-ru isn't wired in
    /// yet), so the target/native pickers below render exactly this list, never a hardcoded 2-case one.
    struct PackOption: Decodable, Identifiable {
        let pairId: String
        let target: String
        let native: String
        let targetLabel: String
        let nativeLabel: String
        var id: String { pairId }
    }
    let schemaVersion: Int
    let status: String
    let styles: [StyleOption]
    let packs: [PackOption]
    let target: String?
    let native: String?
    let styleId: String?
    let answerMode: String?
    let appearance: String?
    let motion: String?
    let animationsEnabled: Bool?
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
    // D4: sidebar section order, shared with `MacRootView`'s slide direction — a lower index
    // slides in from the leading edge, a higher one from the trailing edge.
    static let tabOrder = ["Training", "Matrix", "Vocabulary", "Progress"]
    // D4 fix: computed synchronously in `didSet`, not in a view-level `.onChange`. `.onChange`
    // runs after `body` has already rendered for the new `selectedTab`, so a direction flag it
    // writes only takes effect the render AFTER the one it should describe — stale by one step
    // whenever navigation direction reverses. `didSet` runs inside the same synchronous write
    // that changes `selectedTab`, before SwiftUI re-renders, so `slideForward` is always current
    // for every path that sets `selectedTab` (sidebar `List` binding, external state sync in
    // `consumeTraining`, and the "Открыть Прогресс" button).
    @Published var slideForward = true
    @Published var selectedTab = "Training" {
        didSet {
            guard oldValue != selectedTab else { return }
            let order = Self.tabOrder
            slideForward = (order.firstIndex(of: selectedTab) ?? 0) >= (order.firstIndex(of: oldValue) ?? 0)
            trainingSession.dispatch(command: "tab", value: selectedTab)
        }
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
        if let styleId = preferences?.styleId { trainingSession.dispatch(command: "styleId", value: styleId) }
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
    private let tabs: [(String, String, String)] = [
        ("Training", "Карточки", "rectangle.on.rectangle"),
        ("Matrix", "Матрица", "tablecells"),
        ("Vocabulary", "Словарь", "text.book.closed"),
        ("Progress", "Прогресс", "chart.bar")
    ]
    /// D5: same system-Reduce-Motion-or-app-Motion.Reduced gate the cards already use
    /// (`TrainingView.cardMotionReduced`), so the tab transition snaps together with everything
    /// else it gates.
    private var tabMotionReduced: Bool { reduceMotion || model.preferences?.motion == "Reduced" || model.preferences?.animationsEnabled == false }
    private var tabTransition: AnyTransition {
        .asymmetric(
            insertion: .move(edge: model.slideForward ? .trailing : .leading).combined(with: .opacity),
            removal: .move(edge: model.slideForward ? .leading : .trailing).combined(with: .opacity)
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
                // the sidebar section order via `model.slideForward` (computed synchronously in
                // `MacModel.selectedTab`'s `didSet`, so it is already correct for the switch that
                // is about to render — see the fix note there) — a directional slide/crossfade
                // instead of the old plain opacity-only `.animation(value:)` crossfade. ~300ms with
                // a Material-style "emphasized" decelerate curve
                // (`timingCurve(0.2, 0, 0, 1, ...)`), snapped under `tabMotionReduced`.
                ZStack {
                    detailContent(model.selectedTab)
                        .id(model.selectedTab)
                        .transition(tabTransition)
                }
                .frame(minWidth: 320, minHeight: 420)
                .toolbar { SettingsLink { Label("Настройки", systemImage: "gearshape") } }
                .animation(tabMotionReduced ? nil : .timingCurve(0.2, 0, 0, 1, duration: 0.3), value: model.selectedTab)
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
    private var cardMotionReduced: Bool { reduceMotion || model.preferences?.motion == "Reduced" || model.preferences?.animationsEnabled == false }

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

                    switch matrix.section {
                    case "Cases":
                        GroupBox("Все семь падежей на одной группе слов") {
                            matrixTableView(matrix.casesTable)
                            Text(matrix.caseNote).foregroundStyle(.secondary)
                        }
                        GroupBox("Сравнение типов склонения") { matrixTableView(matrix.comparisonTable) }
                    case "Verbs":
                        GroupBox("Лицо × число × время") {
                            matrixTableView(matrix.verbsTable)
                            Text(matrix.verbFutureExplanation).foregroundStyle(.secondary)
                        }
                        GroupBox("English: лицо × время (\"see\")") { matrixTableView(matrix.enVerbsTable) }
                        GroupBox("do-support: вопрос и отрицание") { matrixTableView(matrix.enDoSupportTable) }
                    case "Pronouns":
                        GroupBox("Местоимения") {
                            Text(matrix.pronounIntro)
                            matrixTableView(matrix.personalPronounsTable)
                            Text(matrix.pronounFooter).foregroundStyle(.secondary)
                        }
                        GroupBox(matrix.possessiveTitle) { matrixTableView(matrix.possessivesTable) }
                    default:
                        Text(matrix.pipelineTitle).font(.headline)
                        Text(matrix.pipelineSummary)
                        ForEach(matrix.systemCards, id: \.id) { card in
                            GroupBox(card.title) {
                                VStack(alignment: .leading) {
                                    Text(card.explanation)
                                    systemCardChain(card.steps).font(.headline).textSelection(.enabled)
                                        .accessibilityLabel("\(card.title): \(card.example)")
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

    /// UC-09 part 2/2: the one renderer every "Матрица" table now shares — a header row plus data
    /// rows, each cell either plain text or a [contrastPair]. Column width matches the existing
    /// desktop/web tables' own convention (a narrower leading row-label column, wider data columns).
    private func matrixTableView(_ table: TrainingSnapshot.Matrix.Table) -> some View {
        ScrollView(.horizontal) {
            VStack(alignment: .leading, spacing: 6) {
                HStack(alignment: .top, spacing: 12) {
                    Text(table.rowHeaderLabel).bold().frame(width: 170, alignment: .leading)
                    ForEach(table.columnHeaders, id: \.self) { header in
                        Text(header).bold().frame(width: 200, alignment: .leading)
                    }
                }
                Divider()
                ForEach(Array(table.rows.enumerated()), id: \.offset) { _, row in
                    HStack(alignment: .top, spacing: 12) {
                        Text(row.header).frame(width: 170, alignment: .leading)
                        ForEach(Array(row.cells.enumerated()), id: \.offset) { _, cell in
                            Group {
                                if let contrast = cell.contrast { contrastPair(contrast) } else { Text(cell.value) }
                            }.frame(width: 200, alignment: .leading)
                        }
                    }
                    Divider()
                }
            }
        }
        .textSelection(.enabled)
    }

}

/// C2 (EmphasisUXAudit E6): a system-map card's chain (`żona → żonę → żony`) is one `before` (the
/// starting form) followed by every step's `after` — each step's own diff still highlights only
/// the morpheme that changed at that step, never the whole word. System cards live in the
/// reference matrix, outside the exercise reveal gate, so showing every step's `after` here does
/// not leak an exercise answer (contract §2/§4 exception for reference tables).
private func systemCardChain(_ steps: [TrainingSnapshot.Matrix.ContrastPair]) -> Text {
    guard let first = steps.first else { return Text("") }
    return steps.reduce(highlightedText(first.beforeParts, before: true)) { result, step in
        result + Text(" → ") + highlightedText(step.afterParts, before: false)
    }
}

/// Emphasis contract tokens (`ContrastHighlightPlan.md` §"Контракт выделения (Emphasis contract)"):
/// `before` is a warm red with a **dashed** underline, `after` a cool accent with a **solid**
/// underline — colour is never the only cue, and both keep ≥4.5:1 contrast against the card
/// background in light and dark appearance (checked against plain white/`windowBackgroundColor`,
/// the worst case for this card). This is the *only* place this app turns a `HighlightPart` list
/// into styled `Text` (every call site above and in `MacStyleBlockView.swift` goes through it), so
/// there is one rendering path, not a second "simplified" one (contract §3).
///
/// [before] is only the *fallback* role for a part with no [TrainingSnapshot.HighlightPart.side]
/// of its own: a part that does carry a side always wins, since a mixed-role list (a style block's
/// own prose holding both a literal `focus.before` and a literal `focus.after` fragment) needs each
/// part's own role, not one role for the whole list (W3 correction, EmphasisUXAudit E7/S4) — the
/// same resolution order as the web reference's `appendContrastParts` (`TrainingWebApp.kt`).
func highlightedText(_ parts: [TrainingSnapshot.HighlightPart], before: Bool) -> Text {
    parts.reduce(Text("")) { result, part in
        let fragment = Text(part.text)
        guard part.changed else { return result + fragment }
        let isBefore = part.side.map { $0 == "before" } ?? before
        return result + fragment.bold()
            .foregroundColor(isBefore ? .emphasisBefore : .emphasisAfter)
            .underline(true, pattern: isBefore ? .dash : .solid)
    }
}

extension Color {
    /// Warm red, ~6.5:1 on white / ~7.3:1 on `windowBackgroundColor`'s dark value.
    static let emphasisBefore = Color(nsColor: NSColor(name: nil) { $0.bestMatch(from: [.aqua, .darkAqua]) == .darkAqua
        ? NSColor(red: 1.00, green: 0.541, blue: 0.502, alpha: 1) : NSColor(red: 0.702, green: 0.149, blue: 0.118, alpha: 1) })
    /// Cool teal, ~5.9:1 on white / ~8.9:1 on `windowBackgroundColor`'s dark value — never
    /// red/orange/yellow, so it cannot be confused with `emphasisBefore` (E9).
    static let emphasisAfter = Color(nsColor: NSColor(name: nil) { $0.bestMatch(from: [.aqua, .darkAqua]) == .darkAqua
        ? NSColor(red: 0.310, green: 0.820, blue: 0.773, alpha: 1) : NSColor(red: 0.043, green: 0.431, blue: 0.486, alpha: 1) })
}

private struct VocabularyView: View {
    @ObservedObject var model: MacModel
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    /// Same gate as `TrainingView.cardMotionReduced` (D5): system Reduce Motion or the app's own
    /// `Motion.Reduced` setting, so this card's flip snaps together with everything else it gates.
    private var cardMotionReduced: Bool { reduceMotion || model.preferences?.motion == "Reduced" || model.preferences?.animationsEnabled == false }
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
                    // EnRuAcceptance-2026-09-28.md §7 item 4: the active pack's own 2 directions
                    // (`state.directionOptions`), never a hardcoded "Русский → польский"/"Польский
                    // → русский" pair — en-ru now offers its own real "Русский → английский"/
                    // "Английский → русский" choice here the same way.
                    Picker("Направление", selection: Binding(get: { state.direction }, set: { model.vocab("direction", $0) })) {
                        ForEach(state.directionOptions) { option in Text(option.label).tag(option.wire) }
                    }.pickerStyle(.segmented)
                        .padding(3)
                        .accessibilityIdentifier("vocabularyDirection")
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

/// EN-22 (Plans/Kotlin/EnRuPackPlan.md §6): target/native pickers next to the style picker —
/// `MacPreferencesSession.set("target"/"native", …)` really switches `polski.data.packRegistry`'s
/// active pack (EN-06/EN-08), not just a saved preference. Built from `model.preferences?.packs`
/// (never a hardcoded pl/en case list) so it grows on its own once a second pack is safely
/// registered; today that list has exactly pl-ru, so both pickers render one disabled-looking but
/// real option — the existing pl-ru choice is unaffected either way.
private struct TargetNativePickers: View {
    @ObservedObject var model: MacModel
    private var targets: [PreferencesSnapshot.PackOption] {
        var seen = Set<String>()
        return (model.preferences?.packs ?? []).filter { seen.insert($0.target).inserted }
    }
    private var natives: [PreferencesSnapshot.PackOption] {
        var seen = Set<String>()
        return (model.preferences?.packs ?? []).filter { seen.insert($0.native).inserted }
    }
    var body: some View {
        Picker("Изучаемый язык", selection: Binding(
            get: { model.preferences?.target ?? "pl" },
            set: { model.preference("target", $0) }
        )) {
            ForEach(targets) { pack in Text(pack.targetLabel).tag(pack.target) }
        }
        .accessibilityIdentifier("targetPicker")
        Picker("Родной язык", selection: Binding(
            get: { model.preferences?.native ?? "ru" },
            set: { model.preference("native", $0) }
        )) {
            ForEach(natives) { pack in Text(pack.nativeLabel).tag(pack.native) }
        }
        .accessibilityIdentifier("nativePicker")
    }
}

/// UC-10 S1: replaces the old 2-value "Объяснение" `Picker` (`Logic`/`Situations`) with the 4
/// style recipes, each row's label/description coming from `model.preferences?.styles` (recipe
/// data, host-side Russian fallback while CONTENT is unmerged — never hardcoded here). A
/// native-contrast row gets a secondary hint, not a disabled state, whenever the current exercise's
/// skill has no `nativeParallel` content: `StyleComposer` already falls back tolerantly rather than
/// crashing or showing an empty block, so choosing it here is always safe.
private struct StylePickerRows: View {
    @ObservedObject var model: MacModel
    var body: some View {
        ForEach(model.preferences?.styles ?? []) { style in
            Button {
                model.preference("styleId", style.id)
            } label: {
                HStack(alignment: .top, spacing: 10) {
                    Image(systemName: model.preferences?.styleId == style.id ? "largecircle.fill.circle" : "circle")
                        .foregroundStyle(.tint)
                    VStack(alignment: .leading, spacing: 2) {
                        Text(style.label)
                        Text(style.description).font(.caption).foregroundStyle(.secondary)
                        if style.id == "NativeContrast" && model.training?.styleBlocks?.nativeContrastAvailable == false {
                            Text("Недоступно для текущего навыка — будет показан другой стиль")
                                .font(.caption2).foregroundStyle(.orange)
                        }
                    }
                    Spacer(minLength: 0)
                }
            }
            .buttonStyle(.plain)
            .accessibilityIdentifier("stylePicker.\(style.id)")
        }
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
                Section("Язык") { TargetNativePickers(model: model) }
                Section("Обучение") { StylePickerRows(model: model) }
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
                // D5: off keeps every Rive effect unloaded (RiveViewModel is only ever created
                // inside RiveEffectOverlay's `!reduceMotion` guard, and this toggle now widens that
                // same gate — see `cardMotionReduced`/`tabMotionReduced`) and snaps all remaining
                // motion instant, same as system Reduce Motion or `Motion.Reduced` already do.
                Toggle("Анимации", isOn: Binding(
                    get: { model.preferences?.animationsEnabled ?? true },
                    set: { model.preference("animationsEnabled", $0 ? "true" : "false") }
                ))
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

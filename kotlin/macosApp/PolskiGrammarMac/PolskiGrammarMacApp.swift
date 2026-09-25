import SwiftUI
import AppKit
import UniformTypeIdentifiers
import PolskiShared

private struct TrainingSnapshot: Decodable {
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
    let exercise: Exercise?
    let matrix: Matrix
    let progress: [Skill]
    let effects: [Effect]
}

private struct VocabularySnapshot: Decodable {
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
}

private struct PreferencesSnapshot: Decodable {
    let schemaVersion: Int
    let status: String
    let method: String?
    let answerMode: String?
    let appearance: String?
    let motion: String?
    let error: String?
}

private enum DocumentKind: String, CaseIterable, Identifiable {
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

private struct JSONDocument: FileDocument {
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

@MainActor
private final class MacModel: ObservableObject {
    @Published private(set) var training: TrainingSnapshot?
    @Published private(set) var vocabulary: VocabularySnapshot?
    @Published private(set) var preferences: PreferencesSnapshot?
    @Published var error: String?
    @Published var resetEffectId: Int64?
    @Published var exportEffectId: Int64?
    @Published var exportDocument: JSONDocument?
    @Published var selectedTab = "Training" {
        didSet { if oldValue != selectedTab { trainingSession.dispatch(command: "tab", value: selectedTab) } }
    }
    private let trainingSession: MacSession
    private let vocabularySession: MacVocabularySession
    private let preferencesSession: MacPreferencesSession
    private var claimedEffects = Set<Int64>()
    private var vocabularyRefreshTimer: Timer?
    private var activationObserver: NSObjectProtocol?

    init() {
        let root = ProcessInfo.processInfo.environment["POLSKI_MAC_DATA_DIR"]
            ?? FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
                .appendingPathComponent("Polski Grammar Matrix", isDirectory: true).path
        trainingSession = MacSession(directory: root)
        vocabularySession = MacVocabularySession(directory: root)
        preferencesSession = MacPreferencesSession(directory: root)
        trainingSession.onState = { [weak self] raw in Task { @MainActor in self?.consumeTraining(raw) } }
        vocabularySession.onState = { [weak self] raw in Task { @MainActor in self?.consumeVocabulary(raw) } }
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
                Group {
                    switch model.selectedTab {
                    case "Matrix": MatrixView(model: model)
                    case "Vocabulary": VocabularyView(model: model)
                    case "Progress": ProgressViewNative(model: model)
                    default: TrainingView(model: model)
                    }
                }
                .frame(minWidth: 320, minHeight: 420)
                .toolbar { SettingsLink { Label("Настройки", systemImage: "gearshape") } }
            }
        }
        .preferredColorScheme(colorScheme)
        .animation(reduceMotion ? nil : .easeInOut(duration: 0.12), value: model.selectedTab)
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
}

private struct TrainingView: View {
    @ObservedObject var model: MacModel
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                header
                if let state = model.training {
                    if state.loadStatus != "Ready" {
                        recovery(state)
                    } else if let exercise = state.exercise {
                        exerciseCard(state, exercise)
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
        .animation(reduceMotion || model.preferences?.motion == "Reduced" ? nil : .easeInOut(duration: 0.12), value: model.training?.phase)
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

    private func exerciseCard(_ state: TrainingSnapshot, _ exercise: TrainingSnapshot.Exercise) -> some View {
        contentCard {
            VStack(alignment: .leading, spacing: 24) {
                VStack(alignment: .leading, spacing: 8) {
                    Text("ПРЕДЛОЖЕНИЕ").font(.caption.weight(.semibold))
                        .tracking(1.4).foregroundStyle(.secondary)
                    highlightedText(exercise.sourceParts, before: true)
                        .font(.system(size: 28, weight: .medium, design: .rounded))
                        .accessibilityLabel(exercise.source)
                        .textSelection(.enabled)
                        .fixedSize(horizontal: false, vertical: true)
                }
                Divider()
                if state.introPending {
                    VStack(alignment: .leading, spacing: 18) {
                        Text("Знакомство с навыком").font(.title3.weight(.semibold))
                        Text("Посмотрите на предложение и переходите к заданию, когда будете готовы.")
                            .foregroundStyle(.secondary)
                        Button("Перейти к заданию") { model.send("continueIntroduction") }
                            .buttonStyle(.borderedProminent)
                            .controlSize(.large)
                    }
                } else {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("ЗАДАНИЕ").font(.caption.weight(.semibold))
                            .tracking(1.4).foregroundStyle(.secondary)
                        Text(exercise.prompt ?? "").font(.title3.weight(.medium))
                            .fixedSize(horizontal: false, vertical: true)
                    }
                    if state.phase == "Question" {
                        VStack(alignment: .leading, spacing: 18) {
                            Picker("Способ ответа", selection: Binding(
                                get: { state.answerMode },
                                set: { model.preference("answerMode", $0) }
                            )) {
                                Text("Вслух").tag("Oral")
                                Text("Напечатать").tag("Typed")
                            }
                            .pickerStyle(.segmented)
                            .padding(3)

                            .frame(maxWidth: 360)
                            if state.answerMode == "Typed" {
                                TextField("Ответ по-польски", text: Binding(
                                    get: { model.training?.draft ?? "" },
                                    set: { model.send("draft", $0) }
                                ), axis: .vertical)
                                .textFieldStyle(.roundedBorder)
                                .lineLimit(2...4)
                                .font(.body)
                            }
                            HStack {
                                Spacer()
                                Button("Показать ответ") { model.send("reveal", exercise.id) }
                                    .keyboardShortcut(.return, modifiers: [.command])
                                    .buttonStyle(.borderedProminent)
                                    .controlSize(.large)
                            }
                        }
                    } else if state.phase == "Revealed" {
                        VStack(alignment: .leading, spacing: 16) {
                            Text("ОТВЕТ").font(.caption.weight(.semibold))
                                .tracking(1.4).foregroundStyle(.secondary)
                            highlightedText(exercise.expectedParts ?? [], before: false)
                                .font(.title2.weight(.semibold))
                                .accessibilityLabel(exercise.expected ?? "")
                                .textSelection(.enabled)
                            if let answer = exercise.frozenAnswer {
                                Text("Ваш ответ: \(answer)").foregroundStyle(.secondary)
                            }
                            if let changes = exercise.changes, !changes.isEmpty {
                                Text("Что изменилось").font(.headline)
                                ForEach(Array(changes.enumerated()), id: \.offset) { _, change in
                                    VStack(alignment: .leading, spacing: 3) {
                                        (Text("Было: ")
                                         + Text(change.from).foregroundColor(Color(nsColor: .systemRed)).underline()
                                         + Text(" → Стало: ")
                                         + highlightedText(change.toParts, before: false))
                                            .textSelection(.enabled)
                                        Text(change.reason).foregroundStyle(.secondary)
                                    }
                                }
                            }
                            if let formula = exercise.formula, !formula.isEmpty {
                                VStack(alignment: .leading, spacing: 7) {
                                    Text("ЗАПОМНИ").font(.caption.weight(.semibold))
                                    Text(formula).font(.headline)
                                    if let feedback = exercise.methodFeedback, !feedback.isEmpty {
                                        Text(feedback)
                                    }
                                    if let explanation = exercise.explanation, !explanation.isEmpty {
                                        Text(explanation)
                                    }
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(14)
                                .background(Color.accentColor.opacity(0.12), in: RoundedRectangle(cornerRadius: 12))
                            }
                            HStack(spacing: 10) {
                                Spacer()
                                Button("Повторить") { model.send("rate", "\(exercise.id)|Again") }
                                    .keyboardShortcut("1", modifiers: [.command])
                                Button("Вспомнил") { model.send("rate", "\(exercise.id)|Good") }
                                    .keyboardShortcut("2", modifiers: [.command])
                                    .buttonStyle(.borderedProminent)
                            }
                            .controlSize(.large)
                        }
                    }
                }
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

private func highlightedText(_ parts: [TrainingSnapshot.HighlightPart], before: Bool) -> Text {
    parts.reduce(Text("")) { result, part in
        let fragment = Text(part.text)
        return result + (part.changed
            ? fragment.bold().foregroundColor(Color(nsColor: before ? .systemRed : .systemOrange)).underline()
            : fragment)
    }
}

private struct VocabularyView: View {
    @ObservedObject var model: MacModel
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
                        GroupBox("Карточка слова") {
                            VStack(alignment: .leading, spacing: 8) {
                                Text(item.lemma).font(.title2.bold())
                                if state.revealed {
                                    Text(item.translation)
                                    Text(item.form)
                                    Text(item.example)
                                    HStack {
                                        Button("Повторить") { model.vocab("again") }
                                        Button("Вспомнил") { model.vocab("good") }
                                    }
                                } else { Button("Показать ответ") { model.vocab("reveal") } }
                            }.frame(maxWidth: .infinity, alignment: .leading)
                        }
                    }
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
            Text("Polski Grammar Matrix · польский ↔ русский").foregroundStyle(.secondary)
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

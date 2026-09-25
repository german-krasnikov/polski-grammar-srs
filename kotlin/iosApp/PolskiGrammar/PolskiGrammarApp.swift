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

extension Dictionary where Key == String, Value == Any {
    func string(_ key: String) -> String { self[key] as? String ?? "" }
    func int(_ key: String) -> Int { (self[key] as? NSNumber)?.intValue ?? 0 }
    func int64(_ key: String) -> Int64? { (self[key] as? NSNumber)?.int64Value }
    func bool(_ key: String) -> Bool { (self[key] as? Bool) ?? false }
    func record(_ key: String) -> Record { self[key] as? Record ?? [:] }
    func rows(_ key: String) -> [Record] { self[key] as? [Record] ?? [] }
    func strings(_ key: String) -> [String] { self[key] as? [String] ?? [] }
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

final class AppModel: ObservableObject {
    @Published var state: Record = [:]
    @Published var vocabularyState: Record = [:]
    @Published var notice: String?
    @Published var preferences: Record = [:]
    @Published var exportFile: ProgressFile?
    @Published var exportEffectId: Int64?
    @Published var resetEffectId: Int64?
    @Published var focusEffectId: Int64?
    private let session = IosSession()
    private let vocabulary = IosVocabularySession()
    private let preferencesSession = IosPreferencesSession(defaults: .standard)
    private var lastSnapshot = ""
    private var claimedEffects = Set<Int64>()

    init() {
        session.onState = { [weak self] json in
            DispatchQueue.main.async { self?.receive(json) }
        }
        vocabulary.onState = { [weak self] json in
            DispatchQueue.main.async { self?.receiveVocabulary(json) }
        }
        preferencesSession.onState = { [weak self] json in
            DispatchQueue.main.async { self?.receivePreferences(json) }
        }
        receive(session.currentSnapshot())
        receiveVocabulary(vocabulary.currentSnapshot())
        receivePreferences(preferencesSession.currentSnapshot())
        if preferences.string("status") == "Ready" {
            send("explanationMethod", preferences.string("method"))
        }
    }

    deinit {
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
        if field == "method" { send("explanationMethod", value) }
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
                send("explanationMethod", preferences.string("method"))
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
        state = parsed
        for effect in parsed.rows("effects") {
            guard let id = effect.int64("id"), claimedEffects.insert(id).inserted else { continue }
            switch effect.string("kind") {
            case "export": exportFile = ProgressFile(text: effect.string("json")); exportEffectId = id
            case "reset": resetEffectId = id
            case "focus": focusEffectId = id
            default: acknowledge(id, completed: false)
            }
        }
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

    var body: some Scene {
        WindowGroup {
            TabView(selection: Binding(
                get: { model.state.string("tab") },
                set: { model.send("tab", $0) }
            )) {
                NavigationStack { TrainingView(model: model).toolbar { settingsToolbar } }
                    .tabItem { Label("Тренировка", systemImage: "square.stack") }.tag("Training")
                NavigationStack { MatrixView(model: model).toolbar { settingsToolbar } }
                    .tabItem { Label("Матрица", systemImage: "tablecells") }.tag("Matrix")
                NavigationStack { ProgressView(model: model, importing: $importing).toolbar { settingsToolbar } }
                    .tabItem { Label("Прогресс", systemImage: "chart.bar") }.tag("Progress")
                NavigationStack { VocabularyView(model: model, importing: $importingVocabulary,
                    exporting: $exportingVocabulary, exportFile: $vocabularyFile).toolbar { settingsToolbar } }
                    .tabItem { Label("Слова", systemImage: "character.book.closed") }.tag("Vocabulary")
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
            .onChange(of: scenePhase) { _, phase in if phase == .active { model.send("refresh") } }
            .tint(Color(uiColor: .systemTeal))
            .preferredColorScheme(colorScheme)
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

private func formattedDate(_ milliseconds: Int64?) -> String {
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

    var body: some View {
        Form {
            if model.preferences.string("status") == "RecoveryRequired" {
                Section("Восстановление") {
                    Text(model.preferences.string("error")).foregroundStyle(.red)
                    Text("Экспортируйте исходные настройки перед импортом проверенной копии.")
                }
            } else {
                Section("Обучение") {
                    Picker("Подача объяснений", selection: Binding(
                        get: { model.preferences.string("method") },
                        set: { model.setPreference("method", $0) }
                    )) {
                        Text("Схемы и логика").tag("Logic")
                        Text("Живые ситуации").tag("Situations")
                    }
                    Picker("Ответ", selection: Binding(
                        get: { model.preferences.string("answerMode") },
                        set: { model.setPreference("answerMode", $0) }
                    )) {
                        Text("Вслух").tag("Oral")
                        Text("Напечатать").tag("Typed")
                    }
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
                }
            }
            Section("Данные настроек") {
                Button("Экспортировать настройки JSON") {
                    exportFile = model.exportPreferences()
                    exporting = exportFile != nil
                }
                Button("Импортировать настройки JSON") { importing = true }
            }
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
    @State private var localDraft = ""
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
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
                    if let error = state["error"] as? String { Text(error).foregroundStyle(.red) }
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
                    Picker("Подача объяснений", selection: Binding(
                        get: { state.string("explanationMethod") },
                        set: { model.setPreference("method", $0) }
                    )) {
                        Text("Схемы и логика").tag("Logic")
                        Text("Живые ситуации").tag("Situations")
                    }
                }
                switch state.string("phase") {
                case "ChainComplete":
                    Section(state.string("chainCompletionTitle")) {
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
                    if !card.isEmpty { cardContent }
                }
                Section {
                    let introducing = state.string("phase") == "Question" && state.bool("introPending")
                    Button(!introducing && state.bool("showReference") ? "Скрыть таблицу" : "Таблица под рукой") {
                        model.send("reference")
                    }
                    .disabled(introducing)
                    if state.bool("showReference") && !introducing {
                        ForEach(Array(state.rows("referenceRows").enumerated()), id: \.offset) { _, row in
                            VStack(alignment: .leading, spacing: 4) {
                                Text(row.string("title")).font(.headline)
                                NativeContrastPairView(pair: row.record("pair"))
                            }
                        }
                        Button("Все таблицы и схема") { model.send("tab", "Matrix") }
                    }
                }
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
        .animation(reduceMotion || model.preferences.string("motion") == "Reduced" ? nil : .default,
                   value: state.string("phase"))
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

    private func revealButton(title: String, expands: Bool = true) -> some View {
        Button(title) {
            answerFocused = false
            model.send("reveal")
        }
        .accessibilityIdentifier("revealAnswer")
        .buttonStyle(.borderedProminent)
        .controlSize(.large)
        .frame(maxWidth: expands ? .infinity : nil)
        .onAppear {
            if let id = model.focusEffectId {
                model.focusEffectId = nil
                model.acknowledge(id, completed: false)
            }
        }
    }

    @ViewBuilder private var cardContent: some View {
        Section("\(card.string("skillLevel")) · \(card.string("skillTitle"))") {
            Text("Исходное предложение").font(.caption).foregroundStyle(.secondary)
            highlightedSentence(card.rows("sourceParts"), before: true)
                .font(.system(.title2, design: .rounded, weight: .semibold))
                .accessibilityLabel(card.string("source"))
            if state.bool("introPending") && state.string("phase") == "Question" {
                Text("Знакомство с навыком").font(.headline)
                Text(card.string("methodIntroduce"))
                Button("Перейти к заданию") { model.send("continueIntroduction") }
                    .buttonStyle(.borderedProminent)
                    .accessibilityIdentifier("continueIntroduction")
            } else {
            Label(card.string("prompt"), systemImage: "arrow.turn.down.right")
                .font(.headline)
                .foregroundStyle(.primary)
            Text(card.string("methodLead")).font(.footnote).foregroundStyle(.secondary)
            if state.string("phase") == "Question" {
                Text(card.string("methodRetrieve"))
                Picker("Ответ", selection: Binding(get: { state.string("answerMode") }, set: { model.send("answerMode", $0) })) {
                    Text("Вслух / про себя").tag("Oral")
                    Text("Напечатать").tag("Typed")
                }
                if state.string("answerMode") == "Typed" {
                    TextField("Ответ по-польски", text: Binding(get: { localDraft }, set: {
                        localDraft = $0
                        model.send("draft", $0)
                    }), axis: .vertical)
                        .lineLimit(2...5).textInputAutocapitalization(.sentences)
                        .focused($answerFocused)
                        .accessibilityLabel("Ответ по-польски")
                        .accessibilityIdentifier("typedAnswer")
                } else {
                    Text("Произнеси целое предложение, затем покажи ответ.").foregroundStyle(.secondary)
                }
                if state.string("answerMode") != "Typed" || !answerFocused {
                    revealButton(title: state.string("answerMode") == "Typed" ? "Проверить ответ" : "Показать ответ")
                }
            } else {
                Text("Эталон").font(.caption).foregroundStyle(.secondary)
                highlightedSentence(card.rows("expectedParts"), before: false)
                    .font(.system(.title2, design: .rounded, weight: .semibold))
                    .accessibilityLabel(card.string("expected"))
                if !card.strings("accepted").isEmpty { Text("Также: \(card.strings("accepted").joined(separator: " / "))") }
                if state.string("answerMode") == "Typed" {
                    Label(card.bool("correct") ? "Совпадает с правильным вариантом" : "Сравни свой ответ с эталоном",
                          systemImage: card.bool("correct") ? "checkmark.circle" : "info.circle")
                    Text(card.string("frozenAnswer").isEmpty ? "Ответ не введён" : card.string("frozenAnswer"))
                }
                if state.string("explanationMethod") == "Situations" {
                    Text(card.string("methodFeedback"))
                    Text(card.string("explanation"))
                }
                Text("Что изменилось").font(.headline)
                ForEach(Array(card.rows("changes").enumerated()), id: \.offset) { _, change in
                    VStack(alignment: .leading, spacing: 3) {
                        (Text("Было: ") + Text(change.string("from")).foregroundColor(Color(uiColor: .systemRed)).underline()
                         + Text(" → Стало: ") + highlightedNewForm(change))
                        Text(change.string("reason")).foregroundStyle(.secondary)
                    }
                }
                VStack(alignment: .leading, spacing: 7) {
                    Text("ЗАПОМНИ").font(.caption.weight(.semibold))
                    Text(card.string("formula")).font(.headline).bold()
                    if state.string("explanationMethod") == "Logic" {
                        Text(card.string("methodFeedback"))
                        Text(card.string("explanation"))
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(14)
                .background(Color.accentColor.opacity(0.12), in: RoundedRectangle(cornerRadius: 16))
                VStack(alignment: .leading, spacing: 9) {
                    Text("Когда повторить?").font(.headline)
                    Text(card.string("methodReview"))
                    Text("Свайп влево — повторить · вправо — вспомнил")
                        .font(.footnote).foregroundStyle(.secondary)
                        .accessibilityIdentifier("ratingSwipeArea")
                    HStack(spacing: 8) {
                        ForEach([("Again", "Повторить"), ("Good", "Вспомнил")], id: \.0) { rating, label in
                            Button {
                                model.send("rate", rating)
                            } label: {
                                VStack(alignment: .leading, spacing: 3) {
                                    Text(label).fontWeight(.semibold)
                                    Text(formattedDate(card.record("intervals").int64(rating)))
                                        .font(.caption).foregroundStyle(.secondary)
                                }
                                .frame(maxWidth: .infinity, minHeight: 48, alignment: .leading)
                            }
                            .buttonStyle(.bordered)
                            .accessibilityIdentifier("rate\(rating)")
                        }
                    }
                }
                .padding(.vertical, 4)
                .contentShape(Rectangle())
                .simultaneousGesture(DragGesture(minimumDistance: 18).onEnded { gesture in
                    guard state.string("phase") == "Revealed" else { return }
                    let x = gesture.translation.width
                    let y = gesture.translation.height
                    guard abs(x) >= 80, abs(x) > abs(y) * 1.5 else { return }
                    model.send("rate", x < 0 ? "Again" : "Good")
                })
            }
            }
        }
    }

    private func highlightedNewForm(_ change: Record) -> Text {
        change.rows("toParts").reduce(Text("")) { result, part in
            let fragment = Text(part.string("text")).bold()
            return result + (part.bool("changed")
                ? fragment.foregroundColor(Color(uiColor: .systemOrange)).underline()
                : fragment)
        }
    }

    private func highlightedSentence(_ parts: [Record], before: Bool) -> Text {
        parts.reduce(Text("")) { result, part in
            let fragment = Text(part.string("text"))
            return result + (part.bool("changed")
                ? fragment.bold().foregroundColor(Color(uiColor: before ? .systemRed : .systemOrange)).underline()
                : fragment)
        }
    }
}

private struct NativeContrastPairView: View {
    let pair: Record

    private func markedText(_ parts: [Record], before: Bool) -> Text {
        parts.reduce(Text("")) { result, part in
            let fragment = Text(part.string("text"))
            return result + (part.bool("changed")
                ? fragment.bold().foregroundColor(Color(uiColor: before ? .systemRed : .systemOrange))
                    .underline(true, pattern: before ? .dash : .solid)
                : fragment)
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 3) {
            Text("Было").font(.caption)
            markedText(pair.rows("beforeParts"), before: true)
            Text("→").font(.caption)
            Text("Стало").font(.caption)
            markedText(pair.rows("afterParts"), before: false)
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
                    Text(card.string("example")).font(.footnote).textSelection(.enabled)
                }
                .accessibilityElement(children: .combine)
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
                if let error = state["error"] as? String { Text(error).foregroundStyle(.red) }
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
    private var state: Record { model.vocabularyState }

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
                        let card = state.record("current")
                        let polishAnswer = state.string("direction") == "ru-pl"
                        Text(polishAnswer ? "Вспомни по-польски" : "Вспомни по-русски")
                            .font(.caption).foregroundStyle(.secondary)
                        Text(polishAnswer ? card.string("translation") : card.string("lemma"))
                            .font(.title2.weight(.semibold))
                        if !state.bool("revealed") {
                            Toggle("Напечатать ответ", isOn: Binding(
                                get: { state.bool("typed") },
                                set: { model.sendVocabulary("typed", $0 ? "true" : "false") }
                            ))
                            if state.bool("typed") {
                                TextField("Твой ответ", text: Binding(
                                    get: { state.string("draft") },
                                    set: { model.sendVocabulary("draft", $0) }
                                ))
                                .textInputAutocapitalization(.never)
                            }
                            Button("Показать ответ") { model.sendVocabulary("reveal") }
                                .accessibilityIdentifier("vocabularyReveal")
                        } else {
                            Text(polishAnswer ? card.string("lemma") : card.string("translation"))
                                .font(.title2.weight(.bold))
                            LabeledContent("Перевод", value: card.string("translation"))
                            LabeledContent("Форма", value: card.string("form"))
                            Text(card.string("example"))
                                .font(.callout)
                            if state.bool("typed") {
                                Text("Твой ответ: \(state.string("draft")). Сравни сам и выбери оценку.")
                                    .font(.footnote)
                            }
                            HStack {
                                Button("Повторить") { model.sendVocabulary("again") }
                                    .buttonStyle(.bordered)
                                    .accessibilityIdentifier("vocabularyAgain")
                                Spacer()
                                Button("Вспомнил") { model.sendVocabulary("good") }
                                    .buttonStyle(.borderedProminent)
                                    .accessibilityIdentifier("vocabularyGood")
                            }
                            .disabled(state.bool("busy"))
                            .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                                Button("Повторить") { model.sendVocabulary("again") }.tint(.orange)
                            }
                            .swipeActions(edge: .leading, allowsFullSwipe: true) {
                                Button("Вспомнил") { model.sendVocabulary("good") }.tint(.green)
                            }
                        }
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

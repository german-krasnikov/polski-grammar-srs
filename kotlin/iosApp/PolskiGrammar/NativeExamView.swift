import SwiftUI
import AVFoundation
import Combine
import PolskiShared

/// Scene-owned bridge state. Switching app tabs keeps the Kotlin answer snapshot intact.
final class NativeExamModel: ObservableObject {
    @Published private(set) var state: Record = [:]
    @Published private(set) var setSlotDrafts: [String: [String]] = [:]
    private let session = IosExamSession()
    let audio = ExamSegmentPlayer()
    private var audioChange: AnyCancellable?

    init() {
        audioChange = audio.objectWillChange.sink { [weak self] _ in self?.objectWillChange.send() }
        refresh()
    }
    deinit { audio.stop(); session.close() }

    private func refresh() {
        guard let data = session.currentSnapshot().data(using: .utf8),
              let decoded = (try? JSONSerialization.jsonObject(with: data)) as? Record else { return }
        state = decoded
    }

    func selectExam(_ routeId: String) { audio.stop(); setSlotDrafts.removeAll(); session.selectExam(routeId: routeId); refresh() }
    func selectModule(_ module: String) {
        if state.record("part").bool("submitted") { setSlotDrafts.removeAll() }
        audio.stop(); session.selectModule(module: module); refresh()
        if module == "listening" { audio.checkAvailability(assetId: state.record("part").string("audioAssetId")) }
    }
    func selectGrammar() { selectModule("grammar") }
    func startTimed() {
        audio.stop(); setSlotDrafts.removeAll(); session.startTimed(nowEpochMs: now); refresh()
        audio.checkAvailability(assetId: state.record("part").string("audioAssetId"))
    }
    func tick() {
        guard state.record("sitting").bool("completed") == false,
              state["sitting"] is Record else { return }
        let oldModule = state.record("sitting").string("module")
        session.tick(nowEpochMs: now); refresh()
        if oldModule != state.record("sitting").string("module") { audio.stop() }
    }
    func answer(_ itemId: String, _ value: String, module: String) {
        session.answerInModule(module: module, itemId: itemId, value: value, nowEpochMs: now); refresh()
    }
    func answerSetSlot(_ questionId: String, slot: Int, count: Int, text: String, module: String) {
        var drafts = setSlotDrafts[questionId] ?? Array(repeating: "", count: count)
        if drafts.count < count { drafts += Array(repeating: "", count: count - drafts.count) }
        drafts[slot] = text
        setSlotDrafts[questionId] = drafts
        let semanticAnswer = drafts.filter { !$0.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty }
            .joined(separator: "|")
        session.answerInModule(module: module, itemId: questionId, value: semanticAnswer, nowEpochMs: now)
        refresh()
    }
    func answerBankToken(_ itemId: String, _ tokenId: String) {
        session.answerBankTokenAt(itemId: itemId, tokenId: tokenId, nowEpochMs: now); refresh()
    }
    func next(module: String) { audio.stop(); session.nextInModule(module: module, nowEpochMs: now); refresh() }
    func previous(module: String) { audio.stop(); session.previousInModule(module: module, nowEpochMs: now); refresh() }
    func submit(module: String) { audio.stop(); session.submitInModule(module: module, nowEpochMs: now); refresh() }
    func selectWritingSet(_ id: String) {
        session.selectWritingSetAt(setId: id.isEmpty ? nil : id, nowEpochMs: now); refresh()
    }
    func exitGrammar() {
        if state.record("part").bool("submitted") { setSlotDrafts.removeAll() }
        audio.stop(); session.exitGrammar(); refresh()
    }
    func close() { audio.stop(); setSlotDrafts.removeAll(); session.close(); refresh() }
    private var now: Int64 { Int64(Date().timeIntervalSince1970 * 1000) }
}

struct NativeExamView: View {
    @Environment(\.scenePhase) private var scenePhase
    @ObservedObject var model: NativeExamModel
    let copy: Record
    private let clock = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        Group {
            switch model.state.string("screen") {
            case "modules": modules
            case "grammar": grammar
            case "grammarResult": result
            case "part": part
            case "sittingResult": sittingResult
            default: catalog
            }
        }
        .frame(maxWidth: 900)
        .frame(maxWidth: .infinity)
        .navigationTitle(uiText(copy, "exam", fallback: "exam"))
        .onReceive(clock) { _ in model.tick() }
        .onChange(of: model.state.record("part").string("id")) { _, _ in model.audio.stop() }
        .onChange(of: scenePhase) { _, phase in
            if phase == .active { model.tick() }
            else { model.audio.pause() }
        }
        .onAppear { model.tick() }
        .onDisappear { model.audio.pause() }
    }

    private var catalog: some View {
        List {
            Section(uiText(copy, "selectExam", fallback: "selectExam")) {
                ForEach(model.state.rows("catalog"), id: \.stringRouteId) { exam in
                    Button {
                        model.selectExam(exam.string("routeId"))
                    } label: {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(exam.string("displayTitle"))
                                .font(.headline)
                            Text("\(uiText(copy, "grammar", fallback: "grammar")) · \(exam.int("grammarItemCount"))")
                                .font(.subheadline).foregroundStyle(.secondary)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .contentShape(Rectangle())
                    }
                    .accessibilityIdentifier("exam-\(exam.string("routeId"))")
                }
            }
        }
    }

    private var modules: some View {
        List {
            Section(uiText(copy, "examChooseModule", fallback: "examChooseModule")) {
                ForEach(model.state.rows("modules"), id: \.stringModule) { module in
                    Button {
                        model.selectModule(module.string("module"))
                    } label: {
                        HStack {
                            VStack(alignment: .leading, spacing: 3) {
                                Text(uiText(copy, module.string("module"), fallback: module.string("module")))
                                    .font(.headline)
                            }
                            Spacer()
                            Image(systemName: "chevron.right").foregroundStyle(.secondary)
                        }
                        .frame(minHeight: 44)
                    }
                    .accessibilityIdentifier("exam-module-\(module.string("module"))")
                }
            }
            Button(uiText(copy, "examTimedStart", fallback: "Start timed exam")) { model.startTimed() }
                .accessibilityIdentifier("exam-start-timed")
            Button(uiText(copy, "selectExam", fallback: "selectExam")) { model.close() }
        }
    }

    private var grammar: some View {
        let card = model.state.record("grammar")
        let itemId = card.string("itemId")
        return ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                if model.state["sitting"] is Record {
                    let sitting = model.state.record("sitting")
                    Text("\(sitting.int("position")) / 4 · \(max(0, sitting.int("remainingMs") / 1000)) s")
                        .font(.headline.monospacedDigit())
                        .accessibilityIdentifier("exam-timer")
                }
                Text("\(card.int("currentIndex") + 1) / \(card.int("itemCount"))")
                    .font(.subheadline).foregroundStyle(.secondary)
                Text(card.string("instruction"))
                    .font(.headline)
                    .accessibilityIdentifier("exam-instruction")
                Text(card.string("context"))
                    .font(.title3)
                    .accessibilityIdentifier("exam-context")
                if !card.string("questionTarget").isEmpty {
                    Text(card.string("questionTarget"))
                        .fontWeight(.semibold)
                        .accessibilityIdentifier("exam-question-target")
                }
                answerControl(card: card, itemId: itemId)
                HStack(spacing: 12) {
                    Button(uiText(copy, "examPrevious", fallback: "examPrevious")) { model.previous(module: "grammar") }
                        .disabled(card.bool("isFirst"))
                        .accessibilityIdentifier("exam-previous")
                    Spacer()
                    if card.bool("isLast") {
                        Button(uiText(copy, "finish", fallback: "finish")) { model.submit(module: "grammar") }
                            .buttonStyle(.borderedProminent)
                            .accessibilityIdentifier("exam-submit")
                    } else {
                        Button(uiText(copy, "next", fallback: "next")) { model.next(module: "grammar") }
                            .buttonStyle(.borderedProminent)
                            .accessibilityIdentifier("exam-next")
                    }
                }
                if model.state["sitting"] is Record, !card.bool("isLast") {
                    Button(uiText(copy, "finish", fallback: "Finish part")) { model.submit(module: "grammar") }
                        .accessibilityIdentifier("exam-finish-part")
                }
                if !(model.state["sitting"] is Record) {
                    Button(uiText(copy, "examBackToModules", fallback: "examBackToModules")) { model.exitGrammar() }
                        .accessibilityIdentifier("exam-exit-grammar")
                }
            }
            .padding()
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }

    private var part: some View {
        let card = model.state.record("part")
        let module = card.string("module")
        return ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                if model.state["sitting"] is Record {
                    let sitting = model.state.record("sitting")
                    Text("\(sitting.int("position")) / 4 · \(max(0, sitting.int("remainingMs") / 1000)) s")
                        .font(.headline.monospacedDigit())
                        .accessibilityIdentifier("exam-timer")
                }
                Text(uiText(copy, module, fallback: module)).font(.title2.weight(.semibold))
                Text("\(card.int("position")) / \(card.int("count"))")
                    .foregroundStyle(.secondary)
                if module == "writing", !card.strings("sets").isEmpty {
                    Picker(uiText(copy, "writingSet", fallback: "Writing set"), selection: Binding(
                        get: { card.string("selectedSet") },
                        set: { model.selectWritingSet($0) }
                    )) {
                        Text(uiText(copy, "selectWritingSet", fallback: "Select a set")).tag("")
                        ForEach(card.strings("sets"), id: \.self) { Text($0).tag($0) }
                    }
                    .accessibilityIdentifier("exam-writing-set")
                }
                if !card.string("instruction").isEmpty {
                    Text(card.string("instruction")).font(.headline)
                        .accessibilityIdentifier("exam-part-instruction")
                }
                if !card.string("text").isEmpty {
                    Text(card.string("text"))
                        .textSelection(.enabled)
                        .accessibilityIdentifier("exam-part-text")
                }
                if !card.string("promptImage").isEmpty {
                    examImage(card.string("promptImage"), alt: card.string("promptImageAlt"))
                }
                if module == "listening" { listeningControls(card) }
                if module == "writing" {
                    if !card.string("taskId").isEmpty {
                        TextEditor(text: Binding(
                            get: { model.state.record("part").string("draft") },
                            set: { model.answer(card.string("taskId"), $0, module: module) }
                        ))
                        .frame(minHeight: 180)
                        .accessibilityLabel(uiText(copy, "examAnswerPrompt", fallback: "Answer"))
                        .accessibilityIdentifier("exam-writing-draft")
                        if card["wordLimit"] is NSNumber {
                            Text("\(card.int("wordCount")) / \(card.int("wordLimit"))")
                                .foregroundStyle(.secondary)
                        }
                    }
                } else {
                    ForEach(card.rows("questions"), id: \.stringQuestionId) { question in
                        questionControl(question, module: module)
                    }
                }
                if card.bool("submitted") {
                    partScore(card)
                }
                if !(model.state["sitting"] is Record) {
                    Button(uiText(copy, "examBackToModules", fallback: "Modules")) { model.exitGrammar() }
                        .accessibilityIdentifier("exam-exit-part")
                }
            }
            .padding()
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .safeAreaInset(edge: .bottom) {
            if !card.bool("submitted") {
                HStack(spacing: 12) {
                    Button(uiText(copy, "examPrevious", fallback: "Previous")) { model.previous(module: module) }
                        .disabled(!card.bool("canPrevious"))
                        .accessibilityIdentifier("exam-previous")
                    Spacer()
                    Button(uiText(copy, "next", fallback: "Next")) { model.next(module: module) }
                        .disabled(!card.bool("canNext"))
                        .accessibilityIdentifier("exam-next")
                    Button(uiText(copy, "finish", fallback: "Finish part")) { model.submit(module: module) }
                        .buttonStyle(.borderedProminent)
                        .accessibilityIdentifier("exam-submit")
                }
                .frame(minHeight: 44)
                .padding(.horizontal)
                .padding(.vertical, 8)
                .background(.bar)
            }
        }
    }

    @ViewBuilder
    private func questionControl(_ question: Record, module: String) -> some View {
        let id = question.string("id")
        VStack(alignment: .leading, spacing: 9) {
            Text(question.string("text")).fontWeight(.medium)
                .accessibilityIdentifier("exam-question-\(id)")
            if question.string("answerMode") == "unordered_set" {
                if question.strings("options").isEmpty {
                    ForEach(0..<max(1, question.int("answerSlotCount")), id: \.self) { slot in
                        TextField("\(slot + 1)", text: Binding(
                            get: {
                                let drafts = model.setSlotDrafts[id] ?? []
                                return drafts.indices.contains(slot) ? drafts[slot] : ""
                            },
                            set: { model.answerSetSlot(id, slot: slot, count: max(1, question.int("answerSlotCount")), text: $0, module: module) }
                        ))
                        .textFieldStyle(.roundedBorder)
                        .accessibilityIdentifier("exam-answer-\(id)-\(slot)")
                    }
                } else {
                    ForEach(question.strings("options"), id: \.self) { option in
                        let selected = question.string("answer").split(separator: "|").map(String.init)
                        Button {
                            var next = selected
                            if next.contains(option) { next.removeAll { $0 == option } }
                            else if next.count < question.int("answerSlotCount") { next.append(option) }
                            model.answer(id, next.joined(separator: "|"), module: module)
                        } label: {
                            Label(option, systemImage: selected.contains(option) ? "checkmark.square.fill" : "square")
                        }
                        .accessibilityAddTraits(selected.contains(option) ? [.isSelected] : [])
                    }
                }
            } else if !question.strings("options").isEmpty {
                ForEach(question.strings("options"), id: \.self) { option in
                    Button(option) { model.answer(id, option, module: module) }
                        .buttonStyle(.bordered)
                        .accessibilityAddTraits(question.string("answer") == option ? [.isSelected] : [])
                }
            } else {
                TextField(uiText(copy, "examAnswerPrompt", fallback: "Answer"), text: Binding(
                    get: { model.state.record("part").rows("questions").first { $0.string("id") == id }?.string("answer") ?? "" },
                    set: { model.answer(id, $0, module: module) }
                ))
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .textFieldStyle(.roundedBorder)
                .accessibilityIdentifier("exam-answer-\(id)")
            }
            if question["status"] != nil {
                Text(reviewStatus(question))
                    .font(.caption.weight(.semibold))
                Text(question.strings("officialExamples").joined(separator: " / "))
                    .font(.footnote).foregroundStyle(.secondary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    @ViewBuilder
    private func examImage(_ reference: String, alt: String) -> some View {
        let name = URL(fileURLWithPath: reference).lastPathComponent
        if let url = Bundle.main.url(forResource: name, withExtension: nil),
           let image = UIImage(contentsOfFile: url.path) {
            Image(uiImage: image)
                .resizable().scaledToFit()
                .accessibilityLabel(alt)
                .accessibilityIdentifier("exam-prompt-image")
        } else {
            Text(uiText(copy, "examImageUnavailable", fallback: "Image unavailable"))
                .foregroundStyle(.red)
        }
    }

    private func listeningControls(_ card: Record) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Button(uiText(copy, model.audio.playing ? "examAudioPause" : "examAudioPlay", fallback: model.audio.playing ? "Pause" : "Play")) {
                    model.audio.toggle(assetId: card.string("audioAssetId"),
                                       startMs: card.int("segmentStartMs"), endMs: card.int("segmentEndMs"))
                }
                .accessibilityIdentifier("exam-audio-play")
                Button(uiText(copy, "examAudioReplay", fallback: "Replay")) {
                    model.audio.replay(assetId: card.string("audioAssetId"),
                                       startMs: card.int("segmentStartMs"), endMs: card.int("segmentEndMs"))
                }
                .accessibilityIdentifier("exam-audio-replay")
            }
            if let error = model.audio.error {
                Text(uiText(copy, error, fallback: "Audio unavailable")).foregroundStyle(.red)
            }
            Slider(value: Binding(
                get: { model.audio.progress },
                set: { model.audio.seek(assetId: card.string("audioAssetId"),
                                        startMs: card.int("segmentStartMs"),
                                        endMs: card.int("segmentEndMs"), fraction: $0) }
            ), in: 0...1)
                .accessibilityLabel(uiText(copy, "examAudioSeek", fallback: "Seek recording"))
                .accessibilityValue("\(Int(model.audio.progress * 100)) percent")
                .accessibilityIdentifier("exam-audio-seek")
            HStack {
                VStack(alignment: .leading) {
                    Text(uiText(copy, "examAudioElapsed", fallback: "Elapsed")).font(.caption)
                    Text(audioTime(model.audio.elapsedSeconds))
                        .monospacedDigit()
                        .accessibilityIdentifier("exam-audio-elapsed")
                }
                Spacer()
                VStack(alignment: .trailing) {
                    Text(uiText(copy, "examAudioDuration", fallback: "Duration")).font(.caption)
                    Text(audioTime(model.audio.durationSeconds > 0 ? model.audio.durationSeconds :
                        Double(max(0, card.int("segmentEndMs") - card.int("segmentStartMs"))) / 1000))
                        .monospacedDigit()
                        .accessibilityIdentifier("exam-audio-duration")
                }
            }
        }
    }

    private func audioTime(_ seconds: Double) -> String {
        let whole = Int(max(0, seconds).rounded(.down))
        return "\(whole / 60):\(String(format: "%02d", whole % 60))"
    }

    private func partScore(_ card: Record) -> some View {
        let score = card.record("score")
        return VStack(alignment: .leading, spacing: 8) {
            Text(uiText(copy, "results", fallback: "Results")).font(.headline)
            Text("\(formatQuarter(score.int("confirmedQuarter"))) / \(formatQuarter(score.int("possibleQuarter")))")
            if score.int("manualPendingQuarter") > 0 {
                Text("\(uiText(copy, "examManualReview", fallback: "Manual review")): \(formatQuarter(score.int("manualPendingQuarter")))")
            }
            if score.bool("isFinal"), score["finalPercent"] != nil {
                Text("\(score.int("finalPercent"))%")
            }
        }
        .accessibilityIdentifier("exam-part-result")
    }

    private var sittingResult: some View {
        let sitting = model.state.record("sitting")
        return List {
            Section(uiText(copy, "results", fallback: "Results")) {
                Text("\(formatQuarter(sitting.int("confirmedQuarter"))) / \(formatQuarter(sitting.int("possibleQuarter")))")
                if sitting.int("manualPendingQuarter") > 0 {
                    Text(uiText(copy, "examManualReview", fallback: "Manual review"))
                }
                if sitting.bool("isFinal"), sitting["finalPercent"] != nil {
                    Text("\(sitting.int("finalPercent"))%")
                }
            }
            Section(uiText(copy, "examTimedPart", fallback: "Parts")) {
                ForEach(sitting.rows("partScores"), id: \.stringModule) { score in
                    VStack(alignment: .leading, spacing: 4) {
                        Text(uiText(copy, score.string("module"), fallback: score.string("module")))
                            .font(.headline)
                        Text("\(formatQuarter(score.int("confirmedQuarter"))) / \(formatQuarter(score.int("possibleQuarter")))")
                        if score.int("manualPendingQuarter") > 0 {
                            Text(uiText(copy, "examManualReview", fallback: "Manual review"))
                                .font(.caption)
                        }
                    }
                }
            }
            Button(uiText(copy, "selectExam", fallback: "Select exam")) { model.close() }
        }
    }

    private func formatQuarter(_ quarter: Int) -> String {
        let whole = quarter / 4
        let remainder = quarter % 4
        switch remainder {
        case 0: return "\(whole)"
        case 1: return "\(whole),25"
        case 2: return "\(whole),5"
        default: return "\(whole),75"
        }
    }

    @ViewBuilder
    private func answerControl(card: Record, itemId: String) -> some View {
        switch card.string("questionType") {
        case "choose_form":
            ForEach(card.strings("options"), id: \.self) { option in
                Button(option) { model.answer(itemId, option, module: "grammar") }
                    .buttonStyle(.bordered)
                    .tint(card.string("answer") == option ? .accentColor : .secondary)
                    .accessibilityAddTraits(card.string("answer") == option ? [.isSelected] : [])
                    .accessibilityIdentifier("exam-option-\(option)")
            }
        case "word_bank":
            ForEach(card.rows("bankTokens"), id: \.stringTokenId) { token in
                Button(token.string("text")) { model.answerBankToken(itemId, token.string("tokenId")) }
                    .buttonStyle(.bordered)
                    .accessibilityLabel("\(token.string("text")), \(token.int("position"))")
                    .accessibilityAddTraits(card.string("selectedBankTokenId") == token.string("tokenId") ? [.isSelected] : [])
                    .accessibilityIdentifier("exam-bank-\(token.string("tokenId"))")
            }
        case "fill_gap", "transform", "word_formation", "question_formation":
            TextField(uiText(copy, "examAnswerPrompt", fallback: "examAnswerPrompt"), text: Binding(
                get: { model.state.record("grammar").string("answer") },
                set: { model.answer(itemId, $0, module: "grammar") }
            ))
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
            .textFieldStyle(.roundedBorder)
            .accessibilityIdentifier("exam-grammar-answer")
        default:
            Text(uiText(copy, "examNativeUnavailable", fallback: "examNativeUnavailable"))
        }
    }

    private var result: some View {
        let graded = model.state.record("grammar")
        return List {
            Section(uiText(copy, "results", fallback: "results")) {
                LabeledContent(uiText(copy, "examConfirmedPoints", fallback: "examConfirmedPoints"),
                               value: "\(graded.string("confirmedPoints")) / \(graded.string("possiblePoints"))")
                if graded.int("manualPendingQuarter") > 0 {
                    LabeledContent(uiText(copy, "examPendingPoints", fallback: "examPendingPoints"),
                                   value: graded.string("pendingPoints"))
                }
                if graded.bool("final") {
                    Text("\(graded.int("percentage"))%")
                        .font(.title2.weight(.semibold))
                }
            }
            Section(uiText(copy, "examOfficialExamples", fallback: "examOfficialExamples")) {
                ForEach(graded.rows("review"), id: \.stringItemId) { item in
                    VStack(alignment: .leading, spacing: 5) {
                        Text(item.string("context"))
                        Text(item.string("answer"))
                            .foregroundStyle(.secondary)
                        Text(reviewStatus(item))
                            .font(.caption.weight(.semibold))
                        if item.string("status") != "ConfirmedFull" {
                            Text(item.strings("officialExamples").joined(separator: " / "))
                                .font(.footnote)
                        }
                        if !item.rows("rubricComponents").isEmpty {
                            Text(uiText(copy, "examManualReview", fallback: "Manual review"))
                                .font(.footnote.weight(.semibold))
                            ForEach(item.rows("rubricComponents"), id: \.stringComponentId) { component in
                                HStack(alignment: .firstTextBaseline, spacing: 8) {
                                    Text(component.string("criterionText"))
                                    Spacer(minLength: 8)
                                    Text(component.string("creditPoints"))
                                        .foregroundStyle(.secondary)
                                }
                                .font(.footnote)
                                .accessibilityElement(children: .combine)
                                .accessibilityIdentifier("exam-rubric-\(component.string("componentId"))")
                            }
                        }
                    }
                    .accessibilityIdentifier("exam-review-\(item.string("itemId"))")
                }
            }
            Button(uiText(copy, "tryAgain", fallback: "tryAgain")) { model.selectGrammar() }
            Button(uiText(copy, "examBackToModules", fallback: "examBackToModules")) { model.exitGrammar() }
        }
    }

    private func reviewStatus(_ item: Record) -> String {
        switch item.string("status") {
        case "ConfirmedFull": return uiText(copy, "matches", fallback: "matches")
        case "ConfirmedPartial": return uiText(copy, "examConfirmedPoints", fallback: "examConfirmedPoints")
        case "PendingManual": return uiText(copy, "examManualReview", fallback: "examManualReview")
        default: return uiText(copy, "differs", fallback: "differs")
        }
    }
}

private extension Dictionary where Key == String, Value == Any {
    var stringRouteId: String { string("routeId") }
    var stringModule: String { string("module") }
    var stringTokenId: String { string("tokenId") }
    var stringItemId: String { string("itemId") }
    var stringQuestionId: String { string("id") }
    var stringComponentId: String { string("componentId") }
}

/// Owns one offline listening segment. A card change, tab switch, or scene
/// disposal stops its timer and audio; no playback state crosses into Kotlin.
final class ExamSegmentPlayer: ObservableObject {
    @Published private(set) var playing = false
    @Published private(set) var progress = 0.0
    @Published private(set) var error: String?
    private var player: AVAudioPlayer?
    private var timer: Timer?
    private var currentKey = ""
    private var segmentStart = 0.0
    private var segmentEnd = 0.0
    var elapsedSeconds: Double { progress * durationSeconds }
    var durationSeconds: Double { max(0, segmentEnd - segmentStart) }

    func checkAvailability(assetId: String) {
        if Bundle.main.url(forResource: assetId, withExtension: "mp3") == nil {
            error = "examAudioUnavailable"
        }
    }

    func toggle(assetId: String, startMs: Int, endMs: Int) {
        let key = "\(assetId):\(startMs):\(endMs)"
        if currentKey != key || player == nil { open(assetId: assetId, startMs: startMs, endMs: endMs) }
        guard let player, error == nil else { return }
        if player.isPlaying {
            pause()
        } else {
            if player.currentTime >= segmentEnd { player.currentTime = segmentStart; progress = 0 }
            playing = player.play()
            if !playing { error = "examAudioPlaybackFailed"; return }
            timer?.invalidate()
            timer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { [weak self] _ in self?.updateProgress() }
        }
    }

    func replay(assetId: String, startMs: Int, endMs: Int) {
        stop()
        toggle(assetId: assetId, startMs: startMs, endMs: endMs)
    }

    /** Seek within the source segment, including before playback starts. */
    func seek(assetId: String, startMs: Int, endMs: Int, fraction: Double) {
        let key = "\(assetId):\(startMs):\(endMs)"
        if currentKey != key || player == nil { open(assetId: assetId, startMs: startMs, endMs: endMs) }
        guard let player, error == nil else { return }
        let clamped = fraction.isFinite ? min(1, max(0, fraction)) : 0
        player.currentTime = segmentStart + clamped * durationSeconds
        progress = clamped
        if clamped >= 1 {
            player.pause(); playing = false
            timer?.invalidate(); timer = nil
        }
    }

    func stop() {
        timer?.invalidate(); timer = nil
        player?.stop(); player = nil
        currentKey = ""; playing = false; progress = 0
        error = nil
    }

    /** Suspend for tab and scene changes without losing the segment position. */
    func pause() {
        player?.pause()
        playing = false
        timer?.invalidate(); timer = nil
    }

    private func open(assetId: String, startMs: Int, endMs: Int) {
        stop()
        guard let url = Bundle.main.url(forResource: assetId, withExtension: "mp3") else {
            error = "examAudioUnavailable"; return
        }
        do {
            let loaded = try AVAudioPlayer(contentsOf: url)
            guard loaded.prepareToPlay(), endMs > startMs,
                  Double(startMs) / 1000 < loaded.duration else {
                error = "examAudioInvalidSegment"; return
            }
            segmentStart = Double(startMs) / 1000
            segmentEnd = min(Double(endMs) / 1000, loaded.duration)
            guard segmentEnd > segmentStart else { error = "examAudioInvalidSegment"; return }
            loaded.currentTime = segmentStart
            player = loaded
            currentKey = "\(assetId):\(startMs):\(endMs)"
        } catch {
            self.error = "examAudioUnavailable"
        }
    }

    private func updateProgress() {
        guard let player else { stop(); return }
        progress = min(1, max(0, (player.currentTime - segmentStart) / (segmentEnd - segmentStart)))
        if player.currentTime >= segmentEnd || !player.isPlaying {
            player.pause(); playing = false
            timer?.invalidate(); timer = nil
        }
    }
}

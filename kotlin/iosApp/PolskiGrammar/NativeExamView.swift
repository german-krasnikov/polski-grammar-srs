import SwiftUI
import PolskiShared

/// Scene-owned bridge state. Switching app tabs keeps the Kotlin answer snapshot intact.
final class NativeExamModel: ObservableObject {
    @Published private(set) var state: Record = [:]
    private let session = IosExamSession()

    init() { refresh() }
    deinit { session.close() }

    private func refresh() {
        guard let data = session.currentSnapshot().data(using: .utf8),
              let decoded = (try? JSONSerialization.jsonObject(with: data)) as? Record else { return }
        state = decoded
    }

    func selectExam(_ routeId: String) { session.selectExam(routeId: routeId); refresh() }
    func selectGrammar() { session.selectModule(module: "grammar"); refresh() }
    func answer(_ itemId: String, _ value: String) { session.answer(itemId: itemId, value: value); refresh() }
    func answerBankToken(_ itemId: String, _ tokenId: String) {
        session.answerBankToken(itemId: itemId, tokenId: tokenId); refresh()
    }
    func next() { session.next(); refresh() }
    func previous() { session.previous(); refresh() }
    func submit() { session.submit(); refresh() }
    func exitGrammar() { session.exitGrammar(); refresh() }
    func close() { session.close(); refresh() }
}

struct NativeExamView: View {
    @ObservedObject var model: NativeExamModel
    let copy: Record

    var body: some View {
        Group {
            switch model.state.string("screen") {
            case "modules": modules
            case "grammar": grammar
            case "grammarResult": result
            default: catalog
            }
        }
        .frame(maxWidth: 900)
        .frame(maxWidth: .infinity)
        .navigationTitle(uiText(copy, "exam", fallback: "exam"))
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
                    let available = module.bool("availableOnIpad")
                    Button {
                        if available { model.selectGrammar() }
                    } label: {
                        HStack {
                            VStack(alignment: .leading, spacing: 3) {
                                Text(uiText(copy, module.string("module"), fallback: module.string("module")))
                                    .font(.headline)
                                if !available {
                                    Text(uiText(copy, "examNativeUnavailable", fallback: "examNativeUnavailable"))
                                        .font(.caption).foregroundStyle(.secondary)
                                }
                            }
                            Spacer()
                            if available { Image(systemName: "chevron.right").foregroundStyle(.secondary) }
                        }
                        .frame(minHeight: 44)
                    }
                    .disabled(!available)
                    .accessibilityIdentifier("exam-module-\(module.string("module"))")
                }
            }
            Button(uiText(copy, "selectExam", fallback: "selectExam")) { model.close() }
        }
    }

    private var grammar: some View {
        let card = model.state.record("grammar")
        let itemId = card.string("itemId")
        return ScrollView {
            VStack(alignment: .leading, spacing: 18) {
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
                    Button(uiText(copy, "examPrevious", fallback: "examPrevious")) { model.previous() }
                        .disabled(card.bool("isFirst"))
                        .accessibilityIdentifier("exam-previous")
                    Spacer()
                    if card.bool("isLast") {
                        Button(uiText(copy, "finish", fallback: "finish")) { model.submit() }
                            .buttonStyle(.borderedProminent)
                            .accessibilityIdentifier("exam-submit")
                    } else {
                        Button(uiText(copy, "next", fallback: "next")) { model.next() }
                            .buttonStyle(.borderedProminent)
                            .accessibilityIdentifier("exam-next")
                    }
                }
                Button(uiText(copy, "examBackToModules", fallback: "examBackToModules")) { model.exitGrammar() }
                    .accessibilityIdentifier("exam-exit-grammar")
            }
            .padding()
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }

    @ViewBuilder
    private func answerControl(card: Record, itemId: String) -> some View {
        switch card.string("questionType") {
        case "choose_form":
            ForEach(card.strings("options"), id: \.self) { option in
                Button(option) { model.answer(itemId, option) }
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
                set: { model.answer(itemId, $0) }
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
}

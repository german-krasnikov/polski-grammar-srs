import SwiftUI

/// Native presentation of the transient Cloze session owned by TrainingStore.
struct ClozeView: View {
    @ObservedObject var model: AppModel
    @Environment(\.scenePhase) private var scenePhase
    @State private var advanceTask: Task<Void, Never>?
    @State private var pendingChoiceKey: String?

    private var session: Record { model.state.record("cloze") }
    private var current: Record { session.record("current") }
    private var index: Int { session.int("currentIndex") }
    private var total: Int { session.int("total") }
    private var id: String { current.string("id") }
    private var selected: String? { current["selectedForm"] as? String }
    private var correct: String? { current["correctForm"] as? String }
    private var options: [String] { current["options"] as? [String] ?? [] }
    private var questionKey: String { "\(index):\(id)" }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                if model.state.string("loadStatus") == "Loading" ||
                    (model.state.string("loadStatus") == "Ready" && session.isEmpty) {
                    ProgressView("Загрузка заданий…")
                        .accessibilityIdentifier("clozeLoading")
                } else if id.isEmpty {
                    ContentUnavailableView("Нет заданий", systemImage: "text.insert",
                        description: Text("Попробуй начать заново."))
                        .accessibilityIdentifier("clozeEmpty")
                } else {
                    Text("Задание \(index + 1) из \(total)")
                        .font(.headline)
                        .accessibilityIdentifier("clozePosition")
                    Text(current.string("displaySentence"))
                        .font(.title3)
                        .fixedSize(horizontal: false, vertical: true)
                        .accessibilityAddTraits(.updatesFrequently)
                        .accessibilityIdentifier("clozeSentence")
                        .environment(\.locale, Locale(identifier: "pl_PL"))
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: 145), spacing: 10)], spacing: 10) {
                        ForEach(options, id: \.self) { form in
                            Button {
                                pendingChoiceKey = questionKey
                                model.chooseCloze(id: id, form: form)
                            } label: {
                                Text(form)
                                    .frame(maxWidth: .infinity, minHeight: 44)
                                    .fixedSize(horizontal: false, vertical: true)
                            }
                            .buttonStyle(.borderedProminent)
                            .tint(selected == nil ? .accentColor
                                : form == correct ? .green : form == selected ? .red : .gray)
                            .disabled(selected != nil)
                            .accessibilityLabel(optionLabel(form))
                            .accessibilityIdentifier("clozeOption-\(form)")
                        }
                    }
                    if let selected, let correct {
                        Text(selected == correct ? "Верно: \(correct)" : "Неверно. Правильная форма: \(correct)")
                            .font(.headline)
                            .fixedSize(horizontal: false, vertical: true)
                            .accessibilityLabel(selected == correct
                                ? "Верно. Правильная форма: \(correct)"
                                : "Неверно. Выбрано: \(selected). Правильная форма: \(correct)")
                            .accessibilityIdentifier("clozeResult")
                    }
                }
                HStack {
                    Button("Назад") { cancelAdvance(); pendingChoiceKey = nil; model.send("clozePrevious") }
                        .disabled(index <= 0 || total == 0)
                        .accessibilityIdentifier("clozePrevious")
                    Button("Далее") { cancelAdvance(); pendingChoiceKey = nil; model.send("clozeNext") }
                        .disabled(index >= total - 1 || total == 0)
                        .accessibilityIdentifier("clozeNext")
                    Spacer()
                    Button("Заново") { cancelAdvance(); pendingChoiceKey = nil; model.send("clozeRestart") }
                        .accessibilityIdentifier("clozeRestart")
                }
                .buttonStyle(.bordered)
            }
            .frame(maxWidth: 700, alignment: .leading)
            .frame(maxWidth: .infinity)
            .padding()
        }
        .navigationTitle("Подстановка")
        .onAppear { startIfReady() }
        .onChange(of: model.state.string("loadStatus")) { _, _ in startIfReady() }
        .onChange(of: questionKey) { _, _ in cancelAdvance(); pendingChoiceKey = nil }
        .onChange(of: selected) { old, new in
            guard old == nil, new != nil, pendingChoiceKey == questionKey,
                  index < total - 1 else { return }
            pendingChoiceKey = nil
            scheduleAdvance(id: id, index: index)
        }
        .onDisappear { cancelAdvance(); pendingChoiceKey = nil }
        .onChange(of: scenePhase) { _, phase in
            if phase != .active { cancelAdvance(); pendingChoiceKey = nil }
        }
    }

    private func optionLabel(_ form: String) -> String {
        guard let selected, let correct else { return form }
        if form == correct { return "\(form), правильно" }
        if form == selected { return "\(form), неверный выбор" }
        return form
    }

    private func startIfReady() {
        guard model.state.string("tab") == "Cloze",
              model.state.string("loadStatus") == "Ready",
              session.isEmpty else { return }
        model.send("tab", "Cloze")
    }

    private func cancelAdvance() {
        advanceTask?.cancel()
        advanceTask = nil
    }

    private func scheduleAdvance(id: String, index: Int) {
        cancelAdvance()
        advanceTask = Task { @MainActor in
            try? await Task.sleep(for: .milliseconds(1500))
            guard !Task.isCancelled,
                  model.state.string("tab") == "Cloze",
                  model.state.record("cloze").int("currentIndex") == index,
                  model.state.record("cloze").record("current").string("id") == id else { return }
            model.send("clozeNext")
        }
    }
}

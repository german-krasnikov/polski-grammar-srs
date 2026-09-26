import XCTest

/// D1 correctness (`Plans/Kotlin/FlipCardRivePlan.md`, replacing FC2-05's flip-swap proof, now
/// moot since D1 removed the native 3D flip): the question card itself is a reveal trigger, exactly
/// once, and — the same class of regression FC2 once found for the old flip's tap gesture — the
/// Question-only "Ответ" Picker's menu-style popup must keep working with a tap-to-reveal gesture
/// attached elsewhere during Question (`FlashCardView.swift`'s `questionHeader`, a sibling of the
/// Picker's `answerControls`, never an ancestor of it).
final class FlipCorrectnessUITests: XCTestCase {
    private func continueIntroductionIfPresent(_ app: XCUIApplication) {
        let next = app.buttons["Перейти к заданию"]
        for _ in 0..<7 {
            if !next.exists || next.isHittable { break }
            app.swipeUp()
        }
        if next.exists { next.tap() }
    }

    /// Tapping the question sentence — not the explicit "Показать ответ" button — reveals the
    /// answer exactly once: absent before, present right after, and the question itself never
    /// disappears (D1 unfolds the answer below it, it never replaces it, unlike the old flip).
    func testTappingTheQuestionRevealsTheAnswerOnce() {
        let app = XCUIApplication()
        app.launch()
        let question = app.staticTexts["To jest moja piękna żona."]
        XCTAssertTrue(question.waitForExistence(timeout: 20))
        continueIntroductionIfPresent(app)
        XCTAssertFalse(app.staticTexts["Эталон"].exists, "must start on the question, unrevealed")

        question.tap()
        XCTAssertTrue(app.staticTexts["Эталон"].waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(question.exists, "the question must stay visible above the unfolded answer")
    }

    /// The historical regression FC2 found for the old flip's tap gesture: an `.onTapGesture`
    /// ancestor of the "Ответ" Picker broke its menu-style popup, making "Напечатать" untappable.
    /// D1's tap-to-reveal gesture lives on a sibling of the Picker, not an ancestor, so switching
    /// to "Напечатать" (and typing into the resulting field) must still work while that gesture is
    /// attached (Question phase, unrevealed — exactly when both exist at once).
    func testQuestionTapGestureDoesNotBreakTheAnswerModePicker() {
        let app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].waitForExistence(timeout: 20))
        continueIntroductionIfPresent(app)
        let mode = app.buttons.matching(NSPredicate(format: "label BEGINSWITH %@", "Ответ,")).firstMatch
        for _ in 0..<7 {
            if mode.exists && mode.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(mode.waitForExistence(timeout: 5), app.debugDescription)
        mode.tap()
        let typed = app.buttons["Напечатать"]
        XCTAssertTrue(typed.waitForExistence(timeout: 5), app.debugDescription)
        typed.tap()
        let answer = app.descendants(matching: .any)["typedAnswer"].firstMatch
        XCTAssertTrue(answer.waitForExistence(timeout: 5), app.debugDescription)
        answer.tap()
        answer.typeText("Widzę moją piękną żonę.")
        XCTAssertEqual(answer.value as? String, "Widzę moją piękną żonę.")
        XCTAssertFalse(app.staticTexts["Эталон"].exists, "typing must never itself trigger a reveal")
    }
}

import XCTest

/// D2 (`Plans/Kotlin/FlipCardRivePlan.md` §12-18): the vocabulary card's whole-panel flip,
/// mirroring the web reference host's `FlipCard`/`installTapGesture` contract natively —
/// see `VocabularyCardView.swift`'s own doc comment for the exact behaviour this proves.
final class VocabularyFlipUITests: XCTestCase {
    private func selectBookIfNeeded(_ app: XCUIApplication) {
        app.buttons["Слова"].firstMatch.tap()
        let select = app.buttons["vocabularySelect-noun.book"]
        for _ in 0..<8 {
            if select.exists && select.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(select.waitForExistence(timeout: 5), app.debugDescription)
        if select.label.hasPrefix("Добавить") { select.tap() }
        for _ in 0..<8 {
            if app.buttons["vocabularyReveal"].isHittable { break }
            app.swipeDown()
        }
    }

    /// Unrevealed: there is no separate "Показать ответ" button any more — a tap on the card
    /// (found the same way the old button's own identifier used to be) reveals *and* flips to
    /// the answer face in one motion, and does so exactly once: the rating buttons that only the
    /// answer face carries are absent before the tap and hittable right after.
    func testTapOnUnrevealedCardRevealsAndFlipsOnce() {
        let app = XCUIApplication()
        app.launch()
        selectBookIfNeeded(app)
        let reveal = app.buttons["vocabularyReveal"]
        XCTAssertTrue(reveal.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertFalse(app.buttons["vocabularyAgain"].exists, "must start on the question face, unrevealed")
        XCTAssertFalse(app.buttons["vocabularyGood"].exists)

        reveal.tap()

        for _ in 0..<6 {
            if app.buttons["vocabularyGood"].isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(app.buttons["vocabularyGood"].isHittable, app.debugDescription)
        XCTAssertTrue(app.buttons["vocabularyAgain"].isHittable)
        let capture = XCTAttachment(screenshot: app.screenshot())
        capture.name = "vocabulary-card-revealed-flip"
        capture.lifetime = .keepAlways
        add(capture)
        // Reveal is exactly once: the reveal identifier's own element is gone now that the card
        // shows the answer face instead of the question face.
        XCTAssertFalse(app.buttons["vocabularyReveal"].exists)
    }

    /// Revealed: tapping the card again (anywhere on the answer face except the rating buttons)
    /// flips it back and forth **purely visually** — it never re-reveals, never dispatches a
    /// second rating, and the rating buttons keep working normally afterwards.
    func testTappingRevealedCardFlipsVisuallyWithoutRating() {
        let app = XCUIApplication()
        app.launch()
        selectBookIfNeeded(app)
        let reveal = app.buttons["vocabularyReveal"]
        XCTAssertTrue(reveal.waitForExistence(timeout: 5), app.debugDescription)
        reveal.tap()
        for _ in 0..<6 {
            if app.buttons["vocabularyGood"].isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(app.buttons["vocabularyGood"].isHittable, app.debugDescription)
        let selectionCount = app.staticTexts.matching(NSPredicate(
            format: "label BEGINSWITH %@", "Мой словарь · "
        )).firstMatch
        XCTAssertTrue(selectionCount.waitForExistence(timeout: 5))
        let before = selectionCount.label

        let answerFace = app.staticTexts["vocabularyAnswerFace"]
        XCTAssertTrue(answerFace.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(answerFace.isHittable, app.debugDescription)
        answerFace.tap()

        // Purely visual: no rating was dispatched, and the same rating buttons are still there.
        XCTAssertEqual(selectionCount.label, before)
        XCTAssertTrue(app.buttons["vocabularyGood"].isHittable, app.debugDescription)
        XCTAssertTrue(app.buttons["vocabularyAgain"].isHittable)

        // "Again" (not "Good"): keeps the shared `noun.book` fixture due again soon, the same
        // choice `testNativeVocabularyRevealAndBinaryRating` makes, so this test never strands a
        // sibling test with a long FSRS interval on the one simulator both share.
        app.buttons["vocabularyAgain"].tap()
        XCTAssertFalse(app.buttons["vocabularyAgain"].waitForExistence(timeout: 2), app.debugDescription)
    }
}

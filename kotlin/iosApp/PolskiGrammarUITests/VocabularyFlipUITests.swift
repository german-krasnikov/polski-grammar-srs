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
    /// the answer face in one motion, and does so exactly once: the answer face's own stable tap
    /// target (`vocabularyAnswerFace`, only ever rendered by [VocabularyCardView]'s back face) is
    /// absent before the tap and hittable right after. D3: there are no rating buttons any more
    /// either — [VocabularyCardView]'s answer face is a `SwipeToRate` gesture surface instead.
    func testTapOnUnrevealedCardRevealsAndFlipsOnce() {
        let app = XCUIApplication()
        app.launch()
        selectBookIfNeeded(app)
        let reveal = app.buttons["vocabularyReveal"]
        XCTAssertTrue(reveal.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertFalse(app.staticTexts["vocabularyAnswerFace"].exists, "must start on the question face, unrevealed")

        reveal.tap()

        let answerFace = app.staticTexts["vocabularyAnswerFace"]
        // See `testNativeVocabularyRevealAndBinaryRating`'s own comment: a bounded wait before any
        // scrolling avoids a swipe landing mid-flip and overscrolling past this single-screen card.
        _ = answerFace.waitForExistence(timeout: 1)
        for _ in 0..<6 {
            if answerFace.exists && answerFace.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(answerFace.isHittable, app.debugDescription)
        let capture = XCTAttachment(screenshot: app.screenshot())
        capture.name = "vocabulary-card-revealed-flip"
        capture.lifetime = .keepAlways
        add(capture)
        // Reveal is exactly once: the reveal identifier's own element is gone now that the card
        // shows the answer face instead of the question face.
        XCTAssertFalse(app.buttons["vocabularyReveal"].exists)
    }

    /// Revealed: tapping the card again (anywhere on the answer face) flips it away **purely
    /// visually** — it never re-reveals and never dispatches a rating. Rating itself is a
    /// separate horizontal-drag gesture (D3, coverage for the drag itself lives in
    /// `PolskiGrammarUITests`'s own `testNativeVocabularyRevealAndBinaryRating`, a `TapGesture`
    /// and a `DragGesture` on the same view never fire for the same touch); a plain tap here must
    /// never trigger it. `noun.book` is never rated in this test, so it stays exactly as due as
    /// before for any sibling test that reuses it (this host's tap-driven flip has no way back to
    /// the answer face once flipped away — unrelated to D3 and untouched here, so this test proves
    /// only the flip-away half of the contract, not a round trip).
    func testTappingRevealedCardFlipsVisuallyWithoutRating() {
        let app = XCUIApplication()
        app.launch()
        selectBookIfNeeded(app)
        let reveal = app.buttons["vocabularyReveal"]
        XCTAssertTrue(reveal.waitForExistence(timeout: 5), app.debugDescription)
        reveal.tap()
        let answerFace = app.staticTexts["vocabularyAnswerFace"]
        _ = answerFace.waitForExistence(timeout: 1)
        for _ in 0..<6 {
            if answerFace.exists && answerFace.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(answerFace.isHittable, app.debugDescription)
        let selectionCount = app.staticTexts.matching(NSPredicate(
            format: "label BEGINSWITH %@", "Мой словарь · "
        )).firstMatch
        XCTAssertTrue(selectionCount.waitForExistence(timeout: 5))
        let before = selectionCount.label

        answerFace.tap()

        // Purely visual: no rating was dispatched, and the tap flipped the card away rather than
        // leaving the answer face (and its swipe-to-rate surface) showing.
        XCTAssertEqual(selectionCount.label, before)
        XCTAssertFalse(answerFace.waitForExistence(timeout: 1),
            "a tap must flip the card away, not leave the answer face showing or dispatch a rating")
    }

    /// I3: the vocabulary list row's select toggle (`checkmark.circle.fill`/`circle`) is a plain
    /// icon-only `Button` — its hit area must meet Apple HIG's 44x44pt minimum, not just the small
    /// glyph, so it can be tapped reliably.
    func testVocabularySelectButtonHas44ptTouchTarget() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Слова"].firstMatch.tap()
        let select = app.buttons["vocabularySelect-noun.book"]
        for _ in 0..<8 {
            if select.exists && select.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(select.waitForExistence(timeout: 5), app.debugDescription)
        // Rounded: SwiftUI's own layout solver reports the 44pt `.frame(minWidth:minHeight:)` back
        // as e.g. 43.99999999999994 (double-precision layout noise, not an actual sub-44pt target)
        // — reproduced consistently, so this rounds rather than masking a real regression.
        XCTAssertGreaterThanOrEqual(select.frame.width.rounded(), 44, app.debugDescription)
        XCTAssertGreaterThanOrEqual(select.frame.height.rounded(), 44, app.debugDescription)
        let capture = XCTAttachment(screenshot: app.screenshot())
        capture.name = "vocabulary-select-touch-target"
        capture.lifetime = .keepAlways
        add(capture)
    }
}

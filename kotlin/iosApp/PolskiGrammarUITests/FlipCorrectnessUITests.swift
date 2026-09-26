import XCTest

/// FC2-05's proof (`Plans/Kotlin/FlipCardRivePlan.md` §12.1, R1): the face swap must happen only
/// past the real 90° point of the flip, in **both** directions — never merely approximated by a
/// fixed timer that assumes a symmetric ease curve. This is a correctness test, not a performance
/// one (contrast `FlipRivePerfUITests.swift`, which this file's helpers deliberately mirror in
/// shape): it drives the app the same way a person would, then samples the accessibility tree at
/// two points bracketing the swap.
///
/// `FlashCardView`'s `#if DEBUG`-only `POLSKI_FLIP_DEBUG_SCALE` environment variable (read once,
/// per the plan's §12.1 wording) slows each 0.25s half down to 15s here, so the whole two-half flip
/// takes 30s — long enough for a reliable mid-flip sample despite `XCUIElement.tap()`'s own
/// synchronization: empirically (confirmed from `xcodebuild test`'s own `t = ...` timeline, not
/// assumed), a `tap()` that kicks off one of these long animations does not return to the test
/// immediately — XCUITest's "wait for app to idle" step itself blocks for several seconds first.
/// [elapsedSince] measures from the tap itself, not from when `tap()` happens to return, so that
/// variable overhead never eats into the intended 40%/65% sampling points. This env var is never
/// set outside a test process — production and a person's own Debug build both always see the
/// unscaled 0.25s halves.
final class FlipCorrectnessUITests: XCTestCase {
    private let debugScale = "60" // 0.25s half -> 15s; 30s total; real swap at t=15s (50%)
    private var halfSeconds: TimeInterval { 15.0 }
    private var totalSeconds: TimeInterval { halfSeconds * 2 }

    private func continueIntroductionIfPresent(_ app: XCUIApplication) {
        let next = app.buttons["Перейти к заданию"]
        for _ in 0..<7 {
            if !next.exists || next.isHittable { break }
            app.swipeUp()
        }
        if next.exists { next.tap() }
    }

    /// A previous test run (e.g. `FlipRivePerfUITests`'s variant A) may have left the persisted
    /// "Движение" preference set to "Сокращённое" on this simulator — `cardMotionReduced` would
    /// then snap every flip instantly regardless of `POLSKI_FLIP_DEBUG_SCALE`, since the scaled
    /// animation path is only reachable when motion is NOT reduced. Force it to "Системное"
    /// unconditionally before this test's own assertions depend on the animated path running.
    private func ensureMotionIsNotReduced(_ app: XCUIApplication) {
        app.buttons["openSettings"].tap()
        XCTAssertTrue(app.staticTexts["Внешний вид"].waitForExistence(timeout: 5), app.debugDescription)
        let row = app.buttons.matching(NSPredicate(format: "label BEGINSWITH %@", "Движение,")).firstMatch
        for _ in 0..<5 {
            if row.exists && row.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(row.waitForExistence(timeout: 5), app.debugDescription)
        row.tap()
        app.buttons["Системное"].firstMatch.tap()
        app.buttons["Готово"].tap()
    }

    /// Sleeps only the remainder of `target` still outstanding since `start` — never a blind fixed
    /// duration — so a slow `tap()` (see the file doc comment) shortens or skips the wait instead of
    /// stacking on top of it and overshooting the intended sample point.
    private func sleepUntilElapsed(_ target: TimeInterval, since start: Date) {
        let remaining = target - Date().timeIntervalSince(start)
        if remaining > 0 { Thread.sleep(forTimeInterval: remaining) }
    }

    /// One round trip, sampled at ~40% (still on [from], the swap must not have happened yet) and
    /// ~65% (already on [to], the swap must be done) of [totalSeconds], measured from [start] (the
    /// instant just before the triggering `tap()`) — the same bracketing FC2-03 (web) does via CDP's
    /// `Animation.setPlaybackRate`, adapted to this host's own `POLSKI_FLIP_DEBUG_SCALE` seam.
    private func assertSwapOnlyPastHalfway(_ app: XCUIApplication, from: XCUIElement, to: XCUIElement, direction: String, since start: Date) {
        sleepUntilElapsed(totalSeconds * 0.4, since: start)
        XCTAssertTrue(from.exists, "\(direction): the pre-swap face must still be mounted before 90°. \(app.debugDescription)")
        XCTAssertFalse(to.exists, "\(direction): the post-swap face (and its text) must not exist before 90°. \(app.debugDescription)")

        sleepUntilElapsed(totalSeconds * 0.65, since: start)
        XCTAssertTrue(to.waitForExistence(timeout: 5), "\(direction): the post-swap face must exist once past 90°. \(app.debugDescription)")
        XCTAssertFalse(from.exists, "\(direction): the pre-swap face must be unmounted once past 90°. \(app.debugDescription)")

        sleepUntilElapsed(totalSeconds * 1.1, since: start) // let it fully settle before the next round trip
    }

    /// Covers both directions R1 requires: the auto-flip on `Reveal` (front -> back, the exact
    /// case FC2-01 found genuinely broken on the web host) and a manual tap flip back (back ->
    /// front) — the same two `setFlipped(...)` call sites in `FlashCardView.swift`.
    func testAnswerRevealSwapsFacesOnlyPastTheHalfwayPointInBothDirections() {
        let app = XCUIApplication()
        app.launchEnvironment["POLSKI_FLIP_DEBUG_SCALE"] = debugScale
        app.launch()
        XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].waitForExistence(timeout: 20))
        continueIntroductionIfPresent(app)
        ensureMotionIsNotReduced(app)

        let front = app.staticTexts["Исходное предложение"]
        let back = app.staticTexts["Эталон"]
        let reveal = app.buttons["revealAnswer"]
        for _ in 0..<10 {
            if reveal.exists && reveal.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(reveal.waitForExistence(timeout: 10), app.debugDescription)
        XCTAssertTrue(front.exists, "must start on the question face")

        let revealStart = Date()
        reveal.tap() // triggers FlashCardView's auto-flip: onChange(of: state.phase) -> setFlipped(true)
        assertSwapOnlyPastHalfway(app, from: front, to: back, direction: "front -> back (auto-reveal)", since: revealStart)

        let backTapStart = Date()
        back.tap() // manual flip back: backFace's onTapGesture -> setFlipped(false)
        assertSwapOnlyPastHalfway(app, from: back, to: front, direction: "back -> front (manual tap)", since: backTapStart)
    }
}

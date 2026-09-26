import XCTest

/// Test-only performance measurement harness for FlipCardRivePlan.md §5 (iOS variants A/no
/// animation, B/native flip only, C/flip+Rive). This file adds NO production code — it drives the
/// existing app UI (Settings picker "Движение", the #if DEBUG long-press measurement toggle) the
/// same way a person would, and prints `PERF_MARK` lines with wall-clock timestamps that a
/// companion shell script (Plans/Kotlin/artifacts/flip-rive/ios/) parses out of the `xcodebuild
/// test` log, correlating them with an external `ps`-based RSS/CPU sampler of the live app
/// process (XCTest's own XCTMemoryMetric/XCTCPUMetric(application:) report to the .xcresult only,
/// not back to test code, so they can't be correlated against named checkpoints like "after 50
/// cycles" from inside the test itself).
final class FlipRivePerfUITests: XCTestCase {
    private func mark(_ label: String) {
        print("PERF_MARK \(label) \(Date().timeIntervalSince1970)")
    }

    private func continueIntroductionIfPresent(_ app: XCUIApplication) {
        let next = app.buttons["Перейти к заданию"]
        for _ in 0..<7 {
            if !next.exists || next.isHittable { break }
            app.swipeUp()
        }
        if next.exists { next.tap() }
    }

    private func resetTrainingProgress(_ app: XCUIApplication) {
        app.buttons["Прогресс"].firstMatch.tap()
        let reset = app.buttons["Сбросить прогресс"]
        for _ in 0..<8 {
            if reset.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(reset.waitForExistence(timeout: 5), app.debugDescription)
        reset.tap()
        let confirmation = app.alerts["Сбросить весь прогресс?"]
        XCTAssertTrue(confirmation.waitForExistence(timeout: 5), app.debugDescription)
        confirmation.buttons["Сбросить"].tap()
        let notice = app.alerts["Сообщение"]
        if notice.waitForExistence(timeout: 2) { notice.buttons["ОК"].tap() }
        app.buttons["Тренировка"].firstMatch.tap()
    }

    private func setMotionReduced(_ app: XCUIApplication, reduced: Bool) {
        app.buttons["openSettings"].tap()
        XCTAssertTrue(app.staticTexts["Внешний вид"].waitForExistence(timeout: 5), app.debugDescription)
        // "Движение" is a Menu-style Picker: its accessibility label is "Движение, <current value>",
        // not the bare title (see e.g. the existing "Ответ, ..." pattern in PolskiGrammarUITests.swift).
        let row = app.buttons.matching(NSPredicate(format: "label BEGINSWITH %@", "Движение,")).firstMatch
        for _ in 0..<5 {
            if row.exists && row.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(row.waitForExistence(timeout: 5), app.debugDescription)
        row.tap()
        let target = reduced ? "Сокращённое" : "Системное"
        let option = app.buttons[target].firstMatch
        XCTAssertTrue(option.waitForExistence(timeout: 5), app.debugDescription)
        option.tap()
        app.buttons["Готово"].tap()
    }

    /// Toggles the #if DEBUG-only `polski.debug.riveDisabled` measurement flag (variant B) via the
    /// same long-press-on-version-text affordance a person uses; leaves it OFF for variant A/C.
    private func setRiveDisabledForMeasurement(_ app: XCUIApplication, disabled: Bool) {
        app.buttons["openSettings"].tap()
        let toggleLabel = app.staticTexts["Тестовая версия"]
        for _ in 0..<5 {
            if toggleLabel.exists && toggleLabel.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(toggleLabel.waitForExistence(timeout: 5), app.debugDescription)
        let currentlyOn = app.staticTexts["Замер: Rive-эффекты отключены (вариант B)"].exists
        if currentlyOn != disabled { toggleLabel.press(forDuration: 1.0) }
        app.buttons["Готово"].tap()
    }

    /// Beyond the intro overlay, a long run of rate cycles can also hit two milestone phases that
    /// replace the card: "ChainComplete" (offers "К повторениям"/"Следующий набор слов") and
    /// "NoDue" (offers "Потренировать цепочку" once the schedule has nothing due right now) — see
    /// `PolskiGrammarApp.swift`'s `switch state.string("phase")`. Both are dismissed the same way a
    /// person would, to get back to a revealable card.
    private func revealCurrentCard(_ app: XCUIApplication) {
        let reveal = app.buttons["revealAnswer"]
        let backToReviews = app.buttons["К повторениям"]
        let trainChain = app.buttons["Потренировать цепочку"]
        for _ in 0..<15 {
            if reveal.exists && reveal.isHittable { break }
            continueIntroductionIfPresent(app)
            if backToReviews.exists && backToReviews.isHittable { backToReviews.tap(); continue }
            if trainChain.exists && trainChain.isHittable { trainChain.tap(); continue }
            app.swipeUp()
        }
        XCTAssertTrue(reveal.waitForExistence(timeout: 10), app.debugDescription)
        reveal.tap()
        let back = app.staticTexts["Эталон"]
        for _ in 0..<7 {
            if back.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(back.waitForExistence(timeout: 5), app.debugDescription)
    }

    /// One flip round trip: back-face -> front-face -> back-face, ending in the same state it
    /// started in so the next iteration is repeatable.
    private func flipRoundTrip(_ app: XCUIApplication) {
        let back = app.staticTexts["Эталон"]
        let front = app.staticTexts["Исходное предложение"]
        XCTAssertTrue(back.exists, app.debugDescription)
        back.tap()
        for _ in 0..<7 {
            if front.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(front.waitForExistence(timeout: 5), app.debugDescription)
        front.tap()
        for _ in 0..<7 {
            if back.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(back.waitForExistence(timeout: 5), app.debugDescription)
    }

    /// Swipe-rates the currently revealed card (alternating direction like the existing
    /// `testBinaryRatingSwipesAdvanceOnceInEachDirection`), then re-reveals the next card so the
    /// next iteration starts from the same "back visible" state.
    private func swipeRateAndReveal(_ app: XCUIApplication, left: Bool) {
        let rating = app.descendants(matching: .any)["ratingSwipeArea"].firstMatch
        for _ in 0..<6 {
            if rating.exists && rating.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(rating.isHittable, app.debugDescription)
        if left { rating.swipeLeft() } else { rating.swipeRight() }
        revealCurrentCard(app)
    }

    private func runVariant(_ app: XCUIApplication, name: String) {
        resetTrainingProgress(app)
        revealCurrentCard(app)

        mark("\(name)_baseline")
        for i in 0..<5 {
            mark("\(name)_flip\(i)_start")
            flipRoundTrip(app)
            mark("\(name)_flip\(i)_end")
        }
        for i in 0..<5 {
            mark("\(name)_rate\(i)_start")
            swipeRateAndReveal(app, left: i % 2 == 0)
            mark("\(name)_rate\(i)_end")
        }
    }

    func testPerfVariantA_NoAnimation() {
        let app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].waitForExistence(timeout: 20))
        setRiveDisabledForMeasurement(app, disabled: false)
        setMotionReduced(app, reduced: true)
        runVariant(app, name: "variantA")
        setMotionReduced(app, reduced: false) // leave the shared UserDefaults clean for the next run
    }

    func testPerfVariantB_NativeFlipOnly() {
        let app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].waitForExistence(timeout: 20))
        setMotionReduced(app, reduced: false)
        setRiveDisabledForMeasurement(app, disabled: true)
        runVariant(app, name: "variantB")
        setRiveDisabledForMeasurement(app, disabled: false)
    }

    func testPerfVariantC_FlipPlusRive() {
        let app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].waitForExistence(timeout: 20))
        setMotionReduced(app, reduced: false)
        setRiveDisabledForMeasurement(app, disabled: false)
        runVariant(app, name: "variantC")

        // Leak-detection loop: 50 more rate cycles past the 5 already counted above, with a
        // checkpoint every 10 for the external memory sampler to correlate against.
        for i in 0..<50 {
            if i % 10 == 0 { mark("variantC_cycle\(i)") }
            swipeRateAndReveal(app, left: i % 2 == 0)
        }
        mark("variantC_cycle50_done")
    }

    /// App launch regression (XCTApplicationLaunchMetric), independent of variant — the debug
    /// flag/motion setting only affect in-card behavior, not launch (RiveViewModels are created
    /// lazily on first accepted rating per FlipCardRivePlan.md §10, not at launch).
    func testPerfAppLaunch() {
        measure(metrics: [XCTApplicationLaunchMetric()]) {
            XCUIApplication().launch()
        }
    }
}

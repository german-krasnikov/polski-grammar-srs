import XCTest
import UIKit

final class PolskiGrammarUITests: XCTestCase {
    func testNativeAppearanceSettingsKeepsTrainingCard() {
        let app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].waitForExistence(timeout: 20))
        let settings = app.buttons["openSettings"]
        XCTAssertTrue(settings.waitForExistence(timeout: 5))
        settings.tap()
        XCTAssertTrue(app.staticTexts["Внешний вид"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Тема")).firstMatch.exists)
        XCTAssertFalse(app.staticTexts["Liquid Glass"].exists)
        let capture = XCTAttachment(screenshot: app.screenshot())
        capture.name = "native-appearance-settings"
        capture.lifetime = .keepAlways
        add(capture)
        app.buttons["Готово"].tap()
        XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].exists)
    }

    /// D5: the "Анимации" master switch defaults on and persists off across a relaunch, via the
    /// same `IosPreferencesSession` JSON document `NSUserDefaults`-backs every other setting with
    /// (`testNativeAppearanceSettingsKeepsTrainingCard` above covers the sibling pickers in the
    /// same section).
    func testAnimationsToggleDefaultsOnAndPersistsOffAcrossRelaunch() {
        let app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].waitForExistence(timeout: 20))
        app.buttons["openSettings"].tap()
        let toggle = app.switches["Анимации"]
        XCTAssertTrue(toggle.waitForExistence(timeout: 5))
        XCTAssertEqual(toggle.value as? String, "1")
        tapAnimationsSwitch(toggle)
        XCTAssertEqual(toggle.value as? String, "0")
        app.buttons["Готово"].tap()

        app.terminate()
        app.launch()
        XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].waitForExistence(timeout: 20))
        app.buttons["openSettings"].tap()
        let toggleAfterRelaunch = app.switches["Анимации"]
        XCTAssertTrue(toggleAfterRelaunch.waitForExistence(timeout: 5))
        XCTAssertEqual(toggleAfterRelaunch.value as? String, "0")
        tapAnimationsSwitch(toggleAfterRelaunch) // restore the default so later tests in the same run see it on.
    }

    /// Form/List wraps a `Toggle` row in an outer, whole-row accessibility element (for VoiceOver)
    /// that shares the same label and `Switch` type as the real `UISwitch` nested inside it — so
    /// `app.switches["Анимации"]` resolves to that outer wrapper, and `.tap()` on it synthesizes a
    /// coordinate tap at the row's center, which lands left of the actual control and does nothing.
    /// Descending into its own `switches` query reaches the real, trailing-edge switch instead.
    private func tapAnimationsSwitch(_ outerRow: XCUIElement) {
        outerRow.switches.firstMatch.tap()
    }

    /// C1: a progress save failure must stay visible above every tab (not just Progress), with
    /// the export action reachable, even though loadStatus stays Ready. `IosProgressRepository`
    /// exposes an opt-in UI-test seam (`POLSKI_UITEST_FORCE_SAVE_FAILURE`) to force that failure
    /// deterministically instead of relying on real disk pressure.
    func testSaveFailureShowsErrorBannerOnEveryTabWithExportReachable() {
        let app = XCUIApplication()
        app.launchEnvironment["POLSKI_UITEST_FORCE_SAVE_FAILURE"] = "1"
        app.launch()
        XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].waitForExistence(timeout: 20))
        continueIntroductionIfPresent(app)
        let reveal = app.buttons["revealAnswer"]
        for _ in 0..<7 {
            if reveal.exists && reveal.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(reveal.isHittable)
        reveal.tap()
        rateViaSwipe(app, good: true)

        let bannerText = "Не удалось сохранить прогресс. Экспортируй JSON перед закрытием."
        XCTAssertTrue(app.staticTexts[bannerText].waitForExistence(timeout: 10), app.debugDescription)
        XCTAssertTrue(app.buttons["errorBannerExport"].exists, app.debugDescription)

        for tab in ["Матрица", "Прогресс", "Слова", "Тренировка"] {
            app.buttons[tab].firstMatch.tap()
            XCTAssertTrue(app.staticTexts[bannerText].waitForExistence(timeout: 5), "\(tab): \(app.debugDescription)")
            XCTAssertTrue(app.buttons["errorBannerExport"].exists, tab)
        }
    }

    #if S6_PICKER_ACCEPTANCE
    func testS6VocabularySystemFileExporterSaves() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Слова"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Мой словарь · 2"].waitForExistence(timeout: 10))
        let export = app.buttons["Экспортировать словарь JSON"]
        XCTAssertTrue(export.waitForExistence(timeout: 10))
        export.tap()
        let save = app.buttons.matching(NSPredicate(format: "label IN %@", ["Save", "Сохранить"])).firstMatch
        XCTAssertTrue(save.waitForExistence(timeout: 10), app.debugDescription)
        let localFiles = app.staticTexts.matching(NSPredicate(
            format: "label IN %@", ["On My iPhone", "On My iPad"]
        )).firstMatch
        XCTAssertTrue(localFiles.exists, app.debugDescription)
        save.tap()
        XCTAssertFalse(save.waitForExistence(timeout: 2))
    }

    func testS6VocabularySystemFileImporterRestoresFourCards() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Слова"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Мой словарь · 0"].waitForExistence(timeout: 10))
        app.buttons["Импортировать словарь JSON"].tap()
        let exported = app.cells.matching(NSPredicate(
            format: "identifier BEGINSWITH %@", "polski-vocabulary-pl-ru.json"
        )).firstMatch
        XCTAssertTrue(exported.waitForExistence(timeout: 10), app.debugDescription)
        exported.tap()
        print("S6 AFTER FILE TAP: \(app.debugDescription)")
        let restored = app.staticTexts["Мой словарь · 2"]
        XCTAssertTrue(restored.waitForExistence(timeout: 10), app.debugDescription)
        if app.alerts["Сообщение"].exists { app.alerts["Сообщение"].buttons["ОК"].tap() }
        app.terminate()
        app.launch()
        app.buttons["Слова"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Мой словарь · 2"].waitForExistence(timeout: 10))
    }

    func testS6SafariDownloadsInvalidPairIntoFiles() {
        let safari = XCUIApplication(bundleIdentifier: "com.apple.mobilesafari")
        safari.activate()
        let download = safari.buttons["Download"]
        XCTAssertTrue(download.waitForExistence(timeout: 10), safari.debugDescription)
        safari.coordinate(withNormalizedOffset: CGVector(dx: 0.80, dy: 0.53)).tap()
        XCTAssertFalse(download.waitForExistence(timeout: 2), safari.debugDescription)
    }

    func testS6VocabularyRecoveryExportsRawThroughFiles() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Слова"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Экспортируйте исходный JSON и импортируйте исправленный словарь."].waitForExistence(timeout: 10))
        app.buttons["Экспортировать словарь JSON"].tap()
        let save = app.buttons["Save"]
        XCTAssertTrue(save.waitForExistence(timeout: 10), app.debugDescription)
        save.tap()
        let replace = app.buttons["Replace"]
        if replace.waitForExistence(timeout: 2) { replace.tap() }
    }

    func testS6VocabularyRecoveryImportsRepairedFile() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Слова"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Экспортируйте исходный JSON и импортируйте исправленный словарь."].waitForExistence(timeout: 10))
        app.buttons["Импортировать словарь JSON"].tap()
        let repaired = app.cells.matching(NSPredicate(
            format: "identifier BEGINSWITH %@", "polski-vocabulary-pl-ru.json"
        )).firstMatch
        XCTAssertTrue(repaired.waitForExistence(timeout: 10), app.debugDescription)
        repaired.tap()
        XCTAssertTrue(app.staticTexts["Мой словарь · 2"].waitForExistence(timeout: 10), app.debugDescription)
        if app.alerts["Сообщение"].exists { app.alerts["Сообщение"].buttons["ОК"].tap() }
        app.terminate()
        app.launch()
        app.buttons["Слова"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Мой словарь · 2"].waitForExistence(timeout: 10))
    }

    func testS6VocabularySystemFileImporterRejectsCurrentFile() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Слова"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Мой словарь · 2"].waitForExistence(timeout: 10))
        app.buttons["Импортировать словарь JSON"].tap()
        let invalid = app.cells.matching(NSPredicate(
            format: "identifier BEGINSWITH %@", "polski-vocabulary-pl-ru.json"
        )).firstMatch
        XCTAssertTrue(invalid.waitForExistence(timeout: 10), app.debugDescription)
        invalid.tap()
        XCTAssertTrue(app.alerts["Сообщение"].waitForExistence(timeout: 10), app.debugDescription)
        XCTAssertFalse(app.staticTexts["Словарь импортирован"].exists)
        app.alerts["Сообщение"].buttons["ОК"].tap()
        XCTAssertTrue(app.staticTexts["Мой словарь · 2"].exists)
    }
    #endif

    func testS6VocabularyFileImporterCancellationKeepsSelection() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Слова"].firstMatch.tap()
        let selection = app.staticTexts.matching(NSPredicate(
            format: "label BEGINSWITH %@", "Мой словарь · "
        )).firstMatch
        XCTAssertTrue(selection.waitForExistence(timeout: 10))
        let before = selection.label
        app.buttons["Импортировать словарь JSON"].tap()
        // M17: on iOS 26 the system document picker exposes Cancel as a Link, not a Button.
        let cancel = app.descendants(matching: .any)["Отменить"].firstMatch
        XCTAssertTrue(cancel.waitForExistence(timeout: 10), app.debugDescription)
        cancel.tap()
        XCTAssertEqual(selection.label, before)
    }

    /// M10: an answerMode picked in Settings must be restored on the next launch, the same way
    /// explanationMethod already is, not silently fall back to the Oral default.
    func testAnswerModeChosenInSettingsSurvivesAppRestart() {
        let app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].waitForExistence(timeout: 20))
        continueIntroductionIfPresent(app)
        let settings = app.buttons["openSettings"]
        XCTAssertTrue(settings.waitForExistence(timeout: 5))
        settings.tap()
        // Not a label-based query: the training screen underneath this sheet has its own
        // "Ответ" picker (FlashCardView) that stays mounted and shares the label, so
        // `label BEGINSWITH "Ответ,"` matched it instead of the Settings one 3/3 on iPhone.
        let mode = app.descendants(matching: .any)["settingsAnswerModePicker"].firstMatch
        for _ in 0..<7 {
            if mode.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(mode.waitForExistence(timeout: 5), app.debugDescription)
        mode.tap()
        app.buttons["Напечатать"].tap()
        app.buttons["Готово"].tap()
        let answer = app.descendants(matching: .any)["typedAnswer"].firstMatch
        XCTAssertTrue(answer.waitForExistence(timeout: 5), app.debugDescription)

        app.terminate()
        app.launch()
        XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].waitForExistence(timeout: 20))
        continueIntroductionIfPresent(app)
        let answerAfterRestart = app.descendants(matching: .any)["typedAnswer"].firstMatch
        XCTAssertTrue(answerAfterRestart.waitForExistence(timeout: 10), app.debugDescription)

        // Restore the default so later tests in this run see the usual Oral experience.
        settings.tap()
        mode.tap()
        app.buttons["Вслух"].tap()
        app.buttons["Готово"].tap()
    }

    /// D3: rating is a whole-card swipe on touch — there is no `rateGood`/`rateAgain` button any
    /// more. Scrolls the swipe zone (shared identifier `ratingSwipeArea` on both the training and
    /// the vocabulary card, `FlashCardView`/`VocabularyCardView`'s own `SwipeToRate` call sites)
    /// into view and commits a rating via a coordinate-based drag — every other test's plain
    /// "advance one review" mechanism; `testBinaryRatingSwipesAdvanceOnceInEachDirection` is the
    /// one test that verifies the direction contract itself.
    ///
    /// A coordinate drag (not the `XCUIElement.swipeLeft()/swipeRight()` convenience) on purpose:
    /// a scenario with extra content above the card (a test that also shows the reference table)
    /// can leave this zone sitting right at the scrollview's own visible edge — still `isHittable`,
    /// but `swipeLeft()/swipeRight()` still failed there ("visible frame is empty") since it
    /// re-derives the element's clipped frame at synthesis time, which can round to zero right at
    /// that edge. A drag between two explicit offsets inside the element's own frame doesn't hit
    /// that path — reproduced failing with `swipeLeft()` there, passing with this, before writing it.
    private func rateViaSwipe(_ app: XCUIApplication, good: Bool) {
        // `staticTexts[...]` (its own concrete type — confirmed in a debug snapshot), not
        // `descendants(matching: .any)[...]`: on a reference-table-heavy screen (e.g. the reference
        // table dance in `testFirstMethodIntroductionKeepsReferenceAnswerHiddenUntilContinue`) the
        // accessibility tree is huge (the case-declension table alone is 35+ elements), and the
        // broad `.any` descendant query over that whole tree reproduced unreliable there — `exists`
        // sometimes staying false for many seconds with no scrolling able to change that, on the
        // very same screen a narrower, cheaper query resolves quickly and consistently for.
        let zone = app.staticTexts["ratingSwipeArea"].firstMatch
        // The revealed panel can land mounted but scrolled *just past the top edge* right after
        // `reveal.tap()` — a debug snapshot showed a small negative-Y frame there — instead of the
        // usual case every other call site's plain forward `swipeUp()` hunt already handles, where
        // it sits below the fold. A full-screen `swipeUp()` is the wrong tool to find out which: it
        // moves *away* from a that-close-above zone by far more than its own frame, unmounting it
        // from the lazy Form before this ever gets a reading — reproduced: once that happens, the
        // zone stops existing at all for the rest of a one-directional hunt, no matter the budget,
        // because the reversal below only ever triggers *after* first seeing it exist. A few small,
        // coordinate-based nudges (~15% of the screen — far short of its own frame height) in
        // alternating directions probe both sides first without that risk; once one direction finds
        // it, or neither does (the common case — it is below the fold and just needs the usual
        // longer hunt), the plain forward `swipeUp()` every other call site already uses takes over.
        func nudge(down: Bool) {
            let (a, b): (CGFloat, CGFloat) = down ? (0.35, 0.65) : (0.65, 0.35)
            let start = app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: a))
            let end = app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: b))
            start.press(forDuration: 0.02, thenDragTo: end)
        }
        for i in 0..<6 {
            if zone.exists && zone.isHittable { break }
            nudge(down: i % 2 == 0)
        }
        for _ in 0..<15 {
            if zone.exists && zone.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(zone.isHittable, app.debugDescription)
        let start = zone.coordinate(withNormalizedOffset: CGVector(dx: good ? 0.15 : 0.85, dy: 0.5))
        let end = zone.coordinate(withNormalizedOffset: CGVector(dx: good ? 0.95 : 0.05, dy: 0.5))
        start.press(forDuration: 0.05, thenDragTo: end)
    }

    private func continueIntroductionIfPresent(_ app: XCUIApplication) {
        let next = app.buttons["Перейти к заданию"]
        for _ in 0..<7 {
            if !next.exists || next.isHittable { break }
            app.swipeUp()
        }
        if !next.exists {
            // D3's swipe-to-rate commits the domain rating only after its fly-out animation
            // finishes (`SwipeToRate`, mirroring the web host's own commit-then-settle order) —
            // a caller right after a swipe must give the next card (which might need its own
            // introduction) a brief moment to actually mount, not assume it is already there.
            // 1.5s, not the fly-out's own ~0.22s: a generous margin over it, not a tight one —
            // reproduced an occasional miss at 0.5s under load, never at 1.5s.
            _ = next.waitForExistence(timeout: 1.5)
        }
        // A bounded tap-and-verify retry, not a single unconditional tap: reproduced this
        // specific button occasionally swallowing one synthesized tap with no effect (the
        // accessibility hierarchy snapshot briefly reports an "Automation type mismatch" —
        // Button vs PopUpButton — right around here once anything elsewhere on screen carries a
        // custom `.accessibilityActions`, e.g. the answer panel's own VoiceOver rating actions),
        // leaving the intro screen showing with `next` still present afterwards. Retrying the tap
        // up to 3 times, each time re-checking `next.exists`, costs nothing when the first tap
        // already worked (the loop exits immediately) and reliably recovers when it didn't.
        for _ in 0..<3 {
            guard next.exists else { return }
            next.tap()
            if !next.waitForExistence(timeout: 1) { return }
        }
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
    }

    func testFirstMethodIntroductionKeepsReferenceAnswerHiddenUntilContinue() {
        let app = XCUIApplication()
        app.launch()
        resetTrainingProgress(app)
        app.buttons["Тренировка"].firstMatch.tap()
        XCTAssertTrue(app.buttons["continueIntroduction"].waitForExistence(timeout: 20))
        XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].exists)
        XCTAssertFalse(app.staticTexts["Widzę moją piękną żonę."].exists)
        let reference = app.buttons["Таблица под рукой"]
        for _ in 0..<7 {
            if reference.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(reference.exists)
        XCTAssertFalse(reference.isEnabled)
        let target = app.descendants(matching: .any)["Было: moja piękna żona; Стало: moją piękną żonę"].firstMatch
        XCTAssertFalse(target.exists)
        let next = app.buttons["continueIntroduction"]
        for _ in 0..<7 {
            if next.isHittable { break }
            app.swipeDown()
        }
        XCTAssertTrue(next.isHittable)
        next.tap()
        for _ in 0..<10 {
            if reference.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(reference.waitForExistence(timeout: 5))
        XCTAssertTrue(reference.isEnabled)
        reference.tap()
        for _ in 0..<7 {
            if target.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(target.waitForExistence(timeout: 5))

        let reveal = app.buttons["revealAnswer"]
        for _ in 0..<7 {
            if reveal.isHittable { break }
            app.swipeDown()
        }
        XCTAssertTrue(reveal.exists)
        reveal.tap()
        rateViaSwipe(app, good: true)
        XCTAssertTrue(next.waitForExistence(timeout: 5))
        let neutral = app.buttons["Таблица под рукой"]
        for _ in 0..<10 {
            if neutral.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(neutral.exists)
        XCTAssertFalse(neutral.isEnabled)
        XCTAssertFalse(app.buttons["Скрыть таблицу"].exists)
        XCTAssertFalse(target.exists)
        for _ in 0..<7 {
            if next.isHittable { break }
            app.swipeDown()
        }
        next.tap()
        let restored = app.buttons["Скрыть таблицу"]
        for _ in 0..<10 {
            if restored.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(restored.waitForExistence(timeout: 5))
        XCTAssertTrue(restored.isEnabled)
        for _ in 0..<7 {
            if target.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(target.exists)
    }

    func testMethodSwitchKeepsTypedDraftThroughRevealAndOneReview() {
        let app = XCUIApplication()
        app.launch()
        resetTrainingProgress(app)
        app.buttons["Тренировка"].firstMatch.tap()
        XCTAssertTrue(app.buttons["continueIntroduction"].waitForExistence(timeout: 20))
        continueIntroductionIfPresent(app)
        let method = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Стиль объяснений")).firstMatch
        for _ in 0..<7 {
            if method.isHittable { break }
            app.swipeDown()
        }
        XCTAssertTrue(method.isHittable)
        if !method.label.contains("Через правило") {
            method.tap()
            let ruleFirst = app.descendants(matching: .any)["Через правило"].firstMatch
            XCTAssertTrue(ruleFirst.waitForExistence(timeout: 5))
            ruleFirst.tap()
        }
        XCTAssertTrue(method.label.contains("Через правило"))
        let mode = app.buttons.matching(NSPredicate(format: "label BEGINSWITH %@", "Ответ,")).firstMatch
        for _ in 0..<7 {
            if mode.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(mode.waitForExistence(timeout: 5))
        mode.tap()
        app.buttons["Напечатать"].tap()
        let answer = app.descendants(matching: .any)["typedAnswer"].firstMatch
        XCTAssertTrue(answer.waitForExistence(timeout: 5))
        answer.tap()
        answer.typeText("Moja proba")

        #if DIRECT_PICKER_ACCEPTANCE
        XCTAssertTrue(method.isHittable, app.debugDescription)
        XCTAssertTrue(app.keyboards.firstMatch.exists, app.debugDescription)
        #else
        app.swipeDown()
        for _ in 0..<7 {
            if method.isHittable { break }
            app.swipeDown()
        }
        #endif
        XCTAssertTrue(method.isHittable)
        #if !DIRECT_PICKER_ACCEPTANCE
        XCTAssertFalse(app.keyboards.firstMatch.exists, app.debugDescription)
        #endif
        let pinnedReveal = app.buttons["revealAnswer"]
        if pinnedReveal.exists && pinnedReveal.isHittable {
            XCTAssertFalse(method.frame.intersects(pinnedReveal.frame))
        }
        print("METHOD_BEFORE_TAP: method=\(method.frame), keyboard=\(app.keyboards.firstMatch.exists), typed=\(answer.exists)")
        method.tap()
        let situations = app.descendants(matching: .any)["Через ситуацию"].firstMatch
        XCTAssertTrue(situations.waitForExistence(timeout: 5), app.debugDescription)
        situations.tap()
        for _ in 0..<8 {
            if answer.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(answer.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertEqual(answer.value as? String, "Moja proba")

        let reveal = app.buttons["revealAnswer"]
        for _ in 0..<7 {
            if reveal.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(reveal.exists)
        reveal.tap()
        XCTAssertTrue(app.staticTexts["Widzę moją piękną żonę."].waitForExistence(timeout: 5))
        let revealedCapture = XCTAttachment(screenshot: app.screenshot())
        revealedCapture.name = "method-cycle-revealed"
        revealedCapture.lifetime = .keepAlways
        add(revealedCapture)
        let frozen = app.staticTexts["Moja proba"]
        for _ in 0..<8 {
            if frozen.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(frozen.exists)

        for _ in 0..<7 {
            if method.isHittable { break }
            app.swipeDown()
        }
        XCTAssertTrue(method.isHittable)
        method.tap()
        app.descendants(matching: .any)["Через правило"].firstMatch.tap()
        for _ in 0..<8 {
            if frozen.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(frozen.exists)
        rateViaSwipe(app, good: true)
        app.buttons["Прогресс"].firstMatch.tap()
        let total = app.descendants(matching: .any)["totalReviews"].firstMatch
        for _ in 0..<8 {
            if total.exists { break }
            app.swipeDown()
        }
        XCTAssertTrue(total.waitForExistence(timeout: 5))
        XCTAssertTrue(total.label.hasSuffix(", 1"), total.label)
    }

    func testNativeContrastSupportPairsHaveOrderedAccessibleNames() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Матрица"].firstMatch.tap()
        for name in [
            "Было: moja żona; Стало: moją żonę",
            "Было: moja żona; Стало: moją żoną",
            "Было: żona; Стало: żonie",
            "Было: moją żonę; Стало: jego żonę",
            "Было: moją żonę; Стало: ich żonę",
        ] {
            let pair = app.descendants(matching: .any)[name].firstMatch
            for _ in 0..<18 {
                if pair.exists { break }
                app.swipeUp()
            }
            XCTAssertTrue(pair.waitForExistence(timeout: 5), name)
        }
    }

    func testNativeGeneratedCaseContrastKeepsFullWordsInSemantics() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Матрица"].firstMatch.tap()
        let section = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Раздел")).firstMatch
        XCTAssertTrue(section.waitForExistence(timeout: 10))
        section.tap()
        app.buttons["Падежи и окончания"].tap()
        let pair = app.descendants(matching: .any)["Было: moja piękna żona; Стало: moją piękną żonę"].firstMatch
        for _ in 0..<12 {
            if pair.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(pair.waitForExistence(timeout: 5))
    }

    func testInventoryAuthoredMatrixAndVocabularyCopyOnSimulator() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Матрица"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Один небольшой словарь. Видно, что меняется при каждой операции."].waitForExistence(timeout: 10))

        app.buttons["Слова"].firstMatch.tap()
        let instruction = app.staticTexts.matching(NSPredicate(format: "label CONTAINS %@", "Исходные 32 карточки прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR.")).firstMatch
        for _ in 0..<8 {
            if instruction.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(instruction.waitForExistence(timeout: 5))

        let picker = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Подборка")).firstMatch
        for _ in 0..<3 {
            if picker.isHittable { break }
            app.swipeDown()
        }
        XCTAssertTrue(picker.isHittable)
        picker.tap()
        let top100 = app.descendants(matching: .any).matching(NSPredicate(format: "label == %@", "Топ 100")).firstMatch
        XCTAssertTrue(top100.waitForExistence(timeout: 5))
        top100.tap()
        let coverage = app.staticTexts["Готово 7/100 · недоступно 93"]
        for _ in 0..<8 {
            if coverage.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(coverage.waitForExistence(timeout: 5))
        let unavailable = app.buttons["vocabularySelect-"].firstMatch
        for _ in 0..<8 {
            if unavailable.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(unavailable.waitForExistence(timeout: 5))
        XCTAssertFalse(unavailable.isEnabled)
    }

    func testNativeChainCompletionShowsFiveAnswersAndKeepsFiveRatings() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Прогресс"].firstMatch.tap()
        let total = app.descendants(matching: .any)["totalReviews"].firstMatch
        XCTAssertTrue(total.waitForExistence(timeout: 10))
        let before = Int(total.label.split(separator: ",").last?.trimmingCharacters(in: .whitespaces) ?? "") ?? -1
        XCTAssertGreaterThanOrEqual(before, 0)
        app.buttons["Тренировка"].firstMatch.tap()

        for step in 0..<5 {
            let summary = app.staticTexts["\(step) / 5 · Вижу → Прошлое → Отрицание → Владелец → Говорю о"]
            for _ in 0..<5 {
                if summary.exists { break }
                app.swipeDown()
            }
            XCTAssertTrue(summary.waitForExistence(timeout: 5))
            continueIntroductionIfPresent(app)
            let reveal = app.buttons["revealAnswer"]
            for _ in 0..<7 {
                if reveal.exists && reveal.isHittable { break }
                app.swipeUp()
            }
            XCTAssertTrue(reveal.isHittable)
            reveal.tap()
            rateViaSwipe(app, good: true)
        }
        let completed = app.staticTexts["Цепочка завершена"]
        for _ in 0..<8 {
            if completed.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(completed.waitForExistence(timeout: 5))
        let finalSummary = app.staticTexts["5 / 5 · Вижу → Прошлое → Отрицание → Владелец → Говорю о"]
        for _ in 0..<8 {
            if finalSummary.exists { break }
            app.swipeDown()
        }
        XCTAssertTrue(finalSummary.waitForExistence(timeout: 5))
        for sentence in [
            "1. Widzę moją piękną żonę.",
            "2. Widziałem moją piękną żonę.",
            "3. Nie widziałem mojej pięknej żony.",
            "4. Nie widziałem ich pięknej żony.",
            "5. Mówię o ich pięknej żonie.",
        ] {
            let answer = app.staticTexts[sentence]
            for _ in 0..<5 {
                if answer.exists { break }
                app.swipeUp()
            }
            XCTAssertTrue(answer.waitForExistence(timeout: 5), sentence)
        }
        XCTAssertFalse(app.staticTexts["5 преобразований"].exists)
        app.terminate()
        app.launch()
        app.buttons["Прогресс"].firstMatch.tap()
        XCTAssertTrue(total.waitForExistence(timeout: 10))
        let after = Int(total.label.split(separator: ",").last?.trimmingCharacters(in: .whitespaces) ?? "") ?? -1
        XCTAssertEqual(after, before + 5)
    }

    func testNativePronounTeachingShowsCompactContextsAndOwnerDemo() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Матрица"].firstMatch.tap()
        let section = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Раздел")).firstMatch
        XCTAssertTrue(section.waitForExistence(timeout: 10))
        section.tap()
        app.buttons["Местоимения"].tap()

        XCTAssertTrue(app.staticTexts["После предлогов у местоимений третьего лица появляется n-."].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["ja"].waitForExistence(timeout: 5))
        for label in [
            "Было: ja; Стало: mnie",
            "Было: ja; Стало: ze mną",
        ] {
            let pair = app.descendants(matching: .any)[label].firstMatch
            for _ in 0..<12 {
                if pair.exists { break }
                app.swipeUp()
            }
            XCTAssertTrue(pair.waitForExistence(timeout: 5), "Missing pronoun contrast: \(label)")
        }

        let ownerHeading = app.descendants(matching: .any)["mój — мой"].firstMatch
        for _ in 0..<20 {
            if ownerHeading.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(ownerHeading.waitForExistence(timeout: 5))
        let owner = app.descendants(matching: .any)["Было: moja piękna żona; Стало: moją piękną żonę"].firstMatch
        for _ in 0..<8 {
            if owner.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(owner.waitForExistence(timeout: 5))
        let genitive = app.descendants(matching: .any)["Было: moja piękna żona; Стало: mojej pięknej żony"].firstMatch
        for _ in 0..<8 {
            if genitive.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(genitive.waitForExistence(timeout: 5))
        let drill = app.buttons["Тренировать смену владельца"]
        for _ in 0..<8 {
            if drill.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(drill.waitForExistence(timeout: 5))
    }

    func testNativeVerbGenderControlChangesSelectedSubjectOnly() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Матрица"].firstMatch.tap()
        let section = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Раздел")).firstMatch
        XCTAssertTrue(section.waitForExistence(timeout: 10))
        section.tap()
        app.buttons["Времена и лица"].tap()

        let explanation = app.staticTexts["Составное будущее: będę + инфинитив или форма на -ł. У być: będę, без второго глагола."]
        XCTAssertTrue(explanation.waitForExistence(timeout: 5))
        let gender = app.buttons.matching(NSPredicate(format: "label BEGINSWITH %@", "Род")).firstMatch
        XCTAssertTrue(gender.waitForExistence(timeout: 5))
        gender.tap()
        app.buttons["Мужской"].tap()
        let masculinePast = app.descendants(matching: .any)["Было: robić; Стало: robiłem"].firstMatch
        let future = app.descendants(matching: .any)["Было: robić; Стало: będę robić"].firstMatch
        for _ in 0..<6 {
            if masculinePast.exists && future.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(masculinePast.waitForExistence(timeout: 5))
        XCTAssertTrue(future.waitForExistence(timeout: 5))

        for _ in 0..<6 {
            if gender.isHittable { break }
            app.swipeDown()
        }
        XCTAssertTrue(gender.isHittable)
        gender.tap()
        app.buttons["Женский"].tap()
        let femininePast = app.descendants(matching: .any)["Было: robić; Стало: robiłam"].firstMatch
        for _ in 0..<6 {
            if femininePast.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(femininePast.waitForExistence(timeout: 5))
        XCTAssertFalse(masculinePast.exists)
        for _ in 0..<6 {
            if app.staticTexts["on — он"].exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(app.staticTexts["on — он"].waitForExistence(timeout: 5))
        // "on — он" and its own change row can land on opposite sides of a lazy Form's mounted
        // window: reproduced on iPad Pro 11 — the label was already on screen but the row below it
        // (this list's own layout, unrelated to the earlier gender picker) was not yet mounted, so a
        // plain wait (no scroll) timed out; a bounded scroll-until-exists, like every other lookup
        // in this test, reaches it on both iPhone and iPad.
        let robilChange = app.descendants(matching: .any)["Было: robić; Стало: robił"].firstMatch
        for _ in 0..<6 {
            if robilChange.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(robilChange.waitForExistence(timeout: 5))
    }

    func testNativeCasesShowCompactNoteAndOrderedComparisonNouns() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Матрица"].firstMatch.tap()
        let section = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Раздел")).firstMatch
        XCTAssertTrue(section.waitForExistence(timeout: 10))
        section.tap()
        app.buttons["Падежи и окончания"].tap()

        let note = app.staticTexts["Wołacz — форма обращения. «Zachwycam się…» требует Narzędnik; «Przyglądam się…» — Celownik."]
        for _ in 0..<20 {
            if note.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(note.waitForExistence(timeout: 5))

        let comparison = app.staticTexts["Сравнение типов склонения"]
        for _ in 0..<8 {
            if comparison.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(comparison.waitForExistence(timeout: 5))
        for lemma in ["mąż", "kolega", "pies", "dom", "żona", "książka", "dziecko"] {
            let noun = app.staticTexts[lemma]
            for _ in 0..<8 {
                if noun.exists { break }
                app.swipeUp()
            }
            XCTAssertTrue(noun.waitForExistence(timeout: 5), lemma)
        }
    }

    func testNativeVocabularyRevealAndBinaryRating() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Слова"].firstMatch.tap()
        let select = app.buttons["vocabularySelect-noun.wife"]
        for _ in 0..<8 {
            if select.exists && select.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(select.waitForExistence(timeout: 5))
        if select.label.hasPrefix("Добавить") { select.tap() }
        for _ in 0..<8 {
            if app.buttons["vocabularyReveal"].isHittable { break }
            app.swipeDown()
        }
        let reveal = app.buttons["vocabularyReveal"]
        XCTAssertTrue(reveal.waitForExistence(timeout: 5))
        // D3: no rating buttons on touch — swiping the revealed card is the only way to rate.
        XCTAssertFalse(app.buttons["vocabularyAgain"].exists)
        XCTAssertFalse(app.buttons["vocabularyGood"].exists)
        reveal.tap()
        let ratingZone = app.descendants(matching: .any)["ratingSwipeArea"].firstMatch
        // The whole-panel flip's face swap lands ~0.25s after `reveal.tap()` (half of
        // `VocabularyCardView.flipDuration`) — a bounded wait here first, before any scrolling,
        // avoids a swipe landing on the still-showing (shorter) question face and overscrolling
        // straight past this single-screen card into the catalog list below it (reproduced: a
        // swipe that fires before the swap settles can travel that far with nothing to stop it).
        _ = ratingZone.waitForExistence(timeout: 1)
        for _ in 0..<5 {
            if ratingZone.exists && ratingZone.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(ratingZone.isHittable)
        XCTAssertFalse(app.buttons["vocabularyReveal"].exists, "reveal happens exactly once")
        let capture = XCTAttachment(screenshot: app.screenshot())
        capture.name = "native-vocabulary-iphone"
        capture.lifetime = .keepAlways
        add(capture)
        ratingZone.swipeLeft() // Again
        XCTAssertTrue(app.staticTexts["Мой словарь · 1"].waitForExistence(timeout: 5))
        app.terminate()
        app.launch()
        app.buttons["Слова"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Мой словарь · 1"].waitForExistence(timeout: 10))
    }

    func testBinaryRatingSwipesAdvanceOnceInEachDirection() {
        let app = XCUIApplication()
        app.launch()
        resetTrainingProgress(app)
        app.buttons["Тренировка"].firstMatch.tap()
        continueIntroductionIfPresent(app)
        let reveal = app.buttons["revealAnswer"]
        XCTAssertTrue(reveal.waitForExistence(timeout: 20))
        reveal.tap()
        // D3: no rating buttons on touch — swiping is the only way to rate.
        XCTAssertFalse(app.buttons["rateAgain"].exists)
        XCTAssertFalse(app.buttons["rateGood"].exists)
        let rating = app.descendants(matching: .any)["ratingSwipeArea"].firstMatch
        for _ in 0..<6 {
            if rating.exists && rating.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(rating.isHittable)
        rating.swipeLeft()
        continueIntroductionIfPresent(app)
        XCTAssertTrue(reveal.waitForExistence(timeout: 5))
        reveal.tap()
        for _ in 0..<6 {
            if rating.exists && rating.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(rating.isHittable)
        rating.swipeRight()
        continueIntroductionIfPresent(app)
        XCTAssertTrue(reveal.waitForExistence(timeout: 5))
    }

    /// D1 (`Plans/Kotlin/FlipCardRivePlan.md`, replacing FC-14's flip test): the question card
    /// expands downward into the answer/explanation/rating panel exactly once — there is no flip,
    /// so tapping the already-revealed panel is a no-op (it never hides the answer or re-offers
    /// Reveal). Exactly one review still comes from the rating button afterwards.
    func testTappingRevealedCardDoesNothingThenRatingCountsOnce() {
        let app = XCUIApplication()
        app.launch()
        resetTrainingProgress(app)
        app.buttons["Тренировка"].firstMatch.tap()
        continueIntroductionIfPresent(app)
        let reveal = app.buttons["revealAnswer"]
        for _ in 0..<7 {
            if reveal.exists && reveal.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(reveal.isHittable)
        reveal.tap()

        let changed = app.staticTexts["Что изменилось"]
        for _ in 0..<7 {
            if changed.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(changed.waitForExistence(timeout: 5), app.debugDescription)
        let back = app.staticTexts["Эталон"]
        XCTAssertTrue(back.exists)
        // The question stays visible above the unfolded answer — D1 never hides it.
        XCTAssertTrue(app.staticTexts["Исходное предложение"].exists)

        back.tap() // No-op: D1 is one-directional, there is no flip-back.
        XCTAssertTrue(changed.exists, "tapping the revealed answer must not hide it")
        XCTAssertFalse(app.buttons["revealAnswer"].exists,
            "tapping the revealed answer must not re-offer Reveal — the answer stays revealed")

        rateViaSwipe(app, good: true)

        app.buttons["Прогресс"].firstMatch.tap()
        let total = app.descendants(matching: .any)["totalReviews"].firstMatch
        // resetTrainingProgress scrolled this tab's list down to reach "Сбросить прогресс"; that
        // scroll position persists across the tab switches above, so "Сводка" (and totalReviews)
        // needs scrolling back to the top before it is on screen again.
        for _ in 0..<8 {
            if total.exists { break }
            app.swipeDown()
        }
        // resetTrainingProgress guarantees a clean 0, so exactly one rating (through one no-op
        // tap-on-revealed first) must land on exactly 1 — not 2 or more from a spurious extra review.
        XCTAssertTrue(total.waitForExistence(timeout: 10), app.debugDescription)
        XCTAssertTrue(total.label.hasSuffix(", 1"), total.label)
    }

    func testTypedPolishAnswerUsesNativeInput() {
        #if IPAD_LANDSCAPE_ACCEPTANCE
        XCUIDevice.shared.orientation = .landscapeLeft
        defer { XCUIDevice.shared.orientation = .portrait }
        #endif
        let app = XCUIApplication()
        app.launch()
        resetTrainingProgress(app)
        app.buttons["Тренировка"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].waitForExistence(timeout: 20))
        continueIntroductionIfPresent(app)
        let mode = app.buttons.matching(NSPredicate(format: "label BEGINSWITH %@", "Ответ,")).firstMatch
        XCTAssertTrue(mode.exists)
        mode.tap()
        app.buttons["Напечатать"].tap()
        let answer = app.descendants(matching: .any)["typedAnswer"].firstMatch
        XCTAssertTrue(answer.waitForExistence(timeout: 5))
        answer.tap()
        answer.typeText("Widzę moją piękną żonę.")
        XCTAssertEqual(answer.value as? String, "Widzę moją piękną żonę.")
        let reveal = app.buttons["revealAnswer"]
        print("TYPED_REVEAL_FRAME: \(reveal.frame), KEYBOARD: \(app.keyboards.firstMatch.frame)")
        XCTAssertTrue(reveal.isHittable)
        if app.frame.width > 700, app.keyboards.firstMatch.exists {
            XCTAssertLessThan(reveal.frame.midY, app.keyboards.firstMatch.frame.minY - 55)
        }
        reveal.tap()
        let match = app.staticTexts["Совпадает с правильным вариантом"]
        for _ in 0..<5 {
            if match.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(match.waitForExistence(timeout: 5), app.debugDescription)
    }

    func testNativeTrainingMatrixAndProgress() {
        #if REDUCE_MOTION_ACCEPTANCE
        XCTAssertTrue(UIAccessibility.isReduceMotionEnabled, "Simulator Reduce Motion must be enabled for this acceptance run")
        #endif
        let app = XCUIApplication()
        app.launch()
        app.buttons["Прогресс"].firstMatch.tap()
        let initialTotal = app.descendants(matching: .any)["totalReviews"].firstMatch
        XCTAssertTrue(initialTotal.waitForExistence(timeout: 10))
        let before = Int(initialTotal.label.split(separator: ",").last?.trimmingCharacters(in: .whitespaces) ?? "") ?? -1
        app.buttons["Тренировка"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].waitForExistence(timeout: 20))
        XCTAssertFalse(app.staticTexts["Widzę moją piękną żonę."].exists)
        continueIntroductionIfPresent(app)

        let reveal = app.buttons["revealAnswer"]
        for _ in 0..<5 {
            if reveal.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(reveal.exists)
        reveal.tap()
        XCTAssertTrue(app.staticTexts["Widzę moją piękną żonę."].waitForExistence(timeout: 5))
        rateViaSwipe(app, good: true)
        for _ in 0..<4 { app.swipeDown() }
        XCTAssertTrue(app.staticTexts["1 / 5 · Вижу → Прошлое → Отрицание → Владелец → Говорю о"].waitForExistence(timeout: 5))

        app.buttons["Матрица"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Что хочу сказать? → Какой падеж нужен? → Меняю всю группу"].waitForExistence(timeout: 5))
        for line in [
            "вижу кого? что? → Widzę moją żonę. Окончания меняются во всей группе.",
            "с моей женой → z moją żoną. Женское -ą — творительный.",
            "говорю о жене → mówię o żonie. Местный требует предлога.",
            "мой / его / их → moją / jego / ich żonę. jego, jej, ich не склоняются.",
        ] {
            let text = app.staticTexts[line]
            for _ in 0..<20 {
                if text.exists { break }
                app.swipeUp()
            }
            XCTAssertTrue(text.waitForExistence(timeout: 5), line)
        }
        app.buttons["Прогресс"].firstMatch.tap()
        for _ in 0..<8 {
            if app.buttons["Экспортировать JSON"].exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(app.buttons["Экспортировать JSON"].waitForExistence(timeout: 5))

        app.terminate()
        app.launch()
        app.buttons["Прогресс"].firstMatch.tap()
        let total = app.descendants(matching: .any)["totalReviews"].firstMatch
        XCTAssertTrue(total.waitForExistence(timeout: 10))
        let after = Int(total.label.split(separator: ",").last?.trimmingCharacters(in: .whitespaces) ?? "") ?? -1
        XCTAssertEqual(before + 1, after, total.label)
    }

    /// StylesBlueprint.md §1/§6/S2: switching the training card's style picker changes which
    /// composed `Block`s show on its already-revealed back — rule-first alone shows `formula`
    /// there (`StyleRegistry`'s recipes), minimal-theory alone shows `whyOnDemand`, and neither
    /// shows for situation-first (whose back is `Changes` only). Asserted by each block's own
    /// `accessibilityIdentifier` (`styleBlock-<kind>`, `FlashCardView.swift`), not by any authored
    /// text — pl-ru `styleContent` is being written in a separate worktree and isn't in yet.
    func testSwitchingStyleChangesWhichBlocksShow() {
        let app = XCUIApplication()
        app.launch()
        resetTrainingProgress(app)
        app.buttons["Тренировка"].firstMatch.tap()
        XCTAssertTrue(app.buttons["continueIntroduction"].waitForExistence(timeout: 20))
        continueIntroductionIfPresent(app)

        let picker = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Стиль объяснений")).firstMatch
        func selectStyle(_ label: String) {
            for _ in 0..<7 {
                if picker.isHittable { break }
                app.swipeDown()
            }
            XCTAssertTrue(picker.isHittable, app.debugDescription)
            guard !picker.label.contains(label) else { return }
            picker.tap()
            let option = app.descendants(matching: .any)[label].firstMatch
            XCTAssertTrue(option.waitForExistence(timeout: 5), app.debugDescription)
            option.tap()
        }
        func scrollToTop() { for _ in 0..<10 { app.swipeDown() } }
        // Hunts downward from the top for `identifier` in the (lazy Form-backed) answer panel —
        // the same bounded swipeUp hunt every other test in this file already uses to find a
        // specific row, just returning whether it was ever found instead of asserting it was.
        func blockShown(_ identifier: String) -> Bool {
            scrollToTop()
            let element = app.descendants(matching: .any)[identifier].firstMatch
            for _ in 0..<10 {
                if element.exists { return true }
                app.swipeUp()
            }
            return element.exists
        }

        selectStyle("Через правило")
        let reveal = app.buttons["revealAnswer"]
        for _ in 0..<8 {
            if reveal.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(reveal.waitForExistence(timeout: 5), app.debugDescription)
        reveal.tap()
        XCTAssertTrue(blockShown("styleBlock-formula"), app.debugDescription)
        XCTAssertFalse(blockShown("styleBlock-whyOnDemand"), app.debugDescription)

        // ST-04: switching style on the same already-revealed card is a display choice only —
        // no new review, no exercise change — so the very same card's back re-composes live.
        selectStyle("Через ситуацию")
        XCTAssertFalse(blockShown("styleBlock-formula"), app.debugDescription)
        XCTAssertFalse(blockShown("styleBlock-whyOnDemand"), app.debugDescription)

        selectStyle("Минимум теории")
        XCTAssertFalse(blockShown("styleBlock-formula"), app.debugDescription)
        XCTAssertTrue(blockShown("styleBlock-whyOnDemand"), app.debugDescription)

        selectStyle("Через правило")
        XCTAssertTrue(blockShown("styleBlock-formula"), app.debugDescription)
        XCTAssertFalse(blockShown("styleBlock-whyOnDemand"), app.debugDescription)
    }

    /// Tester-added (not in StylesBlueprint.md's own test list): the 4th style, native-contrast, and
    /// situation-first's post-reveal `rule` block have no XCUITest coverage anywhere in this file —
    /// `testSwitchingStyleChangesWhichBlocksShow` above only exercises rule-first/situation-first/
    /// minimal-theory's `formula`/`whyOnDemand`. This drives all 4 with real, now-authored
    /// `courses/pl-ru/course.json` `styleContent` and captures one screenshot per style for visual
    /// review, plus asserts each style's blueprint-intended block (StylesBlueprint.md §1 TABLE 1).
    func testFourStylesShowIntendedBlocksWithRealContent() {
        let app = XCUIApplication()
        app.launch()
        resetTrainingProgress(app)
        app.buttons["Тренировка"].firstMatch.tap()
        XCTAssertTrue(app.buttons["continueIntroduction"].waitForExistence(timeout: 20))
        continueIntroductionIfPresent(app)

        let picker = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Стиль объяснений")).firstMatch
        func selectStyle(_ label: String) {
            for _ in 0..<7 {
                if picker.isHittable { break }
                app.swipeDown()
            }
            XCTAssertTrue(picker.isHittable, app.debugDescription)
            guard !picker.label.contains(label) else { return }
            picker.tap()
            let option = app.descendants(matching: .any)[label].firstMatch
            XCTAssertTrue(option.waitForExistence(timeout: 5), app.debugDescription)
            option.tap()
        }
        func scrollToTop() { for _ in 0..<10 { app.swipeDown() } }
        func blockShown(_ identifier: String) -> Bool {
            scrollToTop()
            let element = app.descendants(matching: .any)[identifier].firstMatch
            for _ in 0..<10 {
                if element.exists { return true }
                app.swipeUp()
            }
            return element.exists
        }
        func capture(_ name: String) {
            let shot = XCTAttachment(screenshot: app.screenshot())
            shot.name = name
            shot.lifetime = .keepAlways
            add(shot)
        }

        selectStyle("Через правило")
        let reveal = app.buttons["revealAnswer"]
        for _ in 0..<8 {
            if reveal.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(reveal.waitForExistence(timeout: 5), app.debugDescription)
        reveal.tap()
        XCTAssertTrue(blockShown("styleBlock-formula"), app.debugDescription)
        XCTAssertTrue(blockShown("styleBlock-table") || blockShown("styleBlock-formula"), "rule-first front should show table/formula")
        // D2 (StylesIntegrationTest-2026-09-27.md): the rule block must show the exercise's own
        // detail (`Block.Rule.detail`), not just the skill's rule text — a distinct sub-element so
        // this checks real rendered content, not merely that some rule block exists.
        XCTAssertTrue(blockShown("styleBlock-rule"), "rule-first back should show the rule block — " + app.debugDescription)
        XCTAssertTrue(blockShown("styleBlock-rule-detail"), "rule-first back should show the exercise's own detail under the rule — " + app.debugDescription)
        capture("style-rule-first-revealed")

        // situation-first: scene stays up front (D1 — the front never hides after reveal), and the
        // card is already revealed from the rule-first step above — ST-04 says switching style
        // never resets phase, so there is no second revealAnswer to find here.
        selectStyle("Через ситуацию")
        scrollToTop()
        XCTAssertTrue(app.descendants(matching: .any)["styleBlock-scene"].firstMatch.waitForExistence(timeout: 5), app.debugDescription)
        capture("style-situation-first-front-scene")
        capture("style-situation-first-revealed")
        XCTAssertTrue(blockShown("styleBlock-rule"), "situation-first back must show the rule block after reveal — " + app.debugDescription)

        selectStyle("Через сравнение с родным")
        scrollToTop()
        capture("style-native-contrast-front")
        XCTAssertTrue(blockShown("styleBlock-nativeParallel"), "native-contrast front should show nativeParallel with authored styleContent — " + app.debugDescription)
        // Real authored detail, not just block presence (report D2's own critique of the Android
        // pass): `case.acc.f`'s styleContent.nativeParallel[0].note is real content, distinctive
        // enough to be safe to match on.
        let noteDetail = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS %@", "żona→żonę"))
        XCTAssertTrue(noteDetail.firstMatch.waitForExistence(timeout: 5), "native-contrast pair should show its authored note detail — " + app.debugDescription)
        capture("style-native-contrast-revealed")

        selectStyle("Минимум теории")
        scrollToTop()
        capture("style-minimal-theory-front")
        XCTAssertTrue(blockShown("styleBlock-whyOnDemand"), app.debugDescription)
        capture("style-minimal-theory-revealed")
    }
}

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
        let good = app.buttons["rateGood"]
        for _ in 0..<7 {
            if good.exists && good.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(good.isHittable)
        good.tap()

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
        let mode = app.buttons.matching(NSPredicate(format: "label BEGINSWITH %@", "Ответ,")).firstMatch
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
        let good = app.buttons["rateGood"]
        for _ in 0..<7 {
            if good.isHittable { break }
            app.swipeUp()
        }
        good.tap()
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
        let method = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Подача объяснений")).firstMatch
        for _ in 0..<7 {
            if method.isHittable { break }
            app.swipeDown()
        }
        XCTAssertTrue(method.isHittable)
        if method.label.contains("Живые ситуации") {
            method.tap()
            let logic = app.descendants(matching: .any)["Схемы и логика"].firstMatch
            XCTAssertTrue(logic.waitForExistence(timeout: 5))
            logic.tap()
        }
        XCTAssertTrue(method.label.contains("Схемы и логика"))
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
        let situations = app.descendants(matching: .any)["Живые ситуации"].firstMatch
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
        app.descendants(matching: .any)["Схемы и логика"].firstMatch.tap()
        for _ in 0..<8 {
            if frozen.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(frozen.exists)
        let good = app.buttons["rateGood"]
        for _ in 0..<7 {
            if good.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(good.isHittable)
        good.tap()
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
            let good = app.buttons["rateGood"]
            for _ in 0..<7 {
                if good.exists && good.isHittable { break }
                app.swipeUp()
            }
            XCTAssertTrue(good.isHittable)
            good.tap()
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
        XCTAssertTrue(app.descendants(matching: .any)["Было: robić; Стало: robił"].firstMatch.waitForExistence(timeout: 5))
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
        XCTAssertFalse(app.buttons["vocabularyAgain"].exists)
        reveal.tap()
        for _ in 0..<5 {
            if app.buttons["vocabularyAgain"].isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(app.buttons["vocabularyAgain"].isHittable)
        XCTAssertTrue(app.buttons["vocabularyGood"].isHittable)
        let capture = XCTAttachment(screenshot: app.screenshot())
        capture.name = "native-vocabulary-iphone"
        capture.lifetime = .keepAlways
        add(capture)
        app.buttons["vocabularyAgain"].tap()
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
        XCTAssertFalse(app.buttons["rateHard"].exists)
        XCTAssertFalse(app.buttons["rateEasy"].exists)
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

        let good = app.buttons["rateGood"]
        for _ in 0..<7 {
            if good.exists && good.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(good.isHittable)
        good.tap()

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
        let good = app.buttons["rateGood"]
        for _ in 0..<6 {
            if good.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(good.exists)
        good.tap()
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
}

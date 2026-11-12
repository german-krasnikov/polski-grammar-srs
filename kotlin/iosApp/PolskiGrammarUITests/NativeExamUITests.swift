import XCTest

final class NativeExamUITests: XCTestCase {
    private func openPaper(_ id: String = "b1-2019") -> XCUIApplication {
        let app = XCUIApplication()
        app.launch()
        let exam = app.buttons["Экзамен"]
        XCTAssertTrue(exam.waitForExistence(timeout: 15), app.debugDescription)
        exam.tap()
        let paper = app.buttons["exam-\(id)"]
        XCTAssertTrue(paper.waitForExistence(timeout: 5), app.debugDescription)
        paper.tap()
        return app
    }

    func testPublishedReadingAndWritingAreInteractive() {
        let app = openPaper()
        app.buttons["exam-module-reading"].tap()
        XCTAssertTrue(app.staticTexts["exam-part-text"].waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(app.descendants(matching: .any).matching(NSPredicate(format: "identifier BEGINSWITH %@", "exam-question-")).firstMatch.exists)
        app.buttons["exam-exit-part"].tap()
        app.buttons["exam-module-writing"].tap()
        let set = app.descendants(matching: .any)["exam-writing-set"].firstMatch
        XCTAssertTrue(set.waitForExistence(timeout: 5), app.debugDescription)
        set.tap()
        app.buttons["I"].tap()
        let draft = app.textViews["exam-writing-draft"]
        XCTAssertTrue(draft.waitForExistence(timeout: 5), app.debugDescription)
        draft.tap()
        draft.typeText("Żółć. Mój tekst.")
        XCTAssertTrue((draft.value as? String ?? "").contains("Żółć"), app.debugDescription)
        app.buttons["exam-submit"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["exam-part-result"].waitForExistence(timeout: 5), app.debugDescription)
    }

    func testPublishedListeningLoadsOfflineSegment() {
        let app = openPaper("b1-2017")
        app.buttons["exam-module-listening"].tap()
        let play = app.buttons["exam-audio-play"]
        XCTAssertTrue(play.waitForExistence(timeout: 5), app.debugDescription)
        play.tap()
        let seek = app.sliders["exam-audio-seek"]
        XCTAssertTrue(seek.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertFalse(app.staticTexts["Аудиозапись недоступна"].exists)
        let progressPastFortySeconds = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
            let value = seek.value as? String ?? ""
            let percent = Int(value.split(separator: " ").first ?? "0") ?? 0
            return percent >= 16 // first published segment is 115–369 s (254 s)
        }, object: nil)
        XCTAssertEqual(XCTWaiter.wait(for: [progressPastFortySeconds], timeout: 55), .completed,
                       "The bundled recording must advance beyond 40 seconds: \(seek.value ?? "none")")
        app.buttons["exam-audio-replay"].tap()
    }

    func testTimedExamUsesPublishedOrderAndClock() {
        let app = openPaper("b2-2020")
        app.buttons["exam-start-timed"].tap()
        XCTAssertTrue(app.staticTexts["exam-timer"].waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(app.buttons["exam-audio-play"].exists, app.debugDescription)
        app.buttons["exam-submit"].tap()
        XCTAssertTrue(app.staticTexts["exam-part-text"].waitForExistence(timeout: 5), app.debugDescription)
    }

    func testPublishedPromptImageLoadsFromBundle() {
        let app = openPaper("b1-2019")
        app.buttons["exam-module-listening"].tap()
        for _ in 0..<6 {
            if app.images["exam-prompt-image"].exists { break }
            app.buttons["exam-next"].tap()
        }
        XCTAssertTrue(app.images["exam-prompt-image"].waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertFalse(app.staticTexts["Изображение недоступно"].exists)
    }

    func testListeningFreeSetOffersSeparateAnswerSlots() {
        let app = openPaper("b2-2019")
        app.buttons["exam-module-listening"].tap()
        let slot = app.textFields["exam-answer-certyfikat-polski-B2-2019-listening-5-1-1"]
        for _ in 0..<6 {
            if slot.exists { break }
            app.buttons["exam-next"].tap()
        }
        XCTAssertTrue(slot.waitForExistence(timeout: 5), app.debugDescription)
        slot.tap()
        slot.typeText("żółć próba")
        XCTAssertEqual(slot.value as? String, "żółć próba", app.debugDescription)
        app.buttons["Матрица"].tap()
        let matrixSelected = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "selected == true"), object: app.buttons["Матрица"])
        XCTAssertEqual(XCTWaiter.wait(for: [matrixSelected], timeout: 5), .completed, app.debugDescription)
        XCTAssertTrue(app.navigationBars["Матрица"].waitForExistence(timeout: 5), app.debugDescription)
        app.buttons["Экзамен"].tap()
        let examSelected = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "selected == true"), object: app.buttons["Экзамен"])
        XCTAssertEqual(XCTWaiter.wait(for: [examSelected], timeout: 5), .completed, app.debugDescription)
        XCTAssertTrue(slot.waitForExistence(timeout: 5),
                      "Listening answer slot should survive tab return: \(app.debugDescription)")
        XCTAssertEqual(slot.value as? String, "żółć próba", app.debugDescription)
        app.buttons["exam-submit"].tap()
        XCTAssertTrue(app.staticTexts["exam-part-result"].firstMatch.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(app.staticTexts.matching(NSPredicate(format: "label CONTAINS %@", "Требуется ручная проверка: 1")).firstMatch.exists,
                      app.debugDescription)
        XCTAssertFalse(app.staticTexts.matching(NSPredicate(format: "label CONTAINS %@", "%")).firstMatch.exists,
                       "Provisional manual work must not show a final percentage")
    }

    func testListeningSegmentHasAccessibleSeekAndElapsedDuration() {
        let app = openPaper("b1-2017")
        app.buttons["exam-module-listening"].tap()
        let seek = app.sliders["exam-audio-seek"]
        XCTAssertTrue(seek.waitForExistence(timeout: 5),
                      "Listening needs a segment-clamped seek control: \(app.debugDescription)")
        guard seek.exists else { return }
        XCTAssertFalse(seek.label.isEmpty, "VoiceOver needs a spoken seek label")
        let elapsed = app.staticTexts["exam-audio-elapsed"]
        let duration = app.staticTexts["exam-audio-duration"]
        XCTAssertTrue(elapsed.exists, app.debugDescription)
        XCTAssertTrue(duration.exists, app.debugDescription)
        guard elapsed.exists, duration.exists else { return }
        XCTAssertEqual(duration.label, "4:14", "B1-2017 first source segment spans 115–369 seconds")
        seek.adjust(toNormalizedSliderPosition: 0.8)
        XCTAssertTrue(elapsed.label.hasPrefix("3:"), "Seek should stay within the 4:14 segment: \(elapsed.label)")
        app.buttons["exam-audio-replay"].tap()
        XCTAssertTrue(elapsed.label.hasPrefix("0:"), "Replay should return to segment start: \(elapsed.label)")
    }

    func testPublishedTwoChoiceSetRejectsThirdSelection() {
        let app = openPaper("b2-2017")
        app.buttons["exam-module-listening"].tap()
        for _ in 0..<3 { app.buttons["exam-next"].tap() }
        XCTAssertTrue(app.staticTexts["exam-question-certyfikat-polski-B2-2017-listening-4-1"]
            .waitForExistence(timeout: 5), app.debugDescription)
        let first = app.buttons["1"].firstMatch
        let second = app.buttons["2"].firstMatch
        let third = app.buttons["3"].firstMatch
        XCTAssertTrue(first.exists && second.exists && third.exists, app.debugDescription)
        first.tap()
        second.tap()
        XCTAssertTrue(first.isSelected && second.isSelected, app.debugDescription)
        third.tap()
        XCTAssertTrue(first.isSelected && second.isSelected, "Two authored selections should remain")
        XCTAssertFalse(third.isSelected, "B2-2017 Listening IV/1 accepts two selections, not three")
    }

    func testRetryClearsOldFreeSetSlotDrafts() {
        let app = openPaper("b2-2019")
        app.buttons["exam-module-listening"].tap()
        let slot = app.textFields["exam-answer-certyfikat-polski-B2-2019-listening-5-1-1"]
        for _ in 0..<6 {
            if slot.exists { break }
            app.buttons["exam-next"].tap()
        }
        XCTAssertTrue(slot.waitForExistence(timeout: 5), app.debugDescription)
        slot.tap()
        slot.typeText("stary szkic")
        app.buttons["exam-submit"].tap()
        XCTAssertTrue(app.buttons["exam-exit-part"].waitForExistence(timeout: 5), app.debugDescription)
        app.buttons["exam-exit-part"].tap()
        app.buttons["exam-module-listening"].tap()
        for _ in 0..<6 {
            if slot.exists { break }
            app.buttons["exam-next"].tap()
        }
        XCTAssertTrue(slot.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertNotEqual(slot.value as? String, "stary szkic", "A new attempt cannot reuse Swift-only slot drafts")
    }

    func testListeningPausesOnTabAndBackgroundWithoutLosingPosition() {
        let app = openPaper("b1-2017")
        app.buttons["exam-module-listening"].tap()
        let seek = app.sliders["exam-audio-seek"]
        let elapsed = app.staticTexts["exam-audio-elapsed"]
        XCTAssertTrue(seek.waitForExistence(timeout: 5), app.debugDescription)
        seek.adjust(toNormalizedSliderPosition: 0.5)
        XCTAssertTrue(elapsed.label.hasPrefix("2:"), app.debugDescription)
        app.buttons["exam-audio-play"].tap()
        app.buttons["Матрица"].tap()
        let matrixSelected = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "selected == true"), object: app.buttons["Матрица"])
        XCTAssertEqual(XCTWaiter.wait(for: [matrixSelected], timeout: 5), .completed, app.debugDescription)
        XCTAssertTrue(app.navigationBars["Матрица"].waitForExistence(timeout: 5), app.debugDescription)
        app.buttons["Экзамен"].tap()
        let examSelected = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "selected == true"), object: app.buttons["Экзамен"])
        XCTAssertEqual(XCTWaiter.wait(for: [examSelected], timeout: 5), .completed, app.debugDescription)
        XCTAssertTrue(elapsed.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(elapsed.label.hasPrefix("2:"), "Tab return should retain the paused segment position")
        XCUIDevice.shared.press(.home)
        app.activate()
        XCTAssertTrue(elapsed.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(elapsed.label.hasPrefix("2:"), "Foreground should retain the paused segment position")
        app.buttons["exam-audio-play"].tap()
        XCTAssertTrue(elapsed.label.hasPrefix("2:"), "Resume should continue inside the source segment")
        app.buttons["exam-exit-part"].tap()
        app.buttons["exam-module-listening"].tap()
        XCTAssertEqual(app.staticTexts["exam-audio-elapsed"].label, "0:00",
                       "Leaving the module should release the previous player")
    }

    func testWritingSetPlaceholderClearsSelectionButRetainsSetDraft() {
        let app = openPaper("b1-2019")
        app.buttons["exam-module-writing"].tap()
        let picker = app.descendants(matching: .any)["exam-writing-set"].firstMatch
        XCTAssertTrue(picker.waitForExistence(timeout: 5), app.debugDescription)
        picker.tap()
        app.buttons["I"].tap()
        let draft = app.textViews["exam-writing-draft"]
        XCTAssertTrue(draft.waitForExistence(timeout: 5), app.debugDescription)
        draft.tap()
        draft.typeText("Żółć, szkic pierwszego zadania.")
        app.buttons["exam-next"].tap()
        picker.tap()
        app.buttons["Выберите набор"].tap()
        XCTAssertFalse(draft.exists, "An unselected writing set must show no task editor")
        picker.tap()
        app.buttons["I"].tap()
        XCTAssertTrue(draft.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue((draft.value as? String ?? "").contains("Żółć"),
                      "Re-selecting set I should restore its first-task draft")
    }

    func testPublishedTimedSittingCompletesFourPartsWithProvisionalWriting() {
        let app = openPaper("b2-2020")
        app.buttons["exam-start-timed"].tap()
        XCTAssertTrue(app.staticTexts["exam-timer"].waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(app.buttons["exam-audio-play"].exists, app.debugDescription)
        app.buttons["exam-submit"].tap()
        XCTAssertTrue(app.staticTexts["exam-part-text"].waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(app.staticTexts["exam-timer"].exists, app.debugDescription)
        app.buttons["exam-submit"].tap()
        XCTAssertTrue(app.staticTexts["exam-context"].waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(app.staticTexts["exam-timer"].exists, app.debugDescription)
        app.buttons["exam-finish-part"].tap()
        let picker = app.descendants(matching: .any)["exam-writing-set"].firstMatch
        XCTAssertTrue(picker.waitForExistence(timeout: 5), app.debugDescription)
        picker.tap()
        app.buttons["I"].tap()
        let draft = app.textViews["exam-writing-draft"]
        XCTAssertTrue(draft.waitForExistence(timeout: 5), app.debugDescription)
        draft.tap()
        draft.typeText("Żółć. Proszę o sprawdzenie mojej pracy.")
        app.buttons["exam-submit"].tap()
        XCTAssertTrue(app.staticTexts["Результаты"].waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertFalse(app.staticTexts["exam-timer"].exists, "Completed sitting has no active deadline")
        XCTAssertTrue(app.staticTexts.matching(NSPredicate(format: "label CONTAINS %@", "Требуется ручная проверка")).firstMatch.exists,
                      app.debugDescription)
        XCTAssertFalse(app.staticTexts.matching(NSPredicate(format: "label CONTAINS %@", "%")).firstMatch.exists,
                       "Provisional Writing must not show a whole-sitting final percentage")
    }

    func testLargeTextAndLandscapeKeepNativeExamAnswerReachable() {
        let app = openPaper("b1-2019")
        app.buttons["exam-module-writing"].tap()
        let picker = app.descendants(matching: .any)["exam-writing-set"].firstMatch
        XCTAssertTrue(picker.waitForExistence(timeout: 5), app.debugDescription)
        picker.tap()
        app.buttons["I"].tap()
        let source = app.staticTexts["exam-part-text"]
        let draft = app.textViews["exam-writing-draft"]
        let submit = app.buttons["exam-submit"]
        XCTAssertTrue(source.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(draft.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(submit.isHittable, app.debugDescription)
        draft.tap()
        draft.typeText("Żółć, duży tekst.")
        XCUIDevice.shared.orientation = .landscapeLeft
        XCTAssertTrue(source.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(draft.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue((draft.value as? String ?? "").contains("Żółć"), app.debugDescription)
        XCTAssertTrue(submit.isHittable, "Submit must remain reachable in landscape at large text")
        XCUIDevice.shared.orientation = .portrait
    }

    func testPublishedTimedListeningNaturallyExpiresIntoReading() {
        let app = openPaper("b2-2020")
        app.buttons["exam-start-timed"].tap()
        let timer = app.staticTexts["exam-timer"]
        XCTAssertTrue(timer.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(timer.label.hasPrefix("1 / 4"), timer.label)
        let startedAt = Date()
        let nextPart = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "label BEGINSWITH %@", "2 / 4"), object: timer)
        XCTAssertEqual(XCTWaiter.wait(for: [nextPart], timeout: 2_520), .completed,
                       "The authored B2-2020 Listening limit is 2,400 seconds; last timer: \(timer.label)")
        XCTAssertGreaterThanOrEqual(Date().timeIntervalSince(startedAt), 2_395,
                                    "The transition must follow the natural 40-minute deadline")
        XCTAssertTrue(app.staticTexts["exam-part-text"].waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertFalse(app.buttons["exam-audio-play"].exists, "Reading should replace Listening after timeout")
    }
}

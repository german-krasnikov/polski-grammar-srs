import XCTest

final class NativeExamUITests: XCTestCase {
    private func assertWritingGuidance(_ app: XCUIApplication, category: String,
                                       languagePoints: Int, correctnessPoints: Int) {
        let picker = app.descendants(matching: .any)["exam-writing-set"].firstMatch
        XCTAssertTrue(picker.waitForExistence(timeout: 10), app.debugDescription)
        picker.tap()
        app.buttons["I"].tap()
        let instruction = app.staticTexts["exam-part-instruction"]
        XCTAssertTrue(instruction.waitForExistence(timeout: 10), app.debugDescription)
        XCTAssertTrue(instruction.label.localizedCaseInsensitiveContains("Wykonanie zadania"), instruction.label)
        XCTAssertTrue(instruction.label.localizedCaseInsensitiveContains(category), instruction.label)
        XCTAssertTrue(instruction.label.contains("0 – \(languagePoints) p."), instruction.label)
        XCTAssertTrue(instruction.label.contains("0 – \(correctnessPoints) p."), instruction.label)
        XCTAssertTrue(app.staticTexts["exam-part-text"].exists, app.debugDescription)
        XCTAssertTrue(app.textViews["exam-writing-draft"].exists, app.debugDescription)
        XCTContext.runActivity(named: "Native writing criteria \(category) \(languagePoints)/\(correctnessPoints)") { activity in
            let screenshot = XCTAttachment(screenshot: app.screenshot())
            screenshot.lifetime = .keepAlways
            activity.add(screenshot)
        }
    }

    private func openPaper(_ id: String = "b1-2019") -> XCUIApplication {
        let app = XCUIApplication()
        app.launch()
        let exam = app.buttons["Экзамен"]
        XCTAssertTrue(exam.waitForExistence(timeout: 15), app.debugDescription)
        exam.tap()
        let paper = app.buttons["exam-\(id)"]
        for _ in 0..<10 {
            if paper.exists { break }
            app.swipeUp()
        }
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

    func testOfficialWritingGuidanceAppearsInStandaloneNativePapers() {
        for (paper, category, language, correctness) in [
            ("b1-2017", "Bogactwo językowe", 10, 10),
            ("b2-2017", "Bogactwo językowe", 16, 14),
            ("c1-2022-11", "Środki językowe", 18, 12),
        ] {
            let app = openPaper(paper)
            app.buttons["exam-module-writing"].tap()
            assertWritingGuidance(app, category: category, languagePoints: language,
                                  correctnessPoints: correctness)
            app.terminate()
        }
    }

    func testOfficialWritingGuidanceAppearsInTimedNativePapers() {
        for (paper, category, language, correctness) in [
            ("b1-2017", "Bogactwo językowe", 10, 10),
            ("b2-2017", "Bogactwo językowe", 16, 14),
            ("c1-2022-11", "Środki językowe", 18, 12),
        ] {
            let app = openPaper(paper)
            app.buttons["exam-start-timed"].tap()
            XCTAssertTrue(app.staticTexts["exam-timer"].waitForExistence(timeout: 10), app.debugDescription)
            app.buttons["exam-submit"].tap() // Listening → Reading
            XCTAssertTrue(app.staticTexts["exam-part-text"].waitForExistence(timeout: 10), app.debugDescription)
            app.buttons["exam-submit"].tap() // Reading → Grammar
            XCTAssertTrue(app.staticTexts["exam-context"].waitForExistence(timeout: 10), app.debugDescription)
            app.buttons["exam-finish-part"].tap() // Grammar → Writing
            XCTAssertTrue(app.staticTexts["exam-timer"].exists, app.debugDescription)
            assertWritingGuidance(app, category: category, languagePoints: language,
                                  correctnessPoints: correctness)
            app.terminate()
        }
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
        XCTAssertEqual(app.images["exam-prompt-image"].label,
                       "Ilustracje A–K do zadania V: A narzędzia jako przykład; B marker, C kosmetyczka, D ciężarówka, E pudła, F ubrania na wieszakach, G wspólny posiłek, H koce, I środki czystości, J osoba niosąca pudła, K pomoc przyjaciół.")
        XCTAssertFalse(app.staticTexts["Изображение недоступно"].exists)
    }

    func testPublishedMatchingChoicesPreserveSelectionByQuestion() {
        let app = openPaper("b1-2017")
        app.buttons["exam-module-listening"].tap()
        for _ in 0..<4 { app.buttons["exam-next"].tap() }
        XCTAssertTrue(app.staticTexts["exam-question-B1-2017-l5-1"].waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertEqual(app.staticTexts["exam-question-B1-2017-l5-1"].label,
                       "opłata paszportowa w wysokości 30 zł")
        let choices = ["A. paszport dla emerytów i rencistów", "B. paszport tymczasowy dla dorosłych",
                       "C. paszport dla osób powyżej 70 lat", "D. paszport dla dzieci do 7 lat",
                       "E. paszport dla dorosłych na 10 lat", "F. paszport dla dzieci poniżej 13 lat"]
        for choice in choices {
            XCTAssertTrue(app.buttons[choice].firstMatch.exists, "Missing source matching choice: \(choice)")
        }
        let selected = app.buttons[choices[3]].firstMatch
        selected.tap()
        XCTAssertTrue(selected.isSelected, "The first matching question must retain its choice")
        app.buttons["exam-previous"].tap()
        app.buttons["exam-next"].tap()
        XCTAssertTrue(selected.isSelected, "Matching answer must survive task navigation")
    }

    func testPublishedReadingFragmentsRetainFirstAnswer() {
        let app = openPaper("b2-2020")
        app.buttons["exam-module-reading"].tap()
        app.buttons["exam-next"].tap()
        XCTAssertTrue(app.staticTexts["exam-question-certyfikat-polski-B2-2020-reading-2-1"]
            .waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertEqual(app.staticTexts["exam-question-certyfikat-polski-B2-2020-reading-2-1"].label,
                       "Fragment do luki [1]")
        let sourceChoices = ["A", "B", "D", "E", "F", "G", "H", "I", "J", "K"]
        for choice in sourceChoices {
            XCTAssertTrue(app.buttons[choice].firstMatch.exists, "Missing source fragment: \(choice)")
        }
        XCTAssertFalse(app.buttons["C"].exists, "The source fragment list omits C")
        let selected = app.buttons["E"].firstMatch
        for _ in 0..<8 {
            if selected.frame.maxY < 1000 { break }
            app.scrollViews.firstMatch.swipeUp()
        }
        XCTAssertLessThan(selected.frame.maxY, 1000,
                          "Scroll the first source fragment above the fixed navigation controls")
        XCTAssertTrue(selected.isHittable, "The first source fragment E must be reachable")
        selected.tap()
        XCTAssertTrue(selected.isSelected, "First gap should hold E")
        app.buttons["exam-previous"].tap()
        app.buttons["exam-next"].tap()
        XCTAssertTrue(selected.isSelected, "First gap should still hold E after navigation")
    }

    func testPublishedGrammarDuplicateBankTokensKeepSelectedIdentity() {
        let app = openPaper("b1-2020")
        app.buttons["exam-module-grammar"].tap()
        let first = app.buttons["exam-bank-certyfikat-polski-B1-2020-grammar-8-bank-2"]
        let second = app.buttons["exam-bank-certyfikat-polski-B1-2020-grammar-8-bank-7"]
        for _ in 0..<55 {
            if first.exists { break }
            app.buttons["exam-next"].tap()
        }
        XCTAssertTrue(first.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(second.exists, app.debugDescription)
        XCTAssertEqual(first.label, "w, 2")
        XCTAssertEqual(second.label, "w, 7")
        second.tap()
        XCTAssertTrue(second.isSelected, "The second printed w must be selected by token ID")
        XCTAssertFalse(first.isSelected, "The first printed w must remain unselected")
        app.buttons["exam-previous"].tap()
        app.buttons["exam-next"].tap()
        XCTAssertTrue(second.isSelected, "The second w token identity must survive navigation")
        XCTAssertFalse(first.isSelected)
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

    func testPublishedC1NovemberRubricAppearsOnlyAfterGrammarSubmit() {
        let app = openPaper("c1-2022-11")
        app.buttons["exam-module-grammar"].tap()
        let rubricId = "exam-rubric-certyfikat-polski-C1-2022-11-g2-q1-c1"
        XCTAssertFalse(app.descendants(matching: .any)[rubricId].exists,
                       "Official rubric must be withheld while answering")
        for _ in 0..<10 { app.buttons["exam-next"].tap() }
        XCTAssertTrue(app.staticTexts["exam-context"].label.contains("Agencje turystyczne"), app.debugDescription)
        let answer = app.textFields["exam-grammar-answer"]
        XCTAssertTrue(answer.waitForExistence(timeout: 5), app.debugDescription)
        answer.tap()
        answer.typeText("Agencje turystyczne odnotowują spadek zainteresowania klientów.")
        XCTAssertFalse(app.descendants(matching: .any)[rubricId].exists,
                       "Rubric cannot reveal official criteria before submit")
        for _ in 0..<100 {
            if app.buttons["exam-submit"].exists { break }
            let next = app.buttons["exam-next"]
            XCTAssertTrue(next.exists, "The published Grammar sequence must reach Submit")
            next.tap()
        }
        let submit = app.buttons["exam-submit"]
        XCTAssertTrue(submit.waitForExistence(timeout: 5), app.debugDescription)
        submit.tap()
        XCTAssertTrue(app.staticTexts["Результаты"].waitForExistence(timeout: 5), app.debugDescription)
        let first = app.descendants(matching: .any)[rubricId]
        for _ in 0..<30 {
            if first.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(first.exists, "Submitted C1 November 2022 review must show source rubric: \(app.debugDescription)")
        XCTAssertTrue(first.label.contains("odnotowują"), first.label)
        XCTAssertTrue(first.label.contains("0,25"), first.label)
        let fourth = app.descendants(matching: .any)["exam-rubric-certyfikat-polski-C1-2022-11-g2-q1-c4"]
        XCTAssertTrue(fourth.exists, "All four source components must render in review")
        XCTAssertTrue(fourth.label.contains("klientów"), fourth.label)
    }

    func testPublishedC1AprilPairRouteShowsSeparateSourceInputs() {
        let app = XCUIApplication()
        app.launch()
        app.buttons["Экзамен"].tap()
        let november = app.buttons["exam-c1-2023-11"]
        let april = app.buttons["exam-c1-2024-04"]
        for _ in 0..<10 {
            if november.exists && april.exists { break }
            app.swipeUp()
        }
        XCTAssertTrue(november.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(april.exists, "Month-specific C1 routes must remain distinct")
        XCTAssertTrue(april.label.contains("2024-04"), april.label)
        april.tap()
        app.buttons["exam-module-grammar"].tap()
        for _ in 0..<7 { app.buttons["exam-next"].tap() }
        XCTAssertEqual(app.staticTexts["exam-context"].label, "(kilka)")
        let answer = app.textFields["exam-grammar-answer"]
        XCTAssertTrue(answer.waitForExistence(timeout: 5), app.debugDescription)
        answer.tap()
        answer.typeText("kilkorgiem")
        app.buttons["exam-next"].tap()
        XCTAssertEqual(app.staticTexts["exam-context"].label, "(przyjaciel)")
        answer.tap()
        answer.typeText("przyjaciół")
        app.buttons["exam-previous"].tap()
        XCTAssertEqual(app.staticTexts["exam-context"].label, "(kilka)")
        XCTAssertEqual(answer.value as? String, "kilkorgiem",
                       "First paired inflection must survive moving to the second source item")
        app.buttons["exam-next"].tap()
        XCTAssertEqual(answer.value as? String, "przyjaciół",
                       "Second paired inflection must survive back navigation")
    }

    func testWindowedAppsMenuResizePreservesAnswerDraftAndSubmit() {
        let app = openPaper("b2-2019")
        let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")
        let tileControls = springboard.buttons["window-controls:dev.polski.grammarmatrix.ios"]
        if app.windows.firstMatch.frame.width < 800 {
            XCTAssertTrue(tileControls.waitForExistence(timeout: 5), springboard.debugDescription)
            tileControls.tap()
            springboard.buttons["Zoom-button"].tap()
        }
        app.buttons["exam-module-listening"].tap()
        let draft = app.textFields["exam-answer-certyfikat-polski-B2-2019-listening-5-1-1"]
        for _ in 0..<6 {
            if draft.exists { break }
            app.buttons["exam-next"].tap()
        }
        XCTAssertTrue(draft.waitForExistence(timeout: 5), app.debugDescription)
        draft.tap()
        draft.typeText("żółć przy zmianie rozmiaru")
        app.staticTexts["exam-part-instruction"].tap()
        let before = app.windows.firstMatch.frame
        XCTAssertGreaterThan(before.width, 800, "Start with the 834-point iPad display")
        let menuReveal = springboard.coordinate(withNormalizedOffset: CGVector(dx: 0.50, dy: 0.01))
        menuReveal.press(forDuration: 0.1, thenDragTo:
            springboard.coordinate(withNormalizedOffset: CGVector(dx: 0.50, dy: 0.11)))
        let windowControls = springboard.buttons["Window Controls"]
        XCTAssertTrue(windowControls.waitForExistence(timeout: 5), springboard.debugDescription)
        windowControls.tap()
        let windowMenu = springboard.buttons["Окно"]
        XCTAssertTrue(windowMenu.waitForExistence(timeout: 5), springboard.debugDescription)
        windowMenu.tap()
        let moveAndResize = springboard.buttons["Move & Resize"]
        XCTAssertTrue(moveAndResize.waitForExistence(timeout: 5), springboard.debugDescription)
        moveAndResize.tap()
        let leftHalf = springboard.buttons["Left"]
        XCTAssertTrue(leftHalf.waitForExistence(timeout: 5), springboard.debugDescription)
        leftHalf.tap()
        let after = app.windows.firstMatch.frame
        print("WINDOWED_APPS_FRAME_BEFORE=\(before) AFTER=\(after)")
        XCTAssertLessThan(after.width, before.width - 100,
                          "The iPadOS Left layout must narrow the app window: \(app.debugDescription)")
        let tiledScreenshot = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        tiledScreenshot.name = "Published exam at 412-point iPad window"
        tiledScreenshot.lifetime = .keepAlways
        add(tiledScreenshot)
        XCTAssertTrue(draft.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertEqual(draft.value as? String, "żółć przy zmianie rozmiaru", app.debugDescription)
        let submit = app.buttons["exam-submit"]
        for _ in 0..<5 {
            if submit.isHittable { break }
            app.swipeUp()
        }
        XCTAssertTrue(submit.isHittable, "Submit must remain reachable after genuine iPad window resize")
        XCTAssertTrue(tileControls.waitForExistence(timeout: 5), springboard.debugDescription)
        tileControls.tap()
        let zoom = springboard.buttons["Zoom-button"]
        XCTAssertTrue(zoom.waitForExistence(timeout: 5), springboard.debugDescription)
        zoom.tap()
        let restored = app.windows.firstMatch.frame
        print("WINDOWED_APPS_FRAME_RESTORED=\(restored)")
        XCTAssertGreaterThanOrEqual(restored.width, before.width - 1,
                                    "The test must leave the app at its original width")
    }

}

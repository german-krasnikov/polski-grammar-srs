package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement

/** Shared "Back/Next/Finish" exam nav button — was copy-pasted as a private `navBtn` in
 *  `ExamReadingWeb.kt`, `ExamListeningWeb.kt`, `ExamWritingWeb.kt` and `ExamGrammarWeb.kt`. */
internal fun navBtn(label: String, primary: Boolean = false, onClick: () -> Unit): HTMLButtonElement =
    (document.createElement("button") as HTMLButtonElement).apply {
        type = "button"; className = if (primary) "exam-nav-btn primary" else "exam-nav-btn"
        textContent = label; addEventListener("click", { onClick() })
    }

package polski.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.HtmlElementView
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.KeyboardEvent
import polski.spike.SpikeRating

@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal actual fun SpikeHtmlInput(
    value: String,
    onValueChange: (String) -> Unit,
    onReveal: () -> Unit,
    modifier: Modifier,
) {
    val change = rememberUpdatedState(onValueChange)
    val reveal = rememberUpdatedState(onReveal)
    HtmlElementView(
        factory = {
            (document.createElement("div") as HTMLDivElement).apply {
                setAttribute("style", "padding:4px;background:#f6f3eb;box-sizing:border-box;width:100%;height:100%")
                val title = document.createElement("h1")
                title.textContent = "Polski Grammar Matrix — web spike"
                title.setAttribute("style", "font-size:1.4rem;margin:0 0 8px")
                appendChild(title)
                val prompt = document.createElement("p")
                prompt.textContent = "Powiedz po polsku: Я говорю по-польски"
                prompt.setAttribute("style", "margin:0 0 8px")
                appendChild(prompt)
                val label = document.createElement("label")
                label.setAttribute("for", "spike-answer")
                label.textContent = "Twoja odpowiedź"
                appendChild(label)
                val textarea = document.createElement("textarea") as HTMLTextAreaElement
                textarea.id = "spike-answer"
                textarea.rows = 2
                textarea.setAttribute("style", "box-sizing:border-box;width:100%;font:inherit")
                textarea.addEventListener("input", { change.value(textarea.value) })
                textarea.addEventListener("keydown", { event ->
                    val key = event as KeyboardEvent
                    if (key.key == "Enter" && !key.shiftKey && !key.isComposing) {
                        key.preventDefault()
                        reveal.value()
                    }
                })
                appendChild(textarea)
            }
        },
        modifier = modifier,
        update = { container ->
            val textarea = container.querySelector("textarea") as HTMLTextAreaElement
            if (textarea.value != value) textarea.value = value
        },
    )
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal actual fun SpikeHtmlTable(modifier: Modifier) {
    HtmlElementView(
        factory = {
            (document.createElement("div") as HTMLDivElement).apply {
                setAttribute("style", "width:100%;height:100%;overflow-x:auto;overflow-y:auto;background:#f6f3eb")
                val table = document.createElement("table")
                table.setAttribute("style", "min-width:420px;border-collapse:collapse;width:100%;font:inherit")
                val caption = document.createElement("caption")
                caption.textContent = "Przypadki / Падежи — kot"
                table.appendChild(caption)
                val header = document.createElement("tr")
                listOf("Przypadek", "Forma").forEach { title ->
                    val cell = document.createElement("th")
                    cell.setAttribute("scope", "col")
                    cell.textContent = title
                    header.appendChild(cell)
                }
                table.appendChild(header)
                listOf(
                    "Mianownik" to "kot",
                    "Dopełniacz" to "kota",
                    "Celownik" to "kotu",
                    "Biernik" to "kota",
                    "Narzędnik" to "kotem",
                    "Miejscownik" to "kocie",
                    "Wołacz" to "kocie",
                ).forEach { (case, form) ->
                    val row = document.createElement("tr")
                    val heading = document.createElement("th")
                    heading.setAttribute("scope", "row")
                    heading.textContent = case
                    row.appendChild(heading)
                    val cell = document.createElement("td")
                    cell.textContent = form
                    row.appendChild(cell)
                    table.appendChild(row)
                }
                appendChild(table)
            }
        },
        modifier = modifier,
    )
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal actual fun SpikeHtmlActions(
    revealed: Boolean,
    rating: SpikeRating?,
    onReveal: () -> Unit,
    onRate: (SpikeRating) -> Unit,
    modifier: Modifier,
) {
    val reveal = rememberUpdatedState(onReveal)
    val rate = rememberUpdatedState(onRate)
    HtmlElementView(
        factory = {
            (document.createElement("div") as HTMLDivElement).apply {
                setAttribute("style", "box-sizing:border-box;width:100%;height:100%;padding:4px;background:#f6f3eb;font:inherit")
                val answer = document.createElement("p")
                answer.id = "spike-answer-key"
                appendChild(answer)
                val revealButton = document.createElement("button")
                revealButton.id = "spike-reveal"
                revealButton.textContent = "Pokaż odpowiedź"
                revealButton.addEventListener("click", { reveal.value() })
                appendChild(revealButton)
                val ratings = document.createElement("div")
                ratings.id = "spike-ratings"
                ratings.setAttribute("style", "display:none;gap:8px;flex-wrap:wrap")
                SpikeRating.entries.forEach { choice ->
                    val button = document.createElement("button")
                    button.textContent = choice.name.lowercase().replaceFirstChar { it.uppercase() }
                    button.addEventListener("click", { rate.value(choice) })
                    ratings.appendChild(button)
                }
                appendChild(ratings)
                val status = document.createElement("p")
                status.id = "spike-rating-status"
                status.setAttribute("aria-live", "polite")
                appendChild(status)
            }
        },
        modifier = modifier,
        update = { container ->
            val answer = container.querySelector("#spike-answer-key")
            answer?.textContent = if (revealed) "Odpowiedź: Mówię po polsku" else ""
            val revealButton = container.querySelector("#spike-reveal")
            val ratings = container.querySelector("#spike-ratings")
            if (revealed) {
                revealButton?.setAttribute("hidden", "")
                ratings?.setAttribute("style", "display:flex;gap:8px;flex-wrap:wrap")
            } else {
                revealButton?.removeAttribute("hidden")
                ratings?.setAttribute("style", "display:none;gap:8px;flex-wrap:wrap")
            }
            container.querySelector("#spike-rating-status")?.textContent =
                rating?.let { "Ocena: ${it.name}" }.orEmpty()
        },
    )
}

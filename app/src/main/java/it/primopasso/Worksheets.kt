package it.primopasso

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ScaleInput(value: Int, low: String, high: String, onChange: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", color = LavenderDeep, fontFamily = Serif, fontSize = 52.sp)
        Slider(
            value = value.toFloat(), onValueChange = { onChange(it.toInt()) }, valueRange = 0f..10f, steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = LavenderDeep, activeTrackColor = Lavender, inactiveTrackColor = LavenderMist,
                activeTickColor = Color.Transparent, inactiveTickColor = Color.Transparent
            )
        )
        Row(Modifier.fillMaxWidth()) {
            Text("0 · $low", color = PlumFaint, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("10 · $high", color = PlumFaint, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FieldInput(f: WField, value: String, onChange: (String) -> Unit) {
    when (f) {
        is WField.Text -> {
            OutlinedTextField(
                value = value, onValueChange = onChange,
                placeholder = { Text(f.hint.ifBlank { "Scrivi qui…" }, color = PlumFaint) },
                modifier = Modifier.fillMaxWidth(), minLines = f.lines, shape = RoundedCornerShape(20.dp), colors = softFieldColors()
            )
            if (f.examples.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Oppure scegli:", color = PlumFaint, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    f.examples.forEach { ex -> Chip(ex, value == ex) { onChange(ex) } }
                }
            }
        }
        is WField.Scale -> ScaleInput(value.toIntOrNull() ?: 5, f.low, f.high) { onChange(it.toString()) }
        is WField.Choice -> {
            val sel = value.split(", ").filter { it.isNotBlank() }.toSet()
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                f.options.forEach { o ->
                    val on = o in sel
                    Chip(o, on) {
                        val next = if (f.multi) (if (on) sel - o else sel + o) else setOf(o)
                        onChange(f.options.filter { it in next }.joinToString(", "))
                    }
                }
            }
        }
        is WField.Lines -> {
            val parts = value.split("\n").let { l -> List(f.count) { i -> l.getOrElse(i) { "" } } }
            parts.forEachIndexed { i, v ->
                OutlinedTextField(
                    value = v,
                    onValueChange = { nv -> onChange(parts.toMutableList().also { it[i] = nv }.joinToString("\n")) },
                    placeholder = { Text(f.hints.getOrElse(i) { "${i + 1}." }, color = PlumFaint) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(18.dp), colors = softFieldColors()
                )
            }
        }
        is WField.Info -> {
            SoftCard(tint = lerpColor(Color.White, Lavender, 0.14f)) {
                Body(f.text, Plum, 16)
            }
        }
    }
}

/** Compilazione guidata di una scheda, un campo per pagina. */
@Composable
fun WorksheetFlow(prefs: Prefs, ws: Worksheet, existing: WorksheetEntry?, onClose: () -> Unit, onStart: (String) -> Unit) {
    var page by remember { mutableIntStateOf(if (existing != null) ws.fields.size else -1) }
    var answers by remember { mutableStateOf(existing?.answers ?: emptyMap()) }
    val time = remember { existing?.time ?: System.currentTimeMillis() }
    val total = ws.fields.size

    fun save() = prefs.saveWorksheet(WorksheetEntry(time, ws.id, answers))

    PageColumn(onBack = onClose) {
        Overline("Modulo ${ws.module} · scheda di lavoro")
        Spacer(Modifier.height(8.dp))
        when {
            page < 0 -> {
                Display(ws.title, 30)
                Spacer(Modifier.height(6.dp))
                Text(ws.subtitle, color = LavenderDeep, fontSize = 15.sp)
                Spacer(Modifier.height(16.dp))
                Body(ws.intro, Plum, 16)
                Spacer(Modifier.height(10.dp))
                Body("$total passaggi · puoi lasciare vuoto ciò che non ti va di scrivere.", PlumFaint, 13)
                Spacer(Modifier.height(28.dp))
                PrimaryButton("Iniziamo", Modifier.fillMaxWidth()) { page = 0 }
            }
            page < total -> {
                val f = ws.fields[page]
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(ws.title, color = PlumSoft, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text("${page + 1} di $total", color = PlumFaint, fontSize = 13.sp)
                }
                Spacer(Modifier.height(8.dp))
                ProgressLine((page + 1) / total.toFloat())
                Spacer(Modifier.height(26.dp))
                Display(f.prompt, 24)
                Spacer(Modifier.height(18.dp))
                FieldInput(f, answers[f.key] ?: (if (f is WField.Scale) "5" else "")) { v -> answers = answers + (f.key to v) }
                Spacer(Modifier.height(28.dp))
                PrimaryButton(if (page == total - 1) "Completa" else "Avanti", Modifier.fillMaxWidth()) {
                    if (f is WField.Scale && answers[f.key] == null) answers = answers + (f.key to "5")
                    if (page == total - 1) save()
                    page += 1
                }
                if (page > 0) {
                    Spacer(Modifier.height(10.dp))
                    GhostButton("Indietro", Modifier.fillMaxWidth()) { page -= 1 }
                }
            }
            else -> {
                Display(ws.title, 28)
                Spacer(Modifier.height(4.dp))
                Text(
                    Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("EEEE d MMMM", IT)),
                    color = PlumFaint, fontSize = 13.sp
                )
                Spacer(Modifier.height(16.dp))
                WorksheetSummary(ws, answers)
                Spacer(Modifier.height(22.dp))
                val step = ws.stepKey?.let { answers[it] }?.takeIf { it.isNotBlank() }
                if (step != null) {
                    PrimaryButton("Inizia ora: ${step.take(40)}${if (step.length > 40) "…" else ""}", Modifier.fillMaxWidth()) { onStart(step) }
                    Spacer(Modifier.height(10.dp))
                }
                GhostButton("Modifica", Modifier.fillMaxWidth()) { page = 0 }
                Spacer(Modifier.height(10.dp))
                GhostButton("Chiudi", Modifier.fillMaxWidth(), onClose)
            }
        }
    }
}

@Composable
fun WorksheetSummary(ws: Worksheet, answers: Map<String, String>) {
    SoftCard {
        ws.fields.filter { it !is WField.Info }.forEach { f ->
            val v = answers[f.key].orEmpty()
            if (v.isNotBlank()) {
                Text(f.prompt, color = LavenderDeep, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(if (f is WField.Scale) "$v / 10" else v, color = Plum, fontSize = 15.sp, lineHeight = 21.sp)
                Spacer(Modifier.height(12.dp))
            }
        }
        // confronto prima/dopo, quando esiste
        val before = answers["prima"]?.toIntOrNull() ?: answers["credo_prima"]?.toIntOrNull()
        val after = answers["dopo"]?.toIntOrNull() ?: answers["credo_dopo"]?.toIntOrNull()
        if (before != null && after != null) {
            SoftCard(tint = lerpColor(Color.White, Lavender, 0.18f), padding = PaddingValues(14.dp)) {
                Text(
                    when {
                        after < before -> "Da $before a $after: è sceso di ${before - after} punti."
                        after == before -> "È rimasto a $before: va bene, a volte serve ripetere."
                        else -> "È salito da $before a $after: capita, riprova con calma più avanti."
                    },
                    color = Plum, fontSize = 15.sp, fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// =============================== SCHEDA STRUMENTI ===============================

@Composable
fun ToolsTab(prefs: Prefs, version: Int, onOpen: (String) -> Unit, onEntry: (Long) -> Unit) {
    val entries = remember(version) { prefs.worksheetEntries() }
    val fmt = DateTimeFormatter.ofPattern("d MMM", IT)
    TabColumn {
        Spacer(Modifier.height(6.dp))
        Display("Strumenti", 30)
        Spacer(Modifier.height(8.dp))
        Body("Le schede di lavoro della terapia cognitivo-comportamentale, guidate passo passo. Scrivere aiuta a vedere con chiarezza: ogni scheda resta salvata e puoi rileggerla.")
        if (entries.isNotEmpty()) {
            SectionTitle("Le tue schede", "${entries.size} compilate")
            entries.take(6).forEach { e ->
                val ws = worksheetById(e.worksheetId) ?: return@forEach
                val first = ws.fields.firstOrNull { it is WField.Text }?.let { e.answers[it.key] }.orEmpty()
                SoftCard(padding = PaddingValues(16.dp), modifier = Modifier.padding(bottom = 8.dp), onClick = { onEntry(e.time) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(ws.title, color = Plum, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            if (first.isNotBlank()) Text(first, color = PlumSoft, fontSize = 13.sp, maxLines = 1)
                        }
                        Text(Instant.ofEpochMilli(e.time).atZone(ZoneId.systemDefault()).format(fmt), color = PlumFaint, fontSize = 12.sp)
                    }
                }
            }
        }
        modules.forEach { m ->
            val list = worksheets.filter { it.module == m.n }
            if (list.isNotEmpty()) {
                SectionTitle("Modulo ${m.n} · ${m.title}")
                list.forEach { ws ->
                    val n = entries.count { it.worksheetId == ws.id }
                    SoftCard(padding = PaddingValues(16.dp), modifier = Modifier.padding(bottom = 10.dp), onClick = { onOpen(ws.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(ws.title, color = Plum, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text(ws.subtitle, color = PlumSoft, fontSize = 13.sp)
                            }
                            if (n > 0) Text("$n×", color = LavenderDeep, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("  ›", color = LavenderDeep, fontSize = 22.sp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Body(
            "Le schede si ispirano a strumenti usati nella terapia cognitivo-comportamentale. Sono esercizi di auto-aiuto e non sostituiscono un percorso con un professionista.",
            PlumFaint, 12
        )
    }
}

// =============================== PERCORSO (lezioni per modulo) ===============================

@Composable
fun ProgramTab(prefs: Prefs, version: Int, onLesson: (Int) -> Unit) {
    val done = remember(version) { prefs.lessonsDone() }
    val next = remember(version) { prefs.nextLesson() }
    val profile = remember(version) { profileById(prefs.blockProfile) }
    TabColumn {
        Spacer(Modifier.height(6.dp))
        Display("Il percorso\nin 8 moduli", 30)
        Spacer(Modifier.height(8.dp))
        Body("Un programma basato sui principi della terapia cognitivo-comportamentale per la procrastinazione. Una lezione al mattino, cinque minuti, da leggere o ascoltare, con un esercizio collegato. Consigliato: un modulo a settimana.")
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${done.size} di ${lessons.size} lezioni", color = Plum, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("${done.size * 100 / lessons.size}%", color = LavenderDeep, fontSize = 14.sp)
        }
        Spacer(Modifier.height(10.dp))
        ProgressLine(done.size / lessons.size.toFloat())

        if (profile != null) {
            val forMe = personalLessonIds(prefs.quizAnswers(), profile).mapNotNull { lessonById(it) }
            SectionTitle("Scelte per te", profile.title)
            forMe.take(4).forEach { l -> LessonRow(l, l.id in done, l.id == next?.id) { onLesson(l.id) } }
        }

        modules.forEach { m ->
            val ls = lessons.filter { it.module == m.n }
            val dm = ls.count { it.id in done }
            SectionTitle("Modulo ${m.n} · ${m.title}", "${m.goal} · $dm di ${ls.size}")
            ls.forEach { l -> LessonRow(l, l.id in done, l.id == next?.id) { onLesson(l.id) } }
        }
    }
}

@Composable
private fun LessonRow(l: Lesson, isDone: Boolean, isNext: Boolean, onClick: () -> Unit) {
    SoftCard(
        padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        modifier = Modifier.padding(bottom = 8.dp),
        tint = when {
            isNext -> lerpColor(Color.White, Rose, 0.2f)
            isDone -> lerpColor(Color.White, Lavender, 0.12f)
            else -> CardWhite
        },
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (isDone) LavenderDeep else LavenderMist),
                contentAlignment = Alignment.Center
            ) { Text(if (isDone) "✓" else "${l.id}", color = if (isDone) Color.White else LavenderDeep, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(l.title, color = Plum, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                if (l.worksheet != null) Text("con scheda: ${worksheetById(l.worksheet)?.title}", color = PlumFaint, fontSize = 12.sp)
            }
            if (isNext) Text("oggi", color = LavenderDeep, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// =============================== TERMOMETRO (andamento) ===============================

@Composable
fun ThermometerTrend(prefs: Prefs, version: Int) {
    val checks = remember(version) { prefs.checkins().filter { it.scores.isNotEmpty() }.reversed() }
    if (checks.isEmpty()) return
    SectionTitle("Il tuo termometro nel tempo", "Dai check-in settimanali, da 0 a 10")
    SoftCard {
        thermometer.forEach { (key, label) ->
            Text(label, color = Plum, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.height(54.dp)) {
                checks.takeLast(8).forEach { c ->
                    val v = c.scores[key] ?: 0
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$v", color = PlumSoft, fontSize = 10.sp)
                        Box(
                            Modifier
                                .width(20.dp)
                                .height((4 + v * 3.6).dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (key == "capace") Lavender else Rose)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        Body("Non è un test clinico: è un modo per vedere come cambia il tuo rapporto con il rimandare, settimana dopo settimana.", PlumFaint, 12, TextAlign.Start)
    }
}

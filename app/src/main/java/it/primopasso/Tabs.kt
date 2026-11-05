package it.primopasso

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@Composable
private fun BigStartCard(onStart: () -> Unit) {
    SoftCard(tint = lerpColor(Color.White, Lavender, 0.22f), onClick = onStart) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BlendOrb(listOf(Lavender, Rose), 64.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Inizia ora", color = Plum, fontFamily = Serif, fontSize = 23.sp)
                Body("Stai rimandando qualcosa? Troviamo il primo passo. Solo 5 minuti.", size = 14)
            }
        }
        Spacer(Modifier.height(14.dp))
        PrimaryButton("Trova il primo passo", Modifier.fillMaxWidth(), onClick = onStart)
    }
}

@Composable
private fun LessonCard(prefs: Prefs, version: Int, onLesson: (Int) -> Unit) {
    val next = remember(version) { prefs.nextLesson() }
    val doneToday = remember(version) { prefs.lessonDoneToday() }
    val todayId = remember(version) { prefs.lessonTodayId() }
    when {
        next == null -> SoftCard(onClick = { onLesson(1) }) {
            Text("Hai completato tutte le 30 lezioni 🌼", color = Plum, fontFamily = Serif, fontSize = 20.sp)
            Body("Puoi rileggerle quando vuoi.", size = 13)
        }
        doneToday -> SoftCard(onClick = { lessonById(todayId)?.let { onLesson(it.id) } }) {
            Overline("Lezione di oggi fatta ✓")
            Spacer(Modifier.height(6.dp))
            Text(lessonById(todayId)?.title ?: "", color = Plum, fontFamily = Serif, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Body("Domani ti aspetta: ${next.title}. Saltare un giorno non rompe nulla.", size = 13)
            Spacer(Modifier.height(8.dp))
            Text(
                "Leggila adesso, se vuoi →", color = LavenderDeep, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onLesson(next.id) }
                    .padding(vertical = 4.dp)
            )
        }
        else -> SoftCard(tint = lerpColor(Color.White, Rose, 0.18f), onClick = { onLesson(next.id) }) {
            Overline("Lezione ${next.id} di ${lessons.size} · 5 minuti")
            Spacer(Modifier.height(6.dp))
            Text(next.title, color = Plum, fontFamily = Serif, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Body(next.body.substringBefore("\n\n"), size = 14)
            Spacer(Modifier.height(12.dp))
            PrimaryButton("Leggi la lezione", color = lerpColor(Rose, LavenderDeep, 0.6f)) { onLesson(next.id) }
        }
    }
}

@Composable
private fun PendingRow(t: PendingTask, onStart: () -> Unit, onRemove: (() -> Unit)?) {
    SoftCard(padding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), modifier = Modifier.padding(bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(categoryById(t.category).symbol, color = LavenderDeep, fontSize = 18.sp)
            Spacer(Modifier.width(12.dp))
            Text(t.text, color = Plum, fontSize = 15.sp, modifier = Modifier.weight(1f))
            if (onRemove != null) {
                Text(
                    "×", color = PlumFaint, fontSize = 20.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onRemove)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
            Text(
                "Inizia", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(LavenderDeep)
                    .clickable(onClick = onStart)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

// =============================== OGGI ===============================

@Composable
fun HomeTab(
    prefs: Prefs,
    version: Int,
    onStart: () -> Unit,
    onStartPending: (Long) -> Unit,
    onLesson: (Int) -> Unit,
    onProfile: () -> Unit,
    onOpenTab: (Int) -> Unit,
    onSettings: () -> Unit
) {
    val profile = remember(version) { profileById(prefs.blockProfile) }
    val pending = remember(version) { prefs.pending() }
    val week = remember(version) {
        val since = LocalDate.now().toEpochDay() - 6
        prefs.startLog().filter { it.epochDay >= since }
    }
    val nudge = dailyNudges[(LocalDate.now().toEpochDay() % dailyNudges.size).toInt()]

    TabColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                val d = LocalDate.now()
                Overline("${d.dayOfWeek.getDisplayName(TextStyle.FULL, IT)} ${d.dayOfMonth} ${d.month.getDisplayName(TextStyle.FULL, IT)}")
                Spacer(Modifier.height(4.dp))
                Display(if (prefs.name.isBlank()) greeting() else "${greeting()}, ${prefs.name}", 28)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Impostazioni", tint = PlumSoft)
            }
        }
        Spacer(Modifier.height(6.dp))
        QuoteText(nudge, 17, color = PlumSoft)
        Spacer(Modifier.height(18.dp))

        BigStartCard(onStart)

        SectionTitle("La lezione del mattino")
        LessonCard(prefs, version, onLesson)

        if (pending.isNotEmpty()) {
            SectionTitle("Le cose che rimandi", "Una alla volta, quando vuoi")
            pending.take(3).forEach { t -> PendingRow(t, onStart = { onStartPending(t.id) }, onRemove = null) }
            if (pending.size > 3) {
                Text(
                    "Vedi tutte (${pending.size}) →", color = LavenderDeep, fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { onOpenTab(1) }
                        .padding(vertical = 6.dp)
                )
            }
        }

        SectionTitle("Questa settimana")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("${week.size}", "inizi", Modifier.weight(1f))
            StatTile("${week.sumOf { it.minutes }}", "minuti", Modifier.weight(1f))
            StatTile("${week.count { it.finished }}", "cose finite", Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Body("Qui non ci sono serie da perdere: ogni inizio si somma.", PlumFaint, 12)

        SectionTitle("Il tuo profilo")
        SoftCard(onClick = onProfile) {
            if (profile == null) {
                Text("Scopri come ti blocchi", color = Plum, fontFamily = Serif, fontSize = 19.sp)
                Body("Nove domande per ricevere esercizi su misura.", size = 13)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(profile.symbol, Lavender)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(profile.title, color = Plum, fontFamily = Serif, fontSize = 18.sp)
                        Body(profile.tip, size = 13)
                    }
                }
            }
        }
    }
}

// =============================== INIZIA ===============================

@Composable
fun StartTab(prefs: Prefs, version: Int, onStart: () -> Unit, onStartPending: (Long) -> Unit, onKit: (String) -> Unit, onChanged: () -> Unit) {
    val pending = remember(version) { prefs.pending() }
    val profile = remember(version) { profileById(prefs.blockProfile) }
    var newText by remember { mutableStateOf("") }

    TabColumn {
        Spacer(Modifier.height(6.dp))
        Display("Inizia ora", 30)
        Spacer(Modifier.height(8.dp))
        Body("Non devi finire. Devi solo iniziare: il primo gesto, cinque minuti, poi decidi tu.")
        Spacer(Modifier.height(18.dp))
        BigStartCard(onStart)

        SectionTitle("Le cose che rimandi", "Scrivile qui: escono dalla testa e restano al sicuro")
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newText,
                onValueChange = { newText = it },
                placeholder = { Text("Aggiungi una cosa…", color = PlumFaint) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = softFieldColors()
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Aggiungi", color = LavenderDeep, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        if (newText.isNotBlank()) {
                            prefs.addPending(newText, "altro")
                            newText = ""
                            onChanged()
                        }
                    }
                    .padding(10.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        if (pending.isEmpty()) {
            Body("Nessuna cosa in sospeso salvata. Che sollievo, oppure è il momento di scriverne una.", PlumFaint, 13)
        }
        pending.forEach { t ->
            PendingRow(t, onStart = { onStartPending(t.id) }, onRemove = {
                prefs.removePending(t.id)
                onChanged()
            })
        }

        if (profile != null) {
            SectionTitle("Per come funzioni tu", profile.title)
            profile.kitIds.mapNotNull { kitById(it) }.forEach { KitRow(it, true) { onKit(it.id) } }
        }
    }
}

// =============================== LEZIONI ===============================

@Composable
fun LessonsTab(prefs: Prefs, version: Int, onLesson: (Int) -> Unit) {
    val done = remember(version) { prefs.lessonsDone() }
    val next = remember(version) { prefs.nextLesson() }
    val profile = remember(version) { profileById(prefs.blockProfile) }

    TabColumn {
        Spacer(Modifier.height(6.dp))
        Display("30 lezioni\ndel mattino", 30)
        Spacer(Modifier.height(8.dp))
        Body("Una al giorno, cinque minuti, con un micro-inizio da provare subito. Si va avanti quando vuoi tu: saltare un giorno non rompe nulla.")
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${done.size} di ${lessons.size} lezioni", color = Plum, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("${done.size * 100 / lessons.size}%", color = LavenderDeep, fontSize = 14.sp)
        }
        Spacer(Modifier.height(10.dp))
        ProgressLine(done.size / lessons.size.toFloat())
        Spacer(Modifier.height(18.dp))
        LessonCard(prefs, version, onLesson)

        if (profile != null) {
            val forMe = profile.lessonIds.mapNotNull { lessonById(it) }
            SectionTitle("Consigliate per te", profile.title)
            forMe.forEach { l ->
                SoftCard(padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp), modifier = Modifier.padding(bottom = 8.dp), onClick = { onLesson(l.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Dot(l.id in done, 30, if (l.id in done) "✓" else "${l.id}")
                        Spacer(Modifier.width(12.dp))
                        Text(l.title, color = Plum, fontSize = 15.sp, modifier = Modifier.weight(1f))
                        Text("›", color = LavenderDeep, fontSize = 22.sp)
                    }
                }
            }
        }

        SectionTitle("Tutte le lezioni")
        lessons.forEach { l ->
            val isDone = l.id in done
            SoftCard(
                padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                modifier = Modifier.padding(bottom = 8.dp),
                tint = if (isDone) lerpColor(Color.White, Lavender, 0.12f) else CardWhite,
                onClick = { onLesson(l.id) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Dot(isDone, 30, if (isDone) "✓" else "${l.id}")
                    Spacer(Modifier.width(12.dp))
                    Text(l.title, color = if (isDone || l.id == next?.id) Plum else PlumSoft, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    if (l.id == next?.id) Text("oggi", color = LavenderDeep, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// =============================== KIT ===============================

@Composable
fun KitTab(prefs: Prefs, version: Int, onKit: (String) -> Unit) {
    val profile = remember(version) { profileById(prefs.blockProfile) }
    TabColumn {
        Spacer(Modifier.height(6.dp))
        Display("Kit per partire", 30)
        Spacer(Modifier.height(8.dp))
        Body("Tecniche brevi e pratiche per quando sei bloccata. Usale da sole o prima di \"Inizia ora\".")
        if (profile != null) {
            SectionTitle("Per come funzioni tu", profile.title)
            profile.kitIds.mapNotNull { kitById(it) }.forEach { KitRow(it, true) { onKit(it.id) } }
            SectionTitle("Tutte le tecniche")
            startKit.filter { it.id !in profile.kitIds }.forEach { KitRow(it, false) { onKit(it.id) } }
        } else {
            Spacer(Modifier.height(16.dp))
            startKit.forEach { KitRow(it, false) { onKit(it.id) } }
        }
    }
}

// =============================== REGISTRO ===============================

@Composable
fun LogTab(prefs: Prefs, version: Int, onStart: () -> Unit) {
    val log = remember(version) { prefs.startLog() }
    val fmt = DateTimeFormatter.ofPattern("EEE d MMM · HH:mm", IT)
    TabColumn {
        Spacer(Modifier.height(6.dp))
        Display("Il tuo registro", 30)
        Spacer(Modifier.height(8.dp))
        Body("Quando e perché tendi a rimandare, e quanti inizi hai già fatto. Nessun giudizio: solo informazioni utili per conoscerti.")
        Spacer(Modifier.height(16.dp))
        if (log.isEmpty()) {
            SoftCard(onClick = onStart) {
                Body("Ogni volta che usi \"Inizia ora\", qui vedrai i tuoi inizi, i motivi più frequenti e i momenti in cui parti meglio.", size = 14)
                Spacer(Modifier.height(10.dp))
                Text("Fai il primo inizio →", color = LavenderDeep, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            return@TabColumn
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("${log.size}", "inizi\nin tutto", Modifier.weight(1f))
            StatTile("${log.sumOf { it.minutes }}", "minuti\ndi azione", Modifier.weight(1f))
            StatTile("${log.count { it.finished }}", "cose\nfinite", Modifier.weight(1f))
        }

        // ultimi 7 giorni
        SectionTitle("Gli ultimi 7 giorni")
        SoftCard {
            val today = LocalDate.now().toEpochDay()
            val counts = (6 downTo 0).map { k -> today - k to log.count { it.epochDay == today - k } }
            val max = (counts.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
            Row(Modifier.fillMaxWidth().height(110.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                counts.forEach { (ep, n) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (n > 0) "$n" else "", color = PlumSoft, fontSize = 11.sp)
                        Box(
                            Modifier
                                .width(22.dp)
                                .height((6 + 70 * n / max).dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (n > 0) Lavender else LavenderMist)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(LocalDate.ofEpochDay(ep).dayOfWeek.getDisplayName(TextStyle.SHORT, IT).take(3), color = PlumSoft, fontSize = 11.sp)
                    }
                }
            }
        }

        val reasons = log.filter { it.reason.isNotBlank() }.groupingBy { it.reason }.eachCount().entries.sortedByDescending { it.value }
        if (reasons.isNotEmpty()) {
            SectionTitle("Perché rimandi", "Il motivo più frequente è il punto su cui lavorare")
            SoftCard {
                val max = reasons.first().value
                reasons.forEach { (r, n) -> BarRow(r, n, max, Rose) }
                Spacer(Modifier.height(8.dp))
                val top = reasons.first().key
                Body(
                    when (top) {
                        "È troppo grande" -> "Consiglio: \"Il passo ridicolo\" nel Kit."
                        "Non so da dove iniziare" -> "Consiglio: \"Svuota la testa\" nel Kit."
                        "Paura di sbagliare" -> "Consiglio: \"La bozza brutta\" nel Kit."
                        "È noioso" -> "Consiglio: \"Prima il passo, poi il premio\" nel Kit."
                        "Sono stanca" -> "Consiglio: inizi da due minuti e un \"Sospiro per ripartire\"."
                        "Ho troppe cose" -> "Consiglio: \"Svuota la testa\", poi una cosa sola."
                        else -> "Osserva cosa succede la prossima volta che rimandi: è già un passo."
                    },
                    LavenderDeep, 13
                )
            }
        }

        SectionTitle("Quando inizi meglio")
        SoftCard {
            val slots = listOf(
                "Mattina" to log.count { it.hour in 5..11 },
                "Pomeriggio" to log.count { it.hour in 12..17 },
                "Sera" to log.count { it.hour >= 18 || it.hour < 5 }
            )
            val max = slots.maxOf { it.second }
            slots.forEach { (l, n) -> BarRow(l, n, max, Lavender) }
        }

        val cats = log.groupingBy { it.category }.eachCount().entries.sortedByDescending { it.value }
        SectionTitle("Su cosa")
        SoftCard {
            val max = cats.first().value
            cats.forEach { (c, n) -> val cat = categoryById(c); BarRow("${cat.symbol} ${cat.label}", n, max, Peach) }
        }

        SectionTitle("Gli ultimi inizi")
        log.take(15).forEach { e ->
            SoftCard(padding = PaddingValues(16.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(categoryById(e.category).symbol, color = LavenderDeep, fontSize = 18.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(e.task, color = Plum, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text(
                            Instant.ofEpochMilli(e.time).atZone(ZoneId.systemDefault()).format(fmt) +
                                " · ${e.minutes} min" + if (e.reason.isNotBlank()) " · ${e.reason.lowercase()}" else "",
                            color = PlumFaint, fontSize = 12.sp
                        )
                    }
                    if (e.finished) Text("✓", color = LavenderDeep, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

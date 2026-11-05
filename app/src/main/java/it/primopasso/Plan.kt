package it.primopasso

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// =============================== VOCE (lezioni da ascoltare) ===============================

class Speaker(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    var ready = false
        private set
    var italian = true
        private set
    private var onEnd: (() -> Unit)? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val r = tts.setLanguage(Locale.ITALIAN)
            italian = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
            tts.setSpeechRate(0.9f)
            tts.setPitch(1.0f)
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) { if (utteranceId == "end") onEnd?.invoke() }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) { onEnd?.invoke() }
            })
            ready = true
        }
    }

    fun speak(parts: List<String>, whenDone: () -> Unit): Boolean {
        if (!ready) return false
        onEnd = whenDone
        tts.stop()
        parts.forEachIndexed { i, p ->
            tts.speak(p, if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, if (i == parts.lastIndex) "end" else "p$i")
            tts.playSilentUtterance(450, TextToSpeech.QUEUE_ADD, "s$i")
        }
        return true
    }

    fun stop() = tts.stop()

    fun shutdown() {
        try {
            tts.stop()
            tts.shutdown()
        } catch (_: Exception) {
        }
    }
}

// =============================== COMPAGNA DI INIZIO ===============================

fun shareIntention(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    try {
        context.startActivity(Intent.createChooser(send, "Dillo a un'amica").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
    }
}

// =============================== IL TUO PIANO ===============================

@Composable
private fun TimelineItem(title: String, text: String, last: Boolean) {
    Row {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(3.dp, LavenderDeep, CircleShape)
                    .background(Color.White)
            )
            if (!last) Box(Modifier.width(3.dp).height(58.dp).background(LavenderDeep.copy(alpha = 0.5f)))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.padding(bottom = if (last) 0.dp else 10.dp)) {
            Text(title, color = Plum, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(text, color = PlumSoft, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
fun PlanScreen(prefs: Prefs, buttonLabel: String, onBack: (() -> Unit)?, onDone: () -> Unit) {
    val a = remember { prefs.quizAnswers() }
    val p = profileById(prefs.blockProfile)
    val kit = personalKitIds(a, p).mapNotNull { kitById(it)?.title }
    val lessonsForMe = personalLessonIds(a, p)
    val areas = (a["aree"] ?: emptySet()).mapNotNull { id -> startCategories.firstOrNull { it.id == id }?.label?.lowercase() }
    val items = listOf(
        "Una lezione di 5 minuti ogni mattina" to "Alle ${formatTime(prefs.morningMinutes)}, da leggere o ascoltare. ${lessonsForMe.size} scelte per te, anche su fiducia e insicurezza.",
        "Micro-inizi da ${prefs.preferredMinutes} minuti" to (if (areas.isEmpty()) "Per tutto ciò che rimandi, un primo passo minuscolo e un timer gentile." else "Per ${areas.joinToString(", ")}: un primo passo minuscolo e un timer gentile."),
        "Il tuo kit personale" to kit.joinToString(" · "),
        "Un check-in ogni settimana" to "Per vedere i progressi, alleggerire ciò che pesa e scegliere l'obiettivo successivo.",
        "Obiettivi e piccole abitudini" to "Senza serie da perdere: saltare un giorno non rompe nulla.",
        "Pensieri che bloccano" to "Uno strumento ispirato alle tecniche cognitivo-comportamentali per riformulare i pensieri che ti fermano.",
        "Una compagna di inizio" to "Condividi la tua intenzione con un'amica: alleate, non giudici."
    )
    val content: @Composable () -> Unit = {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            BlendOrb(listOf(Lavender, Rose, Peach), 96.dp)
            Spacer(Modifier.height(16.dp))
            Overline("Il tuo piano")
            Spacer(Modifier.height(6.dp))
            Display(if (prefs.name.isBlank()) "Costruito su di te" else "${prefs.name}, ecco il tuo piano", 27, align = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            if (p != null) Body("${p.symbol}  ${p.name}", LavenderDeep, 14, TextAlign.Center)
        }
        Spacer(Modifier.height(22.dp))
        SoftCard {
            items.forEachIndexed { i, (t, d) -> TimelineItem(t, d, i == items.lastIndex) }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("🌱" to "Passi\npiccoli", "🎯" to "Obiettivi", "✅" to "Abitudini", "🤝" to "Alleate").forEach { (e, l) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(76.dp)) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(lerpColor(Color.White, Lavender, 0.2f)),
                        contentAlignment = Alignment.Center
                    ) { Text(e, fontSize = 24.sp) }
                    Spacer(Modifier.height(6.dp))
                    Text(l, color = Plum, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 15.sp)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        PrimaryButton(buttonLabel, Modifier.fillMaxWidth(), onClick = onDone)
        Spacer(Modifier.height(10.dp))
        Body("Il piano si adatta a te: puoi rifare il questionario quando vuoi.", PlumFaint, 12, TextAlign.Center, Modifier.fillMaxWidth())
    }
    if (onBack != null) PageColumn(onBack = onBack) { content() } else CenterPage(center = false) { content() }
}

// =============================== CHECK-IN SETTIMANALE ===============================

private val moodFaces = listOf("😣", "😕", "😐", "🙂", "😊")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckinFlow(prefs: Prefs, onClose: () -> Unit) {
    val context = LocalContext.current
    var stage by remember { mutableIntStateOf(0) }
    var mood by remember { mutableIntStateOf(3) }
    var well by remember { mutableStateOf(setOf<String>()) }
    var lighten by remember { mutableStateOf(setOf<String>()) }
    var note by remember { mutableStateOf("") }
    var target by remember { mutableIntStateOf(if (prefs.weeklyTarget > 0) prefs.weeklyTarget else 5) }
    var intention by remember { mutableStateOf(prefs.weeklyIntention) }
    val since = LocalDate.now().toEpochDay() - 6
    val week = remember { prefs.startLog().filter { it.epochDay >= since } }
    val habitsDone = remember { prefs.habits().sumOf { h -> (0..6).count { prefs.habitDone(h.id, since + it) } } }

    PageColumn(accent = Rose, onBack = onClose) {
        Overline("Check-in della settimana")
        Spacer(Modifier.height(8.dp))
        when (stage) {
            0 -> {
                Display("Com'è andata\nquesta settimana?", 28)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("${week.size}", "inizi", Modifier.weight(1f))
                    StatTile("${week.sumOf { it.minutes }}", "minuti", Modifier.weight(1f))
                    StatTile("$habitsDone", "abitudini\nspuntate", Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Body(
                    if (week.isEmpty()) "Nessun inizio registrato: va bene così. Il check-in serve proprio a ripartire, senza giudizio."
                    else "Ogni numero qui è un momento in cui hai scelto di iniziare.",
                    PlumSoft, 14
                )
                SectionTitle("Come ti senti, in generale?")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    moodFaces.forEachIndexed { i, f ->
                        Box(
                            Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(if (mood == i + 1) lerpColor(Color.White, Lavender, 0.45f) else Color(0x99FFFFFF))
                                .clickable { mood = i + 1 },
                            contentAlignment = Alignment.Center
                        ) { Text(f, fontSize = 26.sp) }
                    }
                }
                Spacer(Modifier.height(28.dp))
                PrimaryButton("Continua", Modifier.fillMaxWidth()) { stage = 1 }
            }
            1 -> {
                Display("Cosa è andato bene?", 28)
                Spacer(Modifier.height(6.dp))
                Body("Anche le cose piccole. Scegli o scrivi.", size = 14)
                Spacer(Modifier.height(16.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    checkinWentWell.forEach { w -> Chip(w, w in well) { well = if (w in well) well - w else well + w } }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    placeholder = { Text("Qualcos'altro di cui sei contenta?", color = PlumFaint) },
                    modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(20.dp), colors = softFieldColors()
                )
                Spacer(Modifier.height(26.dp))
                PrimaryButton("Continua", Modifier.fillMaxWidth()) { stage = 2 }
            }
            2 -> {
                Display("Cosa vuoi\nalleggerire?", 28)
                Spacer(Modifier.height(6.dp))
                Body("Non serve fare di più: a volte serve fare meno.", size = 14)
                Spacer(Modifier.height(16.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    checkinLighten.forEach { w -> Chip(w, w in lighten, Rose) { lighten = if (w in lighten) lighten - w else lighten + w } }
                }
                Spacer(Modifier.height(26.dp))
                PrimaryButton("Continua", Modifier.fillMaxWidth()) { stage = 3 }
            }
            3 -> {
                Display("La prossima\nsettimana", 28)
                SectionTitle("Quanti inizi ti va di fare?", "Scegli un numero gentile, non eroico")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(2, 3, 5, 7).forEach { n -> Chip("$n inizi", target == n) { target = n } }
                }
                SectionTitle("La tua intenzione")
                OutlinedTextField(
                    value = intention, onValueChange = { intention = it },
                    placeholder = { Text("Es. Mettere ordine nelle carte, un po' alla volta", color = PlumFaint) },
                    modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(20.dp), colors = softFieldColors()
                )
                Spacer(Modifier.height(26.dp))
                PrimaryButton("Salva il check-in", Modifier.fillMaxWidth()) {
                    prefs.addCheckin(
                        WeeklyCheckin(System.currentTimeMillis(), mood, well.joinToString(", "), lighten.joinToString(", "), note, target, intention)
                    )
                    stage = 4
                }
            }
            else -> {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(20.dp))
                    BlendOrb(listOf(Lavender, Rose), 110.dp)
                    Spacer(Modifier.height(20.dp))
                    Display("Check-in fatto", 28, align = TextAlign.Center)
                    Spacer(Modifier.height(10.dp))
                    Body("Obiettivo: $target inizi. ${if (intention.isNotBlank()) "Intenzione: $intention." else ""}", Plum, 15, TextAlign.Center)
                    Spacer(Modifier.height(26.dp))
                    PrimaryButton("Fatto", Modifier.fillMaxWidth(), onClick = onClose)
                    Spacer(Modifier.height(10.dp))
                    GhostButton("💬  Condividila con un'amica", Modifier.fillMaxWidth()) {
                        shareIntention(context, "Questa settimana voglio fare $target piccoli inizi" + (if (intention.isNotBlank()) ": $intention" else "") + ". Mi fai il tifo? 🌱")
                    }
                }
            }
        }
    }
}

// =============================== ABITUDINI ===============================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitsTab(prefs: Prefs, version: Int, onChanged: () -> Unit) {
    var tick by remember { mutableIntStateOf(0) }
    val habits = remember(version, tick) { prefs.habits() }
    var newText by remember { mutableStateOf("") }
    val today = LocalDate.now().toEpochDay()

    TabColumn {
        Spacer(Modifier.height(6.dp))
        Display("Piccole abitudini", 30)
        Spacer(Modifier.height(8.dp))
        Body("Gesti minuscoli, ripetuti con leggerezza. Tocca un giorno per segnarlo: niente serie da perdere, ogni volta si somma.")
        Spacer(Modifier.height(16.dp))
        if (habits.isEmpty()) {
            SoftCard(tint = lerpColor(Color.White, Lavender, 0.14f)) {
                Text("Inizia da una sola", color = Plum, fontFamily = Serif, fontSize = 19.sp)
                Body("Scegline una qui sotto o scrivila tu. Più è piccola, meglio funziona.", size = 13)
            }
            Spacer(Modifier.height(12.dp))
        }
        habits.forEach { h ->
            key(h.id, tick) {
                SoftCard(padding = PaddingValues(16.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(h.text, color = Plum, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Text(
                            "×", color = PlumFaint, fontSize = 20.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    prefs.removeHabit(h.id)
                                    tick++
                                    onChanged()
                                }
                                .padding(horizontal = 8.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        for (k in 6 downTo 0) {
                            val d = today - k
                            val done = prefs.habitDone(h.id, d)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (done) LavenderDeep else LavenderMist)
                                        .clickable {
                                            prefs.toggleHabit(h.id, d)
                                            tick++
                                        },
                                    contentAlignment = Alignment.Center
                                ) { if (done) Text("✓", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    LocalDate.ofEpochDay(d).dayOfWeek.getDisplayName(TextStyle.SHORT, IT).take(3),
                                    color = if (k == 0) LavenderDeep else PlumSoft, fontSize = 11.sp,
                                    fontWeight = if (k == 0) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    val n = (0..6).count { prefs.habitDone(h.id, today - it) }
                    Spacer(Modifier.height(8.dp))
                    Text(if (n == 0) "Quando vuoi, anche oggi." else "$n volte negli ultimi 7 giorni", color = PlumSoft, fontSize = 12.sp)
                }
            }
        }

        SectionTitle("Aggiungi un'abitudine")
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newText, onValueChange = { newText = it },
                placeholder = { Text("Scrivi la tua…", color = PlumFaint) },
                singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(18.dp), colors = softFieldColors()
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Aggiungi", color = LavenderDeep, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        if (newText.isNotBlank()) {
                            prefs.addHabit(newText)
                            newText = ""
                            tick++
                        }
                    }
                    .padding(10.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            habitIdeas.filter { idea -> habits.none { it.text == idea } }.forEach { idea ->
                Chip("+ $idea", false) {
                    prefs.addHabit(idea)
                    tick++
                }
            }
        }
    }
}


// =============================== PENSIERI CHE BLOCCANO ===============================

@Composable
fun ThoughtsTab(prefs: Prefs, version: Int, onNew: () -> Unit) {
    val list = remember(version) { prefs.thoughts() }
    val fmt = DateTimeFormatter.ofPattern("EEE d MMM", IT)
    TabColumn {
        Spacer(Modifier.height(6.dp))
        Display("Pensieri\nche bloccano", 30)
        Spacer(Modifier.height(8.dp))
        Body("A volte non è il compito a fermarci, ma un pensiero: \"tanto non ce la faccio\", \"deve essere perfetto\". Qui impari a riconoscerlo e a trasformarlo in uno più gentile e realistico.")
        Spacer(Modifier.height(18.dp))
        SoftCard(tint = lerpColor(Color.White, Rose, 0.2f), onClick = onNew) {
            Text("Ho un pensiero che mi blocca", color = Plum, fontFamily = Serif, fontSize = 21.sp)
            Spacer(Modifier.height(4.dp))
            Body("Quattro passaggi, circa tre minuti.", size = 13)
            Spacer(Modifier.height(12.dp))
            PrimaryButton("Iniziamo", color = lerpColor(Rose, LavenderDeep, 0.6f), onClick = onNew)
        }
        SectionTitle("I tipi di pensiero più comuni")
        thoughtTypes.forEach { t ->
            Row(Modifier.padding(vertical = 5.dp)) {
                Text("✦  ", color = LavenderDeep, fontSize = 14.sp)
                Column {
                    Text(t.name, color = Plum, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text(t.explain, color = PlumSoft, fontSize = 13.sp)
                }
            }
        }
        if (list.isNotEmpty()) {
            SectionTitle("I tuoi pensieri trasformati")
            list.take(20).forEach { e ->
                SoftCard(padding = PaddingValues(16.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                    Text(Instant.ofEpochMilli(e.time).atZone(ZoneId.systemDefault()).format(fmt), color = LavenderDeep, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("«${e.thought}»", color = PlumSoft, fontSize = 14.sp)
                    if (e.reframe.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text("→ ${e.reframe}", color = Plum, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Body(
            "Ispirato alle tecniche cognitivo-comportamentali, è un esercizio di auto-aiuto e non sostituisce un percorso con un professionista.",
            PlumFaint, 12
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThoughtFlow(prefs: Prefs, onClose: () -> Unit, onStart: (String) -> Unit) {
    var stage by remember { mutableIntStateOf(0) }
    var thought by remember { mutableStateOf("") }
    var types by remember { mutableStateOf(setOf<String>()) }
    var reframe by remember { mutableStateOf("") }
    var step by remember { mutableStateOf("") }

    fun save() = prefs.addThought(ThoughtEntry(System.currentTimeMillis(), thought, types.toList(), reframe, step))

    PageColumn(accent = Rose, onBack = onClose) {
        Overline("Pensieri che bloccano · ${stage + 1} di 4")
        Spacer(Modifier.height(8.dp))
        ProgressLine((stage + 1) / 4f)
        Spacer(Modifier.height(22.dp))
        when (stage) {
            0 -> {
                Display("Qual è il pensiero\nche ti ferma?", 27)
                Spacer(Modifier.height(6.dp))
                Body("Scrivilo così come lo senti, con le tue parole.", size = 14)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = thought, onValueChange = { thought = it },
                    placeholder = { Text("Es. Tanto non ce la farò mai", color = PlumFaint) },
                    modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(20.dp), colors = softFieldColors()
                )
                Spacer(Modifier.height(12.dp))
                Text("Oppure scegline uno:", color = PlumFaint, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    thoughtExamples.forEach { t -> Chip(t, thought == t, Rose) { thought = t } }
                }
                Spacer(Modifier.height(26.dp))
                PrimaryButton("Continua", Modifier.fillMaxWidth()) { if (thought.isNotBlank()) stage = 1 }
            }
            1 -> {
                Display("Che tipo di\npensiero è?", 27)
                Spacer(Modifier.height(6.dp))
                Body("Riconoscerlo gli toglie un po' di forza. Puoi sceglierne più di uno.", size = 14)
                Spacer(Modifier.height(10.dp))
                QuoteText("«$thought»", 18, color = PlumSoft)
                Spacer(Modifier.height(14.dp))
                thoughtTypes.forEach { t ->
                    val on = t.id in types
                    SoftCard(
                        tint = if (on) lerpColor(Color.White, Lavender, 0.25f) else CardWhite,
                        padding = PaddingValues(16.dp), modifier = Modifier.padding(bottom = 8.dp),
                        onClick = { types = if (on) types - t.id else types + t.id }
                    ) {
                        Text(t.name, color = Plum, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(t.explain, color = PlumSoft, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(18.dp))
                PrimaryButton("Continua", Modifier.fillMaxWidth()) { stage = 2 }
            }
            2 -> {
                Display("Guardalo da\nun'altra parte", 27)
                Spacer(Modifier.height(14.dp))
                SoftCard(tint = lerpColor(Color.White, Lavender, 0.12f)) {
                    reframeQuestions.forEach { q ->
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text("?  ", color = LavenderDeep, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(q, color = Plum, fontSize = 15.sp)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = reframe, onValueChange = { reframe = it },
                    placeholder = { Text("Un pensiero più gentile e realistico è…", color = PlumFaint) },
                    modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(20.dp), colors = softFieldColors()
                )
                Spacer(Modifier.height(8.dp))
                Body("Esempio: «Non ce la farò mai» → «Non so ancora come finirò, ma posso fare il primo passo adesso».", PlumFaint, 12)
                Spacer(Modifier.height(24.dp))
                PrimaryButton("Continua", Modifier.fillMaxWidth()) { stage = 3 }
            }
            else -> {
                Display("Un passo piccolo,\nadesso", 27)
                Spacer(Modifier.height(6.dp))
                Body("Il pensiero nuovo diventa vero quando fai qualcosa. Qual è il gesto più piccolo possibile?", size = 14)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = step, onValueChange = { step = it },
                    placeholder = { Text("Es. Aprire il file e scrivere il titolo", color = PlumFaint) },
                    modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(20.dp), colors = softFieldColors()
                )
                Spacer(Modifier.height(26.dp))
                PrimaryButton("Salva e inizia ora", Modifier.fillMaxWidth()) {
                    save()
                    onStart(step)
                }
                Spacer(Modifier.height(10.dp))
                GhostButton("Salva e basta", Modifier.fillMaxWidth()) {
                    save()
                    onClose()
                }
            }
        }
    }
}

// =============================== CONSIGLIO E OBIETTIVO (Oggi) ===============================

@Composable
fun TipCard(prefs: Prefs) {
    val p = prefs.blockProfile ?: "avvio"
    val tips = profileTips[p] ?: profileTips.getValue("avvio")
    val tip = tips[(LocalDate.now().toEpochDay() % tips.size).toInt()]
    SoftCard(tint = lerpColor(Color.White, Peach, 0.35f)) {
        Overline("Il consiglio di oggi")
        Spacer(Modifier.height(6.dp))
        QuoteText(tip, 18)
    }
}

@Composable
fun GoalCard(prefs: Prefs, version: Int, onCheckin: () -> Unit) {
    val since = LocalDate.now().toEpochDay() - 6
    val n = remember(version) { prefs.startLog().count { it.epochDay >= since } }
    val target = prefs.weeklyTarget.coerceAtLeast(1)
    val due = remember(version) { prefs.checkinDue() }
    SoftCard(onClick = onCheckin, tint = if (due) lerpColor(Color.White, Rose, 0.22f) else CardWhite) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Overline("Obiettivo della settimana")
                Spacer(Modifier.height(4.dp))
                Text("$n di $target inizi", color = Plum, fontFamily = Serif, fontSize = 21.sp)
            }
            Text("🎯", fontSize = 28.sp)
        }
        if (prefs.weeklyIntention.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Body(prefs.weeklyIntention, PlumSoft, 13)
        }
        Spacer(Modifier.height(10.dp))
        ProgressLine(n / target.toFloat())
        Spacer(Modifier.height(10.dp))
        Text(
            if (due) "È il momento del check-in settimanale →" else "Check-in settimanale →",
            color = LavenderDeep, fontSize = 14.sp, fontWeight = FontWeight.Medium
        )
    }
}

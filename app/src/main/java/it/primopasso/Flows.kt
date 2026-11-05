package it.primopasso

import android.app.Activity
import android.app.TimePickerDialog
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================== INIZIA ORA ===============================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MicroStartFlow(prefs: Prefs, pending: PendingTask?, initialStep: String? = null, onClose: () -> Unit) {
    val context = LocalContext.current
    val profile = remember { profileById(prefs.blockProfile) }
    var stage by remember { mutableIntStateOf(0) }
    var task by remember { mutableStateOf(pending?.text ?: "") }
    var category by remember { mutableStateOf(pending?.category ?: "altro") }
    var reason by remember { mutableStateOf<String?>(null) }
    var ideaIndex by remember { mutableIntStateOf(0) }
    var step by remember { mutableStateOf(initialStep?.takeIf { it.isNotBlank() } ?: microTemplates.getValue(pending?.category ?: "altro")[0]) }
    var minutes by remember { mutableIntStateOf(prefs.preferredMinutes) }
    var totalSeconds by remember { mutableIntStateOf(0) }
    val cheer = remember { startCheers.random() }

    fun log(finished: Boolean) {
        prefs.addStart(
            StartEntry(
                System.currentTimeMillis(), task.ifBlank { "Una cosa rimandata" }, category,
                reason ?: "", ((totalSeconds + 59) / 60).coerceAtLeast(1), finished
            )
        )
        if (finished && pending != null) prefs.removePending(pending.id)
    }

    when (stage) {
        0 -> PageColumn(onBack = onClose) {
            Display("Cosa stai rimandando?", 28)
            Spacer(Modifier.height(6.dp))
            Body("Scrivilo come ti viene, non serve che sia ordinato.", size = 14)
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = task,
                onValueChange = { task = it },
                placeholder = { Text("Es. rispondere alla mail della banca", color = PlumFaint) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = softFieldColors()
            )
            SectionTitle("Di che tipo è?")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                startCategories.forEach { c ->
                    Chip("${c.symbol}  ${c.label}", category == c.id) {
                        category = c.id
                        ideaIndex = 0
                        step = microTemplates.getValue(c.id)[0]
                    }
                }
            }
            SectionTitle("Il primo passo", "Così piccolo da sembrare quasi ridicolo")
            SoftCard(tint = lerpColor(Color.White, Lavender, 0.16f)) {
                OutlinedTextField(
                    value = step,
                    onValueChange = { step = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = softFieldColors()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "↻  Un'altra idea", color = LavenderDeep, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable {
                            val l = microTemplates.getValue(category)
                            ideaIndex = (ideaIndex + 1) % l.size
                            step = l[ideaIndex]
                        }
                        .padding(vertical = 6.dp)
                )
                profile?.let {
                    Spacer(Modifier.height(4.dp))
                    Body(it.tip, PlumSoft, 13)
                }
            }
            SectionTitle("Perché lo rimandi?", "Facoltativo: serve al tuo registro")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                startReasons.forEach { r -> Chip(r, reason == r, Rose) { reason = if (reason == r) null else r } }
            }
            SectionTitle("Quanto tempo?")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(2, 5, 10).forEach { m -> Chip("$m min", minutes == m) { minutes = m } }
            }
            Spacer(Modifier.height(28.dp))
            PrimaryButton("Inizia ora", Modifier.fillMaxWidth()) { stage = 1 }
            Spacer(Modifier.height(10.dp))
            GhostButton("💬  Dillo a un'amica, poi inizia", Modifier.fillMaxWidth()) {
                shareIntention(
                    context,
                    "Sto per iniziare: ${task.ifBlank { "una cosa che rimando" }}. Primo passo: $step. Mi fai il tifo? Tra $minutes minuti ti dico com'è andata 🌱"
                )
                stage = 1
            }
            if (pending == null) {
                Spacer(Modifier.height(10.dp))
                GhostButton("Salvala per dopo", Modifier.fillMaxWidth()) {
                    prefs.addPending(task.ifBlank { step }, category)
                    onClose()
                }
            }
        }

        1 -> StartTimer(task, step, minutes, onEnd = { s ->
            totalSeconds += s
            stage = 2
        }, onCancel = onClose)

        2 -> CenterPage {
            BlendOrb(listOf(Lavender, Rose), 120.dp)
            Spacer(Modifier.height(24.dp))
            Display("Hai iniziato!", 32, align = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Body(
                if (task.isBlank()) "${durationLabel(totalSeconds)} solo per partire."
                else "${durationLabel(totalSeconds)} su \"$task\".",
                Plum, 16, TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            QuoteText(cheer, 19, TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            PrimaryButton("Continuo altri 5 minuti", Modifier.fillMaxWidth()) {
                minutes = 5
                stage = 1
            }
            Spacer(Modifier.height(10.dp))
            GhostButton("Ho finito tutto 🎉", Modifier.fillMaxWidth()) {
                log(true)
                stage = 3
            }
            Spacer(Modifier.height(10.dp))
            GhostButton("Mi fermo qui, va bene così", Modifier.fillMaxWidth()) {
                log(false)
                if (pending == null && task.isNotBlank()) prefs.addPending(task, category)
                onClose()
            }
            if (pending == null && task.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Body("Se ti fermi, la salvo tra le cose da riprendere.", PlumFaint, 12, TextAlign.Center)
            }
        }

        else -> CenterPage {
            BlendOrb(listOf(Rose, Peach, Lavender), 130.dp)
            Spacer(Modifier.height(24.dp))
            Display("Fatto!", 34, align = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Body("Una cosa in sospeso in meno. Concediti una piccola ricompensa: te la sei guadagnata.", Plum, 16, TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            PrimaryButton("Grazie", Modifier.fillMaxWidth(), onClick = onClose)
        }
    }
}

@Composable
fun StartTimer(task: String, step: String, minutes: Int, onEnd: (Int) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    val total = minutes * 60_000L
    var elapsed by remember { mutableLongStateOf(0L) }
    var paused by remember { mutableStateOf(false) }

    LaunchedEffect(paused) {
        if (paused) return@LaunchedEffect
        var last = withFrameMillis { it }
        while (elapsed < total) {
            val now = withFrameMillis { it }
            elapsed += now - last
            last = now
        }
        vibrate(context, 300)
        onEnd((total / 1000).toInt())
    }

    val left = ((total - elapsed) / 1000).coerceAtLeast(0)
    SoftBackground {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Overline("Inizia ora · $minutes minuti", modifier = Modifier.weight(1f).padding(start = 6.dp))
                IconButton(onClick = onCancel) { Icon(Icons.Filled.Close, contentDescription = "Chiudi", tint = Plum) }
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(270.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(LavenderMist, style = Stroke(16f))
                    drawArc(
                        LavenderDeep, -90f, 360f * (elapsed / total.toFloat()), false,
                        topLeft = Offset(8f, 8f), size = Size(size.width - 16f, size.height - 16f),
                        style = Stroke(16f, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("%d:%02d".format(left / 60, left % 60), color = Plum, fontFamily = Serif, fontSize = 56.sp)
                    Text(if (paused) "in pausa" else "restano", color = PlumSoft, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(30.dp))
            Text(
                step, color = Plum, fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 22.sp,
                lineHeight = 30.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
            if (task.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Body(task, PlumSoft, 14, TextAlign.Center)
            }
            Spacer(Modifier.height(12.dp))
            Body("Basta il primo gesto. Se vuoi fermarti, puoi.", PlumFaint, 13, TextAlign.Center)
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GhostButton(if (paused) "Riprendi" else "Pausa", Modifier.width(140.dp)) { paused = !paused }
                PrimaryButton("Ho fatto", Modifier.width(160.dp)) { onEnd((elapsed / 1000).toInt().coerceAtLeast(1)) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// =============================== KIT ===============================

@Composable
fun KitFlow(exercise: Exercise, onClose: () -> Unit) {
    var done by remember { mutableStateOf(false) }
    val cheer = remember { startCheers.random() }
    if (!done) {
        val p = exercise.pattern
        if (p != null) BreathPlayer(p, exercise.cycles, exercise.title, Lavender, { done = true }, onClose)
        else StepsPlayer(exercise, exercise.title, Lavender, { done = true }, onClose)
    } else {
        CenterPage {
            BlendOrb(listOf(Lavender, Rose), 110.dp)
            Spacer(Modifier.height(22.dp))
            Display("Ben fatto", 30, align = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            QuoteText(cheer, 19, TextAlign.Center)
            Spacer(Modifier.height(30.dp))
            PrimaryButton("Fatto", Modifier.fillMaxWidth(), onClick = onClose)
        }
    }
}

// =============================== LEZIONE ===============================

@Composable
fun LessonScreen(prefs: Prefs, lesson: Lesson, onDone: () -> Unit, onTry: () -> Unit, onWorksheet: (String) -> Unit) {
    val context = LocalContext.current
    val wasDone = remember { lesson.id in prefs.lessonsDone() }
    val speaker = remember { Speaker(context) }
    val recorded = remember { LessonPlayer(context) }
    val hasRecording = remember { recorded.hasAudio(lesson.id) }
    var music by remember { mutableStateOf(prefs.musicOn) }
    var playing by remember { mutableStateOf(false) }
    var noVoice by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose {
            recorded.stop()
            speaker.shutdown()
        }
    }
    PageColumn(onBack = onDone) {
        Overline("Modulo ${lesson.module} · ${moduleById(lesson.module)?.title ?: ""}")
        Spacer(Modifier.height(4.dp))
        Text("Lezione ${lesson.id} di ${lessons.size} · 5 minuti", color = PlumFaint, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Display(lesson.title, 30)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (playing) LavenderDeep else lerpColor(Color.White, Lavender, 0.22f))
                    .clickable {
                        if (playing) {
                            recorded.stop()
                            speaker.stop()
                            playing = false
                        } else if (hasRecording) {
                            playing = recorded.play(lesson.id, music) { playing = false }
                        } else {
                            val ok = speaker.speak(
                                listOf("Lezione ${lesson.id}. ${lesson.title}.") + lesson.body.split("\n\n") +
                                    listOf("Il concetto chiave. ${lesson.key}", "Per oggi. ${lesson.tryToday}")
                            ) { playing = false }
                            playing = ok
                            noVoice = !ok || !speaker.italian
                        }
                    }
                    .padding(horizontal = 18.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (playing) "■" else "▶", color = if (playing) Color.White else LavenderDeep, fontSize = 14.sp)
                Spacer(Modifier.width(10.dp))
                Text(if (playing) "Ferma l'ascolto" else "Ascolta la lezione", color = if (playing) Color.White else LavenderDeep, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            if (hasRecording) {
                Spacer(Modifier.width(10.dp))
                Text(
                    if (music) "♪ Musica sì" else "♪ Musica no",
                    color = if (music) LavenderDeep else PlumFaint, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (music) LavenderMist else Color(0x80FFFFFF))
                        .clickable {
                            music = !music
                            prefs.musicOn = music
                            recorded.setMusic(music)
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                )
            }
        }
        if (noVoice) {
            Spacer(Modifier.height(6.dp))
            Body("Se non senti nulla, attiva la voce italiana in Impostazioni del telefono → Sintesi vocale.", PlumFaint, 12)
        }
        Spacer(Modifier.height(16.dp))
        lesson.body.split("\n\n").forEach {
            Body(it, Plum, 17)
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(6.dp))
        SoftCard(tint = lerpColor(Color.White, Lavender, 0.18f)) {
            Overline("Il concetto chiave")
            Spacer(Modifier.height(8.dp))
            QuoteText(lesson.key, 19)
        }
        Spacer(Modifier.height(12.dp))
        SoftCard(tint = lerpColor(Color.White, Rose, 0.22f)) {
            Overline("Per oggi")
            Spacer(Modifier.height(8.dp))
            Body(lesson.tryToday, Plum, 16)
        }
        Spacer(Modifier.height(26.dp))
        val ws = worksheetById(lesson.worksheet)
        if (ws != null) {
            PrimaryButton("Compila la scheda: ${ws.title}", Modifier.fillMaxWidth()) {
                if (!wasDone) prefs.markLessonDone(lesson.id)
                onWorksheet(ws.id)
            }
            Spacer(Modifier.height(10.dp))
        }
        (if (ws == null) PrimaryButton("Provo subito, ${prefs.preferredMinutes} minuti", Modifier.fillMaxWidth()) {
            if (!wasDone) prefs.markLessonDone(lesson.id)
            onTry()
        } else GhostButton("Provo subito, ${prefs.preferredMinutes} minuti", Modifier.fillMaxWidth()) {
            if (!wasDone) prefs.markLessonDone(lesson.id)
            onTry()
        })
        Spacer(Modifier.height(10.dp))
        GhostButton(if (wasDone) "Chiudi" else "Letta, grazie", Modifier.fillMaxWidth()) {
            if (!wasDone) prefs.markLessonDone(lesson.id)
            onDone()
        }
    }
}

// =============================== PROFILO ===============================

@Composable
fun ProfileScreen(prefs: Prefs, onBack: () -> Unit, onRedo: () -> Unit) {
    val p = profileById(prefs.blockProfile)
    PageColumn(onBack = onBack) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (p == null) {
                Display("Non hai ancora fatto il questionario", 24, align = TextAlign.Center)
            } else {
                ProfilePortrait(p, personalNotes(prefs.quizAnswers()))
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(if (p == null) "Fai il questionario" else "Rifai il questionario", Modifier.fillMaxWidth(), onClick = onRedo)
        }
    }
}

// =============================== IMPOSTAZIONI ===============================

@Composable
fun SettingsScreen(prefs: Prefs, onBack: () -> Unit, onChanged: () -> Unit, onRequestNotifications: () -> Unit) {
    val context = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf(prefs.name) }

    PageColumn(onBack = onBack) {
        Display("Impostazioni", 28)
        SectionTitle("Il tuo nome")
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                prefs.name = it
                onChanged()
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = softFieldColors()
        )
        SectionTitle("Promemoria", "Tocca un orario per cambiarlo")
        SoftCard(padding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)) {
            key(tick) {
                listOf(
                    Triple("Lezione del mattino", prefs.morningOn, prefs.morningMinutes),
                    Triple("Micro-inizio del pomeriggio", prefs.afternoonOn, prefs.afternoonMinutes)
                ).forEachIndexed { i, (label, on0, minutes) ->
                    val on = on0 && prefs.remindersChosen
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            formatTime(minutes), color = if (on) LavenderDeep else PlumFaint, fontSize = 18.sp, fontFamily = Serif,
                            modifier = Modifier
                                .width(70.dp)
                                .clickable {
                                    TimePickerDialog(context, { _, h, m ->
                                        if (i == 0) prefs.morningMinutes = h * 60 + m else prefs.afternoonMinutes = h * 60 + m
                                        Reminders.scheduleAll(context)
                                        tick++
                                    }, minutes / 60, minutes % 60, true).show()
                                }
                        )
                        Text(label, color = if (on) Plum else PlumFaint, fontSize = 15.sp, modifier = Modifier.weight(1f))
                        Switch(
                            checked = on,
                            onCheckedChange = {
                                prefs.remindersChosen = true
                                if (i == 0) prefs.morningOn = it else prefs.afternoonOn = it
                                if (it) onRequestNotifications()
                                Reminders.scheduleAll(context)
                                tick++
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White, checkedTrackColor = Lavender,
                                uncheckedThumbColor = PlumFaint, uncheckedTrackColor = Color(0x99FFFFFF),
                                uncheckedBorderColor = Hairline
                            )
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Body(
            "Primo Passo aiuta a iniziare le cose di tutti i giorni. Non è una diagnosi né una terapia: se senti che il blocco pesa molto sulla tua vita, parlarne con un professionista può aiutare.",
            PlumFaint, 12
        )
    }
}

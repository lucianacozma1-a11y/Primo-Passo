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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

@Composable
private fun OptionCard(label: String, selected: Boolean, multi: Boolean, onClick: () -> Unit) {
    SoftCard(
        tint = if (selected) lerpColor(Color.White, Lavender, 0.25f) else CardWhite,
        padding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        modifier = Modifier.padding(bottom = 10.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Plum, fontSize = 17.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            if (multi) {
                Box(
                    Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) LavenderDeep else LavenderMist),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) Text("✓", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** Note personali dal questionario, senza numeri inventati. */
fun personalNotes(a: Map<String, Set<String>>): List<String> {
    val out = mutableListOf<String>()
    if (a["stanchezza"]?.contains("spesso") == true)
        out += "Ti senti spesso stanca: qui vale la regola \"meno, ma con gentilezza\". Anche due minuti contano."
    if (a["rimuginio"]?.contains("spesso") == true)
        out += "La sera la mente fatica a staccare: \"Svuota la testa\" e \"Prepara il terreno\" possono aiutarti a posare i pensieri."
    if (a["no"]?.contains("spesso") == true)
        out += "Ti è difficile dire di no: nelle lezioni trovi come proteggere il tuo tempo senza sensi di colpa."
    val sac = (a["sacrifici"] ?: emptySet()) - "niente"
    if (sac.isNotEmpty())
        out += "Sacrifichi spesso ${sac.joinToString(", ") { it.lowercase() }}: ricordati che anche tu sei nella lista delle priorità."
    return out
}

@Composable
fun ProfilePortrait(profile: BlockProfile, notes: List<String>) {
    BlendOrb(listOf(Lavender, Rose, Peach), 120.dp)
    Spacer(Modifier.height(18.dp))
    Overline("Il tuo modo di bloccarti")
    Spacer(Modifier.height(8.dp))
    Display(profile.title, 28, align = TextAlign.Center)
    Spacer(Modifier.height(4.dp))
    Text(profile.name, color = LavenderDeep, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    Spacer(Modifier.height(14.dp))
    Body(profile.description, Plum, 16, TextAlign.Center)
    Spacer(Modifier.height(18.dp))
    SoftCard {
        Overline("Cosa ti aiuta")
        Spacer(Modifier.height(8.dp))
        profile.helps.forEach {
            Row(Modifier.padding(vertical = 4.dp)) {
                Text("✦  ", color = LavenderDeep, fontSize = 15.sp)
                Text(it, color = Plum, fontSize = 15.sp)
            }
        }
    }
    if (notes.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        SoftCard(tint = lerpColor(Color.White, Rose, 0.18f)) {
            Overline("Per te")
            Spacer(Modifier.height(8.dp))
            notes.forEach {
                Body(it, Plum, 14)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    Body(
        "Non è una diagnosi: è solo un modo per scegliere gli esercizi più adatti a te. Puoi rifare il questionario quando vuoi.",
        PlumFaint, 12, TextAlign.Center
    )
}

/** Questionario completo: introduzione, domande, ritratto finale. */
@Composable
fun QuizFlow(prefs: Prefs, showIntro: Boolean, finishLabel: String, onFinish: () -> Unit, onClose: (() -> Unit)?) {
    var step by remember { mutableIntStateOf(if (showIntro) -1 else 0) }
    var answers by remember { mutableStateOf(mapOf<String, Set<String>>()) }
    val total = quizQuestions.size

    LaunchedEffect(step) {
        if (step >= total) {
            prefs.saveQuiz(answers)
            prefs.blockProfile = computeProfile(answers)
        }
    }

    CenterPage(center = step < 0 || step >= total) {
        when {
            step < 0 -> {
                BlendOrb(listOf(Lavender, Rose, Peach), 150.dp)
                Spacer(Modifier.height(26.dp))
                Display("Come ti blocchi?", 30, align = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Body(
                    "Nove domande, due minuti. Non è un test e non ci sono risposte giuste: serve solo a capire come funzioni, per proporti gli esercizi più adatti.",
                    Plum, 16, TextAlign.Center
                )
                Spacer(Modifier.height(34.dp))
                PrimaryButton("Iniziamo", Modifier.fillMaxWidth()) { step = 0 }
                if (onClose != null) {
                    Spacer(Modifier.height(10.dp))
                    GhostButton("Non ora", Modifier.fillMaxWidth(), onClose)
                }
            }
            step < total -> {
                val q = quizQuestions[step]
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (step > 0 || onClose != null) "‹ Indietro" else "",
                        color = PlumSoft, fontSize = 14.sp,
                        modifier = Modifier
                            .clickable {
                                if (step > 0) step -= 1 else onClose?.invoke()
                            }
                            .padding(vertical = 6.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    Text("${step + 1} di $total", color = PlumFaint, fontSize = 13.sp)
                }
                Spacer(Modifier.height(8.dp))
                ProgressLine((step + 1) / total.toFloat())
                Spacer(Modifier.height(30.dp))
                Display(q.text, 25, modifier = Modifier.fillMaxWidth())
                q.hint?.let {
                    Spacer(Modifier.height(6.dp))
                    Body(it, size = 14, modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(22.dp))
                q.options.forEach { o ->
                    val sel = answers[q.id]?.contains(o.value) == true
                    OptionCard(o.label, sel, q.multi) {
                        if (q.multi) {
                            val cur = answers[q.id] ?: emptySet()
                            val next = if (sel) cur - o.value
                            else if (o.value == "niente") setOf("niente")
                            else cur - "niente" + o.value
                            answers = answers + (q.id to next)
                        } else {
                            answers = answers + (q.id to setOf(o.value))
                            step += 1
                        }
                    }
                }
                if (q.multi) {
                    Spacer(Modifier.height(14.dp))
                    PrimaryButton("Continua", Modifier.fillMaxWidth()) { step += 1 }
                }
            }
            else -> {
                val p = profileById(computeProfile(answers)) ?: blockProfiles.first()
                ProfilePortrait(p, personalNotes(answers))
                Spacer(Modifier.height(26.dp))
                PrimaryButton(finishLabel, Modifier.fillMaxWidth(), onClick = onFinish)
            }
        }
    }
}

@Composable
fun Onboarding(prefs: Prefs, onRequestNotifications: () -> Unit, onFinish: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf(prefs.name) }

    when (page) {
        0 -> CenterPage {
            BlendOrb(listOf(Rose, Lavender, Peach), 170.dp)
            Spacer(Modifier.height(30.dp))
            Display("Un passo alla volta", 30, align = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Body(
                "Non serve motivazione e non serve forza di volontà. Serve un primo passo, così piccolo da non fare paura. Qui lo troviamo insieme.",
                Plum, 16, TextAlign.Center
            )
            Spacer(Modifier.height(26.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Come vuoi che ti chiami? (facoltativo)", color = PlumFaint) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = softFieldColors()
            )
            Spacer(Modifier.height(30.dp))
            PrimaryButton("Iniziamo", Modifier.fillMaxWidth()) {
                prefs.name = name
                page = 1
            }
        }
        1 -> QuizFlow(prefs, showIntro = true, finishLabel = "Continua", onFinish = { page = 2 }, onClose = null)
        else -> CenterPage {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) {
                    Box(
                        Modifier
                            .height(5.dp)
                            .width(if (it == 2) 26.dp else 10.dp)
                            .clip(CircleShape)
                            .background(Lavender)
                    )
                }
            }
            Spacer(Modifier.height(30.dp))
            Display("Due piccoli\npromemoria?", 28, align = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Body("Inviti gentili, mai insistenti. Puoi cambiare gli orari o spegnerli quando vuoi.", align = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            SoftCard(padding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)) {
                Row(Modifier.padding(vertical = 6.dp)) {
                    Text(formatTime(prefs.morningMinutes), color = LavenderDeep, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(60.dp))
                    Text("La lezione del mattino", color = Plum, fontSize = 15.sp)
                }
                Row(Modifier.padding(vertical = 6.dp)) {
                    Text(formatTime(prefs.afternoonMinutes), color = LavenderDeep, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(60.dp))
                    Text("Un micro-inizio del pomeriggio", color = Plum, fontSize = 15.sp)
                }
            }
            Spacer(Modifier.height(30.dp))
            PrimaryButton("Sì, volentieri", Modifier.fillMaxWidth()) {
                prefs.morningOn = true
                prefs.afternoonOn = true
                prefs.remindersChosen = true
                onRequestNotifications()
                prefs.onboardingDone = true
                onFinish()
            }
            Spacer(Modifier.height(10.dp))
            GhostButton("Non ora", Modifier.fillMaxWidth()) {
                prefs.morningOn = false
                prefs.afternoonOn = false
                prefs.remindersChosen = true
                prefs.onboardingDone = true
                onFinish()
            }
        }
    }
}

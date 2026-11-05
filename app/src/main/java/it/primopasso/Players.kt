package it.primopasso

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos

@Composable
private fun KeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}

@Composable
private fun PlayerTopBar(title: String, subtitle: String, onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Overline(title)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = PlumSoft, fontSize = 14.sp)
        }
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, contentDescription = "Chiudi", tint = Plum)
        }
    }
}

/** Guida al respiro con qualsiasi schema di fasi. */
@Composable
fun BreathPlayer(
    pattern: Pattern,
    cycles: Int,
    title: String,
    accent: Color = Lavender,
    onFinish: (seconds: Int) -> Unit,
    onCancel: () -> Unit
) {
    KeepScreenOn()
    val context = LocalContext.current
    val phases = pattern.phases
    val n = phases.size

    val targets = remember(pattern) {
        val raw = phases.mapIndexed { i, p ->
            when (p.kind) {
                Kind.IN -> if (phases[(i + 1) % n].kind == Kind.IN) 0.84f else 1f
                Kind.OUT -> 0.52f
                else -> -1f
            }
        }.toMutableList()
        repeat(2) {
            for (i in 0 until n) if (raw[i] < 0f) {
                val prev = raw[(i - 1 + n) % n]
                if (prev >= 0f) raw[i] = prev
            }
        }
        raw.map { if (it < 0f) 0.52f else it }
    }

    var phase by remember { mutableIntStateOf(-1) }
    var cycle by remember { mutableIntStateOf(1) }
    var elapsed by remember { mutableLongStateOf(0L) }
    var paused by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(paused, finished) {
        if (paused || finished) return@LaunchedEffect
        var last = withFrameMillis { it }
        while (true) {
            val now = withFrameMillis { it }
            elapsed += now - last
            last = now
            val len = if (phase < 0) 3000L else phases[phase].seconds * 1000L
            if (elapsed >= len) {
                elapsed -= len
                if (phase == n - 1 && cycle >= cycles) {
                    finished = true
                    vibrate(context, 120)
                    break
                }
                if (phase == n - 1) {
                    cycle += 1
                    phase = 0
                } else {
                    phase += 1
                }
                vibrate(context)
            }
        }
    }

    LaunchedEffect(finished) { if (finished) onFinish(pattern.cycleSeconds * cycles) }

    val len = if (phase < 0) 3000f else phases[phase].seconds * 1000f
    val p = (elapsed / len).coerceIn(0f, 1f)
    val eased = ((1 - cos(PI * p)) / 2).toFloat()
    val scale = if (phase < 0) 0.52f else {
        val from = targets[(phase - 1 + n) % n]
        val to = targets[phase]
        from + (to - from) * eased
    }
    val secondsLeft = ceil((len - elapsed) / 1000f).toInt().coerceAtLeast(1)
    val label = if (phase < 0) "Preparati" else phases[phase].label
    val hint = if (phase < 0) "Siediti comoda, spalle morbide" else phases[phase].hint

    val totalMs = pattern.cycleSeconds * cycles * 1000f
    val doneMs = if (phase < 0) 0f else
        ((cycle - 1) * pattern.cycleSeconds * 1000f) + phases.take(phase).sumOf { it.seconds } * 1000f + elapsed
    val overall = (doneMs / totalMs).coerceIn(0f, 1f)
    val remaining = ((totalMs - doneMs) / 1000f).toInt().coerceAtLeast(0)

    SoftBackground(accent) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PlayerTopBar(title, "${pattern.name} · ciclo $cycle di $cycles", onCancel)

            Spacer(Modifier.weight(1f))

            Box(Modifier.size(310.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val maxR = size.minDimension / 2f
                    val c = Offset(size.width / 2, size.height / 2)
                    // arco di avanzamento totale
                    drawCircle(Hairline, radius = maxR - 4f, style = Stroke(3f))
                    drawArc(
                        accent, -90f, 360f * overall, false,
                        topLeft = Offset(4f, 4f), size = Size(size.width - 8f, size.height - 8f),
                        style = Stroke(5f, cap = StrokeCap.Round)
                    )
                    // anelli guida
                    drawCircle(Color(0x293B2F56), radius = maxR * 0.86f, center = c, style = Stroke(1.5f))
                    drawCircle(Color(0x293B2F56), radius = maxR * 0.86f * 0.52f, center = c, style = Stroke(1.5f))
                    // alone
                    val r = maxR * 0.86f * scale
                    drawCircle(
                        Brush.radialGradient(listOf(accent.copy(alpha = 0.45f), Color.Transparent), c, r * 1.4f),
                        radius = r * 1.4f, center = c
                    )
                    // sfera del respiro
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color.White, lerpColor(accent, Color.White, 0.35f), accent),
                            Offset(c.x - r * 0.3f, c.y - r * 0.35f), r * 1.6f
                        ),
                        radius = r, center = c
                    )
                    drawCircle(Color.White.copy(alpha = 0.6f), radius = r, center = c, style = Stroke(2f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = label,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "label"
                    ) { l ->
                        Text(l, color = Plum, fontFamily = Serif, fontSize = 30.sp)
                    }
                    Text("$secondsLeft", color = Plum.copy(alpha = 0.7f), fontSize = 46.sp, fontWeight = FontWeight.Light)
                }
            }

            Spacer(Modifier.height(30.dp))
            AnimatedContent(
                targetState = hint,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "hint"
            ) { h ->
                Text(
                    h, color = Plum, fontFamily = Serif, fontStyle = FontStyle.Italic,
                    fontSize = 21.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(10.dp))
            Text("Ancora ${durationLabel(remaining)}", color = PlumFaint, fontSize = 13.sp)

            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GhostButton("Termina", Modifier.width(140.dp), onCancel)
                PrimaryButton(if (paused) "Riprendi" else "Pausa", Modifier.width(140.dp), accent) {
                    paused = !paused
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** Esercizio guidato a passi, con timer opzionale per ogni passo. */
@Composable
fun StepsPlayer(
    exercise: Exercise,
    title: String,
    accent: Color = Lavender,
    onFinish: (seconds: Int) -> Unit,
    onCancel: () -> Unit
) {
    KeepScreenOn()
    val context = LocalContext.current
    val steps = exercise.steps
    var index by remember { mutableIntStateOf(0) }
    var elapsed by remember(index) { mutableLongStateOf(0L) }
    var spent by remember { mutableLongStateOf(0L) }
    val step = steps[index]

    fun next() {
        vibrate(context)
        if (index < steps.size - 1) index += 1
        else onFinish(((spent + elapsed) / 1000).toInt().coerceAtLeast(30))
    }

    LaunchedEffect(index) {
        var last = withFrameMillis { it }
        while (true) {
            val now = withFrameMillis { it }
            elapsed += now - last
            spent += now - last
            last = now
            if (step.seconds > 0 && elapsed >= step.seconds * 1000L) {
                next()
                break
            }
        }
    }

    SoftBackground(accent) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PlayerTopBar(title, "Passo ${index + 1} di ${steps.size}", onCancel)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                steps.indices.forEach { i ->
                    Box(
                        Modifier
                            .height(4.dp)
                            .width(if (i == index) 28.dp else 14.dp)
                            .clip(CircleShape)
                            .background(if (i <= index) accent else LavenderMist)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            AnimatedContent(
                targetState = index,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "step"
            ) { i ->
                val s = steps[i]
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 12.dp)) {
                    Display(s.title, 32, align = TextAlign.Center)
                    Spacer(Modifier.height(22.dp))
                    QuoteText(s.text, 21, TextAlign.Center)
                }
            }

            Spacer(Modifier.height(36.dp))

            if (step.seconds > 0) {
                val frac = (elapsed / (step.seconds * 1000f)).coerceIn(0f, 1f)
                val left = ceil(step.seconds - elapsed / 1000f).toInt().coerceAtLeast(0)
                Box(Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(Hairline, style = Stroke(4f))
                        drawArc(
                            accent, -90f, 360f * frac, false,
                            topLeft = Offset(2f, 2f), size = Size(size.width - 4f, size.height - 4f),
                            style = Stroke(6f, cap = StrokeCap.Round)
                        )
                    }
                    Text("$left", color = Plum, fontFamily = Serif, fontSize = 34.sp)
                }
            }

            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (index > 0) {
                    GhostButton("Indietro", Modifier.width(130.dp)) { index -= 1 }
                }
                PrimaryButton(
                    if (index == steps.size - 1) "Ho finito" else if (step.seconds > 0) "Avanti" else "Fatto, avanti",
                    Modifier.width(if (index > 0) 170.dp else 220.dp),
                    accent
                ) { next() }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

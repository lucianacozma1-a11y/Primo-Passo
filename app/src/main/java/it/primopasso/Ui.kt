package it.primopasso

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// ---------- Palette calma: salvia, azzurro nebbia, sabbia ----------
val Plum = Color(0xFF2C3A3D)          // testo principale
val PlumSoft = Color(0xFF5E6E70)      // testo secondario
val PlumFaint = Color(0xFF9AA8A8)     // testo tenue
val Lavender = Color(0xFF86B3A5)      // accento
val LavenderDeep = Color(0xFF4E8577)
val LavenderMist = Color(0xFFE2EEE9)
val Rose = Color(0xFFA9C8DE)
val Peach = Color(0xFFE6DAC4)
val BgTop = Color(0xFFF5F8F6)
val BgMid = Color(0xFFEAF2EF)
val BgBottom = Color(0xFFF2EFE7)
val CardWhite = Color(0xC7FFFFFF)
val CardBorder = Color(0xE6FFFFFF)
val Hairline = Color(0x1F2C3A3D)

val Serif = FontFamily.Serif

@Composable
fun CalmaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = LavenderDeep, onPrimary = Color.White, secondary = Rose,
            background = BgTop, surface = Color.White, onSurface = Plum, onBackground = Plum,
            surfaceVariant = LavenderMist, onSurfaceVariant = PlumSoft
        ),
        content = content
    )
}

/** Sfondo lavanda con nuvole di colore che si muovono lentamente. */
@Composable
fun SoftBackground(accent: Color = Lavender, content: @Composable BoxScope.() -> Unit) {
    val t = rememberInfiniteTransition(label = "bg")
    val a by t.animateFloat(0f, 1f, infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Reverse), label = "a")
    val b by t.animateFloat(0f, 1f, infiniteRepeatable(tween(21000, easing = LinearEasing), RepeatMode.Reverse), label = "b")
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgMid, BgBottom)))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val c1 = Offset(w * (0.1f + 0.45f * a), h * (0.08f + 0.06f * b))
            drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = 0.30f), Color.Transparent), c1, w * 0.8f), w * 0.8f, c1)
            val c2 = Offset(w * (0.95f - 0.4f * b), h * (0.55f + 0.12f * a))
            drawCircle(Brush.radialGradient(listOf(Rose.copy(alpha = 0.28f), Color.Transparent), c2, w * 0.75f), w * 0.75f, c2)
            val c3 = Offset(w * (0.2f + 0.3f * b), h * (0.95f - 0.05f * a))
            drawCircle(Brush.radialGradient(listOf(Peach.copy(alpha = 0.30f), Color.Transparent), c3, w * 0.7f), w * 0.7f, c3)
        }
        content()
    }
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    tint: Color = CardWhite,
    padding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    var m = modifier
        .fillMaxWidth()
        .shadow(10.dp, shape, ambientColor = Lavender.copy(alpha = 0.25f), spotColor = Lavender.copy(alpha = 0.25f))
        .clip(shape)
        .background(tint)
        .border(1.dp, CardBorder, shape)
    if (onClick != null) m = m.clickable(onClick = onClick)
    Column(m.padding(padding), content = content)
}

@Composable
fun Display(text: String, size: Int = 30, color: Color = Plum, align: TextAlign = TextAlign.Start, modifier: Modifier = Modifier) {
    Text(text, color = color, fontFamily = Serif, fontSize = size.sp, lineHeight = (size * 1.2).sp, textAlign = align, modifier = modifier)
}

@Composable
fun Overline(text: String, color: Color = LavenderDeep, modifier: Modifier = Modifier) {
    Text(text.uppercase(), color = color, fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold, modifier = modifier)
}

@Composable
fun SectionTitle(text: String, subtitle: String? = null) {
    Column(Modifier.padding(top = 28.dp, bottom = 12.dp)) {
        Text(text, color = Plum, fontFamily = Serif, fontSize = 21.sp)
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = PlumSoft, fontSize = 13.sp)
        }
    }
}

@Composable
fun Body(text: String, color: Color = PlumSoft, size: Int = 15, align: TextAlign = TextAlign.Start, modifier: Modifier = Modifier) {
    Text(text, color = color, fontSize = size.sp, lineHeight = (size * 1.5).sp, textAlign = align, modifier = modifier)
}

@Composable
fun QuoteText(text: String, size: Int = 20, align: TextAlign = TextAlign.Start, color: Color = Plum) {
    Text(text, color = color, fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = size.sp, lineHeight = (size * 1.45).sp, textAlign = align)
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, color: Color = LavenderDeep, onClick: () -> Unit) {
    Box(
        modifier
            .height(54.dp)
            .shadow(8.dp, RoundedCornerShape(50), ambientColor = color, spotColor = color)
            .clip(RoundedCornerShape(50))
            .background(Brush.horizontalGradient(listOf(color, lerpColor(color, Rose, 0.45f))))
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun GhostButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(52.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0x80FFFFFF))
            .border(1.dp, Hairline, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Plum, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun Chip(text: String, selected: Boolean, color: Color = Lavender, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) color.copy(alpha = 0.32f) else Color(0x99FFFFFF))
            .border(if (selected) 1.5.dp else 1.dp, if (selected) color else Hairline, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 10.dp)
    ) {
        Text(text, color = Plum, fontSize = 14.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
fun IconBubble(symbol: String, color: Color = Lavender, size: Dp = 46.dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.12f)))),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, fontSize = (size.value * 0.42f).sp, color = Plum)
    }
}

@Composable
fun BackBar(title: String? = null, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = Plum)
        }
        if (title != null) Text(title, color = PlumSoft, fontSize = 15.sp)
    }
}

@Composable
fun ProgressLine(fraction: Float, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(LavenderMist)
    ) {
        if (fraction > 0f) {
            Box(
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(Lavender, Rose)))
            )
        }
    }
}

@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = Lavender.copy(alpha = 0.2f), spotColor = Lavender.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(22.dp))
            .background(CardWhite)
            .padding(vertical = 16.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(value, color = LavenderDeep, fontFamily = Serif, fontSize = 26.sp)
        Spacer(Modifier.height(2.dp))
        Text(label, color = PlumSoft, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 15.sp)
    }
}

fun lerpColor(a: Color, b: Color, t: Float) = Color(
    a.red + (b.red - a.red) * t,
    a.green + (b.green - a.green) * t,
    a.blue + (b.blue - a.blue) * t,
    a.alpha + (b.alpha - a.alpha) * t
)

/**
 * Sfera che mescola i colori di una o più emozioni.
 * Ogni colore è una nuvola che ruota lentamente: insieme formano il tuo stato d'animo.
 */
@Composable
fun BlendOrb(colors: List<Color>, size: Dp, modifier: Modifier = Modifier, animate: Boolean = true) {
    val t = rememberInfiniteTransition(label = "orb")
    val rot by t.animateFloat(0f, 6.2832f, infiniteRepeatable(tween(18000, easing = LinearEasing)), label = "rot")
    val pulse by t.animateFloat(0.94f, 1f, infiniteRepeatable(tween(4000), RepeatMode.Reverse), label = "pulse")
    val cs = if (colors.isEmpty()) listOf(Lavender, Rose) else colors
    Canvas(modifier.size(size)) {
        val r = this.size.minDimension / 2f * (if (animate) pulse else 1f)
        val c = Offset(this.size.width / 2, this.size.height / 2)
        drawCircle(Brush.radialGradient(listOf(cs.first().copy(alpha = 0.35f), Color.Transparent), c, r * 1.25f), r * 1.25f, c)
        drawCircle(Color.White, r, c)
        cs.forEachIndexed { i, col ->
            val ang = (if (animate) rot else 0f) + i * (6.2832f / cs.size)
            val off = if (cs.size == 1) 0f else r * 0.38f
            val p = Offset(c.x + cos(ang) * off, c.y + sin(ang) * off)
            drawCircle(Brush.radialGradient(listOf(col.copy(alpha = 0.95f), col.copy(alpha = 0f)), p, r * 0.95f), r, c)
        }
        drawCircle(
            Brush.radialGradient(listOf(Color.White.copy(alpha = 0.75f), Color.Transparent), Offset(c.x - r * 0.35f, c.y - r * 0.4f), r * 0.6f),
            r, c
        )
    }
}

fun vibrate(context: Context, ms: Long = 40) {
    try {
        val v = context.getSystemService(Vibrator::class.java) ?: return
        if (v.hasVibrator()) v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    } catch (_: Exception) {
    }
}

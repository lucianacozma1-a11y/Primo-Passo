package it.primopasso

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.util.Locale

val IT: Locale = Locale.ITALIAN

fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Buongiorno"
    in 12..17 -> "Buon pomeriggio"
    in 18..21 -> "Buonasera"
    else -> "Buonanotte"
}

@Composable
fun TabColumn(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 48.dp)
    ) { content() }
}

@Composable
fun PageColumn(accent: Color = Lavender, onBack: (() -> Unit)?, content: @Composable () -> Unit) {
    SoftBackground(accent) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 12.dp, end = 20.dp, top = 8.dp, bottom = 40.dp)
        ) {
            if (onBack != null) BackBar(onBack = onBack)
            Column(Modifier.padding(start = 8.dp)) { content() }
        }
    }
}

@Composable
fun CenterPage(accent: Color = Lavender, center: Boolean = true, content: @Composable () -> Unit) {
    SoftBackground(accent) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (center) Arrangement.Center else Arrangement.Top
        ) { content() }
    }
}

@Composable
fun softFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Lavender, unfocusedBorderColor = Hairline,
    focusedTextColor = Plum, unfocusedTextColor = Plum, cursorColor = LavenderDeep,
    focusedContainerColor = Color(0xB3FFFFFF), unfocusedContainerColor = Color(0x80FFFFFF)
)

@Composable
fun BarRow(label: String, n: Int, max: Int, color: Color) {
    Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Plum, fontSize = 13.sp, modifier = Modifier.width(130.dp))
        Box(
            Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(LavenderMist)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(if (max == 0) 0f else n / max.toFloat())
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
        Text("  $n", color = PlumSoft, fontSize = 12.sp)
    }
}

@Composable
fun KitRow(ex: Exercise, highlight: Boolean, onClick: () -> Unit) {
    SoftCard(
        padding = PaddingValues(16.dp),
        modifier = Modifier.padding(bottom = 10.dp),
        tint = if (highlight) lerpColor(Color.White, Lavender, 0.16f) else CardWhite,
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(ex.title, color = Plum, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(ex.subtitle, color = PlumSoft, fontSize = 13.sp)
            }
            Text(durationLabel(ex.seconds), color = LavenderDeep, fontSize = 12.sp)
            Spacer(Modifier.width(4.dp))
            Text("›", color = LavenderDeep, fontSize = 22.sp)
        }
    }
}

@Composable
fun Dot(on: Boolean, size: Int = 26, content: String = "") {
    Box(
        Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(50))
            .background(if (on) LavenderDeep else LavenderMist),
        contentAlignment = Alignment.Center
    ) {
        Text(content, color = if (on) Color.White else LavenderDeep, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

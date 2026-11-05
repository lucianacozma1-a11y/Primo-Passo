package it.primopasso

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class Route {
    data class MicroStart(val pendingId: Long? = null, val step: String? = null) : Route()
    object Plan : Route()
    object Checkin : Route()
    object Thought : Route()
    data class ReadLesson(val id: Int) : Route()
    data class Kit(val id: String) : Route()
    object Profile : Route()
    object Quiz : Route()
    object Settings : Route()
}

data class LaunchRequest(val tab: Int, val route: Route?)

class MainActivity : ComponentActivity() {
    private var launch by mutableStateOf<LaunchRequest?>(null)

    private val askNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { Reminders.scheduleAll(this) }

    fun requestNotifications() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Reminders.createChannel(this)
        val prefs = Prefs(this)
        if (intent?.getBooleanExtra("skip_onboarding", false) == true) {
            prefs.onboardingDone = true
            prefs.remindersChosen = true
            if (prefs.blockProfile == null) prefs.blockProfile = "avvio"
        }
        Reminders.scheduleAll(this)
        if (prefs.onboardingDone) requestNotifications()
        launch = parse(intent, prefs)
        setContent {
            CalmaTheme { App(prefs, launch, { launch = null }, { requestNotifications() }) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val prefs = Prefs(this)
        if (intent.getBooleanExtra("skip_onboarding", false)) {
            prefs.onboardingDone = true
            prefs.remindersChosen = true
            if (prefs.blockProfile == null) prefs.blockProfile = "avvio"
        }
        parse(intent, prefs)?.let { launch = it }
    }

    private fun parse(intent: Intent?, prefs: Prefs): LaunchRequest? {
        if (intent == null) return null
        when (intent.getStringExtra(Reminders.EXTRA)) {
            "morning" -> return LaunchRequest(0, prefs.nextLesson()?.let { Route.ReadLesson(it.id) })
            "afternoon" -> return LaunchRequest(1, Route.MicroStart())
        }
        if (intent.hasExtra("open_tab") || intent.hasExtra("open_route")) {
            val route = when (intent.getStringExtra("open_route")) {
                "start" -> Route.MicroStart()
                "lesson" -> Route.ReadLesson(prefs.nextLesson()?.id ?: 1)
                "quiz" -> Route.Quiz
                "profile" -> Route.Profile
                "plan" -> Route.Plan
                "checkin" -> Route.Checkin
                "thought" -> Route.Thought
                "kit" -> Route.Kit(intent.getStringExtra("kit") ?: "k_ridicolo")
                else -> null
            }
            return LaunchRequest(intent.getIntExtra("open_tab", 0), route)
        }
        return null
    }
}

private data class TabItem(val label: String, val icon: ImageVector)

private val tabItems = listOf(
    TabItem("Oggi", Icons.Filled.Home),
    TabItem("Inizia", Icons.Filled.PlayArrow),
    TabItem("Lezioni", Icons.Filled.DateRange),
    TabItem("Kit", Icons.Filled.Build),
    TabItem("Abitudini", Icons.Filled.CheckCircle),
    TabItem("Pensieri", Icons.Filled.Face),
    TabItem("Registro", Icons.AutoMirrored.Filled.List)
)

@Composable
fun App(prefs: Prefs, launch: LaunchRequest?, onLaunchHandled: () -> Unit, requestNotifications: () -> Unit) {
    var onboarded by remember { mutableStateOf(prefs.onboardingDone) }
    var tab by remember { mutableIntStateOf(0) }
    var stack by remember { mutableStateOf(listOf<Route>()) }
    var version by remember { mutableIntStateOf(0) }
    val ctx = androidx.compose.ui.platform.LocalContext.current

    fun push(r: Route) {
        stack = stack + r
    }

    fun pop() {
        stack = stack.dropLast(1)
        version++
    }

    fun replaceTop(r: Route) {
        stack = stack.dropLast(1) + r
        version++
    }

    LaunchedEffect(launch) {
        val l = launch ?: return@LaunchedEffect
        onboarded = prefs.onboardingDone
        if (onboarded) {
            tab = l.tab
            stack = listOfNotNull(l.route)
        }
        onLaunchHandled()
    }

    if (!onboarded) {
        Onboarding(prefs, requestNotifications) {
            onboarded = true
            Reminders.scheduleAll(ctx)
            version++
        }
        return
    }

    BackHandler(enabled = stack.isNotEmpty() || tab != 0) {
        if (stack.isNotEmpty()) pop() else tab = 0
    }

    val top = stack.lastOrNull()
    if (top != null) {
        when (top) {
            is Route.MicroStart -> {
                val p = top.pendingId?.let { id -> prefs.pending().firstOrNull { it.id == id } }
                MicroStartFlow(prefs, p, top.step, onClose = { pop() })
            }
            is Route.ReadLesson -> {
                val l = lessonById(top.id)
                if (l == null) pop() else LessonScreen(prefs, l, onDone = { pop() }, onTry = { replaceTop(Route.MicroStart()) })
            }
            is Route.Kit -> {
                val k = kitById(top.id)
                if (k == null) pop() else KitFlow(k, onClose = { pop() })
            }
            Route.Profile -> ProfileScreen(prefs, onBack = { pop() }, onRedo = { replaceTop(Route.Quiz) })
            Route.Quiz -> QuizFlow(prefs, showIntro = true, finishLabel = "Vai al mio piano", onFinish = { replaceTop(Route.Plan) }, onClose = { pop() })
            Route.Plan -> PlanScreen(prefs, buttonLabel = "Fatto", onBack = { pop() }) { pop() }
            Route.Checkin -> CheckinFlow(prefs, onClose = { pop() })
            Route.Thought -> ThoughtFlow(prefs, onClose = { pop() }, onStart = { st -> replaceTop(Route.MicroStart(step = st)) })
            Route.Settings -> SettingsScreen(prefs, onBack = { pop() }, onChanged = { version++ }, onRequestNotifications = requestNotifications)
        }
        return
    }

    SoftBackground {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            TopTabs(tab) { tab = it }
            AnimatedContent(
                targetState = tab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tabs",
                modifier = Modifier.fillMaxSize()
            ) { t ->
                when (t) {
                    0 -> HomeTab(
                        prefs, version,
                        onStart = { push(Route.MicroStart()) },
                        onStartPending = { push(Route.MicroStart(it)) },
                        onLesson = { push(Route.ReadLesson(it)) },
                        onProfile = { push(Route.Profile) },
                        onOpenTab = { tab = it },
                        onSettings = { push(Route.Settings) },
                        onPlan = { push(Route.Plan) },
                        onCheckin = { push(Route.Checkin) }
                    )
                    1 -> StartTab(
                        prefs, version,
                        onStart = { push(Route.MicroStart()) },
                        onStartPending = { push(Route.MicroStart(it)) },
                        onKit = { push(Route.Kit(it)) },
                        onChanged = { version++ }
                    )
                    2 -> LessonsTab(prefs, version, onLesson = { push(Route.ReadLesson(it)) })
                    3 -> KitTab(prefs, version, onKit = { push(Route.Kit(it)) })
                    4 -> HabitsTab(prefs, version, onChanged = { version++ })
                    5 -> ThoughtsTab(prefs, version, onNew = { push(Route.Thought) })
                    else -> LogTab(prefs, version, onStart = { push(Route.MicroStart()) })
                }
            }
        }
    }
}

@Composable
private fun TopTabs(selected: Int, onSelect: (Int) -> Unit) {
    val scroll = rememberScrollState()
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 6.dp)
            .horizontalScroll(scroll)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabItems.forEachIndexed { i, item ->
            val sel = i == selected
            Row(
                Modifier
                    .shadow(if (sel) 10.dp else 4.dp, RoundedCornerShape(50), ambientColor = Lavender.copy(alpha = 0.3f), spotColor = Lavender.copy(alpha = 0.3f))
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (sel) Brush.linearGradient(listOf(LavenderDeep, Lavender))
                        else Brush.linearGradient(listOf(Color(0xE6FFFFFF), Color(0xE6FFFFFF)))
                    )
                    .clickable { onSelect(i) }
                    .padding(horizontal = 16.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(item.icon, contentDescription = null, tint = if (sel) Color.White else LavenderDeep, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text(item.label, color = if (sel) Color.White else Plum, fontSize = 14.sp, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium)
            }
        }
    }
}

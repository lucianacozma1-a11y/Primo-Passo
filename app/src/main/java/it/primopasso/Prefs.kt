package it.primopasso

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Un inizio registrato. */
data class StartEntry(
    val time: Long,
    val task: String,
    val category: String,
    val reason: String,
    val minutes: Int,
    val finished: Boolean
) {
    val epochDay: Long get() = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
    val hour: Int get() = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).hour
}

data class WeeklyCheckin(val time: Long, val mood: Int, val wentWell: String, val lighten: String, val note: String, val target: Int, val intention: String)
data class Habit(val id: Long, val text: String)
data class ThoughtEntry(val time: Long, val thought: String, val types: List<String>, val reframe: String, val step: String)

/** Una cosa che stai rimandando, salvata per iniziarla quando vuoi. */
data class PendingTask(val id: Long, val text: String, val category: String)

class Prefs(context: Context) {
    private val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences("primopasso", Context.MODE_PRIVATE)

    private fun today() = LocalDate.now().toEpochDay()

    // ---- profilo ----
    var onboardingDone: Boolean
        get() = sp.getBoolean("onboarding", false)
        set(v) = sp.edit().putBoolean("onboarding", v).apply()

    var name: String
        get() = sp.getString("name", "") ?: ""
        set(v) = sp.edit().putString("name", v.trim()).apply()

    var blockProfile: String?
        get() = sp.getString("block_profile", null)
        set(v) = sp.edit().putString("block_profile", v).apply()

    fun saveQuiz(answers: Map<String, Set<String>>) {
        val o = JSONObject()
        answers.forEach { (k, v) -> o.put(k, JSONArray(v.toList())) }
        sp.edit().putString("quiz", o.toString()).apply()
    }

    fun quizAnswers(): Map<String, Set<String>> = try {
        val o = JSONObject(sp.getString("quiz", "{}") ?: "{}")
        val out = HashMap<String, Set<String>>()
        o.keys().forEach { k ->
            val a = o.getJSONArray(k)
            out[k] = (0 until a.length()).map { a.getString(it) }.toSet()
        }
        out
    } catch (_: Exception) {
        emptyMap()
    }

    // ---- lezioni ----
    fun lessonsDone(): Set<Int> = (sp.getStringSet("lessons_done", emptySet()) ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()

    fun markLessonDone(id: Int) {
        val set = HashSet(sp.getStringSet("lessons_done", emptySet()) ?: emptySet())
        set.add(id.toString())
        sp.edit().putStringSet("lessons_done", set).putLong("lesson_day", today()).putInt("lesson_today_id", id).apply()
    }

    fun lessonDoneToday() = sp.getLong("lesson_day", -1L) == today()
    fun lessonTodayId() = sp.getInt("lesson_today_id", -1)
    fun nextLesson(): Lesson? = lessons.firstOrNull { it.id !in lessonsDone() }

    // ---- registro degli inizi ----
    fun startLog(): List<StartEntry> = try {
        val a = JSONArray(sp.getString("starts", "[]") ?: "[]")
        (0 until a.length()).map {
            val o = a.getJSONObject(it)
            StartEntry(o.optLong("t"), o.optString("x"), o.optString("c"), o.optString("r"), o.optInt("m"), o.optBoolean("f"))
        }.sortedByDescending { it.time }
    } catch (_: Exception) {
        emptyList()
    }

    fun addStart(e: StartEntry) {
        val a = try {
            JSONArray(sp.getString("starts", "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }
        a.put(
            JSONObject().put("t", e.time).put("x", e.task).put("c", e.category)
                .put("r", e.reason).put("m", e.minutes).put("f", e.finished)
        )
        sp.edit().putString("starts", a.toString()).apply()
    }

    // ---- cose rimandate ----
    fun pending(): List<PendingTask> = try {
        val a = JSONArray(sp.getString("pending", "[]") ?: "[]")
        (0 until a.length()).map {
            val o = a.getJSONObject(it)
            PendingTask(o.optLong("i"), o.optString("x"), o.optString("c"))
        }
    } catch (_: Exception) {
        emptyList()
    }

    private fun savePending(list: List<PendingTask>) {
        val a = JSONArray()
        list.forEach { a.put(JSONObject().put("i", it.id).put("x", it.text).put("c", it.category)) }
        sp.edit().putString("pending", a.toString()).apply()
    }

    fun addPending(text: String, category: String) {
        if (text.isBlank()) return
        savePending(pending() + PendingTask(System.currentTimeMillis(), text.trim(), category))
    }

    fun removePending(id: Long) = savePending(pending().filter { it.id != id })

    // ---- preferenze dal questionario ----
    var preferredMinutes: Int
        get() = sp.getInt("pref_minutes", 5)
        set(v) = sp.edit().putInt("pref_minutes", v).apply()

    fun applyQuiz(a: Map<String, Set<String>>) {
        preferredMinutes = preferredMinutes(a)
        morningMinutes = preferredMorning(a)
        if (weeklyTarget == 0) weeklyTarget = if (preferredMinutes <= 2) 3 else 5
    }

    // ---- obiettivo della settimana ----
    var weeklyTarget: Int
        get() = sp.getInt("weekly_target", 0)
        set(v) = sp.edit().putInt("weekly_target", v).apply()

    var weeklyIntention: String
        get() = sp.getString("weekly_intention", "") ?: ""
        set(v) = sp.edit().putString("weekly_intention", v.trim()).apply()

    // ---- check-in settimanale ----
    private fun jsonList(key: String): JSONArray = try {
        JSONArray(sp.getString(key, "[]") ?: "[]")
    } catch (_: Exception) {
        JSONArray()
    }

    fun checkins(): List<WeeklyCheckin> {
        val a = jsonList("checkins")
        return (0 until a.length()).map {
            val o = a.getJSONObject(it)
            WeeklyCheckin(o.optLong("t"), o.optInt("m"), o.optString("w"), o.optString("l"), o.optString("n"), o.optInt("g"), o.optString("i"))
        }.sortedByDescending { it.time }
    }

    fun addCheckin(c: WeeklyCheckin) {
        val a = jsonList("checkins")
        a.put(JSONObject().put("t", c.time).put("m", c.mood).put("w", c.wentWell).put("l", c.lighten).put("n", c.note).put("g", c.target).put("i", c.intention))
        sp.edit().putString("checkins", a.toString()).putLong("last_checkin_day", today()).apply()
        weeklyTarget = c.target
        weeklyIntention = c.intention
    }

    var installDay: Long
        get() {
            if (!sp.contains("install_day")) sp.edit().putLong("install_day", today()).apply()
            return sp.getLong("install_day", today())
        }
        set(v) = sp.edit().putLong("install_day", v).apply()

    fun checkinDue(): Boolean {
        val last = sp.getLong("last_checkin_day", -1L)
        return if (last < 0) today() - installDay >= 6 else today() - last >= 7
    }

    // ---- piccole abitudini ----
    fun habits(): List<Habit> {
        val a = jsonList("habits")
        return (0 until a.length()).map { val o = a.getJSONObject(it); Habit(o.optLong("i"), o.optString("x")) }
    }

    fun addHabit(text: String) {
        if (text.isBlank()) return
        val a = jsonList("habits")
        a.put(JSONObject().put("i", System.currentTimeMillis()).put("x", text.trim()))
        sp.edit().putString("habits", a.toString()).apply()
    }

    fun removeHabit(id: Long) {
        val list = habits().filter { it.id != id }
        val a = JSONArray()
        list.forEach { a.put(JSONObject().put("i", it.id).put("x", it.text)) }
        sp.edit().putString("habits", a.toString()).apply()
    }

    fun habitDone(id: Long, day: Long) = (sp.getStringSet("habit_done", emptySet()) ?: emptySet()).contains("$id:$day")

    fun toggleHabit(id: Long, day: Long) {
        val set = HashSet(sp.getStringSet("habit_done", emptySet()) ?: emptySet())
        val k = "$id:$day"
        if (!set.remove(k)) set.add(k)
        sp.edit().putStringSet("habit_done", set).apply()
    }

    // ---- pensieri che bloccano ----
    fun thoughts(): List<ThoughtEntry> {
        val a = jsonList("thoughts")
        return (0 until a.length()).map {
            val o = a.getJSONObject(it)
            val t = o.optJSONArray("y") ?: JSONArray()
            ThoughtEntry(o.optLong("t"), o.optString("x"), (0 until t.length()).map { i -> t.getString(i) }, o.optString("r"), o.optString("s"))
        }.sortedByDescending { it.time }
    }

    fun addThought(e: ThoughtEntry) {
        val a = jsonList("thoughts")
        a.put(JSONObject().put("t", e.time).put("x", e.thought).put("y", JSONArray(e.types)).put("r", e.reframe).put("s", e.step))
        sp.edit().putString("thoughts", a.toString()).apply()
    }

    // ---- promemoria ----
    var morningOn: Boolean
        get() = sp.getBoolean("rem_morning", true)
        set(v) = sp.edit().putBoolean("rem_morning", v).apply()

    var morningMinutes: Int
        get() = sp.getInt("rem_morning_time", 8 * 60 + 30)
        set(v) = sp.edit().putInt("rem_morning_time", v).apply()

    var afternoonOn: Boolean
        get() = sp.getBoolean("rem_afternoon", true)
        set(v) = sp.edit().putBoolean("rem_afternoon", v).apply()

    var afternoonMinutes: Int
        get() = sp.getInt("rem_afternoon_time", 15 * 60)
        set(v) = sp.edit().putInt("rem_afternoon_time", v).apply()

    var remindersChosen: Boolean
        get() = sp.getBoolean("reminders_chosen", false)
        set(v) = sp.edit().putBoolean("reminders_chosen", v).apply()
}

fun formatTime(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)

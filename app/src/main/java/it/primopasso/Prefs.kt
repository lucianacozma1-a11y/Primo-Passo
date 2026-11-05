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

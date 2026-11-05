package it.primopasso

import androidx.compose.ui.graphics.Color

// ---------------- Modello del respiro / esercizi (usato dai player) ----------------

enum class Kind { IN, HOLD, OUT, REST }

data class Phase(val kind: Kind, val seconds: Int, val label: String, val hint: String)

data class Pattern(
    val id: String,
    val name: String,
    val tagline: String,
    val about: String,
    val phases: List<Phase>,
    val defaultCycles: Int
) {
    val cycleSeconds: Int get() = phases.sumOf { it.seconds }
}

data class Step(val title: String, val text: String, val seconds: Int = 0)

data class Exercise(
    val id: String,
    val title: String,
    val subtitle: String,
    val pattern: Pattern? = null,
    val cycles: Int = 0,
    val steps: List<Step> = emptyList(),
    val forProfiles: List<String> = emptyList()
) {
    val seconds: Int
        get() = if (pattern != null) pattern.cycleSeconds * cycles
        else steps.sumOf { if (it.seconds > 0) it.seconds else 20 }
}

fun durationLabel(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return when {
        m == 0 -> "$s s"
        s == 0 -> "$m min"
        else -> "$m min $s s"
    }
}

val SIGH = Pattern(
    "sigh", "Sospiro per ripartire", "Abbassa la tensione prima di iniziare",
    "Due inspirazioni ravvicinate e una lunga espirazione: scarica l'agitazione in meno di un minuto.",
    listOf(
        Phase(Kind.IN, 2, "Inspira", "Dal naso"),
        Phase(Kind.IN, 1, "Ancora un po'", "Riempi fino in cima"),
        Phase(Kind.OUT, 6, "Espira", "Lenta, dalla bocca")
    ), 5
)

// ---------------- Profili: come ti blocchi ----------------

data class BlockProfile(
    val id: String,
    val name: String,
    val symbol: String,
    val title: String,
    val description: String,
    val helps: List<String>,
    val tip: String,
    val lessonIds: List<Int>,
    val kitIds: List<String>
)

val blockProfiles = listOf(
    BlockProfile(
        "avvio", "Il blocco della partenza", "◔",
        "Ti serve il segnale di partenza",
        "Sai cosa fare e saresti capace di farlo. Quello che manca è la spinta dei primi secondi: il compito sembra un blocco unico, troppo grande per entrarci.",
        listOf("Passi così piccoli da sembrare ridicoli", "Un timer che parte per te", "Fare accanto a qualcuno"),
        "Non pensare al compito intero: solo al primo gesto.",
        listOf(1, 10, 11, 13), listOf("k_ridicolo", "k_conto", "k_due")
    ),
    BlockProfile(
        "rimugino", "La mente che pesa ogni scelta", "∞",
        "Pensi tanto, prima di muoverti",
        "La tua mente vuole fare bene, così valuta ogni opzione e ogni rischio. Il pensiero gira in tondo e intanto il tempo passa. Non è pigrizia: è troppa cura.",
        listOf("Decidere in 10 secondi", "La bozza volutamente brutta", "\"Fatto\" prima di \"perfetto\""),
        "Non serve la scelta perfetta: va bene la prima ragionevole.",
        listOf(16, 17, 19, 21), listOf("k_dieci", "k_bozza", "k_svuota")
    ),
    BlockProfile(
        "scadenza", "Il motore dell'ultimo minuto", "⏳",
        "Ti accendi con l'urgenza",
        "Sotto pressione funzioni benissimo, ma l'attesa ti costa ansia, corse e notti corte. L'urgenza è il tuo carburante: possiamo crearne di più gentili, molto prima della scadenza.",
        listOf("Mini-scadenze vicine e dichiarate", "Un timer ben visibile", "Cinque minuti oggi, non tutto domani"),
        "Il timer è la tua urgenza gentile: usalo come una piccola scadenza.",
        listOf(3, 11, 12, 20), listOf("k_minisc", "k_due", "k_terreno")
    ),
    BlockProfile(
        "disperdo", "L'attenzione che cerca novità", "✺",
        "La tua attenzione ama le cose nuove",
        "Parti con entusiasmo, poi qualcosa di più interessante ti chiama e ti ritrovi altrove. Non è mancanza di volontà: la tua mente cerca stimoli, e si può lavorare con lei invece che contro.",
        listOf("Una cosa sola sullo schermo", "Una ricompensa subito dopo", "Blocchi brevi e chiari"),
        "Per questi minuti, una cosa sola: chiudi il resto.",
        listOf(22, 12, 14, 5), listOf("k_unasola", "k_ricompensa", "k_svuota")
    )
)

fun profileById(id: String?) = blockProfiles.firstOrNull { it.id == id }

// ---------------- Questionario ----------------

data class QOption(val label: String, val profile: String? = null, val value: String = label)

data class QQuestion(val id: String, val text: String, val hint: String?, val options: List<QOption>, val multi: Boolean = false)

private val freq = listOf(QOption("Spesso", value = "spesso"), QOption("A volte", value = "avolte"), QOption("Raramente", value = "raramente"))

private const val ALL = "Scegli tutto ciò che vale per te"

val quizQuestions = listOf(
    QQuestion(
        "q1", "Quando rimandi qualcosa, cosa succede di solito?", ALL,
        listOf(
            QOption("So cosa fare ma non riesco a partire", "avvio"),
            QOption("Ci penso e ripenso, e resto ferma", "rimugino"),
            QOption("Aspetto l'ultimo momento, poi corro", "scadenza"),
            QOption("Inizio, poi mi distraggo con altro", "disperdo")
        ), multi = true
    ),
    QQuestion(
        "q2", "Cosa ti passa per la testa prima di iniziare?", ALL,
        listOf(
            QOption("Mi sembra una montagna", "avvio"),
            QOption("Non so da dove cominciare", "avvio"),
            QOption("Ho paura di sbagliare o di non farlo bene", "rimugino"),
            QOption("Devo prima capire tutto alla perfezione", "rimugino"),
            QOption("Tanto c'è ancora tempo", "scadenza"),
            QOption("Mi vengono in mente mille altre cose", "disperdo"),
            QOption("Mi sento già stanca", value = "stanca_pre")
        ), multi = true
    ),
    QQuestion(
        "q3", "Quando riesci a lavorare bene?", ALL,
        listOf(
            QOption("Quando qualcuno mi dà il via o è con me", "avvio"),
            QOption("Quando la decisione è già presa", "rimugino"),
            QOption("Sotto pressione, all'ultimo", "scadenza"),
            QOption("Quando è nuovo o interessante", "disperdo"),
            QOption("Quando sono riposata", value = "riposata")
        ), multi = true
    ),
    QQuestion(
        "emozioni", "Cosa senti quando rimandi?", ALL,
        listOf(
            QOption("Senso di colpa", value = "colpa"), QOption("Ansia", value = "ansia"),
            QOption("Vergogna", value = "vergogna"), QOption("Frustrazione", value = "frustrazione"),
            QOption("Sollievo, per un po'", value = "sollievo"), QOption("Niente di particolare", value = "niente")
        ), multi = true
    ),
    QQuestion(
        "invece", "Cosa fai invece, quando rimandi?", ALL,
        listOf(
            QOption("Scorro il telefono", value = "telefono"), QOption("Faccio altre cose meno importanti", value = "altro"),
            QOption("Pulisco o riordino", value = "pulisco"), QOption("Mangio o spizzico", value = "mangio"),
            QOption("Mi sdraio o dormo", value = "dormo"), QOption("Penso e ripenso", value = "penso")
        ), multi = true
    ),
    QQuestion(
        "aree", "Cosa rimandi più spesso?", ALL,
        listOf(
            QOption("Casa e ordine", value = "casa"), QOption("Carte e burocrazia", value = "carte"),
            QOption("Lavoro", value = "lavoro"), QOption("Studio", value = "studio"),
            QOption("Movimento", value = "movimento"), QOption("Messaggi e telefonate", value = "messaggi"),
            QOption("Cura di me", value = "cura")
        ), multi = true
    ),
    QQuestion(
        "energia", "In che momento della giornata hai più energia?", null,
        listOf(
            QOption("Appena sveglia", value = "presto"), QOption("Tarda mattina", value = "mattina"),
            QOption("Pomeriggio", value = "pomeriggio"), QOption("Sera", value = "sera")
        )
    ),
    QQuestion(
        "durata", "Per quanto riesci a concentrarti senza fatica?", "Sii sincera: qui si parte da dove sei davvero",
        listOf(
            QOption("Un paio di minuti", value = "2"), QOption("Circa cinque minuti", value = "5"),
            QOption("Dieci, quindici minuti", value = "10"), QOption("Più di venti minuti", value = "20")
        )
    ),
    QQuestion(
        "aiuta", "Cosa ti aiuta a partire?", ALL,
        listOf(
            QOption("Una scadenza", value = "scadenza"), QOption("Qualcuno vicino a me", value = "compagnia"),
            QOption("La musica", value = "musica"), QOption("Una ricompensa", value = "ricompensa"),
            QOption("Una lista chiara", value = "lista"), QOption("Un timer", value = "timer"),
            QOption("Un posto in ordine", value = "ordine"), QOption("Non lo so ancora", value = "nonso")
        ), multi = true
    ),
    QQuestion(
        "voce", "Come ti parli quando non riesci a fare qualcosa?", null,
        listOf(
            QOption("Mi critico duramente", value = "dura"), QOption("Mi dico che sono pigra", value = "pigra"),
            QOption("Di solito mi capisco", value = "gentile"), QOption("Non ci faccio caso", value = "neutra")
        )
    ),
    QQuestion("stanchezza", "Ti senti stanca, nella mente o nel corpo?", null, freq),
    QQuestion("rimuginio", "La sera la mente fatica a staccare?", null, freq),
    QQuestion("no", "Ti è difficile dire di no agli altri?", null, freq),
    QQuestion(
        "sacrifici", "Cosa sacrifichi più spesso per gli altri?", ALL,
        listOf(
            QOption("Il sonno"), QOption("Il tempo per me"), QOption("I miei obiettivi"),
            QOption("Lo spazio emotivo"), QOption("Niente di particolare", value = "niente")
        ), multi = true
    ),
    QQuestion(
        "desideri", "Cosa ti piacerebbe, tra un mese?", ALL,
        listOf(
            QOption("Iniziare senza tanta fatica"), QOption("Meno cose in sospeso"),
            QOption("Meno senso di colpa"), QOption("Più tempo per me"),
            QOption("Più energia"), QOption("Più fiducia in me stessa")
        ), multi = true
    )
)

/** Valori "esclusivi" che deselezionano gli altri nelle domande multiple. */
val exclusiveValues = setOf("niente", "nonso")

private val helpToKit = mapOf(
    "scadenza" to "k_minisc", "ricompensa" to "k_ricompensa", "lista" to "k_svuota",
    "timer" to "k_due", "ordine" to "k_terreno", "musica" to "k_unasola"
)

fun personalKitIds(a: Map<String, Set<String>>, p: BlockProfile?): List<String> {
    val fromHelp = (a["aiuta"] ?: emptySet()).mapNotNull { helpToKit[it] }
    val extra = mutableListOf<String>()
    if (a["q2"]?.contains("stanca_pre") == true || a["stanchezza"]?.contains("spesso") == true) extra += "k_sospiro"
    if (a["invece"]?.contains("penso") == true) extra += "k_dieci"
    return ((p?.kitIds ?: emptyList()) + fromHelp + extra).distinct().take(5)
}

fun personalLessonIds(a: Map<String, Set<String>>, p: BlockProfile?): List<Int> {
    val extra = mutableListOf<Int>()
    val voce = a["voce"] ?: emptySet()
    if ("dura" in voce || "pigra" in voce) extra += listOf(2, 23, 16)
    if (a["no"]?.contains("spesso") == true) extra += 9
    if (a["invece"]?.contains("telefono") == true) extra += 22
    if (a["invece"]?.contains("penso") == true) extra += 18
    if (a["emozioni"]?.contains("ansia") == true) extra += listOf(13, 15)
    if (a["stanchezza"]?.contains("spesso") == true) extra += 3
    return (extra + (p?.lessonIds ?: emptyList())).distinct().take(7)
}

fun preferredMinutes(a: Map<String, Set<String>>): Int = when (a["durata"]?.firstOrNull()) {
    "2" -> 2
    "10", "20" -> 10
    else -> 5
}

fun preferredMorning(a: Map<String, Set<String>>): Int = when (a["energia"]?.firstOrNull()) {
    "presto" -> 7 * 60 + 45
    "mattina" -> 9 * 60 + 30
    "pomeriggio" -> 13 * 60 + 30
    "sera" -> 18 * 60
    else -> 8 * 60 + 30
}

fun computeProfile(answers: Map<String, Set<String>>): String {
    val counts = LinkedHashMap<String, Int>()
    quizQuestions.forEach { q ->
        q.options.filter { it.profile != null && answers[q.id]?.contains(it.value) == true }.forEach {
            counts[it.profile!!] = (counts[it.profile] ?: 0) + 1
        }
    }
    val q1 = quizQuestions.first()
    val first = q1.options.firstOrNull { answers[q1.id]?.contains(it.value) == true }?.profile
    val max = counts.values.maxOrNull() ?: return first ?: "avvio"
    val top = counts.filterValues { it == max }.keys
    return if (first != null && first in top) first else top.first()
}

// ---------------- Kit per partire ----------------

val startKit = listOf(
    Exercise(
        "k_ridicolo", "Il passo ridicolo", "Rimpicciolisci finché diventa facile",
        steps = listOf(
            Step("Scegli", "Pensa alla cosa che stai rimandando."),
            Step("Rimpicciolisci", "Qual è il primissimo gesto? Rendilo più piccolo. Ancora più piccolo. Finché ti fa quasi sorridere."),
            Step("Fallo adesso", "Fai solo quel gesto. Niente di più.", 60),
            Step("Nota", "Hai iniziato. Se vuoi, fai il gesto successivo.")
        )
    ),
    Exercise(
        "k_conto", "Conto alla rovescia", "Parti prima che la mente trovi scuse",
        steps = listOf(
            Step("Pensa al gesto", "Visualizza il primo movimento che farai: alzarti, aprire, prendere."),
            Step("5, 4, 3, 2, 1", "Conta all'indietro, lentamente.", 5),
            Step("Muoviti", "Al \"uno\" fai il gesto, subito, senza pensarci.")
        )
    ),
    Exercise(
        "k_due", "Regola dei due minuti", "Se è breve, fallo ora",
        steps = listOf(
            Step("Cerca una cosa breve", "Pensa a una cosa in sospeso che richiede meno di due minuti."),
            Step("Falla", "Ora. Il timer è per te.", 120),
            Step("Una in meno", "Una cosa in meno nella testa. Senti la differenza?")
        )
    ),
    Exercise(
        "k_dieci", "Decidi in 10 secondi", "Esci dal giro dei pensieri",
        steps = listOf(
            Step("Le opzioni", "Elenca le due o tre opzioni tra cui stai esitando."),
            Step("Dieci secondi", "Scegline una, la prima ragionevole. Va bene così.", 10),
            Step("Vai", "Fai subito il primo passo dell'opzione scelta. Nessun ripensamento per oggi.")
        )
    ),
    Exercise(
        "k_bozza", "La bozza brutta", "Cinque minuti di imperfezione permessa",
        steps = listOf(
            Step("Il permesso", "Ti dai il permesso di fare una versione brutta. Nessuno la vedrà."),
            Step("Scrivi, abbozza, inizia", "Cinque minuti senza correggere, senza rileggere.", 300),
            Step("Esiste", "Ora c'è qualcosa da migliorare. Il più è fatto.")
        )
    ),
    Exercise(
        "k_svuota", "Svuota la testa", "Libera spazio prima di iniziare",
        steps = listOf(
            Step("Prendi un foglio", "Anche una nota sul telefono va bene."),
            Step("Scrivi tutto", "Tutto ciò che ti gira in testa: cose da fare, preoccupazioni, idee. Senza ordine.", 120),
            Step("Scegli una cosa", "Tra tutte, cerchia l'unica cosa da fare adesso. Il resto è al sicuro sul foglio.")
        )
    ),
    Exercise(
        "k_minisc", "La mini-scadenza", "Un'urgenza gentile, creata da te",
        steps = listOf(
            Step("Cosa", "Scegli una piccola parte del compito."),
            Step("Quando", "Fissa un orario vicino: entro un'ora, entro pranzo."),
            Step("Chi", "Dillo a qualcuno o scrivilo dove lo vedi. Una scadenza dichiarata è più reale."),
            Step("Via", "Ora fai i primi cinque minuti, per partire in vantaggio.")
        )
    ),
    Exercise(
        "k_unasola", "Una cosa sola", "Dieci minuti senza distrazioni",
        steps = listOf(
            Step("Chiudi", "Chiudi le schede e le app che non servono. Telefono lontano o a faccia in giù."),
            Step("Una cosa", "Tieni davanti a te solo ciò che serve per questo compito."),
            Step("Dieci minuti", "Solo questo. Se arriva un pensiero, scrivilo e torna qui.", 600)
        )
    ),
    Exercise(
        "k_ricompensa", "Prima il passo, poi il premio", "Dai al cervello un motivo per partire",
        steps = listOf(
            Step("Scegli il premio", "Una cosa piccola e piacevole: una canzone, un tè, un messaggio a un'amica."),
            Step("Cinque minuti", "Fai cinque minuti del compito.", 300),
            Step("Premiati", "Ora goditi il premio, senza sensi di colpa. Te lo sei guadagnato.")
        )
    ),
    Exercise(
        "k_terreno", "Prepara il terreno", "Rendi facile il domani",
        steps = listOf(
            Step("Domani", "Pensa a una cosa che vuoi fare domani."),
            Step("Gli strumenti", "Prepara oggi ciò che serve: apri il file, metti fuori le scarpe, scrivi il numero da chiamare."),
            Step("Il primo gesto", "Scrivi su un foglietto il primo gesto di domattina e lascialo dove lo vedrai.")
        )
    ),
    Exercise("k_sospiro", "Sospiro per ripartire", "Quando l'agitazione ti blocca", SIGH, 5)
)

fun kitById(id: String) = startKit.firstOrNull { it.id == id }

// ---------------- Inizia ora: categorie, primi passi, motivi ----------------

data class StartCategory(val id: String, val label: String, val symbol: String)

val startCategories = listOf(
    StartCategory("casa", "Casa e ordine", "⌂"),
    StartCategory("carte", "Carte", "✉"),
    StartCategory("lavoro", "Lavoro", "▤"),
    StartCategory("studio", "Studio", "✎"),
    StartCategory("movimento", "Movimento", "↟"),
    StartCategory("messaggi", "Messaggi", "☏"),
    StartCategory("cura", "Cura di me", "♡"),
    StartCategory("altro", "Altro", "✦")
)

fun categoryById(id: String) = startCategories.firstOrNull { it.id == id } ?: startCategories.last()

val microTemplates = mapOf(
    "casa" to listOf("Prendi un sacchetto e butta 5 cose", "Libera solo il piano del tavolo", "Rimetti a posto 10 oggetti, non uno di più"),
    "carte" to listOf("Apri la busta o il documento, solo guardalo", "Scrivi su un foglio cosa ti serve per finire", "Cerca il sito o il numero che ti serve"),
    "lavoro" to listOf("Apri il file e scrivi solo il titolo", "Scrivi la prima frase, anche brutta", "Elenca i tre passi successivi"),
    "studio" to listOf("Apri il libro alla pagina giusta", "Leggi solo il primo paragrafo", "Scrivi tre parole chiave"),
    "movimento" to listOf("Metti le scarpe da ginnastica", "Fai un minuto di stretching", "Esci e cammina fino all'angolo"),
    "messaggi" to listOf("Apri la chat e scrivi solo \"Ciao!\"", "Scrivi la bozza senza inviarla", "Componi il numero, il resto viene"),
    "cura" to listOf("Riempi un bicchiere d'acqua e bevilo", "Prenota solo l'appuntamento", "Prepara le cose per la doccia"),
    "altro" to listOf("Scrivi qual è il primissimo gesto", "Prepara ciò che ti serve, senza iniziare", "Fai solo la parte più facile")
)

val startReasons = listOf("È noioso", "È troppo grande", "Non so da dove iniziare", "Paura di sbagliare", "Sono stanca", "Ho troppe cose", "Non lo so")

val startCheers = listOf(
    "Il passo più difficile era questo, ed è fatto.",
    "Hai trasformato un \"dopo\" in un \"adesso\".",
    "Ogni inizio si somma. Questo conta.",
    "Non serviva la motivazione: è bastato partire.",
    "Ecco la prova che puoi iniziare, anche oggi."
)

val dailyNudges = listOf(
    "Non devi finire. Devi solo iniziare.",
    "Il primo passo può essere minuscolo.",
    "Cinque minuti cambiano la giornata più di un'ora di sensi di colpa.",
    "La motivazione arriva dopo aver iniziato.",
    "Saltare un giorno non rompe nulla. Si riparte da qui.",
    "Fatto è meglio di perfetto.",
    "Una cosa sola, adesso."
)

val Sun = Color(0xFFE6DAC4)
val Sage = Color(0xFF8DB8A3)


// ---------------- Consiglio del giorno ----------------

val profileTips = mapOf(
    "avvio" to listOf(
        "Oggi scegli il passo più ridicolo possibile: aprire, prendere, scrivere una parola.",
        "Se non parti, rimpicciolisci ancora. Il problema non sei tu: è il passo troppo grande.",
        "Chiedi a qualcuno di fare \"il via\" con te: un messaggio basta.",
        "Il timer parte per te: tu devi solo esserci.",
        "Prepara gli strumenti prima di iniziare: è già un inizio."
    ),
    "rimugino" to listOf(
        "Dai a una scelta di oggi dieci secondi, non dieci minuti.",
        "Scrivi la versione brutta: si sistema dopo.",
        "Chiediti: cosa succede di peggio se lo faccio al 70%?",
        "Quando giri in tondo, scrivi le opzioni e scegli la prima ragionevole.",
        "Il pensiero perfetto non arriva: arriva il pensiero fatto."
    ),
    "scadenza" to listOf(
        "Crea una mini-scadenza entro oggi e dilla a qualcuno.",
        "Cinque minuti oggi valgono più di una notte di corsa domani.",
        "Metti il timer ben visibile: è la tua urgenza gentile.",
        "Dividi la scadenza grande in tre scadenze piccole.",
        "Prepara stasera il primo passo di domani."
    ),
    "disperdo" to listOf(
        "Una cosa sola: chiudi le altre schede per dieci minuti.",
        "Tieni accanto un foglio \"dopo\" per i pensieri che arrivano.",
        "Premiati subito dopo il primo blocco di cinque minuti.",
        "Rendi nuovo il compito: cambia posto, musica o ordine.",
        "Blocchi brevi e chiari: cinque minuti, pausa, altri cinque."
    )
)

// ---------------- Pensieri che bloccano ----------------

data class ThoughtType(val id: String, val name: String, val explain: String)

val thoughtExamples = listOf(
    "Non ce la farò mai", "Deve essere perfetto", "Ormai è troppo tardi", "Gli altri sono più bravi di me",
    "Se sbaglio è un disastro", "Sono pigra", "Lo faccio quando avrò voglia", "Non ho tempo"
)

val thoughtTypes = listOf(
    ThoughtType("tutto", "Tutto o niente", "Vedo solo due possibilità: perfetto o fallimento."),
    ThoughtType("catastrofe", "Catastrofe", "Immagino subito lo scenario peggiore."),
    ThoughtType("devo", "I \"devo\"", "Regole rigide su come dovrei essere o fare."),
    ThoughtType("etichetta", "Etichetta", "Mi giudico con una parola: pigra, incapace, disordinata."),
    ThoughtType("mente", "Leggere nel pensiero", "Do per scontato cosa pensano gli altri di me."),
    ThoughtType("futuro", "Previsione negativa", "Sono sicura che andrà male, prima ancora di provare."),
    ThoughtType("confronto", "Confronto", "Mi misuro con gli altri e mi sento sempre indietro.")
)

val reframeQuestions = listOf(
    "Quali prove ho che sia vero? E quali che non lo sia?",
    "Cosa è successo le altre volte in cui l'ho pensato?",
    "Cosa direi a un'amica che pensa la stessa cosa?",
    "Qual è una versione più gentile e realistica di questo pensiero?"
)

// ---------------- Piccole abitudini ----------------

val habitIdeas = listOf(
    "Un bicchiere d'acqua al risveglio", "Cinque minuti di ordine", "Leggere due pagine",
    "Preparare le cose la sera", "Una passeggiata breve", "Scrivere le tre cose del giorno",
    "Spegnere gli schermi prima di dormire", "Un respiro lento prima di iniziare", "Rispondere a un messaggio rimandato"
)

val checkinWentWell = listOf("Ho iniziato più cose", "Mi sono sentita meno in colpa", "Ho finito qualcosa di importante", "Ho riposato", "Ho chiesto aiuto", "Ho detto qualche no")
val checkinLighten = listOf("Meno impegni", "Più pause", "Dormire di più", "Dire più no", "Meno telefono", "Chiedere aiuto")

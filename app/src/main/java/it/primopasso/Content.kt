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
        listOf(2, 3, 4, 10, 14), listOf("k_ridicolo", "k_conto", "k_due")
    ),
    BlockProfile(
        "rimugino", "La mente che pesa ogni scelta", "∞",
        "Pensi tanto, prima di muoverti",
        "La tua mente vuole fare bene, così valuta ogni opzione e ogni rischio. Il pensiero gira in tondo e intanto il tempo passa. Non è pigrizia: è troppa cura.",
        listOf("Decidere in 10 secondi", "La bozza volutamente brutta", "\"Fatto\" prima di \"perfetto\""),
        "Non serve la scelta perfetta: va bene la prima ragionevole.",
        listOf(6, 7, 8, 21), listOf("k_dieci", "k_bozza", "k_svuota")
    ),
    BlockProfile(
        "scadenza", "Il motore dell'ultimo minuto", "⏳",
        "Ti accendi con l'urgenza",
        "Sotto pressione funzioni benissimo, ma l'attesa ti costa ansia, corse e notti corte. L'urgenza è il tuo carburante: possiamo crearne di più gentili, molto prima della scadenza.",
        listOf("Mini-scadenze vicine e dichiarate", "Un timer ben visibile", "Cinque minuti oggi, non tutto domani"),
        "Il timer è la tua urgenza gentile: usalo come una piccola scadenza.",
        listOf(9, 4, 15, 24), listOf("k_minisc", "k_due", "k_terreno")
    ),
    BlockProfile(
        "disperdo", "L'attenzione che cerca novità", "✺",
        "La tua attenzione ama le cose nuove",
        "Parti con entusiasmo, poi qualcosa di più interessante ti chiama e ti ritrovi altrove. Non è mancanza di volontà: la tua mente cerca stimoli, e si può lavorare con lei invece che contro.",
        listOf("Una cosa sola sullo schermo", "Una ricompensa subito dopo", "Blocchi brevi e chiari"),
        "Per questi minuti, una cosa sola: chiudi il resto.",
        listOf(11, 12, 13, 27), listOf("k_unasola", "k_ricompensa", "k_svuota")
    )
)

fun profileById(id: String?) = blockProfiles.firstOrNull { it.id == id }

// ---------------- Questionario ----------------

data class QOption(val label: String, val profile: String? = null, val value: String = label)

data class QQuestion(val id: String, val text: String, val hint: String?, val options: List<QOption>, val multi: Boolean = false)

private val freq = listOf(QOption("Spesso", value = "spesso"), QOption("A volte", value = "avolte"), QOption("Raramente", value = "raramente"))

val quizQuestions = listOf(
    QQuestion(
        "q1", "Quando rimandi qualcosa, di solito…", null,
        listOf(
            QOption("So cosa fare ma non riesco a partire", "avvio"),
            QOption("Ci penso e ripenso, e resto ferma", "rimugino"),
            QOption("Aspetto l'ultimo momento, poi corro", "scadenza"),
            QOption("Inizio, poi mi distraggo con altro", "disperdo")
        )
    ),
    QQuestion(
        "q2", "Cosa succede nella tua testa prima di iniziare?", null,
        listOf(
            QOption("Mi sembra una montagna", "avvio"),
            QOption("Ho paura di sbagliare o di non farlo bene", "rimugino"),
            QOption("Non sento urgenza, finché non c'è", "scadenza"),
            QOption("Mi vengono in mente mille altre cose", "disperdo")
        )
    ),
    QQuestion(
        "q3", "Quando riesci a lavorare bene?", null,
        listOf(
            QOption("Quando qualcuno mi dà il via o è con me", "avvio"),
            QOption("Quando la decisione è già presa", "rimugino"),
            QOption("Sotto pressione, all'ultimo", "scadenza"),
            QOption("Quando è nuovo o interessante", "disperdo")
        )
    ),
    QQuestion("stanchezza", "Ti senti stanca, nella mente o nel corpo?", null, freq),
    QQuestion("rimuginio", "Ti capita di rimuginare la sera, faticando a staccare?", null, freq),
    QQuestion("no", "Ti è difficile dire di no agli altri?", null, freq),
    QQuestion(
        "sacrifici", "Cosa sacrifichi più spesso per gli altri?", "Scegli tutto ciò che vale per te",
        listOf(
            QOption("Il sonno"), QOption("Il tempo per me"), QOption("I miei obiettivi"),
            QOption("Lo spazio emotivo"), QOption("Niente di particolare", value = "niente")
        ),
        multi = true
    ),
    QQuestion(
        "aree", "Cosa rimandi più spesso?", "Scegli tutto ciò che vale per te",
        listOf(
            QOption("Casa e ordine", value = "casa"), QOption("Carte e burocrazia", value = "carte"),
            QOption("Lavoro", value = "lavoro"), QOption("Studio", value = "studio"),
            QOption("Movimento", value = "movimento"), QOption("Messaggi e telefonate", value = "messaggi"),
            QOption("Cura di me", value = "cura")
        ),
        multi = true
    ),
    QQuestion(
        "desideri", "Cosa ti piacerebbe, tra un mese?", "Scegli tutto ciò che vale per te",
        listOf(
            QOption("Iniziare senza tanta fatica"), QOption("Meno cose in sospeso"),
            QOption("Meno senso di colpa"), QOption("Più tempo per me"), QOption("Più energia")
        ),
        multi = true
    )
)

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

// ---------------- Lezioni del mattino ----------------

data class Lesson(val id: Int, val title: String, val body: String, val tryToday: String)

val lessons = listOf(
    Lesson(1, "Non sei pigra",
        "Rimandare non è pigrizia. È il modo in cui il cervello evita una sensazione spiacevole: noia, paura di sbagliare, confusione, stanchezza.\n\nIn pratica non rimandi il compito: rimandi l'emozione che il compito ti porta. Saperlo cambia tutto, perché smetti di combattere contro te stessa.",
        "Pensa a una cosa che rimandi e chiediti: quale sensazione sto evitando?"),
    Lesson(2, "Il compito è troppo grande",
        "Il cervello non sa iniziare \"sistemare casa\". Sa iniziare \"metti tre piatti nel lavandino\".\n\nPiù il primo passo è concreto e visibile, meno resistenza incontra. Un buon primo passo si può fotografare.",
        "Riscrivi una cosa che rimandi come un'azione fisica di meno di due minuti."),
    Lesson(3, "Il passo ridicolo",
        "Se il primo passo ti sembra ancora pesante, rimpiccioliscilo finché ti fa quasi ridere: \"apri il computer\", \"prendi la penna\", \"metti le scarpe\".\n\nIl punto non è finire. Il punto è partire: una volta in movimento, continuare costa molto meno.",
        "Fai un passo ridicolo adesso, su qualcosa che rimandi."),
    Lesson(4, "Cinque minuti, e basta",
        "Promettiti solo cinque minuti, con il permesso sincero di fermarti dopo.\n\nSpesso, una volta partita, continui. E se ti fermi, hai comunque iniziato: la prossima volta riprendere sarà più facile.",
        "Usa \"Inizia ora\" con 5 minuti su una cosa che rimandi."),
    Lesson(5, "Togli attrito",
        "Rendi facile ciò che vuoi fare e un po' più difficile ciò che ti distrae.\n\nScarpe da ginnastica vicino alla porta, il file già aperto, il telefono in un'altra stanza. Piccoli spostamenti, grandi differenze.",
        "Togli un ostacolo dalla cosa che rimandi, anche solo prepararne gli strumenti."),
    Lesson(6, "Decidi in dieci secondi",
        "Per le scelte piccole, la differenza tra le opzioni è minima, mentre il costo di pensarci è grande.\n\nDarti un tempo breve per decidere libera energia per fare.",
        "Per una scelta di oggi, conta fino a dieci e scegli. Senza tornare indietro."),
    Lesson(7, "La bozza brutta",
        "Concediti una prima versione volutamente imperfetta. Una mail scritta male, un elenco disordinato, un primo schizzo.\n\nSi può migliorare solo ciò che esiste. La bozza brutta toglie al perfezionismo il suo potere.",
        "Scrivi la \"versione brutta\" di qualcosa in cinque minuti."),
    Lesson(8, "Fatto è meglio di perfetto",
        "Il perfezionismo spesso è paura travestita da standard alto.\n\nPer molte cose, il \"abbastanza buono\" è esattamente ciò che serve. Tieni la cura per le poche cose che la meritano davvero.",
        "Scegli una cosa di oggi da fare al 70%, e consegnala così."),
    Lesson(9, "Mini-scadenze",
        "Se l'urgenza ti accende, creane di vicine e gentili: \"entro le 11 mando la mail\".\n\nFunziona ancora meglio se qualcuno lo sa: dichiararlo rende la scadenza reale.",
        "Dai a una cosa una scadenza entro oggi e dilla a qualcuno."),
    Lesson(10, "Fare in compagnia",
        "Lavorare accanto a qualcuno, anche in silenzio o in videochiamata, aiuta molti cervelli a restare sul compito.\n\nNon serve che ti aiuti: basta la sua presenza.",
        "Proponi a qualcuno venti minuti in cui ognuna fa le sue cose."),
    Lesson(11, "Una cosa sola",
        "Ogni cambio di attività costa energia e ti fa ripartire da zero.\n\nUna finestra, una scheda, un compito. Per pochi minuti, il resto può aspettare.",
        "Chiudi tutto tranne ciò che ti serve, per dieci minuti."),
    Lesson(12, "Ricompensa subito",
        "Il cervello impara da ciò che arriva subito, non da un premio tra un mese.\n\nAbbina un piccolo piacere a ogni inizio: una canzone, un tè, cinque minuti di sole.",
        "Scegli una ricompensa e concedila subito dopo il prossimo inizio."),
    Lesson(13, "La lista del dopo",
        "Quando arriva un pensiero non urgente mentre lavori, non inseguirlo: scrivilo nella lista del dopo.\n\nLa mente si calma quando sa che l'idea non andrà persa.",
        "Tieni oggi un foglio o una nota \"dopo\" vicino a te."),
    Lesson(14, "Il momento giusto non arriva",
        "Aspettare di sentirsi pronte rimanda all'infinito.\n\nLa motivazione, spesso, arriva dopo aver iniziato, non prima. È l'azione che crea la voglia, non il contrario.",
        "Inizia una cosa senza sentirti pronta. Solo due minuti."),
    Lesson(15, "Quando funzioni meglio",
        "Ognuno ha momenti della giornata in cui la mente è più lucida.\n\nMetti le cose difficili nei tuoi momenti buoni e quelle facili nei momenti stanchi.",
        "Osserva oggi in che momento ti senti più presente."),
    Lesson(16, "Energia, non solo tempo",
        "Un'ora da stanca non vale un'ora da riposata.\n\nOrganizzare la giornata guardando l'energia, e non solo l'orologio, evita di combattere contro te stessa.",
        "Per una cosa di domani, scegli l'orario in cui avrai più energia."),
    Lesson(17, "Dire di no",
        "Ogni sì a qualcosa è un no a qualcos'altro, spesso a te stessa e alle tue cose.\n\nUn no gentile protegge il tempo per ciò che conta.",
        "Prepara una frase: \"Mi piacerebbe, ma questa settimana non riesco.\""),
    Lesson(18, "Il no che prende tempo",
        "Non serve rispondere subito a ogni richiesta.\n\n\"Ci penso e ti dico domani\" ti dà lo spazio per scegliere invece di reagire, e per non riempire la settimana senza accorgertene.",
        "Alla prossima richiesta, rispondi: \"Ti faccio sapere\"."),
    Lesson(19, "Un'ora per te",
        "Se non lo metti in agenda, il tempo per te sparisce per primo.\n\nTrattalo come un appuntamento importante, perché lo è.",
        "Segna un'ora solo tua in questa settimana."),
    Lesson(20, "Non sei la tua lista",
        "Il tuo valore non dipende da quante cose spunti.\n\nSei degna di riposo anche nei giorni in cui non hai concluso nulla. Paradossalmente, sentirlo rende più facile iniziare.",
        "Dì a te stessa una cosa che apprezzi di te che non riguarda il fare."),
    Lesson(21, "Parlati come a un'amica",
        "La voce critica non ti spinge a fare: ti blocca e ti fa vergognare.\n\nLa gentilezza, invece, ti fa ripartire prima. Non è indulgenza: è strategia.",
        "Quando ti critichi oggi, chiediti: cosa direi a un'amica?"),
    Lesson(22, "Dopo uno stop si riparte",
        "Saltare un giorno non cancella nulla di ciò che hai costruito.\n\nConta tornare, non non fermarsi mai. Qui non ci sono serie da perdere.",
        "Riprendi una cosa interrotta, anche solo per due minuti."),
    Lesson(23, "Lo scroll non riposa",
        "Prendere il telefono \"un attimo\" sembra una pausa, ma raramente riposa: spesso stanca di più.\n\nScegli prima con cosa sostituirlo: alzarti, bere, guardare fuori.",
        "Quando prendi il telefono senza motivo, fai invece una pausa vera di un minuto."),
    Lesson(24, "Prepara il terreno la sera",
        "Prepara stasera ciò che serve per iniziare domani: il file aperto, la borsa pronta, la lista delle tre cose.\n\nAl mattino il primo passo sarà già fatto.",
        "Prima di dormire, prepara una cosa per domani."),
    Lesson(25, "Le tre cose del giorno",
        "Una lista di quindici cose paralizza. Scegline tre: le più importanti.\n\nIl resto è un bonus. Finire le tre ti lascia la sensazione di aver fatto, invece di quella di non aver finito.",
        "Scrivi solo tre cose per oggi."),
    Lesson(26, "Il corpo prima della mente",
        "A volte il blocco è nel corpo: stanchezza, fame, sete, tensione.\n\nPrima di forzare la testa, prova a sistemare il corpo.",
        "Prima di iniziare, alzati, bevi un bicchiere d'acqua e fai cinque respiri lenti."),
    Lesson(27, "Un suono per partire",
        "Musica senza parole o un suono costante aiutano molte menti a concentrarsi.\n\nUsato sempre allo stesso modo, diventa un segnale: quando parte, si inizia.",
        "Scegli una \"musica per iniziare\" e usala ogni volta."),
    Lesson(28, "Celebra i piccoli inizi",
        "Notare ciò che hai fatto rinforza l'abitudine molto più che rimproverarti per ciò che manca.\n\nOgni inizio conta, anche quello di cinque minuti.",
        "Stasera scrivi tre cose che hai iniziato oggi."),
    Lesson(29, "Capire il perché",
        "Nel tuo registro vedi quali motivi tornano più spesso quando rimandi.\n\nQuello è il punto su cui lavorare: se è \"troppo grande\", rimpicciolisci; se è \"paura di sbagliare\", bozza brutta.",
        "Apri il registro e guarda il motivo più frequente."),
    Lesson(30, "Il tuo metodo",
        "Ora sai cosa funziona per te. Non serve usare tutto: bastano tre tecniche che ti aiutano davvero.\n\nSono il tuo kit personale, da tirare fuori ogni volta che ti blocchi.",
        "Scrivi le tue tre tecniche preferite e tienile a portata di mano.")
)

fun lessonById(id: Int) = lessons.firstOrNull { it.id == id }

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

val Sun = Color(0xFFF3C969)
val Sage = Color(0xFF8DB8A3)

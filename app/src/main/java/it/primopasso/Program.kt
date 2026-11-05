package it.primopasso

/*
 * Programma ispirato ai protocolli di terapia cognitivo-comportamentale (TCC) per la procrastinazione:
 * psicoeducazione, auto-monitoraggio, valori e obiettivi, attivazione comportamentale e pianificazione,
 * tolleranza del disagio ed esposizione graduale, ristrutturazione cognitiva, esperimenti comportamentali,
 * controllo degli stimoli, autocompassione e prevenzione delle ricadute.
 */

data class Module(val n: Int, val title: String, val goal: String)

val modules = listOf(
    Module(1, "Capire il rimandare", "Come funziona il ciclo dell'evitamento e perché si ripete"),
    Module(2, "Osservare senza giudicare", "Riconoscere quando, dove e perché rimandi"),
    Module(3, "Ciò che conta per te", "Valori e obiettivi chiari che danno senso alla fatica"),
    Module(4, "Dal pensare al fare", "Scomporre, pianificare e decidere in anticipo"),
    Module(5, "Stare nel disagio", "Tollerare le emozioni scomode senza scappare"),
    Module(6, "I pensieri che frenano", "Riconoscere e riformulare i pensieri automatici"),
    Module(7, "Mettere alla prova le convinzioni", "Esperimenti per smontare le regole rigide"),
    Module(8, "Ambiente, gentilezza e continuità", "Proteggere i progressi nel tempo")
)

data class Lesson(
    val id: Int,
    val module: Int,
    val title: String,
    val body: String,
    val key: String,
    val tryToday: String,
    val worksheet: String? = null
)

val lessons = listOf(
    // ---------- Modulo 1 ----------
    Lesson(1, 1, "Il ciclo del rimandare",
        "Quasi ogni volta che rimandi succede la stessa sequenza. Pensi al compito e senti un disagio: noia, ansia, confusione. Per togliere il disagio fai altro: il telefono, una pulizia, un caffè. E arriva un sollievo immediato.\n\nQuel sollievo è il punto chiave. Il cervello impara che evitare funziona, perché toglie subito la sensazione spiacevole. In psicologia si chiama rinforzo: un comportamento che ti dà sollievo tende a ripetersi.\n\nPoi però il compito è ancora lì, spesso più urgente. Arrivano colpa e ansia, il disagio la volta dopo è più forte, e il ciclo ricomincia. Non sei debole: sei dentro un meccanismo che si autoalimenta. Per uscirne bisogna prima vederlo.",
        "Rimandare funziona nel breve periodo: ti toglie il disagio. Per questo si ripete.",
        "Compila la scheda \"Il tuo ciclo\" pensando a una cosa che rimandi da tempo.",
        "ciclo"),
    Lesson(2, 1, "Non è pigrizia: sono emozioni",
        "Le ricerche sulla procrastinazione mostrano che spesso è una strategia, imperfetta, per gestire le emozioni: un modo per sentirsi meglio subito, a spese del sé di domani.\n\nLe emozioni più comuni sono noia (\"è pesante\"), ansia e insicurezza (\"e se non ne sono capace?\"), confusione (\"non so da dove iniziare\"), frustrazione o risentimento (\"non dovrei essere io a farlo\").\n\nCapirlo cambia la domanda. Non \"perché sono così pigra?\", ma \"quale emozione sto cercando di evitare?\". Quando dai un nome all'emozione, puoi scegliere uno strumento adatto: rimpicciolire il compito se è confusione, una bozza brutta se è paura di sbagliare, una ricompensa se è noia.",
        "Non rimandi il compito: rimandi l'emozione che il compito ti porta.",
        "La prossima volta che rimandi, usa \"Inizia ora\" e scegli il motivo: è il tuo primo dato."),
    Lesson(3, 1, "Motivazione ed energia",
        "Molte persone aspettano di sentirsi motivate per iniziare. Ma nella maggior parte dei casi la motivazione arriva dopo aver iniziato, non prima: è l'azione che crea la voglia.\n\nLa motivazione dipende da alcuni fattori: quanto il compito ti sembra importante, quanto ti senti capace di farlo, quanto è lontana la ricompensa e quanto è forte la tentazione di fare altro. Quando il premio è lontano e le distrazioni sono vicine, vincono le distrazioni.\n\nConta anche l'energia: la stanchezza mentale è reale. Mettere i compiti difficili nei momenti in cui sei più lucida non è un trucco, è rispetto per come funzioni.",
        "Non aspettare la motivazione: falla nascere con un'azione piccola.",
        "Osserva oggi in quali ore ti senti più lucida e annotale nel tuo programma della giornata.",
        "programma"),
    // ---------- Modulo 2 ----------
    Lesson(4, 2, "Osservare è già cambiare",
        "Nella terapia cognitivo-comportamentale, il primo strumento è l'auto-osservazione. Annotare quando rimandi, cosa stavi facendo e cosa sentivi rende visibile ciò che di solito accade in automatico.\n\nL'importante è osservare come farebbe una ricercatrice curiosa, non come una giudice severa. Non stai raccogliendo prove contro di te: stai raccogliendo informazioni.\n\nIn Primo Passo il registro lo fai quasi senza accorgertene: ogni volta che usi \"Inizia ora\" e scegli un motivo, l'app memorizza ora, tipo di compito e ragione. Dopo una o due settimane emergono i tuoi schemi.",
        "Ciò che osservi con curiosità smette di comandarti.",
        "Questa settimana registra almeno tre inizi, scegliendo ogni volta il motivo per cui rimandavi."),
    Lesson(5, 2, "L'analisi A-B-C",
        "Uno strumento classico è l'analisi A-B-C. A sono gli antecedenti: cosa succede prima (ora, luogo, stanchezza, un pensiero). B è il comportamento: cosa fai invece del compito. C sono le conseguenze: cosa ottieni subito e cosa succede dopo.\n\nEsempio: sono le 21, sei stanca, pensi alla mail per la banca (A). Apri i social \"per un minuto\" (B). Subito ti senti sollevata, ma a letto arriva l'ansia e il giorno dopo la mail è ancora lì (C).\n\nQuando vedi l'A-B-C, trovi i punti dove intervenire: cambiare l'antecedente (farlo al mattino), rendere più difficile il comportamento di fuga (telefono in un'altra stanza), rendere più vicina una conseguenza positiva.",
        "Ogni rimando ha un prima, un durante e un dopo: lì ci sono le leve per cambiare.",
        "Compila un'analisi A-B-C sull'ultima volta che hai rimandato.",
        "abc"),
    Lesson(6, 2, "I tuoi schemi ricorrenti",
        "Dopo qualche osservazione, iniziano a vedersi gli schemi: certi orari, certi tipi di compito, certe emozioni tornano sempre.\n\nForse rimandi soprattutto le cose burocratiche, la sera, quando sei stanca. Forse i compiti in cui temi il giudizio altrui. Forse quando hai troppe cose e non sai scegliere.\n\nConoscere i tuoi \"punti caldi\" ti permette di prepararti in anticipo: un piano per la sera, un aiuto per le carte, una bozza brutta per ciò che verrà giudicato. Non devi cambiare tutto: basta intervenire dove il rimandare nasce più spesso.",
        "Non serve cambiare tutto: basta intervenire nei tuoi punti caldi.",
        "Apri il Registro e guarda il motivo e il momento più frequenti. Scrivi un'analisi A-B-C su quel momento.",
        "abc"),
    // ---------- Modulo 3 ----------
    Lesson(7, 3, "I valori: la tua bussola",
        "I valori sono le direzioni che rendono la tua vita significativa: famiglia, salute, libertà, crescita, cura, creatività. Non si raggiungono mai del tutto, come una direzione sulla bussola.\n\nMolti compiti rimandati sono noiosi in sé, ma sono collegati a un valore. Pagare una bolletta non è emozionante, ma è legato alla tranquillità e alla sicurezza. Fare una telefonata difficile può essere legato alla sincerità o alla cura di una relazione.\n\nQuando colleghi un compito a ciò che conta per te, la fatica cambia significato: non è più \"devo\", ma \"scelgo, perché mi importa\".",
        "Un compito noioso pesa meno quando sai a quale valore ti avvicina.",
        "Compila la scheda \"I tuoi valori\" e collega un compito rimandato a ciò che conta per te.",
        "valori"),
    Lesson(8, 3, "Obiettivi chiari e gentili",
        "Un obiettivo vago come \"mettermi in pari\" non si può iniziare: il cervello non sa da dove partire e non sa quando hai finito.\n\nUn obiettivo utile è concreto (cosa farai), osservabile (come saprai che è fatto), realistico rispetto alla tua energia e collegato a un momento preciso. Per esempio: \"Entro venerdì invio i tre documenti al commercialista, un documento al giorno, dopo pranzo.\"\n\nGentile non vuol dire facile: vuol dire che tiene conto della tua vita reale, della stanchezza e degli imprevisti.",
        "Se non sai come appare \"fatto\", non saprai mai da dove iniziare.",
        "Trasforma un'intenzione vaga in un obiettivo chiaro con la scheda \"Un obiettivo gentile\".",
        "obiettivo"),
    Lesson(9, 3, "Dire di no per dire sì",
        "Il tempo e l'energia sono limitati. Ogni sì detto controvoglia toglie spazio alle cose che contano per te, e spesso proprio a quelle che rimandi.\n\nDire no in modo gentile e chiaro è un'abilità che si allena: \"Grazie di avermelo chiesto, questa settimana non riesco\". Non servono lunghe giustificazioni. Puoi anche prendere tempo: \"Ci penso e ti dico domani\".\n\nProteggere il tempo non è egoismo: è il modo per esserci davvero, anche per gli altri, senza arrivare svuotata.",
        "Ogni no gentile protegge un sì importante.",
        "Questa settimana prova a dire un no, oppure a rispondere \"ti faccio sapere\" a una richiesta."),
    // ---------- Modulo 4 ----------
    Lesson(10, 4, "Rendere il compito piccolo",
        "Nella TCC si usa l'assegnazione graduale dei compiti: un compito grande e minaccioso viene diviso in passi così piccoli da sembrare facili.\n\nRegola pratica: il primo passo deve essere un'azione fisica, visibile, che richiede meno di cinque minuti. Non \"preparare la presentazione\", ma \"aprire un file nuovo e scrivere il titolo\".\n\nOgni passo completato produce un piccolo senso di riuscita, che rende più facile il passo successivo. Non stai \"barando\": stai lavorando con il tuo cervello invece che contro.",
        "Un compito si inizia solo quando il primo passo è più piccolo della tua resistenza.",
        "Scomponi un compito grande con la scheda \"Scala dei passi\" e fai subito il primo.",
        "scala"),
    Lesson(11, 4, "Il piano se-allora",
        "Le intenzioni generiche (\"lo farò domani\") si perdono facilmente. Molte ricerche mostrano che è più probabile agire se decidi in anticipo quando, dove e come, con una frase del tipo: \"Se sono le 14 e ho finito il pranzo, allora apro il documento alla scrivania.\"\n\nIl piano se-allora collega l'azione a un segnale preciso: quando il segnale arriva, non devi decidere, devi solo seguire il piano.\n\nPuoi anche preparare piani per gli ostacoli: \"Se mi viene voglia di prendere il telefono, allora lo metto in un'altra stanza e faccio due minuti.\"",
        "Decidi una volta, in anticipo: quando arriva il momento, segui il piano.",
        "Scrivi il tuo primo piano se-allora per una cosa che rimandi.",
        "seallora"),
    Lesson(12, 4, "Pianificare con realismo",
        "Tendiamo tutti a sottovalutare il tempo che serve: è un errore così comune da avere un nome, la fallacia della pianificazione. Poi, quando la lista non torna, ci sentiamo in colpa e rimandiamo ancora.\n\nAlcune regole aiutano: scegli al massimo tre cose importanti al giorno; stima il tempo e aggiungi la metà; lavora a blocchi brevi con pause vere; metti i compiti difficili nelle tue ore di energia.\n\nUna giornata realistica che riesci a completare vale più di una giornata perfetta sulla carta.",
        "Meglio tre cose fatte che quindici scritte.",
        "Programma domani con la scheda \"Programma la giornata\".",
        "programma"),
    // ---------- Modulo 5 ----------
    Lesson(13, 5, "Il disagio non è un segnale di stop",
        "Quando pensi a un compito che eviti, il corpo e la mente producono disagio: tensione, pesantezza, un \"non mi va\". Spesso lo interpretiamo come un segnale per fermarci.\n\nMa il disagio non è pericoloso: è solo spiacevole. Le emozioni funzionano a ondate: salgono, raggiungono un picco e poi scendono, spesso più in fretta di quanto pensiamo, soprattutto se iniziamo ad agire.\n\nIl cambiamento non arriva quando il disagio sparisce, ma quando impari che puoi iniziare anche con il disagio presente.",
        "Puoi iniziare anche senza sentirti pronta: il disagio può venire con te.",
        "Quando senti il \"non mi va\", dai un voto da 0 a 10 al disagio, fai due minuti e ridai il voto.",
        "impulso"),
    Lesson(14, 5, "Surfare l'impulso",
        "Quando arriva l'impulso di scappare dal compito, prova a non obbedire subito. Osservalo come un'onda: dove lo senti nel corpo? Com'è? Cresce, si stabilizza, cala?\n\nI passi sono semplici: noti l'impulso, gli dai un nome (\"ecco la voglia di scappare\"), respiri lentamente, resti con la sensazione per uno o due minuti senza agire, poi scegli cosa fare.\n\nOgni volta che \"surfi\" un impulso invece di seguirlo, insegni al cervello che il disagio passa anche senza fuggire. È un allenamento: all'inizio è difficile, poi diventa più naturale.",
        "Un impulso è un'onda: se non la segui, passa.",
        "Usa la scheda \"Surfare l'impulso\" la prossima volta che vuoi scappare da un compito.",
        "impulso"),
    Lesson(15, 5, "Avvicinarsi un passo alla volta",
        "Più eviti una cosa, più diventa minacciosa. È il meccanismo dell'evitamento: protegge nel breve periodo e fa crescere la paura nel lungo.\n\nL'esposizione graduale fa il contrario: ti avvicini alle cose che eviti partendo dalla meno difficile e salendo piano. Prima apri la cartella delle carte, poi leggi un documento, poi ne compili uno.\n\nRipetendo, il disagio associato a quel compito si riduce. Non devi affrontare subito la cosa più difficile: basta salire di un gradino.",
        "L'evitamento fa crescere la paura; avvicinarsi piano la fa diminuire.",
        "Costruisci la tua \"Scala del disagio\" e affronta il primo gradino.",
        "esposizione"),
    // ---------- Modulo 6 ----------
    Lesson(16, 6, "I pensieri automatici",
        "Tra una situazione e ciò che senti c'è quasi sempre un pensiero, così veloce da non notarlo. Davanti a un compito: \"Non ce la farò mai\", \"Dovrei già averlo fatto\", \"Sarà orribile\".\n\nQuesti pensieri automatici sembrano verità, ma sono interpretazioni. Influenzano emozioni (ansia, colpa) e comportamenti (evitare).\n\nIl primo passo è catturarli: quando senti un calo d'umore o la voglia di scappare, chiediti \"cosa mi sto dicendo in questo momento?\". Scriverlo crea distanza: non sei il pensiero, sei chi lo osserva.",
        "Non è la situazione a bloccarti, ma ciò che ti dici su di essa.",
        "Oggi cattura un pensiero automatico e scrivilo nel \"Registro dei pensieri\".",
        "pensieri"),
    Lesson(17, 6, "Le trappole del pensiero",
        "Alcuni modi di pensare sono così comuni che hanno un nome. Tutto o niente: \"Se non lo faccio perfettamente, è inutile\". Catastrofe: \"Se sbaglio la mail, sarà un disastro\". I \"devo\": \"Dovrei riuscirci senza fatica\".\n\nEtichetta: \"Sono pigra\". Lettura del pensiero: \"Penseranno che sono incapace\". Previsione negativa: \"Tanto andrà male\". Confronto: \"Gli altri lo fanno in metà del tempo\".\n\nRiconoscere la trappola non la fa sparire, ma le toglie forza: diventa \"ah, ecco il mio pensiero tutto o niente\".",
        "Dare un nome alla trappola ti aiuta a non caderci.",
        "Riprendi un pensiero che ti blocca e individua le trappole nel \"Registro dei pensieri\".",
        "pensieri"),
    Lesson(18, 6, "Il registro dei pensieri",
        "Il registro dei pensieri è uno degli strumenti centrali della TCC. Scrivi la situazione, il pensiero automatico, l'emozione e la sua intensità. Poi cerchi le prove a favore e quelle contro, come farebbe un'osservatrice imparziale.\n\nInfine costruisci un pensiero alternativo, non per forza positivo ma più equilibrato e realistico: \"Non so ancora come verrà, ma posso iniziare con una prima bozza\".\n\nAlla fine rivaluti l'emozione. Spesso scende di qualche punto: abbastanza per fare il primo passo.",
        "Non serve pensare positivo: serve pensare in modo più equilibrato.",
        "Compila un registro dei pensieri completo e confronta l'intensità prima e dopo.",
        "pensieri"),
    // ---------- Modulo 7 ----------
    Lesson(19, 7, "Le convinzioni che mantengono il rimandare",
        "Sotto i pensieri automatici ci sono spesso convinzioni più profonde, regole su come dovremmo lavorare: \"Devo sentirmi pronta per iniziare\", \"Lavoro meglio sotto pressione\", \"Se non è perfetto, non vale\", \"Prima devo avere tutto chiaro\".\n\nSembrano ragionevoli, ma tengono in piedi il ciclo del rimandare. Se aspetti di sentirti pronta, aspetterai a lungo. Se credi di lavorare meglio all'ultimo, non sperimenterai mai l'alternativa.\n\nLa buona notizia è che le convinzioni si possono mettere alla prova, come ipotesi.",
        "Le regole rigide sembrano protezioni, ma spesso sono gabbie.",
        "Scegli la tua convinzione più forte: nella prossima lezione la metterai alla prova."),
    Lesson(20, 7, "L'esperimento comportamentale",
        "Invece di discutere con una convinzione, la metti alla prova come una scienziata. Scrivi la convinzione e quanto ci credi da 0 a 10, poi progetti un piccolo esperimento.\n\nEsempio: \"Lavoro meglio sotto pressione\". Esperimento: fare una parte del compito tre giorni prima della scadenza e osservare qualità, stress e tempo impiegato. Prima scrivi cosa prevedi, poi cosa è successo davvero.\n\nAlla fine rivaluti quanto ci credi. Anche un piccolo calo è una scoperta: la regola non è più una verità assoluta.",
        "Una convinzione è un'ipotesi: puoi verificarla con i fatti.",
        "Progetta il tuo primo \"Esperimento comportamentale\".",
        "esperimento"),
    Lesson(21, 7, "Perfezionismo e paura del giudizio",
        "Il perfezionismo è uno dei motori più potenti del rimandare. Se il risultato deve essere perfetto, iniziare fa paura, perché ogni bozza mostra che non lo è.\n\nSpesso dietro c'è la paura del giudizio: degli altri o di noi stesse. Rimandare protegge, per un po', dall'essere valutate.\n\nUn esperimento utile: consegnare qualcosa \"all'80%\" e osservare cosa succede davvero. Quasi sempre le conseguenze sono molto più lievi del previsto, e il tempo risparmiato è enorme.",
        "Abbastanza buono, consegnato, vale più di perfetto, rimandato.",
        "Fai un esperimento: consegna una cosa all'80% e annota cosa succede davvero.",
        "esperimento"),
    // ---------- Modulo 8 ----------
    Lesson(22, 8, "L'ambiente lavora per te",
        "Il comportamento dipende molto dall'ambiente. Il telefono a portata di mano, le notifiche e le schede aperte sono inviti continui a fare altro.\n\nIl controllo degli stimoli funziona in due direzioni: togliere i segnali che portano alla distrazione (telefono in un'altra stanza, notifiche spente) e aggiungere quelli che portano al compito (il documento già aperto, un posto dedicato, una musica che usi solo per lavorare).\n\nNon è una questione di forza di volontà: è rendere la strada giusta la più facile.",
        "Non contare sulla forza di volontà: rendi facile la strada giusta.",
        "Organizza il tuo spazio con la scheda \"Il tuo spazio senza distrazioni\".",
        "ambiente"),
    Lesson(23, 8, "Gentilezza dopo un passo falso",
        "Dopo aver rimandato, molte persone si criticano duramente, pensando che serva a fare meglio. Le ricerche indicano il contrario: l'autocritica aumenta il disagio associato al compito e quindi la voglia di evitarlo.\n\nIn uno studio, studentesse e studenti che si erano perdonati per aver rimandato lo studio del primo esame hanno rimandato meno prima di quello successivo.\n\nAutocompassione non significa giustificarsi: significa riconoscere l'errore, ricordare che capita a tutti e parlarsi come si parlerebbe a un'amica, per poi ripartire.",
        "La gentilezza verso te stessa non ti rende pigra: ti fa ripartire prima.",
        "Scrivi una \"Lettera gentile dopo un passo falso\".",
        "autocompassione"),
    Lesson(24, 8, "Mantenere i progressi",
        "Dopo qualche settimana avrai strumenti e abitudini nuove. Arriveranno comunque giorni in cui rimandi: è normale, non è una ricaduta definitiva.\n\nLa differenza la fa avere un piano: conoscere i segnali che stai scivolando nel vecchio ciclo (più telefono, più rimuginio, lista che cresce), sapere quali strumenti ti aiutano di più e a chi puoi scrivere.\n\nRivedi ogni tanto il tuo percorso e ricomincia da un modulo quando serve. Un passo falso non cancella ciò che hai imparato.",
        "Un giorno storto è un dato, non un fallimento.",
        "Scrivi il tuo \"Piano per i momenti difficili\".",
        "ricadute")
)

fun lessonById(id: Int) = lessons.firstOrNull { it.id == id }
fun moduleById(n: Int) = modules.firstOrNull { it.n == n }

// ---------------- Schede di lavoro ----------------

sealed class WField(val key: String, val prompt: String) {
    class Text(key: String, prompt: String, val hint: String = "", val examples: List<String> = emptyList(), val lines: Int = 2) : WField(key, prompt)
    class Scale(key: String, prompt: String, val low: String, val high: String) : WField(key, prompt)
    class Choice(key: String, prompt: String, val options: List<String>, val multi: Boolean = true) : WField(key, prompt)
    class Lines(key: String, prompt: String, val count: Int, val hints: List<String>) : WField(key, prompt)
    class Info(key: String, prompt: String, val text: String) : WField(key, prompt)
}

data class Worksheet(
    val id: String,
    val module: Int,
    val title: String,
    val subtitle: String,
    val intro: String,
    val fields: List<WField>,
    val stepKey: String? = null
)

private val emotionsList = listOf("Noia", "Ansia", "Insicurezza", "Confusione", "Frustrazione", "Colpa", "Vergogna", "Stanchezza", "Risentimento")

val worksheets = listOf(
    Worksheet("ciclo", 1, "Il tuo ciclo", "Vedi il meccanismo che ti tiene ferma",
        "Pensa a un compito che rimandi da tempo. Ripercorriamo insieme il ciclo: ciò che senti, cosa fai invece e cosa ottieni.",
        listOf(
            WField.Text("compito", "Quale compito rimandi?", "Es. Telefonare all'assicurazione"),
            WField.Choice("emozioni", "Quando ci pensi, cosa senti?", emotionsList),
            WField.Scale("intensita", "Quanto è forte questo disagio?", "lieve", "fortissimo"),
            WField.Text("invece", "Cosa fai invece?", "Es. Controllo il telefono, riordino la cucina", listOf("Scorro il telefono", "Faccio altre cose", "Pulisco", "Mangio qualcosa", "Mi sdraio")),
            WField.Text("sollievo", "Cosa ti dà, subito, non farlo?", "Es. Mi sento più leggera per un po'"),
            WField.Text("dopo", "E dopo, cosa succede?", "Es. La sera mi sento in colpa e il compito è più urgente"),
            WField.Info("visto", "Ecco il tuo ciclo", "Il sollievo immediato rinforza l'evitamento, le conseguenze arrivano dopo. Vederlo è il primo passo per interromperlo: basta cambiare un anello, per esempio rendere il compito più piccolo."),
            WField.Text("anello", "Quale anello potresti cambiare la prossima volta?", "Es. Rendere il compito più piccolo, togliere il telefono", listOf("Rendere il compito più piccolo", "Togliere il telefono", "Farlo al mattino", "Chiedere aiuto"))
        ), stepKey = "anello"),
    Worksheet("abc", 2, "Analisi A-B-C", "Prima, durante e dopo un rimando",
        "Ripensa all'ultima volta che hai rimandato. Osserva con curiosità, senza giudicarti.",
        listOf(
            WField.Text("a_situazione", "A · Cosa stava succedendo prima?", "Ora, luogo, con chi eri, cosa stavi facendo"),
            WField.Choice("a_stato", "A · Come stavi?", listOf("Stanca", "Agitata", "Annoiata", "Sopraffatta", "Affamata", "Distratta", "Tranquilla")),
            WField.Text("a_pensiero", "A · Cosa ti sei detta?", "Es. Lo faccio dopo, adesso non ho la testa"),
            WField.Text("b", "B · Cosa hai fatto invece?", "Es. Ho aperto i social"),
            WField.Text("c_subito", "C · Cosa hai ottenuto subito?", "Es. Sollievo, distrazione"),
            WField.Text("c_dopo", "C · Cosa è successo dopo?", "Es. Ansia la sera, compito rimasto lì"),
            WField.Text("leva", "Dove potresti intervenire la prossima volta?", "Cambiare la A, la B o rendere più vicina una conseguenza positiva", listOf("Farlo in un altro momento", "Telefono lontano", "Una ricompensa subito dopo", "Un primo passo più piccolo"))
        )),
    Worksheet("valori", 3, "I tuoi valori", "Collega i compiti a ciò che conta",
        "I valori sono direzioni, non traguardi. Scoprire quali sono i tuoi rende più facile scegliere e iniziare.",
        listOf(
            WField.Choice("aree", "Quali aree della vita contano di più per te?", listOf("Famiglia", "Relazioni", "Salute", "Lavoro", "Crescita", "Creatività", "Libertà", "Tranquillità economica", "Comunità", "Spiritualità")),
            WField.Text("perche", "Scegline una: perché è importante per te?", "Es. La tranquillità economica mi fa sentire libera e serena"),
            WField.Text("persona", "Che persona vuoi essere in quest'area?", "Es. Una persona che si prende cura delle sue cose con calma"),
            WField.Text("compito", "Quale compito rimandato è collegato a questo valore?", "Es. Sistemare le bollette"),
            WField.Text("passo", "Qual è un passo piccolo che ti avvicina a questo valore, oggi?", "Es. Aprire l'app della banca e guardare le scadenze")
        ), stepKey = "passo"),
    Worksheet("obiettivo", 3, "Un obiettivo gentile", "Da intenzione vaga a obiettivo chiaro",
        "Un buon obiettivo è concreto, osservabile, realistico e collegato a un momento.",
        listOf(
            WField.Text("vago", "Cosa vorresti fare, detto come ti viene?", "Es. Mettermi in pari con le carte"),
            WField.Text("concreto", "Cosa farai, concretamente?", "Es. Archiviare le bollette del 2026"),
            WField.Text("fatto", "Come saprai che è fatto?", "Es. Tutte in una cartella, divise per mese"),
            WField.Text("quando", "Entro quando, e in quali momenti?", "Es. Entro domenica, 15 minuti dopo cena"),
            WField.Text("ostacolo", "Cosa potrebbe metterti i bastoni tra le ruote?", "Es. La stanchezza la sera"),
            WField.Text("primo", "Qual è il primo passo, da meno di 5 minuti?", "Es. Prendere la scatola delle carte e metterla sul tavolo")
        ), stepKey = "primo"),
    Worksheet("scala", 4, "Scala dei passi", "Scomponi un compito grande",
        "Dividi il compito in passi sempre più piccoli. Il primo deve essere un'azione visibile da meno di cinque minuti.",
        listOf(
            WField.Text("compito", "Qual è il compito grande?", "Es. Preparare la presentazione per lunedì"),
            WField.Lines("passi", "Scrivi i passi, dal primo all'ultimo", 5, listOf("1. Aprire un file nuovo e scrivere il titolo", "2. Elencare 3 punti chiave", "3. Una slide per punto", "4. Aggiungere immagini", "5. Rileggere una volta")),
            WField.Text("oggi", "Quale passo fai oggi?", "Di solito il primo", lines = 1)
        ), stepKey = "oggi"),
    Worksheet("seallora", 4, "Piano se-allora", "Decidi in anticipo, poi segui il piano",
        "Colleghi un'azione piccola a un segnale preciso. Quando arriva il segnale, non devi decidere: segui il piano.",
        listOf(
            WField.Text("compito", "Per quale compito?", "Es. Fare la dichiarazione dei redditi"),
            WField.Text("se", "SE… (quando, dove, dopo cosa)", "Es. Se è martedì e ho finito il caffè del mattino", listOf("Se ho finito il caffè del mattino", "Se rientro a casa", "Se è l'ora di pranzo", "Se ho appena chiuso il computer")),
            WField.Text("allora", "ALLORA… (un'azione piccola e concreta)", "Es. Allora apro il sito e cerco il mio codice"),
            WField.Text("ostacolo", "Quale ostacolo potrebbe arrivare?", "Es. La voglia di prendere il telefono"),
            WField.Text("piano_b", "SE arriva l'ostacolo, ALLORA…", "Es. Allora metto il telefono in un'altra stanza e faccio due minuti")
        ), stepKey = "allora"),
    Worksheet("programma", 4, "Programma la giornata", "Tre cose, con realismo",
        "Una giornata realistica che riesci a completare vale più di una perfetta sulla carta.",
        listOf(
            WField.Choice("energia", "In quali momenti di solito hai più energia?", listOf("Mattina presto", "Tarda mattina", "Primo pomeriggio", "Tardo pomeriggio", "Sera"), multi = true),
            WField.Lines("tre", "Le tre cose importanti di domani", 3, listOf("1. La più importante", "2.", "3.")),
            WField.Text("tempi", "Quanto tempo stimi per ognuna? Ora aggiungi la metà.", "Es. 30 min → 45 min"),
            WField.Text("quando", "In quali momenti le farai?", "Metti la più difficile nelle tue ore di energia"),
            WField.Text("pause", "Quando farai le pause?", "Es. 5 minuti ogni 25")
        )),
    Worksheet("impulso", 5, "Surfare l'impulso", "Resta con il disagio, senza scappare",
        "Quando arriva la voglia di fuggire dal compito, la osserviamo come un'onda.",
        listOf(
            WField.Text("compito", "Da quale compito vorresti scappare?", "Es. Scrivere il report"),
            WField.Scale("prima", "Quanto è forte l'impulso di scappare, adesso?", "debole", "fortissimo"),
            WField.Choice("corpo", "Dove lo senti nel corpo?", listOf("Petto", "Stomaco", "Gola", "Spalle", "Testa", "Mani", "Non lo so")),
            WField.Info("onda", "Osserva l'onda per due minuti", "Respira lentamente. Nota la sensazione, dalle un nome: \"ecco la voglia di scappare\". Non devi fare nulla: solo osservare come cambia. Quando ti senti pronta, vai avanti."),
            WField.Scale("dopo", "E adesso, quanto è forte?", "debole", "fortissimo"),
            WField.Text("notato", "Cosa hai notato?", "Es. È salita e poi è scesa un po'"),
            WField.Text("passo", "Qual è il primo passo che puoi fare adesso?", "Es. Aprire il documento")
        ), stepKey = "passo"),
    Worksheet("esposizione", 5, "Scala del disagio", "Avvicinati un gradino alla volta",
        "Elenca i compiti che eviti, dal meno al più difficile. Poi parti dal primo gradino.",
        listOf(
            WField.Text("area", "In quale area eviti di più?", "Es. Carte e burocrazia"),
            WField.Lines("gradini", "Dal gradino più facile al più difficile", 5, listOf("1. Aprire la cartella", "2. Leggere un documento", "3. Fare una telefonata breve", "4. Compilare un modulo", "5. Andare allo sportello")),
            WField.Scale("disagio", "Quanto disagio ti dà il primo gradino?", "nessuno", "moltissimo"),
            WField.Text("quando", "Quando affronterai il primo gradino?", "Es. Domani dopo colazione")
        )),
    Worksheet("pensieri", 6, "Registro dei pensieri", "Dal pensiero automatico a uno più equilibrato",
        "Lo strumento centrale della terapia cognitivo-comportamentale. Prenditi qualche minuto, con calma.",
        listOf(
            WField.Text("situazione", "La situazione", "Es. Devo mandare il preventivo al cliente"),
            WField.Text("pensiero", "Il pensiero automatico", "Cosa ti sei detta?", thoughtExamples),
            WField.Choice("emozioni", "Le emozioni", emotionsList),
            WField.Scale("prima", "Quanto è forte l'emozione?", "lieve", "fortissima"),
            WField.Choice("trappole", "Quali trappole del pensiero riconosci?", thoughtTypes.map { it.name }),
            WField.Text("pro", "Quali prove ci sono che il pensiero sia vero?", "Solo fatti, non sensazioni"),
            WField.Text("contro", "Quali prove ci sono che non sia del tutto vero?", "Es. L'ultima volta è andata bene"),
            WField.Text("alternativo", "Un pensiero più equilibrato e realistico", "Es. Non so come verrà, ma posso iniziare con una bozza", lines = 3),
            WField.Scale("dopo", "E adesso, quanto è forte l'emozione?", "lieve", "fortissima"),
            WField.Text("passo", "Un passo piccolo da fare adesso", "Es. Aprire il file del preventivo")
        ), stepKey = "passo"),
    Worksheet("esperimento", 7, "Esperimento comportamentale", "Metti alla prova una convinzione",
        "Tratta la convinzione come un'ipotesi da verificare. Prima prevedi, poi osservi cosa succede davvero.",
        listOf(
            WField.Text("convinzione", "La convinzione da mettere alla prova", "", listOf(
                "Devo sentirmi motivata per iniziare", "Lavoro meglio sotto pressione", "Se non è perfetto non vale",
                "Prima devo avere tutto chiaro", "Se chiedo aiuto penseranno che sono incapace", "Non ho abbastanza tempo per farlo bene")),
            WField.Scale("credo_prima", "Quanto ci credi, da 0 a 10?", "per niente", "totalmente"),
            WField.Text("esperimento", "Quale esperimento puoi fare?", "Es. Iniziare per 10 minuti senza sentirmi pronta"),
            WField.Text("previsione", "Cosa prevedi che succederà?", "Es. Non concluderò niente e mi sentirò peggio"),
            WField.Info("prova", "Ora fai l'esperimento", "Puoi farlo subito o più tardi. Quando l'hai fatto, torna qui per scrivere cosa è successo davvero: la scheda resta salvata."),
            WField.Text("risultato", "Cosa è successo davvero?", "Es. Dopo 3 minuti ero dentro, ho lavorato 20 minuti"),
            WField.Text("imparato", "Cosa hai imparato?", "Es. La motivazione è arrivata dopo"),
            WField.Scale("credo_dopo", "Quanto ci credi adesso?", "per niente", "totalmente")
        )),
    Worksheet("ambiente", 8, "Il tuo spazio senza distrazioni", "Rendi facile la strada giusta",
        "Il controllo degli stimoli: togli i segnali che portano alla distrazione, aggiungi quelli che portano al compito.",
        listOf(
            WField.Choice("distrazioni", "Cosa ti distrae di più?", listOf("Telefono", "Notifiche", "Social", "Email", "Rumori", "Disordine", "Persone che interrompono", "Fame o sete", "Televisione")),
            WField.Text("togli", "Cosa puoi togliere o allontanare?", "Es. Telefono in un'altra stanza, notifiche spente per 30 minuti"),
            WField.Text("aggiungi", "Cosa puoi aggiungere per facilitare il compito?", "Es. Documento già aperto, acqua sul tavolo, una lista"),
            WField.Text("luogo", "Qual è il tuo posto per i compiti difficili?", "Es. Il tavolo della cucina, al mattino"),
            WField.Text("segnale", "Quale segnale di inizio userai?", "Es. Una musica che uso solo per lavorare")
        )),
    Worksheet("autocompassione", 8, "Lettera gentile dopo un passo falso", "Parlati come a un'amica",
        "L'autocritica aumenta il disagio e la voglia di evitare. La gentilezza ti aiuta a ripartire.",
        listOf(
            WField.Text("successo", "Cosa è successo?", "Es. Ho rimandato di nuovo la consegna"),
            WField.Text("critica", "Cosa ti dice la voce critica?", "Es. Sei la solita, non cambierai mai"),
            WField.Info("comune", "Non sei sola", "Rimandare è un'esperienza umana comune: capita a moltissime persone, anche molto capaci. Non dice chi sei: dice che stavi gestendo qualcosa di difficile."),
            WField.Text("amica", "Cosa diresti a un'amica nella stessa situazione?", "Es. È stata una settimana pesante, puoi ripartire da un passo piccolo", lines = 3),
            WField.Text("frase", "Una frase gentile da dire a te stessa", "Es. Ho sbagliato, e posso ricominciare adesso"),
            WField.Text("prossima", "Cosa farai di diverso la prossima volta?", "Es. Dividere il lavoro in tre giorni")
        ), stepKey = "prossima"),
    Worksheet("ricadute", 8, "Piano per i momenti difficili", "Proteggi i tuoi progressi",
        "I giorni in cui rimandi arriveranno: con un piano ripartire è più facile.",
        listOf(
            WField.Choice("segnali", "Quali segnali ti dicono che stai tornando nel vecchio ciclo?", listOf("Più telefono", "Rimugino di più", "La lista cresce", "Dormo male", "Evito di guardare le scadenze", "Mi critico di più")),
            WField.Choice("strumenti", "Quali strumenti ti hanno aiutata di più?", listOf("Scala dei passi", "Piano se-allora", "Registro dei pensieri", "Surfare l'impulso", "Bozza brutta", "Timer di 5 minuti", "Compagna di inizio", "Gentilezza verso di me")),
            WField.Text("persona", "A chi puoi scrivere quando ti blocchi?", "Es. Mia sorella, la mia amica Sara"),
            WField.Text("frase", "Una frase da ricordare nei momenti difficili", "Es. Un giorno storto è un dato, non un fallimento")
        ))
)

fun worksheetById(id: String?) = worksheets.firstOrNull { it.id == id }

// ---------------- Termometro settimanale ----------------

/** Indicatori di auto-osservazione (0-10), non un test clinico. */
val thermometer = listOf(
    "rimandato" to "Quanto hai rimandato cose importanti?",
    "disagio" to "Quanto disagio provavi pensando ai tuoi compiti?",
    "critica" to "Quanto ti sei criticata?",
    "capace" to "Quanto ti sei sentita capace di iniziare?"
)

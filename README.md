# yarepository
Ya Another Repository con un bellissimo YaFilter tutto da provare


In questo documento sintetico si vuole descrivere l'utilizzo di un pojo java finalizzato alla composizione dinamica di query HQL hibernate (usando i Criteria), senza però usare una riga di codice di hibernate.

L'obiettivo del pojo e' raccogliere le clausole definite al suo interno, serializzare in modo comodo i risultati di clausola, e infine far generare al repository la Query hibernate rispettando al meglio la sintassi HQL, da immettere nell'entitymanager.

E' un filtro studiato per ottenere risultati da una base dati mappata su hibernate, in base ad arbitrarie e complesse query, il più possibili aderenti alle esigenze specifiche, ma usando un autogeneratore standard e riusabile, oltre che tutto incapsulato in un pojo, e usato solo dal repository.



## Il risultato è il vantaggio di comporre ricerche hibernate senza usare hibernate.



E' richiesta una minima conoscenza del funzionamento di hibernate e delle piu comuni regole di composizione di una query a partire da criteria. Negli anni queste interfacce cambiano , anche profondamente nel tempo, per decisioni implementative di chi sviluppa hibernate stesso. Questo filter e' progettato per aggiornarsi facilmente alle nuove specifiche delle versioni successive di hibernate, con minime modifiche al generatore query. 

La prima release funzionante di un repository progettato attorno a questo Filter (ai tempi BOSearcher), risale al 2007, su piattaforma dotnet, framework 4. Successivamente fu riusato in ambito java (dopo porting) nel 2010 fino al 2014 su vari progetti in Java, tutt'ora utilizzati. In tutti i casi l'adozione di questo filtro ha permesso tempi di sviluppo efficaci e stabili per progetti arbitrariamente complessi.

Il pojo è intuitivo e ha bisogno di poche accortezze, che, nel tempo, sono state inquadrate fin dal momento in cui si istanzia il pojo.



La regola generale è che una istanza di un FilterSearch deve essere sempre associato a una entita hibernate su cui eseguire la ricerca, altrimenti errore null exception.

La maggior parte delle clausole sono facili inserimenti di coppie field/valore come ad esempio nelle uguaglianze, la lista di campi che non devono essere nulli o viceversa ecc... ecc... . Tuttavia alcune clausole, per la loro stretta natura di requisiti input, sono organizzati con pojo filter specifici per quel tipo di clausola.

Tutte le singole clausole sono gestite dal filter come lista di occorrenze, quindi , al netto di anomalie, le clausole sono sempre gestite in multi occorrenza, le quali vengono concatenate con logica AND dal generatore interno.

Se si vuole imporre una clausola di or, è sufficiente creare due o piu filtersearch separati in cui ciascuno esprimere i casi da associare in OR. Questo sistema permette la creazione di alberature di query.



### Per chi è destinato.

Programmatori che hanno la necessita di comporre query arbitrariamente complesse usando i criteria hibernate.



### A cosa serve il YaFilterSearcher

A usare i criteria hibernate usando un semplice insieme di pojo java.



### Come si usa

E' un oggetto pojo tradizionale serializzabile in cui è possibile popolare delle strutture dati per accogliere clausole di ricerca tipiche di un database.

Si istanzia il filtro dando come argomento la classe dell'entita su cui eseguire la ricerca (la root di hibernate)

Dopo aver compilato il filtro si passa al metodo repository.search(searcher.getSerialized())



### Di cosa ha bisogno

L'unico requisito per poter funzionare è avere i dati di input come richiesti dal chiamante , e che per ciascuno di essi sia supportata la clausola di ricerca dal filtro (il campo correttamente mappato, che non sia di tipo formula, ecc... ecc...).



### Ha requisiti di framework dell'applicativo su cui opera

No! L'unico requisito, oltre a una implementazione JPA definita a livello di progetto,  è che abbia i dati in input a disposizione per la costruzione del filter e che sia presente in libreria il BaseRepository che riconosce il filtro, oltre ad hibernate ovviamente.

L'oggetto searcher è disaccoppiato da ogni logica di come viene trasportato il dato in input e in output. Lo scopo è accedere ai risultati del repository con un criteria hibernate ben formato e arbitrariamente complesso senza usare i criteria.



### Quali clausole supporta

supporto join (beta)

uguaglianza

between tra tipi di oggetti rangeable

like

like insensitive

confronto con operatori ( =, !=, <, <=, >=)

isnull

isnotnull

iszero

isnotempty

confronto di un booleano

concatenare piu filter in or (cluster di query hql concatenate in or)

order by controllo delle clausole di desc asc per singolo campo in order

maxresult (paginazione)

firstresult (paginazione)

lista campi in proiezione (tracciato dati sottoinsieme di quello dell'entita)

clausola in

clausola notIn

clausola di negazione

# Compilazione progetto
eseguire

```
  mvn clean install
```
  Repository nexus http://alessandromodica.com:8081/nexus/repository/maven-releases/
  
```
<dependency>
  <groupId>it.alessandromodica.open</groupId>
  <artifactId>yarepository</artifactId>
  <version>1.1.0</version>
</dependency>
```

# Utilizzo

Importare in un progetto JPA compliance la libreria yarepository e iniettare in Autowired il repository con le interfacce per leggere IQueries e per scrivere ICommand

Presto una guida piu approfondita 

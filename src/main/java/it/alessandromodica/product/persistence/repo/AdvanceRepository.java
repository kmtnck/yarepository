package it.alessandromodica.product.persistence.repo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import it.alessandromodica.product.persistence.exceptions.RepositoryException;
import it.alessandromodica.product.persistence.searcher.YAFilterOperatorClause.Operators;
import it.alessandromodica.product.persistence.searcher.YAFilterSearch;

/**
 * Classe astratta in cui sono raccolte le implementazioni standard per
 * l'accesso al database. Viene ereditata da tutte le classi repository
 * implementate per l'applicazione corrente. Di solito e' sufficiente un solo
 * repository, ma potrebbero esserne implementati piu di uno a seconda le
 * esigenze
 * 
 * @author Alessandro
 *
 * @param <T>
 */
/**
 * 
 * @author amodica
 *
 * @param <T>    entita principale
 * @param <JOIN> quando viene definito un join questa e' l'entita che ne
 *               definisce il target
 */
@Repository
public class AdvanceRepository {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	protected String nameClass;

	@PersistenceContext
	EntityManager em;

	protected Class<?> classEntity;

	protected void setClass(Class<?> classEntity) {

		if (classEntity != null) {
			this.nameClass = classEntity.getName();
			this.classEntity = classEntity;
		}
	}

	/**
	 * Classe specifica per l'uso del BaseRepository
	 * 
	 * Questo pojo fornisce tutte le informazioni minime necessarie per comporre un
	 * CriteriaQuery a libero arbitrio del chiamante.
	 * 
	 * Dopo che sono stati costruiti e accodati i Predicate, si esegue la
	 * injectPredicates la quale aggiunge la clausola where.
	 * 
	 * Successivamente si crea la Query con il builder La query e' quindi pronta per
	 * essere eseguita a discrezione del chiamante.
	 * 
	 * Il metodo
	 * 
	 * injectPredicate()
	 * 
	 * esegue un override della clausola where azzerando i predicati precedenti (non
	 * banale!)
	 * 
	 * Con questo pojo si permette una serializzazione custom di una query
	 * arbitraria sia di lettura che di scrittura con delega completa al chiamante.
	 * 
	 * E' il contrario del YaFilter il quale permette una serializzazione automatica
	 * delle canoniche ricerche sql.
	 * 
	 * Il target dell'entita e' istanziata al momento del recupero di queste
	 * informazioni da parte del chiamante tramite il metodo
	 * 
	 * public InfoHibernateQuery getInfoQuery(Class<?> classEntity)
	 * 
	 * pseudocodice di recupero da una vista o tabella con chiave primaria composita
	 * (codice, processo):
	 * 
	 * a) istanza oggetto infoquery var infoquery =
	 * repo.getInfoQuery(EntitaDelDominioMore.class);
	 * 
	 * b) creazione dei predicati con i metodi hibernate usando le informazioni da
	 * infoquery Predicate pred1 =
	 * infoquery.getBuilder().equal(repository.setFieldRoot(infoquery.getRoot(),"id.codice"),
	 * value); ... Predicate predX =
	 * infoquery.getBuilder().equal(repository.setFieldRoot(infoquery.getRoot(),"id.processo"),
	 * value);
	 * 
	 * c) aggiunta dei predicati nella lista di infoquery
	 * infoquery.getPredicates().add(pred1); infoquery.getPredicates().add(pred2);
	 * 
	 * d) una volta definita la lista, si esegue il metodo injectPredicates
	 * (aggiunta della clausola .where nel criteria) infoquery.injectPredicates();
	 * 
	 * e) recupero della istanza Query tramite il metodo del repository passando il
	 * criterio di infoquery Query query =
	 * repository.createQuery(infoquery.getCriteria());
	 * 
	 * f) esecuzione della query vera e propria List rawlist =
	 * query.getResultList();
	 * 
	 * g) analisi dei dati
	 *
	 * Il punto b,c,d,e puo' essere adattato per costruire una query di tipo
	 * scrittura.
	 * 
	 * @author amodica
	 *
	 */
	public class InfoHibernateQuery {
		private CriteriaBuilder builder;
		private CriteriaQuery criteria;
		private Root<?> root;
		private List<Expression> predicates = new ArrayList<Expression>();
		private EntityType entity_;

		public CriteriaBuilder getBuilder() {
			return builder;
		}

		public void setBuilder(CriteriaBuilder builder) {
			this.builder = builder;
		}

		public CriteriaQuery getCriteria() {
			return criteria;
		}

		public void setCriteria(CriteriaQuery criteria) {
			this.criteria = criteria;
		}

		public Root<?> getRoot() {
			return root;
		}

		public void setRoot(Root<?> root) {
			this.root = root;
		}

		public List<Expression> getPredicates() {
			return predicates;
		}

		public void setPredicates(List<Expression> predicates) {
			this.predicates = predicates;
		}

		void injectPredicates() {
			criteria.where(predicates.toArray(new Predicate[predicates.size()]));
		}

		public EntityType getEntity_() {
			return entity_;
		}

		public void setEntity_(EntityType entity_) {
			this.entity_ = entity_;
		}
	}

	/**
	 * Override nativa repository
	 * 
	 * @param criteria
	 * @return
	 */
	public Query createQuery(CriteriaQuery criteria) {
		return em.createQuery(criteria);
	}

	/**
	 * @param classEntity
	 * @param countQuery
	 * @param fieldToCount
	 * @return
	 */
	public InfoHibernateQuery getInfoQuery(Class<?> classEntity, boolean countQuery, String fieldToCount) {

		InfoHibernateQuery result = new InfoHibernateQuery();

		// Istanza del builder
		CriteriaBuilder builder = em.getCriteriaBuilder();
		result.setBuilder(builder);
		result.setEntity_(em.getMetamodel().entity(classEntity));

		// Si inizializza il tipo del criteriaquery come tipo raw
		CriteriaQuery criteria = null;
		Root<?> root = null;
		if (!countQuery) {

			criteria = builder.createQuery(classEntity);
			root = criteria.from(classEntity);
		} else {

			criteria = builder.createQuery(Long.class);
			root = criteria.from(classEntity);
			
			Path<Object> patch = getPathByRoot(root, fieldToCount);
			criteria.select(builder.countDistinct(patch));
		}

		result.setCriteria(criteria);
		result.setRoot(root);

		return result;
	}

	public InfoHibernateQuery getInfoQuery(Class<?> classEntity) {
		return getInfoQuery(classEntity, false, null);
	}

	public Metamodel getMetamodel() {
		return em.getMetamodel();
	}

	/**
	 * Permette la creazione di predicati di tipo comparable
	 * 
	 * @param <Y>
	 * @param builder
	 * @param field
	 * @param start
	 * @param end
	 * @param typeData
	 * @return
	 */
	public <Y extends Comparable<? super Y>> Predicate createRangePredicate(CriteriaBuilder builder, Expression field,
			Object start, Object end, Class<?> typeData) {
		Predicate result = null;
		if (start != null && end != null) {
			// TODO :asserts!

			if (start.equals(end))
				result = builder.equal(field, (Y) start);

			if (typeData == null || !typeData.getName().contains("String")) {
				result = builder.between(field, (Y) start, (Y) end);
			} else {
				result = builder.and(builder.greaterThanOrEqualTo(field, (Y) start),
						builder.lessThanOrEqualTo(field, (Y) end));
			}

		} else if (start != null) {
			result = builder.greaterThanOrEqualTo(field, (Y) start);
		} else {
			result = builder.lessThanOrEqualTo(field, (Y) end);
		}

		return result;
	}

	/**
	 * Facilita l'inserimento delle clausole di ordinamento
	 * 
	 * @param builder
	 * @param root
	 * @param field
	 * @param descendent
	 * @return
	 */
	public Order getOrderByRoot(CriteriaBuilder builder, Root<?> root, String field, boolean descendent) {

		Order cOrder = null;
		String[] splitField = field.split("\\.");

		if (descendent) {
			if (splitField.length == 2)
				cOrder = builder.desc(root.get(splitField[0]).get(splitField[1]));
			else
				cOrder = builder.desc(root.get(field));
		} else {

			if (splitField.length == 2)
				cOrder = builder.asc(root.get(splitField[0]).get(splitField[1]));
			else
				cOrder = builder.asc(root.get(field));
		}

		return cOrder;
	}

	/**
	 * Permette la definizione di tracciati dati personalizzati tramite la tecnica
	 * delle projections
	 * 
	 * Take care this method!
	 * 
	 * @param fieldsprojection
	 * @param root
	 * @param query
	 * @return
	 */
	public Selection[] getProjections(List<String> fieldsprojection, Root root) {
		List<Selection> projections = new ArrayList<Selection>();
		for (String cField : fieldsprojection) {
			try {
				projections.add(getPathByRoot(root, cField)/*root.get(cField)*/);
			} catch (Exception e) {
				continue;
			}
		}

		return projections.toArray(new Selection[projections.size()]);

	}

	/**
	 * Assegna il giusto riferimento del campo a partire dalla entita root. Ogni
	 * navigazione è un path per raggiungere un certo valore di un parametro
	 * definito negli oggetti pojo. Quindi ogni separazione . rappresenta la
	 * navigazione in un oggetto EntityBean. Ogni oggetto path qui ritornato puo
	 * essere usato per "comporre" un tracciato dati custom cui il tipo hibernate e'
	 * tuple. Questi tracciati dati estremamente custom possono essere bindati a
	 * pojo non per forza autogenerati ma mappati come oggetti hibernate che pero
	 * rappresentano i tracciati dati custom. Questi tracciati custom sono usati
	 * quando si richiede la visualizzazione e ricerca di informazioni afferenti a
	 * legami join concatenati , ad esempio l'aspetto della merceologia degli
	 * articoli dal punto di vista di un carico.
	 * 
	 * L'alternativa ai tracciati dati custom bindati su hibernate a query hql
	 * arbitrariamente complessa, e' quella di delegare al database la logica di
	 * relazione dei dati aggregati e esporre il tracciato dati custom sotto forma
	 * di vista, la quale puo essere gestita come un normale pojo hibernate
	 * autogenerato con il solo costo di un oggetto db in piu in cui pero ha anche
	 * la logica che puo essere modificata senza modificare l'applicazione.
	 * 
	 * L'uso piu comune di questo metodo e' quello di individuare un campo esatto e gestirlo in modo puntuale, ad esempio il count su un solo field per non generare l'errore oracle missing parenthesis
	 * @param root
	 * @param field
	 * @return
	 */
	public Path<Object> getPathByRoot(Root root, String field) {

		String[] splitField = field.split("\\.");
		Path<Object> result = null;

		result = root.get(splitField[0]);
		if (splitField.length > 1) {

			for (int i = 1; i < splitField.length; i++) {
				result = result.get(splitField[i]);
			}

			// return root.get(splitField[0]).get(splitField[1]);

		} /*
			 * else result = root.get(field);
			 */

		return result;
	}

	/**
	 * Facilita l'inserimento di clausole join
	 * 
	 * @param join
	 * @param field
	 * @return
	 */
	public Path<Object> setFieldJoin(Join join, String field) {

		String[] splitField = field.split("\\.");
		if (splitField.length == 2) {
			return join.get(splitField[0]).get(splitField[1]);
		} else
			return join.get(field);
	}

	/*
	 * 
	 * Elenco metodi CRUD
	 * 
	 * 
	 */

	public List<?> getAll() {
		try {
			List<?> obj = em.createQuery("from " + nameClass).getResultList();
			return obj;
		} catch (RuntimeException e) {
			log.error("Errore durante il recupero di tutti gli elementi " + e.getMessage(), e);
			throw e;

		}
	}

	/**
	 * Il criterio order e'una specializzazione del tipo Criterion, il quale ha lo
	 * scopo di istruire hibernate a costruire la query definendo un criterio di
	 * ordinamento su un campo (nome field, no nome campo su db) asc o desc
	 */
	public List<?> getAll(Order orderby) {

		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<?> cq = cb.createQuery(classEntity);
			cq.orderBy(orderby);
			Query query = em.createQuery(cq);

			List<?> obj = query.getResultList();
			return obj;
		} catch (RuntimeException e) {
			log.error("Errore durante il recupero di tutti gli elementi " + e.getMessage(), e);
			throw e;

		}
	}

	public List<?> getAllOrdered(int elementAt, int amount, Order orderby) {

		try {

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<?> cq = cb.createQuery(classEntity);
			cq.orderBy(orderby);
			Query query = em.createQuery(cq);
			query.setMaxResults(amount);
			query.setFirstResult(elementAt);

			List<?> obj = query.getResultList();
			return obj;
		} catch (RuntimeException e) {
			log.error("Errore durante il recupero di tutti gli elementi " + e.getMessage(), e);
			throw e;

		}
	}

	public int getCount(CriteriaQuery<Long> criteria) {
		try {

			Long count = em.createQuery(criteria).getSingleResult();

			return count.intValue();

		} catch (RuntimeException e) {
			log.error("Errore durante il count di entita " + e.getMessage(), e);
			throw e;

		}
	}
	
	public int getCount(InfoHibernateQuery infoquery) {
		try {

			infoquery.injectPredicates();
			Long count = em.createQuery((CriteriaQuery<Long>)infoquery.getCriteria()).getSingleResult();

			return count.intValue();

		} catch (RuntimeException e) {
			log.error("Errore durante il count di entita " + e.getMessage(), e);
			throw e;

		}
	}

	public Predicate createEqual(CriteriaBuilder builder, Root<?> root, String fieldHB, Object valueKey) {
		Predicate equalpred = builder.equal(getPathByRoot(root, fieldHB), valueKey);
		return equalpred;
	}

	public Predicate createIsZero(CriteriaBuilder builder, Root<?> root, String cIsZero) {
		Predicate equalIsZero = builder.equal(getPathByRoot(root, cIsZero), 0);
		return equalIsZero;
	}

	public Predicate createIsNotNull(CriteriaBuilder builder, Root<?> root, String cIsNotNull) {
		Predicate notNullpred = builder.isNotNull(getPathByRoot(root, cIsNotNull));
		return notNullpred;
	}

	public Predicate createIsNull(CriteriaBuilder builder, Root<?> root, String cIsNull) {
		Predicate nullpred = builder.isNull(getPathByRoot(root, cIsNull));
		return nullpred;
	}

	public Predicate createNotIn(Root<?> root, String cNotIn, Object[] listNotIn) {
		Predicate notpred = getPathByRoot(root, cNotIn).in(listNotIn).not();
		return notpred;
	}

	public Predicate createIn(Root<?> root, String cIn, Object[] listIn) {
		Predicate inpred = getPathByRoot(root, cIn).in(listIn);
		return inpred;
	}

	public Predicate createLike(CriteriaBuilder builder, Root<?> root, String field, Object value) {
		Expression<String> rootField = root.get(field);
		Predicate likepred = builder.like(rootField, value.toString());
		return likepred;
	}

	public Predicate createJoin(CriteriaBuilder builder, Root<?> root, String entityToJoin, String fieldToJoin,
			Object valueToJoin) {
		Predicate predJoin = null;
		Expression<Object> exp = setFieldJoin(root.join(entityToJoin), fieldToJoin);
		predJoin = builder.equal(exp, valueToJoin);
		return predJoin;
	}
	
	private class OperatorClause<K extends Comparable<? super K>> {

		public Predicate buildPredicato(CriteriaBuilder builder, Map<String, Object> cOper, Root<?> root) {

			Predicate predicato = null;

			String field = cOper.get(YAFilterSearch.NAME_FIELD).toString();
			Operators operatore = Enum.valueOf(Operators.class, cOper.get("_operatore").toString());

			Expression<K> rootField = root.get(field);
			K value = (K) cOper.get(YAFilterSearch.VALUE_FIELD);

			switch (operatore) {
			case minusequals:
				predicato = builder.lessThanOrEqualTo(rootField, value);
				break;
			case minus:
				predicato = builder.lessThan(rootField, value);
				break;
			case majorequals:
				predicato = builder.greaterThanOrEqualTo(rootField, value);
				break;
			case major:
				predicato = builder.greaterThan(rootField, value);
				break;
			case disequals:
				predicato = builder.notEqual(rootField, value);
				break;
			default:
				break;
			}

			return predicato;
		}

	}
	
	public Predicate createOperator(CriteriaBuilder builder, Root<?> root, Map<String, Object> cOper,
			Class<?> typeData) throws RepositoryException {
		Predicate predicato = null;
		if (typeData != null) {
			OperatorClause buildPredicato = null;

			if (typeData.getName().contains("Date")) {
				buildPredicato = new OperatorClause<Date>();
			} else if (typeData.getName().contains("Integer")) {
				buildPredicato = new OperatorClause<Integer>();
			} else if (typeData.getName().contains("Double")) {
				buildPredicato = new OperatorClause<Double>();
			} else
				throw new RepositoryException(
						"Entita' di clausola operatore non riconosciuta " + typeData.getName());

			predicato = buildPredicato.buildPredicato(builder, cOper, root);
		}
		return predicato;
	}	
	
	public void update(Object obj) {

		setClass((Class<?>) obj.getClass());

		try {
			merge(obj, em);

		} catch (RuntimeException ex) {
			throw ex;
		}
	}

	public void delete(Object obj) {

		setClass((Class<?>) obj.getClass());

		try {
			remove(obj, em);
		} catch (RuntimeException ex) {
			throw ex;
		}

	}

	public void deleteFromId(Object id, String nameField) {

		try {

			em.createQuery("DELETE FROM " + nameClass + " WHERE " + nameField + "=" + id).executeUpdate();

		} catch (RuntimeException ex) {
			throw ex;
		}

	}

	public void deleteAll() throws Exception {
		// TODO Auto-generated method stub
		String hql = "delete from " + nameClass;
		em.createQuery(hql).executeUpdate();
	}

	private void remove(Object obj, EntityManager em) {
		try {
			em.remove(em.contains(obj) ? obj : em.merge(obj));
		} catch (RuntimeException e) {
			log.error("Errore durante la rimozione di una entita " + e.getMessage(), e);
			throw e;
		}
	}

	private void create(Object obj, EntityManager em) {
		try {
			em.persist(obj);
		} catch (RuntimeException e) {
			log.error("Errore durante l'aggiunta di una entita " + e.getMessage(), e);
			throw e;
		}
	}

	private void merge(Object obj, EntityManager em) {
		try {
			em.merge(obj);
		} catch (RuntimeException e) {
			log.error("Errore durante una operazione di merge hibernate su una entita " + e.getMessage(), e);
			throw e;
		}
	}

	public Object getById(Object objId, Class<?> classentity) {

		setClass(classentity);

		return retrieveById(objId, em);

	}

	private Object retrieveById(Object objId, EntityManager em) {
		Object obj = null;
		try {
			obj = em.find(classEntity, objId);
			return obj;
		} catch (RuntimeException e) {
			log.error("Errore durante il recupero di una entita dall'id " + e.getMessage(), e);
			throw e;

		}
	}

	private enum UniqueStrategy {
		single, first, singledefault, firstdefault, list
	}

	@Deprecated
	public List<?> search(CriteriaQuery<?> criteria) {
		return search(em.createQuery(criteria));
	}

	public List<?> search(InfoHibernateQuery infoquery) {
		infoquery.injectPredicates();
		return search(em.createQuery(infoquery.getCriteria()));
	}
	
	private List<?> search(Query query) {
		List<?> result = null;
		try {
			result = query.getResultList();
			return result;
		} catch (RuntimeException e) {
			log.error("Errore durante la ricerca di entita " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Deprecated
	public Object getSingle(CriteriaQuery<?> criteria) throws Exception {
		return getRetrieve(em.createQuery(criteria), UniqueStrategy.single);
	}

	@Deprecated
	public Object getSingleOrDefault(CriteriaQuery<?> criteria) throws Exception {
		return getRetrieve(em.createQuery(criteria), UniqueStrategy.singledefault);
	}

	@Deprecated
	public Object getFirst(CriteriaQuery<?> criteria) throws Exception {
		return getRetrieve(em.createQuery(criteria), UniqueStrategy.first);
	}

	@Deprecated
	public Object getFirstOrDefault(CriteriaQuery<?> criteria) throws Exception {
		return getRetrieve(em.createQuery(criteria), UniqueStrategy.firstdefault);
	}
	
	public Object getSingle(InfoHibernateQuery infoquery) throws Exception {
		infoquery.injectPredicates();
		return getRetrieve(em.createQuery(infoquery.getCriteria()), UniqueStrategy.single);
	}

	public Object getSingleOrDefault(InfoHibernateQuery infoquery) throws Exception {
		infoquery.injectPredicates();
		return getRetrieve(em.createQuery(infoquery.getCriteria()), UniqueStrategy.singledefault);
	}

	public Object getFirst(InfoHibernateQuery infoquery) throws Exception {
		infoquery.injectPredicates();
		return getRetrieve(em.createQuery(infoquery.getCriteria()), UniqueStrategy.first);
	}

	public Object getFirstOrDefault(InfoHibernateQuery infoquery) throws Exception {
		infoquery.injectPredicates();
		return getRetrieve(em.createQuery(infoquery.getCriteria()), UniqueStrategy.firstdefault);
	}	

	private Object getRetrieve(Query query, UniqueStrategy uniquestrategy) throws Exception {
		Object obj = null;
		try {
			obj = getUnique(query, uniquestrategy);
			return obj;
		} catch (RuntimeException e) {
			log.error("Errore durante il recupero di entita " + e.getMessage(), e);
			throw new Exception(e);

		}
	}

	private Object getUnique(Query query, UniqueStrategy uniquestrategy) throws Exception {

		Object obj = null;

		List<Object> result = query.getResultList();

		boolean checkStrategy = false;
		boolean isSingle = false;
		switch (uniquestrategy) {
		case singledefault:
			checkStrategy = result.size() == 0 || result.size() == 1;
			isSingle = true;
			break;
		case firstdefault:
			checkStrategy = result.size() == 0 || result.size() > 0;
			break;
		case single:
			checkStrategy = result.size() == 1;
			isSingle = true;
			break;
		case first:
			checkStrategy = result.size() > 0;
			break;
		default:
			break;
		}

		if (!checkStrategy)
			throw new Exception("Non e' stata trovata una corrispondenza valida per l'entita " + nameClass);
		else {
			if (isSingle) {
				if (result.size() == 1)
					obj = result.get(0);
			} else {
				if (result.size() > 0)
					obj = result.get(0);
			}

		}

		return obj;
	}

	public Number getMax(String nameField) {
		try {

			Number result = retrieveMax(nameField, em);
			return result;
		} catch (RuntimeException e) {
			log.error("Errore durante il recupero del max di entita " + e.getMessage(), e);
			throw e;

		}
	}

	public Number getMax(String nameField, EntityManager em) {

		Number result = retrieveMax(nameField, em);

		return result;
	}

	/**
	 * Questo metodo fornisce gli input per l'operatore max. Per ciascun specifico
	 * operatore aggregato supportato dalla query, si puo' definirne un suo contesto
	 * implementativo dedicato
	 * 
	 * @param nameField
	 * @param em
	 * @return
	 */
	private Number retrieveMax(String nameField, EntityManager em) {
		CriteriaBuilder builder = em.getCriteriaBuilder();

		CriteriaQuery<Integer> criteriaQuery = builder.createQuery(Integer.class);
		Root<?> classRoot = criteriaQuery.from(classEntity);
		criteriaQuery.select(builder.max(getPathByRoot(classRoot, nameField).as(Integer.class)));
		Number result = em.createQuery(criteriaQuery).getSingleResult();
		return result;
	}

}
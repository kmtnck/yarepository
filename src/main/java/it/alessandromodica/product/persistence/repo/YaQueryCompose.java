package  it.alessandromodica.product.persistence.repo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.alessandromodica.product.persistence.exceptions.RepositoryException;
import it.alessandromodica.product.persistence.searcher.YAFilterJoinClause;
import it.alessandromodica.product.persistence.searcher.YAFilterJoinClause.Type;
import it.alessandromodica.product.persistence.searcher.YAFilterOperatorClause.Operators;
import it.alessandromodica.product.persistence.searcher.YAFilterOperatorProperty;
import it.alessandromodica.product.persistence.searcher.YAFilterSearch;
import it.alessandromodica.product.persistence.searcher.YAFilterSearchApp;
import it.alessandromodica.product.persistence.searcher.YAFilterSerializeCriteria;

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
//@Repository
@SuppressWarnings("unchecked")
public abstract class YaQueryCompose<T> {

	@PersistenceContext
	protected EntityManager em;

	private static final Logger log = LoggerFactory.getLogger(YaQueryCompose.class);

	protected Class<T> classEntity;
	protected String nameClass;
	protected YAFilterSerializeCriteria serializeCriteria;
	protected CriteriaBuilder builder;

	protected void setClass(Class<T> classEntity) {

		if (classEntity != null) {
			this.nameClass = classEntity.getName();
			this.classEntity = classEntity;
		}
	}

	public Query buildCriteriaQuery(YAFilterSearchApp searcher) throws RepositoryException {
		return buildCriteriaQuery(searcher.getSerialized());
	}
	
	public Query buildCriteriaQuery(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {
		return buildCriteriaQuery(null, serializeCriteria);
	}

	public boolean isFieldInListLower(String fieldName, YAFilterSerializeCriteria serializeCriteria) {
	    List<String> listLower = serializeCriteria.getListLower();
	    
	    // Utilizza Stream per verificare se il campo è presente nella lista
	    return listLower.stream().anyMatch(field -> field.equals(fieldName));
	}
	
	@SuppressWarnings("rawtypes")
	private Query buildCriteriaQuery(String alias, YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {

		setClass(serializeCriteria.getClassEntity());
		this.serializeCriteria = serializeCriteria;
		
		this.builder = em.getCriteriaBuilder();
		// Si rende il tipo del criteriaquery raw a inizializzazione
		CriteriaQuery query = this.builder.createQuery(classEntity);

		Root<T> root = query.from(classEntity);
		if (alias != null)
			root.alias(alias);

		if (serializeCriteria.getListFieldsProjection().size() > 0) {

			// se esiste almeno una proiezione significa che il tracciato dati deve essere
			// definito con il tipo Object[]
			// e' l'istruzione piu significativa per gestire correttamente e linearmente un
			// tracciato dati inferiore a quello previsto dalla entita'
			query = this.builder.createQuery(Object[].class);
			// dopo aver eseguito l'override della query, parallelamente si ridefinisce il
			// root della query Object[] sulla stessa entita
			// Si ridefinisce la root con la nuova proiezione
			root = query.from(classEntity);

			Selection[] projections = getProjections(serializeCriteria.getListFieldsProjection(), root, query);
			if (projections.length > 0)
				query.multiselect(projections).distinct(true);
		} else
			query = query.select(root).distinct(true);

		List<Predicate> predicates = composeQuery(builder, root, serializeCriteria);
		query.where(predicates.toArray(new Predicate[predicates.size()]));

		List<Order> listsOrder = new ArrayList<Order>(0);
		for (String cOrderBy : serializeCriteria.getListOrderBy()) {
			Order cOrder = null;

			boolean isDescendent = false;
			if (serializeCriteria.getMapDescendent().containsKey(cOrderBy)) {
				isDescendent = serializeCriteria.getMapDescendent().get(cOrderBy);
			}

			cOrder = getOrderByRoot(builder, root, cOrderBy, serializeCriteria.getDescendent() || isDescendent);

			listsOrder.add(cOrder);
		}
		query.orderBy(listsOrder);
		
		query.distinct(serializeCriteria.isDistinct());
		
		Query resultQuery;

		if (serializeCriteria.getMaxResult() > 0) {
			Query limitedCriteriaQuery = em.createQuery(query).setMaxResults(serializeCriteria.getMaxResult())
					.setFirstResult(serializeCriteria.getFirstResult());
			resultQuery = limitedCriteriaQuery;
		} else
			resultQuery = em.createQuery(query);

		return resultQuery;
	}

	private Order getOrderByRoot(CriteriaBuilder builder, Root<T> root, String field, boolean descendent) {

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

	@SuppressWarnings("rawtypes")
	private Selection[] getProjections(List<String> fieldsprojection, Root<?> root, CriteriaQuery<T> query) {
		List<Selection> projections = new ArrayList<Selection>();
		for (String cField : fieldsprojection) {
			try {
				projections.add(getPathByRoot(root, cField));
			} catch (Exception e) {
				continue;
			}
		}

		return projections.toArray(new Selection[projections.size()]);

	}

	/**
	 * Metodo gregario di buildCriteriaQuery per fornire la lista di predicati
	 * eseguendo il parsing dell'oggetto FilterSerialize
	 */
	@SuppressWarnings("rawtypes")
	private List<Predicate> composeQuery(CriteriaBuilder builder, Root<T> root,
			YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {

		List<Predicate> predicates = new ArrayList<Predicate>(0);

		for (YAFilterJoinClause cJoin : serializeCriteria.getListJoinClause()) {

			// XXX: possono esserci varie modalita di join. bisogna sempre tenere in
			// considerazione l'entity graph definito sul bean di testata
			String entityToJoin = cJoin.getEntityToJoin();
			String fieldToJoin = cJoin.getFieldToJoin();
			Object valueToJoin = cJoin.getValueToJoin();
			Type typeJoin = cJoin.getTypeJoin();

			Predicate predJoin = createJoin(builder, root, entityToJoin, fieldToJoin, valueToJoin, typeJoin);

			predicates.add(predJoin);

		}

		Map<String, Object> resultEq = serializeCriteria.getListEquals();
		for (Iterator iterEq = resultEq.entrySet().iterator(); iterEq.hasNext();) {
			Entry cEntry = (Entry) iterEq.next();
			String cKey = cEntry.getKey().toString();

			String fieldHB = cKey;
			Object valueKey = resultEq.get(cKey);

			Predicate equalpred = createEqual(builder, root, fieldHB, valueKey);
			predicates.add(equalpred);
		}

		for (String cBool : serializeCriteria.getListValueBool().keySet()) {

			predicates.add(builder.equal(getPathByRoot(root, cBool), serializeCriteria.getListValueBool().get(cBool)));
		}

		for (Map<String, Object> cLike : serializeCriteria.getListLike()) {
			String field = cLike.get(YAFilterSearch.NAME_FIELD).toString();
			Object value = cLike.get(YAFilterSearch.VALUE_FIELD);
			Predicate likepred = createLike(builder, root, field, value);
			predicates.add(likepred);
		}

		for (Map<String, Object> cInsLike : serializeCriteria.getListLikeInsensitive()) {
			String field = cInsLike.get(YAFilterSearch.NAME_FIELD).toString();
			Object value = cInsLike.get(YAFilterSearch.VALUE_FIELD);
			Expression rootField = getPathByRoot(root, field);
			predicates.add(builder.like(builder.lower(rootField), value.toString().toLowerCase()));
		}

		for (Map<String, Object> cLike : serializeCriteria.getListNotLike()) {
			String field = cLike.get(YAFilterSearch.NAME_FIELD).toString();
			Object value = cLike.get(YAFilterSearch.VALUE_FIELD);
			Predicate likepred = createNotLike(builder, root, field, value);
			predicates.add(likepred);
		}

		for (Map<String, Object> cInsLike : serializeCriteria.getListNotLikeInsensitive()) {
			String field = cInsLike.get(YAFilterSearch.NAME_FIELD).toString();
			Object value = cInsLike.get(YAFilterSearch.VALUE_FIELD);
			Expression rootField = getPathByRoot(root, field);
			predicates.add(builder.notLike(builder.lower(rootField), value.toString().toLowerCase()));
		}

		// Vincoli di controllo tra due valori, vale per il tipo integer, double e date
		for (Map<String, Object> cBT : serializeCriteria.getListbetween()) {

			Class<?> typeData = (Class) cBT.get(YAFilterSearch.TYPE_DATA);
			String field = cBT.get(YAFilterSearch.NAME_FIELD).toString();

			Expression rootField = getPathByRoot(root, field);
			Object valueTo = null;
			Object valueFrom = null;
			if (cBT.get(YAFilterSearch.VALUE_TO) != null)
				valueTo = cBT.get(YAFilterSearch.VALUE_TO);
			if (cBT.get(YAFilterSearch.VALUE_FROM) != null)
				valueFrom = cBT.get(YAFilterSearch.VALUE_FROM);

			predicates.add(createRangePredicate(builder, rootField, valueFrom, valueTo, typeData));

		}

		for (Map<String, Object> cOper : serializeCriteria.getListOperator()) {

			Class<?> typeData = (Class) cOper.get(YAFilterSearch.TYPE_DATA);

			Predicate predicato = createOperator(builder, root, cOper, typeData);

			predicates.add(predicato);
		}

		for (Map<String, Object> cOper : serializeCriteria.getListOperatorProperty()) {

			Class<?> typeData = (Class) cOper.get(YAFilterSearch.TYPE_DATA);
			Predicate predicato = createOperatorProperty(builder, root, cOper, typeData);
			predicates.add(predicato);
		}

		for (String cIn : serializeCriteria.getListIn().keySet()) {

			Object[] listIn = serializeCriteria.getListIn().get(cIn);

			Predicate inpred = createIn(root, cIn, listIn);

			predicates.add(inpred);

		}

		for (String cNotIn : serializeCriteria.getListNotIn().keySet()) {

			Object[] listNotIn = serializeCriteria.getListNotIn().get(cNotIn);
			Predicate notpred = createNotIn(root, cNotIn, listNotIn);
			predicates.add(notpred);
		}

		for (String cIsNull : serializeCriteria.getListIsNull()) {
			Predicate nullpred = createIsNull(builder, root, cIsNull);
			predicates.add(nullpred);
		}

		for (String cIsNotNull : serializeCriteria.getListIsNotNull()) {
			Predicate notNullpred = createIsNotNull(builder, root, cIsNotNull);
			predicates.add(notNullpred);
		}

		// XXX: la lista dei valori non vuoti converge con quelli non nulli.
		// valutare se e' corretto il giro
		for (String cIsNotWS : serializeCriteria.getListIsNotEmpty()) {
			predicates.add(builder.isNotNull(getPathByRoot(root, cIsNotWS)));
		}

		for (String cIsZero : serializeCriteria.getListIsZero()) {
			Predicate equalIsZero = createIsZero(builder, root, cIsZero);
			predicates.add(equalIsZero);
		}

		// Fase di analisi in ricorsione di eventuali altri filtri ricerca legati in
		// clausola or
		List<Predicate> orPredicates = new ArrayList<Predicate>(0);
		for (YAFilterSerializeCriteria cOr : serializeCriteria.getListOrClause()) {

			List<Predicate> orPred = composeQuery(builder, root, cOr);
			Predicate orPredicate = builder.or(orPred.toArray(new Predicate[orPred.size()]));
			orPredicates.add(orPredicate);

		}
		if (orPredicates.size() > 0) {
			Predicate orPredicate = builder.or(orPredicates.toArray(new Predicate[orPredicates.size()]));
			predicates.add(orPredicate);
		}

		List<Predicate> andPredicates = new ArrayList<Predicate>(0);
		for (YAFilterSerializeCriteria cAnd : serializeCriteria.getListAndClause()) {

			List<Predicate> andPred = composeQuery(builder, root, cAnd);
			Predicate andPredicate = builder.and(andPred.toArray(new Predicate[andPred.size()]));
			andPredicates.add(andPredicate);

		}
		if (andPredicates.size() > 0) {
			Predicate andPredicate = builder.or(andPredicates.toArray(new Predicate[andPredicates.size()]));
			predicates.add(andPredicate);
		}
		
		if (serializeCriteria.isNot()) {
			List<Predicate> negatepredicates = new ArrayList<Predicate>(0);
			for (Predicate cp : predicates) {
				Predicate negate = builder.not(cp);
				negatepredicates.add(negate);
			}

			return negatepredicates;

		} else {
			return predicates;
		}
	}

	private Predicate createEqual(CriteriaBuilder builder, Root<T> root, String fieldHB, Object valueKey) {
		Predicate equalpred = builder.equal(getPathByRoot(root, fieldHB), valueKey);
		return equalpred;
	}

	private Predicate createIsZero(CriteriaBuilder builder, Root<T> root, String cIsZero) {
		Predicate equalIsZero = builder.equal(getPathByRoot(root, cIsZero), 0);
		return equalIsZero;
	}

	private Predicate createIsNotNull(CriteriaBuilder builder, Root<T> root, String cIsNotNull) {
		Predicate notNullpred = builder.isNotNull(getPathByRoot(root, cIsNotNull));
		return notNullpred;
	}

	private Predicate createIsNull(CriteriaBuilder builder, Root<T> root, String cIsNull) {
		Predicate nullpred = builder.isNull(getPathByRoot(root, cIsNull));
		return nullpred;
	}

	private Predicate createNotIn(Root<T> root, String cNotIn, Object[] listNotIn) {
		Predicate notpred = getPathByRoot(root, cNotIn).in(listNotIn).not();
		return notpred;
	}

	private Predicate createIn(Root<T> root, String cIn, Object[] listIn) {
		Predicate inpred = getPathByRoot(root, cIn).in(listIn);
		return inpred;
	}

	private Predicate createOperator(CriteriaBuilder builder, Root<T> root, Map<String, Object> cOper,
			Class<?> typeData) throws RepositoryException {
		Predicate predicato = null;
		if (typeData != null) {
			OperatorClause<?> buildPredicato = null;

			String fullName = typeData.getName();
			String[] splitField = fullName.split("\\.");
			String name = splitField[splitField.length - 1];

			switch (name) {
			// Deprecato
			case "Date":
				buildPredicato = new OperatorClause<Date>();
				break;
			case "LocalDate":
				buildPredicato = new OperatorClause<LocalDate>();
				break;
			case "LocalDateTime":
				buildPredicato = new OperatorClause<LocalDateTime>();
				break;
			case "Integer":
				buildPredicato = new OperatorClause<Integer>();
				break;
			case "Long":
				buildPredicato = new OperatorClause<Long>();
				break;
			case "Double":
				buildPredicato = new OperatorClause<Double>();
				break;
			case "BigDecimal":
				buildPredicato = new OperatorClause<BigDecimal>();
				break;
			case "Character":
				buildPredicato = new OperatorClause<Character>();
				break;

			default:
				throw new RepositoryException("Entita' di clausola operatore non riconosciuta " + typeData.getName());
			}

			predicato = buildPredicato.buildPredicato(builder, cOper, root);
		}
		return predicato;
	}

	public Predicate createOperatorProperty(CriteriaBuilder builder, Root<T> root, Map<String, Object> cOper,
			Class<?> typeData) throws RepositoryException {
		Predicate predicato = null;
		if (typeData != null) {
			OperatorProperty<?> buildPredicato = null;

			String fullName = typeData.getName();
			String[] splitField = fullName.split("\\.");
			String name = splitField[splitField.length - 1];

			switch (name) {
			// Deprecato
			case "Date":
				buildPredicato = new OperatorProperty<Date>();
				break;
			case "LocalDate":
				buildPredicato = new OperatorProperty<LocalDate>();
				break;
			case "LocalDateTime":
				buildPredicato = new OperatorProperty<LocalDateTime>();
				break;
			case "Integer":
				buildPredicato = new OperatorProperty<Integer>();
				break;
			case "Long":
				buildPredicato = new OperatorProperty<Long>();
				break;
			case "Double":
				buildPredicato = new OperatorProperty<Double>();
				break;
			case "BigDecimal":
				buildPredicato = new OperatorProperty<BigDecimal>();
				break;
			case "Character":
				buildPredicato = new OperatorProperty<Character>();
				break;

			default:
				throw new RepositoryException("Entita' di clausola operatore non riconosciuta " + typeData.getName());
			}
			predicato = buildPredicato.buildPredicato(builder, cOper, root);
		}
		return predicato;
	}

	private Predicate createLike(CriteriaBuilder builder, Root<T> root, String field, Object value) {
		Expression<String> rootField = getPathByRoot(root, field);
		Predicate likepred = builder.like(rootField, value.toString());
		return likepred;
	}

	private Predicate createNotLike(CriteriaBuilder builder, Root<T> root, String field, Object value) {
		Expression<String> rootField = getPathByRoot(root, field);
		Predicate likepred = builder.notLike(rootField, value.toString());
		return likepred;
	}

	private Predicate createJoin(CriteriaBuilder builder, Root<T> root, String entityToJoin, String fieldToJoin,
			Object valueToJoin, Type typeJoin) {
		JoinType definetype = null;
		if (typeJoin != null) {
			switch (typeJoin) {
			case INNER:
				definetype = JoinType.INNER;
				break;
			case LEFT:
				definetype = JoinType.LEFT;
				break;
			case RIGHT:
				definetype = JoinType.RIGHT;
				break;
			}
		}

		//Predicate predJoin = null;
		Join<T, Object> cjoin;
		cjoin = root.join(entityToJoin, definetype != null ? definetype : JoinType.INNER);
			
		Expression<Object> exp = setFieldJoin(cjoin, fieldToJoin);
		
		Predicate predJoin = (valueToJoin != null) ? builder.equal(exp, valueToJoin) : builder.isNull(exp);
		cjoin.on(predJoin);  // Applica il predicato nella condizione on
		
		return predJoin;
	}

	private <Y extends Comparable<? super Y>> Predicate createRangePredicate(CriteriaBuilder builder, Expression field,
			Object start, Object end, Class<?> typeData) {
		if (start != null && end != null) {
			// TODO :asserts!

			if (start.equals(end))
				return builder.equal(field, (Y) start);

			if (typeData == null || !typeData.getName().contains("String")) {
				return builder.between(field, (Y) start, (Y) end);
			} else {
				return builder.and(builder.greaterThanOrEqualTo(field, (Y) start),
						builder.lessThanOrEqualTo(field, (Y) end));
			}

		} else if (start != null) {
			return builder.greaterThanOrEqualTo(field, (Y) start);
		} else {
			return builder.lessThanOrEqualTo(field, (Y) end);
		}
	}

	/**
	 * Risolve il path di una entita mappata su un model entity
	 */
	public Expression<String> getPathByRoot(Root root, String field) {

	    // Controlla se il campo finale è nella lista dei campi da convertire in lowercase
	    boolean isLower = isFieldInListLower(field, serializeCriteria);
	    
	    
		String[] splitField = field.split("\\.");
		Path<String> result = null;

		result = root.get(splitField[0]);
		if (splitField.length > 1) {

			for (int i = 1; i < splitField.length; i++) {
				result = result.get(splitField[i]);
			}
		}


	    // Se il campo finale deve essere convertito in lowercase, applica la funzione lower
	    if (isLower) {
	        return builder.lower(result); // Restituisce l'espressione in lowercase
	    }

	    return result; // Restituisce il path originale se non è in lowercase

	}

	private Path<Object> setFieldJoin(Join<T, Object> join, String field) {
	    String[] splitField = field.split("\\.");
	    Path<Object> path = join;
	    for (String part : splitField) {
	        path = path.get(part);
	    }
	    return path;
	}

	private class OperatorClause<K extends Comparable<? super K>> extends OperatorCommon {

		public Predicate buildPredicato(CriteriaBuilder builder, Map<String, Object> cOper, Root<T> root) {

			Predicate predicato = null;
			Operators operatore = Enum.valueOf(Operators.class, cOper.get("operatore").toString());
			Expression<K> rootField = getExpressionProperty(root, cOper.get(YAFilterSearch.NAME_FIELD).toString());

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

	private class OperatorProperty<K extends Comparable<? super K>> extends OperatorCommon {

		public Predicate buildPredicato(CriteriaBuilder builder, Map<String, Object> cOper, Root<T> root) {

			Predicate predicato = null;
			YAFilterOperatorProperty.Operators operatore = Enum.valueOf(YAFilterOperatorProperty.Operators.class,
					cOper.get("operatore").toString());

			Expression<K> resolvePathField1 = getExpressionProperty(root,
					cOper.get(YAFilterSearch.PROPERTY_1).toString());
			Expression<K> resolvePathField2 = getExpressionProperty(root,
					cOper.get(YAFilterSearch.PROPERTY_2).toString());

			switch (operatore) {
			case equals:
				predicato = builder.equal(resolvePathField1, resolvePathField2);
				break;
			case minusequals:
				predicato = builder.lessThanOrEqualTo(resolvePathField1, resolvePathField2);
				break;
			case minus:
				predicato = builder.lessThan(resolvePathField1, resolvePathField2);
				break;
			case majorequals:
				predicato = builder.greaterThanOrEqualTo(resolvePathField1, resolvePathField2);
				break;
			case major:
				predicato = builder.greaterThan(resolvePathField1, resolvePathField2);
				break;
			case disequals:
				predicato = builder.notEqual(resolvePathField1, resolvePathField2);
				break;
			default:
				break;
			}

			return predicato;
		}
	}

	private abstract class OperatorCommon<K extends Comparable<? super K>> {
		protected Expression<K> getExpressionProperty(Root<T> root, String field) {
			String[] splitField = field.split("\\.");
			Expression<K> result = resolvePath(root, splitField);

			return result;
		}

		/**
		 * @param root
		 * @param splitField
		 * @return
		 */
		protected Path<K> resolvePath(Root<T> root, String[] splitField) {
			Path<K> resolvePathField = null;
			resolvePathField = root.get(splitField[0]);
			if (splitField.length > 1) {

				for (int i = 1; i < splitField.length; i++) {
					resolvePathField = resolvePathField.get(splitField[i]);
				}
			}
			return resolvePathField;
		}
	}
	
	protected enum UniqueStrategy {
		single, first, singledefault, firstdefault, list
	}

	public List<T> search(CriteriaQuery<T> criteria) throws RepositoryException {
		return search(em.createQuery(criteria));
	}

	public List<T> search(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {

		try {
			return search(buildCriteriaQuery(serializeCriteria));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e.getMessage(),e);
		}
	}
	
	public List<T> search(YAFilterSearchApp searcher) throws RepositoryException {

		return search(buildCriteriaQuery(searcher.getSerialized()));
	}
	
	private List<T> search(Query query) throws RepositoryException {
		List<T> result = null;
		try {
			result = query.getResultList();
			return result;
		} catch (RuntimeException e) {
			log.error("Errore durante la ricerca di entita " + e.getMessage(), e);
			throw new RepositoryException(e);

		}
	}

	public T getSingle(YAFilterSearchApp searcher) throws RepositoryException {
		return (T) getRetrieve(buildCriteriaQuery(searcher.getSerialized()), UniqueStrategy.single);
	}

	public T getSingleOrDefault(YAFilterSearchApp searcher) throws RepositoryException {
		return (T) getRetrieve(buildCriteriaQuery(searcher.getSerialized()), UniqueStrategy.singledefault);
	}

	public T getFirst(YAFilterSearchApp searcher) throws RepositoryException {
		return (T) getRetrieve(buildCriteriaQuery(searcher.getSerialized()), UniqueStrategy.first);
	}

	public T getFirstOrDefault(YAFilterSearchApp searcher) throws RepositoryException {
		return (T) getRetrieve(buildCriteriaQuery(searcher.getSerialized()), UniqueStrategy.firstdefault);
	}
	
	
	public T getSingle(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {
		return (T) getRetrieve(buildCriteriaQuery(serializeCriteria), UniqueStrategy.single);
	}

	public T getSingleOrDefault(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {
		return (T) getRetrieve(buildCriteriaQuery(serializeCriteria), UniqueStrategy.singledefault);
	}

	public T getFirst(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {
		return (T) getRetrieve(buildCriteriaQuery(serializeCriteria), UniqueStrategy.first);
	}

	public T getFirstOrDefault(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {
		return (T) getRetrieve(buildCriteriaQuery(serializeCriteria), UniqueStrategy.firstdefault);
	}

	protected T getRetrieve(Query query, UniqueStrategy uniquestrategy) throws RepositoryException {
		T obj = null;
		try {
			obj = getUnique(query, uniquestrategy);
			return obj;
		} catch (RuntimeException e) {
			log.error("Errore durante il recupero di entita " + e.getMessage(), e);
			throw new RepositoryException(e);

		}
	}

	private T getUnique(Query query, UniqueStrategy uniquestrategy) throws RepositoryException {

		T obj = null;

		List<T> result = query.getResultList();

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
			throw new RepositoryException("Non e' stata trovata una corrispondenza valida per l'entita " + nameClass);
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

	public int getCount(YAFilterSearchApp searcher) throws Exception {
	
		return getCount(searcher.getSerialized());
	}
	
	public int getCount(YAFilterSerializeCriteria serializeCriteria) {
		try {

			setClass(serializeCriteria.getClassEntity());
			this.serializeCriteria = serializeCriteria;
			
			builder = em.getCriteriaBuilder();

			CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
			Root<T> root = countQuery.from(classEntity);
			
			countQuery.distinct(serializeCriteria.isDistinct());
			
			List<Predicate> predicates = composeQuery(builder, root, serializeCriteria);

			countQuery.select(builder.countDistinct(root));

			countQuery.where(predicates.toArray(new Predicate[predicates.size()]));

			Long count = em.createQuery(countQuery).getSingleResult();

			return count.intValue();
		} catch (Exception e) {
			log.error("Errore durante il count di entita " + e.getMessage(), e);
			throw new RuntimeException(e);

		}
	}

	public Number getMax(String nameField, Class<T> classEntity) throws RepositoryException {
		try {

			setClass(classEntity);

			Number result = retrieveMax(nameField, em);
			return result;
		} catch (RuntimeException e) {
			log.error("Errore durante il recupero del max di entita " + e.getMessage(), e);
			throw new RepositoryException(e);

		}
	}

	public Number getMax(String nameField, EntityManager em) throws RepositoryException {

		Number result = retrieveMax(nameField, em);

		return result;
	}

	private Number retrieveMax(String nameField, EntityManager em) {
		builder = em.getCriteriaBuilder();

		CriteriaQuery<Integer> criteriaQuery = builder.createQuery(Integer.class);
		Root<T> classRoot = criteriaQuery.from(classEntity);
		criteriaQuery.select(builder.max(getPathByRoot(classRoot, nameField).as(Integer.class)));
		Number result = em.createQuery(criteriaQuery).getSingleResult();
		return result;
	}	

}
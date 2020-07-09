package it.alessandromodica.product.persistence.repo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityGraph;
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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import it.alessandromodica.product.persistence.exceptions.RepositoryException;
import it.alessandromodica.product.persistence.interfaces.IBulkTransaction;
import it.alessandromodica.product.persistence.interfaces.IRepositoryCommands;
import it.alessandromodica.product.persistence.interfaces.IRepositoryQueries;
import it.alessandromodica.product.persistence.searcher.YAFilterJoinClause;
import it.alessandromodica.product.persistence.searcher.YAFilterOperatorClause.Operators;
import it.alessandromodica.product.persistence.searcher.YAFilterSearch;
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
@SuppressWarnings("unchecked")
@Repository
public class BaseRepository<T, JOIN> implements IRepositoryQueries<T, JOIN>, IRepositoryCommands<T, JOIN>{

	@PersistenceContext
	EntityManager em;

	private static final Logger log = Logger.getLogger(BaseRepository.class);

	protected Class<T> classEntity;

	protected void setClass(Class<T> classEntity) {

		if (classEntity != null) {
			this.nameClass = classEntity.getName();
			this.classEntity = classEntity;
		}
	}

	protected String nameClass;

	public void executeTransaction(IBulkTransaction bulkoperation) throws RepositoryException {
		try {

			bulkoperation.persist();

			log.info("Istruzioni in transazione eseguite correttamente!");
		} catch (Exception ex) {
			log.error("Si e' verificato un errore durante una transazione db", ex);
			throw new RepositoryException(ex.getMessage(), ex);
		}
	}

	protected Query buildCriteriaQuery(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {
		return buildCriteriaQuery(null, serializeCriteria);
	}

	@SuppressWarnings("rawtypes")
	private static <Y extends Comparable<? super Y>> Predicate createRangePredicate(CriteriaBuilder builder,
			Expression field, Object start, Object end, Class<?> typeData) {
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

	@SuppressWarnings("rawtypes")
	protected Query buildCriteriaQuery(String alias, YAFilterSerializeCriteria serializeCriteria)
			throws RepositoryException {

		setClass(serializeCriteria.getClassEntity());

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> query = builder.createQuery(classEntity);

		Root<T> root = query.from(classEntity);
		if (alias != null)
			root.alias(alias);

		if (serializeCriteria.getListFieldsProjection().size() > 0) {
			Selection[] projections = getProjections(serializeCriteria.getListFieldsProjection(), root, query);
			if (projections.length > 0)
				query.multiselect(projections).distinct(true);
		} else
			query = query.select(root).distinct(true);

		//query = query.select(root);

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

			/*
			 * if (serializeCriteria.getDescendent() || isDescendent) { cOrder =
			 * builder.desc(root.get(cOrderBy)); } else { cOrder =
			 * builder.asc(root.get(cOrderBy)); }
			 */

			listsOrder.add(cOrder);
		}
		query.orderBy(listsOrder);

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

	protected List<Predicate> buildPredicates(String alias, YAFilterSerializeCriteria serializeCriteria)
			throws RepositoryException {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> query = builder.createQuery(classEntity);
		Root<T> root = query.from(classEntity);
		if (alias != null)
			root.alias(alias);

		List<Predicate> predicates = composeQuery(builder, root, serializeCriteria);

		return predicates;
	}

	@SuppressWarnings("rawtypes")
	private Selection[] getProjections(List<String> fieldsprojection, Root<?> root, CriteriaQuery<T> query) {
		List<Selection> projections = new ArrayList<Selection>();
		for (String cField : fieldsprojection) {
			try {
				projections.add(root.get(cField));
				query.groupBy(root.get(cField));
			} catch (Exception e) {
				continue;
			}
		}

		return projections.toArray(new Selection[projections.size()]);

	}

	@SuppressWarnings("rawtypes")
	private List<Predicate> composeQuery(CriteriaBuilder builder, Root<T> root,
			YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {

		List<Predicate> predicates = new ArrayList<Predicate>(0);

		// aggiunge inner join per entity graph
		for (String cEg : serializeCriteria.getListEntityGraph()) {
			EntityGraph<T> eg = (EntityGraph<T>) em.getEntityGraph(cEg);
			eg.getAttributeNodes().stream().forEach(an -> {
				root.fetch(an.getAttributeName(), JoinType.INNER);
			});
		}

		for (YAFilterJoinClause<JOIN> cJoin : serializeCriteria.getListJoinClause()) {

			Join<T, JOIN> righeJoin = root.join(cJoin.getEntityToJoin());
			Expression<Object> exp = setFieldJoin(righeJoin, cJoin.getFieldToJoin());

			builder.equal(exp, cJoin.getValueToJoin());
		}

		Map<String, Object> resultEq = serializeCriteria.getListEquals();
		for (Iterator iterEq = resultEq.entrySet().iterator(); iterEq.hasNext();) {
			Entry cEntry = (Entry) iterEq.next();
			String cKey = cEntry.getKey().toString();

			String fieldHB = cKey;
			predicates.add(builder.equal(setFieldRoot(root, fieldHB), resultEq.get(cKey)));
		}

		for (String cBool : serializeCriteria.getListValueBool().keySet()) {

			predicates.add(builder.equal(setFieldRoot(root, cBool), serializeCriteria.getListValueBool().get(cBool)));
		}

		for (Map<String, Object> cLike : serializeCriteria.getListLike()) {
			String field = cLike.get(YAFilterSearch.NAME_FIELD).toString();
			Object value = cLike.get(YAFilterSearch.VALUE_FIELD);
			Expression<String> rootField = root.get(field);
			predicates.add(builder.like(rootField, value.toString()));
		}

		for (Map<String, Object> cInsLike : serializeCriteria.getListLikeInsensitive()) {
			String field = cInsLike.get(YAFilterSearch.NAME_FIELD).toString();
			Object value = cInsLike.get(YAFilterSearch.VALUE_FIELD);
			Expression<String> rootField = root.get(field);
			predicates.add(builder.like(builder.lower(rootField), value.toString().toLowerCase()));
		}

		// Vincoli di controllo tra due valori, vale per il tipo integer, double e date
		for (Map<String, Object> cBT : serializeCriteria.getListbetween()) {

			Class<?> typeData = (Class) cBT.get(YAFilterSearch.TYPE_DATA);
			String field = cBT.get(YAFilterSearch.NAME_FIELD).toString();

			Expression rootField = setFieldRoot(root, field);
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

			predicates.add(predicato);
		}

		for (String cIn : serializeCriteria.getListIn().keySet()) {

			Object[] listIn = serializeCriteria.getListIn().get(cIn);

			predicates.add(setFieldRoot(root, cIn).in(listIn));

		}

		for (String cNotIn : serializeCriteria.getListNotIn().keySet()) {

			Object[] listNotIn = serializeCriteria.getListNotIn().get(cNotIn);
			predicates.add(setFieldRoot(root, cNotIn).in(listNotIn).not());
		}

		for (String cIsNull : serializeCriteria.getListIsNull()) {
			predicates.add(builder.isNull(setFieldRoot(root, cIsNull)));
		}

		for (String cIsNotNull : serializeCriteria.getListIsNotNull()) {
			predicates.add(builder.isNotNull(setFieldRoot(root, cIsNotNull)));
		}

		// XXX: la lista dei valori non vuoti converge con quelli non nulli.
		// valutare se e' corretto il giro
		for (String cIsNotWS : serializeCriteria.getListIsNotEmpty()) {
			predicates.add(builder.isNotNull(setFieldRoot(root, cIsNotWS)));
		}

		for (String cIsZero : serializeCriteria.getListIsZero()) {
			predicates.add(builder.equal(setFieldRoot(root, cIsZero), 0));
		}

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

		if (serializeCriteria.isNot()) {
			List<Predicate> notpred = new ArrayList<Predicate>(0);
			if (predicates.size() > 0) {
				Predicate concatPredicate = null;
				for (Predicate predicate : orPredicates) {
					concatPredicate = builder.and(predicate);
				}
				Predicate not = builder.not(concatPredicate);
				notpred.add(not);
			}
			return notpred;
		} else
			return predicates;

	}

	/**
	 * @param root
	 * @param fieldHB
	 * @return
	 */
	private Path<Object> setFieldRoot(Root<T> root, String field) {

		String[] splitField = field.split("\\.");
		if (splitField.length == 2) {
			return root.get(splitField[0]).get(splitField[1]);
		} else
			return root.get(field);
	}

	private Path<Object> setFieldJoin(Join<T, JOIN> join, String field) {

		String[] splitField = field.split("\\.");
		if (splitField.length == 2) {
			return join.get(splitField[0]).get(splitField[1]);
		} else
			return join.get(field);
	}

	/**
	 * Classe per la composizione del predicato operatore
	 * 
	 * @author amodica
	 *
	 * @param <K>
	 */
	private class OperatorClause<K extends Comparable<? super K>> {

		public Predicate buildPredicato(CriteriaBuilder builder, Map<String, Object> cOper, Root<T> root) {

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

	public List<T> getAll() throws RepositoryException {
		try {
			List<T> obj = em.createQuery("from " + nameClass).getResultList();
			return obj;
		} catch (RuntimeException e) {
			log.error("Errore durante il recupero di tutti gli elementi " + e.getMessage(), e);
			throw new RepositoryException(e);

		}
	}

	/**
	 * Il criterio order è una specializzazione del tipo Criterion, il quale ha
	 * però lo scopo di istruire hibernate a costruire la query definendo un
	 * criterio di ordinamento su un campo (nome field, no nome campo su db) asc o
	 * desc
	 */
	public List<T> getAll(Order orderby) throws RepositoryException {

		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<T> cq = cb.createQuery(classEntity);
			cq.orderBy(orderby);
			Query query = em.createQuery(cq);

			List<T> obj = query.getResultList();
			return obj;
		} catch (RuntimeException e) {
			log.error("Errore durante il recupero di tutti gli elementi " + e.getMessage(), e);
			throw new RepositoryException(e);

		}
	}

	public List<T> getAllOrdered(int elementAt, int amount, Order orderby) throws RepositoryException {

		try {

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<T> cq = cb.createQuery(classEntity);
			cq.orderBy(orderby);
			Query query = em.createQuery(cq);
			query.setMaxResults(amount);
			query.setFirstResult(elementAt);

			List<T> obj = query.getResultList();
			return obj;
		} catch (RuntimeException e) {
			log.error("Errore durante il recupero di tutti gli elementi " + e.getMessage(), e);
			throw new RepositoryException(e);

		}
	}

	public T getByCompositeId(T objId) throws RepositoryException {

		try {

			Serializable id = (Serializable) objId;
			return (T) em.getReference(classEntity, id);

		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			throw new RepositoryException(e);

		}
	}

	@Transactional
	public void add(T obj) throws RepositoryException {

		setClass((Class<T>) obj.getClass());

		try {
			create(obj, em);
		} catch (RepositoryException ex) {
			throw ex;
		}
	}

	@Transactional
	public void update(T obj) throws RepositoryException {

		setClass((Class<T>) obj.getClass());

		try {
			merge(obj, em);

		} catch (RepositoryException ex) {
			throw ex;
		}
	}

	@Transactional
	public void delete(T obj) throws RepositoryException {

		setClass((Class<T>) obj.getClass());

		try {
			remove(obj, em);
		} catch (RepositoryException ex) {
			throw ex;
		}

	}

	@Transactional
	public void deleteFromId(Object id, String nameField) throws RepositoryException {

		try {

			em.createQuery("DELETE FROM " + nameClass + " WHERE " + nameField + "=" + id).executeUpdate();

		} catch (Exception ex) {
			throw new RepositoryException(ex);
		}

	}

	@Transactional
	public void deleteAll() throws RepositoryException {
		// TODO Auto-generated method stub
		String hql = "delete from " + nameClass;
		em.createQuery(hql).executeUpdate();
	}

	private void remove(T obj, EntityManager em) throws RepositoryException {
		try {
			em.remove(em.contains(obj) ? obj : em.merge(obj));
		} catch (RuntimeException e) {
			log.error("Errore durante la rimozione di una entita " + e.getMessage(), e);
			throw new RepositoryException(e);
		}
	}

	private void create(T obj, EntityManager em) throws RepositoryException {
		try {
			em.persist(obj);
		} catch (RuntimeException e) {
			log.error("Errore durante l'aggiunta di una entita " + e.getMessage(), e);
			throw new RepositoryException(e);
		}
	}

	private void merge(T obj, EntityManager em) throws RepositoryException {
		try {
			em.merge(obj);
		} catch (RuntimeException e) {
			log.error("Errore durante una operazione di merge hibernate su una entita " + e.getMessage(), e);
			throw new RepositoryException(e);
		}
	}

	public T getById(Object objId, Class<T> classEntity) throws RepositoryException {

		setClass(classEntity);

		return retrieveById(objId, em);

	}

	private T retrieveById(Object objId, EntityManager em) throws RepositoryException {

		T obj = null;
		try {
			obj = (T) em.find(classEntity, objId);
			return obj;
		} catch (RuntimeException e) {
			log.error("Errore durante il recupero di una entita dall'id " + e.getMessage(), e);
			throw new RepositoryException(e);

		}
	}

	private enum UniqueStrategy {
		single, first, singledefault, firstdefault, list
	}

	public List<T> search(CriteriaQuery<T> criteria) throws RepositoryException {
		return search(em.createQuery(criteria));
	}

	public List<T> search(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {

		return search(buildCriteriaQuery(serializeCriteria));
	}

	public List<T> search(Query query) throws RepositoryException {
		List<T> result = null;
		try {
			result = query.getResultList();
			return result;
		} catch (RuntimeException e) {
			log.error("Errore durante la ricerca di entita " + e.getMessage(), e);
			throw new RepositoryException(e);

		}
	}

	public T getSingle(CriteriaQuery<T> criteria) throws RepositoryException {
		return getRetrieve(em.createQuery(criteria), UniqueStrategy.single);
	}

	public T getSingle(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {
		return (T) getRetrieve(buildCriteriaQuery(serializeCriteria), UniqueStrategy.single);
	}

	public T getSingleOrDefault(CriteriaQuery<T> criteria) throws RepositoryException {
		return getRetrieve(em.createQuery(criteria), UniqueStrategy.singledefault);
	}

	public T getSingleOrDefault(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {
		return (T) getRetrieve(buildCriteriaQuery(serializeCriteria), UniqueStrategy.singledefault);
	}

	public T getFirst(CriteriaQuery<T> criteria) throws RepositoryException {
		return getRetrieve(em.createQuery(criteria), UniqueStrategy.first);
	}

	public T getFirst(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {
		return (T) getRetrieve(buildCriteriaQuery(serializeCriteria), UniqueStrategy.first);
	}

	public T getFirstOrDefault(CriteriaQuery<T> criteria) throws RepositoryException {
		return getRetrieve(em.createQuery(criteria), UniqueStrategy.firstdefault);
	}

	public T getFirstOrDefault(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {
		return (T) getRetrieve(buildCriteriaQuery(serializeCriteria), UniqueStrategy.firstdefault);
	}

	private T getRetrieve(Query query, UniqueStrategy uniquestrategy) throws RepositoryException {
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
		case firstdefault:
			checkStrategy = result.size() == 0 || result.size() > 0;
			break;
		case single:
			checkStrategy = result.size() == 1;
			isSingle = true;
		case first:
			checkStrategy = result.size() > 0;
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

	public int getCount(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException {
		try {

			setClass(serializeCriteria.getClassEntity());

			CriteriaBuilder builder = em.getCriteriaBuilder();

			String alias = nameClass.replace(".", "");
			List<Predicate> predicates = buildPredicates(alias, serializeCriteria);

			CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
			Root<T> entity_ = countQuery.from(classEntity);
			entity_.alias(alias);
			countQuery.select(builder.count(entity_));
			countQuery.where(predicates.toArray(new Predicate[predicates.size()]));

			Long count = em.createQuery(countQuery).getSingleResult();

			return count.intValue();
		} catch (RuntimeException e) {
			log.error("Errore durante il count di entita " + e.getMessage(), e);
			throw new RepositoryException(e);

		}
	}

	public Number getMax(String nameField) throws RepositoryException {
		try {

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
		CriteriaBuilder builder = em.getCriteriaBuilder();

		CriteriaQuery<Integer> criteriaQuery = builder.createQuery(Integer.class);
		Root<T> classRoot = criteriaQuery.from(classEntity);
		criteriaQuery.select(builder.max(setFieldRoot(classRoot, nameField).as(Integer.class)));
		Number result = em.createQuery(criteriaQuery).getSingleResult();
		return result;
	}

}

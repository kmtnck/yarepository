package it.alessandromodica.product.persistence.repo;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.transaction.Transactional;

import org.apache.log4j.Logger;

import it.alessandromodica.product.persistence.exceptions.RepositoryException;
import it.alessandromodica.product.persistence.interfaces.IBulkTransaction;
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
//@Repository
public abstract class BaseRepository<T> extends YaQueryCompose<T> /*implements IRepositoryQueries<T>, IRepositoryCommands<T>*/ {

	private static final Logger log = Logger.getLogger(BaseRepository.class);
	
	public void flush() {
		em.flush();
	}

	/**
	 * Override nativa repository
	 * 
	 */
	public Query createQuery(CriteriaQuery<?> criteria) {
		return em.createQuery(criteria);
	}
	
	/**
	 * Crea una query in formato sql nativo
	 * 
	 * @param sql
	 * @return
	 */
	public Query createNativeQuery(String sql) {
		return em.createNativeQuery(sql);
	}

	public Query createNativeQuery(String sql, Class<?> resultclass) {
		return em.createNativeQuery(sql, resultclass);
	}

	public Query createNativeQuery(String sql, String resultSetMapping) {
		return em.createNativeQuery(sql, resultSetMapping);
	}

	/**
	 * Crea una namequery
	 * 
	 * @param namequery
	 * @return
	 */
	public Query createNamedQuery(String namequery) {
		return em.createNamedQuery(namequery);
	}
	
	public void executeTransaction(IBulkTransaction bulkoperation) throws RepositoryException {
		try {

			bulkoperation.persist();

			log.info("Istruzioni in transazione eseguite correttamente!");
		} catch (Exception ex) {
			log.error("Si e' verificato un errore durante una transazione db", ex);
			throw new RepositoryException(ex.getMessage(), ex);
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
	public void deleteFromId(Object id, String nameField, Class<T> classEntity) throws RepositoryException {

		try {
			setClass(classEntity);
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
	
	public T getSingle(CriteriaQuery<T> criteria) throws RepositoryException {
		return getRetrieve(em.createQuery(criteria), UniqueStrategy.single);
	}

	public T getSingleOrDefault(CriteriaQuery<T> criteria) throws RepositoryException {
		return getRetrieve(em.createQuery(criteria), UniqueStrategy.singledefault);
	}

	public T getFirst(CriteriaQuery<T> criteria) throws RepositoryException {
		return getRetrieve(em.createQuery(criteria), UniqueStrategy.first);
	}

	public T getFirstOrDefault(CriteriaQuery<T> criteria) throws RepositoryException {
		return getRetrieve(em.createQuery(criteria), UniqueStrategy.firstdefault);
	}

	
}

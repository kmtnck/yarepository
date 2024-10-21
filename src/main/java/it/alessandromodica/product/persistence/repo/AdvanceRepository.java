package it.alessandromodica.product.persistence.repo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * 
 * Un esempio di repository avanzato che vuole verificare la bonta di integrazione di un modulo legacy per accedere al database tramite hibernate.
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
public class AdvanceRepository<T> extends BaseRepository<T> {

	private final static Logger log = LoggerFactory.getLogger(AdvanceRepository.class);

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
	@SuppressWarnings("rawtypes")
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Query createQuery(CriteriaQuery criteria) {
		return em.createQuery(criteria);
	}

	/**
	 * @param classEntity
	 * @param countQuery
	 * @param fieldToCount
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public InfoHibernateQuery getInfoQuery(Class<?> classEntity, boolean countQuery, String fieldToCount) {

		InfoHibernateQuery result = new InfoHibernateQuery();
		log.info("Prototipale istanza di un hibernate query per agire a basso livello");
		log.info(
				"Tale utilizzo e' da fare con cautela ed e' un tentativo di integrare un approccio legacy per situazioni emergenziali");
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

			Expression<String> patch = getPathByRoot(root, fieldToCount);
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
}
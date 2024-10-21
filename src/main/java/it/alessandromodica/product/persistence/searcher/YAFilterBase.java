package it.alessandromodica.product.persistence.searcher;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import it.alessandromodica.product.persistence.exceptions.RepositoryException;


/**
 * Classe base del searcher usato per eseguire ricerche tramite lo strumento
 * BOSearcher. I criteri di ricerca sono opportunamente serializzati affinche'
 * siano convertiti in automatico in query HQL riconosciute da hibernate
 * 
 * @author Alessandro
 *
 */
public class YAFilterBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9150468309827097773L;

	protected Class<?> classEntity;

	/**
	 * Metodo che serializza tramite reflection i fields definiti dalla classe che
	 * la estende
	 * 
	 * Il metodo potrebbe subire modifiche in base al tipo di dati presenti sul
	 * datastorage. Sono definiti per alcuni tipi standard le canoniche operazioni
	 * di trasformazione
	 * 
	 * @param src
	 * @return
	 * @throws RepositoryException
	 */
	@SuppressWarnings("rawtypes")
	protected static Map<String, Object> toDictionary(Object src) throws RepositoryException {
		Map<String, Object> result = new HashMap<String, Object>();

		// recupero la classe dell'oggetto
		Class typeClass = src.getClass();

		try {
			for (Field cfield : typeClass.getDeclaredFields()) {

				boolean isStaticField = java.lang.reflect.Modifier.isStatic(cfield.getModifiers())
						&& java.lang.reflect.Modifier.isFinal(cfield.getModifiers());
				if (isStaticField)
					continue;

				if (!cfield.isAccessible())
					cfield.setAccessible(true);

				String nameField = cfield.getName();
				Object value = cfield.get(src);

				if (value != null) {

					// XXX: sono accettati valori di tipo stringa, int, char, double, date e un
					// generico tipo Class
					if (value instanceof String || value instanceof Integer || value instanceof Character
							|| value instanceof Double || value instanceof Long || value instanceof Date || value instanceof LocalDate || value instanceof Boolean || (value instanceof LocalDateTime) || (value instanceof Class<?>)) {

						// XXX: il dato viene castato in modo opportuno per essere inserito nella mappa
						// result finale
						if ((value instanceof String && StringUtils.isNotBlank(value.toString()))
								|| (value instanceof Character && StringUtils.isNotBlank(value.toString()))
								|| (value instanceof Integer && Integer.parseInt(value.toString()) != 0)
								|| (value instanceof Double && Double.parseDouble(value.toString()) != 0.0)
								|| (value instanceof Long && Long.parseLong(value.toString()) != 0.0)
								|| (value instanceof Date) || (value instanceof LocalDate) || (value instanceof LocalDateTime) || value instanceof Boolean || (value instanceof Class<?>)) {

							result.put(nameField, value);
						}

					}
					else
						throw new Exception(String.format("Il tipo %s del valore %s non e' supportato ",value.getClass(),value));
				}
			}

			return result;

		} catch (Exception ex) {
			throw new RepositoryException("Errore durante la serializzazione di un oggetto", ex);
		}

	}

	public Class<?> getClassEntity() {
		return classEntity;
	}

	public void setClassEntity(Class<?> classEntity) {
		this.classEntity = classEntity;
	}

}

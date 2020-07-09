package it.alessandromodica.product.persistence.repo;

import org.apache.log4j.Logger;

/**
 * In questa classe repository è possibile definire tutti i metodi legacy
 * necessari ad accedere a sql puro
 * 
 * E' la classe repository dedicata al software che si vuole realizzare e
 * estende la classe astratta BaseRepository
 * 
 * @author Alessandro
 *
 * @param <T>
 */
@Deprecated
public class AppRepository<T, JOIN> extends BaseRepository<T, JOIN>  {

	private static final Logger log = Logger.getLogger(AppRepository.class);

	@Deprecated
	public AppRepository<T, JOIN> setEntity(Class<T> classEntity) {
		// TODO Auto-generated method stub
		setClass(classEntity);
		return this;

	}

}

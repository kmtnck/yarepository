package it.alessandromodica.product.persistence.repo;

import org.springframework.stereotype.Repository;

import it.alessandromodica.product.persistence.interfaces.IRepositoryCommands;
import it.alessandromodica.product.persistence.interfaces.IRepositoryQueries;

/**
 * In questa classe repository e' possibile definire tutti i metodi legacy
 * necessari ad accedere a sql puro
 * 
 * E' la classe repository dedicata al software che si vuole realizzare e
 * estende la classe astratta BaseRepository
 * 
 * @author Alessandro
 *
 * @param <T>
 */
//@Deprecated
@Repository
public class AppRepository<T> extends BaseRepository<T> implements IRepositoryQueries<T>, IRepositoryCommands<T> {

	//private static final Logger log = Logger.getLogger(AppRepository.class);

	/*
	 * @Deprecated public AppRepository<T> setEntity(Class<T> classEntity) { // TODO
	 * Auto-generated method stub setClass(classEntity); return this;
	 * 
	 * }
	 */

}

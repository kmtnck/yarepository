package it.alessandromodica.product.persistence.repo;

import org.springframework.stereotype.Repository;

import it.alessandromodica.product.persistence.interfaces.IRepositoryCommands;
import it.alessandromodica.product.persistence.interfaces.IRepositoryQueries;

/**
 * 
 * Esempio di repository simile a un jpa springboot ma con le funzionalità
 * YaQueryCompose.
 * 
 * @author Alessandro
 *
 * @param <T>
 */
@Repository
public class AppRepository<T> extends BaseRepository<T> implements IRepositoryQueries<T>, IRepositoryCommands<T> {

}

package it.alessandromodica.product.persistence.interfaces;

import it.alessandromodica.product.persistence.exceptions.RepositoryException;

@Deprecated
public interface IUnitOfWork {

	public void submit(IBulkTransaction instructions) throws RepositoryException;

}

package it.alessandromodica.product.persistence.interfaces;

import it.alessandromodica.product.persistence.exceptions.RepositoryException;

public interface IRepositoryCommands<T, JOIN> {

	public void add(T obj) throws RepositoryException;

	public void delete(T obj) throws RepositoryException;

	public void deleteAll() throws RepositoryException;

	public void deleteFromId(Object id, String nameField) throws RepositoryException;
	
	public void update(T obj) throws RepositoryException;
	
	public void executeTransaction(IBulkTransaction bulkoperation) throws RepositoryException;
}


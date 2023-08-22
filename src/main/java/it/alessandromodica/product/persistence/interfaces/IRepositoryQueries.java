package it.alessandromodica.product.persistence.interfaces;

import java.util.List;

import javax.persistence.criteria.CriteriaQuery;

import it.alessandromodica.product.persistence.exceptions.RepositoryException;
import it.alessandromodica.product.persistence.searcher.YAFilterSerializeCriteria;

public interface IRepositoryQueries<T> {

	public T getById(Object objId, Class<T> classEntity) throws RepositoryException;
	
	public List<T> search(CriteriaQuery<T> criteria) throws RepositoryException;

	public List<T> search(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException;

	public T getSingle(CriteriaQuery<T> criteria) throws RepositoryException ;
	
	public T getSingle(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException;
	
	public T getSingleOrDefault(CriteriaQuery<T> criteria) throws RepositoryException;
	
	public T getSingleOrDefault(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException;
	
	public T getFirst(CriteriaQuery<T> criteria) throws RepositoryException ;
	
	public T getFirst(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException;
	
	public T getFirstOrDefault(CriteriaQuery<T> criteria) throws RepositoryException;
	
	public T getFirstOrDefault(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException;
	
	int getCount(YAFilterSerializeCriteria serializeCriteria) throws RepositoryException;

	Number getMax(String nameField, Class<T> classEntity) throws RepositoryException;

}

package it.alessandromodica.product.persistence.exceptions;

public class RepositoryException extends Exception {

	private static final long serialVersionUID = -3861246110796551546L;

	public RepositoryException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public RepositoryException() {
		// TODO Auto-generated constructor stub

	}

	public RepositoryException(Throwable exception) {
		super(exception);
	}

	public RepositoryException(String message, Throwable exception) {
		super(message, exception);
	}
}

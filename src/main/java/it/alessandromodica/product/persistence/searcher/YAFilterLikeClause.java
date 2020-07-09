package it.alessandromodica.product.persistence.searcher;

/**
 * Classe che rappresenta il criterio di ricerca di tipo like in un canonico
 * database Permette di definire se il like e' di tipo unsensitive, se supportato
 * dal database
 * 
 * @author Alessandro
 *
 */
public class YAFilterLikeClause extends YAFilterBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6553070847079003064L;

	public static final String LIKE_STANDARD = "%";

	private String nameField;
	private String value;
	private boolean insensitive = true;
	
	public String getNameField() {
		return nameField;
	}
	public void setNameField(String nameField) {
		this.nameField = nameField;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean isInsensitive() {
		return insensitive;
	}
	public void setInsensitive(boolean insensitive) {
		this.insensitive = insensitive;
	}

}

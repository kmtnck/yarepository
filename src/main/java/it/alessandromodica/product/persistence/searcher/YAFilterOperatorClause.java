package it.alessandromodica.product.persistence.searcher;

/**
 * Classe che rappresenta il criterio di ricerca di tipo operatore minus, maior
 * e varianti equals.
 * 
 * @author Alessandro
 *
 */
public class YAFilterOperatorClause extends YAFilterBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 387882826647999114L;
/*
	public static final String MINUSEQUALS = "<=";
	public static final String MINUS = "<";
	public static final String MAJOREQUALS = ">=";
	public static final String MAJOR = ">";
	public static final String DISEQUALS = "!=";
*/
	public enum Operators
	{
		minusequals, minus, majorequals, major, disequals
	}
	
	private Class<?> typeData;
	private Object value;
	private String nameField;
	private String operatore;
	
	public Class<?> getTypeData() {
		return typeData;
	}
	public void setTypeData(Class<?> typeData) {
		this.typeData = typeData;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getNameField() {
		return nameField;
	}
	public void setNameField(String nameField) {
		this.nameField = nameField;
	}
	public String getOperatore() {
		return operatore;
	}
	public void setOperatore(String operatore) {
		this.operatore = operatore;
	}

}

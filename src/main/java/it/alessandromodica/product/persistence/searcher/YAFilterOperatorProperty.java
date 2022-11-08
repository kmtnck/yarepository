package it.alessandromodica.product.persistence.searcher;

/**
 * Classe che rappresenta il criterio di ricerca di tipo operatore minus, maior
 * e varianti equals.
 * 
 * @author Alessandro
 *
 */
public class YAFilterOperatorProperty extends YAFilterBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 387882826647999114L;

	public enum Operators
	{
		equals, minusequals, minus, majorequals, major, disequals
	}
	
	private Class<?> typeData;

	private String property1;
	private String property2;
	
	private String operatore;
	
	public Class<?> getTypeData() {
		return typeData;
	}
	public void setTypeData(Class<?> typeData) {
		this.typeData = typeData;
	}

	public String getOperatore() {
		return operatore;
	}
	public void setOperatore(String operatore) {
		this.operatore = operatore;
	}
	public String getProperty1() {
		return property1;
	}
	public void setProperty1(String property1) {
		this.property1 = property1;
	}
	public String getProperty2() {
		return property2;
	}
	public void setProperty2(String property2) {
		this.property2 = property2;
	}

}

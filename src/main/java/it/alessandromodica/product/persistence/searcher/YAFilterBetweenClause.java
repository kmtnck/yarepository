package it.alessandromodica.product.persistence.searcher;

/**
 * Classe che rappresenta il criterio di ricerca between, supportato dalla
 * maggioranza dei database esistenti
 * 
 * @author Alessandro
 *
 */
public class YAFilterBetweenClause extends YAFilterBase {

	/**
	* 
	*/
	private static final long serialVersionUID = -5369911267912281577L;

	private Class<?> typeData;
	private String nameField;
	private Object valueFrom;
	private Object valueTo;

	public Class<?> getTypeData() {
		return typeData;
	}

	public void setTypeData(Class<?> typeData) {
		this.typeData = typeData;
	}

	public String getNameField() {
		return nameField;
	}

	public void setNameField(String nameField) {
		this.nameField = nameField;
	}

	public Object getValueFrom() {
		return valueFrom;
	}

	public void setValueFrom(Object valueFrom) {
		this.valueFrom = valueFrom;
	}

	public Object getValueTo() {
		return valueTo;
	}

	public void setValueTo(Object valueTo) {
		this.valueTo = valueTo;
	}


}

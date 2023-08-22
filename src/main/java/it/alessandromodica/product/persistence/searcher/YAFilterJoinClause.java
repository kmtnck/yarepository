package it.alessandromodica.product.persistence.searcher;

public class YAFilterJoinClause {

	
	Object valueToJoin;
	String entityToJoin;
	String fieldToJoin;

	public Object getValueToJoin() {
		return valueToJoin;
	}

	public void setValueToJoin(Object valueToJoin) {
		this.valueToJoin = valueToJoin;
	}

	public String getEntityToJoin() {
		return entityToJoin;
	}

	public void setEntityToJoin(String entityToJoin) {
		this.entityToJoin = entityToJoin;
	}

	public String getFieldToJoin() {
		return fieldToJoin;
	}

	public void setFieldToJoin(String fieldToJoin) {
		this.fieldToJoin = fieldToJoin;
	}

}

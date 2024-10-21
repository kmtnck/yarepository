package it.alessandromodica.product.persistence.searcher;
public class YAFilterJoinClause {

	public enum Type {
		/** Inner join. */
		INNER,

		/** Left outer join. */
		LEFT,

		/** Right outer join. */
		RIGHT
	}
	
	Object valueToJoin;
	String entityToJoin;
	String fieldToJoin;
	Type typeJoin;
	


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

	public Type getTypeJoin() {
		return typeJoin;
	}

	public void setTypeJoin(Type typeJoin) {
		this.typeJoin = typeJoin;
	}

}

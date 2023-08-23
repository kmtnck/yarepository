package it.alessandromodica.product.persistence.searcher;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import it.alessandromodica.product.persistence.exceptions.RepositoryException;

/**
 * Classe astratta che rappresenta il criterio di ricerca definito in input in
 * una generica richiesta di ricerca sul datastorage. Estende la classe BOBase
 * per poter serializzare in una mappatura i valori. Sono presenti valori
 * statici di tipo stringa utilizzati dal metodo _buildItemClause e dal
 * chiamante BaseRepository. Il metodo _buildItemClause ritorna l'oggetto
 * BOSerializeCritera il quale e' l'unico oggetto riconosciuto dal
 * BaseRepository
 * 
 * @author Alessandro
 *
 */
@SuppressWarnings("rawtypes")
public abstract class YAFilterSearch extends YAFilterBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3897039654760798485L;

	public static final String PROPERTY_1 = "property1";
	public static final String PROPERTY_2 = "property2";
	
	public static final String NAME_FIELD = "nameField";
	public static final String TYPE_DATA = "typeData";
	public static final String VALUE_FIELD = "value";
	public static final String VALUE_FROM = "valueFrom";
	public static final String VALUE_TO = "valueTo";
	public static final String VALUE_INT = "valueInt";
	public static final String VALUE_DOUBLE = "valueDouble";
	public static final String VALUE_DATE = "valueDate";

	public static final String VALUE_TO_DOUBLE = "valueToDouble";
	public static final String VALUE_FROM_DOUBLE = "valueFromDouble";
	public static final String VALUE_FROM_INT = "valueFromInt";
	public static final String VALUE_TO_INT = "valueToInt";
	public static final String VALUE_TO_DATE = "valueToDate";
	public static final String VALUE_FROM_DATE = "valueFromDate";

	private Class<?> classEntity;

	private Map<String, Object> listEquals = new HashMap<String, Object>();

	private List<YAFilterJoinClause> listJoinClause = new ArrayList<YAFilterJoinClause>();

	private List<String> listFieldsProjection = new ArrayList<String>();
	private List<String> listExcludeProjection = new ArrayList<String>();

	private List<YAFilterBetweenClause> listBetweenClause = new ArrayList<YAFilterBetweenClause>();
	private List<YAFilterLikeClause> listLikeClause = new ArrayList<YAFilterLikeClause>();
	private List<YAFilterLikeClause> listNotLikeClause = new ArrayList<YAFilterLikeClause>();
	private List<YAFilterOperatorClause> listOperatorClause = new ArrayList<YAFilterOperatorClause>();
	private List<YAFilterOperatorProperty> listOperatorProperty = new ArrayList<YAFilterOperatorProperty>();

	@Deprecated
	private boolean descendent = false;
	private Map<String, Boolean> mapDescendent = new HashMap<String, Boolean>();

	private List<String> listEntityGraph = new ArrayList<String>();

	private List<String> listOrderBy = new ArrayList<String>();
	private List<String> listIsNull = new ArrayList<String>();
	private List<String> listIsNotNull = new ArrayList<String>();
	private List<String> listIsNotEmpty = new ArrayList<String>();
	private List<String> listIsZero = new ArrayList<String>();
	private List<YAFilterSearchApp> listOrClause = new ArrayList<YAFilterSearchApp>();
	private Map<String, Boolean> listValueBool = new HashMap<String, Boolean>();
	private Map<String, Object[]> listIn = new HashMap<String, Object[]>();
	private Map<String, Object[]> listNotIn = new HashMap<String, Object[]>();

	private int maxResult;
	private int firstResult;
	private boolean not;

	@Deprecated
	public YAFilterSearch() {

	}

	public YAFilterSearch(Class<?> classEntity) {
		this.classEntity = classEntity;
	}

	public static YAFilterSerializeCriteria getEmptyFilter(Class<?> classEntity)
	{
		YAFilterSerializeCriteria empty = new YAFilterSerializeCriteria();
		empty.setClassEntity(classEntity);
		return empty;
	}
	
	public static void setClauseInList(String nameField, Object[] data, YAFilterSearch searcher) {
		if (data != null && data.length > 0)
			searcher.getListIn().put(nameField, data);
	}

	public static void setClauseNotInList(String nameField, Object[] data, YAFilterSearch searcher) {
		if (data != null && data.length > 0)
			searcher.getListNotIn().put(nameField, data);
	}

	public static void setLikeClause(String value, String nameField, YAFilterSearch searcher) {
		setLikeClause(value, nameField, false, searcher);
	}
	
	public static void setLikeClause(String value, String nameField, boolean insensitive, YAFilterSearch searcher) {
		if (StringUtils.isNotBlank(value)) {
			YAFilterLikeClause likeCl = new YAFilterLikeClause();
			likeCl.setNameField(nameField);
			likeCl.setValue("%" + value + "%");
			searcher.getListLikeClause().add(likeCl);
		}
	}	
	
	public static void setNotLikeClause(String value, String nameField, YAFilterSearch searcher) {
		if (StringUtils.isNotBlank(value)) {
			YAFilterLikeClause likeCl = new YAFilterLikeClause();
			likeCl.setNameField(nameField);
			likeCl.setValue("%" + value + "%");
			searcher.getListNotLikeClause().add(likeCl);
		}
	}	

	public static void setBetweenClause(Object valueFrom, Object valueTo, String nameField, YAFilterSearch searcher,
			Class<?> typeData) {
		if (valueFrom != null) {
			YAFilterBetweenClause btw = new YAFilterBetweenClause();
			btw.setNameField(nameField);
			btw.setValueFrom(valueFrom);
			btw.setValueTo(valueTo);
			btw.setTypeData(typeData);
			searcher.getListBetweenClause().add(btw);
		}
	}
	
	public static void setFilterOperatorProperty(String property1, String property2, YAFilterOperatorProperty.Operators operator, Class<?> typeData, YAFilterSearch searcher)
	{
		YAFilterOperatorProperty filter = new YAFilterOperatorProperty();
		filter.setProperty1(property1);
		filter.setProperty2(property2);
		filter.setTypeData(typeData);
		filter.setOperatore(operator.name());
		searcher.getListOperatorProperty().add(filter);
	}	

	public YAFilterSerializeCriteria getSerialized() throws RepositoryException {
		return _buildItemClause(this);
	}

	private static YAFilterSerializeCriteria _buildItemClause(YAFilterSearch searcher) throws RepositoryException {

		YAFilterSerializeCriteria result = new YAFilterSerializeCriteria();

		result.setNot(searcher.isNot());
		
		result.setClassEntity(searcher.getClassEntity());
		// uguaglianze
		result.setListEquals(_serializeBusinessClause(searcher));

		result.getListEquals().putAll(searcher.getListEquals());

		result.setListEntityGraph(searcher.getListEntityGraph());

		result.setListJoinClause(searcher.getListJoinClause());

		// between
		for (YAFilterBetweenClause cBT : searcher.getListBetweenClause()) {
			Map<String, Object> serializeBT = new HashMap<String, Object>();

			serializeBT.put(NAME_FIELD, cBT.getNameField());
			serializeBT.put(TYPE_DATA, cBT.getTypeData());
			serializeBT.put(VALUE_FROM, cBT.getValueFrom());
			serializeBT.put(VALUE_TO, cBT.getValueTo());

			result.getListbetween().add(serializeBT);
		}

		for (YAFilterLikeClause cLike : searcher.getListLikeClause()) {
			if (cLike.isInsensitive())
				result.getListLikeInsensitive().add(_serializeBusinessClause(cLike));
			else
				result.getListLike().add(_serializeBusinessClause(cLike));
		}
		
		for (YAFilterLikeClause cLike : searcher.getListNotLikeClause()) {
			if (cLike.isInsensitive())
				result.getListNotLikeInsensitive().add(_serializeBusinessClause(cLike));
			else
				result.getListNotLike().add(_serializeBusinessClause(cLike));
		}		

		for (YAFilterOperatorClause cOper : searcher.getListOperatorClause()) {
			Map<String, Object> serOper = _serializeBusinessClause(cOper);
			/*
			 * if (cOper.get_valueDouble() == 0 && cOper.get_valueInt() == 0) {
			 * serOper.put(VALUE_INT, 0); }
			 */

			result.getListOperator().add(serOper);
		}
		
		for (YAFilterOperatorProperty cOper : searcher.getListOperatorProperty()) {
			Map<String, Object> serOper = _serializeBusinessClause(cOper);
			result.getListOperatorProperty().add(serOper);
		}		

		for (String cIsNull : searcher.getListIsNull()) {
			result.getListIsNull().add(cIsNull);
		}

		for (String cIsNotNull : searcher.getListIsNotNull()) {
			result.getListIsNotNull().add(cIsNotNull);
		}

		for (String cIsZero : searcher.getListIsZero()) {
			result.getListIsZero().add(cIsZero);
		}

		for (String cIsZero : searcher.getListIsNotEmpty()) {
			result.getListIsNotEmpty().add(cIsZero);
		}

		for (String cOrderBy : searcher.getListOrderBy()) {
			result.getListOrderBy().add(cOrderBy);
		}

		for (Map.Entry<String, Boolean> cValueDesc : searcher.getMapDescendent().entrySet()) {
			result.getMapDescendent().put(cValueDesc.getKey(), cValueDesc.getValue());
		}

		for (String cValueBool : searcher.getListValueBool().keySet()) {
			result.getListValueBool().put(cValueBool, searcher.getListValueBool().get(cValueBool));
		}

		for (String cIn : searcher.getListIn().keySet()) {
			result.getListIn().put(cIn, searcher.getListIn().get(cIn));
		}

		for (String cNotIn : searcher.getListNotIn().keySet()) {
			result.getListNotIn().put(cNotIn, searcher.getListNotIn().get(cNotIn));
		}

		if (searcher.isDescendent()) {
			result.setDescendent(searcher.isDescendent());
		}

		for (YAFilterSearch cOr : searcher.getListOrClause()) {
			if (cOr.getListOrderBy().size() > 0)
				throw new RepositoryException(
						"Non e' permesso impostare criteri di ordinamento in una clausola OR. Utilizzare l'istanza BOSearch principale per impostare l'oggetto ListOrderBy");

			YAFilterSerializeCriteria orSerialized = _buildItemClause(cOr);
			result.getListOrClause().add(orSerialized);
		}

		// La strategia di esclusione field dalla projections e' includere tutti quelli
		// non presenti in questa lista
		if (searcher.getListExcludeProjection().size() > 0) {

			Class<?> classEntity = searcher.getClassEntity();

			if (classEntity == null)
				throw new RepositoryException(
						"E' stato definito un set di campi da escludere in proiezione, ma non e' stata indicata la classe entita di riferimento");

			Field[] fieldsclass = classEntity.getDeclaredFields();
			Field[] fieldsuperclass = classEntity.getSuperclass().getDeclaredFields();

			int aLen = fieldsclass.length;
			int bLen = fieldsuperclass.length;
			Field[] fields = new Field[aLen + bLen];

			System.arraycopy(fieldsclass, 0, fields, 0, aLen);
			System.arraycopy(fieldsuperclass, 0, fields, aLen, bLen);

			for (Object cobj : fields) {
				Field cfield = (Field) cobj;

				boolean isStaticField = java.lang.reflect.Modifier.isStatic(cfield.getModifiers())
						&& java.lang.reflect.Modifier.isFinal(cfield.getModifiers());
				if (isStaticField)
					continue;

				String cName = cfield.getName();
				boolean canInclude = true;
				for (String exclude : searcher.getListExcludeProjection()) {
					if (cName.equals(exclude)) {
						canInclude = false;
						break;
					}
				}
				if (canInclude && !cName.equals("version"))
					result.getListFieldsProjection().add(cName);
			}
		} else {
			for (String cField : searcher.getListFieldsProjection()) {
				result.getListFieldsProjection().add(cField);
			}
		}

		if (searcher.getMaxResult() > 0) {
			result.setMaxResult(searcher.getMaxResult());
			result.setFirstResult(searcher.getFirstResult());
		}

		return result;
	}

	private static Map<String, Object> _serializeBusinessClause(Object searcher) throws RepositoryException {
		Map<String, Object> effective = new HashMap<String, Object>(0);

		Map<String, Object> resultSearch = toDictionary(searcher);
		for (Iterator keys = resultSearch.entrySet().iterator(); keys.hasNext();) {
			Entry cEntry = (Entry) keys.next();
			String cKey = cEntry.getKey().toString();

			effective.put(cKey, resultSearch.get(cKey));
		}

		return effective;
	}

	public abstract boolean checkCompositeId(String nameProperty);

	public int getMaxResult() {
		return maxResult;
	}

	public void setMaxResult(int maxResult) {
		this.maxResult = maxResult;
	}

	public int getFirstResult() {
		return firstResult;
	}

	public void setFirstResult(int firstResult) {
		this.firstResult = firstResult;
	}

	public Map<String, Boolean> getMapDescendent() {
		return mapDescendent;
	}

	public void setMapDescendent(Map<String, Boolean> mapDescendent) {
		this.mapDescendent = mapDescendent;
	}

	public boolean isDescendent() {
		return descendent;
	}

	public void setDescendent(boolean descendent) {
		this.descendent = descendent;
	}

	public List<String> getListFieldsProjection() {
		return listFieldsProjection;
	}

	public void setListFieldsProjection(List<String> listFieldsProjection) {
		this.listFieldsProjection = listFieldsProjection;
	}

	public List<String> getListExcludeProjection() {
		return listExcludeProjection;
	}

	public void setListExcludeProjection(List<String> listExcludeProjection) {
		this.listExcludeProjection = listExcludeProjection;
	}

	public List<YAFilterBetweenClause> getListBetweenClause() {
		return listBetweenClause;
	}

	public void setListBetweenClause(List<YAFilterBetweenClause> listBetweenClause) {
		this.listBetweenClause = listBetweenClause;
	}

	public List<YAFilterLikeClause> getListLikeClause() {
		return listLikeClause;
	}

	public void setListLikeClause(List<YAFilterLikeClause> listLikeClause) {
		this.listLikeClause = listLikeClause;
	}

	public List<YAFilterOperatorClause> getListOperatorClause() {
		return listOperatorClause;
	}

	public void setListOperatorClause(List<YAFilterOperatorClause> listOperatorClause) {
		this.listOperatorClause = listOperatorClause;
	}

	public List<String> getListOrderBy() {
		return listOrderBy;
	}

	public void setListOrderBy(List<String> listOrderBy) {
		this.listOrderBy = listOrderBy;
	}

	public List<String> getListIsNull() {
		return listIsNull;
	}

	public void setListIsNull(List<String> listIsNull) {
		this.listIsNull = listIsNull;
	}

	public List<String> getListIsNotNull() {
		return listIsNotNull;
	}

	public void setListIsNotNull(List<String> listIsNotNull) {
		this.listIsNotNull = listIsNotNull;
	}

	public List<String> getListIsNotEmpty() {
		return listIsNotEmpty;
	}

	public void setListIsNotEmpty(List<String> listIsNotEmpty) {
		this.listIsNotEmpty = listIsNotEmpty;
	}

	public List<String> getListIsZero() {
		return listIsZero;
	}

	public void setListIsZero(List<String> listIsZero) {
		this.listIsZero = listIsZero;
	}

	public List<YAFilterSearchApp> getListOrClause() {
		return listOrClause;
	}

	public void setListOrClause(List<YAFilterSearchApp> listOrClause) {
		this.listOrClause = listOrClause;
	}

	public Map<String, Boolean> getListValueBool() {
		return listValueBool;
	}

	public void setListValueBool(Map<String, Boolean> listValueBool) {
		this.listValueBool = listValueBool;
	}

	public Map<String, Object[]> getListIn() {
		return listIn;
	}

	public void setListIn(Map<String, Object[]> listIn) {
		this.listIn = listIn;
	}

	public Map<String, Object[]> getListNotIn() {
		return listNotIn;
	}

	public void setListNotIn(Map<String, Object[]> listNotIn) {
		this.listNotIn = listNotIn;
	}

	public Map<String, Object> getListEquals() {
		return listEquals;
	}

	public void setListEquals(Map<String, Object> listEquals) {
		this.listEquals = listEquals;
	}

	public List<String> getListEntityGraph() {
		return listEntityGraph;
	}

	public void setListEntityGraph(List<String> listEntityGraph) {
		this.listEntityGraph = listEntityGraph;
	}

	public List<YAFilterJoinClause> getListJoinClause() {
		return listJoinClause;
	}

	public void setListJoinClause(List<YAFilterJoinClause> listJoinClause) {
		this.listJoinClause = listJoinClause;
	}

	public Class<?> getClassEntity() {
		return classEntity;
	}

	public void setClassEntity(Class<?> classEntity) {
		this.classEntity = classEntity;
	}

	public boolean isNot() {
		return not;
	}

	public void setNot(boolean not) {
		this.not = not;
	}

	public List<YAFilterLikeClause> getListNotLikeClause() {
		return listNotLikeClause;
	}

	public void setListNotLikeClause(List<YAFilterLikeClause> listNotLikeClause) {
		this.listNotLikeClause = listNotLikeClause;
	}

	public List<YAFilterOperatorProperty> getListOperatorProperty() {
		return listOperatorProperty;
	}

	public void setListOperatorProperty(List<YAFilterOperatorProperty> listOperatorProperty) {
		this.listOperatorProperty = listOperatorProperty;
	}
}

package it.alessandromodica.product.persistence.searcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * La classe rappresenta l'oggetto dato in pasto al repository per poter generare la query HQL hibernate in modo automatico.
 * Il repository di fatto riconosce unicamente questa classe, disaccoppiando la logica definita nel BOSearcher.
 * L'oggetto e' ritornato dal metodo privato _buildItemsClause presente nel BOSearcher.
 * 
 * @author Alessandro
 *
 */
@SuppressWarnings("rawtypes")
@JsonIgnoreProperties(ignoreUnknown = true)
public class YAFilterSerializeCriteria implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2282917157007071284L;


	private Class classEntity;
	

	private List<String> listEntityGraph = new ArrayList<String>();
	private List<YAFilterJoinClause> listJoinClause = new ArrayList<YAFilterJoinClause>();

	
	private Map<String, Object> listEquals = new HashMap<String, Object>();
	private List<Map<String, Object>> listbetween = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> listLike = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> listLikeInsensitive = new ArrayList<Map<String, Object>>();

	private List<Map<String, Object>> listNotLike = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> listNotLikeInsensitive = new ArrayList<Map<String, Object>>();
	
	private List<Map<String, Object>> listOperator = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> listOperatorProperty = new ArrayList<Map<String, Object>>();
	private List<String> listOrderBy = new ArrayList<String>();
	private List<String> listIsNull = new ArrayList<String>();
	private List<String> listIsNotNull = new ArrayList<String>();
	private List<String> listIsZero = new ArrayList<String>();
	private List<String> listIsNotEmpty = new ArrayList<String>();
	private List<String> listLower = new ArrayList<String>();;
	private Map<String, Boolean> listValueBool = new HashMap<String, Boolean>();
	private List<YAFilterSerializeCriteria> listOrClause = new ArrayList<YAFilterSerializeCriteria>();
	private List<YAFilterSerializeCriteria> listAndClause = new ArrayList<YAFilterSerializeCriteria>();
	private Boolean descendent = false;
	private Map<String,Boolean> mapDescendent = new HashMap<String,Boolean>();

	private int maxResult;
	private int firstResult;
	private boolean not;
	private boolean distinct;

	private List<String> listFieldsProjection = new ArrayList<String>();
	private Map<String, Object[]> listIn = new HashMap<String, Object[]>();
	private Map<String, Object[]> listNotIn = new HashMap<String, Object[]>();

	public Map<String, Object> getListEquals() {
		return listEquals;
	}

	public void setListEquals(Map<String, Object> listEquals) {
		this.listEquals = listEquals;
	}

	public List<Map<String, Object>> getListbetween() {
		return listbetween;
	}

	public void setListbetween(List<Map<String, Object>> listbetween) {
		this.listbetween = listbetween;
	}

	public List<Map<String, Object>> getListLike() {
		return listLike;
	}

	public void setListLike(List<Map<String, Object>> listLike) {
		this.listLike = listLike;
	}

	public List<Map<String, Object>> getListLikeInsensitive() {
		return listLikeInsensitive;
	}

	public void setListLikeInsensitive(
			List<Map<String, Object>> listLikeInsensitive) {
		this.listLikeInsensitive = listLikeInsensitive;
	}

	public List<Map<String, Object>> getListOperator() {
		return listOperator;
	}

	public void setListOperator(List<Map<String, Object>> listOperator) {
		this.listOperator = listOperator;
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

	public List<String> getListIsZero() {
		return listIsZero;
	}

	public void setListIsZero(List<String> listIsZero) {
		this.listIsZero = listIsZero;
	}

	public List<YAFilterSerializeCriteria> getListOrClause() {
		return listOrClause;
	}

	public void setListOrClause(List<YAFilterSerializeCriteria> listOrClause) {
		this.listOrClause = listOrClause;
	}

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

	public Map<String, Boolean> getMapDescendent() {
		return mapDescendent;
	}

	public void setMapDescendent(Map<String, Boolean> mapDescendent) {
		this.mapDescendent = mapDescendent;
	}

	public Boolean getDescendent() {
		return descendent;
	}

	public void setDescendent(Boolean descendent) {
		this.descendent = descendent;
	}

	public List<String> getListFieldsProjection() {
		return listFieldsProjection;
	}

	public void setListFieldsProjection(List<String> listFieldsProjection) {
		this.listFieldsProjection = listFieldsProjection;
	}

	public List<String> getListIsNotEmpty() {
		return listIsNotEmpty;
	}

	public void setListIsNotEmpty(List<String> listIsNotEmpty) {
		this.listIsNotEmpty = listIsNotEmpty;
	}

	public Map<String, Boolean> getListValueBool() {
		return listValueBool;
	}

	public void setListValueBool(Map<String, Boolean> listValueBool) {
		this.listValueBool = listValueBool;
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

	public Class getClassEntity() {
		return classEntity;
	}

	public void setClassEntity(Class classEntity) {
		this.classEntity = classEntity;
	}

	public boolean isNot() {
		return not;
	}

	public void setNot(boolean not) {
		this.not = not;
	}

	public List<Map<String, Object>> getListNotLike() {
		return listNotLike;
	}

	public void setListNotLike(List<Map<String, Object>> listNotLike) {
		this.listNotLike = listNotLike;
	}

	public List<Map<String, Object>> getListNotLikeInsensitive() {
		return listNotLikeInsensitive;
	}

	public void setListNotLikeInsensitive(List<Map<String, Object>> listNotLikeInsensitive) {
		this.listNotLikeInsensitive = listNotLikeInsensitive;
	}

	public List<Map<String, Object>> getListOperatorProperty() {
		return listOperatorProperty;
	}

	public void setListOperatorProperty(List<Map<String, Object>> listOperatorProperty) {
		this.listOperatorProperty = listOperatorProperty;
	}

	public List<YAFilterSerializeCriteria> getListAndClause() {
		return listAndClause;
	}

	public void setListAndClause(List<YAFilterSerializeCriteria> listAndClause) {
		this.listAndClause = listAndClause;
	}

	public List<String> getListLower() {
		return listLower;
	}

	public void setListLower(List<String> listLower) {
		this.listLower = listLower;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}
}

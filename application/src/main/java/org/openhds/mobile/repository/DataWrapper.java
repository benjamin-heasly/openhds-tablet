package org.openhds.mobile.repository;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic representation of a result from any query.
 *
 * Facilitates generic lists and views of results, for example at various levels of
 * hierarchy navigation.  But it's up to the caller to interpret the QueryResult
 * correctly, for example using the extId and "category" (i.e. hierarchy level).
 *
 * Payloads may contain arbitrary data, for example to display with result name and extId.
 *
 * BSH
 */
public class DataWrapper {

	private String category;
	private String extId;
	private String name;
	private Map<Integer, String> stringsPayload = new HashMap<Integer, String>();
	private Map<Integer, Integer> stringIdsPayload = new HashMap<Integer, Integer>();

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getExtId() {
		return extId;
	}

	public void setExtId(String extId) {
		this.extId = extId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<Integer, String> getStringsPayload() {
		return stringsPayload;
	}
	
	public Map<Integer, Integer> getStringIdsPayload() {
		return stringIdsPayload;
	}

	@Override
	public String toString() {
		return "QueryResult[name: " + name + " extId: " + extId + " category: "
				+ category + " + payload size: " + stringsPayload.size() + "]";
	}
}

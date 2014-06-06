package org.openhds.mobile.projectdata;

import java.util.List;

import org.openhds.mobile.database.queries.QueryResult;

import android.content.ContentResolver;

public interface QueryHelper {
	public List<QueryResult> getAll(ContentResolver contentResolver,
			String state);

	public List<QueryResult> getChildren(ContentResolver contentResolver,
			QueryResult qr, String childState);

	public QueryResult getIfExists(ContentResolver contentResolver,
			String state, String extId);
}

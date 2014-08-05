package org.openhds.mobile.database;


import static org.openhds.mobile.OpenHDS.Visits.CONTENT_ID_URI_BASE;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_DATE;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_EXTID;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_FIELDWORKER_EXTID;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_LOCATION_EXTID;

import java.util.Map;

import org.openhds.mobile.model.Visit;
import org.openhds.mobile.projectdata.ProjectFormFields;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

public class VisitAdapter {

	private static ContentValues buildContentValues(Visit visit) {

		ContentValues cv = new ContentValues();

		cv.put(COLUMN_VISIT_EXTID, visit.getVisitExtId());
		cv.put(COLUMN_VISIT_FIELDWORKER_EXTID, visit.getFieldWorkerExtid());
		cv.put(COLUMN_VISIT_DATE, visit.getVisitDate());
		cv.put(COLUMN_VISIT_LOCATION_EXTID, visit.getLocationExtId());

		return cv;

	}

	public static Visit create(Map<String, String> formInstanceData){
		Visit visit = new Visit();
		
		visit.setVisitExtId(formInstanceData.get(ProjectFormFields.Visits
				.getFieldNameFromColumn(COLUMN_VISIT_EXTID)));
		visit.setVisitDate(formInstanceData.get(ProjectFormFields.Visits
				.getFieldNameFromColumn(COLUMN_VISIT_DATE)));
		visit.setFieldWorkerExtid(formInstanceData.get(ProjectFormFields.Visits
				.getFieldNameFromColumn(COLUMN_VISIT_FIELDWORKER_EXTID)));
		visit.setLocationExtId(formInstanceData.get(ProjectFormFields.Visits
				.getFieldNameFromColumn(COLUMN_VISIT_LOCATION_EXTID)));
		
		return visit;
		
	}
	
	public static Uri insert(ContentResolver resolver, Visit visit) {
		ContentValues cv = buildContentValues(visit);

		return resolver.insert(CONTENT_ID_URI_BASE, cv);
	}
}

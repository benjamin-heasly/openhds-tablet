package org.openhds.mobile.database;

import static org.openhds.mobile.OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A;
import static org.openhds.mobile.OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B;
import static org.openhds.mobile.OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE;
import static org.openhds.mobile.OpenHDS.Relationships.COLUMN_RELATIONSHIP_TYPE;
import static org.openhds.mobile.OpenHDS.Relationships.CONTENT_ID_URI_BASE;

import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.model.Relationship;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

public class RelationshipAdapter {

	public static Relationship create(Individual a, Individual b, String type,
			String startDate) {
		Relationship relationship = new Relationship();
		relationship.setIndividualA(a.getExtId());
		relationship.setIndividualB(b.getExtId());
		relationship.setType(type);
		relationship.setStartDate(startDate);
		return relationship;
	}

	private static ContentValues buildContentValues(Relationship relationship) {

		ContentValues cv = new ContentValues();

		cv.put(COLUMN_RELATIONSHIP_INDIVIDUAL_A, relationship.getIndividualA());
		cv.put(COLUMN_RELATIONSHIP_INDIVIDUAL_B, relationship.getIndividualB());
		cv.put(COLUMN_RELATIONSHIP_STARTDATE, relationship.getStartDate());
		cv.put(COLUMN_RELATIONSHIP_TYPE, relationship.getType());

		return cv;
	}

	public static Uri insert(ContentResolver resolver, Relationship relationship) {

		ContentValues cv = buildContentValues(relationship);

		return resolver.insert(CONTENT_ID_URI_BASE, cv);
	}

	public static int update(ContentResolver resolver, Relationship relationship) {

		ContentValues cv = buildContentValues(relationship);

		return resolver.update(
				CONTENT_ID_URI_BASE,
				cv,
				COLUMN_RELATIONSHIP_INDIVIDUAL_A + " = '"
						+ relationship.getIndividualA() + "' AND "
						+ COLUMN_RELATIONSHIP_INDIVIDUAL_B + " = '"
						+ relationship.getIndividualB() + "' ", null);
	}

	public static boolean insertOrUpdate(ContentResolver resolver,
			Relationship relationship) {

		if (!Queries.hasRelationshipByBothIndividuals(resolver,
				relationship.getIndividualA(), relationship.getIndividualB())) {
			return (null != insert(resolver, relationship));
		} else {
			return (update(resolver, relationship) > 0);

		}
	}
}

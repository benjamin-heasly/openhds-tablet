package org.openhds.mobile.database;

import static org.openhds.mobile.OpenHDS.Memberships.COLUMN_INDIVIDUAL_EXTID;
import static org.openhds.mobile.OpenHDS.Memberships.COLUMN_MEMBERSHIP_RELATIONSHIP_TO_HEAD;
import static org.openhds.mobile.OpenHDS.Memberships.COLUMN_MEMBERSHIP_STATUS;
import static org.openhds.mobile.OpenHDS.Memberships.COLUMN_SOCIAL_GROUP_EXTID;
import static org.openhds.mobile.OpenHDS.Memberships.CONTENT_ID_URI_BASE;

import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.model.SocialGroup;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

public class MembershipAdapter {

	public static Membership create(Individual individual, SocialGroup socialGroup,
			String relationshipToHead, String status) {
		Membership membership = new Membership();
		membership.setIndividualExtId(individual.getExtId());
		membership.setSocialGroupExtId(socialGroup.getExtId());
		membership.setRelationshipToHead(relationshipToHead);
		membership.setStatus(status);

		return membership;
	}

	public static Uri insert(ContentResolver resolver, Membership membership) {
		ContentValues cv = new ContentValues();

		cv.put(COLUMN_INDIVIDUAL_EXTID, membership.getIndividualExtId());
		cv.put(COLUMN_MEMBERSHIP_RELATIONSHIP_TO_HEAD, membership.getRelationshipToHead());
		cv.put(COLUMN_SOCIAL_GROUP_EXTID, membership.getSocialGroupExtId());
		cv.put(COLUMN_MEMBERSHIP_STATUS, membership.getStatus());

		return resolver.insert(CONTENT_ID_URI_BASE, cv);
	}

}

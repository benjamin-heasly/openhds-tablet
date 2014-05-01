package org.openhds.mobile.database;

import static org.openhds.mobile.OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_EXTID;
import static org.openhds.mobile.OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_HEAD_INDIVIDUAL_EXTID;
import static org.openhds.mobile.OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_NAME;
import static org.openhds.mobile.OpenHDS.SocialGroups.CONTENT_ID_URI_BASE;

import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.SocialGroup;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

public class SocialGroupAdapter {

	public final static String HOUSE_OF_PREFIX = "House of ";

	public static SocialGroup create(String extId, Individual head) {
		SocialGroup socialGroup = new SocialGroup();
		socialGroup.setExtId(extId);
		socialGroup.setGroupHead(head.getExtId());
		socialGroup.setGroupName(HOUSE_OF_PREFIX + head.getLastName());
		return socialGroup;
	}

	public static int update(ContentResolver resolver, SocialGroup socialGroup) {
		ContentValues cv = new ContentValues();

		cv.put(COLUMN_SOCIAL_GROUP_EXTID, socialGroup.getExtId());
		cv.put(COLUMN_SOCIAL_GROUP_HEAD_INDIVIDUAL_EXTID, socialGroup.getGroupHead());
		cv.put(COLUMN_SOCIAL_GROUP_NAME, socialGroup.getGroupName());

		return resolver.update(CONTENT_ID_URI_BASE, cv,
				COLUMN_SOCIAL_GROUP_EXTID + " = '" + socialGroup.getExtId() + "'", null);
	}

	public static Uri insert(ContentResolver resolver, SocialGroup socialGroup) {
		ContentValues cv = new ContentValues();

		cv.put(COLUMN_SOCIAL_GROUP_EXTID, socialGroup.getExtId());
		cv.put(COLUMN_SOCIAL_GROUP_HEAD_INDIVIDUAL_EXTID, socialGroup.getGroupHead());
		cv.put(COLUMN_SOCIAL_GROUP_NAME, socialGroup.getGroupName());

		return resolver.insert(CONTENT_ID_URI_BASE, cv);
	}
}

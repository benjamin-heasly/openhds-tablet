package org.openhds.mobile.repository.gateway;

import android.content.ContentValues;
import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.repository.Converter;

import static org.openhds.mobile.OpenHDS.Memberships.COLUMN_INDIVIDUAL_EXTID;
import static org.openhds.mobile.OpenHDS.Memberships.COLUMN_MEMBERSHIP_RELATIONSHIP_TO_HEAD;
import static org.openhds.mobile.OpenHDS.Memberships.COLUMN_SOCIAL_GROUP_EXTID;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert Membership to and from database.  Membership-specific queries.
 *
 * Uses the extId of the individual as the id for the Membership.
 *
 */
public class MembershipGateway extends Gateway<Membership> {

    public MembershipGateway() {
        super(OpenHDS.Memberships.CONTENT_ID_URI_BASE, COLUMN_INDIVIDUAL_EXTID, new MembershipConverter());
    }

    private static class MembershipConverter implements Converter<Membership> {

        @Override
        public Membership fromCursor(Cursor cursor) {
            Membership membership = new Membership();

            membership.setIndividualExtId(extractString(cursor, COLUMN_INDIVIDUAL_EXTID));
            membership.setSocialGroupExtId(extractString(cursor, COLUMN_SOCIAL_GROUP_EXTID));
            membership.setRelationshipToHead(extractString(cursor, COLUMN_MEMBERSHIP_RELATIONSHIP_TO_HEAD));

            return membership;
        }

        @Override
        public ContentValues toContentValues(Membership membership) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_INDIVIDUAL_EXTID, membership.getIndividualExtId());
            contentValues.put(COLUMN_SOCIAL_GROUP_EXTID, membership.getSocialGroupExtId());
            contentValues.put(COLUMN_MEMBERSHIP_RELATIONSHIP_TO_HEAD, membership.getRelationshipToHead());

            return contentValues;
        }

        @Override
        public String getId(Membership membership) {
            return membership.getIndividualExtId();
        }
    }
}

package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.model.core.Membership;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import java.util.Set;

import static org.openhds.mobile.OpenHDS.Memberships.INDIVIDUAL_UUID;
import static org.openhds.mobile.OpenHDS.Memberships.LAST_MODIFIED_CLIENT;
import static org.openhds.mobile.OpenHDS.Memberships.LAST_MODIFIED_SERVER;
import static org.openhds.mobile.OpenHDS.Memberships.SOCIAL_GROUP_UUID;
import static org.openhds.mobile.OpenHDS.Memberships.UUID;
import static org.openhds.mobile.repository.RepositoryUtils.EQUALS;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;
import static org.openhds.mobile.repository.RepositoryUtils.insert;
import static org.openhds.mobile.repository.RepositoryUtils.update;

/**
 * Convert Membership to and from database.  Membership-specific queries.
 *
 */
public class MembershipGateway extends Gateway<Membership> {

    public MembershipGateway() {
        super(OpenHDS.Memberships.CONTENT_ID_URI_BASE, UUID, new MembershipConverter());
    }

    @Override
    public Set<String> getColumns() {
        return converter.toContentValues(new Membership()).keySet();
    }

    // take care to update at the correct social group AND individual
    @Override
    public Membership insertOrUpdate(ContentResolver contentResolver, Membership membership) {
        ContentValues contentValues = toContentValues(membership);

        String socialGroupId = membership.getSocialGroupUuid();
        String individualId = membership.getIndividualUuid();
        Membership existingMembership = getFirst(contentResolver,
                findBySocialGroupAndIndividual(socialGroupId, individualId));

        String id = contentValues.getAsString(idColumnName);
        if (null == id || id.trim().isEmpty()) {
            id = java.util.UUID.randomUUID().toString();
            contentValues.put(idColumnName, id);
        }

        if (null == existingMembership) {
            insert(contentResolver, tableUri, contentValues);
        } else {
            final String[] columnNames = {SOCIAL_GROUP_UUID, INDIVIDUAL_UUID};
            final String[] columnValues = {socialGroupId, individualId};
            update(contentResolver, tableUri, contentValues, columnNames, columnValues);
        }

        return getFirst(contentResolver, findBySocialGroupAndIndividual(socialGroupId, individualId));
    }

    public Query findByIndividual(String individualId) {
        return new Query(tableUri, INDIVIDUAL_UUID, individualId, INDIVIDUAL_UUID);
    }

    public Query findBySocialGroupAndIndividual(String socialGroupId, String individualId) {
        final String[] columnNames = {SOCIAL_GROUP_UUID, INDIVIDUAL_UUID};
        final String[] columnValues = {socialGroupId, individualId};
        return new Query(tableUri, columnNames, columnValues, INDIVIDUAL_UUID, EQUALS);
    }

    private static class MembershipConverter implements Converter<Membership> {

        @Override
        public Membership fromCursor(Cursor cursor) {
            Membership membership = new Membership();

            membership.setUuid(extractString(cursor, UUID));
            membership.setIndividualUuid(extractString(cursor, INDIVIDUAL_UUID));
            membership.setSocialGroupUuid(extractString(cursor, SOCIAL_GROUP_UUID));
            membership.setLastModifiedClient(extractString(cursor, LAST_MODIFIED_CLIENT));
            membership.setLastModifiedServer(extractString(cursor, LAST_MODIFIED_SERVER));

            return membership;
        }

        @Override
        public ContentValues toContentValues(Membership membership) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(UUID, membership.getUuid());
            contentValues.put(INDIVIDUAL_UUID, membership.getIndividualUuid());
            contentValues.put(SOCIAL_GROUP_UUID, membership.getSocialGroupUuid());
            contentValues.put(LAST_MODIFIED_CLIENT, membership.getLastModifiedClient());
            contentValues.put(LAST_MODIFIED_SERVER, membership.getLastModifiedServer());

            return contentValues;
        }

        @Override
        public ContentValues toContentValues(FormContent formContent, String entityAlias) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(INDIVIDUAL_UUID, formContent.getContentString(FormContent.TOP_LEVEL_ALIAS, "individualUuid"));
            contentValues.put(SOCIAL_GROUP_UUID, formContent.getContentString(FormContent.TOP_LEVEL_ALIAS, "socialGroupUuid"));
            contentValues.put(UUID, formContent.getContentString(entityAlias, UUID));

            return contentValues;
        }

        @Override
        public String getDefaultAlias() {
            return "membership";
        }


        @Override
        public String getId(Membership membership) {
            return membership.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, Membership entity, String level) {
            return new DataWrapper(entity.getUuid(),
                    entity.getIndividualUuid(),
                    entity.getSocialGroupUuid(),
                    level,
                    Membership.class.getSimpleName(),
                    toContentValues(entity));
        }

        @Override
        public String getClientModificationTime(Membership entity) {
            return entity.getLastModifiedClient();
        }

    }
}

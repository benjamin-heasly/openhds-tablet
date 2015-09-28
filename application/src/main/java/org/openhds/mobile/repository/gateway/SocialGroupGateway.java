package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.core.SocialGroup;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_CLIENT;
import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_SERVER;
import static org.openhds.mobile.OpenHDS.SocialGroups.EXT_ID;
import static org.openhds.mobile.OpenHDS.SocialGroups.GROUP_NAME;
import static org.openhds.mobile.OpenHDS.SocialGroups.UUID;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert SocialGroup to and from database.  SocialGroup-specific queries.
 */
public class SocialGroupGateway extends Gateway<SocialGroup> {

    public SocialGroupGateway() {
        super(OpenHDS.SocialGroups.CONTENT_ID_URI_BASE, UUID, new SocialGroupConverter());
    }

    public Query findByExtId(String extId) {
        return new Query(tableUri, EXT_ID, extId, UUID);
    }

    private static class SocialGroupConverter implements Converter<SocialGroup> {

        @Override
        public SocialGroup fromCursor(Cursor cursor) {
            SocialGroup socialGroup = new SocialGroup();

            socialGroup.setUuid(extractString(cursor, UUID));
            socialGroup.setExtId(extractString(cursor, EXT_ID));
            socialGroup.setGroupName(extractString(cursor, GROUP_NAME));
            socialGroup.setLastModifiedClient(extractString(cursor, LAST_MODIFIED_CLIENT));
            socialGroup.setLastModifiedServer(extractString(cursor, LAST_MODIFIED_SERVER));

            return socialGroup;
        }

        @Override
        public ContentValues toContentValues(SocialGroup socialGroup) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(UUID, socialGroup.getUuid());
            contentValues.put(EXT_ID, socialGroup.getExtId());
            contentValues.put(GROUP_NAME, socialGroup.getGroupName());
            contentValues.put(LAST_MODIFIED_CLIENT, socialGroup.getLastModifiedClient());
            contentValues.put(LAST_MODIFIED_SERVER, socialGroup.getLastModifiedServer());

            return contentValues;
        }

        @Override
        public String getId(SocialGroup socialGroup) {
            return socialGroup.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, SocialGroup socialGroup, String state) {
            DataWrapper dataWrapper = new DataWrapper();

            dataWrapper.setUuid(socialGroup.getUuid());
            dataWrapper.setName(socialGroup.getGroupName());
            dataWrapper.setCategory(state);

            return dataWrapper;
        }
    }
}

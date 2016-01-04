package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.model.core.User;
import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import java.util.Set;

import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_CLIENT;
import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_SERVER;
import static org.openhds.mobile.OpenHDS.Users.FIRST_NAME;
import static org.openhds.mobile.OpenHDS.Users.LAST_NAME;
import static org.openhds.mobile.OpenHDS.Users.PASSWORD_HASH;
import static org.openhds.mobile.OpenHDS.Users.USERNAME;
import static org.openhds.mobile.OpenHDS.Users.UUID;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;

/**
 * Convert User to and from database.  User-specific queries.
 */
public class UserGateway extends Gateway<User> {

    public UserGateway() {
        super(OpenHDS.Users.CONTENT_ID_URI_BASE, UUID, new UserConverter());
    }

    public Query findByUsername(String username) {
        return new Query(tableUri, USERNAME, username, USERNAME);
    }

    @Override
    public Set<String> getColumns() {
        return converter.toContentValues(new User()).keySet();
    }

    private static class UserConverter implements Converter<User> {

        @Override
        public User fromCursor(Cursor cursor) {
            User user = new User();

            user.setUuid(extractString(cursor, UUID));
            user.setUsername(extractString(cursor, USERNAME));
            user.setFirstName(extractString(cursor, FIRST_NAME));
            user.setLastName(extractString(cursor, LAST_NAME));
            user.setPasswordHash(extractString(cursor, PASSWORD_HASH));
            user.setLastModifiedClient(extractString(cursor, LAST_MODIFIED_CLIENT));
            user.setLastModifiedServer(extractString(cursor, LAST_MODIFIED_SERVER));

            return user;
        }

        @Override
        public ContentValues toContentValues(User user) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(UUID, user.getUuid());
            contentValues.put(USERNAME, user.getUsername());
            contentValues.put(PASSWORD_HASH, user.getPasswordHash());
            contentValues.put(LAST_NAME, user.getLastName());
            contentValues.put(FIRST_NAME, user.getFirstName());
            contentValues.put(LAST_MODIFIED_CLIENT, user.getLastModifiedClient());
            contentValues.put(LAST_MODIFIED_SERVER, user.getLastModifiedServer());

            return contentValues;
        }

        @Override
        public ContentValues toContentValues(FormContent formContent, String entityAlias) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(USERNAME, formContent.getContentString(entityAlias, USERNAME));
            contentValues.put(PASSWORD_HASH, formContent.getContentString(entityAlias, PASSWORD_HASH));
            contentValues.put(LAST_NAME, formContent.getContentString(entityAlias, LAST_NAME));
            contentValues.put(FIRST_NAME, formContent.getContentString(entityAlias, FIRST_NAME));

            return contentValues;
        }

        @Override
        public String getDefaultAlias() {
            return "socialGroup";
        }


        @Override
        public String getId(User user) {
            return user.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, User entity, String level) {
            return new DataWrapper(entity.getUuid(),
                    entity.getUsername(),
                    entity.getFirstName(),
                    level,
                    User.class.getSimpleName(),
                    toContentValues(entity));
        }

        @Override
        public String getClientModificationTime(User entity) {
            return entity.getLastModifiedClient();
        }

    }
}

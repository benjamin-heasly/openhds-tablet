package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.core.User;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_CLIENT;
import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_SERVER;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert User to and from database.  User-specific queries.
 */
public class UserGateway extends Gateway<User> {

    public UserGateway() {
        super(OpenHDS.Users.CONTENT_ID_URI_BASE, OpenHDS.Users.UUID, new UserConverter());
    }

    public Query findByUsername(String username) {
        return new Query(tableUri, OpenHDS.Users.USERNAME, username, OpenHDS.Users.USERNAME);
    }

    private static class UserConverter implements Converter<User> {

        @Override
        public User fromCursor(Cursor cursor) {
            User user = new User();

            user.setUuid(extractString(cursor, OpenHDS.Users.UUID));
            user.setUsername(extractString(cursor, OpenHDS.Users.USERNAME));
            user.setFirstName(extractString(cursor, OpenHDS.Users.FIRST_NAME));
            user.setLastName(extractString(cursor, OpenHDS.Users.LAST_NAME));
            user.setPasswordHash(extractString(cursor, OpenHDS.Users.PASSWORD_HASH));
            user.setLastModifiedClient(extractString(cursor, LAST_MODIFIED_CLIENT));
            user.setLastModifiedServer(extractString(cursor, LAST_MODIFIED_SERVER));

            return user;
        }

        @Override
        public ContentValues toContentValues(User user) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(OpenHDS.Users.UUID, user.getUuid());
            contentValues.put(OpenHDS.Users.USERNAME, user.getUsername());
            contentValues.put(OpenHDS.Users.PASSWORD_HASH, user.getPasswordHash());
            contentValues.put(OpenHDS.Users.LAST_NAME, user.getLastName());
            contentValues.put(OpenHDS.Users.FIRST_NAME, user.getFirstName());
            contentValues.put(LAST_MODIFIED_CLIENT, user.getLastModifiedClient());
            contentValues.put(LAST_MODIFIED_SERVER, user.getLastModifiedServer());

            return contentValues;
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

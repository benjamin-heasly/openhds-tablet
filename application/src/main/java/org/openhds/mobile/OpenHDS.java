package org.openhds.mobile;

import android.net.Uri;
import android.provider.BaseColumns;

public class OpenHDS {
    public static final String SCHEME = "content://";
    public static final String AUTHORITY = "org.openhds.Application";
    public static final String DEFAULT_SORT_ORDER = "_id ASC";

    private static Uri contentUri(String tableName) {
        return Uri.parse(SCHEME + AUTHORITY + "/" + tableName);
    }

    private static String contentType(String name) {
        return "vnd.android.cursor.dir/vnd.openhds." + name;
    }

    public interface Common extends BaseColumns {
        public static final String LAST_MODIFIED_SERVER = "lastModifiedOnServer";
        public static final String LAST_MODIFIED_CLIENT = "lastModifiedOnClient";
        public static final String UUID = "uuid";
    }

    public static final class Individuals implements Common {
        public static final String TABLE_NAME = "individuals";
        public static final String CONTENT_NAME = "individual";
        public static final Uri CONTENT_ID_URI_BASE = contentUri(TABLE_NAME);
        public static final String CONTENT_TYPE = contentType(CONTENT_NAME);

        public static final String EXT_ID = "extId";
        public static final String FIRST_NAME = "firstName";
        public static final String MIDDLE_NAME = "middleName";
        public static final String LAST_NAME = "lastName";
        public static final String DOB = "dob";
        public static final String GENDER = "gender";
        public static final String MOTHER = "mother";
        public static final String FATHER = "father";
    }

    public static final class Locations implements Common {
        public static final String TABLE_NAME = "locations";
        public static final String CONTENT_NAME = "location";
        public static final Uri CONTENT_ID_URI_BASE = contentUri(TABLE_NAME);
        public static final String CONTENT_TYPE = contentType(CONTENT_NAME);

        public static final String EXT_ID = "extId";
        public static final String NAME = "name";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String LOCATION_HIERARCHY_UUID = "hierarchyUuid";
    }

    public static final class LocationHierarchies implements Common {
        public static final String TABLE_NAME = "locationsHierarchies";
        public static final String CONTENT_NAME = "locationHierarchy";
        public static final Uri CONTENT_ID_URI_BASE = contentUri(TABLE_NAME);
        public static final String CONTENT_TYPE = contentType(CONTENT_NAME);

        public static final String EXT_ID = "extId";
        public static final String NAME = "name";
        public static final String PARENT_UUID = "parentUuid";
        public static final String LOCATION_HIERARCHY_LEVEL_UUID = "levelUuid";
    }

    public static final class LocationHierarchyLevels implements Common {
        public static final String TABLE_NAME = "locationHierarchyLevels";
        public static final String CONTENT_NAME = "locationHierarchyLevel";
        public static final Uri CONTENT_ID_URI_BASE = contentUri(TABLE_NAME);
        public static final String CONTENT_TYPE = contentType(CONTENT_NAME);

        public static final String NAME = "name";
        public static final String KEY_IDENTIFIER = "keyIdentifier";
    }

    public static final class Visits implements Common {
        public static final String TABLE_NAME = "visits";
        public static final String CONTENT_NAME = "visit";
        public static final Uri CONTENT_ID_URI_BASE = contentUri(TABLE_NAME);
        public static final String CONTENT_TYPE = contentType(CONTENT_NAME);

        public static final String EXT_ID = "extId";
        public static final String DATE = "date";
        public static final String LOCATION_UUID = "location_uuid";
        public static final String FIELD_WORKER_UUID = "fieldWorkerUuid";
    }

    public static final class Relationships implements Common {
        public static final String TABLE_NAME = "relationships";
        public static final String CONTENT_NAME = "relationship";
        public static final Uri CONTENT_ID_URI_BASE = contentUri(TABLE_NAME);
        public static final String CONTENT_TYPE = contentType(CONTENT_NAME);

        public static final String INDIVIDUAL_A_UUID = "individualA";
        public static final String INDIVIDUAL_B_UUID = "individualB";
        public static final String TYPE = "relationshipType";
        public static final String START_DATE = "startDate";
    }

    public static final class Users implements Common {
        public static final String TABLE_NAME = "users";
        public static final String CONTENT_NAME = "user";
        public static final Uri CONTENT_ID_URI_BASE = contentUri(TABLE_NAME);
        public static final String CONTENT_TYPE = contentType(CONTENT_NAME);

        public static final String FIRST_NAME = "fistName";
        public static final String LAST_NAME = "lastName";
        public static final String USERNAME = "username";
        public static final String PASSWORD_HASH = "passwordHash";
    }

    public static final class FieldWorkers implements Common {
        public static final String TABLE_NAME = "fieldWorkers";
        public static final String CONTENT_NAME = "fieldWorker";
        public static final Uri CONTENT_ID_URI_BASE = contentUri(TABLE_NAME);
        public static final String CONTENT_TYPE = contentType(CONTENT_NAME);

        public static final String FIELD_WORKER_ID = "fieldWorkerId";
        public static final String PASSWORD_HASH = "passwordHash";
        public static final String FIRST_NAME = "firstName";
        public static final String LAST_NAME = "lastName";
    }

    public static final class SocialGroups implements Common {
        public static final String TABLE_NAME = "socialGroups";
        public static final String CONTENT_NAME = "socialGroup";
        public static final Uri CONTENT_ID_URI_BASE = contentUri(TABLE_NAME);
        public static final String CONTENT_TYPE = contentType(CONTENT_NAME);

        public static final String EXT_ID = "extId";
        public static final String GROUP_NAME = "groupName";
    }

    public static final class Memberships implements Common {
        public static final String TABLE_NAME = "memberships";
        public static final String CONTENT_NAME = "membership";
        public static final Uri CONTENT_ID_URI_BASE = contentUri(TABLE_NAME);
        public static final String CONTENT_TYPE = contentType(CONTENT_NAME);

        public static final String SOCIAL_GROUP_UUID = "socialGroupUuid";
        public static final String INDIVIDUAL_UUID = "individualUuid";
    }

    public static final class Residencies implements Common {
        public static final String TABLE_NAME = "residencies";
        public static final String CONTENT_NAME = "residency";
        public static final Uri CONTENT_ID_URI_BASE = contentUri(TABLE_NAME);
        public static final String CONTENT_TYPE = contentType(CONTENT_NAME);

        public static final String LOCATION_UUID = "locationUuid";
        public static final String INDIVIDUAL_UUID = "individualUuid";
        public static final String END_TYPE = "endType";
    }

}

package org.openhds.mobile.task;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.listener.SyncDatabaseListener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * AsyncTask responsible for downloading the OpenHDS "database", that is a
 * subset of the OpenHDS database records. It does the downloading
 * incrementally, by downloading parts of the data one at a time. For example,
 * it gets all locations and then retrieves all individuals. Ordering is
 * somewhat important here, because the database has a few foreign key
 * references that must be satisfied (e.g. individual references a location
 * location)
 */
public class SyncEntitiesTask extends
        AsyncTask<Void, Integer, HttpTask.EndResult> {

    private static final String API_PATH = "/api/rest";

    private SyncDatabaseListener listener;
    private ContentResolver resolver;

    private UsernamePasswordCredentials creds;
    private ProgressDialog dialog;
    private HttpGet httpGet;
    private HttpClient client;

    private String baseurl;
    private String username;
    private String password;

    String lastExtId;

    private final List<ContentValues> values = new ArrayList<ContentValues>();
    private final List<ContentValues> membershipValues = new ArrayList<ContentValues>();

    private final ContentValues[] emptyArray = new ContentValues[] {};

    private State state;
    private Entity entity;

    private enum State {
        DOWNLOADING, SAVING
    }

    private enum Entity {
        LOCATION_HIERARCHY, LOCATION, ROUND, VISIT, RELATIONSHIP, INDIVIDUAL, SOCIALGROUP
    }

    public SyncEntitiesTask(String url, String username, String password,
                            ProgressDialog dialog, Context context,
                            SyncDatabaseListener listener) {
        this.baseurl = url;
        this.username = username;
        this.password = password;
        this.dialog = dialog;
        this.listener = listener;
        this.resolver = context.getContentResolver();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        StringBuilder builder = new StringBuilder();
        switch (state) {
            case DOWNLOADING:
                builder.append("Downloading ");
                break;
            case SAVING:
                builder.append("Saving ");
                break;
        }

        switch (entity) {
            case INDIVIDUAL:
                builder.append(" Individuals.");
                break;
            case LOCATION:
                builder.append(" Locations.");
                break;
            case LOCATION_HIERARCHY:
                builder.append(" Location Hierarchy.");
                break;
            case RELATIONSHIP:
                builder.append(" Relationships.");
                break;
            case ROUND:
                builder.append(" Rounds.");
                break;
            case SOCIALGROUP:
                builder.append(" Social Groups.");
                break;
            case VISIT:
                builder.append(" Visits.");
                break;
        }

        dialog.setTitle(builder.toString());
        if (values.length > 0) {
            dialog.setMessage("Saved " + values[0] + " items.");
        } else {
            dialog.setMessage("Please wait.");
        }
    }

    @Override
    protected HttpTask.EndResult doInBackground(Void... params) {
        creds = new UsernamePasswordCredentials(username, password);

        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 60000);
        HttpConnectionParams.setSoTimeout(httpParameters, 90000);
        HttpConnectionParams.setSocketBufferSize(httpParameters, 8192);
        client = new DefaultHttpClient(httpParameters);

        // at this point, we don't care to be smart about which data to
        // download, we simply download it all
        deleteAllTables();

        try {

            entity = Entity.ROUND;
            processUrl(baseurl + API_PATH + "/rounds");

            entity = Entity.VISIT;
            processUrl(baseurl + API_PATH + "/visits/cached");

            entity = Entity.RELATIONSHIP;
            processUrl(baseurl + API_PATH + "/relationships/cached");

            entity = Entity.INDIVIDUAL;
            processUrl(baseurl + API_PATH + "/individuals/cached");

            entity = Entity.SOCIALGROUP;
            processUrl(baseurl + API_PATH + "/socialgroups/cached");

            entity = Entity.LOCATION_HIERARCHY;
            processUrl(baseurl + API_PATH + "/locationhierarchies");

            entity = Entity.LOCATION;
            processUrl(baseurl + API_PATH + "/locations/cached");

        } catch (Exception e) {
            return HttpTask.EndResult.FAILURE;
        }

        return HttpTask.EndResult.SUCCESS;
    }

    private void deleteAllTables() {
        // ordering is somewhat important during delete. a few tables have
        // foreign keys
        resolver.delete(OpenHDS.Memberships.CONTENT_ID_URI_BASE, null, null);
        resolver.delete(OpenHDS.Visits.CONTENT_ID_URI_BASE, null, null);
        resolver.delete(OpenHDS.Relationships.CONTENT_ID_URI_BASE, null, null);
        resolver.delete(OpenHDS.SocialGroups.CONTENT_ID_URI_BASE, null, null);
        resolver.delete(OpenHDS.HierarchyItems.CONTENT_ID_URI_BASE, null, null);
        resolver.delete(OpenHDS.Individuals.CONTENT_ID_URI_BASE, null, null);
        resolver.delete(OpenHDS.Locations.CONTENT_ID_URI_BASE, null, null);
    }

    private void processUrl(String url) throws Exception {
        state = State.DOWNLOADING;
        publishProgress();

        httpGet = new HttpGet(url);
        processResponse();
    }

    private void processResponse() throws Exception {
        InputStream inputStream = getResponse();
        if (inputStream != null)
            processXMLDocument(inputStream);
    }

    private InputStream getResponse() throws AuthenticationException,
            ClientProtocolException, IOException {
        HttpResponse response = null;

        httpGet.addHeader(new BasicScheme().authenticate(creds, httpGet));
        httpGet.addHeader("content-type", "application/xml");
        response = client.execute(httpGet);

        HttpEntity entity = response.getEntity();
        return entity.getContent();
    }

    private void processXMLDocument(InputStream content) throws Exception {
        state = State.SAVING;

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);

        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new InputStreamReader(content));

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT && !isCancelled()) {
            String name = null;

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("count")) {
                        parser.next();
                        int cnt = Integer.parseInt(parser.getText());
                        publishProgress(cnt);
                        parser.nextTag();

                    } else if (name.equalsIgnoreCase("individuals")) {
                        processIndividualParams(parser);
                    } else if (name.equalsIgnoreCase("locations")) {
                        processLocationParams(parser);
                    } else if (name.equalsIgnoreCase("locationhierarchies")) {
                        processHierarchyParams(parser);
                    } else if (name.equalsIgnoreCase("visits")) {
                        processVisitParams(parser);
                    } else if (name.equalsIgnoreCase("socialgroups")) {
                        processSocialGroupParams(parser);
                    } else if (name.equalsIgnoreCase("relationships")) {
                        processRelationshipParams(parser);
                    }
                    break;
            }
            eventType = parser.next();
        }
    }

    private void processHierarchyParams(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.nextTag();

        int count = 0;
        values.clear();
        while (notEndOfXmlDoc("locationHierarchies", parser)) {
            ContentValues cv = new ContentValues();

            parser.nextTag();
            cv.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID,
                    parser.nextText());

            parser.nextTag(); // <level>
            parser.next(); // <keyIdentifier>
            parser.nextText();
            parser.nextTag(); // <name>
            cv.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL,
                    parser.nextText());

            parser.next(); // </level>
            parser.nextTag();
            cv.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME,
                    parser.nextText());

            parser.next(); // <parent>
            parser.nextTag(); // <extId>
            cv.put(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_PARENT,
                    parser.nextText());

            values.add(cv);

            parser.nextTag(); // </parent>
            parser.nextTag(); // </hierarchy>
            parser.nextTag(); // <hierarchy> or </hiearchys>

            if (values.size() >= 100) {
                count += values.size();
                publishProgress(count);
                persistValues(OpenHDS.HierarchyItems.CONTENT_URI);
            }
        }
        persistValues(OpenHDS.HierarchyItems.CONTENT_URI);
    }

    private void persistValues(Uri contentUri) {
        if (!values.isEmpty()) {
            resolver.bulkInsert(contentUri, values.toArray(emptyArray));
        }
        values.clear();
    }

    private boolean notEndOfXmlDoc(String element, XmlPullParser parser)
            throws XmlPullParserException {
        return !element.equals(parser.getName())
                && parser.getEventType() != XmlPullParser.END_TAG
                && !isCancelled();
    }

    private void processLocationParams(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        int count = 0;
        values.clear();

        String tagName;

        parser.nextTag();
        while (notEndOfXmlDoc("locations", parser)) {

            try {
                ContentValues cv = new ContentValues();
                while (true) {
                    tagName = parser.getName();
                    if (null != tagName) {

                        if (tagName.equalsIgnoreCase("location")
                                && parser.getEventType() == XmlPullParser.END_TAG) {
                            parser.next();
                            values.add(cv);
                            break;
                        }

                        if (parser.getEventType() == XmlPullParser.START_TAG) {
                            if (tagName.equalsIgnoreCase("extId")) {
                                cv.put(OpenHDS.Locations.COLUMN_LOCATION_EXTID,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("locationName")) {
                                cv.put(OpenHDS.Locations.COLUMN_LOCATION_NAME,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("latitude")) {
                                cv.put(OpenHDS.Locations.COLUMN_LOCATION_LATITUDE,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("longitude")) {
                                cv.put(OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("communityName")) {
                                cv.put(OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_NAME,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("sectorName")) {
                                cv.put(OpenHDS.Locations.COLUMN_LOCATION_SECTOR_NAME,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("mapAreaName")) {
                                cv.put(OpenHDS.Locations.COLUMN_LOCATION_MAP_AREA_NAME,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("localityName")) {
                                cv.put(OpenHDS.Locations.COLUMN_LOCATION_LOCALITY_NAME,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("locationLevel")) {
                                //<locationLevel>
                                //    <extId>BA15M1000S056</extId>
                                //</locationLevel>
                                while (true) {
                                    tagName = parser.getName();
                                    if (null == tagName) {
                                        parser.next();
                                        continue;

                                    } else if (tagName.equalsIgnoreCase("locationLevel")
                                            && parser.getEventType() == XmlPullParser.END_TAG) {
                                        break;

                                    } else if (tagName.equalsIgnoreCase("extId")) {
                                        cv.put(OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY,
                                                parser.nextText());
                                    }
                                    parser.next();
                                }
                            } else if (tagName.equalsIgnoreCase("collectedBy")) {
                                while (true) {
                                    tagName = parser.getName();
                                    if (null == tagName) {
                                        parser.next();
                                        continue;

                                    } else if (tagName.equalsIgnoreCase("collectedBy")
                                            && parser.getEventType() == XmlPullParser.END_TAG) {
                                        break;
                                    }
                                    parser.next();
                                }
                            }
                        }
                    }
                    parser.next();
                }

            } catch (Exception e) {
                Log.e(getClass().getName(), e.getMessage());
            }

            if (values.size() >= 100) {
                count += values.size();
                publishProgress(count);
                persistValues(OpenHDS.Locations.CONTENT_URI);
            }
        }
        persistValues(OpenHDS.Locations.CONTENT_URI);

    }

    private void processIndividualParams(XmlPullParser parser)
            throws IOException, XmlPullParserException {

        values.clear();

        String textValue;
        String tagName;

        parser.nextTag();
        while (notEndOfXmlDoc("individuals", parser)) {

            try {
                ContentValues cv = new ContentValues();
                while (true) {
                    if (null != (tagName = parser.getName())) {

                        if (tagName.equalsIgnoreCase("individual")
                                && parser.getEventType() == XmlPullParser.END_TAG) {
                            parser.next();
                            values.add(cv);
                            break;
                        }

                        if (parser.getEventType() == XmlPullParser.START_TAG) {
                            if (tagName.equalsIgnoreCase("age")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("ageUnits")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE_UNITS,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("dip")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_ID,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("dob")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("extId")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("father")) {
                                while (true) {
                                    if (null != parser.getName()
                                            && parser.getName()
                                            .equalsIgnoreCase("father")
                                            && parser.getEventType() == XmlPullParser.END_TAG) {
                                        break;
                                    }
                                    parser.next();
                                }
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER,
                                        "UNK");
                            } else if (tagName.equalsIgnoreCase("mother")) {
                                while (true) {
                                    if (null != parser.getName()
                                            && parser.getName()
                                            .equalsIgnoreCase("mother")
                                            && parser.getEventType() == XmlPullParser.END_TAG) {
                                        break;
                                    }
                                    parser.next();
                                }
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER,
                                        "UNK");
                            } else if (tagName.equalsIgnoreCase("firstName")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME,
                                        parser.nextText());
                            } else if (tagName
                                    .equalsIgnoreCase("languagePreference")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("lastName")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("middleName")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("memberStatus")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_STATUS,
                                        parser.nextText());
                            } else if (tagName
                                    .equalsIgnoreCase("otherPhoneNumber")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("phoneNumber")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER,
                                        parser.nextText());
                            } else if (tagName
                                    .equalsIgnoreCase("pointOfContactName")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME,
                                        parser.nextText());
                            } else if (tagName
                                    .equalsIgnoreCase("pointOfContactPhoneNumber")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("gender")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER,
                                        parser.nextText());
                            } else if (tagName.equalsIgnoreCase("memberships")) {
                                pullOutMemberships(parser);
                            } else if (tagName.equalsIgnoreCase("residencies")) {
                                cv.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_EXTID,
                                        (parser = pullOutResidencies(parser)).nextText());
                            }
                        }
                    }
                    parser.next();
                }

            } catch (Exception e) {
                Log.e(getClass().getName(), e.getMessage());
            }

        }

        int test = 0;
        if (values.size() > 0) {
            test = resolver.bulkInsert(OpenHDS.Individuals.CONTENT_ID_URI_BASE,
                    values.toArray(emptyArray));
        }
        if (membershipValues.size() > 0) {
            test = resolver.bulkInsert(OpenHDS.Memberships.CONTENT_ID_URI_BASE,
                    membershipValues.toArray(emptyArray));
        }
        test = 0;
    }

    private void pullOutMemberships(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        ContentValues membershipsCv = new ContentValues();

        while (true) {
            if (null != parser.getName()) {

                if (parser.getName().equalsIgnoreCase("membership")
                        && parser.getEventType() == XmlPullParser.END_TAG) {
                    membershipValues.add(membershipsCv);
                    parser.next();
                    break;
                } else if (parser.getName().equalsIgnoreCase("memberships")
                        && parser.getEventType() == XmlPullParser.END_TAG) {
                    return;

                } else if (parser.getName().equalsIgnoreCase("individual")) {
                    while (true) {

                        if (null != parser.getName()
                                && parser.getName().equalsIgnoreCase("extId")) {
                            membershipsCv
                                    .put(OpenHDS.Memberships.COLUMN_INDIVIDUAL_EXTID,
                                            parser.nextText());
                            parser.next();
                            break;
                        }
                        parser.next();

                    }
                } else if (parser.getName().equalsIgnoreCase("socialGroup")) {
                    while (true) {

                        if (null != parser.getName()
                                && parser.getName().equalsIgnoreCase("extId")) {
                            membershipsCv
                                    .put(OpenHDS.Memberships.COLUMN_SOCIAL_GROUP_EXTID,
                                            parser.nextText());
                            parser.next();
                            break;
                        }
                        parser.next();

                    }
                } else if (parser.getName().equalsIgnoreCase("bIsToA")) {
                    membershipsCv
                            .put(OpenHDS.Memberships.COLUMN_MEMBERSHIP_RELATIONSHIP_TO_HEAD,
                                    parser.nextText());
                }
            }
            parser.next();
        }
    }

    private XmlPullParser pullOutResidencies(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        while (true) {
            if (null != parser.getName()
                    && parser.getName().equalsIgnoreCase("location")) {
                while (true) {
                    if (null != parser.getName()
                            && parser.getName().equalsIgnoreCase("extId")) {
                        return parser;
                    }
                    parser.next();
                }
            }
            parser.next();
        }
    }

    private List<String> parseMembershipExtIds(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        List<String> groups = new ArrayList<String>();

        while (!"memberships".equalsIgnoreCase(parser.getName())) {
            parser.nextTag(); // <socialGroup>
            parser.nextTag(); // <extId>
            groups.add(parser.nextText());
            parser.nextTag(); // <socialGroup>
            parser.nextTag(); // <bIsToA>
            parser.nextText();
            parser.nextTag(); // </membership>
            parser.nextTag(); // <membership> or </memberships>
        }

        return groups;
    }

    private void processVisitParams(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.nextTag();

        values.clear();
        while (notEndOfXmlDoc("visits", parser)) {
            // skip collected by
            parser.nextTag(); // <collectedBy>
            parser.nextTag(); // <extId>
            parser.nextText();
            parser.nextTag(); // </collectedBy>

            ContentValues cv = new ContentValues();

            parser.nextTag();
            cv.put(OpenHDS.Visits.COLUMN_VISIT_EXTID, parser.nextText());

            parser.nextTag();
            cv.put(OpenHDS.Visits.COLUMN_VISIT_DATE, parser.nextText());

            parser.nextTag(); // <visitLocation>
            parser.nextTag();
            cv.put(OpenHDS.Visits.COLUMN_VISIT_LOCATION_EXTID, parser.nextText());
            parser.nextTag(); // </visitLocation>

            values.add(cv);

            parser.nextTag(); // </visit>
            parser.nextTag(); // </visits> or <visit>
        }

        if (!values.isEmpty()) {
            resolver.bulkInsert(OpenHDS.Visits.CONTENT_ID_URI_BASE,
                    values.toArray(emptyArray));
        }
    }

    private void processSocialGroupParams(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.nextTag();

        int count = 0;
        values.clear();
        while (notEndOfXmlDoc("socialGroups", parser)) {
            ContentValues cv = new ContentValues();

            parser.nextTag();
            cv.put(OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_EXTID,
                    parser.nextText());

            parser.next(); // <groupHead>
            parser.next();
            parser.next();
            parser.next();
            parser.next(); // <memberships>
            parser.next(); // </memberships>
            parser.next(); // <residencies>
            parser.next(); // </residencies>
            parser.next();
            parser.next();
            parser.next();
            parser.next(); // <extId>
            cv.put(OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_HEAD_INDIVIDUAL_EXTID,
                    parser.nextText());
            parser.nextTag(); // </groupHead>

            parser.nextTag();
            cv.put(OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_NAME,
                    parser.nextText());

            values.add(cv);

            parser.nextTag(); // </socialGroup>
            parser.nextTag(); // </socialGroups> or <socialGroup>

            if (values.size() >= 100) {
                count += values.size();
                publishProgress(count);
                persistValues(OpenHDS.SocialGroups.CONTENT_URI);
            }
        }
        persistValues(OpenHDS.SocialGroups.CONTENT_URI);
    }

    private void processRelationshipParams(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.nextTag();

        values.clear();
        while (notEndOfXmlDoc("relationships", parser)) {
            ContentValues cv = new ContentValues();

            parser.next(); // <individualA>
            parser.next();// <age>
            parser.next();
            parser.next(); // </age>
            parser.next(); // <memberships>
            parser.next(); // <residencies>
            parser.next(); // <memberships>
            parser.next(); // <residencies>
            parser.next();// <dip>
            parser.next();
            parser.next(); // </dip>
            parser.next(); // <extId>
            cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A,
                    parser.nextText());
            parser.next(); // </individualA>

            parser.next(); // <individualB>
            parser.next();// <age>
            parser.next();
            parser.next(); // </age>
            parser.next(); // <memberships>
            parser.next(); // <residencies>
            parser.next(); // <memberships>
            parser.next(); // <residencies>
            parser.next();// <dip>
            parser.next();
            parser.next(); // </dip>
            parser.next();
            cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B,
                    parser.nextText());
            parser.next(); // </individualB>

            parser.next();
            cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE,
                    parser.nextText());

            parser.next(); // <aIsToB>
            cv.put(OpenHDS.Relationships.COLUMN_RELATIONSHIP_TYPE,
                    parser.nextText());

            values.add(cv);

            parser.next(); // </relationship>
            parser.next(); // </relationships> or <relationship>
        }

        if (!values.isEmpty()) {
            resolver.bulkInsert(OpenHDS.Relationships.CONTENT_ID_URI_BASE,
                    values.toArray(emptyArray));
        }
    }

    protected void onPostExecute(HttpTask.EndResult result) {
        listener.collectionComplete(result);
    }
}

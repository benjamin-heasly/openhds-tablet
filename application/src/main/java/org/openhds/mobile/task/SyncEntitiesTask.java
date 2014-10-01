package org.openhds.mobile.task;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
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
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.model.Relationship;
import org.openhds.mobile.model.SocialGroup;
import org.openhds.mobile.model.Visit;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;
import org.openhds.mobile.repository.gateway.MembershipGateway;
import org.openhds.mobile.repository.gateway.RelationshipGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;
import org.openhds.mobile.repository.gateway.VisitGateway;
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
 * references that must be satisfied (e.g. individual references a location)
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

    public void processXMLDocument(InputStream content) throws Exception {
        state = State.SAVING;

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);

        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new InputStreamReader(content));

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT && !isCancelled()) {
            String name = null;

            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;

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

        int count = 0;
        List<LocationHierarchy> locationHierarchies = new ArrayList<LocationHierarchy>();
        LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();

        parser.nextTag();
        while (notEndOfXmlDoc("locationHierarchies", parser)) {
            LocationHierarchy locationHierarchy = new LocationHierarchy();

            parser.nextTag();
            locationHierarchy.setExtId(parser.nextText());

            parser.nextTag(); // <level>
            parser.next(); // <keyIdentifier>
            parser.nextText();
            parser.nextTag(); // <name>
            locationHierarchy.setLevel(parser.nextText());

            parser.next(); // </level>
            parser.nextTag();
            locationHierarchy.setName(parser.nextText());

            parser.next(); // <parent>
            parser.nextTag(); // <extId>
            locationHierarchy.setParent(parser.nextText());

            locationHierarchies.add(locationHierarchy);

            parser.nextTag(); // </parent>
            parser.nextTag(); // </hierarchy>
            parser.nextTag(); // <hierarchy> or </hiearchys>

            if (locationHierarchies.size() >= 100) {
                count += locationHierarchies.size();
                publishProgress(count);
                locationHierarchyGateway.insertMany(resolver, locationHierarchies);
                locationHierarchies.clear();
            }
        }
        locationHierarchyGateway.insertMany(resolver, locationHierarchies);
    }

    private boolean notEndOfXmlDoc(String element, XmlPullParser parser)
            throws XmlPullParserException {

        String name = parser.getName();
        return !isCancelled()
                && parser.getEventType() != XmlPullParser.END_DOCUMENT
                && !element.equals(name)
                && parser.getEventType() != XmlPullParser.END_TAG;
    }

    private void processLocationParams(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        int count = 0;
        List<Location> locations = new ArrayList<Location>();
        LocationGateway locationGateway = GatewayRegistry.getLocationGateway();

        String tagName;

        parser.nextTag();
        while (notEndOfXmlDoc("locations", parser)) {

            try {
                Location location = new Location();
                while (true) {
                    tagName = parser.getName();
                    if (null != tagName) {

                        if (tagName.equalsIgnoreCase("location")
                                && parser.getEventType() == XmlPullParser.END_TAG) {
                            parser.next();
                            locations.add(location);
                            break;
                        }

                        if (parser.getEventType() == XmlPullParser.START_TAG) {
                            if (tagName.equalsIgnoreCase("extId")) {
                                location.setExtId(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("locationName")) {
                                location.setName(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("latitude")) {
                                location.setLatitude(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("longitude")) {
                                location.setLongitude(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("communityName")) {
                                location.setCommunityName(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("communityCode")) {
                                location.setCommunityCode(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("sectorName")) {
                                location.setSectorName(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("mapAreaName")) {
                                location.setMapAreaName(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("localityName")) {
                                location.setLocalityName(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("buildingNumber")) {
                                String buildingNumber = parser.nextText();
                                if (null != buildingNumber && !buildingNumber.isEmpty()) {
                                    location.setBuildingNumber(Integer.parseInt(buildingNumber));
                                }
                            } else if (tagName.equalsIgnoreCase("floorNumber")) {
                                String floorNumber = parser.nextText();
                                if (null != floorNumber && !floorNumber.isEmpty()) {
                                    location.setFloorNumber(Integer.parseInt(floorNumber));
                                }
                            } else if (tagName.equalsIgnoreCase("regionName")) {
                                location.setRegionName(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("provinceName")) {
                                location.setProvinceName(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("subDistrictName")) {
                                location.setSubDistrictName(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("districtName")) {
                                location.setDistrictName(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("locationHierarchy")) {
                                //<locationLevel>
                                //    <extId>BA15M1000S056</extId>
                                //</locationLevel>
                                while (true) {
                                    tagName = parser.getName();
                                    if (null == tagName) {
                                        parser.next();
                                        continue;

                                    } else if (tagName.equalsIgnoreCase("locationHierarchy")
                                            && parser.getEventType() == XmlPullParser.END_TAG) {
                                        break;

                                    } else if (tagName.equalsIgnoreCase("extId")) {
                                        location.setHierarchyExtId(parser.nextText());
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

            if (locations.size() >= 100) {
                count += locations.size();
                publishProgress(count);
                locationGateway.insertMany(resolver, locations);
                locations.clear();
            }
        }
        locationGateway.insertMany(resolver, locations);
    }

    private void processIndividualParams(XmlPullParser parser)
            throws IOException, XmlPullParserException {

        int count = 0;
        List<Individual> individuals = new ArrayList<Individual>();
        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        List<Membership> memberships = new ArrayList<Membership>();
        MembershipGateway membershipGateway = GatewayRegistry.getMembershipGateway();

        String tagName;

        parser.nextTag();
        while (notEndOfXmlDoc("individuals", parser)) {

            try {
                Individual individual = new Individual();
                while (true) {
                    if (null != (tagName = parser.getName())) {

                        if (tagName.equalsIgnoreCase("individual") && parser.getEventType() == XmlPullParser.END_TAG) {
                            parser.next();
                            individuals.add(individual);
                            break;
                        }

                        if (parser.getEventType() == XmlPullParser.START_TAG) {
                            if (tagName.equalsIgnoreCase("age")) {
                                individual.setAge(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("ageUnits")) {
                                individual.setAgeUnits(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("dip")) {
                                individual.setOtherId(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("dob")) {
                                individual.setDob(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("extId")) {
                                individual.setExtId(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("father")) {
                                while (true) {
                                    if (null != parser.getName()
                                            && parser.getName().equalsIgnoreCase("father")
                                            && parser.getEventType() == XmlPullParser.END_TAG) {
                                        break;
                                    }
                                    parser.next();
                                }
                                individual.setFather("UNK");
                            } else if (tagName.equalsIgnoreCase("mother")) {
                                while (true) {
                                    if (null != parser.getName()
                                            && parser.getName().equalsIgnoreCase("mother")
                                            && parser.getEventType() == XmlPullParser.END_TAG) {
                                        break;
                                    }
                                    parser.next();
                                }
                                individual.setMother("UNK");
                            } else if (tagName.equalsIgnoreCase("firstName")) {
                                individual.setFirstName(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("languagePreference")) {
                                individual.setLanguagePreference(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("lastName")) {
                                individual.setLastName(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("middleName")) {
                                individual.setMiddleName(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("memberStatus")) {
                                individual.setMemberStatus(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("otherPhoneNumber")) {
                                individual.setOtherPhoneNumber(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("phoneNumber")) {
                                individual.setPhoneNumber(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("pointOfContactName")) {
                                individual.setPointOfContactName(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("pointOfContactPhoneNumber")) {
                                individual.setPointOfContactPhoneNumber(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("gender")) {
                                individual.setGender(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("memberships")) {
                                pullOutMemberships(parser, memberships);
                            } else if (tagName.equalsIgnoreCase("residencies")) {
                                pullOutResidency(parser, individual);
                            }
                        }
                    }
                    parser.next();
                }

            } catch (Exception e) {
                Log.e(getClass().getName(), e.getMessage());
            }

        }

        if (individuals.size() > 0) {
            individualGateway.insertMany(resolver, individuals);
        }

        if (memberships.size() > 0) {
            membershipGateway.insertMany(resolver, memberships);
        }
    }

    private void pullOutMemberships(XmlPullParser parser, List<Membership> memberships)
            throws XmlPullParserException, IOException {

        Membership membership = new Membership();

        while (true) {

            if (null != parser.getName()) {

                if (parser.getName().equalsIgnoreCase("membership")
                        && parser.getEventType() == XmlPullParser.END_TAG) {
                    memberships.add(membership);
                    membership = new Membership();
                    parser.next();
                    continue;

                } else if (parser.getName().equalsIgnoreCase("memberships")
                        && parser.getEventType() == XmlPullParser.END_TAG) {
                    return;

                } else if (parser.getName().equalsIgnoreCase("individual")) {
                    while (true) {

                        if (null != parser.getName()
                                && parser.getName().equalsIgnoreCase("extId")) {
                            membership.setIndividualExtId(parser.nextText());
                            parser.next();
                            break;
                        }
                        parser.next();

                    }
                } else if (parser.getName().equalsIgnoreCase("socialGroup")) {
                    while (true) {

                        if (null != parser.getName()
                                && parser.getName().equalsIgnoreCase("extId")) {
                            membership.setSocialGroupExtId(parser.nextText());
                            parser.next();
                            break;
                        }
                        parser.next();

                    }
                } else if (parser.getName().equalsIgnoreCase("bIsToA")) {
                    membership.setRelationshipToHead(parser.nextText());
                }
            }
            parser.next();
        }
    }

    private void pullOutResidency(XmlPullParser parser, Individual individual)
            throws XmlPullParserException, IOException {

        while (true) {

            if (null != parser.getName() && parser.getName().equalsIgnoreCase("endType")) {
                individual.setEndType(parser.nextText());
            }

            if (null != parser.getName() && parser.getName().equalsIgnoreCase("location")) {
                while (true) {
                    if (null != parser.getName() && parser.getName().equalsIgnoreCase("extId")) {
                        individual.setCurrentResidence(parser.nextText());
                        return;
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

    private void processVisitParams(XmlPullParser parser) throws XmlPullParserException, IOException {

        int count = 0;
        List<Visit> visits = new ArrayList<Visit>();
        VisitGateway visitGateway = GatewayRegistry.getVisitGateway();

        String tagName;

        parser.nextTag();
        while (notEndOfXmlDoc("visits", parser)) {

            try {
                Visit visit = new Visit();
                while (true) {
                    tagName = parser.getName();
                    if (null != tagName) {

                        if (tagName.equalsIgnoreCase("visit") && parser.getEventType() == XmlPullParser.END_TAG) {
                            parser.next();
                            visits.add(visit);
                            break;
                        }

                        if (parser.getEventType() == XmlPullParser.START_TAG) {
                            if (tagName.equalsIgnoreCase("extId")) {
                                visit.setVisitExtId(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("visitDate")) {
                                visit.setVisitDate(parser.nextText());
                            } else if (tagName.equalsIgnoreCase("visitLocation")) {
                                //<visitLocation>
                                //    <extId>M401S83E01P1</extId>
                                //</visitLocation>
                                while (true) {
                                    tagName = parser.getName();
                                    if (null == tagName) {
                                        parser.next();
                                        continue;

                                    } else if (tagName.equalsIgnoreCase("visitLocation")
                                            && parser.getEventType() == XmlPullParser.END_TAG) {
                                        break;

                                    } else if (tagName.equalsIgnoreCase("extId")) {
                                        visit.setLocationExtId(parser.nextText());
                                    }
                                    parser.next();
                                }
                            } else if (tagName.equalsIgnoreCase("collectedBy")) {
                                //<collectedBy>
                                //    <extId>UNK</extId>
                                //</collectedBy>
                                while (true) {
                                    tagName = parser.getName();
                                    if (null == tagName) {
                                        parser.next();
                                        continue;

                                    } else if (tagName.equalsIgnoreCase("collectedBy")
                                            && parser.getEventType() == XmlPullParser.END_TAG) {
                                        break;

                                    } else if (tagName.equalsIgnoreCase("extId")) {
                                        visit.setFieldWorkerExtId(parser.nextText());
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

            if (visits.size() >= 100) {
                count += visits.size();
                publishProgress(count);
                visitGateway.insertMany(resolver, visits);
                visits.clear();
            }
        }
        visitGateway.insertMany(resolver, visits);
    }

    private void processSocialGroupParams(XmlPullParser parser) throws XmlPullParserException, IOException {

        int count = 0;
        List<SocialGroup> socialGroups = new ArrayList<SocialGroup>();
        SocialGroupGateway socialGroupGateway = GatewayRegistry.getSocialGroupGateway();

        parser.nextTag();
        while (notEndOfXmlDoc("socialGroups", parser)) {
            SocialGroup socialGroup = new SocialGroup();

            parser.nextTag();
            socialGroup.setExtId(parser.nextText());

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
            socialGroup.setGroupHead(parser.nextText());
            parser.nextTag(); // </groupHead>

            parser.nextTag();
            socialGroup.setGroupName(parser.nextText());

            socialGroups.add(socialGroup);

            parser.nextTag(); // </socialGroup>
            parser.nextTag(); // </socialGroups> or <socialGroup>

            if (socialGroups.size() >= 100) {
                count += socialGroups.size();
                publishProgress(count);
                socialGroupGateway.insertMany(resolver, socialGroups);
                socialGroups.clear();
            }
        }
        socialGroupGateway.insertMany(resolver, socialGroups);
    }

    private void processRelationshipParams(XmlPullParser parser) throws XmlPullParserException, IOException {

        int count = 0;
        List<Relationship> relationships = new ArrayList<Relationship>();
        RelationshipGateway relationshipGateway = GatewayRegistry.getRelationshipGateway();

        parser.nextTag();
        while (notEndOfXmlDoc("relationships", parser)) {
            Relationship relationship = new Relationship();

            parser.next(); // <individualA> or <endDate>
            if ("endDate".equals(parser.getName())) {
                parser.next();// <endDate>
                parser.next();
                parser.next(); // </endDate>
                parser.next();// <endType>
                parser.next();
                parser.next(); // </endType>
            }

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
            relationship.setIndividualA(parser.nextText());
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
            relationship.setIndividualB(parser.nextText());
            parser.next(); // </individualB>

            parser.next();
            relationship.setStartDate(parser.nextText());

            parser.next(); // <aIsToB>
            relationship.setType(parser.nextText());

            relationships.add(relationship);

            parser.next(); // </relationship>
            parser.next(); // </relationships> or <relationship>
        }

        if (!relationships.isEmpty()) {
            relationshipGateway.insertMany(resolver, relationships);
        }
    }

    protected void onPostExecute(HttpTask.EndResult result) {
        listener.collectionComplete(result);
    }
}

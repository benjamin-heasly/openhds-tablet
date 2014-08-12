package org.openhds.mobile.tests;

import android.content.ContentResolver;
import android.test.ProviderTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.Relationship;
import org.openhds.mobile.model.SocialGroup;
import org.openhds.mobile.model.Visit;
import org.openhds.mobile.provider.OpenHDSProvider;
import org.openhds.mobile.provider.PasswordHelper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;
import org.openhds.mobile.repository.gateway.RelationshipGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;
import org.openhds.mobile.repository.gateway.VisitGateway;
import org.openhds.mobile.task.SyncEntitiesTask;

import java.io.InputStream;
import java.util.List;

/**
 * Feed XML documents to SyncEntitiesTask and verify that entities were created in the database.
 *
 * Note, the XML parsing in is quite brittle.  The XML in assets/testXml must
 * not contain extra whitespace.  Otherwise the task might get caught in an infinite
 * loop.  SyncEntitiesTask and SyncFieldWorkersTask are ripe for refactoring!
 *
 * BSH
 */
public class SyncEntitiesTaskTest extends ProviderTestCase2<OpenHDSProvider> {

    private static final String TEST_PASSWORD = "";

    private OpenHDSProvider provider;
    private ContentResolver contentResolver;
    private SyncEntitiesTask syncEntitiesTask;

    private class ConstantPasswordHelper implements PasswordHelper {
        @Override
        public String getPassword() {
            return TEST_PASSWORD;
        }
    }

    public SyncEntitiesTaskTest() {
        super(OpenHDSProvider.class, OpenHDS.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.provider = (OpenHDSProvider) getProvider();
        this.contentResolver = getMockContentResolver();

        // inject a password helper that uses a known password
        // and doesn't use shared preferences (which are not enabled under ProviderTestCase2)
        provider.setPasswordHelper(new ConstantPasswordHelper());

        // make sure we have a fresh database for each test
        SQLiteOpenHelper databaseHelper = provider.getDatabaseHelper();
        SQLiteDatabase.loadLibs(getContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase(TEST_PASSWORD);
        databaseHelper.onUpgrade(db, 0, 0);

        // test the task against the mock content resolver, needs no other dependencies
        syncEntitiesTask = new SyncEntitiesTask(null, null, null, null, getMockContext(), null);
    }

    @Override
    protected void tearDown() {
        SQLiteOpenHelper databaseHelper = provider.getDatabaseHelper();
        SQLiteDatabase db = databaseHelper.getWritableDatabase(TEST_PASSWORD);
        db.close();
    }

    public void testProcessIndividualXML() throws Exception {
        InputStream inputStream = getContext().getAssets().open("testXml/individuals.xml");
        syncEntitiesTask.processXMLDocument(inputStream);

        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        List<Individual> individuals = individualGateway.getList(contentResolver, individualGateway.findAll());
        assertEquals(2, individuals.size());
    }

    public void testProcessLocationHierarchyXML() throws Exception {
        InputStream inputStream = getContext().getAssets().open("testXml/location-hierarchies.xml");
        syncEntitiesTask.processXMLDocument(inputStream);

        LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
        List<LocationHierarchy> locationHierarchies = locationHierarchyGateway.getList(contentResolver, locationHierarchyGateway.findAll());
        assertEquals(2, locationHierarchies.size());
    }

    public void testProcessLocationXML() throws Exception {
        InputStream inputStream = getContext().getAssets().open("testXml/locations.xml");
        syncEntitiesTask.processXMLDocument(inputStream);

        LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
        List<Location> locations = locationGateway.getList(contentResolver, locationGateway.findAll());
        assertEquals(2, locations.size());
    }

    public void testProcessRelationshipXML() throws Exception {
        InputStream inputStream = getContext().getAssets().open("testXml/relationships.xml");
        syncEntitiesTask.processXMLDocument(inputStream);

        RelationshipGateway relationshipGateway = GatewayRegistry.getRelationshipGateway();
        List<Relationship> relationships = relationshipGateway.getList(contentResolver, relationshipGateway.findAll());
        assertEquals(2, relationships.size());
    }

    public void testProcessSocialGroupXML() throws Exception {
        InputStream inputStream = getContext().getAssets().open("testXml/social-groups.xml");
        syncEntitiesTask.processXMLDocument(inputStream);

        SocialGroupGateway socialGroupGateway = GatewayRegistry.getSocialGroupGateway();
        List<SocialGroup> socialGroups = socialGroupGateway.getList(contentResolver, socialGroupGateway.findAll());
        assertEquals(2, socialGroups.size());
    }

    public void testProcessVisitXML() throws Exception {
        InputStream inputStream = getContext().getAssets().open("testXml/visits.xml");
        syncEntitiesTask.processXMLDocument(inputStream);

        VisitGateway visitGateway = GatewayRegistry.getVisitGateway();
        List<Visit> visits = visitGateway.getList(contentResolver, visitGateway.findAll());
        assertEquals(2, visits.size());
    }
}

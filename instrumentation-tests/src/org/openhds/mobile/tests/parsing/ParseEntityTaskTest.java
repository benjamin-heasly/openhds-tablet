package org.openhds.mobile.tests.parsing;

import android.content.ContentResolver;
import android.test.ProviderTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.Relationship;
import org.openhds.mobile.model.SocialGroup;
import org.openhds.mobile.model.Visit;
import org.openhds.mobile.provider.OpenHDSProvider;
import org.openhds.mobile.provider.PasswordHelper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.FieldWorkerGateway;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;
import org.openhds.mobile.repository.gateway.RelationshipGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;
import org.openhds.mobile.repository.gateway.VisitGateway;
import org.openhds.mobile.task.parsing.ParseEntityTask;
import org.openhds.mobile.task.parsing.ParseEntityTaskRequest;
import org.openhds.mobile.task.parsing.entities.FieldWorkerParser;
import org.openhds.mobile.task.parsing.entities.IndividualParser;
import org.openhds.mobile.task.parsing.entities.LocationHierarchyParser;
import org.openhds.mobile.task.parsing.entities.LocationParser;
import org.openhds.mobile.task.parsing.entities.RelationshipParser;
import org.openhds.mobile.task.parsing.entities.SocialGroupParser;
import org.openhds.mobile.task.parsing.entities.VisitParser;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Feed XML documents to a ParseEntityTask and verify that entities were created in the database.
 *
 * BSH
 */
public class ParseEntityTaskTest extends ProviderTestCase2<OpenHDSProvider> {

    private static final String TEST_PASSWORD = "";
    private static final int TASK_TIMEOUT = 10;

    private OpenHDSProvider provider;
    private ContentResolver contentResolver;
    private ParseEntityTask parseEntityTask;

    private class ConstantPasswordHelper implements PasswordHelper {
        @Override
        public String getPassword() {
            return TEST_PASSWORD;
        }
    }

    public ParseEntityTaskTest() {
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

        // test the task against the mock content resolver with no progress dialog
        parseEntityTask = new ParseEntityTask(null, contentResolver);
    }

    @Override
    protected void tearDown() {
        SQLiteOpenHelper databaseHelper = provider.getDatabaseHelper();
        SQLiteDatabase db = databaseHelper.getWritableDatabase(TEST_PASSWORD);
        db.close();
    }

    public void testProcessFieldWorkerXml() throws Exception {
        String fileName = "testXml/field-workers.xml";
        InputStream inputStream = getContext().getAssets().open(fileName);
        FieldWorkerGateway gateway = GatewayRegistry.getFieldWorkerGateway();
        ParseEntityTaskRequest<FieldWorker> parseEntityTaskRequest = new ParseEntityTaskRequest<>(
                fileName,
                inputStream,
                new FieldWorkerParser(),
                gateway);

        // run the task and wait for it to finish
        parseEntityTask.execute(parseEntityTaskRequest);
        parseEntityTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<FieldWorker> entities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(7, entities.size());
    }

    public void testProcessIndividualXml() throws Exception {
        String fileName = "testXml/individuals.xml";
        InputStream inputStream = getContext().getAssets().open(fileName);
        IndividualGateway gateway = GatewayRegistry.getIndividualGateway();
        ParseEntityTaskRequest<Individual> parseEntityTaskRequest = new ParseEntityTaskRequest<>(
                fileName,
                inputStream,
                new IndividualParser(),
                gateway);

        // run the task and wait for it to finish
        parseEntityTask.execute(parseEntityTaskRequest);
        parseEntityTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<Individual> entities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(2, entities.size());
    }

    public void testProcessLocationHierarchyXml() throws Exception {
        String fileName = "testXml/location-hierarchies.xml";
        InputStream inputStream = getContext().getAssets().open(fileName);
        LocationHierarchyGateway gateway = GatewayRegistry.getLocationHierarchyGateway();
        ParseEntityTaskRequest<LocationHierarchy> parseEntityTaskRequest = new ParseEntityTaskRequest<>(
                fileName,
                inputStream,
                new LocationHierarchyParser(),
                gateway);

        // run the task and wait for it to finish
        parseEntityTask.execute(parseEntityTaskRequest);
        parseEntityTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<LocationHierarchy> entities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(2, entities.size());
    }

    public void testProcessLocationXml() throws Exception {
        String fileName = "testXml/locations.xml";
        InputStream inputStream = getContext().getAssets().open(fileName);
        LocationGateway gateway = GatewayRegistry.getLocationGateway();
        ParseEntityTaskRequest<Location> parseEntityTaskRequest = new ParseEntityTaskRequest<>(
                fileName,
                inputStream,
                new LocationParser(),
                gateway);

        // run the task and wait for it to finish
        parseEntityTask.execute(parseEntityTaskRequest);
        parseEntityTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<Location> entities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(2, entities.size());
    }

    public void testProcessRelationshipXml() throws Exception {
        String fileName = "testXml/relationships.xml";
        InputStream inputStream = getContext().getAssets().open(fileName);
        RelationshipGateway gateway = GatewayRegistry.getRelationshipGateway();
        ParseEntityTaskRequest<Relationship> parseEntityTaskRequest = new ParseEntityTaskRequest<>(
                fileName,
                inputStream,
                new RelationshipParser(),
                gateway);

        // run the task and wait for it to finish
        parseEntityTask.execute(parseEntityTaskRequest);
        parseEntityTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<Relationship> entities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(2, entities.size());
    }

    public void testProcessSocialGroupXml() throws Exception {
        String fileName = "testXml/social-groups.xml";
        InputStream inputStream = getContext().getAssets().open(fileName);
        SocialGroupGateway gateway = GatewayRegistry.getSocialGroupGateway();
        ParseEntityTaskRequest<SocialGroup> parseEntityTaskRequest = new ParseEntityTaskRequest<>(
                fileName,
                inputStream,
                new SocialGroupParser(),
                gateway);

        // run the task and wait for it to finish
        parseEntityTask.execute(parseEntityTaskRequest);
        parseEntityTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<SocialGroup> entities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(2, entities.size());
    }

    public void testProcessVisitXml() throws Exception {
        String fileName = "testXml/visits.xml";
        InputStream inputStream = getContext().getAssets().open(fileName);
        VisitGateway gateway = GatewayRegistry.getVisitGateway();
        ParseEntityTaskRequest<Visit> parseEntityTaskRequest = new ParseEntityTaskRequest<>(
                fileName,
                inputStream,
                new VisitParser(),
                gateway);

        // run the task and wait for it to finish
        parseEntityTask.execute(parseEntityTaskRequest);
        parseEntityTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<Visit> entities = gateway.getList(contentResolver, gateway.findAll());
        assertEquals(2, entities.size());
    }



}

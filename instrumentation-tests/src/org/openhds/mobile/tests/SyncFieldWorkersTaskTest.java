package org.openhds.mobile.tests;

import android.content.ContentResolver;
import android.test.ProviderTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.provider.OpenHDSProvider;
import org.openhds.mobile.provider.PasswordHelper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.FieldWorkerGateway;
import org.openhds.mobile.task.HttpTask;
import org.openhds.mobile.task.SyncFieldworkersTask;

import java.io.InputStream;
import java.util.List;

/**
 * Feed XML documents to SyncFieldWorkersTask and verify that FieldWorkers were created in the database.
 *
 * Note, the XML parsing in is quite brittle.  The XML in assets/testXml must
 * not contain extra whitespace.  Otherwise the task might get caught in an infinite
 * loop.  SyncEntitiesTask and SyncFieldWorkersTask are ripe for refactoring!
 *
 * BSH
 */
public class SyncFieldWorkersTaskTest extends ProviderTestCase2<OpenHDSProvider> {

    private static final String TEST_PASSWORD = "";

    private OpenHDSProvider provider;
    private ContentResolver contentResolver;
    private SyncFieldworkersTask syncFieldworkersTask;

    private class ConstantPasswordHelper implements PasswordHelper {
        @Override
        public String getPassword() {
            return TEST_PASSWORD;
        }
    }

    public SyncFieldWorkersTaskTest() {
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
        syncFieldworkersTask = new SyncFieldworkersTask(null, contentResolver, null, null);
    }

    @Override
    protected void tearDown() {
        SQLiteOpenHelper databaseHelper = provider.getDatabaseHelper();
        SQLiteDatabase db = databaseHelper.getWritableDatabase(TEST_PASSWORD);
        db.close();
    }

    public void testProcessFieldWorkerXML() throws Exception {
        InputStream inputStream = getContext().getAssets().open("testXml/field-workers.xml");
        syncFieldworkersTask.processXMLDocument(inputStream);

        FieldWorkerGateway fieldWorkerGateway = GatewayRegistry.getFieldWorkerGateway();
        List<FieldWorker> fieldWorkers = fieldWorkerGateway.getList(contentResolver, fieldWorkerGateway.findAll());
        assertEquals(2, fieldWorkers.size());
    }

}

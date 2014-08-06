package org.openhds.mobile.tests;

import android.content.ContentResolver;
import android.test.ProviderTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.provider.OpenHDSProvider;
import org.openhds.mobile.provider.PasswordHelper;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.Gateway;

import java.util.List;

public abstract class GatewayTest<T> extends ProviderTestCase2<OpenHDSProvider> {

    private static final String TEST_PASSWORD = "";

    private OpenHDSProvider provider;
    private ContentResolver contentResolver;

    protected Gateway<T> gateway;

    private class ConstantPasswordHelper implements PasswordHelper {
        @Override
        public String getPassword() {
            return TEST_PASSWORD;
        }
    }

    // subclass constructor must provide specific gateway implementation
    public GatewayTest(Gateway<T> gateway) {
        super(OpenHDSProvider.class, OpenHDS.AUTHORITY);
        this.gateway = gateway;
    }

    protected abstract T makeTestEntity(String id, String name);

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
    }

    @Override
    protected void tearDown() {
        SQLiteOpenHelper databaseHelper = provider.getDatabaseHelper();
        SQLiteDatabase db = databaseHelper.getWritableDatabase(TEST_PASSWORD);
        db.close();
    }

    public void testSafeToFindWhenEmpty() {
        List<T> allEntities = gateway.findAll(contentResolver);
        assertEquals(0, allEntities.size());

        T entity = gateway.findById(contentResolver, "INVALID");
        assertNull(entity);
    }

    public void testAdd() {
        String id = "TEST";
        T entity = makeTestEntity(id, "mr. test");

        boolean wasInserted = gateway.insertOrUpdate(contentResolver, entity);
        assertEquals(true, wasInserted);

        T savedEntity = gateway.findById(contentResolver, id);
        assertNotNull(savedEntity);
        String savedId = gateway.getConverter().getId(savedEntity);
        assertEquals(id, savedId);

        wasInserted = gateway.insertOrUpdate(contentResolver, entity);
        assertEquals(false, wasInserted);

        savedEntity = gateway.findById(contentResolver, id);
        assertNotNull(savedEntity);
        savedId = gateway.getConverter().getId(savedEntity);
        assertEquals(id, savedId);

        List<T> allEntities = gateway.findAll(contentResolver);
        assertEquals(1, allEntities.size());
    }

    public void testFindAll() {
        T entity1 = makeTestEntity("TEST1", "first person");
        T entity2 = makeTestEntity("TEST2", "second person");
        gateway.insertOrUpdate(contentResolver, entity1);
        gateway.insertOrUpdate(contentResolver, entity2);

        List<T> allEntities = gateway.findAll(contentResolver);
        assertEquals(2, allEntities.size());

        String id1 = gateway.getConverter().getId(allEntities.get(0));
        String id2 = gateway.getConverter().getId(allEntities.get(1));
        assertNotSame(id1, id2);
    }

    public void testFindAllAsIterator() {
        // TODO: implement iterators!
    }

    public void testDelete() {
        String id = "TEST";
        T entity = makeTestEntity(id, "mr. test");

        boolean wasInserted = gateway.insertOrUpdate(contentResolver, entity);

        T savedEntity = gateway.findById(contentResolver, id);
        assertNotNull(savedEntity);
        String savedId = gateway.getConverter().getId(savedEntity);
        assertEquals(id, savedId);

        boolean wasDeleted = gateway.deleteById(contentResolver, id);
        assertTrue(wasDeleted);

        wasDeleted = gateway.deleteById(contentResolver, id);
        assertFalse(wasDeleted);

        savedEntity = gateway.findById(contentResolver, id);
        assertNull(savedEntity);
    }
}

package org.openhds.mobile.tests;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.test.ProviderTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.provider.OpenHDSProvider;
import org.openhds.mobile.provider.PasswordHelper;
import org.openhds.mobile.repository.IndividualGateway;

import java.util.List;

public class IndividualGatewayTest extends ProviderTestCase2<OpenHDSProvider> {

    private IndividualGateway individualGateway;
    private OpenHDSProvider provider;
    private ContentResolver contentResolver;

    private final String TEST_PASSWORD = "";

    public IndividualGatewayTest () {
        super(OpenHDSProvider.class, OpenHDS.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.individualGateway = new IndividualGateway();
        this.provider = (OpenHDSProvider) getProvider();
        this.contentResolver = getMockContentResolver();

        // inject a password helper that uses a known password and
        // doesn't use shared preferences, which are not enabled under ProviderTestCase2
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
        List<Individual> allIndividuals = individualGateway.findAll(contentResolver);
        assertEquals(0, allIndividuals.size());

        Individual individual = individualGateway.findById(contentResolver, "INVALID");
        assertNull(individual);
    }

    public void testAdd() {
        Individual individual = getTestIndividual("TEST", "mr. test");

        boolean wasInserted = individualGateway.insertOrUpdate(contentResolver, individual);
        assertEquals(true, wasInserted);

        Individual savedIndividual = individualGateway.findById(contentResolver, individual.getExtId());
        assertNotNull(savedIndividual);
        assertEquals(individual.getExtId(), savedIndividual.getExtId());

        wasInserted = individualGateway.insertOrUpdate(contentResolver, individual);
        assertEquals(false, wasInserted);

        savedIndividual = individualGateway.findById(contentResolver, individual.getExtId());
        assertNotNull(savedIndividual);
        assertEquals(individual.getExtId(), savedIndividual.getExtId());

        List<Individual> allIndividuals = individualGateway.findAll(contentResolver);
        assertEquals(1, allIndividuals.size());
    }

    private static Individual getTestIndividual(String extId, String name) {
        Individual individual = new Individual();

        individual.setExtId(extId);
        individual.setFirstName(name);
        individual.setLastName(name);
        individual.setDob("2000-01-01 00:00:00");
        individual.setGender("M");
        individual.setMother("MOTHER");
        individual.setFather("FATHER");
        individual.setCurrentResidence("LOCATION");
        individual.setEndType("N/A");
        individual.setOtherId("OTHER");
        individual.setOtherNames(name);
        individual.setAge("50");
        individual.setAgeUnits("YEARS");
        individual.setPhoneNumber("1234567890");
        individual.setOtherPhoneNumber("0987654321");
        individual.setPointOfContactName("CONTACT");
        individual.setMemberStatus("PERMANENT");
        individual.setPointOfContactPhoneNumber("111111111");
        individual.setLanguagePreference("ENGLISH");

        return individual;
    }

    private class ConstantPasswordHelper implements PasswordHelper {

        @Override
        public String getPassword() {
            return TEST_PASSWORD;
        }
    }

}

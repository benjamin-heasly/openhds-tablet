package org.openhds.mobile.tests;

import android.content.ContentResolver;
import android.test.ProviderTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.provider.OpenHDSProvider;
import org.openhds.mobile.repository.IndividualGateway;

import java.util.List;

public class IndividualGatewayTest extends ProviderTestCase2<OpenHDSProvider> {

    private IndividualGateway individualGateway;
    private OpenHDSProvider provider;
    private ContentResolver contentResolver;

    public IndividualGatewayTest () {
        super(OpenHDSProvider.class, OpenHDS.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.individualGateway = new IndividualGateway();
        this.provider = (OpenHDSProvider) getProvider();
        this.contentResolver = getMockContentResolver();

        // make sure we have a fresh database for each test
        // TODO: coordinate test database password by injecting
        // TODO: a context capable of returning a shared preferences with a known password configured here
        // TODO: for example:
        // TODO: http://stackoverflow.com/questions/5267671/unsupportedoperationexception-while-calling-getsharedpreferences-from-unit-tes
        SQLiteOpenHelper databaseHelper = provider.getDatabaseHelper();
        SQLiteDatabase.loadLibs(getContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase("");
        databaseHelper.onUpgrade(db, 0, 0);
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
}

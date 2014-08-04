package org.openhds.mobile.tests;

import android.test.ProviderTestCase2;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.provider.OpenHDSProvider;
import org.openhds.mobile.repository.IndividualGateway;

import java.util.List;

public class IndividualGatewayTest extends ProviderTestCase2<OpenHDSProvider> {

    private IndividualGateway individualGateway;

    public IndividualGatewayTest () {
        super(OpenHDSProvider.class, OpenHDS.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.individualGateway = new IndividualGateway();
    }

    public void testSafeToFindWhenEmpty() {
        List<Individual> allIndividuals = individualGateway.findAll(getMockContentResolver());
        assertEquals(0, allIndividuals.size());

        Individual individual = individualGateway.findById(getMockContentResolver(), "INVALID");
        assertNull(individual);
    }

    public void testAdd() {
        Individual individual = getTestindividual("TEST", "mr. test");
    }

    private static Individual getTestindividual(String extId, String name) {
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

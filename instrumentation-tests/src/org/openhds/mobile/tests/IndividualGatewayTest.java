package org.openhds.mobile.tests;

import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.repository.IndividualGateway;

import java.util.List;

import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME;

public class IndividualGatewayTest extends GatewayTest<Individual> {

    private IndividualGateway individualGateway;

    public IndividualGatewayTest() {
        super(new IndividualGateway());
        this.individualGateway = (IndividualGateway) this.gateway;
    }

    // find individuals with similar ids
    public void testFindByExtIdPrefix() {
        List<Individual> shouldBeEmpty = individualGateway.findByExtIdPrefix(contentResolver, "TEST");
        assertEquals(0, shouldBeEmpty.size());

        Individual individual1 = makeTestEntity("TEST1", "test 1");
        Individual individual2 = makeTestEntity("TEST2", "test 2");
        Individual individual3 = makeTestEntity("TRIAL3", "test 3");

        individualGateway.insertOrUpdate(contentResolver, individual1);
        individualGateway.insertOrUpdate(contentResolver, individual2);
        individualGateway.insertOrUpdate(contentResolver, individual3);

        shouldBeEmpty = individualGateway.findByExtIdPrefix(contentResolver, "DOESNOTEXIST");
        assertEquals(0, shouldBeEmpty.size());

        List<Individual> byPrefix = individualGateway.findByExtIdPrefix(contentResolver, "TEST");
        assertEquals(2, byPrefix.size());

        byPrefix = individualGateway.findByExtIdPrefix(contentResolver, "TRIAL");
        assertEquals(1, byPrefix.size());
        assertEquals("TRIAL3", byPrefix.get(0).getExtId());
    }

    // find individuals with the same residency
    public void testFindByResidency() {
        Location residency = new Location();
        residency.setExtId("FINDME");
        List<Individual> shouldBeEmpty = individualGateway.findByResidency(contentResolver, residency);
        assertEquals(0, shouldBeEmpty.size());

        Individual individual1 = makeTestEntity("TEST1", "test 1");
        Individual individual2 = makeTestEntity("TEST2", "test 2");
        Individual individual3 = makeTestEntity("TEST3", "test 3");

        individual1.setCurrentResidence(residency.getExtId());
        individual2.setCurrentResidence(residency.getExtId());

        individualGateway.insertOrUpdate(contentResolver, individual1);
        individualGateway.insertOrUpdate(contentResolver, individual2);
        individualGateway.insertOrUpdate(contentResolver, individual3);

        Location nobodyLivesHere = new Location();
        nobodyLivesHere.setExtId("DOESNOTEXIST");
        shouldBeEmpty = individualGateway.findByResidency(contentResolver, nobodyLivesHere);
        assertEquals(0, shouldBeEmpty.size());

        List<Individual> byResidency = individualGateway.findByResidency(contentResolver, residency);
        assertEquals(2, byResidency.size());
    }

    // find individuals by some individual-specific criteria: first and last name
    public void testFindByCriteriaEqual() {
        final String[] columnNames = {COLUMN_INDIVIDUAL_FIRST_NAME, COLUMN_INDIVIDUAL_LAST_NAME};
        final String[] columnValues = {"Sam", "Smith"};
        List<Individual> shouldBeEmpty = individualGateway.findByCriteriaEqual(
                contentResolver, columnNames, columnValues, COLUMN_INDIVIDUAL_FIRST_NAME);
        assertEquals(0, shouldBeEmpty.size());

        Individual individual1 = makeTestEntity("TEST1", "test 1");
        Individual individual2 = makeTestEntity("TEST2", "test 2");
        Individual individual3 = makeTestEntity("TEST3", "test 3");

        individual1.setFirstName("Sam");
        individual2.setFirstName("Sal");
        individual3.setFirstName("Sam");

        individual1.setLastName("Smith");
        individual2.setLastName("Smith");
        individual3.setLastName("Doe");

        individualGateway.insertOrUpdate(contentResolver, individual1);
        individualGateway.insertOrUpdate(contentResolver, individual2);
        individualGateway.insertOrUpdate(contentResolver, individual3);

        List<Individual> byCriteria = individualGateway.findByCriteriaEqual(
                contentResolver, columnNames, columnValues, COLUMN_INDIVIDUAL_FIRST_NAME);
        assertEquals(1, byCriteria.size());
        assertEquals("Sam", byCriteria.get(0).getFirstName());
        assertEquals("Smith", byCriteria.get(0).getLastName());
    }

    public void testFindByCriteriaEqualAsIterator() {
        // TODO: implement itarators!
    }

    // find individuals by pattern matching some individual-specific criteria: first and last name
    public void testFindByCriteriaLike() {
        // expect _a_ to match Sam and Sal.  Expect S% to match Smith
        final String[] columnNames = {COLUMN_INDIVIDUAL_FIRST_NAME, COLUMN_INDIVIDUAL_LAST_NAME};
        final String[] columnValues = {"_a_", "S%"};
        List<Individual> shouldBeEmpty = individualGateway.findByCriteriaLike(
                contentResolver, columnNames, columnValues, COLUMN_INDIVIDUAL_FIRST_NAME);
        assertEquals(0, shouldBeEmpty.size());

        Individual individual1 = makeTestEntity("TEST1", "test 1");
        Individual individual2 = makeTestEntity("TEST2", "test 2");
        Individual individual3 = makeTestEntity("TEST3", "test 3");

        individual1.setFirstName("Sam");
        individual2.setFirstName("Sal");
        individual3.setFirstName("Sam");

        individual1.setLastName("Smith");
        individual2.setLastName("Smith");
        individual3.setLastName("Doe");

        individualGateway.insertOrUpdate(contentResolver, individual1);
        individualGateway.insertOrUpdate(contentResolver, individual2);
        individualGateway.insertOrUpdate(contentResolver, individual3);

        List<Individual> byCriteria = individualGateway.findByCriteriaLike(
                contentResolver, columnNames, columnValues, COLUMN_INDIVIDUAL_FIRST_NAME);
        assertEquals(2, byCriteria.size());
    }

    public void testFindByCriteriaLikeAsIterator() {
        // TODO: implement itarators!
    }

    @Override
    protected Individual makeTestEntity(String id, String name) {
        Individual individual = new Individual();

        individual.setExtId(id);
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

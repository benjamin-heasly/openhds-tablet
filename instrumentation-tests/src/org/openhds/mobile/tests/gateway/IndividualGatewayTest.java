package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.repository.Query;
import org.openhds.mobile.repository.gateway.IndividualGateway;

import java.util.List;

import static org.openhds.mobile.OpenHDS.Individuals.FIRST_NAME;
import static org.openhds.mobile.OpenHDS.Individuals.LAST_NAME;

public class IndividualGatewayTest extends GatewayTest<Individual> {

    private IndividualGateway individualGateway;

    public IndividualGatewayTest() {
        super(new IndividualGateway());
        this.individualGateway = (IndividualGateway) this.gateway;
    }

    // find individuals with similar ids
    public void testFindByExtIdPrefix() {
        Query query = individualGateway.findByExtIdPrefixDescending("TEST");
        List<Individual> shouldBeEmpty = individualGateway.getList(contentResolver, query);
        assertEquals(0, shouldBeEmpty.size());

        Individual individual1 = makeTestEntity("TEST1", "test 1");
        Individual individual2 = makeTestEntity("TEST2", "test 2");
        Individual individual3 = makeTestEntity("TRIAL3", "test 3");

        individualGateway.insertOrUpdate(contentResolver, individual1);
        individualGateway.insertOrUpdate(contentResolver, individual2);
        individualGateway.insertOrUpdate(contentResolver, individual3);

        query = individualGateway.findByExtIdPrefixDescending("DOESNOTEXIST");
        shouldBeEmpty = individualGateway.getList(contentResolver, query);
        assertEquals(0, shouldBeEmpty.size());

        query = individualGateway.findByExtIdPrefixDescending("TEST");
        List<Individual> byPrefix = individualGateway.getList(contentResolver, query);
        assertEquals(2, byPrefix.size());

        query = individualGateway.findByExtIdPrefixDescending("TRIAL");
        byPrefix = individualGateway.getList(contentResolver, query);
        assertEquals(1, byPrefix.size());
        assertEquals("TRIAL3", byPrefix.get(0).getExtId());
    }

    // find individuals by some individual-specific criteria: first and last name
    public void testFindByCriteriaEqual() {
        final String[] columnNames = {FIRST_NAME, LAST_NAME};
        final String[] columnValues = {"Sam", "Smith"};
        Query query = individualGateway.findByCriteriaEqual(columnNames, columnValues, FIRST_NAME);

        List<Individual> shouldBeEmpty = individualGateway.getList(contentResolver, query);
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

        List<Individual> byCriteria = individualGateway.getList(contentResolver, query);
        assertEquals(1, byCriteria.size());
        assertEquals("Sam", byCriteria.get(0).getFirstName());
        assertEquals("Smith", byCriteria.get(0).getLastName());
    }

    // find individuals by pattern matching some individual-specific criteria: first and last name
    public void testFindByCriteriaLike() {
        // expect _a_ to match Sam and Sal.  Expect S% to match Smith
        final String[] columnNames = {FIRST_NAME, LAST_NAME};
        final String[] columnValues = {"_a_", "S%"};
        Query query = individualGateway.findByCriteriaLike(columnNames, columnValues, FIRST_NAME);

        List<Individual> shouldBeEmpty = individualGateway.getList(contentResolver, query);
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

        List<Individual> byCriteria = individualGateway.getList(contentResolver, query);
        assertEquals(2, byCriteria.size());
    }

    @Override
    protected Individual makeTestEntity(String id, String name) {
        Individual individual = new Individual();

        individual.setUuid(id);
        individual.setExtId(id);
        individual.setFirstName(name);
        individual.setLastName(name);
        individual.setDob("2000-01-01 00:00:00");
        individual.setGender("M");
        individual.setMother("MOTHER");
        individual.setFather("FATHER");

        return individual;
    }
}

package org.openhds.mobile.tests;

import android.content.ContentResolver;
import android.test.ProviderTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.provider.OpenHDSProvider;
import org.openhds.mobile.provider.PasswordHelper;
import org.openhds.mobile.repository.IndividualGateway;

import java.util.List;

public class IndividualGatewayTest extends GatewayTest<Individual> {

    private IndividualGateway individualGateway;

    public IndividualGatewayTest() {
        super(new IndividualGateway());
        this.individualGateway = (IndividualGateway) this.gateway;
    }

//    + testFindByExtIdPrefix(String) // expect sizes when empty, no matches, matches
//    + testFindByResidency(Residency) // expect sizes when empty, no matches, matches
//    + testFindByCriteriaEqual(columnNames[], columnValues[]) // use criteria like testFindByResidency
//    + testFindByCriteriaLike(columnNames[], columnValues[]) // use criteria like testFindByExtIdPrefix
//    + testFindByCriteriaEqualAsIterator(columnNames[], columnValues[]) // use criteria like testFindByResidency
//    + testFindByCriteriaLikeAsIterator(columnNames[], columnValues[]) // use criteria like testFindByExtIdPrefix

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

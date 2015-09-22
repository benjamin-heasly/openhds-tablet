package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to Individuals.
 */
public class IndividualParser extends EntityParser<Individual> {

    private static final String pageName = "individual";

    @Override
    protected Individual toEntity(DataPage dataPage) {
        Individual individual = new Individual();

        individual.setUuid(dataPage.getFirstString(asList("uuid")));
        individual.setExtId(dataPage.getFirstString(asList("extId")));
        individual.setFirstName(dataPage.getFirstString(asList("firstName")));
        individual.setLastName(dataPage.getFirstString(asList("lastName")));
        individual.setMiddleName(dataPage.getFirstString(asList("middleName")));
        individual.setDob(dataPage.getFirstString(asList("dob")));
        individual.setGender(dataPage.getFirstString(asList("gender")));
        individual.setMother(dataPage.getFirstString(asList("mother", "uuid")));
        individual.setFather(dataPage.getFirstString(asList("father", "uuid")));

        return individual;
    }
}

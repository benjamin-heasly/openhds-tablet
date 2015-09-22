package org.openhds.mobile.projectdata.FormAdapters;

import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.projectdata.ProjectFormFields;

import java.util.HashMap;
import java.util.Map;

import static org.openhds.mobile.OpenHDS.Individuals.*;

public class IndividualFormAdapter {

    public static Individual fromForm(Map<String, String> formInstanceData) {
        Individual individual = new Individual();

        individual.setUuid(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(UUID)));
        individual.setExtId(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(EXT_ID)));
        individual.setFirstName(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(FIRST_NAME)));
        individual.setLastName(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(LAST_NAME)));
        individual.setDob(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(DOB)));
        individual.setGender(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(GENDER)));
        individual.setMother(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(MOTHER)));
        individual.setFather(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(FATHER)));

        return individual;
    }

    public static Map<String, String> toForm( Individual individual) {
        Map<String, String> formFields = new HashMap<String, String>();

        formFields.put(ProjectFormFields.General.ENTITY_UUID, individual.getUuid());
        formFields.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID, individual.getExtId());
        formFields.put(ProjectFormFields.Individuals.FIRST_NAME, individual.getFirstName());
        formFields.put(ProjectFormFields.Individuals.LAST_NAME, individual.getLastName());
        formFields.put(ProjectFormFields.Individuals.DATE_OF_BIRTH, individual.getDob());
        formFields.put(ProjectFormFields.Individuals.GENDER, individual.getGender());
        formFields.put(ProjectFormFields.Individuals.MOTHER_EXTID, individual.getMother());
        formFields.put(ProjectFormFields.Individuals.FATHER_EXTID, individual.getFather());

        return formFields;
    }
}

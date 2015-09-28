package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to FieldWorkers.
 */
public class FieldWorkerParser extends EntityParser<FieldWorker> {

    private static final String pageName = "fieldworker";

    @Override
    protected FieldWorker toEntity(DataPage dataPage) {
        FieldWorker fieldWorker = new FieldWorker();

        fieldWorker.setPasswordHash(dataPage.getFirstString(asList("passwordHash")));
        fieldWorker.setFieldWorkerId(dataPage.getFirstString(asList("fieldWorkerId")));
        fieldWorker.setFirstName(dataPage.getFirstString(asList("firstName")));
        fieldWorker.setLastName(dataPage.getFirstString(asList("lastName")));
        fieldWorker.setUuid(dataPage.getFirstString(asList("uuid")));
        fieldWorker.setLastModifiedServer(dataPage.getFirstString(asList("lastModifiedDate")));

        return fieldWorker;
    }
}

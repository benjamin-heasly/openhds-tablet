package org.openhds.mobile.tests.forms;

import android.test.AndroidTestCase;

import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.utilities.FileUtils;

import java.io.File;

public class FormContentTest extends AndroidTestCase {

    public void testReadHouseholdFormContent() throws Exception {
        File file = FileUtils.writeAssetTempFile(getContext(), "testForms/test-individual-household-instance.xml", "testReadHouseholdFormContent.xml");
        FormContent formContent = FormContent.readFormContent(file);
        assertNotNull(formContent);

        // top-level elements
        assertEquals("101", formContent.getContentString(FormContent.TOP_LEVEL_ALIAS, "registrationVersion"));
        assertEquals("openhds-tablet", formContent.getContentString(FormContent.TOP_LEVEL_ALIAS, "registrationSystemName"));
        assertEquals("2015-06-10T15:30:00.000Z", formContent.getContentString(FormContent.TOP_LEVEL_ALIAS, "registrationDateTime"));
        assertEquals("UNKNOWN", formContent.getContentString(FormContent.TOP_LEVEL_ALIAS, "relationToHead"));

        // top-level elements with special case UUID syntax
        assertEquals("UNKNOWN", formContent.getContentString("collectedBy", FormContent.UUID_FIELD_NAME));
        assertEquals("UNKNOWN", formContent.getContentString("location", FormContent.UUID_FIELD_NAME));
        assertEquals("UNKNOWN", formContent.getContentString("mother", FormContent.UUID_FIELD_NAME));
        assertEquals("UNKNOWN", formContent.getContentString("father", FormContent.UUID_FIELD_NAME));

        // nested individual elements
        assertEquals("TEST_INDIVIDUAL", formContent.getContentString("individual", "uuid"));
        assertEquals("2015-09-21T19:15:31.199Z[UTC]", formContent.getContentString("individual", "collectionDateTime"));
        assertEquals("test-individual", formContent.getContentString("individual", "extId"));
    }

    public void testReadLocationFormContent() throws Exception {
        File file = FileUtils.writeAssetTempFile(getContext(), "testForms/test-location-instance.xml", "testReadLocationFormContent.xml");
        FormContent formContent = FormContent.readFormContent(file);
        assertNotNull(formContent);

        // top-level elements
        assertEquals("101", formContent.getContentString(FormContent.TOP_LEVEL_ALIAS, "registrationVersion"));
        assertEquals("openhds-tablet", formContent.getContentString(FormContent.TOP_LEVEL_ALIAS, "registrationSystemName"));
        assertEquals("2015-06-10T15:30:00.000Z", formContent.getContentString(FormContent.TOP_LEVEL_ALIAS, "registrationDateTime"));

        // top-level elements with special case UUID syntax
        assertEquals("UNKNOWN", formContent.getContentString("collectedBy", FormContent.UUID_FIELD_NAME));
        assertEquals("UNKNOWN", formContent.getContentString("locationHierarchy", FormContent.UUID_FIELD_NAME));

        // nested individual elements
        assertEquals("TEST_LOCATION", formContent.getContentString("location", "uuid"));
        assertEquals("2015-09-21T19:15:31.199Z[UTC]", formContent.getContentString("location", "collectionDateTime"));
        assertEquals("test-location", formContent.getContentString("location", "description"));
    }

    public void testWriteHouseholdFormContent() throws Exception {
        File file = FileUtils.writeAssetTempFile(getContext(), "testForms/test-individual-household-instance.xml", "testWriteHouseholdFormContent.xml");

        // new values to write out
        FormContent formContentOut = new FormContent();
        formContentOut.setContent(FormContent.TOP_LEVEL_ALIAS, "relationToHead", "relationValue");
        formContentOut.setContent(FormContent.TOP_LEVEL_ALIAS, "notATopLevelElement", "notAValue");
        formContentOut.setContent("collectedBy", FormContent.UUID_FIELD_NAME, "uuidValue");
        formContentOut.setContent("notAnAliasUuid", FormContent.UUID_FIELD_NAME, "notAValue");
        formContentOut.setContent("individual", "extId", "extIdValue");
        formContentOut.setContent("notAnAlias", "extId", "notAValue");
        formContentOut.setContent("individual", "notAFieldName", "notAValue");

        // write out and read back in
        assertTrue(formContentOut.writeFormContent(file));
        FormContent formContentIn = FormContent.readFormContent(file);
        assertNotNull(formContentIn);

        // well-formed values must match
        assertEquals("relationValue", formContentIn.getContentString(FormContent.TOP_LEVEL_ALIAS, "relationToHead"));
        assertEquals("uuidValue", formContentIn.getContentString("collectedBy", FormContent.UUID_FIELD_NAME));
        assertEquals("extIdValue", formContentIn.getContentString("individual", "extId"));

        // malformed values should not have been written
        assertFalse(formContentIn.hasContent(FormContent.TOP_LEVEL_ALIAS, "notATopLevelElement"));
        assertFalse(formContentIn.hasContent("notAnAliasUuid", FormContent.UUID_FIELD_NAME));
        assertFalse(formContentIn.hasContent("notAnAlias", "extId"));
        assertFalse(formContentIn.hasContent("individual", "notAFieldName"));
    }

    public void testWriteLocationFormContent() throws Exception {
        File file = FileUtils.writeAssetTempFile(getContext(), "testForms/test-location-instance.xml", "testWriteLocationFormContent.xml");

        // new values to write out
        FormContent formContentOut = new FormContent();
        formContentOut.setContent(FormContent.TOP_LEVEL_ALIAS, "registrationVersion", "registrationValue");
        formContentOut.setContent(FormContent.TOP_LEVEL_ALIAS, "notATopLevelElement", "notAValue");
        formContentOut.setContent("locationHierarchy", FormContent.UUID_FIELD_NAME, "uuidValue");
        formContentOut.setContent("notAnAliasUuid", FormContent.UUID_FIELD_NAME, "notAValue");
        formContentOut.setContent("location", "description", "descriptionValue");
        formContentOut.setContent("notAnAlias", "description", "notAValue");
        formContentOut.setContent("location", "notAFieldName", "notAValue");

        // write out and read back in
        assertTrue(formContentOut.writeFormContent(file));
        FormContent formContentIn = FormContent.readFormContent(file);
        assertNotNull(formContentIn);

        // well-formed values must match
        assertEquals("registrationValue", formContentIn.getContentString(FormContent.TOP_LEVEL_ALIAS, "registrationVersion"));
        assertEquals("uuidValue", formContentIn.getContentString("locationHierarchy", FormContent.UUID_FIELD_NAME));
        assertEquals("descriptionValue", formContentIn.getContentString("location", "description"));

        // malformed values should not have been written
        assertFalse(formContentIn.hasContent(FormContent.TOP_LEVEL_ALIAS, "notATopLevelElement"));
        assertFalse(formContentIn.hasContent("notAnAliasUuid", FormContent.UUID_FIELD_NAME));
        assertFalse(formContentIn.hasContent("notAnAlias", "extId"));
        assertFalse(formContentIn.hasContent("location", "notAFieldName"));
    }

}

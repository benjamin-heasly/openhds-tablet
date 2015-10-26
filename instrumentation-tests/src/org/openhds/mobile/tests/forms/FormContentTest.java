package org.openhds.mobile.tests.forms;

import android.test.AndroidTestCase;

import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.forms.FormInstance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FormContentTest extends AndroidTestCase {

    public void testReadHouseholdFormContent() throws Exception {
        File file = writeAssetTempFile("testForms/test-individual-household-instance.xml", "testReadHouseholdFormContent.xml");
        FormInstance formInstance = new FormInstance();
        formInstance.setFilePath(file.getAbsolutePath());

        FormContent formContent = formInstance.readFormContent();
        assertNotNull(formContent);

        // top-level elements
        assertEquals("101", formContent.getContentString(FormInstance.TOP_LEVEL_ALIAS, "registrationVersion"));
        assertEquals("openhds-tablet", formContent.getContentString(FormInstance.TOP_LEVEL_ALIAS, "registrationSystemName"));
        assertEquals("2015-06-10T15:30:00.000Z", formContent.getContentString(FormInstance.TOP_LEVEL_ALIAS, "registrationDateTime"));
        assertEquals("UNKNOWN", formContent.getContentString(FormInstance.TOP_LEVEL_ALIAS, "relationToHead"));

        // top-level elements with special case UUID syntax
        assertEquals("UNKNOWN", formContent.getContentString("collectedBy", FormInstance.UUID_FIELD_NAME));
        assertEquals("UNKNOWN", formContent.getContentString("location", FormInstance.UUID_FIELD_NAME));
        assertEquals("UNKNOWN", formContent.getContentString("mother", FormInstance.UUID_FIELD_NAME));
        assertEquals("UNKNOWN", formContent.getContentString("father", FormInstance.UUID_FIELD_NAME));

        // nested individual elements
        assertEquals("TEST_INDIVIDUAL", formContent.getContentString("individual", "uuid"));
        assertEquals("2015-09-21T19:15:31.199Z[UTC]", formContent.getContentString("individual", "collectionDateTime"));
        assertEquals("test-individual", formContent.getContentString("individual", "extId"));
    }

    public void testReadLocationFormContent() throws Exception {
        File file = writeAssetTempFile("testForms/test-location-instance.xml", "testReadLocationFormContent.xml");
        FormInstance formInstance = new FormInstance();
        formInstance.setFilePath(file.getAbsolutePath());

        FormContent formContent = formInstance.readFormContent();
        assertNotNull(formContent);

        // top-level elements
        assertEquals("101", formContent.getContentString(FormInstance.TOP_LEVEL_ALIAS, "registrationVersion"));
        assertEquals("openhds-tablet", formContent.getContentString(FormInstance.TOP_LEVEL_ALIAS, "registrationSystemName"));
        assertEquals("2015-06-10T15:30:00.000Z", formContent.getContentString(FormInstance.TOP_LEVEL_ALIAS, "registrationDateTime"));

        // top-level elements with special case UUID syntax
        assertEquals("UNKNOWN", formContent.getContentString("collectedBy", FormInstance.UUID_FIELD_NAME));
        assertEquals("UNKNOWN", formContent.getContentString("locationHierarchy", FormInstance.UUID_FIELD_NAME));

        // nested individual elements
        assertEquals("TEST_LOCATION", formContent.getContentString("location", "uuid"));
        assertEquals("2015-09-21T19:15:31.199Z[UTC]", formContent.getContentString("location", "collectionDateTime"));
        assertEquals("test-location", formContent.getContentString("location", "description"));
    }

    public void testWriteHouseholdFormContent() throws Exception {
        File file = writeAssetTempFile("testForms/test-individual-household-instance.xml", "testWriteHouseholdFormContent.xml");
        FormInstance formInstance = new FormInstance();
        formInstance.setFilePath(file.getAbsolutePath());

        // new values to write out
        FormContent formContentOut = new FormContent();
        formContentOut.setContent(FormInstance.TOP_LEVEL_ALIAS, "relationToHead", "relationValue");
        formContentOut.setContent(FormInstance.TOP_LEVEL_ALIAS, "notATopLevelElement", "notAValue");
        formContentOut.setContent("collectedBy", FormInstance.UUID_FIELD_NAME, "uuidValue");
        formContentOut.setContent("notAnAliasUuid", FormInstance.UUID_FIELD_NAME, "notAValue");
        formContentOut.setContent("individual", "extId", "extIdValue");
        formContentOut.setContent("notAnAlias", "extId", "notAValue");
        formContentOut.setContent("individual", "notAFieldName", "notAValue");

        // write out and read back in
        assertTrue(formInstance.writeFormContent(formContentOut));
        FormContent formContentIn = formInstance.readFormContent();
        assertNotNull(formContentIn);

        // well-formed values must match
        assertEquals("relationValue", formContentIn.getContentString(FormInstance.TOP_LEVEL_ALIAS, "relationToHead"));
        assertEquals("uuidValue", formContentIn.getContentString("collectedBy", FormInstance.UUID_FIELD_NAME));
        assertEquals("extIdValue", formContentIn.getContentString("individual", "extId"));

        // malformed values should not have been written
        assertFalse(formContentIn.hasContent(FormInstance.TOP_LEVEL_ALIAS, "notATopLevelElement"));
        assertFalse(formContentIn.hasContent("notAnAliasUuid", FormInstance.UUID_FIELD_NAME));
        assertFalse(formContentIn.hasContent("notAnAlias", "extId"));
        assertFalse(formContentIn.hasContent("individual", "notAFieldName"));
    }

    public void testWriteLocationFormContent() throws Exception {
        File file = writeAssetTempFile("testForms/test-location-instance.xml", "testWriteLocationFormContent.xml");
        FormInstance formInstance = new FormInstance();
        formInstance.setFilePath(file.getAbsolutePath());

        // new values to write out
        FormContent formContentOut = new FormContent();
        formContentOut.setContent(FormInstance.TOP_LEVEL_ALIAS, "registrationVersion", "registrationValue");
        formContentOut.setContent(FormInstance.TOP_LEVEL_ALIAS, "notATopLevelElement", "notAValue");
        formContentOut.setContent("locationHierarchy", FormInstance.UUID_FIELD_NAME, "uuidValue");
        formContentOut.setContent("notAnAliasUuid", FormInstance.UUID_FIELD_NAME, "notAValue");
        formContentOut.setContent("location", "description", "descriptionValue");
        formContentOut.setContent("notAnAlias", "description", "notAValue");
        formContentOut.setContent("location", "notAFieldName", "notAValue");

        // write out and read back in
        assertTrue(formInstance.writeFormContent(formContentOut));
        FormContent formContentIn = formInstance.readFormContent();
        assertNotNull(formContentIn);

        // well-formed values must match
        assertEquals("registrationValue", formContentIn.getContentString(FormInstance.TOP_LEVEL_ALIAS, "registrationVersion"));
        assertEquals("uuidValue", formContentIn.getContentString("locationHierarchy", FormInstance.UUID_FIELD_NAME));
        assertEquals("descriptionValue", formContentIn.getContentString("location", "description"));

        // malformed values should not have been written
        assertFalse(formContentIn.hasContent(FormInstance.TOP_LEVEL_ALIAS, "notATopLevelElement"));
        assertFalse(formContentIn.hasContent("notAnAliasUuid", FormInstance.UUID_FIELD_NAME));
        assertFalse(formContentIn.hasContent("notAnAlias", "extId"));
        assertFalse(formContentIn.hasContent("location", "notAFieldName"));
    }

    private File writeAssetTempFile(String assetName, String fileName) {
        File file = new File(getContext().getCacheDir() + "/" + fileName);
        if (!file.exists()) try {

            InputStream inputStream = getContext().getAssets().open(assetName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(buffer);
            fileOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return file;
    }

}

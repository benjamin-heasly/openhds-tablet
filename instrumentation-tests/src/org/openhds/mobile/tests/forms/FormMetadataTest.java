package org.openhds.mobile.tests.forms;

import android.test.AndroidTestCase;

import org.openhds.mobile.forms.FormBehaviour;
import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.forms.FormDefinition;
import org.openhds.mobile.repository.search.FormSearchPluginModule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FormMetadataTest extends AndroidTestCase {

    public void testParseMetadata() throws Exception {
        File file = writeAssetTempFile("testForms/test-metadata-form.xml", "testParseMetadata.xml");
        FormDefinition formDefinition = new FormDefinition();
        formDefinition.setFilePath(file.getAbsolutePath());
        FormBehaviour formBehaviour = new FormBehaviour(formDefinition);

        assertTrue(formBehaviour.parseMetadata());

        // form display criteria
        assertTrue(formBehaviour.shouldDisplay("level"));
        assertFalse(formBehaviour.shouldDisplay("notALevel"));

        FormContent shouldDisplayContent = new FormContent();
        shouldDisplayContent.setContent("individual", "gender", "FEMALE");
        shouldDisplayContent.setContent("location", "type", "URBAN");
        assertTrue(formBehaviour.shouldDisplay("level", shouldDisplayContent));

        FormContent notDisplayContent = new FormContent();
        notDisplayContent.setContent("individual", "gender", "MALE");
        notDisplayContent.setContent("location", "type", "RURAL");
        assertFalse(formBehaviour.shouldDisplay("level", notDisplayContent));

        // record consumers
        List<String> consumers = formBehaviour.getConsumers();
        assertEquals(3, consumers.size());
        assertTrue(consumers.contains("individual"));
        assertTrue(consumers.contains("residency"));
        assertTrue(consumers.contains("relationship"));

        // follow up form criteria
        FormContent bothFollowUpContent = new FormContent();
        bothFollowUpContent.setContent("individual", "gender", "FEMALE");
        bothFollowUpContent.setContent("location", "type", "URBAN");
        List<String> bothFollowUpIds = formBehaviour.getFollowUpForms(bothFollowUpContent);
        assertEquals(2, bothFollowUpIds.size());
        assertTrue(bothFollowUpIds.contains("unconditional-follow-up"));
        assertTrue(bothFollowUpIds.contains("conditional-follow-up"));

        FormContent oneFollowUpContent = new FormContent();
        oneFollowUpContent.setContent("individual", "gender", "MALE");
        oneFollowUpContent.setContent("location", "type", "RURAL");
        List<String> oneFollowUpId = formBehaviour.getFollowUpForms(oneFollowUpContent);
        assertEquals(1, oneFollowUpId.size());
        assertTrue(oneFollowUpId.contains("unconditional-follow-up"));

        // search plugins
        List<FormSearchPluginModule> searches = formBehaviour.getSearchPlugins();
        assertEquals(3, searches.size());

        // dig out plugin field names and check them
        List<String> pluginFieldNames = new ArrayList<>();
        for (FormSearchPluginModule plugin : searches) {
            pluginFieldNames.add(plugin.getFieldName());
        }
        assertTrue(pluginFieldNames.contains("motherUuid"));
        assertTrue(pluginFieldNames.contains("fatherUuid"));
        assertTrue(pluginFieldNames.contains("locationUuid"));
    }

    public void testParseNoMetadata() throws Exception {
        File file = writeAssetTempFile("testForms/test-no-metadata-form.xml", "testParseNoMetadata.xml");
        FormDefinition formDefinition = new FormDefinition();
        formDefinition.setFilePath(file.getAbsolutePath());
        FormBehaviour formBehaviour = new FormBehaviour(formDefinition);

        assertTrue(formBehaviour.parseMetadata());

        // by default, always display
        assertTrue(formBehaviour.shouldDisplay("level"));
        assertTrue(formBehaviour.shouldDisplay("notALevel"));

        FormContent shouldDisplayContent = new FormContent();
        shouldDisplayContent.setContent("individual", "gender", "FEMALE");
        shouldDisplayContent.setContent("location", "type", "URBAN");
        assertTrue(formBehaviour.shouldDisplay("level", shouldDisplayContent));

        FormContent notDisplayContent = new FormContent();
        notDisplayContent.setContent("individual", "gender", "MALE");
        notDisplayContent.setContent("location", "type", "RURAL");
        assertTrue(formBehaviour.shouldDisplay("level", notDisplayContent));

        // no default consumers
        List<String> consumers = formBehaviour.getConsumers();
        assertEquals(0, consumers.size());

        // no default follow-up
        FormContent bothFollowUpContent = new FormContent();
        bothFollowUpContent.setContent("individual", "gender", "FEMALE");
        bothFollowUpContent.setContent("location", "type", "URBAN");
        List<String> bothFollowUpIds = formBehaviour.getFollowUpForms(bothFollowUpContent);
        assertEquals(0, bothFollowUpIds.size());

        FormContent oneFollowUpContent = new FormContent();
        oneFollowUpContent.setContent("individual", "gender", "MALE");
        oneFollowUpContent.setContent("location", "type", "RURAL");
        List<String> oneFollowUpId = formBehaviour.getFollowUpForms(oneFollowUpContent);
        assertEquals(0, oneFollowUpId.size());

        // no default searches
        List<FormSearchPluginModule> searches = formBehaviour.getSearchPlugins();
        assertEquals(0, searches.size());
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

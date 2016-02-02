package org.openhds.mobile.tests.forms;

import android.test.AndroidTestCase;

import org.openhds.mobile.forms.FormBehaviour;
import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.forms.FormDefinition;
import org.openhds.mobile.repository.search.FormSearchPluginModule;
import org.openhds.mobile.utilities.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FormMetadataTest extends AndroidTestCase {

    public void testParseMetadata() throws Exception {
        File file = FileUtils.writeAssetTempFile(getContext(), "testForms/test-metadata-form.xml", "testParseMetadata.xml");
        FormDefinition formDefinition = new FormDefinition();
        formDefinition.setFilePath(file.getAbsolutePath());
        FormBehaviour formBehaviour = new FormBehaviour(formDefinition);

        assertTrue(formBehaviour.parseMetadata());

        // form display criteria
        assertEquals(2, formBehaviour.getDisplayLevels().size());
        assertTrue(formBehaviour.shouldDisplay("level-1"));
        assertTrue(formBehaviour.shouldDisplay("level-2"));
        assertFalse(formBehaviour.shouldDisplay("notALevel"));

        FormContent shouldDisplayContent = new FormContent();
        shouldDisplayContent.setContent("individual", "gender", "FEMALE");
        shouldDisplayContent.setContent("location", "type", "URBAN");
        assertTrue(formBehaviour.shouldDisplay("level-1", shouldDisplayContent));

        FormContent notDisplayContent = new FormContent();
        notDisplayContent.setContent("individual", "gender", "MALE");
        notDisplayContent.setContent("location", "type", "RURAL");
        assertFalse(formBehaviour.shouldDisplay("level-1", notDisplayContent));

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
        List<FormSearchPluginModule> searches = formBehaviour.getSearchPlugins("test");
        assertEquals(3, searches.size());

        // dig out plugin field names and check them
        List<String> pluginFieldNames = new ArrayList<>();
        for (FormSearchPluginModule plugin : searches) {
            pluginFieldNames.add(plugin.getDataWrapper().getName());
        }
        assertTrue(pluginFieldNames.contains("motherUuid"));
        assertTrue(pluginFieldNames.contains("fatherUuid"));
        assertTrue(pluginFieldNames.contains("locationUuid"));

        // submission rel and subpath
        assertEquals("myRel", formBehaviour.getSubmissionRel());
        assertEquals("mySubpath", formBehaviour.getSubmissionSubPath());
    }

    public void testParseNoMetadata() throws Exception {
        File file = FileUtils.writeAssetTempFile(getContext(), "testForms/test-no-metadata-form.xml", "testParseNoMetadata.xml");
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
        List<FormSearchPluginModule> searches = formBehaviour.getSearchPlugins("test");
        assertEquals(0, searches.size());
    }

}

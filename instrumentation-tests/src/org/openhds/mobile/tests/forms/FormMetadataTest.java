package org.openhds.mobile.tests.forms;

import android.test.AndroidTestCase;

import org.openhds.mobile.forms.FormBehaviour;
import org.openhds.mobile.forms.FormDefinition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FormMetadataTest extends AndroidTestCase {

    public void testParseMetadata() throws Exception {
        File file = writeAssetTempFile("testForms/test-metadata-form.xml", "testParseMetadata.xml");
        FormDefinition formDefinition = new FormDefinition();
        formDefinition.setFilePath(file.getAbsolutePath());
        FormBehaviour formBehaviour = new FormBehaviour(formDefinition);

        assertTrue(formBehaviour.parseMetadata());

        assertTrue(formBehaviour.shouldDisplayAtLevel("level"));
        assertFalse(formBehaviour.shouldDisplayAtLevel("notALevel"));

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

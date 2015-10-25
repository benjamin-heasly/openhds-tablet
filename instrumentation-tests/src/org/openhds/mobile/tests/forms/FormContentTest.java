package org.openhds.mobile.tests.forms;

import android.test.AndroidTestCase;

import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.forms.FormInstance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FormContentTest extends AndroidTestCase {

    public void testReadHouseholdFormContent() throws Exception {
        File file = writeAssetTempFile("testForms/test-individual-household-form.xml", "household-form.xml");
        FormInstance formInstance = new FormInstance();
        formInstance.setFilePath(file.getAbsolutePath());

        FormContent formContent = formInstance.readFormContent();
        assertNotNull(formContent);
        assertEquals("101", formContent.getContentString(FormInstance.TOP_LEVEL_ALIAS, "registrationVersion"));
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

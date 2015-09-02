package org.openhds.mobile.tests.parsing;

import android.test.AndroidTestCase;

import org.openhds.mobile.task.parsing.DataPage;
import org.openhds.mobile.task.parsing.JsonPageParser;

import java.io.InputStream;

import static java.util.Arrays.asList;

public class JsonPageParserTest extends AndroidTestCase {

    public void testParseHome() throws Exception {
        JsonPageParser pageParser = new JsonPageParser();

        HappyPageHandler happyPageHandler = new HappyPageHandler();
        pageParser.setPageHandler(happyPageHandler);

        InputStream inputStream = getContext().getAssets().open("testJson/home.json");
        pageParser.parsePages(inputStream);

        DataPage lastPage = happyPageHandler.getLastPage();
        assertNotNull(lastPage);

        // root element is an object, expect a single page
        assertEquals(JsonPageParser.OBJECT_NAME, lastPage.getRootName());
        assertEquals(JsonPageParser.OBJECT_NAME, lastPage.getPageName());
        assertEquals(1, happyPageHandler.getPageCount());

        // "content" object and many objects in the _links array
        assertEquals(1, lastPage.getOccurrenceCount(asList("content")));
        assertEquals(1, lastPage.getOccurrenceCount(asList("_links", "self", "href")));
        assertEquals(1, lastPage.getOccurrenceCount(asList("_links", "visits", "href")));
        assertEquals(1, lastPage.getOccurrenceCount(asList("_links", "outMigrations", "href")));

    }

    public void testParseIndividualsPaged() throws Exception {
        JsonPageParser pageParser = new JsonPageParser();

        HappyPageHandler happyPageHandler = new HappyPageHandler();
        pageParser.setPageHandler(happyPageHandler);

        InputStream inputStream = getContext().getAssets().open("testJson/individual-paged.json");
        pageParser.parsePages(inputStream);

        DataPage lastPage = happyPageHandler.getLastPage();
        assertNotNull(lastPage);

        // root element is an object, expect a single page
        assertEquals(JsonPageParser.OBJECT_NAME, lastPage.getRootName());
        assertEquals(JsonPageParser.OBJECT_NAME, lastPage.getPageName());
        assertEquals(1, happyPageHandler.getPageCount());

        // several links, 20 embedded individuals, and some page statistics
        assertEquals(1, lastPage.getOccurrenceCount(asList("_links", "self", "href")));
        assertEquals(1, lastPage.getOccurrenceCount(asList("_links", "bydate", "href")));
        assertEquals(20, lastPage.getOccurrenceCount(asList("_embedded", "individuals", "uuid")));
        assertEquals(20, lastPage.getOccurrenceCount(asList("_embedded", "individuals", "_links", "self", "href")));
        assertEquals(1, lastPage.getOccurrenceCount(asList("page", "size")));
        assertEquals(1, lastPage.getOccurrenceCount(asList("page", "number")));
    }

    public void testParseLocationSingle() throws Exception {
        JsonPageParser pageParser = new JsonPageParser();

        HappyPageHandler happyPageHandler = new HappyPageHandler();
        pageParser.setPageHandler(happyPageHandler);

        InputStream inputStream = getContext().getAssets().open("testJson/location-single.json");
        pageParser.parsePages(inputStream);

        DataPage lastPage = happyPageHandler.getLastPage();
        assertNotNull(lastPage);

        // root element is an object, expect a single page
        assertEquals(JsonPageParser.OBJECT_NAME, lastPage.getRootName());
        assertEquals(JsonPageParser.OBJECT_NAME, lastPage.getPageName());
        assertEquals(1, happyPageHandler.getPageCount());

        // several location fields and links to related resources
        assertEquals("bc1d89ef-f12c-4378-b50e-55970f4a8e3c", lastPage.getFirstString(asList("uuid")));
        assertEquals("UNKNOWN", lastPage.getFirstString(asList("lastModifiedBy", "uuid")));
        assertEquals("sample location", lastPage.getFirstString(asList("description")));
        assertEquals(2, lastPage.getOccurrenceCount(asList("_links", "residencies", "href")));
        assertEquals(1, lastPage.getOccurrenceCount(asList("_links", "lastmodifiedby", "href")));
        assertEquals(1, lastPage.getOccurrenceCount(asList("_links", "section", "href")));
    }

    public void testParseProjectCodesBulk() throws Exception {
        JsonPageParser pageParser = new JsonPageParser();

        HappyPageHandler happyPageHandler = new HappyPageHandler();
        pageParser.setPageHandler(happyPageHandler);

        InputStream inputStream = getContext().getAssets().open("testJson/project-codes-bulk.json");
        pageParser.parsePages(inputStream);

        DataPage lastPage = happyPageHandler.getLastPage();
        assertNotNull(lastPage);

        // root element is an object, expect a single page
        assertEquals(JsonPageParser.ARRAY_NAME, lastPage.getRootName());
        assertEquals(JsonPageParser.OBJECT_NAME, lastPage.getPageName());
        assertEquals(45, happyPageHandler.getPageCount());

        // last project code is the unknown
        assertEquals("UNKNOWN", lastPage.getFirstString(asList("uuid")));
        assertEquals("UNKNOWN", lastPage.getFirstString(asList("codeName")));
        assertEquals("UNKNOWN_STATUS", lastPage.getFirstString(asList("codeValue")));
        assertEquals("UNKNOWN_STATUS", lastPage.getFirstString(asList("codeGroup")));
    }

    private class HappyPageHandler implements JsonPageParser.PageHandler {
        private int pageCount = 0;

        private DataPage lastPage;

        public int getPageCount() {
            return pageCount;
        }

        public DataPage getLastPage() {
            return lastPage;
        }

        @Override
        public boolean handlePage(DataPage dataPage) {
            pageCount++;
            lastPage = dataPage;
            return true;
        }
    }


}

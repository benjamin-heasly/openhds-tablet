package org.openhds.mobile.tests.parsing;

import android.test.AndroidTestCase;

import org.openhds.mobile.links.JsonLinkParser;
import org.openhds.mobile.links.Link;

import java.io.InputStream;
import java.util.Map;

public class JsonLinkParserTest extends AndroidTestCase {

    public void testParseHomeLinks() throws Exception {
        JsonLinkParser jsonLinkParser = new JsonLinkParser();

        InputStream inputStream = getContext().getAssets().open("testJson/home.json");
        Map<String, Link> links = jsonLinkParser.parseLinks(inputStream);

        assertEquals(21, links.size());
        verifyLinkUrl(links, "self", "https://arcane-lake-8447.herokuapp.com/");
        verifyLinkUrl(links, "users", "https://arcane-lake-8447.herokuapp.com/users");
        verifyLinkUrl(links, "outMigrations", "https://arcane-lake-8447.herokuapp.com/outMigrations");
    }

    public void testParseIndividualPagedLinks() throws Exception {
        JsonLinkParser jsonLinkParser = new JsonLinkParser();

        InputStream inputStream = getContext().getAssets().open("testJson/individual-paged.json");
        Map<String, Link> links = jsonLinkParser.parseLinks(inputStream);

        assertEquals(9, links.size());

        verifyLinkUrl(links, "self", "https://arcane-lake-8447.herokuapp.com/individuals");
        verifyLinkParam(links, "self", "page");
        verifyLinkParam(links, "self", "size");
        verifyLinkParam(links, "self", "sort");

        verifyLinkUrl(links, "bydate", "https://arcane-lake-8447.herokuapp.com/individuals/bydate");
        verifyLinkParam(links, "bydate", "afterDate");
        verifyLinkParam(links, "bydate", "beforeDate");

        verifyLinkUrl(links, "voided", "https://arcane-lake-8447.herokuapp.com/individuals/voided");
    }

    public void testParseLocationSingleLinks() throws Exception {
        JsonLinkParser jsonLinkParser = new JsonLinkParser();

        InputStream inputStream = getContext().getAssets().open("testJson/location-single.json");
        Map<String, Link> links = jsonLinkParser.parseLinks(inputStream);

        assertEquals(7, links.size());

        verifyLinkUrl(links, "self", "https://arcane-lake-8447.herokuapp.com/locations/bc1d89ef-f12c-4378-b50e-55970f4a8e3c");
        verifyLinkUrl(links, "locationhierarchy", "https://arcane-lake-8447.herokuapp.com/locationHierarchies/146572f2-f429-4838-9ec7-40edef74982e");
        verifyLinkUrl(links, "section", "https://arcane-lake-8447.herokuapp.com/locations/external/location-4");
    }

    private static void verifyLinkUrl(Map<String, Link> links, String rel, String url) {
        assertTrue(links.containsKey(rel));
        assertEquals(url, links.get(rel).getUrl());
    }

    private static void verifyLinkParam(Map<String, Link> links, String rel, String param) {
        assertTrue(links.containsKey(rel));
        assertTrue(links.get(rel).getParameters().contains(param));
    }
}

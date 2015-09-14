package org.openhds.mobile.links;

import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Parse some HATEOAS JSON HAL links and add them to a Map by rel.
 * <p/>
 * Expect JSON that looks like this:
 * <p/>
 * {
 * "content": "Welcome to OpenHDS.  The Current time UTC is 2015-09-02T00:31:16.883Z",
 * "_links": {
 * "self": {
 * "href": "https:\/\/arcane-lake-8447.herokuapp.com\/"
 * },
 * "individuals": {
 * "href": "https:\/\/arcane-lake-8447.herokuapp.com\/individuals"
 * },
 * "inMigrations": {
 * "href": "https:\/\/arcane-lake-8447.herokuapp.com\/inMigrations"
 * },
 * ...
 * <p/>
 * Or This:
 * <p/>
 * {
 * "_links": {
 * "self": {
 * "href": "https:\/\/arcane-lake-8447.herokuapp.com\/individuals{?page,size,sort}",
 * "templated": true
 * },
 * "next": {
 * "href": "https:\/\/arcane-lake-8447.herokuapp.com\/individuals?page=1&size=20{&sort}",
 * "templated": true
 * },
 * "collection": {
 * "href": "https:\/\/arcane-lake-8447.herokuapp.com\/individuals"
 * },
 * ...
 * <p/>
 * BSH
 */
public class JsonLinkParser {

    public static final String STREAM_ENCODING = "UTF-8";

    public static final String LINKS_NAME = "_links";
    public static final String HREF_NAME = "href";

    public Map<String, Link> parseLinks(InputStream inputStream) throws IOException {

        Map<String, Link> links = new HashMap<>();

        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, STREAM_ENCODING));
        reader.setLenient(false);

        try {
            reader.beginObject();

            while (reader.hasNext()) {
                if (LINKS_NAME.equals(reader.nextName())) {
                    parseLinksObject(reader, links);
                } else {
                    reader.skipValue();
                }
            }

            reader.endObject();

        } finally {
            reader.close();
        }

        return links;
    }

    private void parseLinksObject(JsonReader reader, Map<String, Link> links) throws IOException {

        reader.beginObject();

        while (JsonToken.NAME == reader.peek()) {
            parseLink(reader.nextName(), reader, links);
        }

        reader.endObject();
    }

    private void parseLink(String rel, JsonReader reader, Map<String, Link> links) throws IOException {

        if (JsonToken.BEGIN_OBJECT != reader.peek()) {
            reader.skipValue();
            return;
        }

        reader.beginObject();

        while (JsonToken.NAME == reader.peek()) {
            if (HREF_NAME.equals(reader.nextName())) {
                Link link = Link.parse(rel, reader.nextString());
                links.put(rel, link);
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
    }

}

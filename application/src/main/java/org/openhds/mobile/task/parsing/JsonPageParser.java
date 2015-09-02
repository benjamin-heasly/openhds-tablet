package org.openhds.mobile.task.parsing;

import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse a potentially long JSON data stream as smaller pages.
 * <p/>
 * Breaks a JSON input stream into "pages" based on the root-level object,
 * or objects that are elements of the root-level array.
 * <p/>
 * For example, the following document has a root-level array containing
 * three objects.  So it would be broken into 3 pages:
 * <p/>
 * [
 * { data for page 1 ... },
 * { data for page 2 ... },
 * { data for page 3 ... }
 * ]
 * <p/>
 * Or, for example, the following document has a root-level object,
 * so it would be broken into a single page:
 * <p/>
 * { data for single page ... }
 * <p/>
 * This parser produces pages based on JSON objects.  A root-level
 * object will be represented as a single page.  A root-level array will be
 * represented with one page per element.  Elements of the root-level array
 * must be JSON objects.
 * <p/>
 * BSH
 */
public class JsonPageParser extends AbstractPageParser {

    public static final String STREAM_ENCODING = "UTF-8";
    public static final String OBJECT_NAME = "object";
    public static final String ARRAY_NAME = "array";

    public int parsePages(InputStream inputStream) throws IOException {

        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, STREAM_ENCODING));
        reader.setLenient(false);

        try {
            if (!reader.hasNext()) {
                return 0;
            }

            JsonToken startToken = reader.peek();

            if (JsonToken.BEGIN_ARRAY == startToken) {
                return parseRootArray(reader);

            } else if (JsonToken.BEGIN_OBJECT == startToken) {
                return parsePageObject(reader, OBJECT_NAME) ? 1 : 0;
            }

        } finally {
            reader.close();
        }

        return 0;
    }

    private int parseRootArray(JsonReader reader) throws IOException {
        int nObjects = 0;

        reader.beginArray();

        while (parsePageObject(reader, ARRAY_NAME)) {
            nObjects++;
        }

        reader.endArray();

        return nObjects;
    }

    private boolean parsePageObject(JsonReader reader, String rootName) throws IOException {

        if (JsonToken.BEGIN_OBJECT != reader.peek()) {
            return false;
        }

        DataPage dataPage = new DataPage(rootName, OBJECT_NAME, reader.toString());
        parseObject(reader, dataPage, new ArrayList<String>());
        sendPageToHandler(dataPage);

        return true;
    }

    private void parseObject(JsonReader reader, DataPage dataPage, List<String> path) throws IOException {
        reader.beginObject();

        while (JsonToken.END_OBJECT != reader.peek()) {
            String name = reader.nextName();
            path.add(name);
            parseValue(reader, dataPage, path);
            path.remove(path.size() - 1);
        }

        reader.endObject();
    }

    private void parseArray(JsonReader reader, DataPage dataPage, List<String> path) throws IOException {
        reader.beginArray();

        int index = 0;
        while (JsonToken.END_ARRAY != reader.peek()) {
            parseValue(reader, dataPage, path);
        }

        reader.endArray();
    }

    private void parseValue(JsonReader reader, DataPage dataPage, List<String> path) throws IOException {

        switch (reader.peek()) {
            case BEGIN_OBJECT:
                parseObject(reader, dataPage, path);
                return;
            case BEGIN_ARRAY:
                parseArray(reader, dataPage, path);
                return;
            case NULL:
                reader.skipValue();
                return;
            case BOOLEAN:
                // convert bool to string
                String boolValue = Boolean.toString(reader.nextBoolean());
                dataPage.addText(path, boolValue);
                return;
            case NUMBER:
                // treat number as a string
            case STRING:
                String value = reader.nextString();
                dataPage.addText(path, value);
                return;
            default:
                throw new IOException("Unexpected Json token: " + reader.peek().name());
        }

    }
}

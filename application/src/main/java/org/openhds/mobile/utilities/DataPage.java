package org.openhds.mobile.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flexible, in-memory container for data like XML elements or JSON objects.
 *
 * Maintains a data structure for text data, grouped by "paths" to the data.
 * For XML, the path to some text would be all the element names between
 * the text element and the document root element.  Similarly for JSON
 * objects.  Multiple text data can be stored under the same path.
 *
 * Provides utility methods for getting text out of the data structure.
 *
 * The idea is to bridge the gap between long data streams, which are highly
 * serial and too big to put in memory, and entity objects, which are random
 * in memory and random access.
 *
 * So a stream reader like XMLPageParser can dump streaming data into one
 * DataPage at a time, without worrying about element order or meaning.  Then
 * a converter like IndividualPageConverter can read from one DataPage at a
 * time with random access and without worrying about the whole data stream.
 *
 * It's the same way you read a book.  You flip to a page and then you can
 * look at any part of that page.
 *
 */
public class DataPage {
    private final String rootElementName;
    private final String pageElementName;
    private final Map<List<String>, List<String>> pageText;

    public DataPage(String rootElementName, String pageElementName) {
        this.rootElementName = rootElementName;
        this.pageElementName = pageElementName;

        pageText = new HashMap<>();
    }

    public void addText(List<String> elementPath, String text) {
        if (!pageText.containsKey(elementPath)) {
            // copy the path to avoid modifications to map keys
            pageText.put(new ArrayList<String>(elementPath), new ArrayList<String>());
        }
        pageText.get(elementPath).add(text);
    }
}

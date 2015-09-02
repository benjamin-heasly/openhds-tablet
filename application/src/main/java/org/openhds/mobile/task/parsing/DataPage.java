package org.openhds.mobile.task.parsing;

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
 * serial and too big to put in memory, and entity objects, which must fit
 * in memory to give random access.
 *
 * So a stream reader like XMLPageParser can dump streaming data into one
 * DataPage at a time, without worrying about element order or meaning.  Then
 * a converter like IndividualPageConverter can read from one DataPage at a
 * time with random access and without worrying about the whole data stream.
 *
 */
public class DataPage {
    private final String pageDescription;
    private final String rootName;
    private final String pageName;
    private final Map<List<String>, List<String>> pageText;

    public DataPage(String rootName, String pageName, String pageDescription) {
        this.rootName = rootName;
        this.pageName = pageName;
        this.pageDescription = pageDescription;

        pageText = new HashMap<>();
    }

    public String getRootName() {
        return rootName;
    }

    public String getPageDescription() {
        return pageDescription;
    }

    public String getPageName() {
        return pageName;
    }

    public void addText(List<String> path, String text) {
        if (!pageText.containsKey(path)) {
            // copy the path to avoid modifications to map keyspageText
            pageText.put(new ArrayList<String>(path), new ArrayList<String>());
        }
        pageText.get(path).add(text);
    }

    public int getOccurrenceCount(List<String> path) {
        if (!pageText.containsKey(path)) {
            return 0;
        }

        return pageText.get(path).size();
    }

    public String getStringOccurrence(List<String> path, int occurrence) {
        if (!pageText.containsKey(path)) {
            return null;
        }

        List<String> occurrences = pageText.get(path);
        if (occurrence >= occurrences.size()) {
            return null;
        }

        return occurrences.get(occurrence);
    }

    public int getIntOccurrence(List<String> path, int occurrence) {
        String asString = getStringOccurrence(path, occurrence);
        if (null == asString) {
            return 0;
        }
        return Integer.parseInt(asString);
    }

    public String getFirstString(List<String> path) {
        return getStringOccurrence(path, 0);
    }

    public int getFirstInt(List<String> path) {
        return getIntOccurrence(path, 0);
    }
}

package org.openhds.mobile.utilities;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parse a potentially long XML data stream as smaller pages.
 *
 * Breaks an XML input stream into "pages" based on elements that are one
 * level deep in the XML document.  This allows the parser to read long,
 * unwieldy data streams without putting the whole stream into memory,
 * but still process each "page" with in a convenient, in-memory
 * representation.
 *
 * For example, the following document has a <locations> element at the
 * root, and three <location> elements that are one level deep.  So it
 * would be broken into 3 pages:
 *
 * <locations>
 *     <location>
 *         ... location data for page 1 ...
 *     </location>
 *      <location>
 *         ... location data for page 2 ...
 *     </location>
 *      <location>
 *         ... location data for page 3 ...
 *     </location>
 * </locations>
 *
 * For each page, constructs a flat representation of the XML data.  This
 * is a Map with String text content for values.  The Map keys are Lists
 * of String element names representing the path through the XML page.
 *
 * For example, the following document would have one page, represented by
 * a Map with two entries, one entry for an individual's own extId, and
 * one entry for the extId of the individual's mother:
 *
 * <Individuals>
 *     <Individual>
 *         <extId>12345</extId>
 *         <mother>
 *             <Individual>
 *                 <extId>67890</extId>
 *             </Individual>
 *         </mother>
 *     </Individual>
 * </Individuals>
 *
 * The map entries would look like this:
 *
 * [Individual, extId] -> 12345
 * [Individual, mother, Individual, extId] -> 67890
 *
 * The page parser assumes that these paths are unique within each page,
 * otherwise later elements will overwrite earlier elements.  This will be
 * the case as long as nested elements use distinguishing names, like
 * "mother" and "father".  It will not be the case if the page contains
 * a nested collection.  How to deal with this?
 *
 * After reading each "page", the parser may send the Map representation
 * to a registered listener for further processing.  The listener must use
 * the root element name, page element name, and the specific Map keys to
 * decide how to process the data.
 *
 * For now, the page parser considers only XML elements, and ignores
 * attributes and namespaces.  It also ignores elements that don't have
 * any text inside.
 *
 * BSH
 */
public class XmlStreamPageParser {
    private PageHandler pageHandler;

    public PageHandler getPageHandler() {
        return pageHandler;
    }

    public void setPageHandler(PageHandler pageHandler) {
        this.pageHandler = pageHandler;
    }

    public int parsePages(InputStream inputStream) throws XmlPullParserException, IOException {
        int nPages = 0;

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);

        XmlPullParser pullParser = factory.newPullParser();
        pullParser.setInput(new InputStreamReader(inputStream));

        String rootElementName = null;
        String pageElementName = null;
        List<String> pageElementPath = new ArrayList<>();
        Map<List<String>, String> pageData = new HashMap<>();

        int eventType = pullParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_DOCUMENT) {

                // nothing to do at start of document
                continue;

            } else if(eventType == XmlPullParser.START_TAG) {

                // descend into a page
                int depth = pullParser.getDepth();
                if (1 == depth) {
                    // at root level, set the root element name
                    rootElementName = pullParser.getName();

                } else if (2 == depth ) {
                    // at page level, start a fresh page Map
                    nPages++;
                    pageElementName = pullParser.getName();
                    pageElementPath = new ArrayList<>();
                    pageElementPath.add(pageElementName);
                    pageData = new HashMap<>();

                } else if (2 < depth) {
                    // append another element to the page path
                    pageElementPath.add(pullParser.getName());
                }

            } else if(eventType == XmlPullParser.END_TAG) {

                // ascend back out of a page
                int depth = pullParser.getDepth();
                if (1 == depth) {
                    // at root level, do nothing
                    continue;

                } else if (2 == depth ) {
                    // send finished page to the handler
                    if (null != pageHandler) {
                        pageHandler.handlePage(rootElementName, pageElementName, pageData);
                    }
                    pageElementName = null;

                } else if (2 < depth) {
                    // pop the last element off the page path
                    pageElementPath.remove(pageElementPath.size()-1);
                }

            } else if(eventType == XmlPullParser.TEXT) {

                // put text data into the page Map
                pageData.put(pageElementPath, pullParser.getText());

                // don't modify element path once it's in the map
                pageElementPath = new ArrayList<>(pageElementPath);
            }

            eventType = pullParser.next();
        }

        return nPages;
    }

    public interface PageHandler {
        public void handlePage(String rootElementName, String pageElementName, Map<List<String>, String> pageData);
    }
}

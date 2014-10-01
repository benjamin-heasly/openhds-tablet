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
 * level deep in the XML document.  This allows the parser to read long
 * data streams without putting the whole stream into memory, but still
 * process each "page" with in a convenient, in-memory representation.
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
 * See DataPage for details about pages.
 *
 * After reading a full page, the parser sends the page to a listener for
 * further processing.  The listener can use the root element name and
 * page element name to decide how to process the data.
 *
 * The page parser considers only XML elements, ignores attributes and
 * namespaces.  It also ignores elements that don't have any text
 * inside.
 *
 * BSH
 */
public class XmlPageParser {
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
        List<String> elementPath = null;
        DataPage dataPage = null;

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
                    elementPath = new ArrayList<>();
                    elementPath.add(pageElementName);
                    dataPage = new DataPage(rootElementName, pageElementName);

                } else if (2 < depth) {
                    // append another element to the page path
                    elementPath.add(pullParser.getName());
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
                        pageHandler.handlePage(dataPage);
                    }

                } else if (2 < depth) {
                    // pop the last element off the page path
                    elementPath.remove(elementPath.size()-1);
                }

            } else if(eventType == XmlPullParser.TEXT) {

                // put the text data into the data page
                dataPage.addText(elementPath, pullParser.getText());
            }

            eventType = pullParser.next();
        }

        return nPages;
    }

    public interface PageHandler {
        public void handlePage(DataPage dataPage);
    }
}

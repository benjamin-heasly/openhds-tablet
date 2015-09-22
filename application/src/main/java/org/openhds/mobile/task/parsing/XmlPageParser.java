package org.openhds.mobile.task.parsing;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse a potentially long XML data stream as smaller pages.
 *
 * Breaks an XML input stream into "pages" based on elements that are one
 * level deep in the XML document.
 *
 * For example, the following document has a <locations> element at the root,
 * and three <location> elements that are one level deep.  So it
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
 * This parser considers only XML elements and ignores attributes and
 * namespaces.  It also ignores elements that don't have any text inside.
 *
 * BSH
 */
public class XmlPageParser extends AbstractPageParser {

    @Override
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
        boolean shouldContinue = true;
        while (eventType != XmlPullParser.END_DOCUMENT && shouldContinue) {

            int depth = pullParser.getDepth();

            if(eventType == XmlPullParser.START_TAG) {

                // descend into a page
                if (1 == depth) {
                    // at root level, set the root element name
                    rootElementName = pullParser.getName();

                } else if (2 == depth ) {
                    // at page level, start a fresh page Map
                    nPages++;
                    pageElementName = pullParser.getName();
                    elementPath = new ArrayList<>();
                    //elementPath.add(pageElementName);
                    dataPage = new DataPage(rootElementName, pageElementName, pullParser.getPositionDescription());

                } else if (2 < depth) {
                    // append another element to the page path
                    elementPath.add(pullParser.getName());
                }

            } else if(eventType == XmlPullParser.END_TAG) {

                // ascend back out of a page
                if (2 == depth ) {
                    // send finished page to the handler
                    shouldContinue = sendPageToHandler(dataPage);

                } else if (2 < depth) {
                    // pop the last element off the page path
                    elementPath.remove(elementPath.size()-1);
                }

            } else if(eventType == XmlPullParser.TEXT) {

                if (depth > 1) {
                    String trimmedText = pullParser.getText().trim();
                    if (!trimmedText.isEmpty()) {
                        // put new text into a data page
                        dataPage.addText(elementPath, trimmedText);
                    }
                }
            }

            eventType = pullParser.next();
        }

        return nPages;
    }
}

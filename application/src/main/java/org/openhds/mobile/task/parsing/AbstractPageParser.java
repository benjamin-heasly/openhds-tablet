package org.openhds.mobile.task.parsing;

import java.io.InputStream;

/**
 * Parse a potentially long data stream as smaller pages.
 *
 * Breaks an input stream into "pages" based on some sensible delimiter, like XML elements or JSON
 * objects.  This allows the parser to read long data streams without putting the whole stream into
 * memory, but still process each "page" with in a convenient, in-memory representation.
 *
 * See DataPage for details about pages.
 *
 * After reading a full page, the parser sends the page to a handler for
 * further processing.  The handler can use the root name and page name to decide how to process
 * the data.
 *
 * The handler is free to throw Exceptions, and the parser will send the
 * Exception plus debugging data to a separate error handler for logging
 * etc.  This allows the parser to keep reading the stream.
 *
 * BSH
 */
public abstract class AbstractPageParser {

    public interface PageHandler {
        boolean handlePage(DataPage dataPage);
    }

    public interface PageErrorHandler {
        boolean handlePageError(DataPage dataPage, Exception e);
    }

    private PageHandler pageHandler;

    private PageErrorHandler pageErrorHandler;

    public PageHandler getPageHandler() {
        return pageHandler;
    }

    public void setPageHandler(PageHandler pageHandler) {
        this.pageHandler = pageHandler;
    }

    public PageErrorHandler getPageErrorHandler() {
        return pageErrorHandler;
    }

    public void setPageErrorHandler(PageErrorHandler pageErrorHandler) {
        this.pageErrorHandler = pageErrorHandler;
    }

    protected boolean sendPageToHandler(DataPage dataPage) {
        boolean shouldContinue = true;
        if (null != pageHandler) {
            try {
                shouldContinue = pageHandler.handlePage(dataPage);
            } catch (Exception e) {
                if (null != pageErrorHandler) {
                    shouldContinue = pageErrorHandler.handlePageError(dataPage, e);
                }
            }
        }
        return shouldContinue;
    }

    // subclass must fill in to parse the inputStream and call sendPageToHandler()
    public abstract int parsePages(InputStream inputStream) throws Exception;

}

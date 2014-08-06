package org.openhds.mobile.repository;

import android.content.ContentResolver;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Store a Query and return results from multiple piecewise selects.
 *
 * This allows Queries to return large result sets without having
 * to fully populate a Collection of results (which might take
 * unreasonable time and memory).  It also prevents the caller
 * from having to manage database cursors.
 *
 * BSH
 */
public class ResultsIterator<T> implements Iterator<T> {

    public static final int DEFAULT_WINDOW_SIZE = 10;

    private final ContentResolver contentResolver;
    private final Query query;
    private final Converter<T> converter;

    private final int windowSize;
    private final List<T> windowResults;
    private final int fullResultSize;

    private int windowIndex;
    private int fullResultIndex;

    public ResultsIterator(ContentResolver contentResolver, Query query, Converter<T> converter) {
        this(contentResolver, query, converter, DEFAULT_WINDOW_SIZE);
    }

    public ResultsIterator(ContentResolver contentResolver, Query query, Converter<T> converter, int windowSize) {
        this.contentResolver = contentResolver;
        this.query = query;
        this.converter = converter;
        this.windowSize = windowSize;

        windowResults = new ArrayList<T>();

        // initial query to determine full result count
        Cursor cursor = query.select(contentResolver);
        fullResultSize = cursor.getCount();
        cursor.close();

        // initially positioned before first item
        fullResultIndex = -1;
    }


    @Override
    public boolean hasNext() {
        return fullResultSize > 0 && fullResultIndex < (fullResultSize - 1);
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        fullResultIndex++;
        if (windowIndex >= windowResults.size()) {
            // ran out of results in the current window, get more
            getNextResultSet();
            windowIndex = 0;
        }
        return windowResults.get(windowIndex++);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    // do a piecewise select with windowSize results or fewer
    private void getNextResultSet() {
        windowResults.clear();
        Cursor cursor = query.selectRange(contentResolver, fullResultIndex, windowSize);
        while(cursor.moveToNext()) {
            windowResults.add(converter.fromCursor(cursor));
        }
        cursor.close();
    }
}

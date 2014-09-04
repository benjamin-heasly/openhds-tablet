package org.openhds.mobile.repository;

import android.content.ContentResolver;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.Query;
import org.openhds.mobile.repository.QueryResult;
import org.openhds.mobile.repository.ResultsIterator;

import java.util.Iterator;

/**
 * Store a Query and return results from multiple piecewise selects as QueryResults.
 *
 * This wraps the behavior of a ResultsIterator.  Instead of returning real entity
 * types, it returns generic QueryResults.
 *
 * BSH
 */
public class QueryResultsIterator<T> implements Iterator<QueryResult> {

    private final Converter<T> converter;
    private final ResultsIterator<T> resultsIterator;
    private final String state;

    public QueryResultsIterator(ContentResolver contentResolver, Query query, Converter<T> converter, String state) {
        this.converter = converter;
        this.resultsIterator = new ResultsIterator<T>(contentResolver, query, converter);
        this.state = state;
    }

    public QueryResultsIterator(ContentResolver contentResolver, Query query, Converter<T> converter, String state, int windowMaxSize) {
        this.converter = converter;
        this.resultsIterator = new ResultsIterator<T>(contentResolver, query, converter, windowMaxSize);
        this.state = state;
    }

    @Override
    public boolean hasNext() {
        return resultsIterator.hasNext();
    }

    @Override
    public QueryResult next() {
        return converter.toQueryResult(resultsIterator.next(), state);
    }

    @Override
    public void remove() {
        resultsIterator.remove();
    }
}

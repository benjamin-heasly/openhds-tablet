package org.openhds.mobile.modules;

import android.content.ContentResolver;
import android.content.Context;

import org.openhds.mobile.repository.DataWrapper;

import java.util.List;
import java.util.Map;

/**
 *
 * ModuleHierarchy describes hierarchical data that field workers can navigate in a "drill down"
 * fashion.
 *
 * Implementations must declare the names, labels, and order of navigation "levels".
 *
 * Implementations must also implement the "drill down" queries to be performed at each level.
 *
 */
public interface ModuleHierarchy {

    String getName();

    Map<String, String> getLevelLabels();

    List<String> getLevelSequence();

    void init(Context context);

    List<DataWrapper> getAll(ContentResolver contentResolver, String levelId);

    List<DataWrapper> getChildren(ContentResolver contentResolver, DataWrapper dataWrapper);
}

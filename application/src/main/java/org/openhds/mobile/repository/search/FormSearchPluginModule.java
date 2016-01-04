package org.openhds.mobile.repository.search;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.Gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a "search" to fill in a field of a form.
 *
 * Extends SearchPluginModule to add the name of the form field that
 * needs filling, and the value to fill into it.
 *
 */
public class FormSearchPluginModule extends SearchPluginModule implements Parcelable {
    private String fieldName;
    private DataWrapper dataWrapper;

    public FormSearchPluginModule(Gateway gateway, String label, String fieldName, String level) {
        super(gateway, label);
        dataWrapper = new DataWrapper(null, null, fieldName, level, null, null);
        this.fieldName = fieldName;
    }

    public DataWrapper getDataWrapper() {
        return dataWrapper;
    }

    public void setDataWrapper(DataWrapper dataWrapper) {
        this.dataWrapper = dataWrapper;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    // for Parcelable
    private FormSearchPluginModule(Parcel parcel) {
        label = parcel.readString();
        fieldName = parcel.readString();
        dataWrapper = parcel.readParcelable(DataWrapper.class.getClassLoader());

        final String gatewayName = parcel.readString();
        gateway = GatewayRegistry.getGatewayByName(gatewayName);

        // Android recommends parceling Maps as Bundles
        final List<String> columnList = new ArrayList<>();
        parcel.readStringList(columnList);

        final Bundle columnsAndLabelsBundle = parcel.readBundle();
        columnsAndLabels = new HashMap<>();
        for (String key : columnList) {
            columnsAndLabels.put(key, columnsAndLabelsBundle.getString(key));
        }
    }

    // for Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    // for Parcelable
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(label);
        parcel.writeString(fieldName);
        parcel.writeParcelable(dataWrapper, flags);

        String gatewayName = gateway.getClass().getSimpleName();
        parcel.writeString(gatewayName);

        // Android recommends parceling Maps as Bundles
        final List<String> columnList = new ArrayList<String>(columnsAndLabels.keySet());
        parcel.writeStringList(columnList);

        Bundle columnsAndLabelsBundle = new Bundle();
        for (String key : columnList) {
            columnsAndLabelsBundle.putString(key, columnsAndLabels.get(key));
        }
        parcel.writeBundle(columnsAndLabelsBundle);
    }

    // for Parcelable
    public static final Creator CREATOR = new Creator();

    // for Parcelable
    private static class Creator implements Parcelable.Creator<FormSearchPluginModule> {
        public FormSearchPluginModule createFromParcel(Parcel in) {
            return new FormSearchPluginModule(in);
        }

        public FormSearchPluginModule[] newArray(int size) {
            return new FormSearchPluginModule[size];
        }
    }
}

package org.openhds.mobile.repository;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Generic representation of a result from any query.
 * * <p/>
 * BSH
 */
public class DataWrapper implements Parcelable {

    private final String uuid;
    private final String extId;
    private final String name;
    private final String level;
    private final String className;
    private final ContentValues contentValues;

    public DataWrapper(String uuid, String extId, String name, String level, String className, ContentValues contentValues) {
        this.uuid = uuid;
        this.extId = extId;
        this.name = name;
        this.level = level;
        this.className = className;
        this.contentValues = contentValues;
    }

    public String getUuid() {
        return uuid;
    }

    public String getExtId() {
        return extId;
    }

    public String getName() {
        return name;
    }

    public String getLevel() {
        return level;
    }

    public String getClassName() {
        return className;
    }

    public ContentValues getContentValues() {
        return contentValues;
    }

    // for Parcelable
    private DataWrapper(Parcel parcel) {
        uuid = parcel.readString();
        extId = parcel.readString();
        name = parcel.readString();
        level = parcel.readString();
        className = parcel.readString();
        contentValues = ContentValues.CREATOR.createFromParcel(parcel);
    }

    // for Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    // for Parcelable
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(uuid);
        parcel.writeString(extId);
        parcel.writeString(name);
        parcel.writeString(level);
        parcel.writeString(className);
        contentValues.writeToParcel(parcel, flags);
    }

    // for Parcelable
    public static final Creator CREATOR = new Creator();

    // for Parcelable
    private static class Creator implements Parcelable.Creator<DataWrapper> {
        @Override
        public DataWrapper createFromParcel(Parcel in) {
            return new DataWrapper(in);
        }

        @Override
        public DataWrapper[] newArray(int size) {
            return new DataWrapper[size];
        }
    }
}

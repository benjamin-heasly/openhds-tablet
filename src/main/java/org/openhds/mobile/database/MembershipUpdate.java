package org.openhds.mobile.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.FormXmlReader;
import org.openhds.mobile.model.Membership;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.util.Log;

public class MembershipUpdate implements Updatable {

    public void updateDatabase(ContentResolver resolver, String filepath) {
    	 FormXmlReader xmlReader = new FormXmlReader();
         try {
             Membership membership = xmlReader.readMembership(new FileInputStream(new File(filepath)));
             
             if (membership == null) {
                 return;
             }
             
             ContentValues cv = new ContentValues();
             cv.put(OpenHDS.Memberships.COLUMN_INDIVIDUAL_EXTID, membership.getIndExtId());
             cv.put(OpenHDS.Memberships.COLUMN_SOCIAL_GROUP_EXTID, membership.getGroupextId());


             resolver.insert(OpenHDS.Memberships.CONTENT_ID_URI_BASE, cv);
         } catch (FileNotFoundException e) {
             Log.e(VisitUpdate.class.getName(), "Could not read Membership XML file");
         }
     }
    }



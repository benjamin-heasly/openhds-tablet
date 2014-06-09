package org.openhds.mobile.projectdata;

import java.util.Map;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.activity.Skeletor;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.database.queries.QueryResult;

import android.database.Cursor;



public class CensusFormFilter {

	public static class AddHeadOfHousehold implements FormFilter {

		@Override
		public boolean amIValid(Skeletor skeletor) {

			Map<String, QueryResult> hierarchyPath = skeletor
					.getHierarchyPath();

			if (hierarchyPath
					.containsKey(ProjectActivityBuilder.CensusActivityBuilder.HOUSEHOLD_STATE)) {
				
				String socialGroupExtId = hierarchyPath.get(ProjectActivityBuilder.CensusActivityBuilder.HOUSEHOLD_STATE)
						.getExtId();
						
		
				Cursor testeroo = skeletor.getContentResolver().query(OpenHDS.SocialGroups.CONTENT_ID_URI_BASE, null, OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_EXTID + " = ?",
						new String[] { socialGroupExtId }, null);
				
				
				Cursor sg = Queries.getSocialGroupByExtId(
						skeletor.getContentResolver(), socialGroupExtId);
				

//				
//				if (!socialGroupCursor.moveToFirst()) {
//					SocialGroup socialGroup = Converter.toSocialGroup(
//							socialGroupCursor, true);
//					String headExtId = socialGroup.getGroupHead();
//					socialGroupCursor.close();
//					
//					if ("UNK".equals(headExtId)) {
//						
//						return true;
//						
//					}

				
//				QueryResult qr = hierarchyPath.get(ProjectActivityBuilder.CensusActivityBuilder.HOUSEHOLD_STATE);
//				
//				Cursor individualCursor = Queries.getIndividualsByResidency(skeletor.getContentResolver(), qr.getExtId());
//				
//				individualCursor.moveToFirst();
//				
//				Individual individual = Converter.toIndividual(individualCursor, true);
//				
//				if (individual.getFirstName() == null) {
//					
//					return true;
//					
//				}

//				String socialGroupExtId = hierarchyPath
//						.get(ProjectActivityBuilder.CensusActivityBuilder.HOUSEHOLD_STATE)
//						.getExtId();
//				Cursor socialGroupCursor = Queries.getSocialGroupByExtId(
//						skeletor.getContentResolver(), socialGroupExtId);
//
//				if (!socialGroupCursor.moveToFirst()); {
//					
//					return true;

					
			

			} 

			return false;
		}

	}

	public static class AddMemberOfHousehold implements FormFilter {

		@Override
		public boolean amIValid(Skeletor skeletor) {

			return false;
		}

	}

	public static class EditIndividual implements FormFilter {

		@Override
		public boolean amIValid(Skeletor skeletor) {

			return true;
		}

	}

}

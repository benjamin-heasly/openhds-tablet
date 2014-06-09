package org.openhds.mobile.projectdata;

import java.util.Map;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.activity.Skeletor;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.database.queries.QueryResult;
import org.openhds.mobile.model.SocialGroup;

import android.database.Cursor;

public class CensusFormFilter {

	public boolean hasHeadOfHousehold(Skeletor skeletor,
			Map<String, QueryResult> hierarchyPath) {

		String socialGroupExtId = hierarchyPath.get(
				ProjectActivityBuilder.CensusActivityBuilder.HOUSEHOLD_STATE)
				.getExtId();

		Cursor socialGroupCursor = Queries.getSocialGroupByExtId(
				skeletor.getContentResolver(), socialGroupExtId);

		if (socialGroupCursor.moveToFirst()) {

			SocialGroup sg = Converter.toSocialGroup(socialGroupCursor, true);

			if (!"UNK".equals(sg.getGroupHead())) {

				return true;

			}
		}
		return false;
	}

	public static class AddHeadOfHousehold implements FormFilter {

		@Override
		public boolean amIValid(Skeletor skeletor) {

			Map<String, QueryResult> hierarchyPath = skeletor
					.getHierarchyPath();

			if (hierarchyPath
					.containsKey(ProjectActivityBuilder.CensusActivityBuilder.HOUSEHOLD_STATE)) {

				CensusFormFilter filter = new CensusFormFilter();

				if (!filter.hasHeadOfHousehold(skeletor, hierarchyPath)) {

					return true;

				}

			}

			return false;
		}
	}

	public static class AddMemberOfHousehold implements FormFilter {

		@Override
		public boolean amIValid(Skeletor skeletor) {

			Map<String, QueryResult> hierarchyPath = skeletor
					.getHierarchyPath();

			if (hierarchyPath
					.containsKey(ProjectActivityBuilder.CensusActivityBuilder.HOUSEHOLD_STATE)) {
				
				CensusFormFilter filter = new CensusFormFilter();
				
				if (filter.hasHeadOfHousehold(skeletor, hierarchyPath)) {
					
					return true;
					
				}
				
			}

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

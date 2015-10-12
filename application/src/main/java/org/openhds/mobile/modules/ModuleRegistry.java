package org.openhds.mobile.modules;

import org.openhds.mobile.R;

import java.util.Arrays;
import java.util.List;

public class ModuleRegistry {

    public static final String MODULE_NAME_EXTRA_KEY = "MODULE_NAME_EXTRA_KEY";

    public static List<String> getModuleNames() {
        return Arrays.asList(
                CensusModule.class.getSimpleName(),
                UpdateModule.class.getSimpleName());
    }

    public static NavigationModule getModuleByName(String name) {

        if (CensusModule.class.getSimpleName().equals(name)) {
            return new CensusModule();

        } else if (UpdateModule.class.getSimpleName().equals(name)) {
            return new UpdateModule();
        }

        return null;
    }

    private static class CensusModule implements NavigationModule {

        private static final ModuleAppearance moduleAppearance = new ModuleAppearance(
                R.string.census_portal_label,
                R.string.census_portal_description,
                R.string.census_activity_title,
                R.drawable.census_hierarchy_selector,
                R.drawable.census_data_selector,
                R.drawable.census_form_selector_orange,
                R.drawable.census_hierarchy_selector,
                R.drawable.census_middle_column_drawable);

        private static final ModuleHierarchy moduleHierarchy = new ResidencyModuleHierarchy();

        @Override
        public String getName() {
            return CensusModule.class.getSimpleName();
        }

        @Override
        public ModuleAppearance getModuleAppearance() {
            return moduleAppearance;
        }

        @Override
        public ModuleHierarchy getModuleHierarchy() {
            return moduleHierarchy;
        }
    }

    private static class UpdateModule implements NavigationModule {

        private static final ModuleAppearance moduleAppearance = new ModuleAppearance(
                R.string.update_portal_label,
                R.string.update_portal_description,
                R.string.update_activity_title,
                R.drawable.update_hierarchy_selector,
                R.drawable.update_data_selector,
                R.drawable.update_form_selector,
                R.drawable.update_hierarchy_selector,
                R.drawable.update_middle_column_drawable);

        private static final ModuleHierarchy moduleHierarchy = new ResidencyModuleHierarchy();


        @Override
        public String getName() {
            return UpdateModule.class.getSimpleName();
        }

        @Override
        public ModuleAppearance getModuleAppearance() {
            return moduleAppearance;
        }

        @Override
        public ModuleHierarchy getModuleHierarchy() {
            return moduleHierarchy;
        }
    }

}

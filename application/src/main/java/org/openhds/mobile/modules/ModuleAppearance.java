package org.openhds.mobile.modules;

/**
 * ModuleAppearance declares the labels and colors to be displayed with a navigation module.
 */
public class ModuleAppearance {

    private final int labelId;
    private final int descriptionId;
    private final int titleId;

    private final int portalDrawableId;
    private final int dataSelectionDrawableId;
    private final int formSelectionDrawableId;
    private final int hierarchySelectionDrawableId;
    private final int middleColumnDrawableId;

    public ModuleAppearance(int labelId,
                            int descriptionId,
                            int titleId,
                            int portalDrawableId,
                            int dataSelectionDrawableId,
                            int formSelectionDrawableId,
                            int hierarchySelectionDrawableId,
                            int middleColumnDrawableId) {
        this.labelId = labelId;
        this.descriptionId = descriptionId;
        this.titleId = titleId;
        this.portalDrawableId = portalDrawableId;
        this.dataSelectionDrawableId = dataSelectionDrawableId;
        this.formSelectionDrawableId = formSelectionDrawableId;
        this.hierarchySelectionDrawableId = hierarchySelectionDrawableId;
        this.middleColumnDrawableId = middleColumnDrawableId;
    }

    public int getLabelId() {
        return labelId;
    }

    public int getDescriptionId() {
        return descriptionId;
    }

    public int getTitleId() {
        return titleId;
    }

    public int getPortalDrawableId() {
        return portalDrawableId;
    }

    public int getDataSelectionDrawableId() {
        return dataSelectionDrawableId;
    }

    public int getFormSelectionDrawableId() {
        return formSelectionDrawableId;
    }

    public int getHierarchySelectionDrawableId() {
        return hierarchySelectionDrawableId;
    }

    public int getMiddleColumnDrawableId() {
        return middleColumnDrawableId;
    }
}

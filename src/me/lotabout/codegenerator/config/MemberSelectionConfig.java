package me.lotabout.codegenerator.config;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class MemberSelectionConfig implements PipelineStep {
    public boolean filterConstantField = true;
    public boolean filterEnumField = false;
    public boolean filterTransientModifier = false;
    public boolean filterStaticModifier = true;
    public boolean filterLoggers = true;
    public String filterFieldName = "";
    public String filterFieldType = "";
    public String filterMethodName = "";
    public String filterMethodType = "";
    public boolean enableMethods = false;
    public String providerTemplate = DEFAULT_TEMPLATE;
    public boolean allowMultiSelection = true;
    public boolean allowEmptySelection = true;
    public int sortElements = 0;
    public int stepNumber = 1;

    @Override public String type() {
        return "member-selection";
    }

    @Override public int step() {return stepNumber;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MemberSelectionConfig that = (MemberSelectionConfig) o;

        if (filterConstantField != that.filterConstantField) return false;
        if (filterEnumField != that.filterEnumField) return false;
        if (filterTransientModifier != that.filterTransientModifier) return false;
        if (filterStaticModifier != that.filterStaticModifier) return false;
        if (filterLoggers != that.filterLoggers) return false;
        if (enableMethods != that.enableMethods) return false;
        if (allowMultiSelection != that.allowMultiSelection) return false;
        if (allowEmptySelection != that.allowEmptySelection) return false;
        if (sortElements != that.sortElements) return false;
        if (stepNumber != that.stepNumber) return false;
        if (filterFieldName != null ? !filterFieldName.equals(that.filterFieldName) : that.filterFieldName != null)
            return false;
        if (filterFieldType != null ? !filterFieldType.equals(that.filterFieldType) : that.filterFieldType != null)
            return false;
        if (filterMethodName != null ? !filterMethodName.equals(that.filterMethodName) : that.filterMethodName != null)
            return false;
        if (filterMethodType != null ? !filterMethodType.equals(that.filterMethodType) : that.filterMethodType != null)
            return false;
        return providerTemplate != null ? providerTemplate.equals(that.providerTemplate) : that.providerTemplate == null;
    }

    @Override
    public int hashCode() {
        int result = (filterConstantField ? 1 : 0);
        result = 31 * result + (filterEnumField ? 1 : 0);
        result = 31 * result + (filterTransientModifier ? 1 : 0);
        result = 31 * result + (filterStaticModifier ? 1 : 0);
        result = 31 * result + (filterLoggers ? 1 : 0);
        result = 31 * result + (filterFieldName != null ? filterFieldName.hashCode() : 0);
        result = 31 * result + (filterFieldType != null ? filterFieldType.hashCode() : 0);
        result = 31 * result + (filterMethodName != null ? filterMethodName.hashCode() : 0);
        result = 31 * result + (filterMethodType != null ? filterMethodType.hashCode() : 0);
        result = 31 * result + (enableMethods ? 1 : 0);
        result = 31 * result + (providerTemplate != null ? providerTemplate.hashCode() : 0);
        result = 31 * result + (allowMultiSelection ? 1 : 0);
        result = 31 * result + (allowEmptySelection ? 1 : 0);
        result = 31 * result + sortElements;
        result = 31 * result + stepNumber;
        return result;
    }

    private static String DEFAULT_TEMPLATE = "## set `availableMembers` to provide the members to select\n"
            + "## set `selectedMembers` to select the members initially, set nothing to select all\n"
            + "## Note that it should be type List<PsiMember> or List<MemberEntry>\n"
            + "## And the selected result will be\n"
            + "## - fields1:  List<FieldEntry> where `1` is the step number that you specified\n"
            + "## - methods1: List<MethodEntry>\n"
            + "## - members:  List<MemberEntry>\n"
            + "#set($availableMembers = $class0.members)\n";

}

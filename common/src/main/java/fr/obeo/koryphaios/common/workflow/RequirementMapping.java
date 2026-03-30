package fr.obeo.koryphaios.common.workflow;

import org.eclipse.sirius.koryphaios.data.Requirement;
import org.eclipse.sirius.koryphaios.data.Version;

public record RequirementMapping(Version version, String versionVariable, Requirement requirement, String requirementVariable) {
}

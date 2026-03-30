package fr.obeo.koryphaios.common.workflow;

import java.util.List;

public record IntegrationModelDescription(String name, List<Mock> integratedMocks) implements ModelInterface {
}

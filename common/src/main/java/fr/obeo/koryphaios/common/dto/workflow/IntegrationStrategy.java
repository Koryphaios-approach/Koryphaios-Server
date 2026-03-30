package fr.obeo.koryphaios.common.dto.workflow;

import java.util.List;

public record IntegrationStrategy(List<ConditionResult> when, List<String> guardMockIds, List<EventSubscription> eventSubscriptions) implements ConditionedStrategy {
}

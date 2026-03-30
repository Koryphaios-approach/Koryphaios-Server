package fr.obeo.koryphaios.common.workflow;

import java.util.List;

public record IntegrationStrategy(List<EventResultMatcher> onEventSubscription, List<IntegrationModelDescription> guardMockIds, List<EventSubscription> eventSubscriptions) implements ConditionedStrategy {
}

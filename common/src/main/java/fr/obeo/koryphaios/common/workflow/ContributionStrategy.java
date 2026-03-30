package fr.obeo.koryphaios.common.workflow;

import java.util.List;

public record ContributionStrategy(List<EventResultMatcher> onEventSubscription, List<Mock> guardMockIds, List<EventSubscription> eventSubscriptions) implements ConditionedStrategy {
}

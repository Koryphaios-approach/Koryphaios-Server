package fr.obeo.koryphaios.common.workflow;

import java.util.List;

public interface ConditionedStrategy {

    List<? extends ModelInterface> guardMockIds();

    List<EventSubscription> eventSubscriptions();

    List<EventResultMatcher> onEventSubscription();


}

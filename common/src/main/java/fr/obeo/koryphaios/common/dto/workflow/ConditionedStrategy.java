package fr.obeo.koryphaios.common.dto.workflow;

import java.util.List;

public interface ConditionedStrategy {

    List<String> guardMockIds();

    List<EventSubscription> eventSubscriptions();

}

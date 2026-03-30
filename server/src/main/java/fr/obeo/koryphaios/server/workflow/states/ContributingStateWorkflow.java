package fr.obeo.koryphaios.server.workflow.states;

import fr.obeo.koryphaios.common.events.IVariableManager;
import fr.obeo.koryphaios.common.tool.AcceptationStatus;
import fr.obeo.koryphaios.common.workflow.ConditionedStrategy;
import fr.obeo.koryphaios.common.workflow.ContributionStrategy;
import fr.obeo.koryphaios.server.workflow.SubscriptionToTaskEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ContributingStateWorkflow implements WorkflowPhaseState {

    private static final Logger log = LoggerFactory.getLogger(ContributingStateWorkflow.class);
    private final ContributionStrategy strategy;
    private final Map<String, AcceptationStatus> preconditionResults;
    private final Map<String, IVariableManager> variableManagerMap;
    private final Map<String, SubscriptionToTaskEvent> subscriptionToTaskEventMap;

    public ContributingStateWorkflow(ContributionStrategy strategy, Map<String, IVariableManager> variableManagerMap) {
        this.strategy = strategy;
        this.variableManagerMap = variableManagerMap;
        this.preconditionResults = new LinkedHashMap<>();
        this.subscriptionToTaskEventMap = new HashMap<>();
        strategy.eventSubscriptions()
                .forEach(precondition -> preconditionResults.put(precondition.varName(), AcceptationStatus.IGNORED));
    }

    @Override
    public ConditionedStrategy strategy() {
        return strategy;
    }

    @Override
    public Map<String, AcceptationStatus> getPreconditionResults() {
        return preconditionResults;
    }

    @Override
    public void setPreconditionResult(String precondition, AcceptationStatus acceptationStatus) {
        if(preconditionResults.containsKey(precondition)) {
            preconditionResults.put(precondition, acceptationStatus);
        } else {
            log.warn("precondition result not found for {}", precondition);
        }
    }

    public Map<String, IVariableManager> getVariableManagers() {
        return variableManagerMap;
    }

    public void subscribeToTaskEvent(SubscriptionToTaskEvent subscriptionToTaskEvent) {
        this.subscriptionToTaskEventMap.put(subscriptionToTaskEvent.name(), subscriptionToTaskEvent);
    }

    public Optional<String> getSubscribedEventNameFromUuid(UUID uuid) {
        return this.subscriptionToTaskEventMap.values().stream()
                .filter(sub -> sub.subscriptionId().equals(uuid))
                .map(SubscriptionToTaskEvent::name)
                .findFirst();
    }
}

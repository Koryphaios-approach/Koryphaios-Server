package fr.obeo.koryphaios.server.workflow.states;

import fr.obeo.koryphaios.common.events.IVariableManager;
import fr.obeo.koryphaios.common.tool.AcceptationStatus;
import fr.obeo.koryphaios.common.workflow.ConditionedStrategy;
import fr.obeo.koryphaios.server.workflow.SubscriptionToTaskEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public sealed interface WorkflowPhaseState permits ContributingStateWorkflow, IntegratingStateWorkflow {

    ConditionedStrategy strategy();

    Map<String, AcceptationStatus> getPreconditionResults();

    Map<String, IVariableManager> getVariableManagers();

    void setPreconditionResult(String precondition, AcceptationStatus acceptationStatus);

    void subscribeToTaskEvent(SubscriptionToTaskEvent subscriptionToTaskEvent);

    Optional<String> getSubscribedEventNameFromUuid(UUID uuid);

}

package fr.obeo.koryphaios.server.handler;

import fr.obeo.koryphaios.common.dto.IInput;
import fr.obeo.koryphaios.common.dto.NewIntegrationInput;
import fr.obeo.koryphaios.common.events.IVariableManager;
import fr.obeo.koryphaios.common.workflow.CollaborationModel;
import fr.obeo.koryphaios.common.workflow.EventSubscription;
import fr.obeo.koryphaios.common.workflow.IntegrationModelDescription;
import fr.obeo.koryphaios.common.workflow.Mock;
import fr.obeo.koryphaios.common.workflow.ModelState;
import fr.obeo.koryphaios.server.workflow.VariableManager;
import fr.obeo.koryphaios.server.workflow.states.IntegratingStateWorkflow;
import fr.obeo.koryphaios.server.workflow.states.ModelFlowState;
import fr.obeo.koryphaios.server.workflow.states.WorkflowState;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class NewIntegrationEventHandler implements IEventHandler {

    @Override
    public boolean canHandle(IInput input) {
        return input instanceof NewIntegrationInput;
    }

    @Override
    public void handle(IInput input, CollaborationModel model, WorkflowState state, VariableManager variableManager) {
        if(input instanceof NewIntegrationInput integrationInput) {
            var strategyOpt = model.integrations()
                    .stream()
                    .filter(strat -> strat.guardMockIds().stream().anyMatch(ver -> ver.name().equals(integrationInput.modelName())))
                    .findFirst();
            if(strategyOpt.isPresent()) {
                var strategy = strategyOpt.get();

                var version = model.modelInterfaces()
                        .stream()
                        .filter(IntegrationModelDescription.class::isInstance)
                        .filter(mock -> mock.name().equals(integrationInput.modelName()))
                        .findFirst();

                var integratedModelsVersion =
                        model.modelInterfaces()
                        .stream()
                        .filter(Mock.class::isInstance)
                        .filter(mock -> integrationInput.integratedModelsName().contains(mock.name()))
                        .toList();

                if(version.isPresent()) {
                    var mock = version.get();
                    variableManager.set("model", mock);
                    variableManager.set("integratedModels", integratedModelsVersion);

                    var map = strategy.eventSubscriptions()
                            .stream()
                            .map(EventSubscription::varName)
                            .collect(Collectors.toMap(var -> var, _ -> (IVariableManager) VariableManager.childOf(variableManager)));
                    var versionState = new ModelFlowState(mock, ModelState.CONTRIBUTED, new IntegratingStateWorkflow(strategy, map));
                    state.pushModelState(versionState);
                }
            }
        }
    }
}

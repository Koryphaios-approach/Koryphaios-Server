package fr.obeo.koryphaios.server.handler;

import fr.obeo.koryphaios.common.dto.IInput;
import fr.obeo.koryphaios.common.dto.NewContributionInput;
import fr.obeo.koryphaios.common.events.IVariableManager;
import fr.obeo.koryphaios.common.workflow.CollaborationModel;
import fr.obeo.koryphaios.common.workflow.EventSubscription;
import fr.obeo.koryphaios.common.workflow.ModelState;
import fr.obeo.koryphaios.server.workflow.VariableManager;
import fr.obeo.koryphaios.server.workflow.states.ContributingStateWorkflow;
import fr.obeo.koryphaios.server.workflow.states.ModelFlowState;
import fr.obeo.koryphaios.server.workflow.states.WorkflowState;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Event handler for processing new contribution requests in workflows.
 * <p>
 * This handler manages the creation of contribution workflows by:
 * </p>
 * <ul>
 *   <li>Identifying the appropriate contribution strategy for the target model</li>
 *   <li>Creating variable managers for each event subscription</li>
 *   <li>Initializing the contributing state workflow</li>
 *   <li>Updating the workflow state with the new contribution</li>
 * </ul>
 * <p>
 * When a new contribution is requested, this handler sets up the necessary workflow
 * infrastructure to track and validate the contribution through its lifecycle.
 * </p>
 *
 * @see NewContributionInput
 * @see ContributingStateWorkflow
 * @see IEventHandler
 */
@Service
public class NewContributionEventHandler implements IEventHandler {

    /**
     * Determines whether this handler can process the given input.
     *
     * @param input the input to check
     * @return {@code true} if the input is a {@link NewContributionInput}, {@code false} otherwise
     */
    @Override
    public boolean canHandle(IInput input) {
        return input instanceof NewContributionInput;
    }

    /**
     * Handles a new contribution request by setting up the contribution workflow.
     * <p>
     * This method:
     * </p>
     * <ol>
     *   <li>Finds the contribution strategy that applies to the target model</li>
     *   <li>Locates the model interface definition</li>
     *   <li>Creates variable managers for each event subscription in the strategy</li>
     *   <li>Initializes a new contributing state workflow</li>
     *   <li>Updates the workflow state to reflect the new contribution</li>
     * </ol>
     *
     * @param input the new contribution input event
     * @param model the collaboration model defining workflow structure
     * @param state the current workflow state to update
     * @param variableManager variables to pass to the handler
     */
    @Override
    public void handle(IInput input, CollaborationModel model, WorkflowState state, VariableManager variableManager) {
        if(input instanceof NewContributionInput contributionInput) {
            var strategyOpt = model.contributions()
                    .stream()
                    .filter(strat -> strat.guardMockIds().stream().anyMatch(ver -> ver.getName().equals(contributionInput.modelName())))
                    .findFirst();
            if(strategyOpt.isPresent()) {
                var strategy = strategyOpt.get();

                var version = model.modelInterfaces()
                        .stream()
                        .filter(mock -> mock.name().equals(contributionInput.modelName()))
                        .findFirst();

                if(version.isPresent()) {
                    var mock = version.get();
                    variableManager.set("model", mock);

                    var map = strategy.eventSubscriptions()
                            .stream()
                            .map(EventSubscription::varName)
                            .collect(Collectors.toMap(var -> var, _ -> (IVariableManager) VariableManager.childOf(variableManager)));
                    var versionState = new ModelFlowState(mock, ModelState.NONE, new ContributingStateWorkflow(strategy, map));
                    state.pushModelState(versionState);
                }
            }
        }
    }
}

package fr.obeo.koryphaios.server.workflow;

import fr.obeo.koryphaios.common.dto.workflow.ConditionResult;
import fr.obeo.koryphaios.common.workflow.Assignment;
import fr.obeo.koryphaios.common.workflow.CollaborationModel;
import fr.obeo.koryphaios.common.workflow.ContributionStrategy;
import fr.obeo.koryphaios.common.workflow.EventSubscription;
import fr.obeo.koryphaios.common.workflow.Expression;
import fr.obeo.koryphaios.common.workflow.IWorkflowConverter;
import fr.obeo.koryphaios.common.workflow.IntegrationModelDescription;
import fr.obeo.koryphaios.common.workflow.IntegrationStrategy;
import fr.obeo.koryphaios.common.workflow.Literal;
import fr.obeo.koryphaios.common.workflow.Mock;
import fr.obeo.koryphaios.common.workflow.ModelDependency;
import fr.obeo.koryphaios.common.workflow.ModelInterface;
import fr.obeo.koryphaios.common.workflow.OrchestrationStrategy;
import fr.obeo.koryphaios.common.workflow.RequirementMapping;
import fr.obeo.koryphaios.common.workflow.Statement;
import fr.obeo.koryphaios.common.workflow.TaskCall;
import fr.obeo.koryphaios.common.workflow.TaskEventSubscription;
import fr.obeo.koryphaios.common.workflow.VariableReference;
import fr.obeo.koryphaios.common.workflow.EventResultMatcher;
import fr.obeo.koryphaios.server.ToolReferenceResolver;
import org.eclipse.sirius.koryphaios.data.Requirement;
import org.eclipse.sirius.koryphaios.data.Study;
import org.eclipse.sirius.koryphaios.data.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service responsible for transforming DTO {@link fr.obeo.koryphaios.common.dto.workflow.CollaborationModel} to domain
 * {@link CollaborationModel}.
 * <p>
 * Uses the {@link ToolReferenceResolver} to resolve qualified identifiers
 * ({@code "toolId#name"}) to their domain objects.
 * </p>
 *
 * @see ToolReferenceResolver
 */
@Service
public class WorkflowConverter implements IWorkflowConverter {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowConverter.class);
    private final ToolReferenceResolver referenceResolver;

    public WorkflowConverter(ToolReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    @Override
    public Optional<CollaborationModel> convert(
            fr.obeo.koryphaios.common.dto.workflow.CollaborationModel dtoModel) {
        return convertInternal(dtoModel);
    }

    // ────────────────────── Top-level conversion ──────────────────────

    private Optional<CollaborationModel> convertInternal(
            fr.obeo.koryphaios.common.dto.workflow.CollaborationModel dtoModel) {

        Optional<Study> studyOpt = referenceResolver.resolveData(dtoModel.studyId(), Study.class);
        if (studyOpt.isEmpty()) {
            logger.error("Failed to resolve study ID: {}", dtoModel.studyId());
            return Optional.empty();
        }

        var orchestration = convertOrchestrationStrategy(dtoModel.orchestration());

        List<ModelInterface> modelInterfaces = dtoModel.mocks().stream()
                .map(dtoMock -> (ModelInterface)
                        new Mock(dtoMock.getName()))
                .toList();

        var integrations = convertAll(
                dtoModel.integrations(),
                dto -> convertIntegrationStrategy(dto, modelInterfaces),
                "integration strategies");
        if (integrations.isEmpty()) return Optional.empty();

        var contributions = convertAll(
                dtoModel.contributions(),
                dto -> convertContributionStrategy(dto, modelInterfaces),
                "contribution strategies");
        if (contributions.isEmpty()) return Optional.empty();

        var mappings = convertAll(
                dtoModel.mappings(),
                this::convertRequirementMapping,
                "requirement mappings");
        if (mappings.isEmpty()) return Optional.empty();

        return Optional.of(new CollaborationModel(
                studyOpt.get(),
                orchestration,
                integrations.get(),
                contributions.get(),
                mappings.get(),
                modelInterfaces
        ));
    }

    // ──────────────────── Strategy conversions ────────────────────────

    private OrchestrationStrategy convertOrchestrationStrategy(
            fr.obeo.koryphaios.common.dto.workflow.OrchestrationStrategy dto) {

        List<ModelDependency> dependencies = dto.modelDependencies().stream()
                .map(dep -> new ModelDependency(dep.model(), dep.dependency()))
                .toList();
        return new OrchestrationStrategy(dependencies);
    }

    private Optional<IntegrationStrategy> convertIntegrationStrategy(
            fr.obeo.koryphaios.common.dto.workflow.IntegrationStrategy dto,
            List<ModelInterface> modelInterfaces) {

        var guards = resolveGuards(dto.guardMockIds(), modelInterfaces,
                IntegrationModelDescription.class);
        if (guards.isEmpty()) return Optional.empty();

        var when = convertConditions(dto.when());
        if (when.isEmpty()) return Optional.empty();

        var subscriptions = convertAll(
                dto.eventSubscriptions(), this::convertEventSubscription, "event subscriptions (integration)");
        if (subscriptions.isEmpty()) return Optional.empty();

        return Optional.of(new IntegrationStrategy(
                when.get(), guards.get(), subscriptions.get()));
    }

    private Optional<ContributionStrategy> convertContributionStrategy(
            fr.obeo.koryphaios.common.dto.workflow.ContributionStrategy dto,
            List<ModelInterface> modelInterfaces) {

        var guards = resolveGuards(dto.guardMockIds(), modelInterfaces,
                Mock.class);
        if (guards.isEmpty()) return Optional.empty();

        var conditions = convertConditions(dto.conditions());
        if (conditions.isEmpty()) return Optional.empty();

        var subscriptions = convertAll(
                dto.eventSubscriptions(), this::convertEventSubscription, "event subscriptions (contribution)");
        if (subscriptions.isEmpty()) return Optional.empty();

        return Optional.of(new ContributionStrategy(
                conditions.get(), guards.get(), subscriptions.get()));
    }

    // ──────────────────── Leaf-level conversions ─────────────────────

    private Optional<RequirementMapping> convertRequirementMapping(
            fr.obeo.koryphaios.common.dto.workflow.RequirementMapping dto) {

        Optional<Version> versionOpt = referenceResolver.resolveData(dto.versionId(), Version.class);
        if (versionOpt.isEmpty()) {
            logger.error("Failed to resolve version ID: {}", dto.versionId());
            return Optional.empty();
        }

        Optional<Requirement> requirementOpt = referenceResolver.resolveData(dto.requirementId(), Requirement.class);
        if (requirementOpt.isEmpty()) {
            logger.error("Failed to resolve requirement ID: {}", dto.requirementId());
            return Optional.empty();
        }

        return Optional.of(new RequirementMapping(
                versionOpt.get(), dto.versionVariable(), requirementOpt.get(), dto.requirementVariable()));
    }

    private Optional<EventSubscription> convertEventSubscription(
            fr.obeo.koryphaios.common.dto.workflow.EventSubscription dto) {

        return referenceResolver.resolveEvent(dto.eventId())
                .map(event -> new EventSubscription(
                        dto.varName(), event, dto.args()));
    }

    // ──────────────────── Statement / Expression ─────────────────────

    private Optional<List<Statement>> convertStatements(
            List<fr.obeo.koryphaios.common.dto.workflow.Statement> statements) {
        return convertAll(statements, this::convertStatement, "statements");
    }

    private Optional<Statement> convertStatement(fr.obeo.koryphaios.common.dto.workflow.Statement statement) {
        if (statement instanceof fr.obeo.koryphaios.common.dto.workflow.TaskCall taskCall) {
            return convertTaskCall(taskCall)
                    .map(Statement.class::cast);
        } else if (statement instanceof fr.obeo.koryphaios.common.dto.workflow.Assignment assignment) {
            return convertExpression(assignment.expression())
                    .map(exp -> new Assignment(assignment.variableName(), exp));
        } else if (statement instanceof fr.obeo.koryphaios.common.dto.workflow.TaskEventSubscription tes) {
            return referenceResolver.resolveTask(tes.task())
                    .flatMap(task -> convertAll(tes.args(), this::convertExpression, "expressions")
                            .map(args -> new TaskCall(task, args)))
                    .map(tc -> new TaskEventSubscription(tes.eventSubscriptionName(), tc));
        }
        return Optional.empty();
    }

    private Optional<TaskCall> convertTaskCall(fr.obeo.koryphaios.common.dto.workflow.TaskCall taskCall) {
        return referenceResolver.resolveTask(taskCall.task())
                .flatMap(task -> convertAll(taskCall.args(), this::convertExpression, "expressions")
                        .map(args -> new TaskCall(task, args)));
    }

    private Optional<Expression> convertExpression(fr.obeo.koryphaios.common.dto.workflow.Expression expression) {
        if (expression instanceof fr.obeo.koryphaios.common.dto.workflow.TaskCall taskCall) {
            return convertTaskCall(taskCall)
                    .map(Expression.class::cast);
        } else if (expression instanceof fr.obeo.koryphaios.common.dto.workflow.Literal literal) {
            return Optional.of(new Literal(literal.value()));
        } else if (expression instanceof fr.obeo.koryphaios.common.dto.workflow.VariableReference variableReference) {
            return Optional.of(new VariableReference(variableReference.variableName()));
        }
        return Optional.empty();
    }

    // ────────────────────── Shared helpers ───────────────────────────

    /**
     * Converts all elements of a list using the given converter. Returns empty if any
     * conversion fails (size mismatch).
     */
    private <S, T> Optional<List<T>> convertAll(List<S> sources,
                                                 java.util.function.Function<S, Optional<T>> converter,
                                                 String description) {
        var converted = sources.stream()
                .map(converter)
                .flatMap(Optional::stream)
                .toList();
        if (converted.size() != sources.size()) {
            logger.error("Failed to convert all {} ({}/{})", description, converted.size(), sources.size());
            return Optional.empty();
        }
        return Optional.of(converted);
    }

    /**
     * Converts {@link ConditionResult} DTOs to domain {@link EventResultMatcher} objects.
     */
    private Optional<List<EventResultMatcher>> convertConditions(List<ConditionResult> dtoConditions) {
        var converted = dtoConditions.stream()
                .map(dto -> convertStatements(dto.statements())
                        .map(stmts -> new EventResultMatcher(dto.condition(), dto.status(), stmts)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        if (converted.size() != dtoConditions.size()) {
            logger.error("Failed to convert all condition results ({}/{})", converted.size(), dtoConditions.size());
            return Optional.empty();
        }
        return Optional.of(converted);
    }

    /**
     * Resolves guard IDs to model interface objects of the expected type.
     */
    @SuppressWarnings("unchecked")
    private <T extends ModelInterface> Optional<List<T>> resolveGuards(
            List<String> guardIds,
            List<ModelInterface> modelInterfaces,
            Class<T> expectedType) {

        var guards = guardIds.stream()
                .map(id -> modelInterfaces.stream()
                        .filter(expectedType::isInstance)
                        .map(expectedType::cast)
                        .filter(mi -> mi.name().equals(id))
                        .findFirst()
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
        if (guards.size() != guardIds.size()) {
            logger.error("Failed to resolve all guard IDs ({}/{})", guards.size(), guardIds.size());
            return Optional.empty();
        }
        return Optional.of(guards);
    }
}

package fr.obeo.koryphaios.common.dto.workflow;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "typeName", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TaskCall.class, name = "TaskCall"),
        @JsonSubTypes.Type(value = Assignment.class, name = "Assignment"),
        @JsonSubTypes.Type(value = TaskEventSubscription.class, name = "TaskEventSubscription")
})
public class Statement {
    public String typeName;
    // Assignment fields
    public String variableName;
    public Expression expression;
    // TaskCall fields
    public String task;
    public List<Expression> args;
    // TaskEventSubscription fields
    public String eventSubscriptionName;

    public String variableName() { return variableName; }
    public Expression expression() { return expression; }
    public String task() { return task; }
    public List<Expression> args() { return args; }
    public String eventSubscriptionName() { return eventSubscriptionName; }
}

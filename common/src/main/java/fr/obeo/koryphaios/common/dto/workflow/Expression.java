package fr.obeo.koryphaios.common.dto.workflow;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "typeName", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Literal.class, name = "Literal"),
        @JsonSubTypes.Type(value = VariableReference.class, name = "VariableReference"),
        @JsonSubTypes.Type(value = TaskCall.class, name = "TaskCall")
})
public class Expression extends Statement {
    // Literal field
    public Object value;

    public Object value() { return value; }
}

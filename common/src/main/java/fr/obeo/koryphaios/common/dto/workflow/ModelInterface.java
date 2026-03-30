package fr.obeo.koryphaios.common.dto.workflow;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Mock.class, name = "Mock"),
        @JsonSubTypes.Type(value = IntegrationModelDefinition.class, name = "IntegrationModelDefinition")
})
public interface ModelInterface {

    String name();

}

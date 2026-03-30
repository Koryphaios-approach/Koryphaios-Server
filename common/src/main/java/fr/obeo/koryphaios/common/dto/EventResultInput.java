package fr.obeo.koryphaios.common.dto;

import fr.obeo.koryphaios.common.tool.AcceptationStatus;
import fr.obeo.koryphaios.common.tool.EventResult;

public record EventResultInput(AcceptationStatus status, String message) implements EventResult {

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public AcceptationStatus getResult() {
        return status;
    }
}

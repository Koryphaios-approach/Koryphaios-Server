package fr.obeo.koryphaios.common.tool;

import java.util.concurrent.Future;

public interface Event {
        String getName();

        @Deprecated
        Future<EventResult> listen();
}

package fr.obeo.koryphaios.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "fr.obeo.ocp.koryphaios.server", "fr.obeo.ocp.koryphaios.common" })
public class KoryphaiosServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KoryphaiosServerApplication.class, args);
    }
}

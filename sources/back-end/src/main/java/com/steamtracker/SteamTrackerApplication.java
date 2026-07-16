package com.steamtracker;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class SteamTrackerApplication {

    public static void main(String[] args) {
        loadLocalEnv();
        SpringApplication.run(SteamTrackerApplication.class, args);
    }

    /**
     * Loads variables from a local {@code .env} file into JVM system properties so that
     * Spring can resolve {@code ${...}} placeholders during local development.
     *
     * <p>The file is resolved against a small, ordered list of candidate directories to stay
     * independent of the working directory: the module root when launched from a terminal
     * ({@code sources/back-end}), or the repository root when launched from the IDE. The first
     * existing {@code .env} wins.
     *
     * <p>Existing environment variables and system properties are never overridden, so real
     * deployments (where no {@code .env} file is present) keep using the values provided by the
     * runtime.
     */
    private static void loadLocalEnv() {
        List<String> candidateDirs = List.of(".", "sources/back-end");

        for (String dir : candidateDirs) {
            if (!Files.exists(Path.of(dir, ".env"))) {
                continue;
            }

            Dotenv.configure()
                    .directory(dir)
                    .load()
                    .entries()
                    .forEach(entry -> {
                        if (System.getProperty(entry.getKey()) == null
                                && System.getenv(entry.getKey()) == null) {
                            System.setProperty(entry.getKey(), entry.getValue());
                        }
                    });
            return;
        }
    }
}

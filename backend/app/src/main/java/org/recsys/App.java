package org.recsys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.sql.DataSource;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * The main entry point for the layered application.
 * This class typically initializes the application context and starts the
 * server.
 */
@SpringBootApplication
@EnableJpaAuditing
public class App {

  @Bean
  public Flyway flyway(DataSource dataSource) {
    Flyway flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .load();

    System.out.println("Checking for migrations...");
    flyway.migrate();
    System.out.println("Migrations applied successfully!");

    return flyway;
  }

  public static void main(String[] args) {

    Dotenv dotenv = Dotenv.configure()
        .directory("../")
        .ignoreIfMissing()
        .load();
    dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

    SpringApplication.run(App.class, args);
  }
}

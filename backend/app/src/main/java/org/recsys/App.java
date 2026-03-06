package org.recsys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main entry point for the layered application.
 * This class typically initializes the application context and starts the server.
 */
@SpringBootApplication
public class App {
    
    public static void main(String[] args) {
      SpringApplication.run(App.class, args);
      System.out.println("CRS is running! Check http://localhost:8080");
    }
}

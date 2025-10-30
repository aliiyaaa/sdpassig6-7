package org.example.cli;

import org.example.model.WeatherData;
import org.example.service.WeatherStation;
import org.example.strategy.UpdateStrategy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
public class WeatherCLI implements CommandLineRunner {
    private final WeatherStation station;
    private final Map<String, UpdateStrategy> strategiesByName;
    private final Scanner scanner = new Scanner(System.in);

    public WeatherCLI(WeatherStation station, List<UpdateStrategy> strategies) {
        this.station = station;
        this.strategiesByName = strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getName().toUpperCase(),
                        s -> s
                ));
    }

    @Override
    public void run(String... args) {
        // Run CLI in a separate thread so it doesn't block Spring Boot
        Thread cliThread = new Thread(() -> {
            try {
                // Wait a bit for Spring Boot to fully start
                Thread.sleep(1000);
                
                System.out.println("\n=== Weather Station CLI ===");
                System.out.println("POST/DELETE operations must be done through this CLI menu");
                System.out.println("GET operations are available at http://localhost:8080/api/weather/*\n");

                boolean running = true;
                while (running && !Thread.currentThread().isInterrupted()) {
                    printMenu();
                    System.out.print("Select option: ");
                    String choice = scanner.nextLine().trim();

                    switch (choice) {
                        case "1" -> setStrategy();
                        case "2" -> triggerPoll();
                        case "3" -> manualUpdate();
                        case "4" -> subscribeObserver();
                        case "5" -> unsubscribeObserver();
                        case "6" -> showStatus();
                        case "7", "q", "quit", "exit" -> {
                            running = false;
                            System.out.println("Exiting CLI. Spring Boot continues running.");
                        }
                        default -> System.out.println("Invalid option. Please try again.\n");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        cliThread.setDaemon(true);
        cliThread.start();
    }

    private void printMenu() {
        System.out.println("\n--- Menu ---");
        System.out.println("1. Set Update Strategy");
        System.out.println("2. Trigger Poll Update");
        System.out.println("3. Manual Weather Update");
        System.out.println("4. Subscribe Observer");
        System.out.println("5. Unsubscribe Observer");
        System.out.println("6. Show Status");
        System.out.println("7. Exit CLI");
        System.out.println();
    }

    private void setStrategy() {
        System.out.println("\nAvailable strategies:");
        strategiesByName.keySet().forEach(k -> System.out.println("  - " + k));
        System.out.print("Enter strategy name: ");
        String name = scanner.nextLine().trim().toUpperCase();

        UpdateStrategy strategy = strategiesByName.get(name);
        if (strategy == null) {
            System.out.println("Error: Unknown strategy: " + name);
            return;
        }

        try {
            station.setStrategy(strategy);
            System.out.println("✓ Strategy set to: " + strategy.getName());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void triggerPoll() {
        UpdateStrategy current = station.getCurrentStrategy();
        if (current == null) {
            System.out.println("Error: No strategy set. Set a strategy first.");
            return;
        }
        if (current.getName().equalsIgnoreCase("MANUAL")) {
            System.out.println("Error: Cannot poll with MANUAL strategy. Use Manual Update option instead.");
            return;
        }

        try {
            WeatherData data = station.triggerUpdate(null);
            System.out.println("✓ Poll successful:");
            printWeatherData(data);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void manualUpdate() {
        UpdateStrategy current = station.getCurrentStrategy();
        if (current == null || !current.getName().equalsIgnoreCase("MANUAL")) {
            System.out.println("Error: Current strategy is not MANUAL. Set strategy to MANUAL first.");
            return;
        }

        try {
            System.out.print("Temperature (°C): ");
            double temp = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Humidity (%): ");
            double humidity = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Wind (kph): ");
            double wind = Double.parseDouble(scanner.nextLine().trim());

            WeatherData input = new WeatherData(temp, humidity, wind, Instant.now());
            WeatherData data = station.triggerUpdate(input);

            System.out.println("✓ Manual update successful:");
            printWeatherData(data);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid number format");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void subscribeObserver() {
        System.out.println("\nObserver types:");
        System.out.println("  1. Phone");
        System.out.println("  2. WebApp");
        System.out.println("  3. Outdoor");
        System.out.print("Select type (1-3): ");
        String typeChoice = scanner.nextLine().trim();

        System.out.print("Enter observer ID: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("Error: ID cannot be empty");
            return;
        }

        boolean added;
        String type;
        try {
            switch (typeChoice) {
                case "1" -> {
                    added = station.subscribePhone(id);
                    type = "phone";
                }
                case "2" -> {
                    added = station.subscribeWebApp(id);
                    type = "webapp";
                }
                case "3" -> {
                    added = station.subscribeOutdoor(id);
                    type = "outdoor";
                }
                default -> {
                    System.out.println("Error: Invalid type selection");
                    return;
                }
            }
            if (added) {
                System.out.println("✓ Observer subscribed: " + type + "/" + id);
            } else {
                System.out.println("ℹ Observer already exists: " + type + "/" + id);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void unsubscribeObserver() {
        System.out.print("Enter observer ID to unsubscribe: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("Error: ID cannot be empty");
            return;
        }

        try {
            boolean removed = station.unsubscribe(id);
            if (removed) {
                System.out.println("✓ Observer unsubscribed: " + id);
            } else {
                System.out.println("ℹ Observer not found: " + id);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void showStatus() {
        System.out.println("\n--- Current Status ---");
        UpdateStrategy current = station.getCurrentStrategy();
        System.out.println("Current Strategy: " + (current != null ? current.getName() : "(none)"));
        System.out.println("Available Strategies: " + String.join(", ", strategiesByName.keySet()));
        
        WeatherData last = station.getLastData();
        if (last != null) {
            System.out.println("\nLast Weather Data:");
            printWeatherData(last);
        } else {
            System.out.println("\nLast Weather Data: (none)");
        }

        var observers = station.getObservers();
        System.out.println("\nSubscribed Observers: " + observers.size());
        observers.forEach(o -> System.out.println("  - " + o.getId() + " (" + o.getClass().getSimpleName() + ")"));
    }

    private void printWeatherData(WeatherData data) {
        System.out.println("  Temperature: " + String.format("%.1f", data.getTemperatureCelsius()) + "°C");
        System.out.println("  Humidity: " + String.format("%.1f", data.getHumidityPercent()) + "%");
        System.out.println("  Wind: " + String.format("%.1f", data.getWindKph()) + " kph");
        System.out.println("  Observed At: " + data.getObservedAt());
    }
}


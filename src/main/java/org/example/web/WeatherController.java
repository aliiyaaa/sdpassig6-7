package org.example.web;

import org.example.model.WeatherData;
import org.example.service.WeatherStation;
import org.example.strategy.UpdateStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    private final WeatherStation station;
    private final Map<String, UpdateStrategy> strategiesByName = new HashMap<>();

    public WeatherController(WeatherStation station, List<UpdateStrategy> strategies) {
        this.station = station;
        if (strategies == null || strategies.isEmpty()) {
            throw new IllegalStateException("No UpdateStrategy beans found. Ensure strategy classes are annotated with @Component.");
        }
        for (UpdateStrategy strategy : strategies) {
            if (strategy != null) {
                strategiesByName.put(strategy.getName().toUpperCase(), strategy);
            }
        }
        if (strategiesByName.isEmpty()) {
            throw new IllegalStateException("No valid strategies found after filtering.");
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> current() {
        WeatherData data = station.getLastData();
        if (data == null) {
            return ResponseEntity.ok(Map.of("message", "No weather data available yet. Trigger an update first."));
        }
        return ResponseEntity.ok(data);
    }

    @GetMapping("/strategy")
    public Map<String, String> getStrategy() {
        Map<String, String> res = new HashMap<>();
        UpdateStrategy s = station.getCurrentStrategy();
        res.put("current", s == null ? null : s.getName());
        res.put("available", String.join(",", strategiesByName.keySet()));
        return res;
    }

    @PutMapping("/strategy/{name}")
    public ResponseEntity<?> setStrategy(@PathVariable String name) {
        UpdateStrategy strategy = strategiesByName.get(name.toUpperCase());
        if (strategy == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Unknown strategy: " + name, "available", strategiesByName.keySet()));
        }
        try {
            station.setStrategy(strategy);
            return ResponseEntity.ok(Map.of("message", "Strategy set to " + strategy.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> triggerUpdate() {
        try {
            UpdateStrategy current = station.getCurrentStrategy();
            if (current == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "No strategy set"));
            }
            if ("MANUAL".equalsIgnoreCase(current.getName())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot trigger update with MANUAL strategy. Use /update/manual endpoint instead."));
            }
            WeatherData data = station.triggerUpdate(null);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/update/manual")
    public ResponseEntity<?> manualUpdate(@RequestBody Map<String, Double> request) {
        try {
            UpdateStrategy current = station.getCurrentStrategy();
            if (current == null || !"MANUAL".equalsIgnoreCase(current.getName())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Current strategy is not MANUAL. Set strategy to MANUAL first."));
            }

            Double temp = request.get("temperatureCelsius");
            Double humidity = request.get("humidityPercent");
            Double wind = request.get("windKph");

            if (temp == null || humidity == null || wind == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing required fields: temperatureCelsius, humidityPercent, windKph"));
            }

            WeatherData input = new WeatherData(temp, humidity, wind, Instant.now());
            WeatherData data = station.triggerUpdate(input);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/observers")
    public ResponseEntity<?> getObservers() {
        List<Map<String, String>> observers = station.getObservers().stream()
            .map(obs -> {
                Map<String, String> info = new HashMap<>();
                info.put("id", obs.getId());
                info.put("type", obs.getClass().getSimpleName());
                return info;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("observers", observers, "count", observers.size()));
    }

    @PostMapping("/observers")
    public ResponseEntity<?> subscribeObserver(@RequestBody Map<String, String> request) {
        String id = request.get("id");
        String type = request.get("type");

        if (id == null || type == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Missing required fields: id, type"));
        }

        boolean success;
        switch (type.toUpperCase()) {
            case "PHONE":
                success = station.subscribePhone(id);
                break;
            case "WEBAPP":
                success = station.subscribeWebApp(id);
                break;
            case "OUTDOOR":
                success = station.subscribeOutdoor(id);
                break;
            default:
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid type. Use: PHONE, WEBAPP, or OUTDOOR"));
        }

        if (!success) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Observer with id '" + id + "' already exists"));
        }

        return ResponseEntity.ok(Map.of("message", "Observer subscribed successfully", "id", id, "type", type));
    }

    @DeleteMapping("/observers/{id}")
    public ResponseEntity<?> unsubscribeObserver(@PathVariable String id) {
        boolean removed = station.unsubscribe(id);
        if (!removed) {
            return ResponseEntity.notFound()
                .build();
        }
        return ResponseEntity.ok(Map.of("message", "Observer unsubscribed successfully", "id", id));
    }
}



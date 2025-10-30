package org.example.web;

import org.example.model.WeatherData;
import org.example.service.WeatherStation;
import org.example.strategy.UpdateStrategy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

}



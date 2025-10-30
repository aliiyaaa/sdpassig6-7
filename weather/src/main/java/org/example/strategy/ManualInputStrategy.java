package org.example.strategy;

import org.example.model.WeatherData;
import org.springframework.stereotype.Component;

@Component
public class ManualInputStrategy implements UpdateStrategy {
    @Override
    public WeatherData update(WeatherData manualInputOrNull) {
        if (manualInputOrNull == null) {
            throw new IllegalArgumentException("Manual strategy requires non-null WeatherData input");
        }
        return manualInputOrNull;
    }

    @Override
    public String getName() {
        return "MANUAL";
    }
}



package org.example.strategy;

import org.example.model.WeatherData;

public interface UpdateStrategy {
    WeatherData update(WeatherData manualInputOrNull);
    String getName();
}



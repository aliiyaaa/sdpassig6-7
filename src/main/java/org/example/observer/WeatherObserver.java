package org.example.observer;

import org.example.model.WeatherData;

public interface WeatherObserver {
    String getId();
    void update(WeatherData data);
}



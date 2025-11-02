package org.example.observer;

import org.example.model.WeatherData;

public class OutdoorDisplay implements WeatherObserver {
    private final String id;
    private WeatherData lastData;

    public OutdoorDisplay(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void update(WeatherData data) {
        this.lastData = data;
        System.out.println("🏢 OutdoorDisplay [" + id + "] received update: " + 
            String.format("%.1f°C, %.1f%% humidity, %.1f kph wind", 
                data.getTemperatureCelsius(), 
                data.getHumidityPercent(), 
                data.getWindKph()));
    }

    public WeatherData getLastData() {
        return lastData;
    }
}



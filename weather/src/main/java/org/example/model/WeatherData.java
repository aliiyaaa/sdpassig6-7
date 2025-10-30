package org.example.model;

import java.time.Instant;

public class WeatherData {
    private final double temperatureCelsius;
    private final double humidityPercent;
    private final double windKph;
    private final Instant observedAt;

    public WeatherData(double temperatureCelsius, double humidityPercent, double windKph, Instant observedAt) {
        this.temperatureCelsius = temperatureCelsius;
        this.humidityPercent = humidityPercent;
        this.windKph = windKph;
        this.observedAt = observedAt;
    }

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public double getHumidityPercent() {
        return humidityPercent;
    }

    public double getWindKph() {
        return windKph;
    }

    public Instant getObservedAt() {
        return observedAt;
    }
}



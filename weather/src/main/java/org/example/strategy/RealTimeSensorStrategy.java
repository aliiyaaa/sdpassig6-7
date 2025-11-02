package org.example.strategy;

import org.example.model.WeatherData;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RealTimeSensorStrategy implements UpdateStrategy {
    @Override
    public WeatherData update(WeatherData manualInputOrNull) {
        double temperature = ThreadLocalRandom.current().nextDouble(-10.0, 40.0);
        double humidity = ThreadLocalRandom.current().nextDouble(10.0, 100.0);
        double wind = ThreadLocalRandom.current().nextDouble(0.0, 60.0);
        return new WeatherData(temperature, humidity, wind, Instant.now());
    }

    @Override
    public String getName() {
        return "REALTIME";
    }
}



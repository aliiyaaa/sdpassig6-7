package org.example.strategy;

import org.example.model.WeatherData;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ScheduledBatchStrategy implements UpdateStrategy {
    @Override
    public WeatherData update(WeatherData manualInputOrNull) {
        double temperature = ThreadLocalRandom.current().nextDouble(-5.0, 35.0);
        double humidity = ThreadLocalRandom.current().nextDouble(20.0, 90.0);
        double wind = ThreadLocalRandom.current().nextDouble(0.0, 40.0);
        return new WeatherData(temperature, humidity, wind, Instant.now());
    }

    @Override
    public String getName() {
        return "SCHEDULED";
    }
}



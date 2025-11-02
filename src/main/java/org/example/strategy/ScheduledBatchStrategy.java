package org.example.strategy;

import org.example.model.WeatherData;
import org.example.service.WeatherStation;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ScheduledBatchStrategy implements UpdateStrategy {
    private final WeatherStation weatherStation;
    private boolean schedulingEnabled = true;

    public ScheduledBatchStrategy(@Lazy WeatherStation weatherStation) {
        this.weatherStation = weatherStation;
    }

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

    /**
     * Scheduled method that automatically triggers weather updates every 30 seconds
     * when this strategy is the active strategy.
     * 
     * The fixedDelayString means it will wait 30 seconds after the previous execution
     * completes before running again.
     */
    @Scheduled(fixedDelayString = "30000", initialDelayString = "5000")
    public void scheduledUpdate() {
        if (!schedulingEnabled) {
            return;
        }

        // Only trigger updates if this strategy is currently active
        UpdateStrategy currentStrategy = weatherStation.getCurrentStrategy();
        if (currentStrategy != null && "SCHEDULED".equalsIgnoreCase(currentStrategy.getName())) {
            try {
                System.out.println("⏰ Scheduled batch update triggered (SCHEDULED strategy)");
                weatherStation.triggerUpdate(null);
            } catch (Exception e) {
                System.err.println("❌ Error in scheduled update: " + e.getMessage());
            }
        }
    }

    /**
     * Enable or disable automatic scheduling for this strategy
     */
    public void setSchedulingEnabled(boolean enabled) {
        this.schedulingEnabled = enabled;
    }

    public boolean isSchedulingEnabled() {
        return schedulingEnabled;
    }
}



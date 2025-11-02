package org.example.service;

import org.example.model.WeatherData;
import org.example.observer.OutdoorDisplay;
import org.example.observer.PhoneDisplay;
import org.example.observer.WebAppDisplay;
import org.example.observer.WeatherObserver;
import org.example.strategy.UpdateStrategy;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class WeatherStation {
    private final Map<String, WeatherObserver> idToObserver = new LinkedHashMap<>();
    private UpdateStrategy currentStrategy;
    private WeatherData lastData;

    public WeatherStation(Collection<UpdateStrategy> strategies) {
        this.currentStrategy = strategies.stream().findFirst().orElse(null);
    }

    public void setStrategy(UpdateStrategy strategy) {
        this.currentStrategy = Objects.requireNonNull(strategy);
    }

    public UpdateStrategy getCurrentStrategy() {
        return currentStrategy;
    }

    public WeatherData getLastData() {
        return lastData;
    }

    public Collection<WeatherObserver> getObservers() {
        return Collections.unmodifiableCollection(idToObserver.values());
    }

    public boolean subscribePhone(String id) {
        return addObserver(new PhoneDisplay(id));
    }

    public boolean subscribeWebApp(String id) {
        return addObserver(new WebAppDisplay(id));
    }

    public boolean subscribeOutdoor(String id) {
        return addObserver(new OutdoorDisplay(id));
    }

    private boolean addObserver(WeatherObserver observer) {
        if (idToObserver.containsKey(observer.getId())) return false;
        idToObserver.put(observer.getId(), observer);
        if (lastData != null) observer.update(lastData);
        return true;
    }

    public boolean unsubscribe(String id) {
        return idToObserver.remove(id) != null;
    }

    public WeatherData triggerUpdate(WeatherData manualInputOrNull) {
        if (currentStrategy == null) {
            throw new IllegalStateException("No update strategy has been set");
        }
        WeatherData data = currentStrategy.update(manualInputOrNull);
        this.lastData = data;
        notifyObservers(data);
        return data;
    }

    private void notifyObservers(WeatherData data) {
        System.out.println("\n🔔 Notifying " + idToObserver.size() + " observers...");
        for (WeatherObserver observer : idToObserver.values()) {
            observer.update(data);
        }
        System.out.println("✅ All observers notified\n");
    }
}



package com.aicitybrain.service.events;

@FunctionalInterface
public interface CityEventListener {
    void onEvent(CityEvent event);
}

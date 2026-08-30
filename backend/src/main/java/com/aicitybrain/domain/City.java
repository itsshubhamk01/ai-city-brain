package com.aicitybrain.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * The simulated city itself. The demo seeds exactly one city — "NovaCity" — but the
 * schema supports several, so the platform can grow into a genuinely multi-tenant
 * "one deployment, many cities" product later without a redesign.
 */
@Entity
@Table(name = "cities")
public class City extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private long population;

    @Column(name = "center_lat", nullable = false)
    private double centerLat;

    @Column(name = "center_lng", nullable = false)
    private double centerLng;

    @Column(name = "timezone", length = 60)
    private String timezone;

    protected City() {
    }

    public City(String name, String description, long population, double centerLat, double centerLng, String timezone) {
        this.name = name;
        this.description = description;
        this.population = population;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
        this.timezone = timezone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }

    public double getCenterLat() {
        return centerLat;
    }

    public double getCenterLng() {
        return centerLng;
    }

    public String getTimezone() {
        return timezone;
    }
}

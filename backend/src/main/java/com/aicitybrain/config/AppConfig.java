package com.aicitybrain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on the scheduler that drives the simulation tick, and async support. */
@Configuration
@EnableScheduling
@EnableAsync
public class AppConfig {
}

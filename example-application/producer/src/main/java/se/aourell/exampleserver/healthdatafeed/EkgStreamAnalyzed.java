package se.aourell.exampleserver.healthdatafeed;

public record EkgStreamAnalyzed(String id, int hertz, String tags) implements HealthDataEvent { }

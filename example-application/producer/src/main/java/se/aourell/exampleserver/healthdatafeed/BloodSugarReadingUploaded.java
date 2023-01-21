package se.aourell.exampleserver.healthdatafeed;

public record BloodSugarReadingUploaded(String id, String deviceId, int value) implements HealthDataEvent { }

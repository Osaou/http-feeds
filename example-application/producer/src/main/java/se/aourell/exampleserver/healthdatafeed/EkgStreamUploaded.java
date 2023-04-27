package se.aourell.exampleserver.healthdatafeed;

public record EkgStreamUploaded(String id, String deviceId, String fileUrl) implements HealthDataEvent { }

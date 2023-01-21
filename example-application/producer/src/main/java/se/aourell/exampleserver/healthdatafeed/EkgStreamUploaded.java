package se.aourell.exampleserver.healthdatafeed;

public record EkgStreamUploaded(String id, String deviceId, int hertz, byte[] data) implements HealthDataEvent { }

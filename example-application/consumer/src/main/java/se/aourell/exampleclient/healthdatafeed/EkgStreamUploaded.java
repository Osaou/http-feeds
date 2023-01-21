package se.aourell.exampleclient.healthdatafeed;

public record EkgStreamUploaded(String id, String deviceId, int hertz, byte[] data) { }

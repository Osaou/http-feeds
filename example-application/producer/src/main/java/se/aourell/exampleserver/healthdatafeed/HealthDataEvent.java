package se.aourell.exampleserver.healthdatafeed;

public sealed interface HealthDataEvent
  permits EkgStreamUploaded, BloodSugarReadingUploaded {

  String id();
  String deviceId();
}

package se.aourell.exampleserver.healthdatafeed;

public sealed interface HealthDataEvent
  permits
  EkgStreamUploaded,
  EkgStreamAnalyzed,
  BloodSugarReadingUploaded {

  String id();
}

package se.aourell.exampleserver.healthdatafeed;

import se.aourell.httpfeeds.producer.api.EventFeed;

@EventFeed("health-data")
public sealed interface HealthDataEvent
  permits EkgStreamUploaded, BloodSugarReadingUploaded {

  String id();
  String deviceId();
}

package se.aourell.httpfeedsexample.feeds.patient;

import se.aourell.httpfeeds.api.HttpFeed;

@HttpFeed(feed = "patient", path = "/feed/patient", table = "httpfeed_patient")
public sealed interface PatientEvent
  permits AssessmentEnded, AssessmentStarted, PatientAdded, PatientDeleted {

  String id();
}

package se.aourell.httpfeeds.example.feeds.patient;

import se.aourell.httpfeeds.example.HttpFeed;

@HttpFeed(feed = "patient", path = "/feed/patient", table = "httpfeed_patient")
public sealed interface PatientEvent
  permits AssessmentEnded, AssessmentStarted, PatientAdded, PatientDeleted {

  String id();
}

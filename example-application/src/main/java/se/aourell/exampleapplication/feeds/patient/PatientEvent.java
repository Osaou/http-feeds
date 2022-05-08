package se.aourell.exampleapplication.feeds.patient;

import se.aourell.httpfeeds.api.HttpFeed;

@HttpFeed(path = "/feed/patient", feedName = "patient", persistenceName = "httpfeed_patient")
public sealed interface PatientEvent
  permits AssessmentEnded, AssessmentStarted, PatientAdded, PatientDeleted {

  String id();
}

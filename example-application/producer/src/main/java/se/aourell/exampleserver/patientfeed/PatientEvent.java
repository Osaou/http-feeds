package se.aourell.exampleserver.patientfeed;

import se.aourell.httpfeeds.server.api.HttpFeed;

@HttpFeed(path = "/feed/patient", feedName = "patient", persistenceName = "httpfeed_patient")
public sealed interface PatientEvent
  permits AssessmentEnded, AssessmentStarted, PatientAdded, PatientDeleted {

  String id();
}

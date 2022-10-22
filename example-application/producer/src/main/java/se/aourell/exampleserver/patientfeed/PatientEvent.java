package se.aourell.exampleserver.patientfeed;

import se.aourell.httpfeeds.publisher.api.EventFeed;

@EventFeed("patient")
public sealed interface PatientEvent
  permits AssessmentEnded, AssessmentStarted, PatientAdded, PatientDeleted {

  String id();
}

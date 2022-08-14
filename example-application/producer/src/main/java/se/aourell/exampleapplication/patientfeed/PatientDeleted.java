package se.aourell.exampleapplication.patientfeed;

import se.aourell.httpfeeds.server.api.DeletionEvent;

@DeletionEvent
public record PatientDeleted(String id) implements PatientEvent { }

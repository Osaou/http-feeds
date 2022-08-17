package se.aourell.exampleserver.patientfeed;

import se.aourell.httpfeeds.server.api.DeletionEvent;

@DeletionEvent
public record PatientDeleted(String id) implements PatientEvent { }

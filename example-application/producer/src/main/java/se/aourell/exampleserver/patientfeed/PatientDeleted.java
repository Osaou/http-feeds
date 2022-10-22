package se.aourell.exampleserver.patientfeed;

import se.aourell.httpfeeds.publisher.api.DeletionEvent;

@DeletionEvent
public record PatientDeleted(String id) implements PatientEvent { }

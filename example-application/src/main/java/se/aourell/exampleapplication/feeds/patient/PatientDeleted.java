package se.aourell.exampleapplication.feeds.patient;

import se.aourell.httpfeeds.api.DeletionEvent;

@DeletionEvent
public record PatientDeleted(String id) implements PatientEvent { }

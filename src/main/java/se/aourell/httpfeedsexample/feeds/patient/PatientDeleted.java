package se.aourell.httpfeedsexample.feeds.patient;

import se.aourell.httpfeeds.api.DeletionEvent;

@DeletionEvent
public record PatientDeleted(String id) implements PatientEvent { }

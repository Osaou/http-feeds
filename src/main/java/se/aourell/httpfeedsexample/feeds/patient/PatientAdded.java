package se.aourell.httpfeedsexample.feeds.patient;

public record PatientAdded(String id, String firstName, String lastName) implements PatientEvent { }

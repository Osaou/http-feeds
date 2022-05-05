package se.aourell.httpfeeds.example.feeds.patient;

public record PatientAdded(String id, String firstName, String lastName) implements PatientEvent { }

package se.aourell.httpfeeds.example.feeds.patient;

import se.aourell.httpfeeds.example.Delete;

@Delete
public record PatientDeleted(String id) implements PatientEvent { }

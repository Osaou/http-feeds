package se.aourell.exampleapplication.patientfeed;

public record PatientAdded(String id, String firstName, String lastName) implements PatientEvent { }

package se.aourell.exampleserver.patientfeed;

public record PatientAdded(String id, String firstName, String lastName) implements PatientEvent { }

package se.aourell.exampleapplication.feeds.patient;

import java.time.Instant;

public record AssessmentStarted(String id, String deviceId, Instant startDate, Instant endDate) implements PatientEvent { }

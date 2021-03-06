package se.aourell.exampleapplication.feeds.patient;

import java.time.OffsetDateTime;

public record AssessmentStarted(String id, String deviceId, OffsetDateTime startDate, OffsetDateTime endDate) implements PatientEvent { }

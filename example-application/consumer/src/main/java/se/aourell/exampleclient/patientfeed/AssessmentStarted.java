package se.aourell.exampleclient.patientfeed;

import java.time.Instant;

public record AssessmentStarted(String id, String deviceId, Instant startDate, Instant endDate) { }

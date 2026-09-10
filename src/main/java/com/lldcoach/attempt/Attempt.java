package com.lldcoach.attempt;

import java.time.Instant;

public class Attempt {
    private String id;
    private String problemId;
    private AttemptStatus status;
    private Submission submission;
    private Instant createdAt;
    private Instant updatedAt;

    public Attempt() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProblemId() {
        return problemId;
    }

    public void setProblemId(String problemId) {
        this.problemId = problemId;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

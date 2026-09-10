package com.lldcoach.evaluation;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class Evaluation {
    private String id;
    private String attemptId;
    private int overallScore;
    private Map<String, Integer> dimensionScores;
    private List<String> strengths;
    private List<String> issues;
    private List<String> suggestions;
    private Instant createdAt;

    public Evaluation() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(String attemptId) {
        this.attemptId = attemptId;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(int overallScore) {
        this.overallScore = overallScore;
    }

    public Map<String, Integer> getDimensionScores() {
        return dimensionScores;
    }

    public void setDimensionScores(Map<String, Integer> dimensionScores) {
        this.dimensionScores = dimensionScores;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getIssues() {
        return issues;
    }

    public void setIssues(List<String> issues) {
        this.issues = issues;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

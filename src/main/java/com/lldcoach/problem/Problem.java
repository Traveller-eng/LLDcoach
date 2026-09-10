package com.lldcoach.problem;

import java.util.List;
import java.util.UUID;

/**
 * Domain object representing a Low-Level Design problem.
 */
public class Problem {

    private final String id;
    private final String title;
    private final String description;
    private final Difficulty difficulty;
    private final List<String> requirements;

    public Problem(String title, String description, Difficulty difficulty, List<String> requirements) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.requirements = requirements;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public List<String> getRequirements() {
        return requirements;
    }
}

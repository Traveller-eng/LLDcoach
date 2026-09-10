package com.lldcoach.evaluation;

import com.lldcoach.attempt.Attempt;
import com.lldcoach.attempt.Submission;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class DeterministicEvaluator implements Evaluator {

    @Override
    public Evaluation evaluate(Attempt attempt, Submission submission) {
        if (submission == null) {
            throw new IllegalArgumentException("Cannot evaluate null submission");
        }

        Evaluation eval = new Evaluation();
        eval.setId(UUID.randomUUID().toString());
        eval.setAttemptId(attempt.getId());
        eval.setCreatedAt(Instant.now());

        Map<String, Integer> scores = new HashMap<>();
        List<String> strengths = new ArrayList<>();
        List<String> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        int responsibility = 0;
        int abstraction = 0;
        int encapsulation = 0;
        int relationships = 0;
        int extensibility = 0;
        int designQuality = 0;

        String design = submission.getDesign() == null ? "" : submission.getDesign().toLowerCase();
        String code = submission.getCode() == null ? "" : submission.getCode().toLowerCase();
        String explanation = submission.getExplanation() == null ? "" : submission.getExplanation().toLowerCase();

        // Heuristic Design Checks (Responsibility max 25, Abstraction max 20)
        if (design.isBlank()) {
            issues.add("Missing design document.");
            suggestions.add("Add a structural design document identifying core entities and their responsibilities.");
        } else {
            strengths.add("Heuristic check: Design document is present.");
            responsibility += 10;
            abstraction += 10;
            
            if (design.contains("class") || design.contains("interface") || design.contains("entity") || design.contains("model") || design.contains("responsibilit")) {
                strengths.add("Heuristic check: Design mentions structural terminology (e.g., class, interface, entity, responsibility).");
                responsibility += 15;
                abstraction += 10;
            } else {
                suggestions.add("Consider explicitly using structural terminology (classes, interfaces) in your design.");
            }
        }

        // Heuristic Code Checks (Encapsulation max 15, Relationships max 15, Design Quality max 10)
        if (code.isBlank()) {
            issues.add("Missing code implementation.");
            suggestions.add("Provide code implementation demonstrating the design.");
        } else {
            strengths.add("Heuristic check: Code implementation is present.");
            encapsulation += 5;
            relationships += 5;
            designQuality += 5;
            
            boolean hasEncapsulation = code.contains("private") || code.contains("protected") || code.contains("public") || code.contains("get") || code.contains("set");
            boolean hasRelationships = code.contains("extends") || code.contains("implements") || code.contains("import") || code.contains("association") || code.contains("composition");
            
            if (hasEncapsulation) {
                strengths.add("Heuristic check: Found access modifiers or getters/setters indicating encapsulation.");
                encapsulation += 10;
            } else {
                suggestions.add("Use access modifiers to properly encapsulate internal state.");
            }
            
            if (hasRelationships) {
                strengths.add("Heuristic check: Found keywords indicating relationships (extends, implements, etc.).");
                relationships += 10;
            } else {
                suggestions.add("Show relationships explicitly using interfaces or inheritance if applicable.");
            }
            
            if (hasEncapsulation || hasRelationships) {
                designQuality += 5;
            }
        }

        // Heuristic Explanation Checks (Extensibility max 15)
        if (explanation.isBlank()) {
            issues.add("Missing explanation of design choices.");
            suggestions.add("Provide an explanation discussing trade-offs and extensibility.");
        } else {
            strengths.add("Heuristic check: Explanation is present.");
            extensibility += 5;
            
            if (explanation.contains("extend") || explanation.contains("pattern") || explanation.contains("responsibility") || explanation.contains("decouple") || explanation.contains("flexible") || explanation.contains("choice")) {
                strengths.add("Heuristic check: Explanation discusses extensibility, design choices, or decoupling.");
                extensibility += 10;
            } else {
                suggestions.add("Discuss how your design handles future extensions or changes.");
            }
        }

        scores.put("Responsibility", responsibility);
        scores.put("Abstraction", abstraction);
        scores.put("Encapsulation", encapsulation);
        scores.put("Relationships", relationships);
        scores.put("Extensibility", extensibility);
        scores.put("Design Quality", designQuality);

        int overallScore = responsibility + abstraction + encapsulation + relationships + extensibility + designQuality;
        
        eval.setDimensionScores(scores);
        eval.setOverallScore(overallScore);
        eval.setStrengths(strengths);
        eval.setIssues(issues);
        eval.setSuggestions(suggestions);

        return eval;
    }
}

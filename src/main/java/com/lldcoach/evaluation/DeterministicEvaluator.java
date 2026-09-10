package com.lldcoach.evaluation;

import com.lldcoach.attempt.Attempt;
import com.lldcoach.attempt.Submission;
import com.lldcoach.problem.Problem;
import com.lldcoach.problem.ProblemService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class DeterministicEvaluator implements Evaluator {

    private final ProblemService problemService;

    public DeterministicEvaluator(ProblemService problemService) {
        this.problemService = problemService;
    }

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

        int responsibility = 0; // max 25
        int abstraction = 0;    // max 20
        int encapsulation = 0;  // max 15
        int relationships = 0;  // max 15
        int extensibility = 0;  // max 15
        int designQuality = 0;  // max 10

        String design = submission.getDesign() == null ? "" : submission.getDesign().toLowerCase();
        String code = submission.getCode() == null ? "" : submission.getCode().toLowerCase();
        String explanation = submission.getExplanation() == null ? "" : submission.getExplanation().toLowerCase();
        String fullText = design + " " + code + " " + explanation;

        if (fullText.trim().isEmpty()) {
            issues.add("Submission is entirely empty.");
            suggestions.add("Please provide a design, code, and explanation.");
            scores.put("Responsibility", 0);
            scores.put("Abstraction", 0);
            scores.put("Encapsulation", 0);
            scores.put("Relationships", 0);
            scores.put("Extensibility", 0);
            scores.put("Design Quality", 0);
            eval.setDimensionScores(scores);
            eval.setOverallScore(0);
            eval.setStrengths(strengths);
            eval.setIssues(issues);
            eval.setSuggestions(suggestions);
            return eval;
        }

        // Determine Problem
        Optional<Problem> problemOpt = problemService.getProblemById(attempt.getProblemId());
        String problemTitle = problemOpt.map(p -> p.getTitle().toLowerCase()).orElse("");

        // 1. Responsibility (25)
        int respEntitiesCount = countOccurrences(fullText, List.of("class", "interface", "struct", "enum"));
        if (respEntitiesCount > 2) {
            responsibility += 10;
            strengths.add("Identifies multiple domain concepts and separates responsibilities.");
        } else if (respEntitiesCount > 0) {
            responsibility += 5;
            issues.add("Identifies very few domain concepts.");
            suggestions.add("Try breaking down the problem into more distinct responsibilities.");
        }
        
        if (fullText.contains("responsibilit") || fullText.contains("handle") || fullText.contains("manage")) {
            responsibility += 5;
        }

        // Problem specific responsibility checks
        if (problemTitle.contains("parking")) {
            boolean hasSpot = fullText.contains("spot") || fullText.contains("space");
            boolean hasVehicle = fullText.contains("vehicle") || fullText.contains("car");
            boolean hasTicket = fullText.contains("ticket");
            int matches = (hasSpot ? 1 : 0) + (hasVehicle ? 1 : 0) + (hasTicket ? 1 : 0);
            responsibility += (matches * 3);
            if (matches == 3) {
                responsibility += 1; // +10 total
                strengths.add("Effectively captures core Parking Lot responsibilities (spots, vehicles, tickets).");
            } else if (matches > 0) {
                issues.add("Missing some core domain entities (e.g., parking spots, vehicles, or tickets).");
                suggestions.add("Ensure your design explicitly separates spots, vehicles, and ticketing.");
            }
        } else if (problemTitle.contains("vending")) {
            boolean hasMachine = fullText.contains("machine");
            boolean hasItem = fullText.contains("item") || fullText.contains("product");
            boolean hasMoney = fullText.contains("coin") || fullText.contains("money") || fullText.contains("payment");
            int matches = (hasMachine ? 1 : 0) + (hasItem ? 1 : 0) + (hasMoney ? 1 : 0);
            responsibility += (matches * 3);
            if (matches == 3) {
                responsibility += 1;
                strengths.add("Effectively captures Vending Machine responsibilities (machine, products, payments).");
            }
        } else if (problemTitle.contains("elevator")) {
            boolean hasElevator = fullText.contains("elevator");
            boolean hasFloor = fullText.contains("floor");
            boolean hasRequest = fullText.contains("request");
            int matches = (hasElevator ? 1 : 0) + (hasFloor ? 1 : 0) + (hasRequest ? 1 : 0);
            responsibility += (matches * 3);
            if (matches == 3) {
                responsibility += 1;
                strengths.add("Effectively captures Elevator System responsibilities (elevators, floors, requests).");
            }
        } else {
            responsibility += 10; // generic fallback
        }

        // 2. Abstraction (20)
        if (fullText.contains("interface") || fullText.contains("abstract") || fullText.contains("strategy") || fullText.contains("base")) {
            abstraction += 10;
            strengths.add("Uses meaningful abstractions (interfaces, abstract classes, or patterns).");
            if (countOccurrences(fullText, List.of("implements", "extends", "override")) > 0) {
                abstraction += 10;
                strengths.add("Demonstrates actual structural usage of abstractions.");
            } else {
                abstraction += 5;
                issues.add("Mentions abstractions but lacks concrete implementation evidence.");
                suggestions.add("Ensure you implement or extend the abstractions you define.");
            }
        } else {
            issues.add("Lacks meaningful abstractions.");
            suggestions.add("Introduce interfaces or abstract classes to decouple concepts.");
        }

        // 3. Encapsulation (15)
        if (code.contains("private") || code.contains("protected")) {
            encapsulation += 5;
            if (code.contains("public") && (code.contains("get") || code.contains("set") || code.contains("return"))) {
                encapsulation += 10;
                strengths.add("Demonstrates proper encapsulation by hiding internal state and controlling access.");
            } else {
                encapsulation += 5;
                issues.add("Uses private fields but may lack controlled public access methods.");
                suggestions.add("Ensure you provide public methods to interact with encapsulated state safely.");
            }
        } else {
            issues.add("Weak encapsulation. Internal state may be exposed.");
            suggestions.add("Use private/protected modifiers to encapsulate internal fields.");
        }

        // 4. Relationships (15)
        int relCount = countOccurrences(fullText, List.of("extends", "implements", "has-a", "owns", "contains", "list<", "map<", "association", "composition", "dependency"));
        if (relCount > 2) {
            relationships += 15;
            strengths.add("Models multiple coherent relationships between entities (composition, inheritance, etc.).");
        } else if (relCount > 0) {
            relationships += 8;
            issues.add("Models some relationships, but could be more explicit.");
            suggestions.add("Make relationships (has-a, is-a) more explicit in your design and code.");
        } else {
            issues.add("Lacks clear relationships between entities.");
            suggestions.add("Explicitly model ownership and associations (e.g., composition, inheritance).");
        }

        // 5. Extensibility (15)
        if (problemTitle.contains("parking") && (fullText.contains("pricing") || fullText.contains("strategy") || fullText.contains("allocation"))) {
            extensibility += 15;
            strengths.add("Design anticipates extensibility (e.g., variable pricing or allocation strategies).");
        } else if (problemTitle.contains("vending") && (fullText.contains("state") || fullText.contains("transaction"))) {
            extensibility += 15;
            strengths.add("Design anticipates extensibility (e.g., state management).");
        } else if (problemTitle.contains("elevator") && (fullText.contains("schedul") || fullText.contains("dispatch") || fullText.contains("strategy"))) {
            extensibility += 15;
            strengths.add("Design anticipates extensibility (e.g., scheduling strategies).");
        } else {
            if (fullText.contains("extend") || fullText.contains("flexible") || fullText.contains("decouple") || fullText.contains("pattern")) {
                extensibility += 10;
                strengths.add("Explanation discusses extensibility or decoupling.");
            } else {
                issues.add("Design lacks evidence of extensibility.");
                suggestions.add("Introduce abstractions (like a Strategy pattern) to handle variable behaviors instead of hard-coded logic.");
            }
        }

        // 6. Design Quality (10)
        if (responsibility >= 15 && abstraction >= 10 && relationships >= 8) {
            designQuality += 10;
            strengths.add("Overall design is cohesive with manageable responsibilities.");
        } else if (responsibility >= 10) {
            designQuality += 5;
            issues.add("Design quality is acceptable but can be more cohesive.");
        } else {
            issues.add("Design may suffer from God-class or tightly coupled logic.");
            suggestions.add("Refactor to separate concerns and avoid placing all logic into a single large class.");
        }

        responsibility = Math.min(responsibility, 25);
        abstraction = Math.min(abstraction, 20);
        encapsulation = Math.min(encapsulation, 15);
        relationships = Math.min(relationships, 15);
        extensibility = Math.min(extensibility, 15);
        designQuality = Math.min(designQuality, 10);

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

    private int countOccurrences(String text, List<String> keywords) {
        int count = 0;
        for (String kw : keywords) {
            int index = 0;
            while ((index = text.indexOf(kw, index)) != -1) {
                count++;
                index += kw.length();
            }
        }
        return count;
    }
}

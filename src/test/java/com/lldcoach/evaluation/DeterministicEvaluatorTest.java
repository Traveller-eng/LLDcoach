package com.lldcoach.evaluation;

import com.lldcoach.attempt.Attempt;
import com.lldcoach.attempt.Submission;
import com.lldcoach.problem.Problem;
import com.lldcoach.problem.ProblemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeterministicEvaluatorTest {

    private ProblemService problemService;
    private DeterministicEvaluator evaluator;

    @BeforeEach
    public void setup() {
        problemService = mock(ProblemService.class);
        evaluator = new DeterministicEvaluator(problemService);
    }

    private Attempt createAttempt(String problemTitle) {
        Problem p = new Problem(problemTitle, "desc", null, List.of());
        when(problemService.getProblemById(p.getId())).thenReturn(Optional.of(p));
        
        Attempt attempt = new Attempt();
        attempt.setId(UUID.randomUUID().toString());
        attempt.setProblemId(p.getId());
        return attempt;
    }

    @Test
    public void evaluate_garbageSubmission_scoresVeryLow() {
        Attempt attempt = createAttempt("Parking Lot");
        Submission submission = new Submission("random string", "more random", "garbage");
        
        Evaluation eval = evaluator.evaluate(attempt, submission);
        
        assertTrue(eval.getOverallScore() < 30, "Garbage submission should score very low, actual: " + eval.getOverallScore());
    }
    
    @Test
    public void evaluate_emptySubmission_scoresZero() {
        Attempt attempt = createAttempt("Parking Lot");
        Submission submission = new Submission("", "", "");
        
        Evaluation eval = evaluator.evaluate(attempt, submission);
        
        assertEquals(0, eval.getOverallScore());
    }

    @Test
    public void evaluate_minimalWeakSubmission_scoresBelowStrong() {
        Attempt attempt = createAttempt("Parking Lot");
        Submission submission = new Submission("classes", "private x", "nothing much");
        
        Evaluation eval = evaluator.evaluate(attempt, submission);
        
        assertTrue(eval.getOverallScore() > 0);
        assertTrue(eval.getOverallScore() <= 40, "Weak submission shouldn't score too high, actual: " + eval.getOverallScore());
    }

    @Test
    public void evaluate_reasonablyStructuredParkingLot_scoresModerately() {
        Attempt attempt = createAttempt("Parking Lot");
        Submission submission = new Submission(
            "class ParkingLot, class ParkingSpot", 
            "class ParkingLot { private List<ParkingSpot> spots; public void get() {} }", 
            "I separated responsibilities."
        );
        
        Evaluation eval = evaluator.evaluate(attempt, submission);
        
        assertTrue(eval.getOverallScore() > 40, "Reasonable submission should score > 40");
        assertTrue(eval.getOverallScore() < 80, "Reasonable submission missing advanced features should score < 80");
    }

    @Test
    public void evaluate_strongParkingLot_scoresHigh() {
        Attempt attempt = createAttempt("Parking Lot");
        Submission submission = new Submission(
            "class ParkingLot, interface PricingStrategy, class Vehicle, class Ticket, class ParkingSpot", 
            "public class ParkingLot { private List<ParkingSpot> spots; private PricingStrategy pricingStrategy; public Ticket getTicket() {} } class Car extends Vehicle implements Parkable {}", 
            "I used Strategy pattern for pricing strategy to allow flexible decoupling of responsibilities."
        );
        
        Evaluation eval = evaluator.evaluate(attempt, submission);
        
        assertTrue(eval.getOverallScore() >= 80, "Strong submission should score high, actual: " + eval.getOverallScore());
        assertEquals(25, eval.getDimensionScores().get("Responsibility")); // Has spot, vehicle, ticket + multiple entities
        assertTrue(eval.getDimensionScores().get("Abstraction") >= 10);
        assertTrue(eval.getDimensionScores().get("Extensibility") >= 15);
    }
}

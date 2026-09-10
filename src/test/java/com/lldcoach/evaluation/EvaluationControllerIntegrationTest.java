package com.lldcoach.evaluation;

import com.lldcoach.attempt.Attempt;
import com.lldcoach.attempt.AttemptStatus;
import com.lldcoach.attempt.Submission;
import com.lldcoach.problem.Problem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EvaluationControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @SpyBean
    private Evaluator evaluator;

    private String validProblemId;

    @BeforeEach
    public void setup() {
        ResponseEntity<Problem[]> response = restTemplate.getForEntity("/api/problems", Problem[].class);
        validProblemId = response.getBody()[0].getId();
    }

    private Attempt createSubmittedAttempt() {
        // Start an attempt
        ResponseEntity<Attempt> startResponse = restTemplate.postForEntity(
                "/api/problems/" + validProblemId + "/attempts", null, Attempt.class);
        Attempt attempt = startResponse.getBody();
        
        // Update it
        Submission update = new Submission("design text goes here", "code text goes here", "explanation text goes here");
        restTemplate.put("/api/attempts/" + attempt.getId(), update);
        
        // Submit it
        restTemplate.postForEntity("/api/attempts/" + attempt.getId() + "/submit", null, Attempt.class);
        
        return restTemplate.getForObject("/api/attempts/" + attempt.getId(), Attempt.class);
    }

    private Attempt createDraftAttempt() {
        ResponseEntity<Attempt> startResponse = restTemplate.postForEntity(
                "/api/problems/" + validProblemId + "/attempts", null, Attempt.class);
        return startResponse.getBody();
    }

    @Test
    public void evaluate_submittedAttempt_returns200AndEvaluation() {
        Attempt attempt = createSubmittedAttempt();

        ResponseEntity<Evaluation> response = restTemplate.postForEntity(
                "/api/attempts/" + attempt.getId() + "/evaluate", null, Evaluation.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Evaluation eval = response.getBody();
        assertNotNull(eval);
        assertEquals(attempt.getId(), eval.getAttemptId());
        assertTrue(eval.getOverallScore() >= 0 && eval.getOverallScore() <= 100);
        assertNotNull(eval.getDimensionScores());
        assertFalse(eval.getStrengths().isEmpty() && eval.getIssues().isEmpty());

        // Check attempt status changed to COMPLETED
        Attempt updatedAttempt = restTemplate.getForObject("/api/attempts/" + attempt.getId(), Attempt.class);
        assertEquals(AttemptStatus.COMPLETED, updatedAttempt.getStatus());
    }

    @Test
    public void getEvaluation_existingEvaluation_returns200() {
        Attempt attempt = createSubmittedAttempt();
        restTemplate.postForEntity("/api/attempts/" + attempt.getId() + "/evaluate", null, Evaluation.class);

        ResponseEntity<Evaluation> response = restTemplate.getForEntity(
                "/api/attempts/" + attempt.getId() + "/evaluation", Evaluation.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void evaluate_draftAttempt_returns400() {
        Attempt attempt = createDraftAttempt();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/attempts/" + attempt.getId() + "/evaluate", null, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void evaluate_nonexistentAttempt_returns404() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/attempts/nonexistent/evaluate", null, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getEvaluation_nonexistentEvaluation_returns404() {
        Attempt attempt = createSubmittedAttempt();

        ResponseEntity<Evaluation> response = restTemplate.getForEntity(
                "/api/attempts/" + attempt.getId() + "/evaluation", Evaluation.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void evaluate_evaluatorFailure_changesStatusToFailedAndPreservesSubmission() {
        Attempt attempt = createSubmittedAttempt();

        // Simulate failure
        doThrow(new RuntimeException("Simulated failure")).when(evaluator).evaluate(any(), any());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/attempts/" + attempt.getId() + "/evaluate", null, String.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        // Status should be FAILED
        Attempt updatedAttempt = restTemplate.getForObject("/api/attempts/" + attempt.getId(), Attempt.class);
        assertEquals(AttemptStatus.FAILED, updatedAttempt.getStatus());
        
        // Submission should be preserved
        assertNotNull(updatedAttempt.getSubmission());
        assertEquals("design text goes here", updatedAttempt.getSubmission().getDesign());
    }

    @Test
    public void evaluate_failedAttempt_returns200AndEvaluation() {
        Attempt attempt = createSubmittedAttempt();

        // Simulate failure to transition to FAILED
        doThrow(new RuntimeException("Simulated failure")).when(evaluator).evaluate(any(), any());
        restTemplate.postForEntity("/api/attempts/" + attempt.getId() + "/evaluate", null, String.class);
        
        Attempt failedAttempt = restTemplate.getForObject("/api/attempts/" + attempt.getId(), Attempt.class);
        assertEquals(AttemptStatus.FAILED, failedAttempt.getStatus());

        // Reset spy so next evaluation succeeds
        org.mockito.Mockito.reset(evaluator);

        // Evaluate FAILED attempt
        ResponseEntity<Evaluation> response = restTemplate.postForEntity(
                "/api/attempts/" + attempt.getId() + "/evaluate", null, Evaluation.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Attempt completedAttempt = restTemplate.getForObject("/api/attempts/" + attempt.getId(), Attempt.class);
        assertEquals(AttemptStatus.COMPLETED, completedAttempt.getStatus());
    }

    @Test
    public void evaluate_completedAttempt_returns400() {
        Attempt attempt = createSubmittedAttempt();

        // First evaluate succeeds
        restTemplate.postForEntity("/api/attempts/" + attempt.getId() + "/evaluate", null, Evaluation.class);

        // Try evaluating again
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/attempts/" + attempt.getId() + "/evaluate", null, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void deterministicEvaluator_producesStructuredFeedbackAndOverallScore() {
        Attempt attempt = createSubmittedAttempt();
        
        ResponseEntity<Evaluation> response = restTemplate.postForEntity(
                "/api/attempts/" + attempt.getId() + "/evaluate", null, Evaluation.class);

        Evaluation eval = response.getBody();
        assertNotNull(eval);
        assertTrue(eval.getDimensionScores().containsKey("Responsibility"));
        assertTrue(eval.getDimensionScores().containsKey("Abstraction"));
        assertTrue(eval.getDimensionScores().containsKey("Encapsulation"));
        assertTrue(eval.getDimensionScores().containsKey("Relationships"));
        assertTrue(eval.getDimensionScores().containsKey("Extensibility"));
        assertTrue(eval.getDimensionScores().containsKey("Design Quality"));

        int sum = eval.getDimensionScores().values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(sum, eval.getOverallScore());
    }
}

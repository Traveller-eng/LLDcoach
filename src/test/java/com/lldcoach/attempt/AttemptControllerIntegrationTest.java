package com.lldcoach.attempt;

import com.lldcoach.problem.Problem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AttemptControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String validProblemId;

    @BeforeEach
    public void setup() {
        ResponseEntity<Problem[]> response = restTemplate.getForEntity("/api/problems", Problem[].class);
        validProblemId = response.getBody()[0].getId();
    }

    @Test
    public void startAttempt_existingProblem_returns201AndDraftAttempt() {
        ResponseEntity<Attempt> response = restTemplate.postForEntity(
                "/api/problems/" + validProblemId + "/attempts", null, Attempt.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Attempt attempt = response.getBody();
        assertNotNull(attempt);
        assertNotNull(attempt.getId());
        assertEquals(validProblemId, attempt.getProblemId());
        assertEquals(AttemptStatus.DRAFT, attempt.getStatus());
        assertNotNull(attempt.getCreatedAt());
        assertNotNull(attempt.getUpdatedAt());
        assertNull(attempt.getSubmission());
    }

    @Test
    public void startAttempt_nonexistentProblem_returns404() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/problems/NonExistent/attempts", null, Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void updateDraft_existingDraft_returns200AndUpdatesSubmission() {
        ResponseEntity<Attempt> startResponse = restTemplate.postForEntity(
                "/api/problems/" + validProblemId + "/attempts", null, Attempt.class);
        Attempt attempt = startResponse.getBody();
        
        Submission update = new Submission("my design", "my code", "my explanation");
        HttpEntity<Submission> request = new HttpEntity<>(update);
        
        ResponseEntity<Attempt> updateResponse = restTemplate.exchange(
                "/api/attempts/" + attempt.getId(), HttpMethod.PUT, request, Attempt.class);
                
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        Attempt updated = updateResponse.getBody();
        assertNotNull(updated.getSubmission());
        assertEquals("my design", updated.getSubmission().getDesign());
        assertEquals("my code", updated.getSubmission().getCode());
        assertEquals("my explanation", updated.getSubmission().getExplanation());
        assertTrue(updated.getUpdatedAt().isAfter(attempt.getUpdatedAt()) || updated.getUpdatedAt().equals(attempt.getUpdatedAt()));
    }

    @Test
    public void submitAttempt_validDraft_returns200AndChangesStatus() {
        ResponseEntity<Attempt> startResponse = restTemplate.postForEntity(
                "/api/problems/" + validProblemId + "/attempts", null, Attempt.class);
        Attempt attempt = startResponse.getBody();
        
        Submission update = new Submission("my design", "my code", "my explanation");
        restTemplate.put("/api/attempts/" + attempt.getId(), update);
        
        ResponseEntity<Attempt> submitResponse = restTemplate.postForEntity(
                "/api/attempts/" + attempt.getId() + "/submit", null, Attempt.class);
                
        assertEquals(HttpStatus.OK, submitResponse.getStatusCode());
        Attempt submitted = submitResponse.getBody();
        assertEquals(AttemptStatus.SUBMITTED, submitted.getStatus());
    }

    @Test
    public void submitAttempt_emptySubmission_returns400() {
        ResponseEntity<Attempt> startResponse = restTemplate.postForEntity(
                "/api/problems/" + validProblemId + "/attempts", null, Attempt.class);
        Attempt attempt = startResponse.getBody();
        
        ResponseEntity<String> submitResponse = restTemplate.postForEntity(
                "/api/attempts/" + attempt.getId() + "/submit", null, String.class);
                
        assertEquals(HttpStatus.BAD_REQUEST, submitResponse.getStatusCode());
    }

    @Test
    public void updateDraft_submittedAttempt_returns400() {
        ResponseEntity<Attempt> startResponse = restTemplate.postForEntity(
                "/api/problems/" + validProblemId + "/attempts", null, Attempt.class);
        Attempt attempt = startResponse.getBody();
        
        Submission update = new Submission("my design", "my code", "my explanation");
        restTemplate.put("/api/attempts/" + attempt.getId(), update);
        restTemplate.postForEntity("/api/attempts/" + attempt.getId() + "/submit", null, Attempt.class);
        
        HttpEntity<Submission> request = new HttpEntity<>(update);
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                "/api/attempts/" + attempt.getId(), HttpMethod.PUT, request, String.class);
                
        assertEquals(HttpStatus.BAD_REQUEST, updateResponse.getStatusCode());
    }

    @Test
    public void submitAttempt_alreadySubmitted_returns400() {
        ResponseEntity<Attempt> startResponse = restTemplate.postForEntity(
                "/api/problems/" + validProblemId + "/attempts", null, Attempt.class);
        Attempt attempt = startResponse.getBody();
        
        Submission update = new Submission("my design", "my code", "my explanation");
        restTemplate.put("/api/attempts/" + attempt.getId(), update);
        restTemplate.postForEntity("/api/attempts/" + attempt.getId() + "/submit", null, Attempt.class);
        
        ResponseEntity<String> submitResponse = restTemplate.postForEntity(
                "/api/attempts/" + attempt.getId() + "/submit", null, String.class);
                
        assertEquals(HttpStatus.BAD_REQUEST, submitResponse.getStatusCode());
    }

    @Test
    public void getAttempt_nonexistentAttempt_returns404() {
        ResponseEntity<Attempt> response = restTemplate.getForEntity(
                "/api/attempts/nonexistent-id", Attempt.class);
                
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getAllAttempts_returnsAttemptsOrderedNewestFirst() throws InterruptedException {
        // Start two attempts
        ResponseEntity<Attempt> startResponse1 = restTemplate.postForEntity(
                "/api/problems/" + validProblemId + "/attempts", null, Attempt.class);
        Thread.sleep(10); // Ensure different updated times
        ResponseEntity<Attempt> startResponse2 = restTemplate.postForEntity(
                "/api/problems/" + validProblemId + "/attempts", null, Attempt.class);

        ResponseEntity<List<AttemptHistoryResponse>> response = restTemplate.exchange(
                "/api/attempts",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AttemptHistoryResponse>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<AttemptHistoryResponse> attempts = response.getBody();
        assertTrue(attempts.size() >= 2);
        
        // Find our two attempts and ensure attempt2 comes before attempt1
        int idx1 = -1, idx2 = -1;
        for (int i = 0; i < attempts.size(); i++) {
            if (attempts.get(i).getId().equals(startResponse1.getBody().getId())) idx1 = i;
            if (attempts.get(i).getId().equals(startResponse2.getBody().getId())) idx2 = i;
        }
        assertTrue(idx1 != -1 && idx2 != -1);
        assertTrue(idx2 < idx1); // attempt2 is newer, so smaller index
    }

    @Test
    public void getAttemptsByProblemId_existingProblem_returnsOnlyThatProblemAttempts() {
        ResponseEntity<Attempt> startResponse = restTemplate.postForEntity(
                "/api/problems/" + validProblemId + "/attempts", null, Attempt.class);

        ResponseEntity<List<AttemptHistoryResponse>> response = restTemplate.exchange(
                "/api/problems/" + validProblemId + "/attempts",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AttemptHistoryResponse>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<AttemptHistoryResponse> attempts = response.getBody();
        assertFalse(attempts.isEmpty());
        for (AttemptHistoryResponse attempt : attempts) {
            assertEquals(validProblemId, attempt.getProblemId());
        }
    }

    @Test
    public void getAttemptsByProblemId_nonexistentProblem_returns404() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/problems/nonexistent-problem-id/attempts", String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getAttemptsByProblemId_existingProblemNoAttempts_returnsEmptyList() {
        ResponseEntity<Problem[]> problemsResponse = restTemplate.getForEntity("/api/problems", Problem[].class);
        String secondProblemId = problemsResponse.getBody()[2].getId();
        
        ResponseEntity<List<AttemptHistoryResponse>> response = restTemplate.exchange(
                "/api/problems/" + secondProblemId + "/attempts",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AttemptHistoryResponse>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    public void historyIncludesEvaluationScoreAfterSuccessfulEvaluation() {
        ResponseEntity<Attempt> startResponse = restTemplate.postForEntity(
                "/api/problems/" + validProblemId + "/attempts", null, Attempt.class);
        String attemptId = startResponse.getBody().getId();
        
        Submission update = new Submission("design class", "code public", "explanation extend");
        restTemplate.put("/api/attempts/" + attemptId, update);
        restTemplate.postForEntity("/api/attempts/" + attemptId + "/submit", null, Attempt.class);
        
        restTemplate.postForEntity("/api/attempts/" + attemptId + "/evaluate", null, String.class);
        
        ResponseEntity<List<AttemptHistoryResponse>> response = restTemplate.exchange(
                "/api/attempts",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AttemptHistoryResponse>>() {}
        );
        
        List<AttemptHistoryResponse> attempts = response.getBody();
        AttemptHistoryResponse evaluatedAttempt = attempts.stream()
                .filter(a -> a.getId().equals(attemptId))
                .findFirst()
                .orElse(null);
                
        assertNotNull(evaluatedAttempt);
        assertEquals(AttemptStatus.COMPLETED, evaluatedAttempt.getStatus());
        assertNotNull(evaluatedAttempt.getEvaluationScore());
    }
}

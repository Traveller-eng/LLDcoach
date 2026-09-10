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
}

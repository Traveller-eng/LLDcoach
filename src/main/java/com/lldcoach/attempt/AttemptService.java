package com.lldcoach.attempt;

import com.lldcoach.problem.ProblemService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AttemptService {

    private final ProblemService problemService;
    private final Map<String, Attempt> attemptStore = new ConcurrentHashMap<>();

    public AttemptService(ProblemService problemService) {
        this.problemService = problemService;
    }

    public Optional<Attempt> startAttempt(String problemId) {
        if (problemService.getProblemById(problemId).isEmpty()) {
            return Optional.empty();
        }

        Attempt attempt = new Attempt();
        attempt.setId(UUID.randomUUID().toString());
        attempt.setProblemId(problemId);
        attempt.setStatus(AttemptStatus.DRAFT);
        attempt.setCreatedAt(Instant.now());
        attempt.setUpdatedAt(attempt.getCreatedAt());
        attempt.setSubmission(null);

        attemptStore.put(attempt.getId(), attempt);
        return Optional.of(attempt);
    }

    public Optional<Attempt> getAttempt(String attemptId) {
        return Optional.ofNullable(attemptStore.get(attemptId));
    }

    public Attempt updateDraft(String attemptId, Submission submissionUpdate) {
        Attempt attempt = attemptStore.get(attemptId);
        if (attempt == null) {
            throw new IllegalArgumentException("Attempt not found");
        }
        
        if (attempt.getStatus() != AttemptStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT attempts can be updated");
        }

        Submission sub = attempt.getSubmission();
        if (sub == null) {
            sub = new Submission();
            attempt.setSubmission(sub);
        }
        
        if (submissionUpdate != null) {
            if (submissionUpdate.getDesign() != null) sub.setDesign(submissionUpdate.getDesign());
            if (submissionUpdate.getCode() != null) sub.setCode(submissionUpdate.getCode());
            if (submissionUpdate.getExplanation() != null) sub.setExplanation(submissionUpdate.getExplanation());
        }
        
        attempt.setUpdatedAt(Instant.now());
        return attempt;
    }

    public Attempt submitAttempt(String attemptId) {
        Attempt attempt = attemptStore.get(attemptId);
        if (attempt == null) {
            throw new IllegalArgumentException("Attempt not found");
        }
        
        if (attempt.getStatus() != AttemptStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT attempts can be submitted");
        }

        Submission sub = attempt.getSubmission();
        boolean hasContent = sub != null && (
            (sub.getDesign() != null && !sub.getDesign().trim().isEmpty()) ||
            (sub.getCode() != null && !sub.getCode().trim().isEmpty()) ||
            (sub.getExplanation() != null && !sub.getExplanation().trim().isEmpty())
        );

        if (!hasContent) {
            throw new IllegalArgumentException("Cannot submit an empty attempt");
        }

        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setUpdatedAt(Instant.now());
        return attempt;
    }

    public void updateStatus(String attemptId, AttemptStatus status) {
        Attempt attempt = attemptStore.get(attemptId);
        if (attempt != null) {
            attempt.setStatus(status);
            attempt.setUpdatedAt(Instant.now());
        }
    }
}

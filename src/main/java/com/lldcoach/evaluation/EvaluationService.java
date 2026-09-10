package com.lldcoach.evaluation;

import com.lldcoach.attempt.Attempt;
import com.lldcoach.attempt.AttemptService;
import com.lldcoach.attempt.AttemptStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EvaluationService {

    private final Evaluator evaluator;
    private final AttemptService attemptService;
    private final Map<String, Evaluation> evaluationStore = new ConcurrentHashMap<>();

    public EvaluationService(Evaluator evaluator, AttemptService attemptService) {
        this.evaluator = evaluator;
        this.attemptService = attemptService;
    }

    public Evaluation evaluateAttempt(String attemptId) {
        Optional<Attempt> optionalAttempt = attemptService.getAttempt(attemptId);
        if (optionalAttempt.isEmpty()) {
            throw new IllegalArgumentException("Attempt not found");
        }

        Attempt attempt = optionalAttempt.get();
        if (attempt.getStatus() != AttemptStatus.SUBMITTED && attempt.getStatus() != AttemptStatus.FAILED) {
            throw new IllegalStateException("Only SUBMITTED or FAILED attempts can be evaluated");
        }

        // Change status to EVALUATING
        attemptService.updateStatus(attemptId, AttemptStatus.EVALUATING);

        try {
            Evaluation evaluation = evaluator.evaluate(attempt, attempt.getSubmission());
            evaluationStore.put(attempt.getId(), evaluation);
            
            // On success, change to COMPLETED
            attemptService.updateStatus(attemptId, AttemptStatus.COMPLETED);
            return evaluation;
        } catch (Exception e) {
            // On failure, change to FAILED
            attemptService.updateStatus(attemptId, AttemptStatus.FAILED);
            throw new RuntimeException("Evaluation failed", e);
        }
    }

    public Optional<Evaluation> getEvaluationByAttemptId(String attemptId) {
        return Optional.ofNullable(evaluationStore.get(attemptId));
    }
}

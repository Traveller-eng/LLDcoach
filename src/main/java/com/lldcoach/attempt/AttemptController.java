package com.lldcoach.attempt;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lldcoach.evaluation.EvaluationService;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AttemptController {

    private final AttemptService attemptService;
    private final EvaluationService evaluationService;

    public AttemptController(AttemptService attemptService, EvaluationService evaluationService) {
        this.attemptService = attemptService;
        this.evaluationService = evaluationService;
    }

    @PostMapping("/problems/{problemId}/attempts")
    public ResponseEntity<Attempt> startAttempt(@PathVariable String problemId) {
        return attemptService.startAttempt(problemId)
                .map(attempt -> ResponseEntity.status(HttpStatus.CREATED).body(attempt))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<Attempt> getAttempt(@PathVariable String attemptId) {
        return attemptService.getAttempt(attemptId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/attempts")
    public ResponseEntity<List<AttemptHistoryResponse>> getAllAttempts() {
        List<AttemptHistoryResponse> responses = attemptService.getAllAttempts().stream()
                .map(this::toHistoryResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/problems/{problemId}/attempts")
    public ResponseEntity<List<AttemptHistoryResponse>> getAttemptsByProblemId(@PathVariable String problemId) {
        return attemptService.getAttemptsByProblemId(problemId)
                .map(attempts -> {
                    List<AttemptHistoryResponse> responses = attempts.stream()
                            .map(this::toHistoryResponse)
                            .toList();
                    return ResponseEntity.ok(responses);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private AttemptHistoryResponse toHistoryResponse(Attempt attempt) {
        AttemptHistoryResponse res = new AttemptHistoryResponse();
        res.setId(attempt.getId());
        res.setProblemId(attempt.getProblemId());
        res.setStatus(attempt.getStatus());
        res.setSubmission(attempt.getSubmission());
        res.setCreatedAt(attempt.getCreatedAt());
        res.setUpdatedAt(attempt.getUpdatedAt());

        if (attempt.getStatus() == AttemptStatus.COMPLETED) {
            evaluationService.getEvaluationByAttemptId(attempt.getId())
                    .ifPresent(eval -> res.setEvaluationScore(eval.getOverallScore()));
        }
        return res;
    }

    @PutMapping("/attempts/{attemptId}")
    public ResponseEntity<?> updateAttempt(@PathVariable String attemptId, @RequestBody Submission submission) {
        try {
            Attempt updated = attemptService.updateDraft(attemptId, submission);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            if ("Attempt not found".equals(e.getMessage())) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<?> submitAttempt(@PathVariable String attemptId) {
        try {
            Attempt submitted = attemptService.submitAttempt(attemptId);
            return ResponseEntity.ok(submitted);
        } catch (IllegalArgumentException e) {
            if ("Attempt not found".equals(e.getMessage())) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 or 409
        }
    }
}

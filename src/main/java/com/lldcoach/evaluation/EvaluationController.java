package com.lldcoach.evaluation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attempts")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping("/{attemptId}/evaluate")
    public ResponseEntity<?> evaluateAttempt(@PathVariable String attemptId) {
        try {
            Evaluation evaluation = evaluationService.evaluateAttempt(attemptId);
            return ResponseEntity.ok(evaluation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Evaluator failure results in 500, but Attempt becomes FAILED
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{attemptId}/evaluation")
    public ResponseEntity<Evaluation> getEvaluation(@PathVariable String attemptId) {
        return evaluationService.getEvaluationByAttemptId(attemptId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

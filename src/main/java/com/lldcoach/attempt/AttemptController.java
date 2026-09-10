package com.lldcoach.attempt;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
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

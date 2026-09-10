package com.lldcoach.problem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for handling problem-related HTTP requests.
 * Handles HTTP requests and delegates to ProblemService.
 */
@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    /**
     * GET /api/problems
     * Returns all available problems.
     */
    @GetMapping
    public ResponseEntity<List<Problem>> getAllProblems() {
        return ResponseEntity.ok(problemService.getAllProblems());
    }

    /**
     * GET /api/problems/{id}
     * Returns a single problem by ID.
     * Returns 404 if the problem doesn't exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Problem> getProblemById(@PathVariable String id) {
        return problemService.getProblemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

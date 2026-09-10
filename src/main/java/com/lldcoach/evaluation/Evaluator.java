package com.lldcoach.evaluation;

import com.lldcoach.attempt.Attempt;
import com.lldcoach.attempt.Submission;

public interface Evaluator {
    Evaluation evaluate(Attempt attempt, Submission submission);
}

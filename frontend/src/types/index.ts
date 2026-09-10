export interface Problem {
  id: string;
  title: string;
  description: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  requirements: string[];
}

export type AttemptStatus = 'DRAFT' | 'SUBMITTED' | 'EVALUATING' | 'COMPLETED' | 'FAILED';

export interface Submission {
  design: string | null;
  code: string | null;
  explanation: string | null;
}

export interface Attempt {
  id: string;
  problemId: string;
  status: AttemptStatus;
  submission: Submission | null;
  createdAt: string;
  updatedAt: string;
}

export interface AttemptHistoryResponse {
  id: string;
  problemId: string;
  status: AttemptStatus;
  submission: Submission | null;
  createdAt: string;
  updatedAt: string;
  evaluationScore: number | null;
}

export interface Evaluation {
  id: string;
  attemptId: string;
  overallScore: number;
  dimensionScores: Record<string, number>;
  strengths: string[];
  issues: string[];
  suggestions: string[];
  createdAt: string;
}

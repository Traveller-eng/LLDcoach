import type { Problem, Attempt, AttemptHistoryResponse, Evaluation, Submission } from '../types';

const API_BASE = '/api';

export const api = {
  getProblems: async (): Promise<Problem[]> => {
    const res = await fetch(`${API_BASE}/problems`);
    if (!res.ok) throw new Error('Failed to fetch problems');
    return res.json();
  },

  getProblem: async (id: string): Promise<Problem> => {
    const res = await fetch(`${API_BASE}/problems/${id}`);
    if (!res.ok) throw new Error('Failed to fetch problem');
    return res.json();
  },

  startAttempt: async (problemId: string): Promise<Attempt> => {
    const res = await fetch(`${API_BASE}/problems/${problemId}/attempts`, { method: 'POST' });
    if (!res.ok) throw new Error('Failed to start attempt');
    return res.json();
  },

  getAttempt: async (attemptId: string): Promise<Attempt> => {
    const res = await fetch(`${API_BASE}/attempts/${attemptId}`);
    if (!res.ok) throw new Error('Failed to fetch attempt');
    return res.json();
  },

  updateAttempt: async (attemptId: string, submission: Submission): Promise<Attempt> => {
    const res = await fetch(`${API_BASE}/attempts/${attemptId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(submission),
    });
    if (!res.ok) {
      const errText = await res.text();
      throw new Error(errText || 'Failed to update attempt');
    }
    return res.json();
  },

  submitAttempt: async (attemptId: string): Promise<Attempt> => {
    const res = await fetch(`${API_BASE}/attempts/${attemptId}/submit`, { method: 'POST' });
    if (!res.ok) {
      const errText = await res.text();
      throw new Error(errText || 'Failed to submit attempt');
    }
    return res.json();
  },

  evaluateAttempt: async (attemptId: string): Promise<Evaluation> => {
    const res = await fetch(`${API_BASE}/attempts/${attemptId}/evaluate`, { method: 'POST' });
    if (!res.ok) {
      const errText = await res.text();
      throw new Error(errText || 'Failed to evaluate attempt');
    }
    return res.json();
  },

  getEvaluation: async (attemptId: string): Promise<Evaluation> => {
    const res = await fetch(`${API_BASE}/attempts/${attemptId}/evaluation`);
    if (!res.ok) throw new Error('Failed to fetch evaluation');
    return res.json();
  },

  getAttempts: async (): Promise<AttemptHistoryResponse[]> => {
    const res = await fetch(`${API_BASE}/attempts`);
    if (!res.ok) throw new Error('Failed to fetch attempts');
    return res.json();
  },

  getAttemptsByProblem: async (problemId: string): Promise<AttemptHistoryResponse[]> => {
    const res = await fetch(`${API_BASE}/problems/${problemId}/attempts`);
    if (!res.ok) throw new Error('Failed to fetch attempts for problem');
    return res.json();
  }
};

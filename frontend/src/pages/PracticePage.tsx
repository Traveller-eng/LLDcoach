import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import type { Problem, Attempt, Evaluation } from '../types';

export default function PracticePage() {
  const { problemId, attemptId } = useParams<{ problemId?: string; attemptId?: string }>();
  const navigate = useNavigate();

  const [problem, setProblem] = useState<Problem | null>(null);
  const [attempt, setAttempt] = useState<Attempt | null>(null);
  const [evaluation, setEvaluation] = useState<Evaluation | null>(null);
  
  const [design, setDesign] = useState('');
  const [code, setCode] = useState('');
  const [explanation, setExplanation] = useState('');
  
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [evaluating, setEvaluating] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadData() {
      setLoading(true);
      setError('');
      try {
        if (attemptId) {
          const loadedAttempt = await api.getAttempt(attemptId);
          setAttempt(loadedAttempt);
          const loadedProblem = await api.getProblem(loadedAttempt.problemId);
          setProblem(loadedProblem);
          
          if (loadedAttempt.submission) {
            setDesign(loadedAttempt.submission.design || '');
            setCode(loadedAttempt.submission.code || '');
            setExplanation(loadedAttempt.submission.explanation || '');
          } else {
            setDesign('');
            setCode('');
            setExplanation('');
          }
          
          if (loadedAttempt.status === 'COMPLETED') {
            const evalData = await api.getEvaluation(attemptId);
            setEvaluation(evalData);
          } else {
            setEvaluation(null);
          }
        } else if (problemId) {
          const loadedProblem = await api.getProblem(problemId);
          setProblem(loadedProblem);
          setAttempt(null);
          setEvaluation(null);
          setDesign('');
          setCode('');
          setExplanation('');
        }
      } catch (e: any) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, [problemId, attemptId]);

  const handleSaveDraft = async () => {
    if (!problem) return;
    setSaving(true);
    setError('');
    try {
      let currentAttemptId = attempt?.id;
      if (!currentAttemptId) {
        const newAttempt = await api.startAttempt(problem.id);
        currentAttemptId = newAttempt.id;
        navigate(`/attempts/${currentAttemptId}`, { replace: true });
      }
      
      const updated = await api.updateAttempt(currentAttemptId, { design, code, explanation });
      setAttempt(updated);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setSaving(false);
    }
  };

  const handleSubmit = async () => {
    if (!attempt) return;
    setSaving(true);
    setError('');
    try {
      await api.updateAttempt(attempt.id, { design, code, explanation });
      const submitted = await api.submitAttempt(attempt.id);
      setAttempt(submitted);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setSaving(false);
    }
  };

  const handleEvaluate = async () => {
    if (!attempt) return;
    setEvaluating(true);
    setError('');
    try {
      const evalData = await api.evaluateAttempt(attempt.id);
      setEvaluation(evalData);
      const updatedAttempt = await api.getAttempt(attempt.id);
      setAttempt(updatedAttempt);
    } catch (e: any) {
      setError(e.message);
      const updatedAttempt = await api.getAttempt(attempt.id);
      setAttempt(updatedAttempt);
    } finally {
      setEvaluating(false);
    }
  };

  const handlePracticeAgain = async () => {
    if (!problem) return;
    setSaving(true);
    setError('');
    try {
      const newAttempt = await api.startAttempt(problem.id);
      navigate(`/attempts/${newAttempt.id}`);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="p-4 text-gray-500 dark:text-gray-400">Loading...</div>;
  if (!problem) return <div className="p-4 text-red-500 dark:text-red-400">Problem not found.</div>;

  const isEditable = !attempt || attempt.status === 'DRAFT';

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'DRAFT': return <span className="px-2 py-1 bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300 rounded text-xs font-medium">DRAFT</span>;
      case 'SUBMITTED': return <span className="px-2 py-1 bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200 rounded text-xs font-medium">SUBMITTED</span>;
      case 'FAILED': return <span className="px-2 py-1 bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200 rounded text-xs font-medium">FAILED</span>;
      case 'EVALUATING': return <span className="px-2 py-1 bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200 rounded text-xs font-medium">EVALUATING</span>;
      case 'COMPLETED': return <span className="px-2 py-1 bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200 rounded text-xs font-medium">COMPLETED</span>;
      default: return <span className="px-2 py-1 bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300 rounded text-xs font-medium">{status}</span>;
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
      {/* Problem Details */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 flex flex-col h-[calc(100vh-8rem)] overflow-y-auto">
        <div className="flex justify-between items-start mb-4">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">{problem.title}</h1>
          {attempt && attempt.status !== 'DRAFT' && getStatusBadge(attempt.status)}
        </div>
        <p className="text-gray-700 dark:text-gray-300 mb-6 leading-relaxed">{problem.description}</p>
        <h3 className="font-semibold text-lg text-gray-900 dark:text-gray-100 mb-3">Requirements</h3>
        <ul className="list-disc pl-5 space-y-2 mb-6">
          {problem.requirements.map((req, i) => (
            <li key={i} className="text-gray-600 dark:text-gray-400">{req}</li>
          ))}
        </ul>
        
        {error && (
          <div className="mt-4 p-4 rounded-lg bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-800">
            <p className="text-sm text-red-800 dark:text-red-300">{error}</p>
          </div>
        )}
      </div>

      {/* Workspace */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 flex flex-col h-[calc(100vh-8rem)] overflow-y-auto">
        <div className="flex-1 space-y-6">
          <div>
            <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">Design Document</label>
            <textarea 
              disabled={!isEditable}
              value={design}
              onChange={e => setDesign(e.target.value)}
              className="w-full h-32 p-3 bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 disabled:bg-gray-50 dark:disabled:bg-gray-800 text-gray-900 dark:text-gray-100 placeholder-gray-400 transition-colors"
              placeholder="Describe classes, interfaces, etc."
            />
          </div>
          <div>
            <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">Code Implementation</label>
            <textarea 
              disabled={!isEditable}
              value={code}
              onChange={e => setCode(e.target.value)}
              className="w-full h-48 p-3 font-mono text-sm bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 disabled:bg-gray-50 dark:disabled:bg-gray-800 text-gray-900 dark:text-gray-100 placeholder-gray-400 transition-colors"
              placeholder="public class ..."
            />
          </div>
          <div>
            <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">Design Explanation</label>
            <textarea 
              disabled={!isEditable}
              value={explanation}
              onChange={e => setExplanation(e.target.value)}
              className="w-full h-24 p-3 bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 disabled:bg-gray-50 dark:disabled:bg-gray-800 text-gray-900 dark:text-gray-100 placeholder-gray-400 transition-colors"
              placeholder="Why did you choose this design?"
            />
          </div>
        </div>

        <div className="mt-8 flex space-x-4">
          {isEditable ? (
            <>
              <button 
                onClick={handleSaveDraft}
                disabled={saving || (!design && !code && !explanation)}
                className="px-5 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 transition-colors shadow-sm"
              >
                {saving ? 'Saving...' : 'Save Draft'}
              </button>
              <button 
                onClick={handleSubmit}
                disabled={saving || (!design && !code && !explanation)}
                className="px-5 py-2.5 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50 transition-colors shadow-sm"
              >
                Submit Attempt
              </button>
            </>
          ) : (attempt.status === 'SUBMITTED' || attempt.status === 'FAILED') ? (
            <button 
              onClick={handleEvaluate}
              disabled={evaluating}
              className="px-5 py-2.5 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50 transition-colors shadow-sm"
            >
              {evaluating ? 'Evaluating...' : 'Evaluate Submission'}
            </button>
          ) : attempt.status === 'COMPLETED' && evaluation ? (
            <div className="w-full flex flex-col">
              <div className="flex justify-between items-center mb-4">
                <h3 className="font-bold text-lg text-gray-900 dark:text-gray-100">Evaluation Results</h3>
                <button
                  onClick={handlePracticeAgain}
                  disabled={saving}
                  className="px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 transition-colors shadow-sm disabled:opacity-50"
                >
                  {saving ? 'Creating...' : 'Practice Again'}
                </button>
              </div>
              <div className="p-5 bg-gray-50 dark:bg-gray-900/50 rounded-xl border border-gray-200 dark:border-gray-700">
                <div className="flex justify-between items-center mb-6">
                  <span className="font-semibold text-gray-700 dark:text-gray-300 text-lg">Overall Score</span>
                  <span className={`text-3xl font-bold ${evaluation.overallScore >= 80 ? 'text-green-600 dark:text-green-400' : evaluation.overallScore >= 50 ? 'text-yellow-600 dark:text-yellow-400' : 'text-red-600 dark:text-red-400'}`}>
                    {evaluation.overallScore}/100
                  </span>
                </div>
                
                <h4 className="font-semibold text-gray-800 dark:text-gray-200 mb-3 border-b border-gray-200 dark:border-gray-700 pb-2">Dimensions</h4>
                <div className="grid grid-cols-2 gap-x-4 gap-y-3 text-sm mb-6">
                  {evaluation.dimensionScores && Object.entries(evaluation.dimensionScores).map(([key, val]) => (
                    <div key={key} className="flex justify-between items-center">
                      <span className="text-gray-600 dark:text-gray-400">{key}</span>
                      <span className="font-semibold text-gray-900 dark:text-gray-100 bg-white dark:bg-gray-800 px-2 py-0.5 rounded border border-gray-200 dark:border-gray-700">{val}</span>
                    </div>
                  ))}
                </div>

                {(evaluation.strengths?.length ?? 0) > 0 && (
                  <div className="mb-5">
                    <h4 className="font-semibold text-green-700 dark:text-green-400 mb-2 flex items-center">
                      <span className="mr-2">✓</span> Strengths
                    </h4>
                    <ul className="space-y-1.5 pl-6 text-sm text-gray-600 dark:text-gray-400 list-disc marker:text-green-500">
                      {evaluation.strengths.map((s, i) => <li key={i}>{s}</li>)}
                    </ul>
                  </div>
                )}
                
                {(evaluation.issues?.length ?? 0) > 0 && (
                  <div className="mb-5">
                    <h4 className="font-semibold text-red-700 dark:text-red-400 mb-2 flex items-center">
                      <span className="mr-2">✗</span> Issues
                    </h4>
                    <ul className="space-y-1.5 pl-6 text-sm text-gray-600 dark:text-gray-400 list-disc marker:text-red-500">
                      {evaluation.issues.map((s, i) => <li key={i}>{s}</li>)}
                    </ul>
                  </div>
                )}
                
                {(evaluation.suggestions?.length ?? 0) > 0 && (
                  <div>
                    <h4 className="font-semibold text-blue-700 dark:text-blue-400 mb-2 flex items-center">
                      <span className="mr-2">💡</span> Suggestions
                    </h4>
                    <ul className="space-y-1.5 pl-6 text-sm text-gray-600 dark:text-gray-400 list-disc marker:text-blue-500">
                      {evaluation.suggestions.map((s, i) => <li key={i}>{s}</li>)}
                    </ul>
                  </div>
                )}
              </div>
            </div>
          ) : null}
        </div>
      </div>
    </div>
  );
}

import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import type { AttemptHistoryResponse } from '../types';

export default function HistoryPage() {
  const [history, setHistory] = useState<AttemptHistoryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    api.getAttempts()
      .then(setHistory)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  const handlePracticeAgain = async (e: React.MouseEvent, problemId: string) => {
    e.preventDefault();
    try {
      const newAttempt = await api.startAttempt(problemId);
      navigate(`/attempts/${newAttempt.id}`);
    } catch (err: any) {
      alert(err.message || 'Failed to start practice');
    }
  };

  if (loading) return <div className="p-4 text-gray-500 dark:text-gray-400">Loading history...</div>;
  if (error) return <div className="p-4 text-red-500 dark:text-red-400">Error: {error}</div>;

  if (history.length === 0) {
    return (
      <div className="text-center py-16 bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
        <h3 className="mt-2 text-lg font-semibold text-gray-900 dark:text-gray-100">No attempts yet</h3>
        <p className="mt-2 text-gray-500 dark:text-gray-400">Get started by practicing a problem.</p>
        <div className="mt-6">
          <Link to="/" className="inline-flex items-center rounded-lg bg-indigo-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-indigo-700 transition-colors shadow-sm">
            View Problems
          </Link>
        </div>
      </div>
    );
  }

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'DRAFT': return <span className="px-2.5 py-1 bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300 rounded-full text-xs font-semibold">DRAFT</span>;
      case 'SUBMITTED': return <span className="px-2.5 py-1 bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200 rounded-full text-xs font-semibold">SUBMITTED</span>;
      case 'FAILED': return <span className="px-2.5 py-1 bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200 rounded-full text-xs font-semibold">FAILED</span>;
      case 'EVALUATING': return <span className="px-2.5 py-1 bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200 rounded-full text-xs font-semibold">EVALUATING</span>;
      case 'COMPLETED': return <span className="px-2.5 py-1 bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200 rounded-full text-xs font-semibold">COMPLETED</span>;
      default: return <span className="px-2.5 py-1 bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300 rounded-full text-xs font-semibold">{status}</span>;
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">Attempt History</h1>
      <div className="bg-white dark:bg-gray-800 shadow-sm border border-gray-200 dark:border-gray-700 sm:rounded-xl overflow-hidden">
        <ul role="list" className="divide-y divide-gray-200 dark:divide-gray-700">
          {history.map(attempt => (
            <li key={attempt.id}>
              <Link to={`/attempts/${attempt.id}`} className="block hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
                <div className="px-4 py-5 sm:px-6">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      <p className="text-lg font-medium text-indigo-600 dark:text-indigo-400 truncate">
                        Attempt {attempt.id.substring(0, 8)}
                      </p>
                      {getStatusBadge(attempt.status)}
                    </div>
                    {attempt.status === 'COMPLETED' || attempt.status === 'FAILED' ? (
                      <button
                        onClick={(e) => handlePracticeAgain(e, attempt.problemId)}
                        className="text-sm font-medium text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300"
                      >
                        Practice Again
                      </button>
                    ) : null}
                  </div>
                  <div className="mt-3 sm:flex sm:justify-between">
                    <div className="sm:flex">
                      <p className="flex items-center text-sm text-gray-500 dark:text-gray-400">
                        Problem ID: {attempt.problemId}
                      </p>
                    </div>
                    <div className="mt-2 flex items-center text-sm text-gray-500 dark:text-gray-400 sm:mt-0 gap-4">
                      {attempt.evaluationScore !== null && (
                        <span className="font-semibold text-gray-900 dark:text-gray-100">Score: {attempt.evaluationScore}/100</span>
                      )}
                      <p>
                        Updated: {new Date(attempt.updatedAt).toLocaleString()}
                      </p>
                    </div>
                  </div>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

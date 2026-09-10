import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import type { Problem } from '../types';

export default function ProblemsPage() {
  const [problems, setProblems] = useState<Problem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadProblems() {
      try {
        const data = await api.getProblems();
        setProblems(data);
      } catch (e: any) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    }
    loadProblems();
  }, []);

  if (loading) return <div className="p-4 text-gray-500 dark:text-gray-400">Loading problems...</div>;
  if (error) return <div className="p-4 text-red-500 dark:text-red-400">Error: {error}</div>;

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">Available Problems</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {problems.map((p) => (
          <div key={p.id} className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 flex flex-col hover:shadow-md transition-shadow">
            <div className="flex justify-between items-start mb-4">
              <h2 className="text-xl font-bold text-gray-900 dark:text-gray-100">{p.title}</h2>
              <span className={`px-2.5 py-1 text-xs font-semibold rounded-full ${
                p.difficulty === 'EASY' ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' :
                p.difficulty === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200' :
                'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
              }`}>
                {p.difficulty}
              </span>
            </div>
            <p className="text-gray-600 dark:text-gray-400 mb-6 flex-1 line-clamp-3">
              {p.description}
            </p>
            <Link 
              to={`/practice/${p.id}`}
              className="mt-auto text-center px-4 py-2 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 transition-colors shadow-sm"
            >
              Practice
            </Link>
          </div>
        ))}
      </div>
    </div>
  );
}

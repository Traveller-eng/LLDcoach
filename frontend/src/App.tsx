import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import ProblemsPage from './pages/ProblemsPage';
import HistoryPage from './pages/HistoryPage';
import PracticePage from './pages/PracticePage';
import { BookOpen, Clock, Moon, Sun } from 'lucide-react';

function App() {
  const [isDark, setIsDark] = useState(() => {
    if (typeof window !== 'undefined') {
      const saved = localStorage.getItem('theme');
      if (saved) return saved === 'dark';
      return window.matchMedia('(prefers-color-scheme: dark)').matches;
    }
    return false;
  });

  useEffect(() => {
    const root = window.document.documentElement;
    if (isDark) {
      root.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      root.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  }, [isDark]);

  return (
    <Router>
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 font-sans text-gray-900 dark:text-gray-100 transition-colors duration-200">
        <header className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 sticky top-0 z-10">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between h-16">
              <div className="flex">
                <div className="flex-shrink-0 flex items-center">
                  <span className="text-xl font-bold text-indigo-600 dark:text-indigo-400">LLD Coach</span>
                </div>
                <nav className="ml-6 flex space-x-8">
                  <Link to="/" className="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-900 dark:text-gray-100 hover:text-indigo-600 dark:hover:text-indigo-400">
                    <BookOpen className="w-4 h-4 mr-2" /> Problems
                  </Link>
                  <Link to="/history" className="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-500 dark:text-gray-400 hover:text-indigo-600 dark:hover:text-indigo-400">
                    <Clock className="w-4 h-4 mr-2" /> History
                  </Link>
                </nav>
              </div>
              <div className="flex items-center">
                <button
                  onClick={() => setIsDark(!isDark)}
                  className="p-2 rounded-md text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-indigo-500 transition-colors"
                  aria-label="Toggle dark mode"
                >
                  {isDark ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
                </button>
              </div>
            </div>
          </div>
        </header>

        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Routes>
            <Route path="/" element={<ProblemsPage />} />
            <Route path="/history" element={<HistoryPage />} />
            <Route path="/practice/:problemId" element={<PracticePage />} />
            <Route path="/attempts/:attemptId" element={<PracticePage />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;

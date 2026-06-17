import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './hooks/useAuth';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import OpportunitiesPage from './pages/OpportunitiesPage';
import PipelinePage from './pages/PipelinePage';
import HotelsPage from './pages/HotelsPage';

function ProtectedLayout({ children }) {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <main>{children}</main>
    </div>
  );
}

const ROUTES = [
  { path: '/dashboard', Component: DashboardPage },
  { path: '/opportunities', Component: OpportunitiesPage },
  { path: '/pipeline', Component: PipelinePage },
  { path: '/hotels', Component: HotelsPage },
];

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        {ROUTES.map(({ path, Component }) => (
          <Route
            key={path}
            path={path}
            element={
              <ProtectedLayout>
                <Component />
              </ProtectedLayout>
            }
          />
        ))}
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

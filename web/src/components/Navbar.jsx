import { Link, useLocation, useNavigate } from 'react-router-dom';
import { LayoutDashboard, Target, Hotel, LogOut, Building2, Columns3 } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';

export default function Navbar() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navLink = (to, label, Icon) => {
    const active = location.pathname === to;
    return (
      <Link
        to={to}
        className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-all ${
          active
            ? 'bg-white/15 text-white shadow-sm'
            : 'text-emerald-100 hover:bg-white/10 hover:text-white'
        }`}
      >
        <Icon size={17} />
        {label}
      </Link>
    );
  };

  const initials = (user?.name || 'U')
    .split(' ')
    .map((w) => w[0])
    .slice(0, 2)
    .join('')
    .toUpperCase();

  return (
    <nav className="bg-gradient-to-r from-brand-900 to-brand-700 shadow-lg sticky top-0 z-30">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2.5">
              <div className="bg-white/15 rounded-lg p-1.5">
                <Hotel size={20} className="text-white" />
              </div>
              <span className="text-white font-bold text-lg tracking-tight hidden sm:inline">
                Ventas Hoteleras
              </span>
            </div>
            <div className="hidden md:flex gap-1">
              {navLink('/dashboard', 'Dashboard', LayoutDashboard)}
              {navLink('/opportunities', 'Oportunidades', Target)}
              {navLink('/pipeline', 'Pipeline', Columns3)}
              {navLink('/hotels', 'Hoteles', Building2)}
            </div>
          </div>
          <div className="flex items-center gap-3">
            <div className="hidden lg:flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-full bg-gold-500 flex items-center justify-center text-white text-xs font-bold">
                {initials}
              </div>
              <span className="text-emerald-50 text-sm font-medium">{user?.name}</span>
            </div>
            <button
              onClick={handleLogout}
              className="flex items-center gap-1.5 text-emerald-100 hover:text-white text-sm px-3 py-1.5 border border-white/20 rounded-lg hover:border-white/40 hover:bg-white/5 transition-all"
            >
              <LogOut size={15} />
              <span className="hidden sm:inline">Salir</span>
            </button>
          </div>
        </div>

        {/* Nav móvil */}
        <div className="md:hidden flex gap-1 overflow-x-auto pb-2 -mt-1">
          {navLink('/dashboard', 'Dashboard', LayoutDashboard)}
          {navLink('/opportunities', 'Ventas', Target)}
          {navLink('/pipeline', 'Pipeline', Columns3)}
          {navLink('/hotels', 'Hoteles', Building2)}
        </div>
      </div>
    </nav>
  );
}

import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useLocation } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import Customers from './pages/Customers';
import Resources from './pages/Resources';
import Monitoring from './pages/Monitoring';
import Billing from './pages/Billing';

function Navigation() {
  const location = useLocation();
  
  return (
    <div className="header">
      <div className="container">
        <h1>云资源管理系统</h1>
        <nav className="nav">
          <Link to="/" className={location.pathname === '/' ? 'active' : ''}>仪表盘</Link>
          <Link to="/customers" className={location.pathname === '/customers' ? 'active' : ''}>客户管理</Link>
          <Link to="/resources" className={location.pathname === '/resources' ? 'active' : ''}>资源管理</Link>
          <Link to="/monitoring" className={location.pathname === '/monitoring' ? 'active' : ''}>监控中心</Link>
          <Link to="/billing" className={location.pathname === '/billing' ? 'active' : ''}>费用管理</Link>
        </nav>
      </div>
    </div>
  );
}

function App() {
  return (
    <Router>
      <div className="app">
        <Navigation />
        <div className="container">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/customers" element={<Customers />} />
            <Route path="/resources" element={<Resources />} />
            <Route path="/monitoring" element={<Monitoring />} />
            <Route path="/billing" element={<Billing />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;

import React from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import './MainLayout.css';

const MainLayout = () => {
  return (
    <div className="layout-wrapper">
      <Sidebar />
      <div className="main-container">
        <Header />
        <main className="content-area">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default MainLayout;

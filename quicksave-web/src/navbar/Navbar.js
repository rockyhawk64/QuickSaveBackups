import React from "react";
import "./Navbar.css";
import QuickSaveLogo from "../images/QuickSaveColor.png"

export default function Navbar({ setCurrentPage, currentPage }) {
  return (
    <nav className="navbar">
      <img src={QuickSaveLogo} alt="QuickSave Logo" className="navbar-title" />
      <ul className="nav-links">
        <li
          className={`nav-item ${currentPage === 'overview' ? 'active' : ''}`}
          onClick={() => setCurrentPage('overview')}
        >
          Overview
        </li>
        <li
          className={`nav-item ${currentPage === 'backupmanager' ? 'active' : ''}`}
          onClick={() => setCurrentPage('backupmanager')}
        >
          Backups
        </li>
        <li
          className={`nav-item ${currentPage === 'configuration' ? 'active' : ''}`}
          onClick={() => setCurrentPage('configuration')}
        >
          Configuration
        </li>
      </ul>
    </nav>
  );
}
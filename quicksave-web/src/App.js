import React, { useState } from "react";
import { BrowserRouter as Router } from "react-router-dom";
import Navbar from "./navbar/Navbar";
import Overview from "./overview/Overview";
import BackupManager from "./backupmanager/BackupManager";
import ConfigManager from "./configuration/ConfigManager";

function App() {
  const [currentPage, setCurrentPage] = useState('overview'); // Track the current page

  const renderPage = () => {
    switch (currentPage) {
      case 'overview':
        return <Overview />;
      case 'backupmanager':
        return <BackupManager />;
      case 'configuration':
        return <ConfigManager />;
      default:
        return <Overview />;
    }
  };

  return (
    <Router>
      <div className="app-container">
        <Navbar currentPage={currentPage} setCurrentPage={setCurrentPage} />
        <div className="page-content">
          {renderPage()} {/* Render the corresponding page */}
        </div>
      </div>
    </Router>
  );
}

export default App;
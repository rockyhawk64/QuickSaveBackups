import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";

import Navbar from "./navbar/Navbar";
import Overview from "./overview/Overview";
import BackupManager from "./backupmanager/BackupManager";
import ConfigManager from "./configuration/ConfigManager";

function App() {
  return (
    <Router>
      <div className="app-container">
        <Navbar /> {/* Always visible */}
        <div className="page-content">
          <Routes>
            {/* Default route -> Overview */}
            <Route path="/" element={<Overview />} />

            {/* Backup Manager Page */}
            <Route path="/backupmanager" element={<BackupManager />} />

            {/* Configuration Manager Page */}
            <Route path="/configuration" element={<ConfigManager />} />

            {/* Catch-all: redirect to Overview */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;
import React from "react";
import "./Overview.css";

export default function Overview() {
  return (
    <div className="overview">
      <h2 className="overview-title">Overview</h2>

      <div className="card-container">
        <div className="overview-card">
          <h3>Backup Status</h3>
          <p>Last backup was 2 hours ago.</p>
        </div>
        <div className="overview-card">
          <h3>Configuration</h3>
          <p>4 active configurations.</p>
        </div>
        <div className="overview-card">
          <h3>System Logs</h3>
          <p>No critical errors reported.</p>
        </div>
      </div>
    </div>
  );
}
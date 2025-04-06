import React, { useEffect, useState } from "react";
import "./Overview.css";

export default function Overview() {
  const [serverData, setServerData] = useState({});

  useEffect(() => {
    async function fetchData() {
      try {
        const apiUrl = `${window.location.protocol}//${window.location.host}/api`; // or just "/api" for the same domain
        const response = await fetch(apiUrl);
        const data = await response.json();
        setServerData(data);
      } catch (error) {
        console.error("Error fetching server data:", error);
      }
    }

    // Fetch data immediately when the component mounts
    fetchData();

    // Set interval to fetch data every 1 second
    const intervalId = setInterval(fetchData, 1000);

    // Cleanup interval on component unmount
    return () => clearInterval(intervalId);
  }, []); // Empty dependency array so this effect only runs once on mount

  return (
    <div className="overview">
      <h2 className="overview-title">Server Overview</h2>

      <div className="overview-section">

        <div className="stat-container">
          <h3>Auto Backup Status</h3>
          <div>
            <div className={`status-indicator ${serverData.autoBackupStatus ? "active" : "inactive"}`}>
                {serverData.autoBackupStatus ? "Running" : "Not Running"}
            </div>
            
            <div className={`status-indicator ${serverData.autoBackupStatus ? (serverData.autoBackupAsync ? "async" : "normal") : "inactive-grey"}`}>
                {serverData.autoBackupStatus ? (serverData.autoBackupAsync ? "Async" : "Normal") : (serverData.autoBackupAsync ? "Async" : "Normal")}
            </div>
          </div>

        </div>

        <div className="stat-container">
          <h3>Backup Tasks</h3>
          <div className="stat-value">{serverData.backupsRunning}</div>
          <p className="stat-description">Backup Task(s) Running</p>
        </div>

        <div className="stat-container">
          <h3>Worlds Being Backed Up</h3>
          <ul className="world-list">
            {serverData.backupWorlds && serverData.backupWorlds.map((world, index) => (
              <li key={index}>{world}</li>
            ))}
          </ul>
        </div>

        <div className="stat-container">
          <h3>Server Version</h3>
          <div className="server-version">{serverData.serverVersion}</div>
        </div>
      </div>

        {/* Plugin Documentation Table */}
        <h2 className="overview-title">Plugin Overview</h2>
        <div className="plugin-doc-section">
        <table className="plugin-doc-table">
          <thead>
            <tr>
              <th>Command</th>
              <th>Permission</th>
              <th>Description</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>/qs help</td>
              <td>quicksave.help</td>
              <td>View available commands to the player.</td>
            </tr>
            <tr>
              <td>/qs version</td>
              <td>quicksave.version</td>
              <td>View the version of the plugin.</td>
            </tr>
            <tr>
              <td>/qs status</td>
              <td>quicksave.admin.status</td>
              <td>Check if the plugin is currently backing up any worlds.</td>
            </tr>
            <tr>
              <td>/qs reload</td>
              <td>quicksave.admin.reload</td>
              <td>Reloads the plugin config.</td>
            </tr>
            <tr>
              <td>/qs backup [optional world]</td>
              <td>quicksave.admin.backup</td>
              <td>Backup all the worlds on the server or a specific world manually.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
}

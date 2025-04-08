import React, { useEffect, useState, useRef } from "react";
import "./BackupManager.css";

export default function BackupManager() {
  const [backupData, setBackupData] = useState([]); 
  const [error, setError] = useState(null);  
  const [selectedWorld, setSelectedWorld] = useState(null);  
  const hasSetDefaultWorld = useRef(false); //Necessary: React’s useEffect closure has a stale reference to selectedWorld. Because selectedWorld isn't in the dependency array of the useEffect

  // Fetch all backups
  useEffect(() => {
    async function fetchData() {
      try {
        const apiUrl = `${window.location.protocol}//${window.location.host}/api/backup`;
        const response = await fetch(apiUrl);
        const data = await response.json();

        setBackupData(data.backupFiles);

        const uniqueWorlds = [...new Set(data.backupFiles.map(backup => backup.world))];

        if (uniqueWorlds.length > 0 && !hasSetDefaultWorld.current) {
          setSelectedWorld(uniqueWorlds[0]);
          hasSetDefaultWorld.current = true;
        }
      } catch (error) {
        setError("No backup files available.");
      }
    }

    fetchData();

    // Set interval to fetch data every 1 second
    const intervalId = setInterval(fetchData, 1000);

    // Cleanup interval on component unmount
    return () => clearInterval(intervalId);
  }, []);

  const worldBackups = backupData.filter(backup => backup.world === selectedWorld);

  const handleBackup = async (world = null) => {
    try {
      const apiUrl = `${window.location.protocol}//${window.location.host}/api/backup`;
      const backupUrl = world ? `${apiUrl}/${world}` : apiUrl;
      
      const response = await fetch(backupUrl, { method: "POST" });
  
      if (response.ok) {
        alert(world ? `Backup started for ${world}!` : "Full backup started!");
      } else {
        alert("Failed to start backup.");
      }
    } catch (error) {
      alert("Failed to start backup.");
    }
  };

  const convertToDate = (fileName) => {
    // Match the date and time pattern in the format dd-mmm-yyyy hh-mm-ss.zip
    const regex = /(\d{2}-\w{3}-\d{4} \d{2}-\d{2}-\d{2})\.zip/;
    const match = fileName.match(regex);
  
    if (match) {
      // Extract the date and time part without the .zip extension
      const dateStr = match[1];
  
      // Map month abbreviation to month number
      const monthMap = {
        Jan: 0, Feb: 1, Mar: 2, Apr: 3, May: 4, Jun: 5,
        Jul: 6, Aug: 7, Sep: 8, Oct: 9, Nov: 10, Dec: 11
      };
  
      // Break the date string into parts
      const [day, monthAbbr, year, hour, minute, second] = dateStr.split(/[-\s:]/);
  
      // Convert the month abbreviation to a number
      const month = monthMap[monthAbbr];
  
      if (month !== undefined) {
        // Create a new Date object with the correct format (year, month, day, hour, minute, second)
        return new Date(year, month, day, hour, minute, second);
      }
    }
  
    // Return null if the date can't be parsed
    return null;
  };

  const handleCopyToClipboard = (fileName) => {
    navigator.clipboard.writeText(fileName).then(() => {
      alert("File name copied to clipboard!");
    }).catch(err => {
      console.error("Failed to copy text: ", err);
    });
  };

  const zipBackups = worldBackups.filter(backup => backup.fileName.endsWith('.zip'));
  const sortedBackups = zipBackups
    .map(backup => ({
      ...backup,
      timestamp: convertToDate(backup.fileName)
    }))
    .sort((a, b) => b.timestamp - a.timestamp || a.fileName.localeCompare(b.fileName));  // Sort by timestamp

  return (
    <div className="backup-manager">
      <h2 className="backup-manager-title">Backup Manager</h2>

      {error ? (
        <h3>{error}</h3>
      ) : (
        <>
          <div className="world-tabs">
            <button className="backup-button" onClick={() => handleBackup()}>
                Start Full Backup
            </button>
            {backupData.length === 0 ? (
              <h3>No worlds available</h3>
            ) : (
              [...new Set(backupData.map((backup) => backup.world))].map((world, index) => (
                <button
                  key={index}
                  className={`world-tab ${selectedWorld === world ? "active" : ""}`}
                  onClick={() => setSelectedWorld(world)}
                >
                  {world}
                </button>
              ))
            )}
          </div>

          {selectedWorld && (
            <div className="world-details">
              <button className="backup-button world-backup-button" onClick={() => handleBackup(selectedWorld)}>
                Start World Backup
              </button>
              <h3>{selectedWorld.toUpperCase()} Backups</h3>

              <div className="backup-actions">

                <ul className="backup-list">
                  {sortedBackups.length === 0 ? (
                    <p>No backups available for this world</p>
                  ) : (
                    sortedBackups.map((backup, index) => (
                      <li key={index} className="backup-item">
                        <div className = "tooltip" onClick={() => handleCopyToClipboard(backup.fileName)}>
                        <span className="file-name">
                            {backup.timestamp 
                                ? backup.timestamp.toLocaleString('en-US', {
                                    weekday: 'long',
                                    year: 'numeric',
                                    month: 'long',
                                    day: 'numeric',
                                    hour: 'numeric',
                                    minute: 'numeric',
                                    second: 'numeric',
                                }) 
                                : "No Date Found"}
                          </span>
                          <span className="file-size">{formatFileSize(backup.size)}</span>
                          <span className="tooltiptext">{backup.fileName}</span>
                        </div>
                      </li>
                    ))
                  )}
                </ul>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}

// Helper function to format the file size
function formatFileSize(size) {
  if (size >= 1048576) {
    return (size / 1048576).toFixed(2) + ' MB';
  } else if (size >= 1024) {
    return (size / 1024).toFixed(2) + ' KB';
  } else {
    return size + ' bytes';
  }
}
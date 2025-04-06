import React, { useEffect, useState } from "react";
import "./ConfigManager.css";

export default function ConfigManager() {
  const [isSaving, setIsSaving] = useState(false);
  const [configData, setConfigData] = useState({});
  const [modifiedData, setModifiedData] = useState({});

  useEffect(() => {
    async function fetchData() {
      try {
        const apiUrl = `${window.location.protocol}//${window.location.host}/api/config`;
        const response = await fetch(apiUrl);
        const data = await response.json();

        // Convert all the values in the fetched data to their correct types
        const convertedData = convertData(data);

        setConfigData(convertedData);
        setModifiedData(convertedData);
      } catch (error) {
        console.error("Error fetching configuration data:", error);
      }
    }

    fetchData();
  }, []);

  const convertValue = (value) => {
    // Convert to boolean if the value is a string 'true' or 'false' (case insensitive)
    if (value === "true" || value === "false") {
      return value === "true";
    }
  
    // Convert to number if it's a valid number (check if it's numeric)
    if (!isNaN(value) && value !== "") {
      return Number(value);
    }
  
    // Return as string otherwise
    return value;
  };
  
  // Recursive function to convert all values in an object or array
  const convertData = (data) => {
    if (Array.isArray(data)) {
      return data.map(convertData); // Apply conversion to each item in the array
    } else if (typeof data === "object" && data !== null) {
      return Object.keys(data).reduce((acc, key) => {
        acc[key] = convertData(data[key]); // Recursively apply conversion
        return acc;
      }, {});
    } else {
      return convertValue(data); // Apply conversion to individual value
    }
  };

      const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
    
        let newValue;
        if (type === "checkbox") {
          newValue = checked;
        } else {
          newValue = convertValue(value);
        }
    
        setModifiedData({
          ...modifiedData,
          [name]: newValue,
        });
      };

  const handleSave = async () => {
    setIsSaving(true);
    try {
      const apiUrl = `${window.location.protocol}//${window.location.host}/api/config`;
      await fetch(apiUrl, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(modifiedData),
      });
      alert("Configuration saved successfully.");
    } catch (error) {
      console.error("Error saving configuration:", error);
      alert("Failed to save configuration.");
    }
    setIsSaving(false);
  };

  return (
    <div className="config">
      <h2 className="config-title">Configuration</h2>
      {Object.keys(modifiedData).length === 0 ? (
        <h3>No configuration found.</h3>
      ) : (
      <div>
        <div className="config-section">
            <h3>General Settings</h3>

            <div className="form-row">
            <label className="tooltip" htmlFor="backupLocation">Backup Location:
                <span className="tooltiptext">
                    'Root' will save backups in the root of the server, 'Plugin' will save backups to the plugins data folder.
                </span>
            </label>
            <select
                id="backupLocation"
                name="backupLocation"
                value={modifiedData.backupLocation}
                onChange={handleChange}
            >
                <option value="root">Root</option>
                <option value="plugin">Plugin</option>
            </select>
            </div>

            <div className="form-row">
            <label className="tooltip" htmlFor="backupLocation">Auto Backup:
                <span className="tooltiptext">
                    This sets whether the automatic backup feature is on or off.
                </span>
            </label>
            <div className="toggle-switch">
                <input
                    type="checkbox"
                    id="autoBackup"
                    name="autoBackup"
                    checked={modifiedData.autoBackup}
                    onChange={handleChange}
                />
            </div>
            </div>

            <div className="form-row">
            <label className="tooltip" htmlFor="backupLocation">Async Backup:
                <span className="tooltiptext">
                    If true, worlds are backed up by the auto backup feature individually at staggered times, so their backup times may differ.
                    If false, all worlds are backed up simultaneously based on the backup interval.
                </span>
            </label>
            <div className="toggle-switch">
                <input
                    type="checkbox"
                    id="asyncBackup"
                    name="asyncBackup"
                    checked={modifiedData.asyncBackup}
                    onChange={handleChange}
                />
            </div>
            </div>

            <div className="form-row">
            <label className="tooltip" htmlFor="backupLocation">Backup Interval (in minutes):
                <span className="tooltiptext">
                    This sets the interval at which world backups are performed by the auto backup feature. (360 Mins is 6 Hours)
                </span>
            </label>
            <input
                type="number"
                id="backupInterval"
                name="backupInterval"
                value={modifiedData.backupInterval}
                onChange={handleChange}
                min="1"
            />
            </div>

            <h3>Worlds to Backup</h3>
            <div className="form-row">
                <label className="tooltip" htmlFor="backupLocation">List of worlds to back up:
                <span className="tooltiptext">
                    These are the worlds that will be backed up. Worlds not included will not be touched by the plugin.
                </span>
            </label>
            <textarea
                name="backupWorlds"
                value={modifiedData.backupWorlds?.join("\n") || ""}
                onChange={(e) =>
                    handleChange({
                    target: { name: "config.backupWorlds", value: e.target.value.split("\n") },
                    })
                }
            />

            </div>
        </div>

        <div className="config-section">
            <h3>Format Settings</h3>

            <div className="form-row">
            <label className="tooltip" htmlFor="formatTag">Plugin Tag:
                <span className="tooltiptext">
                This is the tag that will be shown for the plugin.
                </span>
            </label>
            <input
                type="text"
                id="formatTag"
                name="tag"
                value={modifiedData.tag}
                onChange={handleChange}
            />
            </div>

            <div className="form-row">
            <label className="tooltip" htmlFor="formatPerms">No Permission Message:
                <span className="tooltiptext">
                This message is displayed when a player doesn't have the necessary permissions.
                </span>
            </label>
            <input
                type="text"
                id="formatPerms"
                name="perms"
                value={modifiedData.perms}
                onChange={handleChange}
            />
            </div>

            <div className="form-row">
            <label className="tooltip" htmlFor="formatNoWorld">World Not Found Message:
                <span className="tooltiptext">
                This message is displayed when a world isn't found in the configuration.
                </span>
            </label>
            <input
                type="text"
                id="formatNoWorld"
                name="noWorld"
                value={modifiedData.noWorld}
                onChange={handleChange}
            />
            </div>

            <div className="form-row">
            <label className="tooltip" htmlFor="formatAlreadyBackup">Backup Already Running Message:
                <span className="tooltiptext">
                Message sent when a backup for the requested world is already running.
                </span>
            </label>
            <input
                type="text"
                id="formatAlreadyBackup"
                name="alreadyBackup"
                value={modifiedData.alreadyBackup}
                onChange={handleChange}
            />
            </div>

            <div className="form-row">
            <label className="tooltip" htmlFor="formatFailedBackup">Backup Failed Message:
                <span className="tooltiptext">
                Message sent in the console when a backup fails.
                </span>
            </label>
            <input
                type="text"
                id="formatFailedBackup"
                name="failedBackup"
                value={modifiedData.failedBackup}
                onChange={handleChange}
            />
            </div>

            <div className="form-row">
            <label className="tooltip" htmlFor="formatFinishedBackup">Backup Finished Message:
                <span className="tooltiptext">
                Message sent in the console when a backup is complete.
                </span>
            </label>
            <input
                type="text"
                id="formatFinishedBackup"
                name="finishedBackup"
                value={modifiedData.finishedBackup}
                onChange={handleChange}
            />
            </div>

            <div className="form-row">
            <label className="tooltip" htmlFor="formatReload">Reload Message:
                <span className="tooltiptext">
                This is the message that will be shown when the plugin is reloaded.
                </span>
            </label>
            <input
                type="text"
                id="formatReload"
                name="reload"
                value={modifiedData.reload}
                onChange={handleChange}
            />
            </div>

            <div className="form-row">
            <label className="tooltip" htmlFor="formatSaving">Backup Started Message:
                <span className="tooltiptext">
                Message sent when a backup is started.
                </span>
            </label>
            <input
                type="text"
                id="formatSaving"
                name="saving"
                value={modifiedData.saving}
                onChange={handleChange}
            />
            </div>

            <div className="form-row">
            <label className="tooltip" htmlFor="formatNoStatus">No Status Message:
                <span className="tooltiptext">
                Message sent when running the status command and no worlds are being backed up.
                </span>
            </label>
            <input
                type="text"
                id="formatNoStatus"
                name="noStatus"
                value={modifiedData.noStatus}
                onChange={handleChange}
            />
            </div>

            <div className="form-row">
            <label className="tooltip" htmlFor="formatStatus">Status Message:
                <span className="tooltiptext">
                Message sent when running the status command and world/s are being backed up.
                </span>
            </label>
            <input
                type="text"
                id="formatStatus"
                name="status"
                value={modifiedData.status}
                onChange={handleChange}
            />
            </div>
        </div>

        <div className="config-section">
            <h3>Folder Size Limit</h3>

            <div className="form-row">
            <label className="tooltip" htmlFor="folderSizeMaxEnabled">Enable Maximum Folder Size:
                <span className="tooltiptext">
                    If false there will be no maximum folder size.
                </span>
            </label>
            <div className="toggle-switch">
                <input
                    type="checkbox"
                    id="folderSizeMaxEnabled"
                    name="sizeLimit"
                    checked={modifiedData.sizeLimit}
                    onChange={handleChange}
                />
            </div>
            </div>

            <div className="form-row">
            <label className="tooltip" htmlFor="folderSizeMaxValue">Maximum Folder Size (in MB):
                <span className="tooltiptext">
                    The maximum folder size a world can be backed up in megabytes: This value is per world, not total.
                </span>
            </label>
            <input
                type="number"
                id="folderSizeMaxValue"
                name="sizeLimitMax"
                value={modifiedData.sizeLimitMax}
                onChange={handleChange}
                min="1"
                disabled={!modifiedData.sizeLimit}
            />
            </div>
        </div>

        <div className="config-section">
            <h3>Backup Amount</h3>

            <div className="form-row">
            <label className="tooltip" htmlFor="backupAmountMaxEnabled">Enable Maximum Backup Amount:
                <span className="tooltiptext">
                    If disabled the plugin will not have a maximum backup amount.
                </span>
            </label>
            <div className="toggle-switch">
                <input
                    type="checkbox"
                    id="backupAmountMaxEnabled"
                    name="amountLimit"
                    checked={modifiedData.amountLimit}
                    onChange={handleChange}
                />
            </div>
            </div>

            <div className="form-row">
            <label className="tooltip" htmlFor="backupAmountMaxValue">Maximum Number of Backups:
                <span className="tooltiptext">
                    This sets the maximum number of backups a world can have: This value is per world, not total.
                </span>
            </label>
            <input
                type="number"
                id="backupAmountMaxValue"
                name="amountLimitMax"
                value={modifiedData.amountLimitMax}
                onChange={handleChange}
                min="1"
                disabled={!modifiedData.amountLimit}
            />
            </div>
        </div>

        <button
            className="save-button"
            onClick={handleSave}
            disabled={isSaving}
        >
            {isSaving ? "Saving..." : "Save and Reload Plugin"}
        </button>
      </div>
    )}
    </div>
  );
}
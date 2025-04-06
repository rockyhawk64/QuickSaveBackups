import React, { useEffect, useState } from "react";
import "./ConfigManager.css";

export default function ConfigManager() {
  const [isSaving, setIsSaving] = useState(false);
  const [configData, setConfigData] = useState({});
  const [modifiedData, setModifiedData] = useState({
    version: "1.3",
    backupWorlds: ["world", "world_nether", "World3"],  // Default value for backupWorlds
    backupLocation: "plugin",
    autoBackup: true,
    asyncBackup: false,
    backupInterval: 360,
    format: {
      tag: "&6[&7QuickSave&6]",
      perms: "&cNo permission.",
      noWorld: "&cWorld not Found in Config.",
      alreadyBackup: "&cAlready backing up:",
      failedBackup: "&cFailed to back up:",
      finishedBackup: "&aFinished backing up:",
      reload: "&aReloaded.",
      saving: "&aStarting new backup.",
      noStatus: "&aCurrently not backing up any worlds.",
      status: "&aCurrently backing up:",
    },
    folder_size: {
      maximum_enabled: false,
      maximum_value: 3000,
    },
    amount: {
      maximum_enabled: true,
      maximum_value: 10,
    },
  });

  useEffect(() => {
    async function fetchData() {
      setConfigData(modifiedData); // for testing purposes

      try {
        const apiUrl = `${window.location.protocol}//${window.location.host}/api/config`;
        const response = await fetch(apiUrl);
        const data = await response.json();
        setConfigData(data);
        setModifiedData(data);
      } catch (error) {
        console.error("Error fetching configuration data:", error);
      }
    }

    fetchData();
  }, []);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    const nameParts = name.split('.'); // Split name into parts to handle nested values
  
    if (nameParts.length === 1) {
      // Non-nested property
      setModifiedData({
        ...modifiedData,
        [name]: type === "checkbox" ? checked : value,
      });
    } else if (nameParts.length === 2) {
      // Nested property (like "format.tag")
      setModifiedData({
        ...modifiedData,
        [nameParts[0]]: {
          ...modifiedData[nameParts[0]],
          [nameParts[1]]: type === "checkbox" ? checked : value,
        },
      });
    } else if (nameParts.length === 3) {
      // Nested property (like "folder_size.maximum_enabled")
      setModifiedData({
        ...modifiedData,
        [nameParts[0]]: {
          ...modifiedData[nameParts[0]],
          [nameParts[1]]: {
            ...modifiedData[nameParts[0]][nameParts[1]],
            [nameParts[2]]: type === "checkbox" ? checked : value,
          },
        },
      });
    }
  };

  const handleSave = async () => {
    setIsSaving(true);
    try {
      const apiUrl = `${window.location.protocol}//${window.location.host}/api/config/save`;
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
            value={modifiedData.backupWorlds.join("\n")}
            onChange={(e) =>
              handleChange({
                target: { name: "backupWorlds", value: e.target.value.split("\n") },
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
            name="format.tag"
            value={modifiedData.format.tag}
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
            name="format.perms"
            value={modifiedData.format.perms}
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
            name="format.noWorld"
            value={modifiedData.format.noWorld}
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
            name="format.alreadyBackup"
            value={modifiedData.format.alreadyBackup}
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
            name="format.failedBackup"
            value={modifiedData.format.failedBackup}
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
            name="format.finishedBackup"
            value={modifiedData.format.finishedBackup}
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
            name="format.reload"
            value={modifiedData.format.reload}
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
            name="format.saving"
            value={modifiedData.format.saving}
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
            name="format.noStatus"
            value={modifiedData.format.noStatus}
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
            name="format.status"
            value={modifiedData.format.status}
            onChange={handleChange}
        />
        </div>
      </div>

      <div className="config-section">
        <h3>Folder Size Limit</h3>

        <div className="form-row">
          <label className="tooltip" htmlFor="backupLocation">Enable Maximum Folder Size:
            <span className="tooltiptext">
                If false there will be no maximum folder size.
            </span>
          </label>
          <div className="toggle-switch">
            <input
                type="checkbox"
                id="folderSizeMaxEnabled"
                name="folder_size.maximum_enabled"
                checked={modifiedData.folder_size.maximum_enabled}
                onChange={handleChange}
            />
          </div>
        </div>

        <div className="form-row">
          <label className="tooltip" htmlFor="backupLocation">Maximum Folder Size (in MB):
            <span className="tooltiptext">
                The maximum folder size a world can be backed up in megabytes: This value is per world, not total.
            </span>
          </label>
          <input
            type="number"
            id="folderSizeMaxValue"
            name="folder_size.maximum_value"
            value={modifiedData.folder_size.maximum_value}
            onChange={handleChange}
            min="1"
            disabled={!modifiedData.folder_size.maximum_enabled}
          />
        </div>
      </div>

      <div className="config-section">
        <h3>Backup Amount</h3>

        <div className="form-row">
          <label className="tooltip" htmlFor="backupLocation">Enable Maximum Backup Amount:
            <span className="tooltiptext">
                If disabled the plugin will not have a maximum backup amount.
            </span>
          </label>
          <div className="toggle-switch">
            <input
                type="checkbox"
                id="backupAmountMaxEnabled"
                name="amount.maximum_enabled"
                checked={modifiedData.amount.maximum_enabled}
                onChange={handleChange}
            />
          </div>
        </div>

        <div className="form-row">
          <label className="tooltip" htmlFor="backupLocation">Maximum Number of Backups:
            <span className="tooltiptext">
                This sets the maximum number of backups a world can have: This value is per world, not total.
            </span>
          </label>
          <input
            type="number"
            id="backupAmountMaxValue"
            name="amount.maximum_value"
            value={modifiedData.amount.maximum_value}
            onChange={handleChange}
            min="1"
            disabled={!modifiedData.amount.maximum_enabled}
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
  );
}
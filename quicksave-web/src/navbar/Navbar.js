import React from "react";
import { NavLink } from "react-router-dom";
import "./Navbar.css";
import QuickSaveLogo from "../images/QuickSaveColor.png"

export default function Navbar() {
  return (
    <nav className="navbar">
      <img src={QuickSaveLogo} alt="QuickSave Logo" className="navbar-title" />
      <ul className="nav-links">
        <NavLink to="/" end className={({ isActive }) => isActive ? "nav-item active" : "nav-item"}>
          Overview
        </NavLink>
        <NavLink to="/backupmanager" className={({ isActive }) => isActive ? "nav-item active" : "nav-item"}>
          Backups
        </NavLink>
        <NavLink to="/configuration" className={({ isActive }) => isActive ? "nav-item active" : "nav-item"}>
          Configuration
        </NavLink>
      </ul>
    </nav>
  );
}
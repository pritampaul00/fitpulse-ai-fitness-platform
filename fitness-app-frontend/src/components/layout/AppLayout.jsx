import { Outlet, useLocation } from "react-router";
import { Box, Button } from "@mui/material";
import { useContext } from "react";
import { useDispatch } from "react-redux";
import { AuthContext } from "react-oauth2-code-pkce";

import Sidebar from "./Sidebar";
import { logout } from "../../store/authSlice";

const pageTitles = {
    "/dashboard": "Overview",
    "/activities": "Your activities",
    "/goals": "Fitness Goals",
    "/workouts": "Workout Planner",
    "/coach": "AI Fitness Coach",
};

const AppLayout = () => {
    const { logOut } = useContext(AuthContext);
    const dispatch = useDispatch();
    const location = useLocation();

    const isActivityDetail =
        location.pathname.startsWith("/activities/");

    const title = isActivityDetail
        ? "Activity details"
        : pageTitles[location.pathname] || "FitPulse";

    const handleLogout = () => {
        dispatch(logout());
        logOut();
    };

    return (
        <Box className="app-shell">
            <Sidebar />

            <Box className="app-content">
                <header className="app-header">
                    <div>
                        <span className="header-eyebrow">
                            FITPULSE / WORKSPACE
                        </span>
                        <h1>{title}</h1>
                    </div>

                    <Button
                        variant="outlined"
                        onClick={handleLogout}
                        className="logout-button"
                    >
                        Logout
                    </Button>
                </header>

                <main className="app-main">
                    <Outlet />
                </main>
            </Box>
        </Box>
    );
};

export default AppLayout;
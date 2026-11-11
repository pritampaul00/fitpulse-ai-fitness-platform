// import { useContext, useEffect, useState } from "react";

// import { Box, Button, CircularProgress, Typography } from "@mui/material";

// import { AuthContext } from "react-oauth2-code-pkce";
// import { useDispatch } from "react-redux";

// import { BrowserRouter, Navigate, Route, Routes } from "react-router";

// import { setCredentials, logout } from "./store/authSlice";

// import AppLayout from "./components/layout/AppLayout";
// import ActivityForm from "./components/ActivityForm";
// import ActivityList from "./components/ActivityList";
// import ActivityDetail from "./components/ActivityDetail";
// import ChatCoach from "./components/coach/ChatCoach";
// import Dashboard from "./pages/Dashboard";

// import Goals from "./pages/Goals";
// import WorkoutPlanner from "./pages/WorkoutPlanner";

// import "./App.css";

// const ActivitiesPage = () => {
//   return (
//     <Box className="activities-page">
//       <Box className="activities-intro">
//         <Typography variant="h5">Your activities</Typography>

//         <Typography color="text.secondary">
//           Record and review your fitness sessions.
//         </Typography>
//       </Box>

//       <ActivityForm onActivityAdded={() => window.location.reload()} />

//       <ActivityList />
//     </Box>
//   );
// };

// const LoginPage = ({ onLogin }) => {
//   return (
//     <Box className="login-page">
//       <Box className="login-card">
//         <div className="brand-mark login-mark">F</div>

//         <Typography variant="h3">FitPulse</Typography>

//         <Typography className="login-subtitle">
//           Your fitness journey, powered by AI.
//         </Typography>

//         <Typography className="login-description">
//           Track your activities, understand your progress and train smarter with
//           your personal AI coach.
//         </Typography>

//         <Button
//           fullWidth
//           variant="contained"
//           size="large"
//           onClick={() => onLogin()}
//         >
//           Continue with Keycloak
//         </Button>

//         <Typography className="login-footer">
//           PERSONAL FITNESS. INTELLIGENT GUIDANCE.
//         </Typography>
//       </Box>
//     </Box>
//   );
// };

// const AuthenticatedRoutes = () => (
//   <Routes>
//     <Route element={<AppLayout />}>
//       <Route path="/" element={<Navigate to="/dashboard" replace />} />

//       <Route path="/dashboard" element={<Dashboard />} />

//       <Route path="/activities" element={<ActivitiesPage />} />

//       <Route path="/activities/:id" element={<ActivityDetail />} />

//       <Route path="/goals" element={<Goals />} />

//       <Route path="/workouts" element={<WorkoutPlanner />} />

//       <Route path="/coach" element={<ChatCoach />} />

//       <Route path="*" element={<Navigate to="/dashboard" replace />} />
//     </Route>
//   </Routes>
// );

// function App() {
//   const { token, tokenData, logIn, logOut } = useContext(AuthContext);

//   const dispatch = useDispatch();

//   const [authReady, setAuthReady] = useState(false);

//   useEffect(() => {
//     if (token && tokenData) {
//       dispatch(
//         setCredentials({
//           token,
//           user: tokenData,
//         }),
//       );

//       setAuthReady(true);
//     } else {
//       setAuthReady(false);
//       dispatch(logout());
//     }
//   }, [token, tokenData, dispatch]);

//   const handleLogout = () => {
//     dispatch(logout());
//     logOut();
//   };

//   return (
//     <BrowserRouter>
//       {!authReady ? (
//         token ? (
//           <Box className="auth-loading">
//             <CircularProgress />
//             <Typography>Preparing your workspace...</Typography>
//           </Box>
//         ) : (
//           <LoginPage onLogin={() => logIn()} />
//         )
//       ) : (
//         <AuthenticatedRoutes onLogout={handleLogout} />
//       )}
//     </BrowserRouter>
//   );
// }

// export default App;


import { useContext, useEffect, useState } from "react";
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Paper,
  Typography,
} from "@mui/material";
import { AuthContext } from "react-oauth2-code-pkce";
import { useDispatch } from "react-redux";
import { BrowserRouter, Navigate, Route, Routes } from "react-router";

import { setCredentials, logout } from "./store/authSlice";
import AppLayout from "./components/layout/AppLayout";
import ActivityForm from "./components/ActivityForm";
import ActivityList from "./components/ActivityList";
import ActivityDetail from "./components/ActivityDetail";
import ChatCoach from "./components/coach/ChatCoach";
import Dashboard from "./pages/Dashboard";
import Goals from "./pages/Goals";
import WorkoutPlanner from "./pages/WorkoutPlanner";
import { getActivities } from "./services/api";
import "./App.css";

const formatNumber = (value) => new Intl.NumberFormat().format(value || 0);

const ActivitiesPage = () => {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [refreshing, setRefreshing] = useState(false);

  const loadActivities = async ({ showLoader = true } = {}) => {
    if (showLoader) setLoading(true);
    setLoadError("");

    try {
      const response = await getActivities();
      const data = Array.isArray(response.data) ? response.data : [];
      setActivities(data);
    } catch (error) {
      console.error("Unable to load activities:", error);
      setLoadError(
        error.response?.data?.message ||
          "We couldn't load your activities. Please try again.",
      );
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    loadActivities();
  }, []);

  const handleActivityAdded = async () => {
    setRefreshing(true);
    await loadActivities({ showLoader: false });
  };

  const totalDuration = activities.reduce(
    (total, item) => total + (Number(item.duration) || 0),
    0,
  );
  const totalCalories = activities.reduce(
    (total, item) => total + (Number(item.caloriesBurned) || 0),
    0,
  );
  const currentMonthCount = activities.filter((item) => {
    if (!item.startTime) return false;
    const date = new Date(item.startTime);
    const now = new Date();
    return (
      date.getFullYear() === now.getFullYear() &&
      date.getMonth() === now.getMonth()
    );
  }).length;

  const stats = [
    { label: "Total activities", value: formatNumber(activities.length), icon: "◉", tone: "violet" },
    { label: "Calories burned", value: formatNumber(totalCalories), suffix: "kcal", icon: "♨", tone: "orange" },
    { label: "Time trained", value: formatNumber(totalDuration), suffix: "min", icon: "◷", tone: "blue" },
    { label: "This month", value: formatNumber(currentMonthCount), icon: "▦", tone: "green" },
  ];

  return (
    <Box className="activities-page">
      <Box className="activities-heading">
        <Box>
          <Typography className="activities-eyebrow">YOUR FITNESS JOURNAL</Typography>
          <Typography variant="h4" className="activities-title">
            Activities
          </Typography>
          <Typography className="activities-subtitle">
            Track your sessions, review your effort, and build consistency.
          </Typography>
        </Box>
        <Button
          variant="outlined"
          className="activities-refresh"
          onClick={() => {
            setRefreshing(true);
            loadActivities({ showLoader: false });
          }}
          disabled={loading || refreshing}
        >
          {refreshing ? <CircularProgress size={17} /> : "↻"}
          <span>Refresh</span>
        </Button>
      </Box>

      {loadError && (
        <Alert
          severity="error"
          action={
            <Button color="inherit" size="small" onClick={() => loadActivities()}>
              Retry
            </Button>
          }
        >
          {loadError}
        </Alert>
      )}

      <Box className="activity-stats-grid">
        {stats.map((stat) => (
          <Paper key={stat.label} elevation={0} className="activity-stat-card">
            <Box className={`activity-stat-icon ${stat.tone}`}>{stat.icon}</Box>
            <Box className="activity-stat-copy">
              <Typography className="activity-stat-label">{stat.label}</Typography>
              <Box className="activity-stat-value-row">
                <Typography className="activity-stat-value">{stat.value}</Typography>
                {stat.suffix && <Typography className="activity-stat-suffix">{stat.suffix}</Typography>}
              </Box>
            </Box>
          </Paper>
        ))}
      </Box>

      <Box className="activities-content-grid">
        <Box className="activity-form-column">
          <ActivityForm onActivityAdded={handleActivityAdded} />
        </Box>
        <Box className="activity-history-column">
          <ActivityList
            activities={activities}
            loading={loading}
            onRefresh={() => loadActivities({ showLoader: false })}
          />
        </Box>
      </Box>
    </Box>
  );
};

const LoginPage = ({ onLogin }) => (
  <Box className="login-page">
    <Box className="login-card">
      <div className="brand-mark login-mark">F</div>
      <Typography variant="h3">FitPulse</Typography>
      <Typography className="login-subtitle">
        Your fitness journey, powered by AI.
      </Typography>
      <Typography className="login-description">
        Track your activities, understand your progress and train smarter with
        your personal AI coach.
      </Typography>
      <Button
        fullWidth
        variant="contained"
        size="large"
        onClick={() => onLogin()}
      >
        Continue with Keycloak
      </Button>
      <Typography className="login-footer">
        PERSONAL FITNESS. INTELLIGENT GUIDANCE.
      </Typography>
    </Box>
  </Box>
);

const AuthenticatedRoutes = () => (
  <Routes>
    <Route element={<AppLayout />}>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/dashboard" element={<Dashboard />} />
      <Route path="/activities" element={<ActivitiesPage />} />
      <Route path="/activities/:id" element={<ActivityDetail />} />
      <Route path="/goals" element={<Goals />} />
      <Route path="/workouts" element={<WorkoutPlanner />} />
      <Route path="/coach" element={<ChatCoach />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Route>
  </Routes>
);

function App() {
  const { token, tokenData, logIn, logOut } = useContext(AuthContext);
  const dispatch = useDispatch();
  const [authReady, setAuthReady] = useState(false);

  useEffect(() => {
    if (token && tokenData) {
      dispatch(setCredentials({ token, user: tokenData }));
      setAuthReady(true);
    } else {
      setAuthReady(false);
      dispatch(logout());
    }
  }, [token, tokenData, dispatch]);

  const handleLogout = () => {
    dispatch(logout());
    logOut();
  };

  return (
    <BrowserRouter>
      {!authReady ? (
        token ? (
          <Box className="auth-loading">
            <CircularProgress />
            <Typography>Preparing your workspace...</Typography>
          </Box>
        ) : (
          <LoginPage onLogin={() => logIn()} />
        )
      ) : (
        <AuthenticatedRoutes onLogout={handleLogout} />
      )}
    </BrowserRouter>
  );
}

export default App;

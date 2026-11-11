import { Box, Button, Paper, Typography } from "@mui/material";
import { Link } from "react-router";

const Dashboard = () => {
  return (
    <Box className="dashboard-page">
      <Paper className="dashboard-hero" elevation={0}>
        <div className="hero-content">
          <span className="hero-tag">✧ YOUR PERSONAL FITNESS SPACE</span>

          <Typography variant="h3">
            Build better habits.
            <br />
            <span>Get stronger every day.</span>
          </Typography>

          <Typography className="hero-description">
            Track your activities, understand your performance and get
            personalized guidance from your AI coach.
          </Typography>

          <Box className="hero-actions">
            <Button component={Link} to="/coach" variant="contained">
              Talk to AI Coach
            </Button>

            <Button component={Link} to="/activities" variant="outlined">
              View Activities
            </Button>
          </Box>
        </div>

        <div className="hero-decoration">
          <div className="hero-orbit orbit-one" />
          <div className="hero-orbit orbit-two" />
          <div className="hero-center">F</div>
        </div>
      </Paper>

      <Typography className="section-heading">YOUR WORKSPACE</Typography>

      <div className="dashboard-cards">
        <Paper className="dashboard-card" elevation={0}>
          <span className="dashboard-card-icon">◷</span>
          <Typography variant="h6">Activity Tracking</Typography>
          <Typography>
            Record workouts and review your activity history.
          </Typography>
          <Button component={Link} to="/activities">
            Explore activities →
          </Button>
        </Paper>

        <Paper className="dashboard-card featured" elevation={0}>
          <span className="dashboard-card-icon">✧</span>
          <Typography variant="h6">AI Fitness Coach</Typography>
          <Typography>
            Get fitness guidance informed by your activity history.
          </Typography>
          <Button component={Link} to="/coach">
            Open AI Coach →
          </Button>
        </Paper>
      </div>

      <Paper className="dashboard-card" elevation={0}>
        <span className="dashboard-card-icon">▦</span>
        <Typography variant="h6">Workout Planner</Typography>
        <Typography>
          Schedule your workouts and organize your weekly training routine.
        </Typography>
        <Button component={Link} to="/workouts">
          Open Workout Planner →
        </Button>
      </Paper>

      <Paper className="dashboard-note" elevation={0}>
        <Typography variant="h6">Your next step starts here.</Typography>
        <Typography>
          Log your first activity or ask your AI coach for a personalized
          fitness plan.
        </Typography>
      </Paper>
    </Box>
  );
};

export default Dashboard;

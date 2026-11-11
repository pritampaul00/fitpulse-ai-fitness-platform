// import { Card, CardContent, Grid2, Typography } from '@mui/material'
// import React, { useEffect, useState } from 'react'
// import { useNavigate } from 'react-router';
// import { getActivities } from '../services/api';

// const ActivityList = () => {
//   const [activities, setActivities] = useState([]);
//   const navigate = useNavigate();

//   const fetchActivities = async () => {
//     try {
//       const response = await getActivities();
//       setActivities(response.data);
//     } catch (error) {
//       console.error(error);
//     }
//   };

//   useEffect(() => {
//     fetchActivities();
//   }, []);
//   return (
//     <Grid2 container spacing={2}>
//       {activities.map((activity) => (
//         <Grid2 container spacing={{ xs: 2, md: 3 }} columns={{ xs: 4, sm: 8, md: 12 }}>
//             <Card sx={{cursor: 'pointer'}}
//             onClick= {() => navigate(`/activities/${activity.id}`)}>
//                 <CardContent>
//                   <Typography variant='h6'>{activity.type}</Typography>
//                   <Typography>Duration: {activity.duration}</Typography>
//                   <Typography>Calories: {activity.caloriesBurned}</Typography>
//                 </CardContent>
//             </Card>
//         </Grid2>
//       ))}
//   </Grid2>
//   )
// }

// export default ActivityList

import React, { useMemo, useState } from "react";
import {
  Box,
  Chip,
  CircularProgress,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  TextField,
  Typography,
} from "@mui/material";
import { useNavigate } from "react-router";

const FILTERS = [
  ["ALL", "All activities"],
  ["RUNNING", "Running"],
  ["WALKING", "Walking"],
  ["CYCLING", "Cycling"],
  ["SWIMMING", "Swimming"],
  ["WEIGHT_TRAINING", "Weight training"],
  ["YOGA", "Yoga"],
  ["HIIT", "HIIT"],
  ["CARDIO", "Cardio"],
  ["STRETCHING", "Stretching"],
  ["OTHER", "Other"],
];

const ACTIVITY_ICONS = {
  RUNNING: "🏃",
  WALKING: "🚶",
  CYCLING: "🚴",
  SWIMMING: "🏊",
  WEIGHT_TRAINING: "🏋️",
  YOGA: "🧘",
  HIIT: "⚡",
  CARDIO: "❤️",
  STRETCHING: "🤸",
  OTHER: "💪",
};

const formatActivityName = (type = "Activity") =>
  type
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());

const formatDate = (value) => {
  if (!value) return "Date not available";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "Date not available";
  return date.toLocaleString([], {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });
};

const ActivityList = ({ activities = [], loading = false }) => {
  const navigate = useNavigate();
  const [search, setSearch] = useState("");
  const [typeFilter, setTypeFilter] = useState("ALL");

  const filteredActivities = useMemo(() => {
    const query = search.trim().toLowerCase();
    return activities
      .filter(
        (activity) => typeFilter === "ALL" || activity.type === typeFilter,
      )
      .filter((activity) => {
        if (!query) return true;
        const name = formatActivityName(activity.type).toLowerCase();
        const notes = String(
          activity.additionalMetrics?.notes || "",
        ).toLowerCase();
        return name.includes(query) || notes.includes(query);
      })
      .sort((a, b) => {
        const aDate = new Date(a.startTime || a.createdAt || 0).getTime();
        const bDate = new Date(b.startTime || b.createdAt || 0).getTime();
        return bDate - aDate;
      });
  }, [activities, search, typeFilter]);

  return (
    <Paper component="section" elevation={0} className="activity-history-card">
      <Box className="activity-history-header">
        <Box>
          <Typography className="activity-card-eyebrow">
            YOUR JOURNAL
          </Typography>
          <Typography variant="h6" className="activity-card-title">
            Recent activities
          </Typography>
          <Typography className="activity-card-description">
            {activities.length}{" "}
            {activities.length === 1 ? "session" : "sessions"} recorded
          </Typography>
        </Box>
        <Chip
          label={`${filteredActivities.length} shown`}
          className="activity-count-chip"
        />
      </Box>

      <Box className="activity-filter-row">
        <TextField
          fullWidth
          size="small"
          placeholder="Search activities or notes..."
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          className="activity-search"
        />
        <FormControl size="small" className="activity-filter-select">
          <InputLabel id="activity-filter-label">Type</InputLabel>
          <Select
            labelId="activity-filter-label"
            label="Type"
            value={typeFilter}
            onChange={(event) => setTypeFilter(event.target.value)}
          >
            {FILTERS.map(([value, label]) => (
              <MenuItem key={value} value={value}>
                {label}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      {loading ? (
        <Box className="activity-list-state">
          <CircularProgress size={28} />
          <Typography>Loading your activities...</Typography>
        </Box>
      ) : filteredActivities.length === 0 ? (
        <Box className="activity-list-state">
          <Box className="activity-empty-icon">◷</Box>
          <Typography className="activity-empty-title">
            {activities.length === 0
              ? "No activities yet"
              : "No matching activities"}
          </Typography>
          <Typography className="activity-empty-description">
            {activities.length === 0
              ? "Save your first session using the form. Your activity history will appear here."
              : "Try another search term or activity type."}
          </Typography>
        </Box>
      ) : (
        <Box className="activity-list-items">
          {filteredActivities.map((activity) => {
            const metrics = activity.additionalMetrics || {};
            return (
              <Box
                component="button"
                type="button"
                key={activity.id || `${activity.type}-${activity.startTime}`}
                className="activity-list-item"
                onClick={() => navigate(`/activities/${activity.id}`)}
              >
                <Box className="activity-list-icon">
                  {ACTIVITY_ICONS[activity.type] || ACTIVITY_ICONS.OTHER}
                </Box>
                <Box className="activity-list-main">
                  <Typography className="activity-list-name">
                    {formatActivityName(activity.type)}
                  </Typography>
                  <Typography className="activity-list-date">
                    {formatDate(activity.startTime || activity.createdAt)}
                  </Typography>
                  <Box className="activity-list-metrics">
                    <span>{Number(activity.duration) || 0} min</span>
                    <span>{Number(activity.caloriesBurned) || 0} kcal</span>
                    {metrics.distanceKm != null && (
                      <span>{metrics.distanceKm} km</span>
                    )}
                    {metrics.averageHeartRate != null && (
                      <span>{metrics.averageHeartRate} bpm</span>
                    )}
                  </Box>
                </Box>
                <Typography className="activity-list-arrow">›</Typography>
              </Box>
            );
          })}
        </Box>
      )}
    </Paper>
  );
};

export default ActivityList;

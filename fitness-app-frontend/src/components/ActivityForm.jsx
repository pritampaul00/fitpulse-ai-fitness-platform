import React, { useMemo, useState } from "react";
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  TextField,
  Typography,
} from "@mui/material";
import { addActivity } from "../services/api";

const ACTIVITY_TYPES = [
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

const toLocalDateTimeValue = (date) => {
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 16);
};

const createInitialActivity = () => ({
  type: "RUNNING",
  duration: "",
  caloriesBurned: "",
  startTime: toLocalDateTimeValue(new Date()),
  distanceKm: "",
  averageHeartRate: "",
  notes: "",
});

const ActivityForm = ({ onActivityAdded }) => {
  const [activity, setActivity] = useState(createInitialActivity);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const isDistanceActivity = useMemo(
    () => ["RUNNING", "WALKING", "CYCLING", "SWIMMING"].includes(activity.type),
    [activity.type],
  );

  const updateField = (field) => (event) => {
    setActivity((current) => ({ ...current, [field]: event.target.value }));
    setError("");
    setSuccess("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setSuccess("");

    const duration = Number(activity.duration);
    const calories = activity.caloriesBurned === "" ? null : Number(activity.caloriesBurned);
    const distance = activity.distanceKm === "" ? null : Number(activity.distanceKm);
    const heartRate = activity.averageHeartRate === "" ? null : Number(activity.averageHeartRate);

    if (!Number.isInteger(duration) || duration <= 0) {
      setError("Enter a valid duration greater than zero.");
      return;
    }
    if (calories !== null && (!Number.isFinite(calories) || calories < 0)) {
      setError("Calories must be zero or greater.");
      return;
    }
    if (distance !== null && (!Number.isFinite(distance) || distance <= 0)) {
      setError("Distance must be greater than zero.");
      return;
    }
    if (heartRate !== null && (!Number.isInteger(heartRate) || heartRate < 30 || heartRate > 250)) {
      setError("Enter an average heart rate between 30 and 250 bpm.");
      return;
    }
    if (!activity.startTime) {
      setError("Choose when this activity started.");
      return;
    }

    const additionalMetrics = {};
    if (distance !== null) additionalMetrics.distanceKm = distance;
    if (heartRate !== null) additionalMetrics.averageHeartRate = heartRate;
    if (activity.notes.trim()) additionalMetrics.notes = activity.notes.trim();

    const payload = {
      type: activity.type,
      duration,
      caloriesBurned: calories,
      startTime: activity.startTime,
      additionalMetrics,
    };

    setSubmitting(true);
    try {
      await addActivity(payload);
      setActivity(createInitialActivity());
      setSuccess("Activity saved successfully.");
      await onActivityAdded?.();
    } catch (submitError) {
      console.error("Unable to save activity:", submitError);
      setError(
        submitError.response?.data?.message ||
          "We couldn't save this activity. Please check your connection and try again.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Paper component="section" elevation={0} className="activity-form-card">
      <Box className="activity-section-heading">
        <Box>
          <Typography className="activity-card-eyebrow">NEW SESSION</Typography>
          <Typography variant="h6" className="activity-card-title">
            Log an activity
          </Typography>
          <Typography className="activity-card-description">
            Add a workout or movement session to your journal.
          </Typography>
        </Box>
        <Box className="activity-form-mark">＋</Box>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

      <Box component="form" onSubmit={handleSubmit} className="activity-form">
        <FormControl fullWidth>
          <InputLabel id="activity-type-label">Activity type</InputLabel>
          <Select
            labelId="activity-type-label"
            label="Activity type"
            value={activity.type}
            onChange={updateField("type")}
          >
            {ACTIVITY_TYPES.map(([value, label]) => (
              <MenuItem key={value} value={value}>{label}</MenuItem>
            ))}
          </Select>
        </FormControl>

        <Box className="activity-form-two-columns">
          <TextField
            fullWidth
            required
            label="Duration"
            type="number"
            value={activity.duration}
            onChange={updateField("duration")}
            inputProps={{ min: 1, step: 1 }}
            InputProps={{ endAdornment: <Typography className="field-suffix">min</Typography> }}
          />
          <TextField
            fullWidth
            label="Calories burned"
            type="number"
            value={activity.caloriesBurned}
            onChange={updateField("caloriesBurned")}
            inputProps={{ min: 0, step: 1 }}
            InputProps={{ endAdornment: <Typography className="field-suffix">kcal</Typography> }}
          />
        </Box>

        <TextField
          fullWidth
          required
          label="Start date and time"
          type="datetime-local"
          value={activity.startTime}
          onChange={updateField("startTime")}
          InputLabelProps={{ shrink: true }}
        />

        {isDistanceActivity && (
          <TextField
            fullWidth
            label="Distance"
            type="number"
            value={activity.distanceKm}
            onChange={updateField("distanceKm")}
            inputProps={{ min: 0, step: 0.01 }}
            InputProps={{ endAdornment: <Typography className="field-suffix">km</Typography> }}
          />
        )}

        <TextField
          fullWidth
          label="Average heart rate"
          type="number"
          value={activity.averageHeartRate}
          onChange={updateField("averageHeartRate")}
          inputProps={{ min: 30, max: 250, step: 1 }}
          InputProps={{ endAdornment: <Typography className="field-suffix">bpm</Typography> }}
        />

        <TextField
          fullWidth
          multiline
          minRows={3}
          label="Notes (optional)"
          placeholder="How did the session feel? Add any details you want to remember."
          value={activity.notes}
          onChange={updateField("notes")}
        />

        <Button
          type="submit"
          fullWidth
          variant="contained"
          className="activity-submit-button"
          disabled={submitting}
        >
          {submitting ? <CircularProgress size={20} color="inherit" /> : "Save activity"}
        </Button>
      </Box>
    </Paper>
  );
};

export default ActivityForm;

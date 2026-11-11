import { useState } from "react";

import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  InputLabel,
  MenuItem,
  OutlinedInput,
  Select,
  Stack,
  TextField,
  Typography,
} from "@mui/material";

import { generateWorkoutPlan } from "../../services/api";

const WEEKDAYS = [
  { label: "Monday", value: "MONDAY" },
  { label: "Tuesday", value: "TUESDAY" },
  { label: "Wednesday", value: "WEDNESDAY" },
  { label: "Thursday", value: "THURSDAY" },
  { label: "Friday", value: "FRIDAY" },
  { label: "Saturday", value: "SATURDAY" },
  { label: "Sunday", value: "SUNDAY" },
];

const getTomorrowDate = () => {
  const date = new Date();
  date.setDate(date.getDate() + 1);

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
};

const initialForm = {
  availableDays: ["MONDAY", "WEDNESDAY", "FRIDAY"],
  sessionDurationMinutes: 45,
  fitnessLevel: "BEGINNER",
  equipment: "Bodyweight",
  preferredStartTime: "07:00",
  startDate: getTomorrowDate(),
  workoutFocus: "GENERAL_FITNESS",
  preferences: "",
};

const AIWorkoutGenerator = ({
  open,
  onClose,
  onGenerated,
}) => {
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (field, value) => {
    setForm((previous) => ({
      ...previous,
      [field]: value,
    }));
  };

  const handleClose = () => {
    if (loading) return;

    setError("");
    onClose();
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    setError("");

    if (form.availableDays.length === 0) {
      setError("Please select at least one training day.");
      return;
    }

    if (form.availableDays.length > 7) {
      setError("You can select up to seven training days.");
      return;
    }

    try {
      setLoading(true);

      const payload = {
        daysPerWeek: form.availableDays.length,

        sessionDurationMinutes: Number(
          form.sessionDurationMinutes
        ),

        fitnessLevel: form.fitnessLevel,

        equipment: form.equipment
          .split(",")
          .map((item) => item.trim())
          .filter(Boolean),

        availableDays: form.availableDays,

        preferredStartTime: form.preferredStartTime,

        startDate: form.startDate,

        workoutFocus: form.workoutFocus,

        preferences: form.preferences,
      };

      const response = await generateWorkoutPlan(payload);

      onGenerated(response);
      onClose();

    } catch (err) {
      console.error("AI workout generation failed:", err);

      setError(
        err.response?.data?.message ||
          err.response?.data?.error ||
          "Unable to generate your workout plan. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      fullWidth
      maxWidth="sm"
    >
      <Box component="form" onSubmit={handleSubmit}>
        <DialogTitle>
          <Typography variant="h5" fontWeight={700}>
            AI Workout Generator
          </Typography>

          <Typography
            variant="body2"
            color="text.secondary"
            sx={{ mt: 0.5 }}
          >
            Generate a personalized workout plan using your
            fitness goals and activity history.
          </Typography>
        </DialogTitle>

        <DialogContent dividers>
          <Stack spacing={2.5} sx={{ pt: 1 }}>

            {error && (
              <Alert severity="error">
                {error}
              </Alert>
            )}

            {/* Training Days */}
            <FormControl fullWidth>
              <InputLabel id="training-days-label">
                Available Training Days
              </InputLabel>

              <Select
                labelId="training-days-label"
                multiple
                value={form.availableDays}
                onChange={(event) =>
                  handleChange(
                    "availableDays",
                    event.target.value
                  )
                }
                input={
                  <OutlinedInput label="Available Training Days" />
                }
                renderValue={(selected) => (
                  <Box
                    sx={{
                      display: "flex",
                      flexWrap: "wrap",
                      gap: 0.5,
                    }}
                  >
                    {selected.map((day) => (
                      <Chip
                        key={day}
                        label={
                          WEEKDAYS.find(
                            (item) => item.value === day
                          )?.label || day
                        }
                        size="small"
                      />
                    ))}
                  </Box>
                )}
              >
                {WEEKDAYS.map((day) => (
                  <MenuItem
                    key={day.value}
                    value={day.value}
                  >
                    {day.label}
                  </MenuItem>
                ))}
              </Select>

              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ mt: 0.5 }}
              >
                Selected sessions per week: {form.availableDays.length}
              </Typography>
            </FormControl>

            {/* Session Duration */}
            <TextField
              select
              fullWidth
              label="Session Duration"
              value={form.sessionDurationMinutes}
              onChange={(event) =>
                handleChange(
                  "sessionDurationMinutes",
                  event.target.value
                )
              }
            >
              {[30, 45, 60, 75, 90, 120].map((minutes) => (
                <MenuItem key={minutes} value={minutes}>
                  {minutes} minutes
                </MenuItem>
              ))}
            </TextField>

            {/* Fitness Level */}
            <TextField
              select
              fullWidth
              label="Fitness Level"
              value={form.fitnessLevel}
              onChange={(event) =>
                handleChange("fitnessLevel", event.target.value)
              }
            >
              <MenuItem value="BEGINNER">Beginner</MenuItem>
              <MenuItem value="INTERMEDIATE">Intermediate</MenuItem>
              <MenuItem value="ADVANCED">Advanced</MenuItem>
            </TextField>

            {/* Equipment */}
            <TextField
              fullWidth
              label="Available Equipment"
              value={form.equipment}
              onChange={(event) =>
                handleChange("equipment", event.target.value)
              }
              placeholder="Bodyweight, Dumbbells, Resistance Bands"
              helperText="Separate multiple equipment items with commas."
            />

            {/* Preferred Start Date */}
            <TextField
              fullWidth
              type="date"
              label="Plan Start Date"
              value={form.startDate}
              onChange={(event) =>
                handleChange("startDate", event.target.value)
              }
              InputLabelProps={{ shrink: true }}
              inputProps={{
                min: getTomorrowDate(),
              }}
              required
            />

            {/* Preferred Time */}
            <TextField
              fullWidth
              type="time"
              label="Preferred Workout Time"
              value={form.preferredStartTime}
              onChange={(event) =>
                handleChange(
                  "preferredStartTime",
                  event.target.value
                )
              }
              InputLabelProps={{ shrink: true }}
              required
            />

            {/* Workout Focus */}
            <TextField
              select
              fullWidth
              label="Workout Focus"
              value={form.workoutFocus}
              onChange={(event) =>
                handleChange("workoutFocus", event.target.value)
              }
            >
              <MenuItem value="GENERAL_FITNESS">
                General Fitness
              </MenuItem>

              <MenuItem value="MUSCLE_GAIN">
                Muscle Gain
              </MenuItem>

              <MenuItem value="WEIGHT_LOSS">
                Weight Loss
              </MenuItem>

              <MenuItem value="STRENGTH">
                Strength
              </MenuItem>

              <MenuItem value="ENDURANCE">
                Endurance
              </MenuItem>

              <MenuItem value="FLEXIBILITY">
                Flexibility
              </MenuItem>
            </TextField>

            {/* Additional Preferences */}
            <TextField
              fullWidth
              multiline
              rows={3}
              label="Additional Preferences"
              placeholder="Example: Focus on upper body, avoid high-impact exercises."
              value={form.preferences}
              onChange={(event) =>
                handleChange("preferences", event.target.value)
              }
            />

            <Alert severity="info">
              Your existing goals, activity history, and scheduled
              workouts will help personalize the generated plan.
            </Alert>

          </Stack>
        </DialogContent>

        <DialogActions sx={{ p: 2 }}>
          <Button
            onClick={handleClose}
            disabled={loading}
          >
            Cancel
          </Button>

          <Button
            type="submit"
            variant="contained"
            disabled={loading || form.availableDays.length === 0}
          >
            {loading ? (
              <>
                <CircularProgress
                  size={18}
                  color="inherit"
                  sx={{ mr: 1 }}
                />
                Generating...
              </>
            ) : (
              "Generate Workout Plan"
            )}
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  );
};

export default AIWorkoutGenerator;
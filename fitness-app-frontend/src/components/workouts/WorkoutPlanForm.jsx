import { useEffect, useState } from "react";
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from "@mui/material";

const emptyExercise = () => ({
  exerciseName: "",
  sets: 3,
  reps: 10,
  durationMinutes: "",
  notes: "",
});

const getLocalDateTime = (value) => {
  if (!value) return "";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) return "";

  const offset = date.getTimezoneOffset() * 60000;

  return new Date(date.getTime() - offset)
    .toISOString()
    .slice(0, 16);
};

const WorkoutPlanForm = ({
  open,
  onClose,
  onSave,
  initialPlan,
  saving = false,
}) => {
  const [form, setForm] = useState({
    title: "",
    description: "",
    scheduledAt: "",
    exercises: [emptyExercise()],
  });

  const [error, setError] = useState("");

  useEffect(() => {
    if (!open) return;

    if (initialPlan) {
      setForm({
        title: initialPlan.title || "",
        description: initialPlan.description || "",
        scheduledAt: getLocalDateTime(initialPlan.scheduledAt),
        exercises:
          initialPlan.exercises?.length > 0
            ? initialPlan.exercises.map((exercise) => ({
                exerciseName: exercise.exerciseName || "",
                sets: exercise.sets ?? 3,
                reps: exercise.reps ?? 10,
                durationMinutes: exercise.durationMinutes ?? "",
                notes: exercise.notes || "",
              }))
            : [emptyExercise()],
      });
    } else {
      setForm({
        title: "",
        description: "",
        scheduledAt: "",
        exercises: [emptyExercise()],
      });
    }

    setError("");
  }, [open, initialPlan]);

  const updateField = (field, value) => {
    setForm((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const updateExercise = (index, field, value) => {
    setForm((current) => ({
      ...current,
      exercises: current.exercises.map((exercise, exerciseIndex) =>
        exerciseIndex === index
          ? { ...exercise, [field]: value }
          : exercise
      ),
    }));
  };

  const addExercise = () => {
    setForm((current) => ({
      ...current,
      exercises: [...current.exercises, emptyExercise()],
    }));
  };

  const removeExercise = (index) => {
    setForm((current) => ({
      ...current,
      exercises:
        current.exercises.length === 1
          ? [emptyExercise()]
          : current.exercises.filter(
              (_, exerciseIndex) => exerciseIndex !== index
            ),
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    if (!form.title.trim()) {
      setError("Enter a workout title.");
      return;
    }

    if (!form.scheduledAt) {
      setError("Choose a workout date and time.");
      return;
    }

    const validExercises = form.exercises.filter(
      (exercise) => exercise.exerciseName.trim()
    );

    if (validExercises.length === 0) {
      setError("Add at least one exercise.");
      return;
    }

    const payload = {
      title: form.title.trim(),
      description: form.description.trim(),
      scheduledAt: form.scheduledAt,
      exercises: validExercises.map((exercise) => ({
        exerciseName: exercise.exerciseName.trim(),
        sets: exercise.sets === "" ? null : Number(exercise.sets),
        reps: exercise.reps === "" ? null : Number(exercise.reps),
        durationMinutes:
          exercise.durationMinutes === ""
            ? null
            : Number(exercise.durationMinutes),
        notes: exercise.notes.trim(),
      })),
    };

    try {
      await onSave(payload);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.response?.data?.error ||
          "Unable to save the workout plan."
      );
    }
  };

  return (
    <Dialog
      open={open}
      onClose={saving ? undefined : onClose}
      fullWidth
      maxWidth="md"
    >
      <Box component="form" onSubmit={handleSubmit}>
        <DialogTitle>
          {initialPlan ? "Edit Workout Plan" : "Create Workout Plan"}
        </DialogTitle>

        <DialogContent dividers>
          <Stack spacing={2.5}>
            {error && <Alert severity="error">{error}</Alert>}

            <TextField
              label="Workout title"
              value={form.title}
              onChange={(event) =>
                updateField("title", event.target.value)
              }
              fullWidth
              required
              placeholder="Example: Upper Body Strength"
            />

            <TextField
              label="Description"
              value={form.description}
              onChange={(event) =>
                updateField("description", event.target.value)
              }
              fullWidth
              multiline
              minRows={2}
              placeholder="Describe your workout goals..."
            />

            <TextField
              label="Scheduled date and time"
              type="datetime-local"
              value={form.scheduledAt}
              onChange={(event) =>
                updateField("scheduledAt", event.target.value)
              }
              fullWidth
              required
              slotProps={{
                inputLabel: { shrink: true },
              }}
            />

            <Box>
              <Stack
                direction="row"
                justifyContent="space-between"
                alignItems="center"
                sx={{ mb: 2 }}
              >
                <Typography variant="h6" fontWeight={700}>
                  Exercises
                </Typography>

                <Button onClick={addExercise}>
                  + Add exercise
                </Button>
              </Stack>

              <Stack spacing={2}>
                {form.exercises.map((exercise, index) => (
                  <Box
                    key={index}
                    sx={{
                      p: 2,
                      border: "1px solid",
                      borderColor: "divider",
                      borderRadius: 2,
                    }}
                  >
                    <Stack
                      direction="row"
                      justifyContent="space-between"
                      alignItems="center"
                      sx={{ mb: 2 }}
                    >
                      <Typography fontWeight={600}>
                        Exercise {index + 1}
                      </Typography>

                      <IconButton
                        color="error"
                        size="small"
                        onClick={() => removeExercise(index)}
                        aria-label={`Remove exercise ${index + 1}`}
                      >
                        ×
                      </IconButton>
                    </Stack>

                    <Stack spacing={2}>
                      <TextField
                        label="Exercise name"
                        value={exercise.exerciseName}
                        onChange={(event) =>
                          updateExercise(
                            index,
                            "exerciseName",
                            event.target.value
                          )
                        }
                        fullWidth
                        placeholder="Example: Bench Press"
                      />

                      <Box
                        sx={{
                          display: "grid",
                          gridTemplateColumns: {
                            xs: "1fr",
                            sm: "repeat(3, minmax(0, 1fr))",
                          },
                          gap: 2,
                        }}
                      >
                        <TextField
                          label="Sets"
                          type="number"
                          value={exercise.sets}
                          onChange={(event) =>
                            updateExercise(
                              index,
                              "sets",
                              event.target.value
                            )
                          }
                          inputProps={{ min: 0 }}
                        />

                        <TextField
                          label="Reps"
                          type="number"
                          value={exercise.reps}
                          onChange={(event) =>
                            updateExercise(
                              index,
                              "reps",
                              event.target.value
                            )
                          }
                          inputProps={{ min: 0 }}
                        />

                        <TextField
                          label="Duration (minutes)"
                          type="number"
                          value={exercise.durationMinutes}
                          onChange={(event) =>
                            updateExercise(
                              index,
                              "durationMinutes",
                              event.target.value
                            )
                          }
                          inputProps={{ min: 0 }}
                        />
                      </Box>

                      <TextField
                        label="Notes"
                        value={exercise.notes}
                        onChange={(event) =>
                          updateExercise(
                            index,
                            "notes",
                            event.target.value
                          )
                        }
                        fullWidth
                        multiline
                        minRows={2}
                        placeholder="Optional exercise instructions"
                      />
                    </Stack>
                  </Box>
                ))}
              </Stack>
            </Box>
          </Stack>
        </DialogContent>

        <DialogActions sx={{ p: 2 }}>
          <Button onClick={onClose} disabled={saving}>
            Cancel
          </Button>

          <Button
            type="submit"
            variant="contained"
            disabled={saving}
          >
            {saving
              ? "Saving..."
              : initialPlan
                ? "Update Workout"
                : "Create Workout"}
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  );
};

export default WorkoutPlanForm;
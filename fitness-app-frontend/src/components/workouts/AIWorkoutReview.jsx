import { useEffect, useState } from "react";

import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  FormControlLabel,
  IconButton,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";

import {
  Add,
  DeleteOutline,
} from "@mui/icons-material";

import { createWorkoutPlan } from "../../services/api";

const toLocalDateTime = (value) => {
  if (!value) return "";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) return "";

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");

  return `${year}-${month}-${day}T${hours}:${minutes}`;
};

const createEditableSession = (session, index) => ({
  ...session,
  draftId: `${Date.now()}-${index}`,
  scheduledAt: toLocalDateTime(session.scheduledAt),
  exercises: (session.exercises || []).map((exercise) => ({
    ...exercise,
    sets: exercise.sets ?? "",
    reps: exercise.reps ?? "",
    durationMinutes: exercise.durationMinutes ?? "",
    notes: exercise.notes || "",
  })),
  selected: true,
  saved: false,
  error: "",
});

const AIWorkoutReview = ({
  open,
  draft,
  onClose,
  onSaved,
}) => {
  const [sessions, setSessions] = useState([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    if (open && draft?.sessions) {
      setSessions(
        draft.sessions.map((session, index) =>
          createEditableSession(session, index)
        )
      );

      setError("");
      setSuccessMessage("");
    }
  }, [open, draft]);

  const updateSession = (index, field, value) => {
    setSessions((previous) =>
      previous.map((session, sessionIndex) =>
        sessionIndex === index
          ? { ...session, [field]: value }
          : session
      )
    );
  };

  const updateExercise = (
    sessionIndex,
    exerciseIndex,
    field,
    value
  ) => {
    setSessions((previous) =>
      previous.map((session, currentSessionIndex) => {
        if (currentSessionIndex !== sessionIndex) {
          return session;
        }

        const exercises = session.exercises.map(
          (exercise, currentExerciseIndex) =>
            currentExerciseIndex === exerciseIndex
              ? { ...exercise, [field]: value }
              : exercise
        );

        return { ...session, exercises };
      })
    );
  };

  const addExercise = (sessionIndex) => {
    setSessions((previous) =>
      previous.map((session, index) =>
        index === sessionIndex
          ? {
              ...session,
              exercises: [
                ...session.exercises,
                {
                  exerciseName: "",
                  sets: 3,
                  reps: 10,
                  durationMinutes: "",
                  notes: "",
                },
              ],
            }
          : session
      )
    );
  };

  const removeExercise = (sessionIndex, exerciseIndex) => {
    setSessions((previous) =>
      previous.map((session, index) =>
        index === sessionIndex
          ? {
              ...session,
              exercises: session.exercises.filter(
                (_, exerciseIndexToRemove) =>
                  exerciseIndexToRemove !== exerciseIndex
              ),
            }
          : session
      )
    );
  };

  const validateSession = (session) => {
    if (!session.title?.trim()) {
      return "Workout title is required.";
    }

    if (!session.scheduledAt) {
      return "Workout date and time are required.";
    }

    if (
      Number.isNaN(new Date(session.scheduledAt).getTime()) ||
      new Date(session.scheduledAt).getTime() <= Date.now()
    ) {
      return "Workout must be scheduled for a future date and time.";
    }

    if (!session.exercises?.length) {
      return "Add at least one exercise.";
    }

    for (const exercise of session.exercises) {
      if (!exercise.exerciseName?.trim()) {
        return "Every exercise needs a name.";
      }

      const sets = Number(exercise.sets);
      const reps = Number(exercise.reps);
      const duration = Number(exercise.durationMinutes);

      const hasValidPrescription =
        (exercise.sets !== "" && sets > 0) ||
        (exercise.reps !== "" && reps > 0) ||
        (exercise.durationMinutes !== "" && duration > 0);

      if (!hasValidPrescription) {
        return `${exercise.exerciseName} needs sets, reps, or duration.`;
      }

      if (
        (exercise.sets !== "" && sets <= 0) ||
        (exercise.reps !== "" && reps <= 0) ||
        (exercise.durationMinutes !== "" && duration <= 0)
      ) {
        return `Exercise values must be positive for ${exercise.exerciseName}.`;
      }
    }

    return "";
  };

  const handleSaveSelected = async () => {
    setError("");
    setSuccessMessage("");

    const selectedSessions = sessions
      .map((session, index) => ({ session, index }))
      .filter(({ session }) => session.selected && !session.saved);

    if (selectedSessions.length === 0) {
      setError("Select at least one unsaved workout session.");
      return;
    }

    for (const { session, index } of selectedSessions) {
      const validationError = validateSession(session);

      if (validationError) {
        setError(`Session ${index + 1}: ${validationError}`);
        return;
      }
    }

    setSaving(true);

    let savedCount = 0;

    for (const { session, index } of selectedSessions) {
      try {
        const exercises = session.exercises.map((exercise) => ({
          exerciseName: exercise.exerciseName.trim(),

          sets:
            exercise.sets === ""
              ? null
              : Number(exercise.sets),

          reps:
            exercise.reps === ""
              ? null
              : Number(exercise.reps),

          durationMinutes:
            exercise.durationMinutes === ""
              ? null
              : Number(exercise.durationMinutes),

          notes: exercise.notes || "",
        }));

        const description = [
          session.description?.trim(),
          session.rationale
            ? `AI rationale: ${session.rationale}`
            : "",
        ]
          .filter(Boolean)
          .join("\n\n");

        const payload = {
          title: session.title.trim(),
          description,
          scheduledAt: `${session.scheduledAt}:00`,
          exercises,
        };

        await createWorkoutPlan(payload);

        savedCount++;

        setSessions((previous) =>
          previous.map((item, itemIndex) =>
            itemIndex === index
              ? { ...item, saved: true, error: "" }
              : item
          )
        );
      } catch (err) {
        const message =
          err.response?.data?.message ||
          err.response?.data?.error ||
          "Unable to save this workout.";

        setSessions((previous) =>
          previous.map((item, itemIndex) =>
            itemIndex === index
              ? { ...item, error: message }
              : item
          )
        );
      }
    }

    setSaving(false);

    if (savedCount > 0) {
      setSuccessMessage(
        `${savedCount} workout session(s) saved successfully.`
      );

      await onSaved?.(savedCount);
    }

    if (savedCount === selectedSessions.length) {
      onClose();
    } else if (savedCount === 0) {
      setError(
        "No workouts were saved. Check the errors displayed on each session."
      );
    }
  };

  const selectedCount = sessions.filter(
    (session) => session.selected && !session.saved
  ).length;

  const savedCount = sessions.filter(
    (session) => session.saved
  ).length;

  return (
    <Dialog
      open={open}
      onClose={saving ? undefined : onClose}
      fullWidth
      maxWidth="md"
      scroll="paper"
    >
      <DialogTitle>
        <Typography variant="h5" fontWeight={700}>
          Review AI Workout Plan
        </Typography>

        <Typography
          variant="body2"
          color="text.secondary"
          sx={{ mt: 0.5 }}
        >
          Review and edit each session. Only selected workouts
          will be saved to your calendar.
        </Typography>
      </DialogTitle>

      <DialogContent dividers>
        <Stack spacing={2}>
          {draft?.summary && (
            <Alert severity="info">
              {draft.summary}
            </Alert>
          )}

          {error && (
            <Alert severity="error">
              {error}
            </Alert>
          )}

          {successMessage && (
            <Alert severity="success">
              {successMessage}
            </Alert>
          )}

          <Stack
            direction="row"
            justifyContent="space-between"
            alignItems="center"
          >
            <Typography fontWeight={600}>
              Generated sessions
            </Typography>

            <Stack direction="row" spacing={1}>
              <Chip
                label={`${selectedCount} selected`}
                color="primary"
                size="small"
              />

              <Chip
                label={`${savedCount} saved`}
                color="success"
                size="small"
              />
            </Stack>
          </Stack>

          {sessions.map((session, sessionIndex) => (
            <Paper
              key={session.draftId}
              variant="outlined"
              sx={{ p: 2, borderRadius: 2 }}
            >
              <Stack spacing={2}>
                <Stack
                  direction="row"
                  justifyContent="space-between"
                  alignItems="center"
                >
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={session.selected}
                        disabled={saving || session.saved}
                        onChange={(event) =>
                          updateSession(
                            sessionIndex,
                            "selected",
                            event.target.checked
                          )
                        }
                      />
                    }
                    label={
                      <Typography fontWeight={700}>
                        Session {sessionIndex + 1}
                      </Typography>
                    }
                  />

                  {session.saved && (
                    <Chip
                      label="Saved"
                      color="success"
                      size="small"
                    />
                  )}
                </Stack>

                {session.error && (
                  <Alert severity="error">
                    {session.error}
                  </Alert>
                )}

                <TextField
                  fullWidth
                  label="Workout Title"
                  value={session.title || ""}
                  disabled={saving || session.saved}
                  onChange={(event) =>
                    updateSession(
                      sessionIndex,
                      "title",
                      event.target.value
                    )
                  }
                />

                <TextField
                  fullWidth
                  multiline
                  rows={2}
                  label="Description"
                  value={session.description || ""}
                  disabled={saving || session.saved}
                  onChange={(event) =>
                    updateSession(
                      sessionIndex,
                      "description",
                      event.target.value
                    )
                  }
                />

                <TextField
                  fullWidth
                  type="datetime-local"
                  label="Scheduled Date and Time"
                  value={session.scheduledAt || ""}
                  disabled={saving || session.saved}
                  onChange={(event) =>
                    updateSession(
                      sessionIndex,
                      "scheduledAt",
                      event.target.value
                    )
                  }
                  InputLabelProps={{ shrink: true }}
                />

                {session.rationale && (
                  <Alert severity="info">
                    <Typography
                      variant="subtitle2"
                      fontWeight={700}
                    >
                      AI rationale
                    </Typography>

                    {session.rationale}
                  </Alert>
                )}

                <Divider />

                <Typography fontWeight={700}>
                  Exercises
                </Typography>

                {session.exercises.map((exercise, exerciseIndex) => (
                  <Box
                    key={exerciseIndex}
                    sx={{
                      p: 1.5,
                      borderRadius: 2,
                      bgcolor: "action.hover",
                    }}
                  >
                    <Stack spacing={1.5}>
                      <Stack
                        direction="row"
                        alignItems="center"
                        justifyContent="space-between"
                      >
                        <Typography fontWeight={600}>
                          Exercise {exerciseIndex + 1}
                        </Typography>

                        <IconButton
                          color="error"
                          size="small"
                          disabled={saving || session.saved}
                          onClick={() =>
                            removeExercise(
                              sessionIndex,
                              exerciseIndex
                            )
                          }
                        >
                          <DeleteOutline />
                        </IconButton>
                      </Stack>

                      <TextField
                        fullWidth
                        size="small"
                        label="Exercise Name"
                        value={exercise.exerciseName || ""}
                        disabled={saving || session.saved}
                        onChange={(event) =>
                          updateExercise(
                            sessionIndex,
                            exerciseIndex,
                            "exerciseName",
                            event.target.value
                          )
                        }
                      />

                      <Box
                        sx={{
                          display: "grid",
                          gridTemplateColumns: {
                            xs: "1fr",
                            sm: "repeat(3, 1fr)",
                          },
                          gap: 1,
                        }}
                      >
                        <TextField
                          size="small"
                          type="number"
                          label="Sets"
                          value={exercise.sets}
                          disabled={saving || session.saved}
                          onChange={(event) =>
                            updateExercise(
                              sessionIndex,
                              exerciseIndex,
                              "sets",
                              event.target.value
                            )
                          }
                          inputProps={{ min: 1 }}
                        />

                        <TextField
                          size="small"
                          type="number"
                          label="Reps"
                          value={exercise.reps}
                          disabled={saving || session.saved}
                          onChange={(event) =>
                            updateExercise(
                              sessionIndex,
                              exerciseIndex,
                              "reps",
                              event.target.value
                            )
                          }
                          inputProps={{ min: 1 }}
                        />

                        <TextField
                          size="small"
                          type="number"
                          label="Duration (minutes)"
                          value={exercise.durationMinutes}
                          disabled={saving || session.saved}
                          onChange={(event) =>
                            updateExercise(
                              sessionIndex,
                              exerciseIndex,
                              "durationMinutes",
                              event.target.value
                            )
                          }
                          inputProps={{ min: 1 }}
                        />
                      </Box>

                      <TextField
                        fullWidth
                        size="small"
                        label="Exercise Notes"
                        value={exercise.notes || ""}
                        disabled={saving || session.saved}
                        onChange={(event) =>
                          updateExercise(
                            sessionIndex,
                            exerciseIndex,
                            "notes",
                            event.target.value
                          )
                        }
                      />
                    </Stack>
                  </Box>
                ))}

                <Button
                  startIcon={<Add />}
                  variant="outlined"
                  size="small"
                  disabled={saving || session.saved}
                  onClick={() => addExercise(sessionIndex)}
                >
                  Add Exercise
                </Button>
              </Stack>
            </Paper>
          ))}

          {sessions.length === 0 && (
            <Alert severity="warning">
              No generated workout sessions are available.
            </Alert>
          )}
        </Stack>
      </DialogContent>

      <DialogActions sx={{ p: 2 }}>
        <Button onClick={onClose} disabled={saving}>
          Close
        </Button>

        <Button
          variant="contained"
          onClick={handleSaveSelected}
          disabled={saving || selectedCount === 0}
        >
          {saving ? (
            <>
              <CircularProgress
                size={18}
                color="inherit"
                sx={{ mr: 1 }}
              />
              Saving Workouts...
            </>
          ) : (
            `Save ${selectedCount} Selected Workout(s)`
          )}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AIWorkoutReview;
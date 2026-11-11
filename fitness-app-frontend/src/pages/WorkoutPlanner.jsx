import { useCallback, useEffect, useMemo, useState } from "react";

import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Paper,
  Stack,
  Typography,
} from "@mui/material";

import {
  getWorkoutPlans,
  createWorkoutPlan,
  updateWorkoutPlan,
  deleteWorkoutPlan,
  completeWorkoutPlan,
} from "../services/api";

import WorkoutPlanForm from "../components/workouts/WorkoutPlanForm";
import WorkoutAdherence from "../components/workouts/WorkoutAdherence";
import AIWorkoutGenerator from "../components/workouts/AIWorkoutGenerator";
import AIWorkoutReview from "../components/workouts/AIWorkoutReview";

// Get Monday as the start of the week.
const getStartOfWeek = (date) => {
  const result = new Date(date);
  result.setHours(0, 0, 0, 0);

  const day = result.getDay();
  const difference = day === 0 ? -6 : 1 - day;

  result.setDate(result.getDate() + difference);
  return result;
};

// Add or subtract days.
const addDays = (date, days) => {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
};

// Generate a local date key.
const getDateKey = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
};

// Format workout time.
const formatTime = (dateValue) => {
  if (!dateValue) return "Time not available";

  const date = new Date(dateValue);

  if (Number.isNaN(date.getTime())) {
    return "Time not available";
  }

  return date.toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });
};

const WorkoutPlanner = () => {
  const [plans, setPlans] = useState([]);

  const [weekStart, setWeekStart] = useState(() =>
    getStartOfWeek(new Date())
  );

  const [selectedPlan, setSelectedPlan] = useState(null);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  // Workout form state.
  const [formOpen, setFormOpen] = useState(false);
  const [editingPlan, setEditingPlan] = useState(null);

  // AI workout generator state.
  const [aiGeneratorOpen, setAiGeneratorOpen] = useState(false);
  const [generatedDraft, setGeneratedDraft] = useState(null);
  const [aiReviewOpen, setAiReviewOpen] = useState(false);

  // Fetch workout plans.
  const loadWorkoutPlans = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const data = await getWorkoutPlans();

      setPlans(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Failed to load workout plans:", err);

      setError(
        err.response?.data?.message ||
          "Unable to load workout plans. Please try again."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadWorkoutPlans();
  }, [loadWorkoutPlans]);

  // Generate the seven days of the current week.
  const weekDays = useMemo(
    () =>
      Array.from({ length: 7 }, (_, index) =>
        addDays(weekStart, index)
      ),
    [weekStart]
  );

  // Group workout plans by scheduled date.
  const plansByDate = useMemo(() => {
    const grouped = {};

    plans.forEach((plan) => {
      if (!plan.scheduledAt) return;

      const date = new Date(plan.scheduledAt);

      if (Number.isNaN(date.getTime())) return;

      const key = getDateKey(date);

      if (!grouped[key]) {
        grouped[key] = [];
      }

      grouped[key].push(plan);
    });

    Object.values(grouped).forEach((dayPlans) => {
      dayPlans.sort(
        (a, b) =>
          new Date(a.scheduledAt).getTime() -
          new Date(b.scheduledAt).getTime()
      );
    });

    return grouped;
  }, [plans]);

  const todayKey = getDateKey(new Date());

  const weekLabel = `${weekStart.toLocaleDateString([], {
    month: "short",
    day: "numeric",
  })} - ${addDays(weekStart, 6).toLocaleDateString([], {
    month: "short",
    day: "numeric",
    year: "numeric",
  })}`;

  // Navigate between weeks.
  const changeWeek = (amount) => {
    setWeekStart((current) => addDays(current, amount * 7));
    setSelectedPlan(null);
  };

  const goToToday = () => {
    setWeekStart(getStartOfWeek(new Date()));
    setSelectedPlan(null);
  };

  // Status chip colors.
  const getStatusColor = (status) => {
    switch (status?.toUpperCase()) {
      case "COMPLETED":
        return "success";
      case "CANCELLED":
        return "error";
      default:
        return "primary";
    }
  };

  // Open an empty form for creating a workout.
  const handleCreate = () => {
    setEditingPlan(null);
    setError("");
    setFormOpen(true);
  };

  // Open the form with the selected workout.
  const handleEdit = () => {
    if (!selectedPlan) return;

    setEditingPlan(selectedPlan);
    setError("");
    setFormOpen(true);
  };

  // Receive the AI-generated workout draft.
  const handleGeneratedWorkout = (response) => {
    if (!response?.sessions?.length) {
      setError("The AI did not generate any workout sessions.");
      return;
    }

    setGeneratedDraft(response);
    setAiGeneratorOpen(false);
    setAiReviewOpen(true);
    setSelectedPlan(null);
    setError("");
  };

  // Refresh calendar after AI workouts are saved.
  const handleAIWorkoutsSaved = async (savedCount) => {
    console.log(`${savedCount} AI workout(s) saved successfully.`);

    await loadWorkoutPlans();
  };

  // Create or update a workout plan.
  const handleSavePlan = async (payload) => {
    try {
      setSaving(true);
      setError("");

      if (editingPlan) {
        await updateWorkoutPlan(editingPlan.id, payload);
      } else {
        await createWorkoutPlan(payload);
      }

      setFormOpen(false);
      setEditingPlan(null);
      setSelectedPlan(null);

      await loadWorkoutPlans();
    } catch (err) {
      console.error("Failed to save workout plan:", err);

      setError(
        err.response?.data?.message ||
          "Unable to save workout plan. Please try again."
      );
    } finally {
      setSaving(false);
    }
  };

  // Delete a workout plan.
  const handleDeletePlan = async () => {
    if (!selectedPlan) return;

    const confirmed = window.confirm(
      `Delete "${selectedPlan.title}"? This action cannot be undone.`
    );

    if (!confirmed) return;

    try {
      setError("");

      await deleteWorkoutPlan(selectedPlan.id);

      setSelectedPlan(null);
      await loadWorkoutPlans();
    } catch (err) {
      console.error("Failed to delete workout plan:", err);

      setError(
        err.response?.data?.message ||
          "Unable to delete this workout plan."
      );
    }
  };

  // Mark a workout as completed.
  const handleCompleteWorkout = async () => {
    if (!selectedPlan) return;

    try {
      setError("");

      await completeWorkoutPlan(selectedPlan.id);

      setSelectedPlan(null);
      await loadWorkoutPlans();
    } catch (err) {
      console.error("Failed to complete workout:", err);

      setError(
        err.response?.data?.message ||
          "Unable to mark this workout as completed."
      );
    }
  };

  return (
    <Box sx={{ maxWidth: 1400, mx: "auto" }}>

      {/* Page header */}
      <Stack
        direction={{ xs: "column", sm: "row" }}
        justifyContent="space-between"
        alignItems={{ xs: "flex-start", sm: "center" }}
        spacing={2}
        sx={{ mb: 3 }}
      >
        <Box>
          <Typography variant="h4" fontWeight={700}>
            Workout Planner
          </Typography>

          <Typography color="text.secondary" sx={{ mt: 0.5 }}>
            Organize your training schedule and stay consistent.
          </Typography>
        </Box>

        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={1}
        >
          <Button
            variant="outlined"
            onClick={loadWorkoutPlans}
            disabled={loading}
          >
            Refresh
          </Button>

          <Button
            variant="outlined"
            color="secondary"
            onClick={() => setAiGeneratorOpen(true)}
          >
            AI Generate
          </Button>

          <Button
            variant="contained"
            onClick={handleCreate}
          >
            + Create Workout
          </Button>
        </Stack>
      </Stack>

      {/* Error message */}
      {error && (
        <Alert
          severity="error"
          sx={{ mb: 3 }}
          onClose={() => setError("")}
        >
          {error}
        </Alert>
      )}

      {/* AI generation success message */}
      {generatedDraft && (
        <Alert
          severity="success"
          sx={{ mb: 3 }}
          action={
            <Stack direction="row" spacing={1}>
              <Button
                color="inherit"
                size="small"
                onClick={() => setAiReviewOpen(true)}
              >
                Review
              </Button>

              <Button
                color="inherit"
                size="small"
                onClick={() => setGeneratedDraft(null)}
              >
                Dismiss
              </Button>
            </Stack>
          }
        >
          AI generated your workout plan successfully.{" "}
          {generatedDraft.sessions?.length || 0} sessions are ready
          for review.

          {generatedDraft.summary && (
            <Typography variant="body2" sx={{ mt: 0.5 }}>
              {generatedDraft.summary}
            </Typography>
          )}
        </Alert>
      )}

      {/* Week navigation */}
      <Paper
        elevation={0}
        sx={{ p: 2, mb: 3, borderRadius: 3 }}
      >
        <Stack
          direction={{ xs: "column", sm: "row" }}
          justifyContent="space-between"
          alignItems={{ xs: "flex-start", sm: "center" }}
          spacing={2}
        >
          <Typography variant="h6" fontWeight={600}>
            {weekLabel}
          </Typography>

          <Stack direction="row" spacing={1}>
            <Button
              variant="outlined"
              onClick={() => changeWeek(-1)}
            >
              Previous
            </Button>

            <Button
              variant="outlined"
              onClick={goToToday}
            >
              Today
            </Button>

            <Button
              variant="outlined"
              onClick={() => changeWeek(1)}
            >
              Next
            </Button>
          </Stack>
        </Stack>
      </Paper>

      {/* Calendar */}
      {loading ? (
        <Box
          sx={{
            display: "flex",
            justifyContent: "center",
            py: 8,
          }}
        >
          <CircularProgress />
        </Box>
      ) : (
        <>
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: {
                xs: "1fr",
                sm: "repeat(2, minmax(0, 1fr))",
                md: "repeat(7, minmax(0, 1fr))",
              },
              gap: 1.5,
            }}
          >
            {weekDays.map((day) => {
              const dateKey = getDateKey(day);
              const dayPlans = plansByDate[dateKey] || [];
              const isToday = dateKey === todayKey;

              return (
                <Paper
                  key={dateKey}
                  elevation={0}
                  sx={{
                    minHeight: 190,
                    p: 1.5,
                    borderRadius: 2,
                    border: "1px solid",
                    borderColor: isToday
                      ? "primary.main"
                      : "divider",
                    backgroundColor: isToday
                      ? "action.selected"
                      : "background.paper",
                  }}
                >
                  <Box sx={{ mb: 1.5 }}>
                    <Typography
                      variant="caption"
                      color="text.secondary"
                      fontWeight={600}
                    >
                      {day.toLocaleDateString([], {
                        weekday: "short",
                      })}
                    </Typography>

                    <Typography
                      variant="h5"
                      fontWeight={700}
                      color={isToday ? "primary.main" : "text.primary"}
                    >
                      {day.getDate()}
                    </Typography>

                    {isToday && (
                      <Chip
                        label="Today"
                        size="small"
                        color="primary"
                        sx={{ mt: 0.5 }}
                      />
                    )}
                  </Box>

                  <Stack spacing={1}>
                    {dayPlans.length === 0 ? (
                      <Typography
                        variant="caption"
                        color="text.disabled"
                      >
                        No workouts
                      </Typography>
                    ) : (
                      dayPlans.map((plan) => (
                        <Paper
                          key={plan.id}
                          elevation={0}
                          onClick={() => setSelectedPlan(plan)}
                          sx={{
                            p: 1,
                            cursor: "pointer",
                            borderRadius: 1.5,
                            border: "1px solid",
                            borderColor:
                              selectedPlan?.id === plan.id
                                ? "primary.main"
                                : "divider",
                            backgroundColor: "background.default",
                            "&:hover": {
                              borderColor: "primary.main",
                            },
                          }}
                        >
                          <Typography
                            variant="caption"
                            color="text.secondary"
                          >
                            {formatTime(plan.scheduledAt)}
                          </Typography>

                          <Typography
                            variant="body2"
                            fontWeight={600}
                            sx={{
                              overflowWrap: "anywhere",
                              mt: 0.3,
                            }}
                          >
                            {plan.title}
                          </Typography>

                          <Chip
                            label={plan.status || "PLANNED"}
                            size="small"
                            color={getStatusColor(plan.status)}
                            sx={{ mt: 0.8, height: 21 }}
                          />
                        </Paper>
                      ))
                    )}
                  </Stack>
                </Paper>
              );
            })}
          </Box>

          {/* Selected workout details */}
          {selectedPlan && (
            <Paper
              elevation={0}
              sx={{
                mt: 3,
                p: 3,
                borderRadius: 3,
                border: "1px solid",
                borderColor: "divider",
              }}
            >
              <Stack
                direction={{ xs: "column", sm: "row" }}
                justifyContent="space-between"
                alignItems={{ xs: "flex-start", sm: "center" }}
                spacing={2}
              >
                <Box>
                  <Typography variant="h6" fontWeight={700}>
                    {selectedPlan.title}
                  </Typography>

                  <Typography color="text.secondary" sx={{ mt: 0.5 }}>
                    {new Date(selectedPlan.scheduledAt).toLocaleString()}
                  </Typography>
                </Box>

                <Chip
                  label={selectedPlan.status || "PLANNED"}
                  color={getStatusColor(selectedPlan.status)}
                />
              </Stack>

              {selectedPlan.description && (
                <Typography sx={{ mt: 2 }}>
                  {selectedPlan.description}
                </Typography>
              )}

              <Typography fontWeight={600} sx={{ mt: 2, mb: 1 }}>
                Exercises
              </Typography>

              {selectedPlan.exercises?.length ? (
                <Stack spacing={1}>
                  {selectedPlan.exercises.map((exercise, index) => (
                    <Box
                      key={`${exercise.exerciseName}-${index}`}
                      sx={{
                        p: 1.5,
                        borderRadius: 2,
                        backgroundColor: "action.hover",
                      }}
                    >
                      <Typography fontWeight={600}>
                        {exercise.exerciseName}
                      </Typography>

                      <Typography
                        variant="body2"
                        color="text.secondary"
                      >
                        {exercise.sets != null && `${exercise.sets} sets`}
                        {exercise.reps != null && ` · ${exercise.reps} reps`}
                        {exercise.durationMinutes != null &&
                          ` · ${exercise.durationMinutes} minutes`}
                      </Typography>

                      {exercise.notes && (
                        <Typography variant="body2" sx={{ mt: 0.5 }}>
                          {exercise.notes}
                        </Typography>
                      )}
                    </Box>
                  ))}
                </Stack>
              ) : (
                <Typography color="text.secondary">
                  No exercises added.
                </Typography>
              )}

              {/* Workout actions */}
              <Stack
                direction={{ xs: "column", sm: "row" }}
                spacing={1}
                sx={{ mt: 2 }}
              >
                <Button
                  variant="contained"
                  onClick={handleEdit}
                >
                  Edit Workout
                </Button>

                <Button
                  variant="outlined"
                  color="error"
                  onClick={handleDeletePlan}
                >
                  Delete Workout
                </Button>

                {selectedPlan.status !== "COMPLETED" &&
                  selectedPlan.status !== "CANCELLED" && (
                    <Button
                      variant="contained"
                      color="success"
                      onClick={handleCompleteWorkout}
                    >
                      Mark as Completed
                    </Button>
                  )}

                <Button onClick={() => setSelectedPlan(null)}>
                  Close
                </Button>
              </Stack>
            </Paper>
          )}

          {/* Empty calendar */}
          {plans.length === 0 && (
            <Paper
              elevation={0}
              sx={{
                mt: 3,
                p: 4,
                textAlign: "center",
                borderRadius: 3,
              }}
            >
              <Typography variant="h6">
                Your workout calendar is empty.
              </Typography>

              <Typography color="text.secondary" sx={{ mt: 1 }}>
                Create your first workout plan to see it here.
              </Typography>

              <Button
                variant="contained"
                sx={{ mt: 2 }}
                onClick={handleCreate}
              >
                Create Your First Workout
              </Button>
            </Paper>
          )}
        </>
      )}

      {/* Workout adherence dashboard */}
      <WorkoutAdherence />

      {/* AI Workout Generator */}
      <AIWorkoutGenerator
        open={aiGeneratorOpen}
        onClose={() => setAiGeneratorOpen(false)}
        onGenerated={handleGeneratedWorkout}
      />

      {/* AI Workout Review */}
      <AIWorkoutReview
        open={aiReviewOpen}
        draft={generatedDraft}
        onClose={() => setAiReviewOpen(false)}
        onSaved={handleAIWorkoutsSaved}
      />

      {/* Create and edit workout dialog */}
      <WorkoutPlanForm
        open={formOpen}
        onClose={() => {
          setFormOpen(false);
          setEditingPlan(null);
        }}
        onSave={handleSavePlan}
        initialPlan={editingPlan}
        saving={saving}
      />
    </Box>
  );
};

export default WorkoutPlanner;

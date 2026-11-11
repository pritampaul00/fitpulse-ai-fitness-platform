import { useCallback, useEffect, useState } from "react";
import {
  Alert,
  Box,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Typography,
} from "@mui/material";

import {
  getWorkoutAdherence,
  getWorkoutHistory,
} from "../../services/api";

const WorkoutAdherence = () => {
  const [period, setPeriod] = useState(30);
  const [stats, setStats] = useState(null);
  const [history, setHistory] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadDashboard = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const [adherenceData, historyData] = await Promise.all([
        getWorkoutAdherence(period),
        getWorkoutHistory(),
      ]);

      setStats(adherenceData);
      setHistory(Array.isArray(historyData) ? historyData : []);
    } catch (err) {
      console.error("Failed to load workout adherence:", err);

      setError(
        err.response?.data?.message ||
          "Unable to load workout statistics."
      );
    } finally {
      setLoading(false);
    }
  }, [period]);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  const formatDate = (value) => {
    if (!value) return "Date unavailable";

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return "Date unavailable";
    }

    return date.toLocaleString([], {
      dateStyle: "medium",
      timeStyle: "short",
    });
  };

  const statCards = [
    {
      label: "Scheduled",
      value: stats?.totalScheduled ?? 0,
      description: "Non-cancelled workouts",
    },
    {
      label: "Completed",
      value: stats?.completed ?? 0,
      description: "Workouts finished",
    },
    {
      label: "Pending",
      value: stats?.pending ?? 0,
      description: "Not completed",
    },
    {
      label: "Cancelled",
      value: stats?.cancelled ?? 0,
      description: "Cancelled workouts",
    },
  ];

  return (
    <Box sx={{ mt: 4 }}>
      <Stack
        direction={{ xs: "column", sm: "row" }}
        justifyContent="space-between"
        alignItems={{ xs: "flex-start", sm: "center" }}
        spacing={2}
        sx={{ mb: 3 }}
      >
        <Box>
          <Typography variant="h5" fontWeight={700}>
            Workout Progress
          </Typography>

          <Typography color="text.secondary" sx={{ mt: 0.5 }}>
            Track your consistency and training habits.
          </Typography>
        </Box>

        <FormControl size="small" sx={{ minWidth: 150 }}>
          <InputLabel id="adherence-period-label">
            Period
          </InputLabel>

          <Select
            labelId="adherence-period-label"
            value={period}
            label="Period"
            onChange={(event) =>
              setPeriod(Number(event.target.value))
            }
          >
            <MenuItem value={7}>Last 7 days</MenuItem>
            <MenuItem value={30}>Last 30 days</MenuItem>
            <MenuItem value={90}>Last 90 days</MenuItem>
          </Select>
        </FormControl>
      </Stack>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {loading ? (
        <Box sx={{ display: "flex", justifyContent: "center", py: 5 }}>
          <CircularProgress />
        </Box>
      ) : (
        <>
          {/* Adherence percentage */}
          <Card
            elevation={0}
            sx={{
              mb: 2,
              border: "1px solid",
              borderColor: "divider",
              borderRadius: 3,
            }}
          >
            <CardContent sx={{ p: 3 }}>
              <Typography color="text.secondary" variant="body2">
                Workout adherence
              </Typography>

              <Typography
                variant="h3"
                fontWeight={800}
                sx={{ mt: 1 }}
              >
                {stats?.adherencePercentage ?? 0}%
              </Typography>

              <Box
                sx={{
                  mt: 2,
                  height: 10,
                  borderRadius: 10,
                  backgroundColor: "action.hover",
                  overflow: "hidden",
                }}
              >
                <Box
                  sx={{
                    width: `${Math.min(
                      100,
                      Math.max(0, stats?.adherencePercentage ?? 0)
                    )}%`,
                    height: "100%",
                    borderRadius: 10,
                    backgroundColor: "success.main",
                    transition: "width 300ms ease",
                  }}
                />
              </Box>

              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ display: "block", mt: 1 }}
              >
                Completed workouts divided by non-cancelled scheduled
                workouts in the selected period.
              </Typography>
            </CardContent>
          </Card>

          {/* Statistics */}
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: {
                xs: "repeat(2, minmax(0, 1fr))",
                md: "repeat(4, minmax(0, 1fr))",
              },
              gap: 2,
              mb: 4,
            }}
          >
            {statCards.map((card) => (
              <Card
                key={card.label}
                elevation={0}
                sx={{
                  border: "1px solid",
                  borderColor: "divider",
                  borderRadius: 3,
                }}
              >
                <CardContent>
                  <Typography
                    color="text.secondary"
                    variant="body2"
                  >
                    {card.label}
                  </Typography>

                  <Typography
                    variant="h4"
                    fontWeight={700}
                    sx={{ my: 1 }}
                  >
                    {card.value}
                  </Typography>

                  <Typography
                    variant="caption"
                    color="text.secondary"
                  >
                    {card.description}
                  </Typography>
                </CardContent>
              </Card>
            ))}
          </Box>

          {/* Completed workout history */}
          <Box>
            <Typography variant="h6" fontWeight={700} sx={{ mb: 2 }}>
              Completed Workout History
            </Typography>

            {history.length === 0 ? (
              <Card
                elevation={0}
                sx={{
                  border: "1px solid",
                  borderColor: "divider",
                  borderRadius: 3,
                }}
              >
                <CardContent sx={{ textAlign: "center", py: 4 }}>
                  <Typography fontWeight={600}>
                    No completed workouts yet.
                  </Typography>

                  <Typography
                    color="text.secondary"
                    variant="body2"
                    sx={{ mt: 1 }}
                  >
                    Complete a workout to see it in your history.
                  </Typography>
                </CardContent>
              </Card>
            ) : (
              <Stack spacing={1.5}>
                {history.map((plan) => (
                  <Card
                    key={plan.id}
                    elevation={0}
                    sx={{
                      border: "1px solid",
                      borderColor: "divider",
                      borderRadius: 2,
                    }}
                  >
                    <CardContent>
                      <Stack
                        direction={{ xs: "column", sm: "row" }}
                        justifyContent="space-between"
                        alignItems={{ xs: "flex-start", sm: "center" }}
                        spacing={1}
                      >
                        <Box>
                          <Typography fontWeight={700}>
                            {plan.title}
                          </Typography>

                          <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{ mt: 0.5 }}
                          >
                            Scheduled: {formatDate(plan.scheduledAt)}
                          </Typography>

                          {plan.completedAt && (
                            <Typography
                              variant="body2"
                              color="text.secondary"
                              sx={{ mt: 0.5 }}
                            >
                              Completed: {formatDate(plan.completedAt)}
                            </Typography>
                          )}
                        </Box>

                        <Chip
                          label="COMPLETED"
                          color="success"
                          size="small"
                        />
                      </Stack>

                      {plan.exercises?.length > 0 && (
                        <Typography
                          variant="caption"
                          color="text.secondary"
                          sx={{ display: "block", mt: 1 }}
                        >
                          {plan.exercises.length} exercises
                        </Typography>
                      )}
                    </CardContent>
                  </Card>
                ))}
              </Stack>
            )}
          </Box>
        </>
      )}
    </Box>
  );
};

export default WorkoutAdherence;

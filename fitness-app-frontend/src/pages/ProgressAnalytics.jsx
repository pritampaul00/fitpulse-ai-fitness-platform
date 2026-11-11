import { useEffect, useMemo, useState } from "react";

import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    CircularProgress,
    Stack,
    ToggleButton,
    ToggleButtonGroup,
    Typography,
} from "@mui/material";

import { getActivities } from "../services/api";

import "./ProgressAnalytics.css";

const METRICS = {
    workouts: {
        label: "Workouts",
        color: "#9b7afa",
        unit: "",
    },
    duration: {
        label: "Duration",
        color: "#4ecba0",
        unit: " min",
    },
    calories: {
        label: "Calories",
        color: "#f3a65b",
        unit: " kcal",
    },
};

const getActivityDate = (activity) => {
    const value = activity.startTime || activity.createdAt;

    if (!value) return null;

    const date = new Date(value);

    return Number.isNaN(date.getTime()) ? null : date;
};

const startOfDay = (date) =>
    new Date(date.getFullYear(), date.getMonth(), date.getDate());

const dateKey = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
};

const monthKey = (date) =>
    `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;

const formatNumber = (value) =>
    Number(value || 0).toLocaleString("en-IN", {
        maximumFractionDigits: 1,
    });

const ProgressAnalytics = () => {
    const [activities, setActivities] = useState([]);
    const [period, setPeriod] = useState("weekly");
    const [metric, setMetric] = useState("workouts");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let cancelled = false;

        const loadActivities = async () => {
            try {
                setLoading(true);
                setError("");

                const response = await getActivities();

                if (!cancelled) {
                    setActivities(
                        Array.isArray(response.data) ? response.data : []
                    );
                }
            } catch (err) {
                if (!cancelled) {
                    setError(
                        err.response?.data?.message ||
                        "Unable to load progress analytics."
                    );
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        };

        loadActivities();

        return () => {
            cancelled = true;
        };
    }, []);

    const chartData = useMemo(() => {
        const today = startOfDay(new Date());

        if (period === "weekly") {
            const days = [];

            for (let index = 6; index >= 0; index--) {
                const date = new Date(today);
                date.setDate(today.getDate() - index);

                days.push({
                    key: dateKey(date),
                    label: date.toLocaleDateString("en-IN", {
                        weekday: "short",
                    }),
                    workouts: 0,
                    duration: 0,
                    calories: 0,
                });
            }

            const lookup = new Map(
                days.map((day) => [day.key, day])
            );

            activities.forEach((activity) => {
                const date = getActivityDate(activity);
                if (!date) return;

                const bucket = lookup.get(dateKey(date));
                if (!bucket) return;

                bucket.workouts += 1;
                bucket.duration += Number(activity.duration || 0);
                bucket.calories += Number(activity.caloriesBurned || 0);
            });

            return days;
        }

        const months = [];

        for (let index = 5; index >= 0; index--) {
            const date = new Date(
                today.getFullYear(),
                today.getMonth() - index,
                1
            );

            months.push({
                key: monthKey(date),
                label: date.toLocaleDateString("en-IN", {
                    month: "short",
                    year: "2-digit",
                }),
                workouts: 0,
                duration: 0,
                calories: 0,
            });
        }

        const lookup = new Map(
            months.map((month) => [month.key, month])
        );

        activities.forEach((activity) => {
            const date = getActivityDate(activity);
            if (!date) return;

            const bucket = lookup.get(monthKey(date));
            if (!bucket) return;

            bucket.workouts += 1;
            bucket.duration += Number(activity.duration || 0);
            bucket.calories += Number(activity.caloriesBurned || 0);
        });

        return months;
    }, [activities, period]);

    const totals = useMemo(() => {
        return chartData.reduce(
            (result, item) => ({
                workouts: result.workouts + item.workouts,
                duration: result.duration + item.duration,
                calories: result.calories + item.calories,
            }),
            {
                workouts: 0,
                duration: 0,
                calories: 0,
            }
        );
    }, [chartData]);

    const maximum = Math.max(
        1,
        ...chartData.map((item) => item[metric])
    );

    const selectedMetric = METRICS[metric];

    const periodLabel =
        period === "weekly" ? "Last 7 days" : "Last 6 months";

    if (loading) {
        return (
            <Box className="analytics-loading">
                <CircularProgress />
                <Typography>Loading your analytics...</Typography>
            </Box>
        );
    }

    return (
        <Box className="progress-analytics">
            <Box className="analytics-heading">
                <Box>
                    <Typography className="analytics-eyebrow">
                        PERFORMANCE OVERVIEW
                    </Typography>

                    <Typography variant="h5">
                        Progress Analytics
                    </Typography>

                    <Typography className="analytics-subtitle">
                        Understand your training consistency and activity trends.
                    </Typography>
                </Box>

                <ToggleButtonGroup
                    exclusive
                    size="small"
                    value={period}
                    onChange={(event, value) => {
                        if (value) setPeriod(value);
                    }}
                    aria-label="Analytics period"
                >
                    <ToggleButton value="weekly">
                        Weekly
                    </ToggleButton>

                    <ToggleButton value="monthly">
                        Monthly
                    </ToggleButton>
                </ToggleButtonGroup>
            </Box>

            {error && (
                <Alert severity="error">
                    {error}
                </Alert>
            )}

            <Box className="analytics-summary-grid">
                <Card className="analytics-summary-card">
                    <CardContent>
                        <Typography>Workouts</Typography>
                        <Typography variant="h4">
                            {formatNumber(totals.workouts)}
                        </Typography>
                        <Typography className="analytics-card-caption">
                            {periodLabel}
                        </Typography>
                    </CardContent>
                </Card>

                <Card className="analytics-summary-card">
                    <CardContent>
                        <Typography>Total duration</Typography>
                        <Typography variant="h4">
                            {formatNumber(totals.duration)}
                            <span> min</span>
                        </Typography>
                        <Typography className="analytics-card-caption">
                            Recorded workout minutes
                        </Typography>
                    </CardContent>
                </Card>

                <Card className="analytics-summary-card">
                    <CardContent>
                        <Typography>Calories burned</Typography>
                        <Typography variant="h4">
                            {formatNumber(totals.calories)}
                            <span> kcal</span>
                        </Typography>
                        <Typography className="analytics-card-caption">
                            Recorded calorie values
                        </Typography>
                    </CardContent>
                </Card>
            </Box>

            <Card className="analytics-chart-card">
                <CardContent>
                    <Box className="analytics-chart-heading">
                        <Box>
                            <Typography variant="h6">
                                Activity trends
                            </Typography>

                            <Typography className="analytics-subtitle">
                                {periodLabel}
                            </Typography>
                        </Box>

                        <Stack
                            direction="row"
                            spacing={1}
                            className="analytics-metric-select"
                        >
                            {Object.entries(METRICS).map(([key, item]) => (
                                <Button
                                    key={key}
                                    size="small"
                                    variant={
                                        metric === key
                                            ? "contained"
                                            : "outlined"
                                    }
                                    onClick={() => setMetric(key)}
                                >
                                    {item.label}
                                </Button>
                            ))}
                        </Stack>
                    </Box>

                    {chartData.every((item) => item[metric] === 0) ? (
                        <Box className="analytics-no-data">
                            <Typography>
                                No {selectedMetric.label.toLowerCase()} recorded
                                during this period.
                            </Typography>
                        </Box>
                    ) : (
                        <Box className="analytics-chart">
                            <Box className="analytics-y-axis">
                                <span>{formatNumber(maximum)}</span>
                                <span>{formatNumber(maximum / 2)}</span>
                                <span>0</span>
                            </Box>

                            <Box className="analytics-bars-area">
                                <Box className="analytics-grid-lines">
                                    <span />
                                    <span />
                                    <span />
                                </Box>

                                <Box className="analytics-bars">
                                    {chartData.map((item) => {
                                        const value = item[metric];
                                        const height =
                                            value === 0
                                                ? 0
                                                : Math.max(
                                                    3,
                                                    (value / maximum) * 100
                                                );

                                        return (
                                            <Box
                                                className="analytics-bar-column"
                                                key={item.key}
                                                title={`${item.label}: ${formatNumber(value)}${selectedMetric.unit}`}
                                            >
                                                <Typography className="analytics-bar-value">
                                                    {value > 0
                                                        ? formatNumber(value)
                                                        : ""}
                                                </Typography>

                                                <Box className="analytics-bar-track">
                                                    <Box
                                                        className="analytics-bar"
                                                        sx={{
                                                            height: `${height}%`,
                                                            backgroundColor:
                                                                selectedMetric.color,
                                                        }}
                                                    />
                                                </Box>

                                                <Typography className="analytics-bar-label">
                                                    {item.label}
                                                </Typography>
                                            </Box>
                                        );
                                    })}
                                </Box>
                            </Box>
                        </Box>
                    )}

                    <Typography className="analytics-footnote">
                        Values reflect activities recorded in FitPulse.
                    </Typography>
                </CardContent>
            </Card>
        </Box>
    );
};

export default ProgressAnalytics;

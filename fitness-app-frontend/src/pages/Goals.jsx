import { useCallback, useEffect, useState } from "react";

import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Chip,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    Grid,
    IconButton,
    InputLabel,
    LinearProgress,
    MenuItem,
    Select,
    Stack,
    TextField,
    Typography,
} from "@mui/material";

import {
    Add,
    DeleteOutline,
    EditOutlined,
    FlagOutlined,
    Refresh,
} from "@mui/icons-material";

import {
    createGoal,
    deleteGoal,
    getGoals,
    updateGoal,
} from "../services/api";

import ProgressAnalytics from "./ProgressAnalytics";

import "./Goals.css";

import GoalInsights from "../components/goals/GoalInsights";

const initialForm = {
    title: "",
    description: "",
    goalType: "WORKOUT_COUNT",
    targetValue: "",
    startDate: new Date().toISOString().slice(0, 10),
    endDate: "",
};

const goalTypeLabels = {
    WORKOUT_COUNT: "Workouts",
    DURATION_MINUTES: "Minutes",
    CALORIES: "Calories",
};

const statusColors = {
    ACTIVE: "primary",
    COMPLETED: "success",
    EXPIRED: "default",
};

const formatValue = (value) =>
    Number(value || 0).toLocaleString("en-IN", {
        maximumFractionDigits: 2,
    });

const Goals = () => {
    const [goals, setGoals] = useState([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const [filter, setFilter] = useState("ALL");
    const [dialogOpen, setDialogOpen] = useState(false);
    const [editingGoal, setEditingGoal] = useState(null);
    const [form, setForm] = useState(initialForm);
    const [deleteTarget, setDeleteTarget] = useState(null);

    const loadGoals = useCallback(async () => {
        try {
            setLoading(true);
            setError("");

            const response = await getGoals();
            setGoals(Array.isArray(response.data) ? response.data : []);
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to load your goals. Please try again."
            );
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadGoals();
    }, [loadGoals]);

    const openCreateDialog = () => {
        const startDate = new Date();
        const endDate = new Date();
        endDate.setDate(endDate.getDate() + 30);

        setEditingGoal(null);
        setForm({
            ...initialForm,
            startDate: startDate.toISOString().slice(0, 10),
            endDate: endDate.toISOString().slice(0, 10),
        });
        setError("");
        setDialogOpen(true);
    };

    const openEditDialog = (goal) => {
        setEditingGoal(goal);

        setForm({
            title: goal.title || "",
            description: goal.description || "",
            goalType: goal.goalType,
            targetValue: String(goal.targetValue ?? ""),
            startDate: goal.startDate || "",
            endDate: goal.endDate || "",
        });

        setError("");
        setDialogOpen(true);
    };

    const closeDialog = () => {
        if (saving) return;

        setDialogOpen(false);
        setEditingGoal(null);
    };

    const handleFormChange = (event) => {
        const { name, value } = event.target;

        setForm((previous) => ({
            ...previous,
            [name]: value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        const targetValue = Number(form.targetValue);

        if (!form.title.trim()) {
            setError("Please enter a goal title.");
            return;
        }

        if (!Number.isFinite(targetValue) || targetValue <= 0) {
            setError("Please enter a valid target greater than zero.");
            return;
        }

        if (!form.startDate || !form.endDate) {
            setError("Please select both dates.");
            return;
        }

        if (form.endDate < form.startDate) {
            setError("End date cannot be before start date.");
            return;
        }

        const payload = {
            ...form,
            title: form.title.trim(),
            description: form.description.trim(),
            targetValue,
        };

        try {
            setSaving(true);
            setError("");

            if (editingGoal) {
                await updateGoal(editingGoal.id, payload);
                setSuccess("Goal updated successfully.");
            } else {
                await createGoal(payload);
                setSuccess("Goal created successfully.");
            }

            setDialogOpen(false);
            setEditingGoal(null);

            await loadGoals();
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to save your goal."
            );
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async () => {
        if (!deleteTarget) return;

        try {
            await deleteGoal(deleteTarget.id);

            setGoals((previous) =>
                previous.filter((goal) => goal.id !== deleteTarget.id)
            );

            setSuccess("Goal deleted successfully.");
            setDeleteTarget(null);
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to delete this goal."
            );
            setDeleteTarget(null);
        }
    };

    const filteredGoals = goals.filter((goal) => {
        return filter === "ALL" || goal.status === filter;
    });

    const activeCount = goals.filter(
        (goal) => goal.status === "ACTIVE"
    ).length;

    const completedCount = goals.filter(
        (goal) => goal.status === "COMPLETED"
    ).length;

    const expiredCount = goals.filter(
        (goal) => goal.status === "EXPIRED"
    ).length;

    const renderGoalCard = (goal) => {
        const progress = Math.min(
            100,
            Math.max(0, Number(goal.progressPercentage || 0))
        );

        const unit = goalTypeLabels[goal.goalType] || "";

        return (
            <Card className="goal-card" key={goal.id}>
                <CardContent>
                    <Box className="goal-card-header">
                        <Box>
                            <Typography className="goal-category">
                                {goalTypeLabels[goal.goalType] || goal.goalType}
                            </Typography>

                            <Typography variant="h6" className="goal-title">
                                {goal.title}
                            </Typography>
                        </Box>

                        <Chip
                            label={goal.status}
                            color={statusColors[goal.status] || "default"}
                            size="small"
                        />
                    </Box>

                    {goal.description && (
                        <Typography className="goal-description">
                            {goal.description}
                        </Typography>
                    )}

                    <Box className="goal-progress-section">
                        <Box className="goal-progress-labels">
                            <Typography variant="h4">
                                {progress.toFixed(1)}%
                            </Typography>

                            <Typography className="goal-target">
                                {formatValue(goal.currentValue)} /{" "}
                                {formatValue(goal.targetValue)} {unit}
                            </Typography>
                        </Box>

                        <LinearProgress
                            variant="determinate"
                            value={progress}
                            className="goal-progress-bar"
                        />
                    </Box>

                    <Box className="goal-dates">
                        <Typography>
                            Starts: {goal.startDate}
                        </Typography>

                        <Typography>
                            Deadline: {goal.endDate}
                        </Typography>
                    </Box>

                    <Box className="goal-actions">
                        <Button
                            size="small"
                            startIcon={<EditOutlined />}
                            onClick={() => openEditDialog(goal)}
                            disabled={goal.status === "COMPLETED"}
                        >
                            Edit
                        </Button>

                        <Button
                            size="small"
                            color="error"
                            startIcon={<DeleteOutline />}
                            onClick={() => setDeleteTarget(goal)}
                        >
                            Delete
                        </Button>
                    </Box>
                </CardContent>
            </Card>
        );
    };

    return (
        <Box className="goals-page">
            <Box className="goals-heading">
                <Box>
                    <Typography className="goals-eyebrow">
                        YOUR FITNESS JOURNEY
                    </Typography>

                    <Typography variant="h4">
                        Fitness Goals
                    </Typography>

                    <Typography className="goals-subtitle">
                        Set targets, track your progress, and stay consistent.
                    </Typography>
                </Box>

                <Stack direction="row" spacing={1}>
                    <Button
                        variant="outlined"
                        startIcon={<Refresh />}
                        onClick={loadGoals}
                        disabled={loading}
                    >
                        Refresh
                    </Button>

                    <Button
                        variant="contained"
                        startIcon={<Add />}
                        onClick={openCreateDialog}
                    >
                        Create Goal
                    </Button>
                </Stack>
            </Box>

            {success && (
                <Alert
                    severity="success"
                    onClose={() => setSuccess("")}
                >
                    {success}
                </Alert>
            )}

            {error && (
                <Alert
                    severity="error"
                    onClose={() => setError("")}
                >
                    {error}
                </Alert>
            )}

            <Grid container spacing={2} className="goal-stat-grid">
                <Grid item xs={12} sm={4}>
                    <Card className="goal-stat-card">
                        <CardContent>
                            <Typography>Active goals</Typography>
                            <Typography variant="h3">
                                {activeCount}
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={4}>
                    <Card className="goal-stat-card">
                        <CardContent>
                            <Typography>Completed goals</Typography>
                            <Typography variant="h3">
                                {completedCount}
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={4}>
                    <Card className="goal-stat-card">
                        <CardContent>
                            <Typography>Expired goals</Typography>
                            <Typography variant="h3">
                                {expiredCount}
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            <ProgressAnalytics />

            <GoalInsights />
            <Box className="goals-list-heading">
                <Box>
                    <Typography variant="h5">
                        Your goals
                    </Typography>
                    <Typography className="goals-subtitle">
                        Progress updates automatically from your workouts.
                    </Typography>
                </Box>

                <FormControl size="small" className="goal-filter">
                    <InputLabel>Status</InputLabel>
                    <Select
                        value={filter}
                        label="Status"
                        onChange={(event) => setFilter(event.target.value)}
                    >
                        <MenuItem value="ALL">All goals</MenuItem>
                        <MenuItem value="ACTIVE">Active</MenuItem>
                        <MenuItem value="COMPLETED">Completed</MenuItem>
                        <MenuItem value="EXPIRED">Expired</MenuItem>
                    </Select>
                </FormControl>
            </Box>

            {loading ? (
                <Box className="goals-loading">
                    <CircularProgress />
                    <Typography>Loading your goals...</Typography>
                </Box>
            ) : filteredGoals.length === 0 ? (
                <Box className="goals-empty">
                    <FlagOutlined className="goals-empty-icon" />

                    <Typography variant="h6">
                        {filter === "ALL"
                            ? "No fitness goals yet"
                            : `No ${filter.toLowerCase()} goals`}
                    </Typography>

                    <Typography>
                        Create your first goal and start tracking your progress.
                    </Typography>

                    {filter === "ALL" && (
                        <Button
                            variant="contained"
                            startIcon={<Add />}
                            onClick={openCreateDialog}
                        >
                            Create your first goal
                        </Button>
                    )}
                </Box>
            ) : (
                <Grid container spacing={2}>
                    {filteredGoals.map((goal) => (
                        <Grid item xs={12} md={6} key={goal.id}>
                            {renderGoalCard(goal)}
                        </Grid>
                    ))}
                </Grid>
            )}

            <Dialog
                open={dialogOpen}
                onClose={closeDialog}
                fullWidth
                maxWidth="sm"
            >
                <Box component="form" onSubmit={handleSubmit}>
                    <DialogTitle>
                        {editingGoal ? "Edit fitness goal" : "Create fitness goal"}
                    </DialogTitle>

                    <DialogContent>
                        <Stack spacing={2} sx={{ mt: 1 }}>
                            <TextField
                                label="Goal title"
                                name="title"
                                value={form.title}
                                onChange={handleFormChange}
                                required
                                fullWidth
                                inputProps={{ maxLength: 100 }}
                                placeholder="Complete 12 workouts"
                            />

                            <TextField
                                label="Description"
                                name="description"
                                value={form.description}
                                onChange={handleFormChange}
                                multiline
                                rows={2}
                                fullWidth
                                inputProps={{ maxLength: 500 }}
                            />

                            <FormControl fullWidth required>
                                <InputLabel>Goal type</InputLabel>

                                <Select
                                    name="goalType"
                                    value={form.goalType}
                                    label="Goal type"
                                    onChange={handleFormChange}
                                >
                                    <MenuItem value="WORKOUT_COUNT">
                                        Number of workouts
                                    </MenuItem>

                                    <MenuItem value="DURATION_MINUTES">
                                        Workout duration in minutes
                                    </MenuItem>

                                    <MenuItem value="CALORIES">
                                        Calories burned
                                    </MenuItem>
                                </Select>
                            </FormControl>

                            <TextField
                                label={`Target (${goalTypeLabels[form.goalType]})`}
                                name="targetValue"
                                type="number"
                                value={form.targetValue}
                                onChange={handleFormChange}
                                required
                                fullWidth
                                inputProps={{ min: 1, step: "any" }}
                            />

                            <TextField
                                label="Start date"
                                name="startDate"
                                type="date"
                                value={form.startDate}
                                onChange={handleFormChange}
                                required
                                fullWidth
                                InputLabelProps={{ shrink: true }}
                            />

                            <TextField
                                label="Deadline"
                                name="endDate"
                                type="date"
                                value={form.endDate}
                                onChange={handleFormChange}
                                required
                                fullWidth
                                InputLabelProps={{ shrink: true }}
                            />
                        </Stack>
                    </DialogContent>

                    <DialogActions sx={{ p: 2 }}>
                        <Button onClick={closeDialog} disabled={saving}>
                            Cancel
                        </Button>

                        <Button
                            type="submit"
                            variant="contained"
                            disabled={saving}
                        >
                            {saving
                                ? "Saving..."
                                : editingGoal
                                    ? "Save changes"
                                    : "Create goal"}
                        </Button>
                    </DialogActions>
                </Box>
            </Dialog>

            <Dialog
                open={Boolean(deleteTarget)}
                onClose={() => setDeleteTarget(null)}
            >
                <DialogTitle>Delete this goal?</DialogTitle>

                <DialogContent>
                    <Typography>
                        This will permanently delete
                        {" "}{deleteTarget?.title}.
                    </Typography>
                </DialogContent>

                <DialogActions>
                    <Button onClick={() => setDeleteTarget(null)}>
                        Cancel
                    </Button>

                    <Button
                        color="error"
                        variant="contained"
                        onClick={handleDelete}
                    >
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};

export default Goals;

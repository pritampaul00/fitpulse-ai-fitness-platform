import { useState } from "react";

import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    CircularProgress,
    Typography,
} from "@mui/material";

import { AutoAwesome, Refresh } from "@mui/icons-material";

import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

import { generateGoalInsights } from "../../services/api";

import "./GoalInsights.css";

const GoalInsights = () => {
    const [insights, setInsights] = useState("");
    const [generatedAt, setGeneratedAt] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleGenerate = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await generateGoalInsights();

            setInsights(response.data.insights || "");
            setGeneratedAt(response.data.generatedAt || null);
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to generate AI goal insights. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <Card className="goal-insights-card">
            <CardContent>
                <Box className="goal-insights-header">
                    <Box className="goal-insights-title">
                        <Box className="goal-insights-icon">
                            <AutoAwesome />
                        </Box>

                        <Box>
                            <Typography variant="h6">
                                AI Goal Insights
                            </Typography>

                            <Typography className="goal-insights-subtitle">
                                Personalized guidance based on your recorded
                                workouts and fitness goals.
                            </Typography>
                        </Box>
                    </Box>

                    <Button
                        variant="contained"
                        startIcon={
                            loading
                                ? <CircularProgress size={16} color="inherit" />
                                : insights
                                    ? <Refresh />
                                    : <AutoAwesome />
                        }
                        onClick={handleGenerate}
                        disabled={loading}
                    >
                        {loading
                            ? "Analyzing..."
                            : insights
                                ? "Refresh insights"
                                : "Generate insights"}
                    </Button>
                </Box>

                {error && (
                    <Alert severity="error" sx={{ mt: 2 }}>
                        {error}
                    </Alert>
                )}

                {loading && (
                    <Box className="goal-insights-loading">
                        <CircularProgress size={25} />
                        <Typography>
                            Analyzing your goals and workout history...
                        </Typography>
                    </Box>
                )}

                {!loading && !insights && !error && (
                    <Box className="goal-insights-empty">
                        <Typography>
                            Get an AI assessment of your progress, identify
                            goals that need attention, and receive a practical
                            7-day action plan.
                        </Typography>
                    </Box>
                )}

                {!loading && insights && (
                    <Box className="goal-insights-result">
                        <ReactMarkdown remarkPlugins={[remarkGfm]}>
                            {insights}
                        </ReactMarkdown>

                        {generatedAt && (
                            <Typography className="goal-insights-timestamp">
                                Generated: {new Date(generatedAt).toLocaleString()}
                            </Typography>
                        )}
                    </Box>
                )}
            </CardContent>
        </Card>
    );
};

export default GoalInsights;

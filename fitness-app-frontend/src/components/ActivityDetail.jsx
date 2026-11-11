import React, { useEffect, useState } from "react";
import { useParams } from "react-router";

import {
    getActivityById,
    getActivityRecommendation,
    regenerateActivityRecommendation
} from "../services/api";

import {
    Box,
    Card,
    CardContent,
    Divider,
    Typography,
    CircularProgress,
    Alert,
    List,
    ListItem,
    ListItemText,
    Chip,
    Button
} from "@mui/material";

const MAX_ATTEMPTS = 20;
const RETRY_DELAY = 3000;

const ActivityDetail = () => {
    const { id } = useParams();

    const [activity, setActivity] = useState(null);
    const [recommendation, setRecommendation] = useState(null);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [recommendationError, setRecommendationError] = useState("");

    const [regenerating, setRegenerating] = useState(false);
    const [regenerationMessage, setRegenerationMessage] = useState("");

    // Fetch activity details.
    useEffect(() => {
        let cancelled = false;

        const fetchActivity = async () => {
            try {
                setLoading(true);
                setError("");
                setActivity(null);

                const response = await getActivityById(id);

                if (!cancelled) {
                    setActivity(response.data);
                }
            } catch (err) {
                console.error("Activity details error:", err);

                if (!cancelled) {
                    setError(
                        err.response?.status === 404
                            ? "Activity not found."
                            : "Unable to load activity details. Please try again."
                    );
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        };

        if (id) {
            fetchActivity();
        }

        return () => {
            cancelled = true;
        };
    }, [id]);

    // Automatically poll for the initial AI recommendation.
    useEffect(() => {
        if (!id) return;

        let cancelled = false;
        let timer = null;
        let attempts = 0;

        setRecommendation(null);
        setRecommendationError("Waiting for AI recommendation...");

        const fetchRecommendation = async () => {
            try {
                const response = await getActivityRecommendation(id);

                if (cancelled) return;

                setRecommendation(response.data);
                setRecommendationError("");
            } catch (err) {
                if (cancelled) return;

                const status = err.response?.status;

                if (status === 404 && attempts < MAX_ATTEMPTS) {
                    attempts++;

                    setRecommendationError(
                        `Your AI recommendation is being generated. Checking again (${attempts}/${MAX_ATTEMPTS})...`
                    );

                    timer = setTimeout(
                        fetchRecommendation,
                        RETRY_DELAY
                    );
                } else {
                    console.error("Recommendation error:", err);

                    setRecommendationError(
                        status === 404
                            ? "Your recommendation is taking longer than expected. Please try again later."
                            : "Unable to load the AI recommendation."
                    );
                }
            }
        };

        fetchRecommendation();

        return () => {
            cancelled = true;

            if (timer) {
                clearTimeout(timer);
            }
        };
    }, [id]);

    // Regenerate an existing recommendation.
    const handleRegenerateRecommendation = async () => {
        if (regenerating) return;

        setRegenerating(true);
        setRegenerationMessage(
            "Sending your activity to the AI Coach..."
        );

        setRecommendationError("");

        const previousCreatedAt = recommendation?.createdAt
            ? new Date(recommendation.createdAt).getTime()
            : 0;

        try {
            // Request regeneration through Activity Service.
            await regenerateActivityRecommendation(id);

            setRegenerationMessage(
                "AI is analyzing your activity. Please wait..."
            );

            let updated = false;

            for (let attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                await new Promise(resolve =>
                    setTimeout(resolve, RETRY_DELAY)
                );

                const response = await getActivityRecommendation(id);

                const updatedRecommendation = response.data;

                const newCreatedAt = updatedRecommendation.createdAt
                    ? new Date(
                        updatedRecommendation.createdAt
                    ).getTime()
                    : 0;

                if (
                    updatedRecommendation.id &&
                    newCreatedAt > previousCreatedAt
                ) {
                    setRecommendation(updatedRecommendation);
                    setRecommendationError("");

                    setRegenerationMessage(
                        "Your AI recommendation has been regenerated successfully."
                    );

                    updated = true;
                    break;
                }
            }

            if (!updated) {
                setRegenerationMessage(
                    "Generation is taking longer than expected. Your previous recommendation is still available. Please check again shortly."
                );
            }

        } catch (err) {
            console.error(
                "Recommendation regeneration failed:",
                err
            );

            setRegenerationMessage(
                err.response?.data?.message ||
                "Unable to regenerate the recommendation. Please try again."
            );
        } finally {
            setRegenerating(false);
        }
    };

    // Render recommendation lists.
    const renderList = (items) => {
        if (!items || items.length === 0) {
            return (
                <Typography color="text.secondary">
                    No information available.
                </Typography>
            );
        }

        return (
            <List disablePadding>
                {items.map((item, index) => (
                    <ListItem
                        key={index}
                        alignItems="flex-start"
                    >
                        <ListItemText
                            primary={`${index + 1}. ${item}`}
                            primaryTypographyProps={{
                                sx: {
                                    lineHeight: 1.7
                                }
                            }}
                        />
                    </ListItem>
                ))}
            </List>
        );
    };

    // Activity loading state.
    if (loading) {
        return (
            <Box
                sx={{
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    minHeight: "60vh"
                }}
            >
                <CircularProgress />
            </Box>
        );
    }

    // Activity error state.
    if (error) {
        return (
            <Box
                sx={{
                    maxWidth: 900,
                    mx: "auto",
                    p: 3
                }}
            >
                <Alert severity="error">
                    {error}
                </Alert>
            </Box>
        );
    }

    if (!activity) return null;

    return (
        <Box
            sx={{
                maxWidth: 900,
                mx: "auto",
                p: {
                    xs: 2,
                    md: 3
                }
            }}
        >
            {/* Activity Details */}
            <Card
                sx={{
                    mb: 3,
                    borderRadius: 3
                }}
            >
                <CardContent sx={{ p: 3 }}>
                    <Typography
                        variant="h5"
                        fontWeight="bold"
                        gutterBottom
                    >
                        Activity Details
                    </Typography>

                    <Chip
                        label={activity.type}
                        color="primary"
                        sx={{ mb: 2 }}
                    />

                    <Divider sx={{ mb: 2 }} />

                    <Typography paragraph>
                        <strong>Duration:</strong>{" "}
                        {activity.duration} minutes
                    </Typography>

                    <Typography paragraph>
                        <strong>Calories Burned:</strong>{" "}
                        {activity.caloriesBurned} kcal
                    </Typography>

                    <Typography>
                        <strong>Date:</strong>{" "}
                        {activity.createdAt
                            ? new Date(
                                activity.createdAt
                            ).toLocaleString()
                            : "Not available"}
                    </Typography>
                </CardContent>
            </Card>

            {/* AI Recommendation */}
            <Card sx={{ borderRadius: 3 }}>
                <CardContent sx={{ p: 3 }}>

                    {/* Recommendation Header */}
                    <Box
                        sx={{
                            display: "flex",
                            justifyContent: "space-between",
                            alignItems: "center",
                            flexWrap: "wrap",
                            gap: 2,
                            mb: 2
                        }}
                    >
                        <Typography
                            variant="h5"
                            fontWeight="bold"
                        >
                            AI Fitness Recommendation
                        </Typography>

                        <Button
                            variant="contained"
                            onClick={handleRegenerateRecommendation}
                            disabled={regenerating}
                        >
                            {regenerating ? (
                                <>
                                    <CircularProgress
                                        size={16}
                                        color="inherit"
                                        sx={{ mr: 1 }}
                                    />
                                    Regenerating...
                                </>
                            ) : (
                                "Regenerate Recommendation"
                            )}
                        </Button>
                    </Box>

                    {/* Regeneration Status */}
                    {regenerationMessage && (
                        <Alert
                            severity={
                                regenerating
                                    ? "info"
                                    : regenerationMessage.includes(
                                        "successfully"
                                    )
                                        ? "success"
                                        : "warning"
                            }
                            sx={{ mb: 2 }}
                        >
                            {regenerationMessage}
                        </Alert>
                    )}

                    {/* Initial Recommendation Loading */}
                    {!recommendation && recommendationError && (
                        <Alert
                            severity="info"
                            sx={{ mt: 2 }}
                            icon={
                                recommendationError.includes(
                                    "taking longer"
                                ) ||
                                recommendationError.includes("Unable")
                                    ? undefined
                                    : <CircularProgress size={18} />
                            }
                        >
                            {recommendationError}
                        </Alert>
                    )}

                    {/* Recommendation Content */}
                    {recommendation && (
                        <>
                            <Chip
                                label={recommendation.activityType}
                                color="success"
                                sx={{ mb: 2 }}
                            />

                            <Divider sx={{ mb: 3 }} />

                            {/* Analysis */}
                            <Typography
                                variant="h6"
                                fontWeight="bold"
                                gutterBottom
                            >
                                Analysis
                            </Typography>

                            <Typography
                                paragraph
                                sx={{
                                    whiteSpace: "pre-line",
                                    lineHeight: 1.8
                                }}
                            >
                                {recommendation.recommendation}
                            </Typography>

                            <Divider sx={{ my: 3 }} />

                            {/* Improvements */}
                            <Typography
                                variant="h6"
                                fontWeight="bold"
                                gutterBottom
                            >
                                Areas for Improvement
                            </Typography>

                            {renderList(
                                recommendation.improvements
                            )}

                            <Divider sx={{ my: 3 }} />

                            {/* Training Suggestions */}
                            <Typography
                                variant="h6"
                                fontWeight="bold"
                                gutterBottom
                            >
                                Training Suggestions
                            </Typography>

                            {renderList(
                                recommendation.suggestions
                            )}

                            <Divider sx={{ my: 3 }} />

                            {/* Safety Guidelines */}
                            <Typography
                                variant="h6"
                                fontWeight="bold"
                                gutterBottom
                            >
                                Safety Guidelines
                            </Typography>

                            {renderList(
                                recommendation.safety
                            )}

                            <Divider sx={{ my: 3 }} />

                            {/* Generated Date */}
                            <Typography
                                variant="caption"
                                color="text.secondary"
                            >
                                AI Recommendation generated on:{" "}
                                {recommendation.createdAt
                                    ? new Date(
                                        recommendation.createdAt
                                    ).toLocaleString()
                                    : "Not available"}
                            </Typography>
                        </>
                    )}
                </CardContent>
            </Card>
        </Box>
    );
};

export default ActivityDetail;

import axios from "axios";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

const api = axios.create({
  baseURL: API_URL,
  timeout: 30000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

// Activity APIs

export const getActivities = () => api.get("/activities");

export const addActivity = (activity) => api.post("/activities", activity);

export const getActivityById = (id) => api.get(`/activities/${id}`);

export const getActivityRecommendation = (id) =>
  api.get(`/recommendations/activity/${id}`);

export const getActivityDetail = getActivityRecommendation;

export const regenerateActivityRecommendation = (activityId) =>
  api.post(`/activities/${activityId}/recommendation/regenerate`);

// AI Coach APIs

export const sendCoachMessage = (payload) => api.post("/coach/chat", payload);

export const getCoachConversations = () => api.get("/coach/conversations");

export const getCoachConversation = (conversationId) =>
  api.get(`/coach/conversations/${conversationId}`);

export const deleteCoachConversation = (conversationId) =>
  api.delete(`/coach/conversations/${conversationId}`);

// Fitness Goals APIs

export const getGoals = () => api.get("/goals");

export const getGoalById = (goalId) => api.get(`/goals/${goalId}`);

export const createGoal = (goal) => api.post("/goals", goal);

export const updateGoal = (goalId, goal) => api.put(`/goals/${goalId}`, goal);

export const deleteGoal = (goalId) => api.delete(`/goals/${goalId}`);

export const generateGoalInsights = () => api.post("/goal-insights");

// Workout Planner APIs

export const generateWorkoutPlan = async (payload) => {
  const response = await api.post("/ai/workout-plans/generate", payload);
  return response.data;
};

export const getWorkoutPlans = async () => {
  const response = await api.get("/workout-plans");
  return response.data;
};

export const getWorkoutPlan = async (planId) => {
  const response = await api.get(`/workout-plans/${planId}`);
  return response.data;
};

export const createWorkoutPlan = async (workoutPlan) => {
  const response = await api.post("/workout-plans", workoutPlan);
  return response.data;
};

export const updateWorkoutPlan = async (planId, workoutPlan) => {
  const response = await api.put(`/workout-plans/${planId}`, workoutPlan);
  return response.data;
};

export const deleteWorkoutPlan = async (planId) => {
  const response = await api.delete(`/workout-plans/${planId}`);
  return response.data;
};

export const completeWorkoutPlan = async (planId) => {
  const response = await api.post(`/workout-plans/${planId}/complete`);
  return response.data;
};

export const getWorkoutHistory = async () => {
  const response = await api.get("/workout-plans/history");
  return response.data;
};

export const getWorkoutAdherence = async (days = 30) => {
  const response = await api.get(`/workout-plans/adherence?days=${days}`);
  return response.data;
};

export default api;

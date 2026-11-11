import React from "react";
import ReactDOM from "react-dom/client";

import { Provider } from "react-redux";
import { ThemeProvider, createTheme } from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";

import { store } from "./store/store";
import App from "./App";

import { AuthProvider } from "react-oauth2-code-pkce";
import { authConfig } from "./authConfig";

import "./index.css";

const theme = createTheme({
    palette: {
        mode: "dark",
        primary: {
            main: "#7958ed",
        },
        secondary: {
            main: "#a17cf7",
        },
        background: {
            default: "#101116",
            paper: "#191a23",
        },
        text: {
            primary: "#f5f5f8",
            secondary: "#a3a3b2",
        },
    },
    typography: {
        fontFamily: 'Inter, "Segoe UI", Roboto, sans-serif',
    },
    shape: {
        borderRadius: 10,
    },
});

ReactDOM.createRoot(document.getElementById("root")).render(
    <React.StrictMode>
        <ThemeProvider theme={theme}>
            <CssBaseline />

            <AuthProvider
                authConfig={authConfig}
                loadingComponent={<div>Loading...</div>}
            >
                <Provider store={store}>
                    <App />
                </Provider>
            </AuthProvider>

        </ThemeProvider>
    </React.StrictMode>
);

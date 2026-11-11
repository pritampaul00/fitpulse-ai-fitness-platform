// import { useEffect, useRef, useState } from "react";

// import {
//   Avatar,
//   Box,
//   Button,
//   CircularProgress,
//   Divider,
//   IconButton,
//   Paper,
//   Snackbar,
//   TextField,
//   Tooltip,
//   Typography,
// } from "@mui/material";

// import ReactMarkdown from "react-markdown";
// import remarkGfm from "remark-gfm";

// import {
//   deleteCoachConversation,
//   getCoachConversation,
//   getCoachConversations,
//   sendCoachMessage,
// } from "../../services/api";

// import "./ChatCoach.css";

// const suggestions = [
//   "How can I improve my fitness?",
//   "Create a weekly workout routine for me.",
//   "How should I recover after an intense workout?",
//   "Analyze my recent fitness progress.",
// ];

// const ChatCoach = () => {
//   const [conversations, setConversations] = useState([]);
//   const [activeConversationId, setActiveConversationId] = useState(null);
//   const [messages, setMessages] = useState([]);
//   const [input, setInput] = useState("");
//   const [searchQuery, setSearchQuery] = useState("");

//   const [loading, setLoading] = useState(false);
//   const [historyLoading, setHistoryLoading] = useState(true);
//   const [error, setError] = useState("");
//   const [failedMessage, setFailedMessage] = useState("");
//   const [copyNotice, setCopyNotice] = useState(false);

//   const bottomRef = useRef(null);
//   const inputRef = useRef(null);

//   const filteredConversations = conversations.filter((conversation) => {
//     const title = conversation.title || "New conversation";
//     return title.toLowerCase().includes(searchQuery.trim().toLowerCase());
//   });

//   const loadConversations = async (showLoading = false) => {
//     try {
//       if (showLoading) setHistoryLoading(true);
//       const response = await getCoachConversations();
//       setConversations(response.data || []);
//     } catch (err) {
//       console.error("Failed to load conversations", err);
//       setError("Unable to load conversation history.");
//     } finally {
//       if (showLoading) setHistoryLoading(false);
//     }
//   };

//   useEffect(() => {
//     loadConversations(true);
//   }, []);

//   useEffect(() => {
//     bottomRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
//   }, [messages, loading]);

//   const startNewChat = () => {
//     if (loading || historyLoading) return;
//     setActiveConversationId(null);
//     setMessages([]);
//     setInput("");
//     setError("");
//     setFailedMessage("");
//     inputRef.current?.focus();
//   };

//   const openConversation = async (conversationId) => {
//     if (loading || historyLoading) return;
//     try {
//       setError("");
//       setFailedMessage("");
//       setHistoryLoading(true);
//       const response = await getCoachConversation(conversationId);
//       const conversation = response.data;
//       setActiveConversationId(conversation.id);
//       setMessages(conversation.messages || []);
//       setInput("");
//     } catch (err) {
//       console.error("Failed to open conversation", err);
//       setError(
//         err.response?.data?.message || "Unable to open this conversation.",
//       );
//     } finally {
//       setHistoryLoading(false);
//     }
//   };

//   const handleDeleteConversation = async (event, conversationId) => {
//     event.stopPropagation();
//     if (loading || historyLoading) return;
//     try {
//       await deleteCoachConversation(conversationId);
//       setConversations((previous) =>
//         previous.filter((item) => item.id !== conversationId),
//       );
//       if (activeConversationId === conversationId) {
//         setActiveConversationId(null);
//         setMessages([]);
//         setInput("");
//         setError("");
//         setFailedMessage("");
//       }
//     } catch (err) {
//       console.error("Failed to delete conversation", err);
//       setError(
//         err.response?.data?.message || "Could not delete this conversation.",
//       );
//     }
//   };

//   const handleCopyMessage = async (content) => {
//     try {
//       await navigator.clipboard.writeText(content);
//       setCopyNotice(true);
//     } catch (err) {
//       console.error("Failed to copy response:", err);
//       setError("Unable to copy the response. Please try again.");
//     }
//   };

//   const handleSendMessage = async (event, messageOverride = null) => {
//     event?.preventDefault();
//     const message = (messageOverride ?? input).trim();
//     if (!message || loading || historyLoading) return;

//     const previousMessages = [...messages];
//     const optimisticMessage = {
//       role: "user",
//       content: message,
//       timestamp: new Date().toISOString(),
//     };

//     setMessages((previous) => [...previous, optimisticMessage]);
//     setInput("");
//     setLoading(true);
//     setError("");
//     setFailedMessage("");

//     try {
//       const response = await sendCoachMessage({
//         conversationId: activeConversationId,
//         message,
//       });
//       const result = response.data;
//       setActiveConversationId(result.conversationId);
//       setMessages((previous) => [
//         ...previous,
//         {
//           role: "assistant",
//           content: result.reply,
//           timestamp: result.timestamp || new Date().toISOString(),
//           suggestions: Array.isArray(result.suggestions)
//             ? result.suggestions
//             : [],
//         },
//       ]);
//       await loadConversations();
//     } catch (err) {
//       console.error("AI Coach request failed:", err);
//       setMessages(previousMessages);
//       setInput(message);
//       setFailedMessage(message);
//       setError(
//         err.response?.data?.message ||
//           "The AI Coach could not respond. Please try again.",
//       );
//     } finally {
//       setLoading(false);
//     }
//   };

//   const handleRetry = () => {
//     if (!failedMessage || loading || historyLoading) return;
//     handleSendMessage(null, failedMessage);
//   };

//   const formatTime = (timestamp) => {
//     if (!timestamp) return "";
//     const date = new Date(timestamp);
//     if (Number.isNaN(date.getTime())) return "";
//     return date.toLocaleTimeString([], {
//       hour: "2-digit",
//       minute: "2-digit",
//     });
//   };

//   const formatDate = (timestamp) => {
//     if (!timestamp) return "";
//     const date = new Date(timestamp);
//     if (Number.isNaN(date.getTime())) return "";
//     return date.toLocaleDateString();
//   };

//   return (
//     <Box className="coach-layout">
//       <Paper className="coach-history" elevation={0}>
//         <Box className="history-header">
//           <Typography variant="h6">Your conversations</Typography>
//           <Button
//             variant="contained"
//             onClick={startNewChat}
//             disabled={loading || historyLoading}
//             className="new-chat-button"
//           >
//             + New chat
//           </Button>
//         </Box>

//         <Divider />

//         <Box className="history-search">
//           <TextField
//             fullWidth
//             size="small"
//             value={searchQuery}
//             onChange={(event) => setSearchQuery(event.target.value)}
//             placeholder="Search conversations..."
//             inputProps={{ "aria-label": "Search conversations by title" }}
//             className="history-search-input"
//           />
//         </Box>

//         <Box className="history-list">
//           {historyLoading && conversations.length === 0 ? (
//             <CircularProgress size={24} />
//           ) : conversations.length === 0 ? (
//             <Typography className="empty-history">
//               Your conversations will appear here.
//             </Typography>
//           ) : filteredConversations.length === 0 ? (
//             <Typography className="empty-history">
//               No conversations match "{searchQuery}".
//             </Typography>
//           ) : (
//             filteredConversations.map((conversation) => (
//               <Box
//                 key={conversation.id}
//                 className={`history-item ${
//                   activeConversationId === conversation.id ? "active" : ""
//                 }`}
//                 onClick={() => openConversation(conversation.id)}
//                 aria-disabled={loading || historyLoading}
//               >
//                 <Box className="history-item-content">
//                   <Typography className="history-title" noWrap>
//                     {conversation.title || "New conversation"}
//                   </Typography>
//                   <Typography className="history-date">
//                     {formatDate(conversation.updatedAt)}
//                   </Typography>
//                 </Box>

//                 <Tooltip title="Delete conversation">
//                   <span>
//                     <IconButton
//                       size="small"
//                       className="delete-chat"
//                       disabled={loading || historyLoading}
//                       onClick={(event) =>
//                         handleDeleteConversation(event, conversation.id)
//                       }
//                       aria-label="Delete conversation"
//                     >
//                       ×
//                     </IconButton>
//                   </span>
//                 </Tooltip>
//               </Box>
//             ))
//           )}
//         </Box>
//       </Paper>

//       <Box className="coach-main">
//         <Box className="coach-topbar">
//           <Box>
//             <Typography variant="h5" fontWeight={700}>
//               AI Fitness Coach
//             </Typography>
//             <Typography variant="body2" color="text.secondary">
//               Your personal fitness companion
//             </Typography>
//           </Box>
//           <Box className="coach-status">
//             <span className="status-dot" />
//             AI Coach
//           </Box>
//         </Box>

//         <Divider />

//         <Box className="chat-messages">
//           {historyLoading && activeConversationId ? (
//             <Box className="chat-loading">
//               <CircularProgress />
//             </Box>
//           ) : messages.length === 0 ? (
//             <Box className="coach-welcome">
//               <Avatar className="coach-avatar">AI</Avatar>
//               <Typography variant="h4" fontWeight={700}>
//                 Let's improve your fitness.
//               </Typography>
//               <Typography className="welcome-description">
//                 Ask me about your workouts, fitness goals, recovery or activity
//                 performance.
//               </Typography>
//               <Box className="suggestion-grid">
//                 {suggestions.map((suggestion) => (
//                   <Paper
//                     key={suggestion}
//                     elevation={0}
//                     className="suggestion-card"
//                     onClick={() => {
//                       if (loading || historyLoading) return;
//                       setInput(suggestion);
//                       inputRef.current?.focus();
//                     }}
//                   >
//                     <Typography variant="body2">{suggestion}</Typography>
//                   </Paper>
//                 ))}
//               </Box>
//             </Box>
//           ) : (
//             <Box className="message-list">
//               {messages.map((message, index) => (
//                 <Box
//                   key={`${index}-${message.timestamp || ""}`}
//                   className={`message-row ${
//                     message.role === "user" ? "user-row" : "assistant-row"
//                   }`}
//                 >
//                   {message.role !== "user" && (
//                     <Avatar className="message-avatar">AI</Avatar>
//                   )}

//                   <Box
//                     className={`message-bubble ${
//                       message.role === "user"
//                         ? "user-bubble"
//                         : "assistant-bubble"
//                     } ${
//                       message.role === "assistant" &&
//                       /\\n\\s*\\|.*\\|.*\\n\\s*\\|[\\s:|-]+\\|/.test(
//                         message.content || "",
//                       )
//                         ? "assistant-bubble-wide"
//                         : ""
//                     }`}
//                   >
//                     {message.role === "user" ? (
//                       <Typography className="message-content">
//                         {message.content}
//                       </Typography>
//                     ) : (
//                       <div className="message-content markdown-content">
//                         <ReactMarkdown
//                           remarkPlugins={[remarkGfm]}
//                           components={{
//                             table: ({...props }) => (
//                               <div className="markdown-table-wrapper">
//                                 <table {...props} />
//                               </div>
//                             ),
//                           }}
//                         >
//                           {message.content || ""}
//                         </ReactMarkdown>
//                       </div>
//                     )}

//                     <Typography className="message-time">
//                       {formatTime(message.timestamp)}
//                     </Typography>

//                     {message.role !== "user" && (
//                       <Box className="message-actions">
//                         <Tooltip title="Copy response">
//                           <IconButton
//                             size="small"
//                             onClick={() =>
//                               handleCopyMessage(message.content || "")
//                             }
//                             className="message-action-button"
//                             aria-label="Copy AI response"
//                           >
//                             <span className="copy-icon">Copy</span>
//                           </IconButton>
//                         </Tooltip>
//                       </Box>
//                     )}

//                     {message.role === "assistant" &&
//                       index === messages.length - 1 &&
//                       !loading &&
//                       Array.isArray(message.suggestions) &&
//                       message.suggestions.length > 0 && (
//                         <Box className="follow-up-container">
//                           <Typography className="follow-up-label">
//                             Continue the conversation
//                           </Typography>
//                           <Box className="follow-up-list">
//                             {message.suggestions.map((suggestion, suggestionIndex) => (
//                               <Button
//                                 key={`${suggestionIndex}-${suggestion}`}
//                                 size="small"
//                                 variant="outlined"
//                                 className="follow-up-button"
//                                 onClick={() => {
//                                   setInput(suggestion);
//                                   inputRef.current?.focus();
//                                 }}
//                               >
//                                 {suggestion}
//                               </Button>
//                             ))}
//                           </Box>
//                         </Box>
//                       )}
//                   </Box>
//                 </Box>
//               ))}

//               {loading && (
//                 <Box className="message-row assistant-row">
//                   <Avatar className="message-avatar">AI</Avatar>
//                   <Paper className="typing-indicator">
//                     <CircularProgress size={16} />
//                     <Typography variant="caption">Thinking...</Typography>
//                   </Paper>
//                 </Box>
//               )}

//               <div ref={bottomRef} />
//             </Box>
//           )}
//         </Box>

//         {error && (
//           <Box className="coach-error-container">
//             <Typography className="coach-error" role="alert">
//               {error}
//             </Typography>
//             {failedMessage && (
//               <Button
//                 size="small"
//                 variant="outlined"
//                 onClick={handleRetry}
//                 disabled={loading || historyLoading}
//                 className="retry-button"
//               >
//                 Retry
//               </Button>
//             )}
//           </Box>
//         )}

//         <Box
//           component="form"
//           className="chat-input-area"
//           onSubmit={handleSendMessage}
//         >
//           <TextField
//             inputRef={inputRef}
//             fullWidth
//             multiline
//             maxRows={4}
//             placeholder="Ask your AI fitness coach..."
//             value={input}
//             onChange={(event) => setInput(event.target.value)}
//             onKeyDown={(event) => {
//               if (
//                 event.key === "Enter" &&
//                 !event.shiftKey &&
//                 !event.nativeEvent.isComposing
//               ) {
//                 event.preventDefault();
//                 if (input.trim() && !loading && !historyLoading) {
//                   handleSendMessage(event);
//                 }
//               }
//             }}
//             disabled={loading || historyLoading}
//             className="chat-input"
//           />

//           <Button
//             type="submit"
//             variant="contained"
//             disabled={!input.trim() || loading || historyLoading}
//             className="send-button"
//           >
//             Send
//           </Button>
//         </Box>

//         <Typography className="chat-disclaimer">
//           AI-generated fitness guidance is informational. Consult a qualified
//           professional for medical advice.
//         </Typography>
//       </Box>

//       <Snackbar
//         open={copyNotice}
//         autoHideDuration={2000}
//         onClose={() => setCopyNotice(false)}
//         message="AI response copied to clipboard"
//       />
//     </Box>
//   );
// };

// export default ChatCoach;

import { useEffect, useRef, useState } from "react";

import {
  Avatar,
  Box,
  Button,
  CircularProgress,
  Divider,
  IconButton,
  Paper,
  Snackbar,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";

import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

import {
  deleteCoachConversation,
  getCoachConversation,
  getCoachConversations,
} from "../../services/api";

import "./ChatCoach.css";

const API_URL = (
  import.meta.env.VITE_API_URL || "http://localhost:8080/api"
).replace(/\/$/, "");

const suggestions = [
  "How can I improve my fitness?",
  "Create a weekly workout routine for me.",
  "How should I recover after an intense workout?",
  "Analyze my recent fitness progress.",
];

const ChatCoach = () => {
  const [conversations, setConversations] = useState([]);
  const [activeConversationId, setActiveConversationId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [searchQuery, setSearchQuery] = useState("");

  const [loading, setLoading] = useState(false);
  const [streaming, setStreaming] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [error, setError] = useState("");
  const [failedMessage, setFailedMessage] = useState("");
  const [copyNotice, setCopyNotice] = useState(false);

  const bottomRef = useRef(null);
  const inputRef = useRef(null);
  const abortControllerRef = useRef(null);

  const filteredConversations = conversations.filter((conversation) => {
    const title = conversation.title || "New conversation";
    return title.toLowerCase().includes(searchQuery.trim().toLowerCase());
  });

  const loadConversations = async (showLoading = false) => {
    try {
      if (showLoading) setHistoryLoading(true);
      const response = await getCoachConversations();
      setConversations(response.data || []);
    } catch (err) {
      console.error("Failed to load conversations", err);
      setError("Unable to load conversation history.");
    } finally {
      if (showLoading) setHistoryLoading(false);
    }
  };

  useEffect(() => {
    loadConversations(true);
  }, []);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [messages, loading]);

  const startNewChat = () => {
    if (loading || historyLoading) return;
    setActiveConversationId(null);
    setMessages([]);
    setInput("");
    setError("");
    setFailedMessage("");
    inputRef.current?.focus();
  };

  const openConversation = async (conversationId) => {
    if (loading || historyLoading) return;
    try {
      setError("");
      setFailedMessage("");
      setHistoryLoading(true);
      const response = await getCoachConversation(conversationId);
      const conversation = response.data;
      setActiveConversationId(conversation.id);
      setMessages(conversation.messages || []);
      setInput("");
    } catch (err) {
      console.error("Failed to open conversation", err);
      setError(
        err.response?.data?.message || "Unable to open this conversation.",
      );
    } finally {
      setHistoryLoading(false);
    }
  };

  const handleDeleteConversation = async (event, conversationId) => {
    event.stopPropagation();
    if (loading || historyLoading) return;
    try {
      await deleteCoachConversation(conversationId);
      setConversations((previous) =>
        previous.filter((item) => item.id !== conversationId),
      );
      if (activeConversationId === conversationId) {
        setActiveConversationId(null);
        setMessages([]);
        setInput("");
        setError("");
        setFailedMessage("");
      }
    } catch (err) {
      console.error("Failed to delete conversation", err);
      setError(
        err.response?.data?.message || "Could not delete this conversation.",
      );
    }
  };

  const handleCopyMessage = async (content) => {
    try {
      await navigator.clipboard.writeText(content);
      setCopyNotice(true);
    } catch (err) {
      console.error("Failed to copy response:", err);
      setError("Unable to copy the response. Please try again.");
    }
  };

  const handleSendMessage = async (event, messageOverride = null) => {
    event?.preventDefault();
    const message = (messageOverride ?? input).trim();
    if (!message || loading || historyLoading) return;

    const previousMessages = [...messages];
    const optimisticUserMessage = {
      role: "user",
      content: message,
      timestamp: new Date().toISOString(),
    };
    const assistantPlaceholder = {
      role: "assistant",
      content: "",
      timestamp: new Date().toISOString(),
      suggestions: [],
    };

    setMessages((previous) => [
      ...previous,
      optimisticUserMessage,
      assistantPlaceholder,
    ]);
    setInput("");
    setLoading(true);
    setStreaming(false);
    setError("");
    setFailedMessage("");

    const controller = new AbortController();
    abortControllerRef.current = controller;

    let completionReceived = false;

    const updateLastAssistant = (updater) => {
      setMessages((previous) => {
        const updated = [...previous];
        const lastIndex = updated.length - 1;

        if (lastIndex >= 0 && updated[lastIndex].role === "assistant") {
          updated[lastIndex] = updater(updated[lastIndex]);
        }

        return updated;
      });
    };

    const processSseEvent = (rawEvent) => {
      let eventName = "message";
      const dataLines = [];

      rawEvent.split("\n").forEach((line) => {
        if (line.startsWith("event:")) {
          eventName = line.slice(6).trim();
        } else if (line.startsWith("data:")) {
          dataLines.push(line.slice(5).trimStart());
        }
      });

      const data = dataLines.join("\n");
      if (!data) return;

      if (eventName === "meta") {
        const metadata = JSON.parse(data);
        if (metadata.conversationId) {
          setActiveConversationId(metadata.conversationId);
        }
        return;
      }

      if (eventName === "token") {
        setStreaming(true);
        updateLastAssistant((lastMessage) => ({
          ...lastMessage,
          content: lastMessage.content + data,
        }));
        return;
      }

      if (eventName === "complete") {
        const result = JSON.parse(data);
        completionReceived = true;
        updateLastAssistant((lastMessage) => ({
          ...lastMessage,
          timestamp: result.timestamp || new Date().toISOString(),
          suggestions: Array.isArray(result.suggestions)
            ? result.suggestions
            : [],
        }));
        return;
      }

      if (eventName === "error") {
        const result = JSON.parse(data);
        throw new Error(
          result.message || "The AI Coach could not complete the response.",
        );
      }
    };

    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`${API_URL}/coach/chat/stream`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "text/event-stream",
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify({
          conversationId: activeConversationId,
          message,
        }),
        signal: controller.signal,
      });

      if (!response.ok) {
        const responseText = await response.text();
        let errorMessage = "The AI Coach request failed.";

        try {
          const errorBody = JSON.parse(responseText);
          errorMessage = errorBody.message || errorBody.error || errorMessage;
        } catch {
          if (responseText) errorMessage = responseText;
        }

        throw new Error(errorMessage);
      }

      if (!response.body) {
        throw new Error("Your browser does not support streamed responses.");
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = "";

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        buffer = buffer.replace(/\r\n/g, "\n");

        let boundaryIndex;
        while ((boundaryIndex = buffer.indexOf("\n\n")) !== -1) {
          const rawEvent = buffer.slice(0, boundaryIndex);
          buffer = buffer.slice(boundaryIndex + 2);
          processSseEvent(rawEvent);
        }
      }

      buffer += decoder.decode();
      buffer = buffer.replace(/\r\n/g, "\n");
      if (buffer.trim()) processSseEvent(buffer);

      if (!completionReceived && !controller.signal.aborted) {
        throw new Error(
          "The AI response stream ended unexpectedly. Please retry.",
        );
      }

      if (completionReceived) {
        await loadConversations();
      }
    } catch (err) {
      if (err.name === "AbortError") {
        setError("");
        setFailedMessage("");
      } else {
        console.error("AI Coach streaming failed:", err);
        setMessages(previousMessages);
        setInput(message);
        setFailedMessage(message);
        setError(
          err.message || "The AI Coach could not respond. Please try again.",
        );
      }
    } finally {
      abortControllerRef.current = null;
      setStreaming(false);
      setLoading(false);
    }
  };

  const handleStopGeneration = () => {
    abortControllerRef.current?.abort();
  };

  const handleRetry = () => {
    if (!failedMessage || loading || historyLoading) return;
    handleSendMessage(null, failedMessage);
  };

  const formatTime = (timestamp) => {
    if (!timestamp) return "";
    const date = new Date(timestamp);
    if (Number.isNaN(date.getTime())) return "";
    return date.toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const formatDate = (timestamp) => {
    if (!timestamp) return "";
    const date = new Date(timestamp);
    if (Number.isNaN(date.getTime())) return "";
    return date.toLocaleDateString();
  };

  return (
    <Box className="coach-layout">
      <Paper className="coach-history" elevation={0}>
        <Box className="history-header">
          <Typography variant="h6">Your conversations</Typography>
          <Button
            variant="contained"
            onClick={startNewChat}
            disabled={loading || historyLoading}
            className="new-chat-button"
          >
            + New chat
          </Button>
        </Box>

        <Divider />

        <Box className="history-search">
          <TextField
            fullWidth
            size="small"
            value={searchQuery}
            onChange={(event) => setSearchQuery(event.target.value)}
            placeholder="Search conversations..."
            inputProps={{ "aria-label": "Search conversations by title" }}
            className="history-search-input"
          />
        </Box>

        <Box className="history-list">
          {historyLoading && conversations.length === 0 ? (
            <CircularProgress size={24} />
          ) : conversations.length === 0 ? (
            <Typography className="empty-history">
              Your conversations will appear here.
            </Typography>
          ) : filteredConversations.length === 0 ? (
            <Typography className="empty-history">
              No conversations match "{searchQuery}".
            </Typography>
          ) : (
            filteredConversations.map((conversation) => (
              <Box
                key={conversation.id}
                className={`history-item ${
                  activeConversationId === conversation.id ? "active" : ""
                }`}
                onClick={() => openConversation(conversation.id)}
                aria-disabled={loading || historyLoading}
              >
                <Box className="history-item-content">
                  <Typography className="history-title" noWrap>
                    {conversation.title || "New conversation"}
                  </Typography>
                  <Typography className="history-date">
                    {formatDate(conversation.updatedAt)}
                  </Typography>
                </Box>

                <Tooltip title="Delete conversation">
                  <span>
                    <IconButton
                      size="small"
                      className="delete-chat"
                      disabled={loading || historyLoading}
                      onClick={(event) =>
                        handleDeleteConversation(event, conversation.id)
                      }
                      aria-label="Delete conversation"
                    >
                      ×
                    </IconButton>
                  </span>
                </Tooltip>
              </Box>
            ))
          )}
        </Box>
      </Paper>

      <Box className="coach-main">
        <Box className="coach-topbar">
          <Box>
            <Typography variant="h5" fontWeight={700}>
              AI Fitness Coach
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Your personal fitness companion
            </Typography>
          </Box>
          <Box className="coach-status">
            <span className="status-dot" />
            AI Coach
          </Box>
        </Box>

        <Divider />

        <Box className="chat-messages">
          {historyLoading && activeConversationId ? (
            <Box className="chat-loading">
              <CircularProgress />
            </Box>
          ) : messages.length === 0 ? (
            <Box className="coach-welcome">
              <Avatar className="coach-avatar">AI</Avatar>
              <Typography variant="h4" fontWeight={700}>
                Let's improve your fitness.
              </Typography>
              <Typography className="welcome-description">
                Ask me about your workouts, fitness goals, recovery or activity
                performance.
              </Typography>
              <Box className="suggestion-grid">
                {suggestions.map((suggestion) => (
                  <Paper
                    key={suggestion}
                    elevation={0}
                    className="suggestion-card"
                    onClick={() => {
                      if (loading || historyLoading) return;
                      setInput(suggestion);
                      inputRef.current?.focus();
                    }}
                  >
                    <Typography variant="body2">{suggestion}</Typography>
                  </Paper>
                ))}
              </Box>
            </Box>
          ) : (
            <Box className="message-list">
              {messages.map((message, index) => (
                <Box
                  key={`${index}-${message.timestamp || ""}`}
                  className={`message-row ${
                    message.role === "user" ? "user-row" : "assistant-row"
                  }`}
                >
                  {message.role !== "user" && (
                    <Avatar className="message-avatar">AI</Avatar>
                  )}

                  <Box
                    className={`message-bubble ${
                      message.role === "user"
                        ? "user-bubble"
                        : "assistant-bubble"
                    } ${
                      message.role === "assistant" &&
                      /\\n\\s*\\|.*\\|.*\\n\\s*\\|[\\s:|-]+\\|/.test(
                        message.content || "",
                      )
                        ? "assistant-bubble-wide"
                        : ""
                    }`}
                  >
                    {message.role === "user" ? (
                      <Typography className="message-content">
                        {message.content}
                      </Typography>
                    ) : (
                      <div className="message-content markdown-content">
                        <ReactMarkdown
                          remarkPlugins={[remarkGfm]}
                          components={{
                            table: (tableProps) => {
                              const tableAttributes = { ...tableProps };
                              delete tableAttributes.node;

                              return (
                                <div className="markdown-table-wrapper">
                                  <table {...tableAttributes} />
                                </div>
                              );
                            },
                          }}
                        >
                          {message.content || ""}
                        </ReactMarkdown>
                      </div>
                    )}

                    <Typography className="message-time">
                      {formatTime(message.timestamp)}
                    </Typography>

                    {message.role !== "user" && (
                      <Box className="message-actions">
                        <Tooltip title="Copy response">
                          <IconButton
                            size="small"
                            onClick={() =>
                              handleCopyMessage(message.content || "")
                            }
                            className="message-action-button"
                            aria-label="Copy AI response"
                          >
                            <span className="copy-icon">Copy</span>
                          </IconButton>
                        </Tooltip>
                      </Box>
                    )}

                    {message.role === "assistant" &&
                      index === messages.length - 1 &&
                      !loading &&
                      Array.isArray(message.suggestions) &&
                      message.suggestions.length > 0 && (
                        <Box className="follow-up-container">
                          <Typography className="follow-up-label">
                            Continue the conversation
                          </Typography>
                          <Box className="follow-up-list">
                            {message.suggestions.map(
                              (suggestion, suggestionIndex) => (
                                <Button
                                  key={`${suggestionIndex}-${suggestion}`}
                                  size="small"
                                  variant="outlined"
                                  className="follow-up-button"
                                  onClick={() => {
                                    setInput(suggestion);
                                    inputRef.current?.focus();
                                  }}
                                >
                                  {suggestion}
                                </Button>
                              ),
                            )}
                          </Box>
                        </Box>
                      )}
                  </Box>
                </Box>
              ))}

              {loading && !streaming && (
                <Box className="message-row assistant-row">
                  <Avatar className="message-avatar">AI</Avatar>
                  <Paper className="typing-indicator">
                    <CircularProgress size={16} />
                    <Typography variant="caption">Thinking...</Typography>
                  </Paper>
                </Box>
              )}

              <div ref={bottomRef} />
            </Box>
          )}
        </Box>

        {error && (
          <Box className="coach-error-container">
            <Typography className="coach-error" role="alert">
              {error}
            </Typography>
            {failedMessage && (
              <Button
                size="small"
                variant="outlined"
                onClick={handleRetry}
                disabled={loading || historyLoading}
                className="retry-button"
              >
                Retry
              </Button>
            )}
          </Box>
        )}

        <Box
          component="form"
          className="chat-input-area"
          onSubmit={handleSendMessage}
        >
          <TextField
            inputRef={inputRef}
            fullWidth
            multiline
            maxRows={4}
            placeholder="Ask your AI fitness coach..."
            value={input}
            onChange={(event) => setInput(event.target.value)}
            onKeyDown={(event) => {
              if (
                event.key === "Enter" &&
                !event.shiftKey &&
                !event.nativeEvent.isComposing
              ) {
                event.preventDefault();
                if (input.trim() && !loading && !historyLoading) {
                  handleSendMessage(event);
                }
              }
            }}
            disabled={loading || historyLoading}
            className="chat-input"
          />

          {loading ? (
            <Button
              type="button"
              variant="outlined"
              onClick={handleStopGeneration}
              className="stop-button"
            >
              Stop
            </Button>
          ) : (
            <Button
              type="submit"
              variant="contained"
              disabled={!input.trim() || historyLoading}
              className="send-button"
            >
              Send
            </Button>
          )}
        </Box>

        <Typography className="chat-disclaimer">
          AI-generated fitness guidance is informational. Consult a qualified
          professional for medical advice.
        </Typography>
      </Box>

      <Snackbar
        open={copyNotice}
        autoHideDuration={2000}
        onClose={() => setCopyNotice(false)}
        message="AI response copied to clipboard"
      />
    </Box>
  );
};

export default ChatCoach;

require("dotenv").config();

const express = require("express");
const path = require("path");

const uploadRoutes = require("./routes/uploadRoutes");
const aiRoutes = require("./routes/aiRoutes");
const { ensureUploadDirectories } = require("./utils/fileUtils");

const app = express();
const PORT = process.env.PORT || 8080;

ensureUploadDirectories();

app.use(express.json({ limit: "2mb" }));

app.get("/", (req, res) => {
    res.json({
        status: "online",
        service: "Kartavya Media Server"
    });
});

app.get("/health", (req, res) => {
    res.json({
        success: true,
        status: "healthy"
    });
});

app.use("/upload", uploadRoutes);
app.use("/ai", aiRoutes);
app.use("/files", express.static(path.join(__dirname, "uploads")));

app.use((req, res) => {
    res.status(404).json({
        success: false,
        message: "Route not found"
    });
});

app.use((error, req, res, next) => {
    const statusCode = error.statusCode || 500;
    res.status(statusCode).json({
        success: false,
        message: error.publicMessage || error.message || "Internal server error"
    });
});

if (require.main === module) {
    app.listen(PORT, "0.0.0.0", () => {
        console.log(`Kartavya server running on port ${PORT}`);
    });
}

module.exports = app;

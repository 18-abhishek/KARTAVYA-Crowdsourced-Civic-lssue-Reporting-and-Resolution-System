const express = require("express");
const multer = require("multer");
const {
    buildPublicPath,
    createHttpError,
    createUploadMiddleware
} = require("../utils/fileUtils");

const router = express.Router();
const imageUpload = createUploadMiddleware("images");
const audioUpload = createUploadMiddleware("audio");

router.post("/image", imageUpload.single("file"), (req, res) => {
    if (!req.file) {
        throw createHttpError(400, "No image uploaded");
    }

    res.json({
        success: true,
        filename: req.file.filename,
        path: buildPublicPath(req.file)
    });
});

router.post("/audio", audioUpload.single("file"), (req, res) => {
    if (!req.file) {
        throw createHttpError(400, "No audio uploaded");
    }

    res.json({
        success: true,
        filename: req.file.filename,
        path: buildPublicPath(req.file)
    });
});

router.use((error, req, res, next) => {
    if (error instanceof multer.MulterError) {
        return res.status(400).json({
            success: false,
            message: error.message
        });
    }

    next(error);
});

module.exports = router;

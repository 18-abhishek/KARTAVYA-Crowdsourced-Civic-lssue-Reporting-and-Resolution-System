const fs = require("fs");
const path = require("path");
const multer = require("multer");

const UPLOAD_ROOT = path.join(__dirname, "..", "uploads");
const IMAGE_DIR = path.join(UPLOAD_ROOT, "images");
const AUDIO_DIR = path.join(UPLOAD_ROOT, "audio");

const IMAGE_MIME_TYPES = new Set([
    "image/jpeg",
    "image/png",
    "image/webp",
    "image/heic",
    "image/heif"
]);

const AUDIO_MIME_TYPES = new Set([
    "audio/aac",
    "audio/mp4",
    "audio/mpeg",
    "audio/wav",
    "audio/webm",
    "audio/x-m4a",
    "audio/m4a",
    "video/mp4"
]);

const MIME_BY_EXTENSION = {
    ".jpg": "image/jpeg",
    ".jpeg": "image/jpeg",
    ".png": "image/png",
    ".webp": "image/webp",
    ".heic": "image/heic",
    ".heif": "image/heif",
    ".m4a": "audio/mp4",
    ".mp4": "audio/mp4",
    ".aac": "audio/aac",
    ".wav": "audio/wav",
    ".mp3": "audio/mpeg",
    ".webm": "audio/webm"
};

const IMAGE_EXTENSIONS = new Set([
    ".jpg",
    ".jpeg",
    ".png",
    ".webp",
    ".heic",
    ".heif"
]);

const AUDIO_EXTENSIONS = new Set([
    ".m4a",
    ".mp4",
    ".aac",
    ".wav",
    ".mp3",
    ".webm"
]);

function ensureUploadDirectories() {
    fs.mkdirSync(IMAGE_DIR, { recursive: true });
    fs.mkdirSync(AUDIO_DIR, { recursive: true });
}

function getUploadDirectory(file) {
    if (matchesExpectedKind(file, "images")) {
        return IMAGE_DIR;
    }

    if (matchesExpectedKind(file, "audio")) {
        return AUDIO_DIR;
    }

    return null;
}

function createUploadMiddleware(expectedKind) {
    const storage = multer.diskStorage({
        destination: (req, file, cb) => {
            const uploadDirectory = getUploadDirectory(file);

            if (!uploadDirectory || !matchesExpectedKind(file, expectedKind)) {
                return cb(createHttpError(415, "Unsupported file type"));
            }

            cb(null, uploadDirectory);
        },
        filename: (req, file, cb) => {
            const extension = path.extname(file.originalname || "").toLowerCase();
            const uniqueName = `${Date.now()}-${Math.round(Math.random() * 1E9)}${extension}`;
            cb(null, uniqueName);
        }
    });

    return multer({
        storage,
        limits: {
            fileSize: 25 * 1024 * 1024
        },
        fileFilter: (req, file, cb) => {
            if (matchesExpectedKind(file, expectedKind)) {
                return cb(null, true);
            }

            cb(createHttpError(415, "Unsupported file type"));
        }
    });
}

function createHttpError(statusCode, message) {
    const error = new Error(message);
    error.statusCode = statusCode;
    error.publicMessage = message;
    return error;
}

function isImageMimeType(mimeType) {
    return IMAGE_MIME_TYPES.has((mimeType || "").toLowerCase());
}

function isAudioMimeType(mimeType) {
    return AUDIO_MIME_TYPES.has((mimeType || "").toLowerCase());
}

function isExpectedKind(mimeType, expectedKind) {
    if (!expectedKind) {
        return isImageMimeType(mimeType) || isAudioMimeType(mimeType);
    }

    if (expectedKind === "images") {
        return isImageMimeType(mimeType);
    }

    if (expectedKind === "audio") {
        return isAudioMimeType(mimeType);
    }

    return false;
}

function getFileExtension(file) {
    return path.extname(file?.originalname || "").toLowerCase();
}

function isImageExtension(extension) {
    return IMAGE_EXTENSIONS.has(extension);
}

function isAudioExtension(extension) {
    return AUDIO_EXTENSIONS.has(extension);
}

function matchesExpectedKind(file, expectedKind) {
    const mimeType = file?.mimetype;
    const extension = getFileExtension(file);

    if (!expectedKind) {
        return isImageMimeType(mimeType) || isAudioMimeType(mimeType) || isImageExtension(extension) || isAudioExtension(extension);
    }

    if (expectedKind === "images") {
        return isImageMimeType(mimeType) || isImageExtension(extension);
    }

    if (expectedKind === "audio") {
        return isAudioMimeType(mimeType) || isAudioExtension(extension);
    }

    return false;
}

function getMediaKindFromPath(filePath) {
    const normalized = filePath.replace(/\\/g, "/").toLowerCase();

    if (normalized.includes("/uploads/images/")) {
        return "images";
    }

    if (normalized.includes("/uploads/audio/")) {
        return "audio";
    }

    return null;
}

function buildPublicPath(file) {
    const kind = getMediaKindFromPath(file.path);

    if (!kind) {
        throw createHttpError(500, "Could not resolve uploaded file path");
    }

    return `/files/${kind}/${file.filename}`;
}

function resolveLocalMediaPath(mediaReference, expectedKind) {
    if (!mediaReference || typeof mediaReference !== "string") {
        throw createHttpError(400, `${expectedKind} URL or path is required`);
    }

    let pathname = mediaReference.trim();

    try {
        pathname = new URL(pathname).pathname;
    } catch (_) {
        // A relative /files/... path is also valid.
    }

    const normalizedPathname = pathname.replace(/\\/g, "/");
    const prefix = `/files/${expectedKind}/`;

    if (!normalizedPathname.startsWith(prefix)) {
        throw createHttpError(400, `${expectedKind} reference must point to ${prefix}`);
    }

    const filename = path.basename(normalizedPathname);
    const localPath = path.join(UPLOAD_ROOT, expectedKind, filename);

    if (!fs.existsSync(localPath)) {
        throw createHttpError(404, `${expectedKind} file not found on server`);
    }

    return localPath;
}

function getMimeType(filePath) {
    return MIME_BY_EXTENSION[path.extname(filePath).toLowerCase()] || "application/octet-stream";
}

module.exports = {
    AUDIO_DIR,
    IMAGE_DIR,
    UPLOAD_ROOT,
    buildPublicPath,
    createHttpError,
    createUploadMiddleware,
    ensureUploadDirectories,
    getMimeType,
    resolveLocalMediaPath
};

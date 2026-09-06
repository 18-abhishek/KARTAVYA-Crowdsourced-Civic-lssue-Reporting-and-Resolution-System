const fs = require("fs");
const path = require("path");
const admin = require("firebase-admin");
const { getAuth } = require("firebase-admin/auth");
const { FieldValue, getFirestore } = require("firebase-admin/firestore");
const { createHttpError } = require("../utils/fileUtils");

let initialized = false;
let initializationError = null;
const DEFAULT_SERVICE_ACCOUNT_FILENAME = "kartavya-deca3-firebase-adminsdk-fbsvc-947056b9eb.json";

function initializeFirebase({ cacheMissingCredentials = true } = {}) {
    if (initialized || initializationError) {
        return initialized;
    }

    try {
        if (admin.getApps().length) {
            initialized = true;
            return true;
        }

        const serviceAccountJson = process.env.FIREBASE_SERVICE_ACCOUNT_JSON;
        const serviceAccountPath = resolveServiceAccountPath();

        if (serviceAccountJson) {
            admin.initializeApp({
                credential: admin.cert(JSON.parse(serviceAccountJson))
            });
        } else if (serviceAccountPath) {
            admin.initializeApp({
                credential: admin.cert(require(serviceAccountPath))
            });
        } else if (process.env.GOOGLE_APPLICATION_CREDENTIALS || process.env.FIREBASE_PROJECT_ID || process.env.GCLOUD_PROJECT) {
            admin.initializeApp({
                credential: admin.applicationDefault(),
                projectId: process.env.FIREBASE_PROJECT_ID || process.env.GCLOUD_PROJECT
            });
        } else {
            if (cacheMissingCredentials) {
                initializationError = new Error("Firebase Admin credentials are not configured");
            }
            return false;
        }

        initialized = true;
        return true;
    } catch (error) {
        initializationError = error;
        return false;
    }
}

function getFirestoreClient() {
    if (!initializeFirebase()) {
        throw createHttpError(500, initializationError?.message || "Firebase Admin credentials are not configured");
    }

    return getFirestore();
}

async function verifyFirebaseIdToken(authorizationHeader) {
    if (!authorizationHeader) {
        return null;
    }

    const match = authorizationHeader.match(/^Bearer\s+(.+)$/i);

    if (!match) {
        throw createHttpError(401, "Invalid Authorization header");
    }

    if (!initializeFirebase()) {
        throw createHttpError(500, initializationError?.message || "Firebase Admin credentials are not configured");
    }

    return getAuth().verifyIdToken(match[1]);
}

async function saveComplaint({ complaintId, complaint }) {
    const db = getFirestoreClient();
    const data = {
        ...complaint,
        createdAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
        timestamp: FieldValue.serverTimestamp()
    };

    if (complaintId) {
        await db.collection("issues").doc(complaintId).set(data, { merge: true });
        return complaintId;
    }

    const docRef = await db.collection("issues").add(data);
    return docRef.id;
}

function isFirebaseConfigured() {
    return initializeFirebase({ cacheMissingCredentials: false });
}

function resolveServiceAccountPath() {
    const configuredPath = process.env.FIREBASE_SERVICE_ACCOUNT_PATH;

    if (configuredPath && fs.existsSync(configuredPath)) {
        return configuredPath;
    }

    const defaultPath = path.join(__dirname, "..", DEFAULT_SERVICE_ACCOUNT_FILENAME);

    if (fs.existsSync(defaultPath)) {
        return defaultPath;
    }

    return null;
}

module.exports = {
    isFirebaseConfigured,
    saveComplaint,
    verifyFirebaseIdToken,
    getFirestoreClient
};
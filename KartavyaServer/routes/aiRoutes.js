const express = require("express");
const { analyzeVerifiedComplaint, verifyComplaintImage } = require("../services/geminiService");
const { transcribeAudio } = require("../services/sarvamService");
const {
    createHttpError,
    resolveLocalMediaPath
} = require("../utils/fileUtils");
const {
    isFirebaseConfigured,
    saveComplaint,
    verifyFirebaseIdToken
} = require("../services/firebaseService");

const router = express.Router();
const activeProcessingKeys = new Set();
const completedResults = new Map();

router.get("/status", (req, res) => {
    res.json({
        success: true,
        firebaseConfigured: isFirebaseConfigured()
    });
});

router.post("/process-complaint", async (req, res, next) => {
    const {
        issueId,
        userId,
        reporterName,
        imageUrl,
        audioUrl,
        latitude,
        longitude,
        address,
        routingTo
    } = req.body || {};

    const processingKey = issueId || `${imageUrl || ""}|${audioUrl || ""}`;

    try {
        if (!processingKey || processingKey === "|") {
            throw createHttpError(400, "issueId or media URLs are required");
        }

        if (!imageUrl) {
            throw createHttpError(400, "imageUrl is required");
        }

        if (!audioUrl) {
            throw createHttpError(400, "audioUrl is required");
        }

        if (activeProcessingKeys.has(processingKey)) {
            return res.status(409).json({
                success: false,
                message: "AI processing is already running for this complaint"
            });
        }

        if (completedResults.has(processingKey)) {
            return res.json({
                ...completedResults.get(processingKey),
                duplicate: true
            });
        }

        activeProcessingKeys.add(processingKey);

        const decodedToken = await verifyFirebaseIdToken(req.get("Authorization"));
        const effectiveUserId = decodedToken?.uid || userId;

        if (!effectiveUserId) {
            throw createHttpError(400, "userId is required when Authorization token is not provided");
        }

        if (decodedToken && userId && decodedToken.uid !== userId) {
            throw createHttpError(403, "Authenticated user does not match request userId");
        }

        const imagePath = resolveLocalMediaPath(imageUrl, "images");
        const audioPath = resolveLocalMediaPath(audioUrl, "audio");

        const imageVerification = await verifyComplaintImage(imagePath);

        if (!imageVerification.approved) {
            const rejectedResult = {
                success: true,
                approved: false,
                message: "Complaint image was rejected by AI verification",
                ai: {
                    approved: false,
                    category: imageVerification.category,
                    summary: "",
                    description: "",
                    priority: "",
                    reason: imageVerification.reason,
                    transcript: ""
                },
                imageVerification,
                firestore: {
                    written: false
                }
            };

            completedResults.set(processingKey, rejectedResult);
            return res.json(rejectedResult);
        }

        const sarvamResult = await transcribeAudio(audioPath);
        const finalAnalysis = await analyzeVerifiedComplaint({
            imagePath,
            transcript: sarvamResult.transcript,
            imageVerification
        });

        const complaint = buildComplaintDocument({
            userId: effectiveUserId,
            reporterName,
            imageUrl,
            audioUrl,
            latitude,
            longitude,
            address,
            routingTo,
            imageVerification,
            finalAnalysis,
            transcript: sarvamResult.transcript,
            languageCode: sarvamResult.languageCode
        });

        let firestore = {
            written: false
        };

        if (isFirebaseConfigured()) {
            const savedIssueId = await saveComplaint({
                complaintId: issueId,
                complaint
            });

            firestore = {
                written: true,
                issueId: savedIssueId
            };
        }

        const result = {
            success: true,
            approved: finalAnalysis.approved,
            ai: finalAnalysis,
            imageVerification,
            transcript: sarvamResult.transcript,
            languageCode: sarvamResult.languageCode,
            firestore
        };

        completedResults.set(processingKey, result);
        res.json(result);
    } catch (error) {
        next(error);
    } finally {
        activeProcessingKeys.delete(processingKey);
    }
});

function buildComplaintDocument({
    userId,
    reporterName,
    imageUrl,
    audioUrl,
    latitude,
    longitude,
    address,
    routingTo,
    imageVerification,
    finalAnalysis,
    transcript,
    languageCode
}) {
    const status = finalAnalysis.approved ? "REPORTED" : "REJECTED";

    return {
        userId,
        reporterName: reporterName || "Citizen",
        title: finalAnalysis.summary,
        imageUrl,
        imageUrls: [imageUrl],
        audioUrl,
        transcript,
        languageCode,
        aiVerification: {
            approved: finalAnalysis.approved,
            imageApproved: imageVerification.approved,
            imageReason: imageVerification.reason,
            finalReason: finalAnalysis.reason
        },
        aiApproved: finalAnalysis.approved,
        category: finalAnalysis.category,
        summary: finalAnalysis.summary,
        description: finalAnalysis.description,
        priority: finalAnalysis.priority,
        reason: finalAnalysis.reason,
        status,
        latitude: parseOptionalNumber(latitude),
        longitude: parseOptionalNumber(longitude),
        address: address || "",
        routingTo: routingTo || "NDMC Authority"
    };
}

function parseOptionalNumber(value) {
    if (value === null || value === undefined || value === "") {
        return null;
    }

    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
}

module.exports = router;

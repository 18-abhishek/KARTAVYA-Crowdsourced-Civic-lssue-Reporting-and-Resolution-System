const fs = require("fs");
const { GoogleGenAI } = require("@google/genai");
const { createHttpError, getMimeType } = require("../utils/fileUtils");

const GEMINI_MODEL = process.env.GEMINI_MODEL || "gemini-2.5-flash";
const MAX_RETRIES = 3;
const RETRY_BASE_DELAY_MS = 750;

let client;

function getClient() {
    if (!process.env.GEMINI_API_KEY) {
        throw createHttpError(500, "GEMINI_API_KEY is not configured");
    }

    if (!client) {
        client = new GoogleGenAI({
            apiKey: process.env.GEMINI_API_KEY
        });
    }

    return client;
}

async function verifyComplaintImage(imagePath) {
    const imageData = fs.readFileSync(imagePath).toString("base64");
    const response = await generateContentWithRetry({
        model: GEMINI_MODEL,
        contents: [
            {
                inlineData: {
                    mimeType: getMimeType(imagePath),
                    data: imageData
                }
            },
            {
                text: `
You are the first gate in a civic complaint pipeline.

Decide whether this photo clearly represents a genuine public or civic infrastructure issue such as road damage, garbage, broken lights, waterlogging, drainage, public safety hazards, damaged public property, or similar civic problems.

Return ONLY valid JSON with this shape:
{
  "approved": true,
  "category": "short category",
  "reason": "short reason based only on visible evidence"
}

If the image is unclear, private-only, unrelated, staged, a selfie, a document/screenshot, or not a civic/public infrastructure issue, return:
{
  "approved": false,
  "category": "Unverified",
  "reason": "short reason"
}

Do not invent facts that are not visible in the image.
`
            }
        ],
        config: {
            responseMimeType: "application/json"
        }
    });

    const parsed = parseJsonResponse(response.text);

    return {
        approved: Boolean(parsed.approved),
        category: asShortString(parsed.category, "Unverified"),
        reason: asShortString(parsed.reason, "Image could not be verified")
    };
}

async function analyzeVerifiedComplaint({ imagePath, transcript, imageVerification }) {
    const imageData = fs.readFileSync(imagePath).toString("base64");
    const response = await generateContentWithRetry({
        model: GEMINI_MODEL,
        contents: [
            {
                inlineData: {
                    mimeType: getMimeType(imagePath),
                    data: imageData
                }
            },
            {
                text: `
You are producing the final structured analysis for a verified civic complaint.

Inputs:
- The image has already passed first-stage civic issue verification.
- Image verification category: ${imageVerification.category}
- Image verification reason: ${imageVerification.reason}
- Citizen audio transcript: ${transcript || "No transcript returned"}

Use BOTH the verified photo and the transcript. Do not invent facts that are not visible in the image or stated in the transcript. If confidence is limited, say so in the reason or description.

Return ONLY valid JSON with this exact shape:
{
  "approved": true,
  "category": "Road Damage | Garbage | Streetlight | Drainage | Waterlogging | Public Safety | Other",
  "summary": "one short complaint title",
  "description": "concise description using only visible or spoken evidence",
  "priority": "Low | Moderate | High | Urgent",
  "reason": "brief justification",
  "transcript": "the transcript text"
}

Set approved=false only if the transcript and image together reveal this should not become a complaint.
`
            }
        ],
        config: {
            responseMimeType: "application/json"
        }
    });

    const parsed = parseJsonResponse(response.text);

    return {
        approved: Boolean(parsed.approved),
        category: asShortString(parsed.category, imageVerification.category || "Other"),
        summary: asShortString(parsed.summary, "Civic issue reported"),
        description: asShortString(parsed.description, imageVerification.reason || "Civic issue verified from submitted media"),
        priority: normalizePriority(parsed.priority),
        reason: asShortString(parsed.reason, imageVerification.reason || "Verified civic issue"),
        transcript: asShortString(parsed.transcript, transcript || "")
    };
}

async function generateContentWithRetry(request) {
    let lastError;

    for (let attempt = 1; attempt <= MAX_RETRIES; attempt += 1) {
        try {
            return await getClient().models.generateContent(request);
        } catch (error) {
            lastError = error;

            if (!isRetryableGeminiError(error) || attempt === MAX_RETRIES) {
                throw normalizeGeminiError(error);
            }

            await delay(RETRY_BASE_DELAY_MS * Math.pow(2, attempt - 1));
        }
    }

    throw normalizeGeminiError(lastError);
}

function isRetryableGeminiError(error) {
    const status = Number(error?.status || error?.code || error?.response?.status);
    const message = String(error?.message || "");
    return status === 429 || status === 503 || message.includes("429") || message.includes("503");
}

function normalizeGeminiError(error) {
    if (isRetryableGeminiError(error)) {
        return createHttpError(503, "Gemini is temporarily unavailable or rate-limited. Please try again shortly.");
    }

    return error;
}

function parseJsonResponse(text) {
    const cleaned = String(text || "")
        .trim()
        .replace(/^```json\s*/i, "")
        .replace(/^```\s*/i, "")
        .replace(/\s*```$/i, "")
        .trim();

    try {
        return JSON.parse(cleaned);
    } catch (error) {
        const start = cleaned.indexOf("{");
        const end = cleaned.lastIndexOf("}");

        if (start >= 0 && end > start) {
            return JSON.parse(cleaned.slice(start, end + 1));
        }

        throw createHttpError(502, "Gemini returned an invalid JSON response");
    }
}

function asShortString(value, fallback) {
    const text = typeof value === "string" ? value.trim() : "";
    return text || fallback;
}

function normalizePriority(priority) {
    const text = asShortString(priority, "Moderate");
    const allowed = new Set(["Low", "Moderate", "High", "Urgent"]);
    return allowed.has(text) ? text : "Moderate";
}

function delay(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

module.exports = {
    analyzeVerifiedComplaint,
    verifyComplaintImage
};

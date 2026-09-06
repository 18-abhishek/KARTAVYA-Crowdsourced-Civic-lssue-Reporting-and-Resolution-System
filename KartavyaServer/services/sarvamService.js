const fs = require("fs");
const { SarvamAIClient } = require("sarvamai");
const { createHttpError } = require("../utils/fileUtils");

let client;

function getClient() {
    if (!process.env.SARVAM_API_KEY) {
        throw createHttpError(500, "SARVAM_API_KEY is not configured");
    }

    if (!client) {
        client = new SarvamAIClient({
            apiSubscriptionKey: process.env.SARVAM_API_KEY
        });
    }

    return client;
}

async function transcribeAudio(audioPath) {
    const response = await getClient().speechToText.transcribe({
        file: fs.createReadStream(audioPath),
        model: process.env.SARVAM_STT_MODEL || "saaras:v3",
        mode: "transcribe"
    });

    return {
        transcript: response.transcript || "",
        languageCode: response.language_code || null,
        raw: response
    };
}

module.exports = {
    transcribeAudio
};

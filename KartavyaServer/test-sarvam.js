const fs = require("fs");
const { SarvamAIClient } = require("sarvamai");

async function main() {
    console.log("Starting Sarvam Speech-to-Text test...\n");

    const client = new SarvamAIClient({
        apiSubscriptionKey: process.env.SARVAM_API_KEY
    });

    const audioPath = "uploads/audio/test.mp4";

    if (!fs.existsSync(audioPath)) {
        throw new Error(`Audio file not found: ${audioPath}`);
    }

    console.log("Audio file found:");
    console.log(audioPath);
    console.log("\nSending audio to Sarvam...\n");

    const audioFile = fs.createReadStream(audioPath);

    const response = await client.speechToText.transcribe({
        file: audioFile,
        model: "saaras:v3",
        mode: "transcribe"
    });

    console.log("================================");
    console.log("SARVAM RESULT");
    console.log("================================");

    console.log("Detected language:");
    console.log(response.language_code || "Not returned");

    console.log("\nTranscript:");
    console.log(response.transcript || "No transcript returned");

    console.log("\nFull response:");
    console.log(JSON.stringify(response, null, 2));

    console.log("\n================================");
    console.log("TEST COMPLETE");
    console.log("================================");
}

main().catch((error) => {
    console.error("\n================================");
    console.error("SARVAM ERROR");
    console.error("================================");
    console.error(error.message || error);
});
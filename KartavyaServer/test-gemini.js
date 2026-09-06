const { GoogleGenAI } = require("@google/genai");

async function main() {
    console.log("Starting Gemini test...\n");

    const ai = new GoogleGenAI({
        apiKey: process.env.GEMINI_API_KEY
    });

    const response = await ai.models.generateContent({
        model: "gemini-2.5-flash",
        contents: "Explain in one short sentence what a pothole is."
    });

    console.log("GEMINI RESPONSE:");
    console.log(response.text);
}

main().catch((error) => {
    console.error("\nGEMINI ERROR:");
    console.error(error.message || error);
});
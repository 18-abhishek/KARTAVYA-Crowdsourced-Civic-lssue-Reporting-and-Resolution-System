const fs = require("fs");
const { GoogleGenAI } = require("@google/genai");

async function main() {
    console.log("Starting Gemini image verification...\n");

    const ai = new GoogleGenAI({
        apiKey: process.env.GEMINI_API_KEY
    });

    const imagePath = "uploads/images/pothole.webp";

    if (!fs.existsSync(imagePath)) {
        throw new Error(`Image not found: ${imagePath}`);
    }

    const imageData = fs.readFileSync(imagePath).toString("base64");

    const response = await ai.models.generateContent({
        model: "gemini-2.5-flash",
        contents: [
            {
                inlineData: {
                    mimeType: "image/webp",
                    data: imageData
                }
            },
            {
                text: `
Analyze this image for a civic complaint.

Determine whether the image clearly shows a genuine public/civic infrastructure issue.

Return ONLY valid JSON:

{
  "is_civic_issue": true,
  "category": "Road Damage",
  "reason": "Short explanation based only on what is visible"
}

If it is not a civic issue, return false.

Do not invent details that cannot be seen.
`
            }
        ]
    });

    console.log("================================");
    console.log("GEMINI IMAGE VERIFICATION");
    console.log("================================");
    console.log(response.text);
}

main().catch((error) => {
    console.error("\nGEMINI ERROR:");
    console.error(error.message || error);
});
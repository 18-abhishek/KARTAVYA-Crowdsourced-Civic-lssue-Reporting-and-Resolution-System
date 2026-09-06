const { Timestamp } = require("firebase-admin/firestore");
const { getFirestoreClient } = require("../services/firebaseService");

const db = getFirestoreClient();

const demoIssues = [
    {
        id: "demo-issue-001",
        userId: "demo-user-ramesh",
        reporterName: "Ramesh Kumar",
        title: "Large pothole near Kothri Kalan crossing",
        category: "Road Damage",
        summary: "Large pothole near Kothri Kalan crossing",
        description:
            "A large pothole has developed near the Kothri Kalan crossing, making it difficult for vehicles and creating a possible accident hazard.",
        priority: "High",
        status: "IN_PROGRESS",
        reason: "The reported road damage presents a safety concern for vehicles.",
        latitude: 23.2701,
        longitude: 77.3812,
        address: "Kothri Kalan, Bhopal",
        routingTo: "NDMC Authority",
        upvoteCount: 24
    },
    {
        id: "demo-issue-002",
        userId: "demo-user-priya",
        reporterName: "Priya Sharma",
        title: "Streetlight not working near residential lane",
        category: "Streetlighting",
        summary: "Streetlight not working near residential lane",
        description:
            "A streetlight near a residential lane in Kothri Kalan is not functioning, leaving the area poorly illuminated at night.",
        priority: "Moderate",
        status: "REPORTED",
        reason: "Poor street lighting can reduce visibility and create a safety concern.",
        latitude: 23.2710,
        longitude: 77.3820,
        address: "Kothri Kalan, Bhopal",
        routingTo: "NDMC Authority",
        upvoteCount: 5
    },
    {
        id: "demo-issue-003",
        userId: "demo-user-amit",
        reporterName: "Amit Patel",
        title: "Garbage overflowing beside local market",
        category: "Cleanliness",
        summary: "Garbage overflowing beside local market",
        description:
            "Garbage has accumulated around a public collection point near the Kothri Kalan market and is spilling onto the surrounding area.",
        priority: "Moderate",
        status: "ACKNOWLEDGED",
        reason: "Accumulated waste creates an unhygienic public environment.",
        latitude: 23.2695,
        longitude: 77.3804,
        address: "Kothri Kalan Market, Bhopal",
        routingTo: "NDMC Authority",
        upvoteCount: 18
    },
    {
        id: "demo-issue-004",
        userId: "demo-user-sunita",
        reporterName: "Sunita Verma",
        title: "Water pipeline leaking onto road",
        category: "Water & Utilities",
        summary: "Water pipeline leaking onto road",
        description:
            "A leaking water pipeline is releasing water onto the road in Kothri Kalan, creating a wet and slippery section.",
        priority: "High",
        status: "IN_PROGRESS",
        reason: "Continuous water leakage can damage the road and create a hazard.",
        latitude: 23.2707,
        longitude: 77.3798,
        address: "Kothri Kalan, Bhopal",
        routingTo: "NDMC Authority",
        upvoteCount: 31
    },
    {
        id: "demo-issue-005",
        userId: "demo-user-vikas",
        reporterName: "Vikas Gupta",
        title: "Severe waterlogging after rainfall",
        category: "Drainage",
        summary: "Severe waterlogging after rainfall",
        description:
            "Rainwater is collecting on a section of the road in Kothri Kalan because the drainage system is unable to clear the water effectively.",
        priority: "High",
        status: "IN_PROGRESS",
        reason: "Standing water can disrupt traffic and create unsafe road conditions.",
        latitude: 23.2720,
        longitude: 77.3810,
        address: "Kothri Kalan, Bhopal",
        routingTo: "NDMC Authority",
        upvoteCount: 19
    },
    {
        id: "demo-issue-006",
        userId: "demo-user-neha",
        reporterName: "Neha Singh",
        title: "Cracked and uneven road surface",
        category: "Road Damage",
        summary: "Cracked and uneven road surface",
        description:
            "A section of the road in Kothri Kalan has developed multiple cracks and uneven patches that are affecting normal vehicle movement.",
        priority: "Moderate",
        status: "REPORTED",
        reason: "The damaged road surface may worsen if repairs are delayed.",
        latitude: 23.2699,
        longitude: 77.3830,
        address: "Kothri Kalan, Bhopal",
        routingTo: "NDMC Authority",
        upvoteCount: 8
    },
    {
        id: "demo-issue-007",
        userId: "demo-user-rahul",
        reporterName: "Rahul Mehta",
        title: "Damaged roadside barrier creating safety hazard",
        category: "Public Safety",
        summary: "Damaged roadside barrier creating safety hazard",
        description:
            "A roadside safety barrier in Kothri Kalan is damaged and partially displaced, reducing protection for people and vehicles.",
        priority: "High",
        status: "ACKNOWLEDGED",
        reason: "A damaged safety barrier can increase the risk of accidents.",
        latitude: 23.2715,
        longitude: 77.3807,
        address: "Kothri Kalan, Bhopal",
        routingTo: "NDMC Authority",
        upvoteCount: 12
    },
    {
        id: "demo-issue-008",
        userId: "demo-user-pooja",
        reporterName: "Pooja Yadav",
        title: "Overflowing public waste bin",
        category: "Sanitation",
        summary: "Overflowing public waste bin",
        description:
            "A public waste bin in Kothri Kalan has reached capacity and waste is accumulating around it.",
        priority: "Moderate",
        status: "RESOLVED",
        reason: "Overflowing waste reduces cleanliness and can attract pests.",
        latitude: 23.2703,
        longitude: 77.3825,
        address: "Kothri Kalan, Bhopal",
        routingTo: "NDMC Authority",
        upvoteCount: 7
    },
    {
        id: "demo-issue-009",
        userId: "demo-user-arjun",
        reporterName: "Arjun Sharma",
        title: "Multiple streetlights not working on dark lane",
        category: "Streetlighting",
        summary: "Multiple streetlights not working on dark lane",
        description:
            "Several streetlights along a lane in Kothri Kalan are not working, leaving a significant portion of the lane dark after sunset.",
        priority: "High",
        status: "REPORTED",
        reason: "Insufficient lighting can reduce visibility and create a public safety concern.",
        latitude: 23.2724,
        longitude: 77.3799,
        address: "Kothri Kalan, Bhopal",
        routingTo: "NDMC Authority",
        upvoteCount: 15
    },
    {
        id: "demo-issue-010",
        userId: "demo-user-kavita",
        reporterName: "Kavita Jain",
        title: "Damaged water connection causing leakage",
        category: "Water & Utilities",
        summary: "Damaged water connection causing leakage",
        description:
            "A damaged water connection is leaking continuously near a residential area of Kothri Kalan.",
        priority: "Moderate",
        status: "REPORTED",
        reason: "Continued leakage can waste water and damage nearby surfaces.",
        latitude: 23.2692,
        longitude: 77.3818,
        address: "Kothri Kalan, Bhopal",
        routingTo: "NDMC Authority",
        upvoteCount: 21
    }
];

async function seedDemoData() {
    console.log("Starting demo data seed...");

    const batch = db.batch();

    for (const issue of demoIssues) {
        const {
            id,
            upvoteCount,
            ...issueData
        } = issue;

        const issueRef = db.collection("issues").doc(id);

        const randomDaysAgo = Math.floor(Math.random() * 10);

        const createdAt = Timestamp.fromDate(
            new Date(
                Date.now() -
                randomDaysAgo * 24 * 60 * 60 * 1000
            )
        );

        batch.set(
            issueRef,
            {
                ...issueData,

                imageUrl: "",
                imageUrls: [],

                audioUrl: "",

                transcript: "",
                languageCode: "en-IN",

                aiVerification: {
                    approved: true,
                    imageApproved: true,
                    imageReason: "Demo citizen report.",
                    finalReason:
                        "Demo citizen report created for application testing."
                },

                aiApproved: true,

                upvoteCount: upvoteCount,

                upvotedBy: [],

                createdAt: createdAt,

                updatedAt: Timestamp.now(),

                timestamp: Timestamp.now()
            },
            {
                merge: true
            }
        );
    }

    await batch.commit();

    console.log("");
    console.log("====================================");
    console.log("10 demo issues created successfully!");
    console.log("====================================");
    console.log("");

    for (const issue of demoIssues) {
        console.log(
            `${issue.id} | ${issue.category} | ${issue.reporterName} | ${issue.address}`
        );
    }

    console.log("");
    console.log("All demo locations: Kothri Kalan, Bhopal");
    console.log("Images/audio intentionally left empty for now.");
    console.log("");
}

seedDemoData()
    .catch((error) => {
        console.error("Failed to seed demo data:");
        console.error(error);
        process.exit(1);
    });
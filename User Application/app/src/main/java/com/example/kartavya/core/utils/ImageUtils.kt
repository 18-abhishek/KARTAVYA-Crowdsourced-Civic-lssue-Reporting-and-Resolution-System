package com.example.kartavya.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Smart helper to resolve image models for Coil AsyncImage.
 * Maps any issue URL, ID, or title/category to local complaint drawables (img_001..img_010)
 * even when offline or backend is unreachable.
 */
fun getImageModel(
    imageStr: String?,
    issueId: String = "",
    title: String = "",
    category: String = "",
    context: Context
): Any {
    // If it's explicitly the resolved photo requested
    if (imageStr == "img_008_resolved" || imageStr == "008-resolved") {
        val resolvedId = context.resources.getIdentifier("img_008_resolved", "drawable", context.packageName)
        if (resolvedId != 0) return resolvedId
    }

    // 1. Try matching imageStr directly if it's a valid local drawable
    if (!imageStr.isNullOrBlank()) {
        val trimmed = imageStr.trim()
        val cleanName = trimmed
            .replace("R.drawable.", "")
            .removeSuffix(".png")
            .removeSuffix(".jpg")
            .replace("-", "_")

        val resId = context.resources.getIdentifier(cleanName, "drawable", context.packageName)
        if (resId != 0) return resId

        val prefixedId = context.resources.getIdentifier("img_$cleanName", "drawable", context.packageName)
        if (prefixedId != 0) return prefixedId

        // Check if string contains digits e.g. "007", "demo-issue-007", "001"
        val digits = trimmed.filter { it.isDigit() }
        if (digits.isNotBlank()) {
            val numInt = digits.toIntOrNull()
            if (numInt != null && numInt > 0) {
                val index = ((numInt - 1) % 10) + 1
                val numFormatted = "img_%03d".format(index)
                val numResId = context.resources.getIdentifier(numFormatted, "drawable", context.packageName)
                if (numResId != 0) return numResId
            }
        }
    }

    // 2. Extract digits from issueId (e.g., "demo-issue-007" -> 7 -> "img_007", "JH-9821" -> 21 -> "img_001")
    if (issueId.isNotBlank()) {
        val digits = issueId.filter { it.isDigit() }
        if (digits.isNotBlank()) {
            val numInt = digits.toIntOrNull()
            if (numInt != null && numInt > 0) {
                val index = ((numInt - 1) % 10) + 1
                val numFormatted = "img_%03d".format(index)
                val numResId = context.resources.getIdentifier(numFormatted, "drawable", context.packageName)
                if (numResId != 0) return numResId
            }
        }
    }

    // 3. Fallback based on title and category keywords
    val text = (title + " " + category).lowercase()
    val targetDrawable = when {
        text.contains("barrier") || text.contains("pothole") || text.contains("road") || text.contains("crack") || text.contains("surface") -> "img_001"
        text.contains("light") || text.contains("wire") || text.contains("lamp") || text.contains("electric") -> "img_002"
        text.contains("waste") || text.contains("garbage") || text.contains("bin") || text.contains("dump") -> "img_003"
        text.contains("water") || text.contains("pipe") || text.contains("leak") -> "img_004"
        text.contains("sewer") || text.contains("manhole") || text.contains("sanitation") -> "img_005"
        text.contains("traffic") || text.contains("signal") -> "img_006"
        text.contains("tree") || text.contains("power") -> "img_007"
        text.contains("sewage") || text.contains("choke") || text.contains("drain") -> "img_008"
        text.contains("park") || text.contains("bench") || text.contains("fence") -> "img_009"
        else -> "img_010"
    }

    val resId = context.resources.getIdentifier(targetDrawable, "drawable", context.packageName)
    if (resId != 0) return resId

    return context.resources.getIdentifier("img_001", "drawable", context.packageName)
}

// ──────────────────────────────────────────────────────
// Helper: Downsample Bitmap for UI Preview to prevent OOM
// ──────────────────────────────────────────────────────
fun loadDownsampledBitmap(context: Context, uri: android.net.Uri, reqWidth: Int = 1024, reqHeight: Int = 1024): Bitmap? {
    return try {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
        var inSampleSize = 1
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

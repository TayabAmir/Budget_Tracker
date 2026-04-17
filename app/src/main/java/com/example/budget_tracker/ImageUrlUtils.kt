package com.example.budget_tracker

import android.net.Uri

object ImageUrlUtils {

    fun normalizeGoogleImageUrl(rawUrl: String?): String? {
        val initialValue = rawUrl?.trim().orEmpty()
        if (initialValue.isEmpty()) {
            return null
        }

        val candidateUrl = if (initialValue.startsWith("http://") || initialValue.startsWith("https://")) {
            initialValue
        } else {
            "https://$initialValue"
        }

        return try {
            val uri = Uri.parse(candidateUrl)
            val host = uri.host?.lowercase().orEmpty()

            if (host.contains("drive.google.com")) {
                val id = extractDriveFileId(uri)
                if (!id.isNullOrBlank()) {
                    "https://drive.google.com/uc?export=view&id=$id"
                } else {
                    normalizeAnyHttpUrl(candidateUrl)
                }
            } else if (host == "www.google.com" || host == "google.com") {
                // Google image search links are HTML pages; extract direct image URL when present.
                val embeddedImageUrl = uri.getQueryParameter("imgurl")
                    ?: uri.getQueryParameter("url")
                    ?: uri.getQueryParameter("q")

                if (!embeddedImageUrl.isNullOrBlank() && embeddedImageUrl != candidateUrl) {
                    normalizeAnyHttpUrl(Uri.decode(embeddedImageUrl))
                } else {
                    normalizeAnyHttpUrl(candidateUrl)
                }
            } else {
                normalizeAnyHttpUrl(candidateUrl)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun normalizeAnyHttpUrl(value: String): String? {
        val uri = Uri.parse(value)
        val scheme = uri.scheme?.lowercase().orEmpty()
        val hasHost = !uri.host.isNullOrBlank()

        if (!hasHost || (scheme != "http" && scheme != "https")) {
            return null
        }

        return value.replaceFirst("http://", "https://")
    }

    private fun extractDriveFileId(uri: Uri): String? {
        val path = uri.path.orEmpty()
        val pathId = Regex("/file/d/([^/]+)").find(path)?.groupValues?.getOrNull(1)
        return pathId ?: uri.getQueryParameter("id")
    }
}

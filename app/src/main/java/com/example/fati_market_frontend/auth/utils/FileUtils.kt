package com.example.fati_market_frontend.auth.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

fun getFileName(context: Context, uri: Uri): String {
    var result = "Document"
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) result = cursor.getString(index)
            }
        }
    } catch (e: Exception) {
        result = "Selected document"
    }
    return result
}

package com.prayaas.bookbank.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.prayaas.bookbank.data.model.QrPayload

object QrCodeGenerator {

    /**
     * Generate a QR code bitmap for a book copy.
     * The QR encodes a JSON payload: {copyId, bookId, title, author}
     */
    fun generateQrBitmap(
        copyId: String,
        bookId: String,
        title: String,
        author: String,
        size: Int = 512
    ): Bitmap {
        val payload = QrPayload(
            copyId = copyId,
            bookId = bookId,
            title = title,
            author = author
        )
        val jsonString = Gson().toJson(payload)
        return encodeQr(jsonString, size)
    }

    /**
     * Generate QR from a raw string (fallback: just encode the copyId directly).
     */
    fun generateQrBitmapRaw(data: String, size: Int = 512): Bitmap = encodeQr(data, size)

    private fun encodeQr(content: String, size: Int): Bitmap {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    /**
     * Parse a scanned QR string into a QrPayload.
     * Handles both JSON format and plain copyId strings.
     */
    fun parseQrData(raw: String): QrPayload? {
        return try {
            Gson().fromJson(raw, QrPayload::class.java).takeIf { it.copyId.isNotBlank() }
        } catch (e: Exception) {
            // If not JSON, treat the whole string as copyId
            if (raw.isNotBlank()) QrPayload(copyId = raw) else null
        }
    }
}

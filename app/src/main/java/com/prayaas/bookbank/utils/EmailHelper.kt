package com.prayaas.bookbank.utils

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions

/**
 * EmailHelper wraps the Firebase Cloud Function that sends
 * book confirmation emails via SendGrid / Nodemailer.
 *
 * The Cloud Function (index.js) must be deployed to Firebase Functions.
 * See /firebase-functions/index.js in the project root.
 */
object EmailHelper {

    private val functions = FirebaseFunctions.getInstance()

    fun sendOrderConfirmation(
        toEmail: String,
        studentName: String,
        orderId: String,
        books: List<Pair<String, String>>,  // title, author
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val bookListHtml = books.joinToString(separator = "") { (title, author) ->
            """
            <tr>
              <td style="padding:8px 12px;border-bottom:1px solid #f0f0f0;">
                <strong>$title</strong><br>
                <span style="color:#666;font-size:13px;">by $author</span>
              </td>
            </tr>
            """.trimIndent()
        }

        val data = hashMapOf(
            "to" to toEmail,
            "subject" to "Prayaas Book Bank — Order Confirmed #${orderId.take(8).uppercase()}",
            "html" to buildEmailHtml(studentName, orderId, bookListHtml)
        )

        functions.getHttpsCallable("sendEmail")
            .call(data)
            .addOnSuccessListener {
                Log.d("EmailHelper", "Confirmation email sent to $toEmail")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("EmailHelper", "Email failed for $toEmail", e)
                onError(e)
            }
    }

    private fun buildEmailHtml(
        studentName: String,
        orderId: String,
        bookListHtml: String
    ): String = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8"/>
          <style>
            body { font-family: Arial, sans-serif; background: #f8f5f2; margin: 0; padding: 20px; }
            .card { background: white; border-radius: 16px; max-width: 520px; margin: 0 auto; overflow: hidden; }
            .header { background: #E8621A; padding: 24px; text-align: center; }
            .header h1 { color: white; margin: 0; font-size: 22px; }
            .header p { color: rgba(255,255,255,0.85); margin: 4px 0 0; font-size: 13px; }
            .body { padding: 24px; }
            .order-id { font-size: 12px; color: #999; margin-bottom: 4px; }
            .greeting { font-size: 18px; font-weight: bold; color: #1a1a1a; margin-bottom: 16px; }
            table { width: 100%; border-collapse: collapse; margin: 12px 0; }
            .pickup { background: #E3F2FD; border-radius: 10px; padding: 12px 16px; margin-top: 16px; font-size: 13px; color: #1565C0; }
            .footer { text-align: center; padding: 16px 24px; font-size: 11px; color: #999; border-top: 1px solid #f0f0f0; }
          </style>
        </head>
        <body>
          <div class="card">
            <div class="header">
              <h1>📚 Prayaas Book Bank</h1>
              <p>Your order is confirmed!</p>
            </div>
            <div class="body">
              <div class="order-id">Order #${orderId.take(8).uppercase()}</div>
              <div class="greeting">Hi, $studentName! 🎉</div>
              <p style="color:#555;font-size:14px;">Your book reservation has been placed successfully. Please collect your books from the Book Bank counter.</p>
              <p style="font-weight:bold;color:#1a1a1a;margin-bottom:4px;">Books reserved:</p>
              <table>$bookListHtml</table>
              <div class="pickup">
                📍 <strong>Pickup:</strong> Prayaas Book Bank counter<br>
                🕙 <strong>Hours:</strong> 10:00 AM – 5:00 PM, Monday to Saturday<br>
                🪪 Please carry your college ID card
              </div>
            </div>
            <div class="footer">Prayaas Book Bank · College Library System<br>This is an automated message, please do not reply.</div>
          </div>
        </body>
        </html>
    """.trimIndent()
}

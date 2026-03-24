/**
 * Firebase Cloud Functions — Prayaas Book Bank
 * Handles sending confirmation emails via Nodemailer (Gmail SMTP)
 * or SendGrid (recommended for production).
 *
 * Deploy with:
 *   cd firebase-functions
 *   npm install
 *   firebase deploy --only functions
 *
 * Set secrets:
 *   firebase functions:secrets:set GMAIL_USER
 *   firebase functions:secrets:set GMAIL_PASS
 *   (or SENDGRID_API_KEY for SendGrid)
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

admin.initializeApp();

// ─── Nodemailer transport (Gmail SMTP) ────────────────────────────────────────
// For production, replace with SendGrid:
// const sgMail = require("@sendgrid/mail");
// sgMail.setApiKey(process.env.SENDGRID_API_KEY);

function createTransport() {
  return nodemailer.createTransport({
    service: "gmail",
    auth: {
      user: process.env.GMAIL_USER,
      pass: process.env.GMAIL_PASS,   // Use an App Password, not your real password
    },
  });
}

// ─── sendEmail — generic callable ────────────────────────────────────────────
exports.sendEmail = functions.https.onCall(async (data, context) => {
  const { to, subject, html } = data;

  if (!to || !subject || !html) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Missing required fields: to, subject, html"
    );
  }

  try {
    const transporter = createTransport();
    await transporter.sendMail({
      from: `"Prayaas Book Bank" <${process.env.GMAIL_USER}>`,
      to,
      subject,
      html,
    });
    return { success: true };
  } catch (err) {
    console.error("sendEmail error:", err);
    throw new functions.https.HttpsError("internal", err.message);
  }
});

// ─── sendBookConfirmationEmail — specific callable ────────────────────────────
exports.sendBookConfirmationEmail = functions.https.onCall(async (data, context) => {
  const { to, studentName, bookList } = data;

  if (!to || !studentName || !bookList) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Missing required fields"
    );
  }

  const html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8"/>
      <style>
        body{font-family:Arial,sans-serif;background:#f8f5f2;margin:0;padding:20px}
        .card{background:white;border-radius:16px;max-width:520px;margin:0 auto;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08)}
        .header{background:#E8621A;padding:24px;text-align:center}
        .header h1{color:white;margin:0;font-size:22px;font-weight:700}
        .header p{color:rgba(255,255,255,0.85);margin:4px 0 0;font-size:13px}
        .body{padding:24px}
        .greeting{font-size:18px;font-weight:bold;color:#1a1a1a;margin-bottom:12px}
        .book-list{background:#FFF0E8;border-radius:10px;padding:14px;margin:14px 0}
        .pickup{background:#E3F2FD;border-radius:10px;padding:12px 16px;margin-top:16px;font-size:13px;color:#1565C0;line-height:1.6}
        .footer{text-align:center;padding:14px 24px;font-size:11px;color:#999;border-top:1px solid #f0f0f0}
      </style>
    </head>
    <body>
      <div class="card">
        <div class="header">
          <h1>📚 Prayaas Book Bank</h1>
          <p>Your order is confirmed!</p>
        </div>
        <div class="body">
          <div class="greeting">Hi, ${studentName}! 🎉</div>
          <p style="color:#555;font-size:14px;line-height:1.6">
            Your book reservation has been placed successfully.
            Please collect your books from the Prayaas Book Bank counter at the earliest.
          </p>
          <div class="book-list">
            <p style="margin:0 0 8px;font-weight:bold;color:#B84D10;font-size:13px">
              Books reserved:
            </p>
            ${bookList}
          </div>
          <div class="pickup">
            📍 <strong>Pickup:</strong> Prayaas Book Bank counter<br>
            🕙 <strong>Hours:</strong> 10:00 AM – 5:00 PM, Monday to Saturday<br>
            🪪 Please carry your college ID card when collecting books
          </div>
        </div>
        <div class="footer">
          Prayaas Book Bank · College Library System<br>
          This is an automated message, please do not reply.
        </div>
      </div>
    </body>
    </html>
  `;

  try {
    const transporter = createTransport();
    await transporter.sendMail({
      from: `"Prayaas Book Bank" <${process.env.GMAIL_USER}>`,
      to,
      subject: "Prayaas Book Bank — Your Order is Confirmed! 📚",
      html,
    });
    return { success: true };
  } catch (err) {
    console.error("sendBookConfirmationEmail error:", err);
    throw new functions.https.HttpsError("internal", err.message);
  }
});

// ─── Firestore trigger: auto-send email when order is created ─────────────────
exports.onOrderCreated = functions.firestore
  .document("orders/{orderId}")
  .onCreate(async (snap, context) => {
    const order = snap.data();
    const { email, studentName, books } = order;
    if (!email || !studentName || !books) return null;

    const bookList = books
      .map(
        (b) =>
          `<p style="margin:4px 0;font-size:13px;color:#1a1a1a;">
            • <strong>${b.bookTitle}</strong> <span style="color:#666">by ${b.bookAuthor}</span>
          </p>`
      )
      .join("");

    const html = `
      <div style="font-family:Arial,sans-serif;background:#f8f5f2;padding:20px">
        <div style="background:white;border-radius:16px;max-width:520px;margin:0 auto;overflow:hidden">
          <div style="background:#E8621A;padding:24px;text-align:center">
            <h1 style="color:white;margin:0;font-size:22px">📚 Prayaas Book Bank</h1>
            <p style="color:rgba(255,255,255,0.85);margin:4px 0 0;font-size:13px">Order Confirmed</p>
          </div>
          <div style="padding:24px">
            <h2 style="font-size:18px;color:#1a1a1a">Hi, ${studentName}! 🎉</h2>
            <p style="color:#555;font-size:14px">Your book reservation is confirmed. Please collect books from the counter.</p>
            <div style="background:#FFF0E8;border-radius:10px;padding:14px;margin:14px 0">
              <p style="margin:0 0 8px;font-weight:bold;color:#B84D10;font-size:13px">Books reserved:</p>
              ${bookList}
            </div>
            <div style="background:#E3F2FD;border-radius:10px;padding:12px;font-size:13px;color:#1565C0;line-height:1.7">
              📍 <b>Pickup:</b> Prayaas Book Bank counter<br>
              🕙 <b>Hours:</b> 10 AM – 5 PM, Mon–Sat<br>
              🪪 Carry your college ID
            </div>
          </div>
          <div style="text-align:center;padding:12px;font-size:11px;color:#999;border-top:1px solid #f0f0f0">
            Prayaas Book Bank · Automated message
          </div>
        </div>
      </div>
    `;

    try {
      const transporter = createTransport();
      await transporter.sendMail({
        from: `"Prayaas Book Bank" <${process.env.GMAIL_USER}>`,
        to: email,
        subject: "Prayaas Book Bank — Your Order is Confirmed! 📚",
        html,
      });
      console.log(`Confirmation email sent to ${email}`);
      return null;
    } catch (err) {
      console.error("onOrderCreated email failed:", err);
      return null;
    }
  });

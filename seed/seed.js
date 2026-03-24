/**
 * Prayaas Book Bank — Firestore Seed Script
 *
 * Run once to populate your Firestore with sample books and their physical copies.
 *
 * Usage:
 *   1. Install: npm install firebase-admin
 *   2. Download your Firebase service account key from:
 *      Firebase Console → Project Settings → Service accounts → Generate new private key
 *   3. Save it as serviceAccountKey.json in this directory
 *   4. node seed.js
 */

const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const db = admin.firestore();

const books = [
  {
    title: "Data Structures and Algorithms",
    author: "Thomas H. Cormen",
    isbn: "978-0262033848",
    subject: "Computer Science",
    summary:
      "The definitive reference on algorithms covering sorting, searching, graph algorithms, and dynamic programming with rigorous mathematical foundations. Used worldwide in CS undergraduate and graduate programs.",
    coverImageUrl: "",
    totalCopies: 6,
    availableCopies: 4,
    edition: "4th Edition",
    pages: 1292,
    publisher: "MIT Press",
  },
  {
    title: "Operating System Concepts",
    author: "Abraham Silberschatz",
    isbn: "978-1119800361",
    subject: "Computer Science",
    summary:
      "Known as the Dinosaur Book. Covers process management, memory management, file systems, storage, security, and distributed systems. The standard OS textbook used globally.",
    coverImageUrl: "",
    totalCopies: 4,
    availableCopies: 1,
    edition: "10th Edition",
    pages: 976,
    publisher: "Wiley",
  },
  {
    title: "Computer Networks",
    author: "Andrew S. Tanenbaum",
    isbn: "978-0132126953",
    subject: "Computer Science",
    summary:
      "A comprehensive guide to computer networking covering TCP/IP, routing algorithms, the application layer, and network security with practical real-world examples and protocol analysis.",
    coverImageUrl: "",
    totalCopies: 8,
    availableCopies: 6,
    edition: "5th Edition",
    pages: 960,
    publisher: "Pearson",
  },
  {
    title: "Database System Concepts",
    author: "Henry F. Korth",
    isbn: "978-0078022159",
    subject: "Computer Science",
    summary:
      "Covers relational algebra, SQL, database design, normalization, query optimization, transactions, concurrency control, and distributed databases. Essential for DBMS courses.",
    coverImageUrl: "",
    totalCopies: 3,
    availableCopies: 0,
    edition: "7th Edition",
    pages: 1376,
    publisher: "McGraw-Hill",
  },
  {
    title: "Compilers: Principles, Techniques, and Tools",
    author: "Alfred V. Aho",
    isbn: "978-0321486813",
    subject: "Computer Science",
    summary:
      "The Dragon Book — the definitive compiler construction text covering lexical analysis, parsing, semantic analysis, intermediate code generation, optimization, and code generation.",
    coverImageUrl: "",
    totalCopies: 5,
    availableCopies: 3,
    edition: "2nd Edition",
    pages: 1009,
    publisher: "Pearson",
  },
  {
    title: "Engineering Mathematics",
    author: "B.S. Grewal",
    isbn: "978-8174091955",
    subject: "Mathematics",
    summary:
      "Comprehensive coverage of topics in engineering mathematics including calculus, differential equations, linear algebra, transforms, probability, and numerical methods. Widely used across Indian engineering colleges.",
    coverImageUrl: "",
    totalCopies: 10,
    availableCopies: 7,
    edition: "44th Edition",
    pages: 1327,
    publisher: "Khanna Publishers",
  },
  {
    title: "Signals and Systems",
    author: "Alan V. Oppenheim",
    isbn: "978-0138147570",
    subject: "Electronics",
    summary:
      "A thorough treatment of continuous-time and discrete-time signals and systems including Fourier transforms, Laplace transforms, Z-transforms, and applications in communications and control.",
    coverImageUrl: "",
    totalCopies: 4,
    availableCopies: 2,
    edition: "2nd Edition",
    pages: 957,
    publisher: "Pearson",
  },
  {
    title: "Engineering Physics",
    author: "H.K. Malik",
    isbn: "978-0070681132",
    subject: "Physics",
    summary:
      "Covers quantum mechanics, solid state physics, laser technology, fiber optics, and nanotechnology relevant to engineering students. Includes solved problems and numerical examples.",
    coverImageUrl: "",
    totalCopies: 6,
    availableCopies: 5,
    edition: "3rd Edition",
    pages: 730,
    publisher: "McGraw-Hill",
  },
];

async function seed() {
  console.log("🌱 Starting Firestore seed…\n");

  for (const book of books) {
    const bookRef = await db.collection("books").add(book);
    console.log(`📚 Created book: "${book.title}" → ${bookRef.id}`);

    // Create one document per physical copy
    for (let i = 0; i < book.totalCopies; i++) {
      const copyNum = String(i + 1).padStart(3, "0");
      const prefix = book.title
        .split(" ")
        .slice(0, 2)
        .map((w) => w[0].toUpperCase())
        .join("");
      const copyId = `${prefix}-${bookRef.id.slice(0, 4).toUpperCase()}-${copyNum}`;

      const qrPayload = JSON.stringify({
        copyId,
        bookId: bookRef.id,
        title: book.title,
        author: book.author,
      });

      await db.collection("bookCopies").add({
        bookId: bookRef.id,
        qrData: copyId,          // Simple ID string — use QrCodeGenerator to generate actual QR
        isAvailable: i < book.availableCopies,
        currentOrderId: null,
      });
      console.log(`   📎 Copy ${copyNum}: ${copyId} (available: ${i < book.availableCopies})`);
    }
  }

  console.log("\n✅ Seed complete!");
  process.exit(0);
}

seed().catch((err) => {
  console.error("Seed failed:", err);
  process.exit(1);
});

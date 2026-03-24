package com.prayaas.bookbank.data.repository

import com.prayaas.bookbank.data.model.Book
import com.prayaas.bookbank.data.model.Order
import com.prayaas.bookbank.data.model.OrderBook
import com.prayaas.bookbank.data.model.OrderStatus

/**
 * LocalDataSource provides hard-coded sample data so the app is fully
 * navigable even before Firebase / google-services.json is configured.
 *
 * BookRepository uses this as a fallback when Firestore is unavailable.
 */
object LocalDataSource {

    val sampleBooks: List<Book> = listOf(
        Book(
            id = "local_001",
            title = "Data Structures and Algorithms",
            author = "Thomas H. Cormen",
            isbn = "978-0262033848",
            subject = "Computer Science",
            summary = "The definitive reference on algorithms covering sorting, searching, graph algorithms, and dynamic programming with rigorous mathematical foundations. Used worldwide in CS undergraduate and graduate programs.",
            coverImageUrl = "",
            totalCopies = 6,
            availableCopies = 4,
            edition = "4th Edition",
            pages = 1292,
            publisher = "MIT Press"
        ),
        Book(
            id = "local_002",
            title = "Operating System Concepts",
            author = "Abraham Silberschatz",
            isbn = "978-1119800361",
            subject = "Computer Science",
            summary = "Known as the Dinosaur Book. Covers process management, memory management, file systems, storage, security, and distributed systems. The standard OS textbook used globally.",
            coverImageUrl = "",
            totalCopies = 4,
            availableCopies = 1,
            edition = "10th Edition",
            pages = 976,
            publisher = "Wiley"
        ),
        Book(
            id = "local_003",
            title = "Computer Networks",
            author = "Andrew S. Tanenbaum",
            isbn = "978-0132126953",
            subject = "Computer Science",
            summary = "A comprehensive guide to computer networking covering TCP/IP, routing algorithms, the application layer, and network security with practical real-world examples and protocol analysis.",
            coverImageUrl = "",
            totalCopies = 8,
            availableCopies = 6,
            edition = "5th Edition",
            pages = 960,
            publisher = "Pearson"
        ),
        Book(
            id = "local_004",
            title = "Database System Concepts",
            author = "Henry F. Korth",
            isbn = "978-0078022159",
            subject = "Computer Science",
            summary = "Covers relational algebra, SQL, database design, normalization, query optimization, transactions, concurrency control, and distributed databases. Essential for DBMS courses.",
            coverImageUrl = "",
            totalCopies = 3,
            availableCopies = 0,
            edition = "7th Edition",
            pages = 1376,
            publisher = "McGraw-Hill"
        ),
        Book(
            id = "local_005",
            title = "Compilers: Principles, Techniques, and Tools",
            author = "Alfred V. Aho",
            isbn = "978-0321486813",
            subject = "Computer Science",
            summary = "The Dragon Book — the definitive compiler construction text covering lexical analysis, parsing, semantic analysis, intermediate code generation, optimization, and code generation.",
            coverImageUrl = "",
            totalCopies = 5,
            availableCopies = 3,
            edition = "2nd Edition",
            pages = 1009,
            publisher = "Pearson"
        ),
        Book(
            id = "local_006",
            title = "Engineering Mathematics",
            author = "B.S. Grewal",
            isbn = "978-8174091955",
            subject = "Mathematics",
            summary = "Comprehensive coverage of topics in engineering mathematics including calculus, differential equations, linear algebra, integral transforms, probability, and numerical methods.",
            coverImageUrl = "",
            totalCopies = 10,
            availableCopies = 7,
            edition = "44th Edition",
            pages = 1327,
            publisher = "Khanna Publishers"
        ),
        Book(
            id = "local_007",
            title = "Signals and Systems",
            author = "Alan V. Oppenheim",
            isbn = "978-0138147570",
            subject = "Electronics",
            summary = "A thorough treatment of continuous-time and discrete-time signals and systems including Fourier transforms, Laplace transforms, Z-transforms, and applications in communications and control systems.",
            coverImageUrl = "",
            totalCopies = 4,
            availableCopies = 2,
            edition = "2nd Edition",
            pages = 957,
            publisher = "Pearson"
        ),
        Book(
            id = "local_008",
            title = "Engineering Physics",
            author = "H.K. Malik",
            isbn = "978-0070681132",
            subject = "Physics",
            summary = "Covers quantum mechanics, solid state physics, laser technology, fiber optics, and nanotechnology relevant to engineering students, with solved problems and numerical examples.",
            coverImageUrl = "",
            totalCopies = 6,
            availableCopies = 5,
            edition = "3rd Edition",
            pages = 730,
            publisher = "McGraw-Hill"
        ),
        Book(
            id = "local_009",
            title = "Microprocessors and Microcontrollers",
            author = "N. Senthil Kumar",
            isbn = "978-0199456512",
            subject = "Electronics",
            summary = "Covers 8085, 8086 microprocessors and 8051 microcontroller architecture, programming, interfacing, and applications. Includes assembly language programming with practical examples.",
            coverImageUrl = "",
            totalCopies = 5,
            availableCopies = 3,
            edition = "2nd Edition",
            pages = 672,
            publisher = "Oxford University Press"
        ),
        Book(
            id = "local_010",
            title = "Software Engineering",
            author = "Ian Sommerville",
            isbn = "978-0137035151",
            subject = "Computer Science",
            summary = "Comprehensive coverage of software engineering processes, requirements engineering, software design, testing, project management, and software quality for students and practitioners.",
            coverImageUrl = "",
            totalCopies = 7,
            availableCopies = 4,
            edition = "10th Edition",
            pages = 816,
            publisher = "Pearson"
        )
    )

    val sampleOrders: List<Order> = listOf(
        Order(
            orderId = "demo_order_001",
            studentName = "Priya Mehta",
            studentId = "2022CS047",
            mobileNumber = "9123456789",
            email = "priya@college.edu",
            status = OrderStatus.PENDING,
            books = listOf(
                OrderBook(
                    bookId = "local_001",
                    bookTitle = "Data Structures and Algorithms",
                    bookAuthor = "Thomas H. Cormen",
                    copyId = null,
                    isReturned = false
                ),
                OrderBook(
                    bookId = "local_003",
                    bookTitle = "Computer Networks",
                    bookAuthor = "Andrew S. Tanenbaum",
                    copyId = null,
                    isReturned = false
                )
            )
        ),
        Order(
            orderId = "demo_order_002",
            studentName = "Arjun Patel",
            studentId = "2021EC023",
            mobileNumber = "9876543210",
            email = "arjun@college.edu",
            status = OrderStatus.ISSUED,
            books = listOf(
                OrderBook(
                    bookId = "local_002",
                    bookTitle = "Operating System Concepts",
                    bookAuthor = "Abraham Silberschatz",
                    copyId = "OS-LO02-001",
                    isReturned = false
                )
            )
        ),
        Order(
            orderId = "demo_order_003",
            studentName = "Sneha Reddy",
            studentId = "2022ME011",
            mobileNumber = "9000112233",
            email = "sneha@college.edu",
            status = OrderStatus.RETURNED,
            books = listOf(
                OrderBook(
                    bookId = "local_005",
                    bookTitle = "Compilers: Principles, Techniques, and Tools",
                    bookAuthor = "Alfred V. Aho",
                    copyId = "CP-LO05-002",
                    isReturned = true
                )
            )
        ),
        Order(
            orderId = "demo_order_004",
            studentName = "Vikram Singh",
            studentId = "2023CS099",
            mobileNumber = "9988776655",
            email = "vikram@college.edu",
            status = OrderStatus.PARTIAL,
            books = listOf(
                OrderBook(
                    bookId = "local_006",
                    bookTitle = "Engineering Mathematics",
                    bookAuthor = "B.S. Grewal",
                    copyId = "EM-LO06-003",
                    isReturned = false
                ),
                OrderBook(
                    bookId = "local_008",
                    bookTitle = "Engineering Physics",
                    bookAuthor = "H.K. Malik",
                    copyId = null,
                    isReturned = false
                )
            )
        )
    )
}

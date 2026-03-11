import java.io.*;
import java.util.*;

// --- 1. Classes (OOP Concept) ---

// Book Class: Book ki details store karega
class Book {
    private int id;
    private String title;
    private String author;
    private boolean isAvailable;

    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isAvailable = true;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    @Override
    public String toString() {
        String status = isAvailable ? "Available" : "Issued";
        return String.format("[ID: %d] '%s' by %s - %s", id, title, author, status);
    }
}

// Student Class: Student ki details aur issued books store karega
class Student {
    private int id;
    private String name;
    private List<Integer> issuedBooks; // List of Book IDs

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
        this.issuedBooks = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public List<Integer> getIssuedBooks() { return issuedBooks; }

    @Override
    public String toString() {
        String books = issuedBooks.isEmpty() ? "None" : issuedBooks.toString();
        return String.format("[ID: %d] %s - Issued Books: %s", id, name, books);
    }
}

// --- 2. Library Management System (Main Logic) ---

public class LibraryManagementSystem {
    private List<Book> books;
    private List<Student> students;
    private Scanner scanner;
    
    // File names for persistence
    private static final String BOOK_FILE = "books.txt";
    private static final String STUDENT_FILE = "students.txt";

    public LibraryManagementSystem() {
        books = new ArrayList<>();
        students = new ArrayList<>();
        scanner = new Scanner(System.in);
        loadData(); // Load data on startup
    }

    // --- File Handling (Save Data) ---
    private void saveData() {
        try {
            // Save Books
            PrintWriter bookWriter = new PrintWriter(new FileWriter(BOOK_FILE));
            for (Book book : books) {
                bookWriter.println(book.getId() + "," + book.getTitle() + "," + book.getAuthor() + "," + (book.isAvailable() ? "1" : "0"));
            }
            bookWriter.close();

            // Save Students
            PrintWriter studentWriter = new PrintWriter(new FileWriter(STUDENT_FILE));
            for (Student student : students) {
                String bookIds = String.join(",", student.getIssuedBooks().stream().map(String::valueOf).toArray(String[]::new));
                studentWriter.println(student.getId() + "," + student.getName() + "," + bookIds);
            }
            studentWriter.close();
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    // --- File Handling (Load Data) ---
    private void loadData() {
        // Load Books
        if (new File(BOOK_FILE).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(BOOK_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 4) {
                        int id = Integer.parseInt(parts[0]);
                        String title = parts[1];
                        String author = parts[2];
                        boolean available = parts[3].equals("1");
                        books.add(new Book(id, title, author));
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading books: " + e.getMessage());
            }
        }

        // Load Students
        if (new File(STUDENT_FILE).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        int id = Integer.parseInt(parts[0]);
                        String name = parts[1];
                        Student student = new Student(id, name);
                        if (!parts[2].isEmpty()) {
                            String[] bookIds = parts[2].split(",");
                            for (String idStr : bookIds) {
                                student.getIssuedBooks().add(Integer.parseInt(idStr));
                            }
                        }
                        students.add(student);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading students: " + e.getMessage());
            }
        }
    }

    // --- Feature: Add Book ---
    public void addBook() {
        System.out.print("Enter Book Title: ");
        String title = scanner.nextLine();
        System.out.print("Enter Author Name: ");
        String author = scanner.nextLine();

        int newId = 1;
        if (!books.isEmpty()) {
            newId = books.get(books.size() - 1).getId() + 1;
        }

        Book newBook = new Book(newId, title, author);
        books.add(newBook);
        saveData();
        System.out.println("Book Added Successfully! ID: " + newId);
    }

    // --- Feature: Add Student ---
    public void addStudent() {
        System.out.print("Enter Student Name: ");
        String name = scanner.nextLine();

        int newId = 1;
        if (!students.isEmpty()) {
            newId = students.get(students.size() - 1).getId() + 1;
        }

        Student newStudent = new Student(newId, name);
        students.add(newStudent);
        saveData();
        System.out.println("Student Added Successfully! ID: " + newId);
    }

    // --- Feature: Issue Book ---
    public void issueBook() {
        System.out.print("Enter Book ID to Issue: ");
        int bookId = scanner.nextInt();
        System.out.print("Enter Student ID: ");
        int studentId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        Book book = findBook(bookId);
        Student student = findStudent(studentId);

        if (book == null) {
            System.out.println("Book not found!");
            return;
        }
        if (student == null) {
            System.out.println("Student not found!");
            return;
        }
        if (!book.isAvailable()) {
            System.out.println("Book is already issued!");
            return;
        }

        book.setAvailable(false);
        student.getIssuedBooks().add(bookId);
        saveData();
        System.out.println("Book '" + book.getTitle() + "' issued to " + student.getName());
    }

    // --- Feature: Return Book ---
    public void returnBook() {
        System.out.print("Enter Book ID to Return: ");
        int bookId = scanner.nextInt();
        System.out.print("Enter Student ID: ");
        int studentId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        Book book = findBook(bookId);
        Student student = findStudent(studentId);

        if (book == null) {
            System.out.println("Book not found!");
            return;
        }
        if (student == null) {
            System.out.println("Student not found!");
            return;
        }
        if (!student.getIssuedBooks().contains(bookId)) {
            System.out.println("This student does not have this book!");
            return;
        }

        book.setAvailable(true);
        student.getIssuedBooks().remove(Integer.valueOf(bookId));
        saveData();
        System.out.println("Book '" + book.getTitle() + "' returned successfully.");
    }

    // Helper Methods
    private Book findBook(int id) {
        for (Book b : books) {
            if (b.getId() == id) return b;
        }
        return null;
    }

    private Student findStudent(int id) {
        for (Student s : students) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    public void displayBooks() {
        System.out.println("\n--- All Books ---");
        if (books.isEmpty()) System.out.println("No books available.");
        for (Book b : books) System.out.println(b);
    }

    public void displayStudents() {
        System.out.println("\n--- All Students ---");
        if (students.isEmpty()) System.out.println("No students registered.");
        for (Student s : students) System.out.println(s);
    }

    // --- Main Menu ---
    public void start() {
        while (true) {
            System.out.println("\n=== Library Management System ===");
            System.out.println("1. Add Book");
            System.out.println("2. Add Student");
            System.out.println("3. Issue Book");
            System.out.println("4. Return Book");
            System.out.println("5. View All Books");
            System.out.println("6. View All Students");
            System.out.println("7. Exit");
            System.out.print("Enter Choice (1-7): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1: addBook(); break;
                case 2: addStudent(); break;
                case 3: issueBook(); break;
                case 4: returnBook(); break;
                case 5: displayBooks(); break;
                case 6: displayStudents(); break;
                case 7: 
                    System.out.println("Exiting System...");
                    return;
                default: System.out.println("Invalid Choice!");
            }
        }
    }

    // Entry Point
    public static void main(String[] args) {
        LibraryManagementSystem library = new LibraryManagementSystem();
        library.start();
    }
}
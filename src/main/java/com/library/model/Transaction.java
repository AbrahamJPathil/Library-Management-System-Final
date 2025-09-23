package com.library.model;

import java.time.LocalDate;

public class Transaction {
    private int id;
    private String bookIsbn;
    private String bookTitle;
    private String userName;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private boolean returned;

    public Transaction(int id, String bookIsbn, String bookTitle, String userName, LocalDate issueDate, LocalDate dueDate, boolean returned) {
        this.id = id;
        this.bookIsbn = bookIsbn;
        this.bookTitle = bookTitle;
        this.userName = userName;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returned = returned;
    }

    // Standard getters
    public int getId() { return id; }
    public String getBookIsbn() { return bookIsbn; }
    public String getBookTitle() { return bookTitle; }
    public String getUserName() { return userName; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isReturned() { return returned; }
}

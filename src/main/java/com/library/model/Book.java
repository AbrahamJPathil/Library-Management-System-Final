package com.library.model;

public class Book {
    private String title;
    private String author;
    private String genre;
    private String isbn;
    private boolean available;

    public Book(String title, String author, String genre, String isbn, boolean available) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.isbn = isbn;
        this.available = available;
    }

    // Standard getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}

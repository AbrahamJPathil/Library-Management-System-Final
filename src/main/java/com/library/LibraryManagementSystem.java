package com.library;

import com.library.model.*;
import com.library.dao.*;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class LibraryManagementSystem extends Application {

    private final BookDao bookDao = new BookDao();
    private final UserDao userDao = new UserDao();
    private final TransactionDao transactionDao = new TransactionDao();

    private final ObservableList<Book> books = FXCollections.observableArrayList();
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private final ObservableList<Transaction> userTransactions = FXCollections.observableArrayList();

    private Stage primaryStage;
    private BorderPane rootLayout;
    private User loggedInUser;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Library Management System");
        initRootLayout();
        showLoginScreen();
    }

    private void initRootLayout() {
        rootLayout = new BorderPane();
        Scene scene = new Scene(rootLayout, 1100, 750);
        try {
            String cssPath = getClass().getResource("style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Could not load CSS file: " + e.getMessage());
        }
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showLoginScreen() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25));
        Label title = new Label("Library Management System");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        grid.add(title, 0, 0, 2, 1);
        grid.add(new Label("Email:"), 0, 1);
        TextField emailField = new TextField();
        grid.add(emailField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        PasswordField passwordField = new PasswordField();
        grid.add(passwordField, 1, 2);
        Button loginBtn = new Button("Sign in");
        loginBtn.setDefaultButton(true);
        loginBtn.setStyle("-fx-background-color: #2E86AB; -fx-text-fill: white;");
        loginBtn.setOnAction(e -> {
            Optional<User> user = userDao.authenticate(emailField.getText(), passwordField.getText());
            user.ifPresentOrElse(
                authenticatedUser -> {
                    this.loggedInUser = authenticatedUser;
                    showAlert(Alert.AlertType.INFORMATION, "Login Success", "Welcome " + authenticatedUser.getName());
                    if ("Administrator".equals(authenticatedUser.getRole())) {
                        showAdminDashboard();
                    } else {
                        showStudentDashboard();
                    }
                },
                () -> showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.")
            );
        });
        HBox hbBtn = new HBox(10, loginBtn);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        grid.add(hbBtn, 1, 4);
        Hyperlink registerLink = new Hyperlink("Register here");
        registerLink.setOnAction(e -> showRegistrationScreen());
        grid.add(registerLink, 1, 5);
        rootLayout.setCenter(grid);
    }

    private void showRegistrationScreen() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25));
        Label title = new Label("Create New Student Account");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        grid.add(title, 0, 0, 2, 1);
        grid.add(new Label("Full Name:"), 0, 1);
        TextField nameField = new TextField();
        grid.add(nameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        TextField emailField = new TextField();
        grid.add(emailField, 1, 2);
        grid.add(new Label("Password:"), 0, 3);
        PasswordField passwordField = new PasswordField();
        grid.add(passwordField, 1, 3);
        
        Button registerBtn = new Button("Register");
        registerBtn.setOnAction(e -> {
            if (nameField.getText().isEmpty() || emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Form Error", "All fields are required.");
                return;
            }
            User newUser = new User(nameField.getText(), emailField.getText(), passwordField.getText(), "Student", LocalDate.now());
            try {
                userDao.addUser(newUser);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Registration successful! Please log in.");
                showLoginScreen();
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not register user. Email might already exist.");
            }
        });
        Button backBtn = new Button("Back to Login");
        backBtn.setOnAction(e -> showLoginScreen());
        HBox buttonBox = new HBox(10, backBtn, registerBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttonBox, 1, 4);
        rootLayout.setCenter(grid);
    }

    private void showAdminDashboard() {
        BorderPane dashboard = new BorderPane();
        dashboard.setTop(createHeader("Admin Dashboard"));
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
            new Tab("Dashboard", createAdminDashboardContent()),
            new Tab("User Management", createUsersContent()),
            new Tab("Book Management", createBookManagementContent()),
            new Tab("Transactions", createTransactionsContent())
        );
        tabPane.getTabs().forEach(t -> t.setClosable(false));
        dashboard.setCenter(tabPane);
        rootLayout.setCenter(dashboard);
        refreshAllAdminData();
    }
    
    private void refreshAllAdminData() {
        books.setAll(bookDao.getAllBooks());
        users.setAll(userDao.getAllUsers());
        transactions.setAll(transactionDao.getAllTransactions());
    }

    private void showStudentDashboard() {
        BorderPane dashboard = new BorderPane();
        dashboard.setTop(createHeader("Student Dashboard"));
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
            new Tab("Browse Books", createStudentBrowseBooksContent()),
            new Tab("My Issued Books", createStudentMyBooksContent()),
            new Tab("My Book History", createStudentHistoryContent())
        );
        tabPane.getTabs().forEach(t -> t.setClosable(false));
        dashboard.setCenter(tabPane);
        rootLayout.setCenter(dashboard);
        refreshAllStudentData();
    }

    private void refreshAllStudentData() {
        books.setAll(bookDao.getAllBooks());
        userTransactions.setAll(transactionDao.getTransactionsForUser(loggedInUser.getEmail(), false));
    }

    private VBox createStudentBrowseBooksContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        TableView<Book> booksTable = new TableView<>(books);
        VBox.setVgrow(booksTable, Priority.ALWAYS);
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        TableColumn<Book, String> genreCol = new TableColumn<>("Genre");
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        TableColumn<Book, Boolean> availableCol = new TableColumn<>("Available");
        availableCol.setCellValueFactory(new PropertyValueFactory<>("available"));
        booksTable.getColumns().addAll(titleCol, authorCol, genreCol, availableCol);
        content.getChildren().add(booksTable);
        return content;
    }

    private VBox createStudentMyBooksContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        Label title = new Label("Books Currently Issued to Me");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        FilteredList<Transaction> activeTransactions = new FilteredList<>(userTransactions, t -> !t.isReturned());
        TableView<Transaction> booksTable = new TableView<>(activeTransactions);
        VBox.setVgrow(booksTable, Priority.ALWAYS);
        TableColumn<Transaction, String> bookCol = new TableColumn<>("Book Title");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        TableColumn<Transaction, LocalDate> issueCol = new TableColumn<>("Issue Date");
        issueCol.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        TableColumn<Transaction, LocalDate> dueCol = new TableColumn<>("Due Date");
        dueCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        booksTable.getColumns().addAll(bookCol, issueCol, dueCol);
        content.getChildren().addAll(title, booksTable);
        return content;
    }

    private VBox createStudentHistoryContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        Label title = new Label("My Complete Borrowing History");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        FilteredList<Transaction> returnedTransactions = new FilteredList<>(userTransactions, Transaction::isReturned);
        TableView<Transaction> historyTable = new TableView<>(returnedTransactions);
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        TableColumn<Transaction, String> bookCol = new TableColumn<>("Book Title");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        TableColumn<Transaction, LocalDate> issueCol = new TableColumn<>("Issue Date");
        issueCol.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        TableColumn<Transaction, Boolean> returnedCol = new TableColumn<>("Returned");
        returnedCol.setCellValueFactory(new PropertyValueFactory<>("returned"));
        historyTable.getColumns().addAll(bookCol, issueCol, returnedCol);
        content.getChildren().addAll(title, historyTable);
        return content;
    }
    
    private VBox createUsersContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        Button addUserBtn = new Button("Add New User");
        addUserBtn.setStyle("-fx-background-color: #28A745; -fx-text-fill: white;");
        addUserBtn.setOnAction(e -> showAddUserDialog());
        TableView<User> usersTable = new TableView<>(users);
        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        TableColumn<User, LocalDate> joinDateCol = new TableColumn<>("Join Date");
        joinDateCol.setCellValueFactory(new PropertyValueFactory<>("joinDate"));
        TableColumn<User, Void> actionCol = createUserActionColumn();
        usersTable.getColumns().addAll(nameCol, emailCol, roleCol, joinDateCol, actionCol);
        VBox.setVgrow(usersTable, Priority.ALWAYS);
        content.getChildren().addAll(addUserBtn, usersTable);
        return content;
    }

    private VBox createBookManagementContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        Button addBookBtn = new Button("Add New Book");
        addBookBtn.setStyle("-fx-background-color: #28A745; -fx-text-fill: white;");
        addBookBtn.setOnAction(e -> showAddBookDialog());
        TableView<Book> booksTable = new TableView<>(books);
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        TableColumn<Book, String> genreCol = new TableColumn<>("Genre");
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        TableColumn<Book, Boolean> availableCol = new TableColumn<>("Available");
        availableCol.setCellValueFactory(new PropertyValueFactory<>("available"));
        TableColumn<Book, Void> actionCol = createBookActionColumn();
        booksTable.getColumns().addAll(titleCol, authorCol, genreCol, isbnCol, availableCol, actionCol);
        VBox.setVgrow(booksTable, Priority.ALWAYS);
        content.getChildren().addAll(addBookBtn, booksTable);
        return content;
    }

    private VBox createTransactionsContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        GridPane issuePane = new GridPane();
        issuePane.setHgap(10);
        issuePane.setVgap(10);
        issuePane.setPadding(new Insets(10));
        issuePane.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 5;");
        TextField bookIsbnField = new TextField();
        TextField userEmailField = new TextField();
        Button issueBtn = new Button("Issue Book");
        issueBtn.setStyle("-fx-background-color: #2E86AB; -fx-text-fill: white;");
        issuePane.add(new Label("Book ISBN:"), 0, 0);
        issuePane.add(bookIsbnField, 1, 0);
        issuePane.add(new Label("User Email:"), 0, 1);
        issuePane.add(userEmailField, 1, 1);
        issuePane.add(issueBtn, 1, 2);
        issueBtn.setOnAction(e -> {
            try {
                transactionDao.issueBook(bookIsbnField.getText(), userEmailField.getText());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book issued successfully.");
                refreshAllAdminData();
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not issue book: " + ex.getMessage());
            }
        });
        TableView<Transaction> transactionsTable = new TableView<>(transactions);
        TableColumn<Transaction, String> bookCol = new TableColumn<>("Book");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        TableColumn<Transaction, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        TableColumn<Transaction, LocalDate> issueCol = new TableColumn<>("Issue Date");
        issueCol.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        TableColumn<Transaction, LocalDate> dueCol = new TableColumn<>("Due Date");
        dueCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        TableColumn<Transaction, Boolean> statusCol = new TableColumn<>("Returned");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("returned"));
        TableColumn<Transaction, Void> actionCol = createTransactionActionColumn();
        transactionsTable.getColumns().addAll(bookCol, userCol, issueCol, dueCol, statusCol, actionCol);
        VBox.setVgrow(transactionsTable, Priority.ALWAYS);
        content.getChildren().addAll(issuePane, new Label("All Transactions"), transactionsTable);
        return content;
    }

    private TableColumn<Book, Void> createBookActionColumn() {
        TableColumn<Book, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            {
                editBtn.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white;");
                editBtn.setOnAction(event -> showEditBookDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + book.getTitle() + "?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            try {
                                bookDao.deleteBook(book.getIsbn());
                                refreshAllAdminData();
                            } catch (SQLException e) {
                                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete book.");
                            }
                        }
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, editBtn, deleteBtn));
            }
        });
        return actionCol;
    }

    private TableColumn<User, Void> createUserActionColumn() {
        TableColumn<User, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            {
                editBtn.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white;");
                editBtn.setOnAction(event -> showEditUserDialog(getTableView().getItems().get(getIndex())));
                
                // **FIX**: Add safety check before deleting a user
                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user.getEmail().equals(loggedInUser.getEmail())) {
                        showAlert(Alert.AlertType.WARNING, "Action Denied", "You cannot delete your own account.");
                        return;
                    }

                    // The new safety check
                    if (userDao.hasActiveTransactions(user.getEmail())) {
                        showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Cannot delete user. This user has outstanding books that must be returned first.");
                        return;
                    }

                    // If the check passes, proceed with confirmation
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + user.getName() + "? This action is permanent.", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            try {
                                userDao.deleteUser(user.getEmail());
                                refreshAllAdminData();
                            } catch (SQLException e) {
                                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete user.");
                            }
                        }
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, editBtn, deleteBtn));
            }
        });
        return actionCol;
    }
    
    private TableColumn<Transaction, Void> createTransactionActionColumn() {
        TableColumn<Transaction, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button returnBtn = new Button("Return");
            {
                returnBtn.setStyle("-fx-background-color: #28A745; -fx-text-fill: white;");
                returnBtn.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    try {
                        transactionDao.returnBook(transaction.getId(), transaction.getBookIsbn());
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Book returned successfully.");
                        refreshAllAdminData();
                    } catch (SQLException e) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Could not return book: " + e.getMessage());
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()).isReturned()) {
                    setGraphic(null);
                } else {
                    setGraphic(returnBtn);
                }
            }
        });
        return actionCol;
    }

    private void showAddBookDialog() {
        Dialog<Book> dialog = createBookDialog(null);
        dialog.showAndWait().ifPresent(book -> {
            try {
                bookDao.addBook(book);
                refreshAllAdminData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not add book. ISBN might already exist.");
            }
        });
    }

    private void showEditBookDialog(Book bookToEdit) {
        Dialog<Book> dialog = createBookDialog(bookToEdit);
        dialog.showAndWait().ifPresent(book -> {
            try {
                bookDao.updateBook(book);
                refreshAllAdminData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update book.");
            }
        });
    }

    private Dialog<Book> createBookDialog(Book book) {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle(book == null ? "Add New Book" : "Edit Book");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField titleField = new TextField();
        TextField authorField = new TextField();
        TextField genreField = new TextField();
        TextField isbnField = new TextField();
        CheckBox availableCheck = new CheckBox("Available");
        if (book != null) {
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            genreField.setText(book.getGenre());
            isbnField.setText(book.getIsbn());
            isbnField.setEditable(false);
            availableCheck.setSelected(book.isAvailable());
        } else {
            availableCheck.setSelected(true);
        }
        grid.add(new Label("Title:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Author:"), 0, 1); grid.add(authorField, 1, 1);
        grid.add(new Label("Genre:"), 0, 2); grid.add(genreField, 1, 2);
        grid.add(new Label("ISBN:"), 0, 3); grid.add(isbnField, 1, 3);
        grid.add(availableCheck, 1, 4);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if(titleField.getText().isEmpty() || authorField.getText().isEmpty() || isbnField.getText().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Form Error", "Title, Author, and ISBN are required.");
                    return null;
                }
                return new Book(titleField.getText(), authorField.getText(), genreField.getText(), isbnField.getText(), availableCheck.isSelected());
            }
            return null;
        });
        return dialog;
    }

    private void showAddUserDialog() {
        Dialog<User> dialog = createUserDialog(null);
        dialog.showAndWait().ifPresent(user -> {
            try {
                userDao.addUser(user);
                refreshAllAdminData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not add user. Email might already exist.");
            }
        });
    }

    private void showEditUserDialog(User userToEdit) {
        Dialog<User> dialog = createUserDialog(userToEdit);
        dialog.showAndWait().ifPresent(user -> {
            try {
                userDao.updateUser(user);
                refreshAllAdminData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update user.");
            }
        });
    }

    private Dialog<User> createUserDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Add New User" : "Edit User");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField nameField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<String> roleComboBox = new ComboBox<>(FXCollections.observableArrayList("Student", "Administrator"));
        if (user != null) {
            nameField.setText(user.getName());
            emailField.setText(user.getEmail());
            emailField.setEditable(false);
            roleComboBox.setValue(user.getRole());
        } else {
            grid.add(new Label("Password:"), 0, 2);
            grid.add(passwordField, 1, 2);
            roleComboBox.setValue("Student");
        }
        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1); grid.add(emailField, 1, 1);
        grid.add(new Label("Role:"), 0, 3); grid.add(roleComboBox, 1, 3);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if(nameField.getText().isEmpty() || emailField.getText().isEmpty()){
                     showAlert(Alert.AlertType.ERROR, "Form Error", "Name and Email are required.");
                     return null;
                }
                String password = (user == null) ? passwordField.getText() : user.getPassword();
                 if(password.isEmpty() && user == null){
                     showAlert(Alert.AlertType.ERROR, "Form Error", "Password is required for new users.");
                     return null;
                 }
                return new User(nameField.getText(), emailField.getText(), password, roleComboBox.getValue(), LocalDate.now());
            }
            return null;
        });
        return dialog;
    }

    private HBox createHeader(String title) {
        HBox header = new HBox();
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #2E86AB;");
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button userButton = new Button(loggedInUser != null ? loggedInUser.getName() + " ▼" : "User ▼");
        userButton.setStyle("-fx-text-fill: white; -fx-background-color: transparent; -fx-border-color: transparent;");
        ContextMenu userMenu = new ContextMenu();
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> {
            loggedInUser = null;
            showLoginScreen();
        });
        userMenu.getItems().addAll(logoutItem);
        userButton.setOnAction(e -> userMenu.show(userButton, Side.BOTTOM, 0, 0));
        header.getChildren().addAll(titleLabel, spacer, userButton);
        return header;
    }

    private VBox createAdminDashboardContent() {
        VBox content = new VBox(20, new Label("Welcome to the Admin Dashboard. Use the tabs to manage the library."));
        content.setPadding(new Insets(10));
        return content;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}


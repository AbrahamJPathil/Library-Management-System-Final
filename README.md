Library Management system, UI by Java FX

How to use?
1. Clone into the repo
2. Change the credentials in DatabaseHandler.java (Located inside src/main/java/com/library/database with correct MySQL credentials as per your configuration), setup assumes root user with
   password "Project2025"

3. Run the 'database.sql' script in your MySQL client to set up the tables.
   (This script will also create default users: admin@library.com [pass: admin] and student@library.com [pass: student])

4. Open a terminal in this directory and run the final command:    "mvn clean javafx:run"


NB (Update as of Oct 3 10:51 PM IST) --> added another sql script that creates entries with dummy entries, feel free to use that will testing my application

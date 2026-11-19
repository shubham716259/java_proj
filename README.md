# Personal Knowledge Base and Smart Notes

This is a personal notes web application I built for my 2nd-year CSE project. It runs completely on standard Java with MySQL, without using any external frameworks like Spring or Tomcat. I used the built-in HttpServer in com.sun.net.httpserver, so the whole backend runs directly from the command line with just the MySQL connector jar on the classpath.

## What it does

The application lets users register, log in, and manage their own private notebook of study notes, folders, and tags.

- User Authentication: Secure signup and login with SHA-256 hashed passwords and cookie-based session tokens.
- User Isolation: Each user has their own private folders and notes. Unauthenticated visitors are automatically redirected to the login page.
- Notes Management: Notes have a title, content, an optional folder, and multiple comma-separated tags.
- Pinning: Pinned notes stick to the top of the note list and are highlighted on the dashboard.
- Dashboard: Shows pinned notes and the 10 most recently updated notes for the logged-in user.
- Keyword Search: Checks both title and content using Java string methods (toLowerCase, indexOf). Matching words get highlighted on the screen with yellow mark tags. Title matches are weighted heavier than body matches.
- Smart Suggestions: Strips common stopwords and uses a HashMap to count the most frequent words. It then suggests other notes in the user's notebook sharing those keywords, and recommends related tags using a HashSet.
- Folders: Folders can be created, renamed, or deleted. If a folder is deleted, its notes are kept safe and their folder id is simply set to unfiled.
- Safe Actions: All delete operations go through an explicit confirmation page.

## Setting up MySQL

1. Make sure MySQL 8 is running.
2. Open a terminal and run the schema file to create the database, tables, and seed data:

```bash
mysql -u root -p < schema.sql
```

This creates the `notes_db` database with 5 tables (`users`, `folders`, `notes`, `tags`, `note_tags`).

3. Check the `db.properties` file in the project root to match your local MySQL settings:

```properties
db.url=jdbc:mysql://127.0.0.1:3307/notes_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
db.user=root
db.password=
server.port=8080
```

*(Note: If your MySQL server runs on default port 3306 or has a password, update `db.url` and `db.password` accordingly).*

## How to compile and run

I put `mysql-connector-j.jar` in the `lib/` folder so no build tool is needed.

### Compiling from terminal

From the project root directory:

```bash
javac -encoding UTF-8 -cp "lib/mysql-connector-j.jar;src" -d bin src/com/notes/model/*.java src/com/notes/dao/*.java src/com/notes/helper/*.java src/com/notes/web/*.java src/com/notes/Main.java
```

Or just double-click `build.bat` on Windows.

### Running the server

```bash
java -cp "bin;lib/mysql-connector-j.jar" com.notes.Main
```

Or double-click `run.bat` on Windows.

## URL to open

Once the console prints `Server running at http://localhost:8080/`, open your web browser and go to:

http://localhost:8080/

If you are not logged in, you will be directed to the login page (`/login`). You can log in with your account or click "Create account" to register a new user.

## Known limitations

1. Since I did not use a connection pool library, each query opens a new connection via DriverManager. This is fine for single-user local usage, but under hundreds of concurrent users it would slow down.
2. The keyword search is purely in-memory string matching on the fetched notes rather than full-text indexing in MySQL. It works fast for hundreds of notes, but a production system with millions of notes would need MySQL FULLTEXT indexes or Elasticsearch.
3. The server uses com.sun.net.httpserver.HttpServer which does not support HTTPS out of the box without manual SSLContext configuration, so it is meant for local network development.
4. Sessions are kept in memory using a ConcurrentHashMap; restarting the Java process logs out active sessions.

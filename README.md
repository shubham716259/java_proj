# Personal Knowledge Base and Smart Notes

This is a personal notes web application I built for my 2nd-year CSE project. It runs completely on standard Java with MySQL, without using any external frameworks like Spring or Tomcat. I used the built-in HttpServer in com.sun.net.httpserver, so the whole backend runs directly from the command line with just the MySQL connector jar on the classpath.

## What it does

The application lets me create, edit, view, and organize study notes into folders and tags.

- Notes have a title, content, an optional folder, and multiple comma-separated tags.
- Pinned notes always stick to the top of the list and on the dashboard.
- The dashboard shows my pinned notes and the 10 most recently updated notes.
- When searching, it checks both title and content using Java string methods (toLowerCase, indexOf). Matching words get highlighted on the screen with yellow mark tags. Title matches are weighted heavier than body matches.
- When viewing a note, it strips common stopwords and uses a HashMap to count the most frequent words. It then suggests other notes in the database that share those keywords, and recommends related tags using a HashSet.
- Folders can be created, renamed, or deleted. If a folder is deleted, its notes are kept safe and their folder id is simply set to null.
- All delete operations go through an explicit confirmation page.

## Setting up MySQL

1. Make sure MySQL 8 is running.
2. Open a terminal and run the schema file to create the database and seed records:

```bash
mysql -u root -p < schema.sql
```

This creates the `notes_db` database, sets up the four tables (`folders`, `notes`, `tags`, `note_tags`), and inserts 8 sample study notes on Java OOP, collections, SQL joins, and algorithms.

3. Check the `db.properties` file in the project root to match your local MySQL settings:

```properties
db.url=jdbc:mysql://127.0.0.1:3306/notes_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
db.user=root
db.password=your_password
server.port=8080
```

*(Note: In my development environment, MySQL was running on port 3307 with no password, so update this file if you use standard port 3306 or have a root password set.)*

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

## Known limitations

1. Since I did not use a connection pool library, each query opens a new connection via DriverManager. This is fine for single-user local usage, but under hundreds of concurrent users it would slow down.
2. The keyword search is purely in-memory string matching on the fetched notes rather than full-text indexing in MySQL. It works fast for hundreds of notes, but a production system with millions of notes would need MySQL FULLTEXT indexes or Elasticsearch.
3. The server uses com.sun.net.httpserver.HttpServer which does not support HTTPS out of the box without manual SSLContext configuration, so it is meant for local network development.
4. If two tags only differ by punctuation, they might be treated as separate tags unless stripped carefully.

# Personal Knowledge Base and Smart Notes

A full-stack, student-friendly personal knowledge management web application featuring a **React 18 + Vite** frontend and a pure **Core Java (JDK 17) + JDBC** backend exposing a REST JSON API using Java's built-in `com.sun.net.httpserver.HttpServer`.

---

## 1. Project Architecture & Strict Tech Stack

```
smart-notes/
├── backend/
│   ├── src/
│   │   ├── model/                  # Entities demonstrating OOP composition
│   │   │   ├── Folder.java
│   │   │   ├── Tag.java
│   │   │   └── Note.java           # Note HAS-A Folder, Note HAS-MANY Tags
│   │   ├── dao/                    # JDBC data access with PreparedStatement
│   │   │   ├── FolderDAO.java
│   │   │   ├── TagDAO.java
│   │   │   └── NoteDAO.java
│   │   ├── service/                # Business logic & algorithms
│   │   │   ├── SearchService.java  # Scoring (title=3, content=1), sorting (Comparator)
│   │   │   └── SuggestionService.java # Keyword extraction (HashMap), recommendations
│   │   ├── handler/                # REST API handlers with CORS support
│   │   │   ├── BaseHandler.java
│   │   │   ├── NoteHandler.java
│   │   │   ├── FolderHandler.java
│   │   │   └── TagHandler.java
│   │   ├── util/                   # Connection pool & serialization helpers
│   │   │   ├── DBConnection.java   # Reads config.properties
│   │   │   └── JsonUtil.java       # Google Gson serializer/deserializer
│   │   └── Main.java               # HttpServer bootstrap & route registration
│   ├── lib/                        # Plain JAR dependencies (no build tools)
│   │   ├── mysql-connector-j.jar   # MySQL 8 JDBC Driver
│   │   └── gson.jar                # Google Gson JSON parser
│   ├── config.properties           # Database & server port configuration
│   ├── schema.sql                  # Database schema & 8 seed study notes
│   ├── build.bat                   # 1-click Windows compilation script
│   └── run.bat                     # 1-click Windows execution script
│
├── frontend/                       # React 18 + Vite SPA
│   ├── src/
│   │   ├── components/             # Reusable UI components
│   │   │   ├── Sidebar.jsx         # Navigation, folder links, tag filters
│   │   │   ├── SearchBar.jsx       # 300ms debounced search input
│   │   │   ├── NoteCard.jsx        # Note card item with quick actions
│   │   │   ├── NoteList.jsx        # List wrapper with loading/empty states
│   │   │   ├── ConfirmDialog.jsx   # Modal confirmation for deletions
│   │   │   └── Toast.jsx           # Flash alert notifications
│   │   ├── pages/                  # Route views
│   │   │   ├── DashboardPage.jsx   # Route / (pinned + 10 recent + search)
│   │   │   ├── NotesPage.jsx       # Route /notes (search, filters, sort)
│   │   │   ├── NoteDetailPage.jsx  # Route /notes/:id (full note + suggestions)
│   │   │   ├── NoteFormPage.jsx    # Route /notes/new & /notes/:id/edit
│   │   │   └── FoldersPage.jsx     # Route /folders (create, rename, delete)
│   │   ├── api.js                  # Centralized fetch() API client
│   │   ├── App.jsx                 # Router & layout setup
│   │   ├── App.css                 # Clean paper-notebook CSS (no frameworks)
│   │   └── main.jsx                # React 18 entrypoint
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── run.bat                     # 1-click frontend launch script
│
└── README.md
```

---

## 2. Setting Up MySQL

1. Ensure MySQL 8 is running locally.
2. Run `schema.sql` to create `notes_db` and insert sample notes on Java OOP, collections, SQL joins, and binary search:
   ```bash
   mysql -u root -p < backend/schema.sql
   ```
3. Open `backend/config.properties` and verify your credentials:
   ```properties
   db.url=jdbc:mysql://127.0.0.1:3306/notes_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
   db.user=root
   db.password=your_password
   server.port=8080
   ```
   *(Note: In development, if MySQL runs on port 3307 or without password, set `127.0.0.1:3307` and leave `db.password=` empty).*

---

## 3. How to Run

### Backend (Core Java REST API)

#### Running in Terminal / Command Prompt:
1. Navigate to `backend/`:
   ```bash
   cd backend
   ```
2. Compile all source files into `bin/`:
   ```bash
   javac -encoding UTF-8 -cp "lib/mysql-connector-j.jar;lib/gson.jar;src" -d bin src/model/*.java src/util/*.java src/dao/*.java src/service/*.java src/handler/*.java src/Main.java
   ```
3. Run the application:
   ```bash
   java -cp "bin;lib/mysql-connector-j.jar;lib/gson.jar" Main
   ```
   *(Or double-click `backend/run.bat` on Windows).*

#### Running in IntelliJ IDEA:
1. Open the `smart-notes/backend` folder in IntelliJ.
2. Go to **File** &rarr; **Project Structure** &rarr; **Libraries** &rarr; click **+** &rarr; **Java** &rarr; select the `lib/` directory (`mysql-connector-j.jar` and `gson.jar`).
3. Right-click `src/Main.java` and click **Run 'Main.main()'**.

#### Running in VS Code:
1. Open the `smart-notes/backend` folder.
2. In the Java Projects explorer sidebar, expand **Referenced Libraries** &rarr; click **+** &rarr; select the JARs in `lib/`.
3. Open `src/Main.java` and click **Run**.

---

### Frontend (React 18 + Vite)

1. Open a new terminal in the `frontend/` folder:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   npm run dev
   ```
4. Open your browser and navigate to:
   ```
   http://localhost:5173/
   ```

---

## 4. REST API Specification

All endpoints return JSON and include CORS headers allowing `http://localhost:5173` to interact seamlessly.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/notes?q=&folderId=&tag=&sort=&order=` | Search, filter by folder/tag, sort by date/title |
| `GET` | `/api/notes/recent` | Retrieves the 10 most recently updated notes |
| `GET` | `/api/notes/{id}` | Single note detail, extracted keywords, and related notes |
| `POST` | `/api/notes` | Create a new note (title, content, folderId, tags) |
| `PUT` | `/api/notes/{id}` | Update existing note |
| `DELETE` | `/api/notes/{id}` | Delete a note |
| `PATCH` | `/api/notes/{id}/pin` | Toggle pinned state of a note |
| `GET` | `/api/folders` | List all folders with note count |
| `POST` | `/api/folders` | Create a folder |
| `PUT` | `/api/folders/{id}` | Rename a folder |
| `DELETE` | `/api/folders/{id}` | Delete a folder (notes are unfiled via `ON DELETE SET NULL`) |
| `GET` | `/api/tags` | List all tags with usage frequency |

---

## 5. Feature Implementation Mapping

| Feature | Backend Implementation | Frontend Implementation |
|---|---|---|
| **Note CRUD** | `NoteDAO.java`, `NoteHandler.java` | `NoteFormPage.jsx`, `NoteDetailPage.jsx`, `api.js` |
| **Folder Management** | `FolderDAO.java`, `FolderHandler.java` | `FoldersPage.jsx`, `Sidebar.jsx`, `api.js` |
| **Many-to-Many Tags** | `TagDAO.java` (`note_tags` table), `TagHandler.java` | `Sidebar.jsx`, `NoteCard.jsx`, `NoteFormPage.jsx` |
| **Search & Relevance Scoring** | `SearchService.java` (`toLowerCase()`, `indexOf()`, title=3pts, content=1pt) | `SearchBar.jsx` (300ms debounce), `NotesPage.jsx` |
| **Related Searches** | `SearchService.getRelatedSearches()` | `NotesPage.jsx` (clickable chips under search) |
| **Smart Suggestions** | `SuggestionService.java` (`HashMap` frequency, `HashSet` stopwords) | `NoteDetailPage.jsx` (keywords & related notes) |
| **Sorting** | `SearchService.java` (Java `Comparator` by date or title) | `NotesPage.jsx` (sort & order dropdown) |
| **Pin / Unpin Notes** | `NoteDAO.togglePin()`, `NoteHandler.java` | `NoteCard.jsx`, `DashboardPage.jsx` |
| **Recent Notes** | `NoteDAO.getRecentNotes(10)` | `DashboardPage.jsx` |
| **Delete Confirmation** | `NoteHandler.java`, `FolderHandler.java` | `ConfirmDialog.jsx` (modal dialog) |

---

## 6. Viva & Demo Questions and Answers

### Q1: Explain the JDBC flow in this application.
**Answer:**
JDBC (Java Database Connectivity) allows Java applications to execute SQL statements. The standard flow in our application is:
1. **Driver Loading**: In `DBConnection.java`, `Class.forName("com.mysql.cj.jdbc.Driver")` registers the MySQL JDBC driver.
2. **Connection Acquisition**: `DriverManager.getConnection(url, user, password)` requests a socket connection to MySQL.
3. **Statement Preparation**: `conn.prepareStatement(sql)` compiles the SQL structure on the database engine.
4. **Parameter Binding**: `ps.setString(...)` or `ps.setInt(...)` injects variables safely as literal values.
5. **Execution**: `ps.executeQuery()` (for SELECT) returns a `ResultSet`, while `ps.executeUpdate()` (for INSERT/UPDATE/DELETE) returns the affected row count.
6. **Closing Resources**: Every resource is wrapped in a `try-with-resources` block, guaranteeing that `Connection`, `PreparedStatement`, and `ResultSet` close automatically even if an exception occurs.

### Q2: Why did you use `PreparedStatement` instead of `Statement`?
**Answer:**
`PreparedStatement` was chosen for two critical reasons:
1. **SQL Injection Prevention**: `Statement` concatenates raw user strings directly into SQL queries, allowing attackers to inject malicious clauses (e.g. `' OR '1'='1`). `PreparedStatement` pre-compiles the query and treats user parameters strictly as data values, neutralizing injection.
2. **Performance Optimization**: The database parses, compiles, and optimizes the execution plan for the query template once. If the query is executed multiple times with different parameters, the database reuses the cached execution plan.

### Q3: How is Object-Oriented Composition demonstrated in your domain model?
**Answer:**
Composition represents a **HAS-A** relationship between classes rather than an **IS-A** inheritance relationship:
- In `Note.java`:
  - `Note HAS-A Folder`: represented by a private field `Folder folder;`.
  - `Note HAS-MANY Tags`: represented by a private field `Set<Tag> tags = new HashSet<>();`.
This models the real-world domain where a note contains folder metadata and a collection of tags without tight inheritance coupling.

### Q4: How is `HashMap` used for keyword frequency and suggestions?
**Answer:**
In `SuggestionService.java`, the text of a note is stripped of punctuation and common English stopwords (stored in a `HashSet<String>` for $O(1)$ lookups).
A `HashMap<String, Integer>` is used where:
- The **key** is the distinct word.
- The **value** is the occurrence count: `freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);`.
The map entries are sorted by frequency descending, and the top 5 words become the note's keywords. These keywords are then matched against other notes to recommend related study material.

### Q5: How is the many-to-many relationship between Notes and Tags implemented?
**Answer:**
A note can have multiple tags, and a tag can be attached to multiple notes.
In relational databases, this is normalized using a junction table `note_tags`:
- `note_tags(note_id, tag_id)` with composite primary key `(note_id, tag_id)`.
- Foreign key `note_id REFERENCES notes(id) ON DELETE CASCADE`.
- Foreign key `tag_id REFERENCES tags(id) ON DELETE CASCADE`.
In `TagDAO.java`, `syncNoteTags()` runs in a single transaction (`conn.setAutoCommit(false)`): it clears old links and batch-inserts the new tag associations.

### Q6: What is CORS and how did you resolve it in Java `HttpServer`?
**Answer:**
**CORS (Cross-Origin Resource Sharing)** is a browser security mechanism that restricts a web page on one origin (e.g. `http://localhost:5173`) from making HTTP requests to a different origin (e.g. `http://localhost:8080`).
Because `com.sun.net.httpserver.HttpServer` does not handle CORS automatically:
1. In `BaseHandler.java`, we attach headers to every response:
   - `Access-Control-Allow-Origin: *`
   - `Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS`
   - `Access-Control-Allow-Headers: Content-Type, Authorization`
2. When the browser sends an HTTP `OPTIONS` preflight request, the handler detects it and responds immediately with `204 No Content`, allowing the subsequent `POST`, `PUT`, or `DELETE` call to succeed.

### Q7: What are the architectural differences between a REST API backend and a Server-Rendered HTML backend?
**Answer:**
- **Server-Rendered HTML**: The Java server constructs HTML strings or templates on the server and returns the full HTML page with every request. Navigation causes full-page reloads.
- **REST API + SPA (Single Page Application)**: The Java server exposes stateless JSON endpoints handling data only. The React client loads once in the browser and dynamically fetches JSON data in the background via `fetch()`. The UI updates seamlessly without page reloads, providing a responsive desktop-like user experience and clean separation of concerns.

### Q8: Why choose React for the frontend layer of this project?
**Answer:**
1. **Component-Based Architecture**: UI elements like `SearchBar`, `NoteCard`, `NoteList`, and `ConfirmDialog` are encapsulated and reusable.
2. **Declarative State with Hooks**: Using `useState` and `useEffect`, the UI automatically updates whenever notes or filter states change.
3. **Client-Side Routing**: `react-router-dom` enables instantaneous page transitions between Dashboard, Notes list, Edit view, and Folders without triggering server round-trips for HTML.
4. **Built-in XSS Protection**: React automatically escapes strings rendered inside JSX, eliminating Cross-Site Scripting (XSS) vulnerabilities.

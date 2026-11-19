-- Personal Knowledge Base and Smart Notes Database Schema
CREATE DATABASE IF NOT EXISTS notes_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE notes_db;

DROP TABLE IF EXISTS note_tags;
DROP TABLE IF EXISTS notes;
DROP TABLE IF EXISTS folders;
DROP TABLE IF EXISTS tags;

CREATE TABLE folders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    folder_id INT,
    is_pinned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_note_folder FOREIGN KEY (folder_id)
        REFERENCES folders(id) ON DELETE SET NULL
);

CREATE TABLE tags (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE note_tags (
    note_id INT NOT NULL,
    tag_id INT NOT NULL,
    PRIMARY KEY (note_id, tag_id),
    CONSTRAINT fk_nt_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    CONSTRAINT fk_nt_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Seed data: 3 folders
INSERT INTO folders (id, name) VALUES
(1, 'Java Programming'),
(2, 'Database Systems'),
(3, 'Data Structures');

-- Seed tags
INSERT INTO tags (id, name) VALUES
(1, 'oop'),
(2, 'collections'),
(3, 'jdbc'),
(4, 'sql'),
(5, 'algorithms'),
(6, 'basics');

-- 8 Seed notes with real study content
INSERT INTO notes (id, title, content, folder_id, is_pinned, created_at, updated_at) VALUES
(1, 'Core OOP Concepts in Java',
'Object-Oriented Programming centers on four ideas: encapsulation, abstraction, inheritance, and polymorphism. Encapsulation wraps fields into a class and guards them with private access and public methods. Abstraction hides the internal work so the caller only cares about the method contract. Inheritance lets a subclass reuse state and behavior from a superclass. Polymorphism allows a child class to override a parent method and be called dynamically at runtime.',
1, TRUE, '2026-03-01 10:00:00', '2026-03-01 10:00:00'),

(2, 'HashMap vs HashSet Mechanics',
'A HashMap stores key-value pairs while a HashSet only stores unique keys. Internally a HashSet is just backed by a HashMap where the value is a dummy Object. Both use hashCode() to find the array bucket and equals() to resolve collisions inside that bucket. In Java 8 and newer, buckets with more than 8 elements turn from linked lists into red-black trees, keeping lookup times at O(log n) even under bad hash collisions.',
3, TRUE, '2026-03-02 11:30:00', '2026-03-02 11:30:00'),

(3, 'PreparedStatement vs Statement in JDBC',
'Always use PreparedStatement instead of Statement for user inputs. PreparedStatement compiles the SQL query structure first and treats parameters as raw data literals, which completely stops SQL injection. Statement glues raw strings directly into the query, which an attacker can break out of using quotes and OR clauses. PreparedStatement also lets the database engine cache the query execution plan.',
2, FALSE, '2026-03-03 09:15:00', '2026-03-03 09:15:00'),

(4, 'Understanding SQL Joins',
'An INNER JOIN returns only rows that have matching values in both tables. A LEFT JOIN returns all rows from the left table along with matching rows from the right table, filling nulls if there is no match. A RIGHT JOIN does the opposite. In our notes schema, joining notes and folders with a LEFT JOIN ensures notes without a folder still show up in the main list.',
2, FALSE, '2026-03-04 14:00:00', '2026-03-04 14:00:00'),

(5, 'Array vs ArrayList in Java',
'Standard arrays have a fixed size set at creation time and can hold primitives or objects directly. ArrayList is a resizable list backed by an Object array. When an ArrayList runs out of capacity, it allocates a new array that is fifty percent larger and copies the old elements over. Use plain arrays when memory is tight and size is fixed, and ArrayList when the number of items changes often.',
1, FALSE, '2026-03-05 16:45:00', '2026-03-05 16:45:00'),

(6, 'Binary Search Implementation',
'Binary search finds a target value in a sorted array by dividing the search range in half each step. Compare the target to the middle element. If equal, return the index. If smaller, narrow to the left half. If larger, narrow to the right half. The time complexity is O(log n) because the problem size is halved every step, whereas linear search takes O(n).',
3, FALSE, '2026-03-06 13:20:00', '2026-03-06 13:20:00'),

(7, 'JDBC Connection and Transaction Basics',
'A basic JDBC flow requires loading the MySQL driver, creating a Connection, executing a query, and closing resources. Setting auto-commit to false allows grouping multiple SQL statements into a single transaction. If any statement fails, call rollback() to undo the changes. If all statements succeed, call commit() to persist everything together.',
2, FALSE, '2026-03-07 15:10:00', '2026-03-07 15:10:00'),

(8, 'Method Overloading and Overriding',
'Method overloading happens within the same class when methods share the same name but take different argument types or counts. It is resolved at compile time. Method overriding happens when a subclass provides its own version of a method that is already defined in its parent class. It is resolved at runtime using dynamic method dispatch.',
1, FALSE, '2026-03-08 17:05:00', '2026-03-08 17:05:00');

-- Map note_tags
INSERT INTO note_tags (note_id, tag_id) VALUES
(1, 1), (1, 6),
(2, 2), (2, 5),
(3, 3), (3, 4),
(4, 4),
(5, 2), (5, 6),
(6, 5),
(7, 3), (7, 4),
(8, 1), (8, 6);

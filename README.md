# AI-Powered Delivery Assistant

### 1. Why did we use Python instead of calling the AI API directly from Java?

 Python makes calling APIs very easy we can do it in just a few lines but in Java, it takes more code and feels more complicated
 Also Python is commonly used in AI projects.


### 2. What is the DAO pattern? What would happen without it?

DAO stands for Data Access Object. It is a class whose only job is to talk
to the database.All SQL for the "locations" table lives in LocationDAO.java
only.The same applies to "AIQueryLogDAO.java" for the cache table.

Without this pattern, if we wrote SQL directly in Main.java, the code
would become a mix of user interface logic and database logic in one place
If we ever needed to switch from MySQL to PostgreSQL, we would have to hunt
through hundreds of lines of unrelated code to find every query
With DAO, we change only one file and nothing else breaks

### 3. What is single-table inheritance in SQL? What are its trade-offs?

Single-table inheritance means we store all three Java subclasses
(GeneralLocation, DeliveryPoint, Warehouse) in one locations table
A "type" column ('LOC', 'DP', 'WH') tells the Java DAO which subclass to create
Columns that don't apply to a type are stored as NULL 

Advantages: only simple one table, easy to query everything at once

Disadvantages: many NULL values in rows, and we cannot enforce NOT NULL
on type-specific columns (like priority for delivery points) because those columns must accept NULL for other types

### 4. Why does the cache use NOW() - INTERVAL 24 HOUR instead of a boolean flag?

A boolean flag doesn’t expire by itself, so we would need extra logic to update it, and we wouldn’t know when the response was created
Using a 24-hour interval is easier because the database handles expiration automatically using the timestamp so the old responses stay saved for history, but they are simply ignored once they become outdated


### 5. Why must api_key.txt and db.properties never be pushed to GitHub?

If "api_key.txt" is uploaded to GitHub, bots can quickly find it and use it, so this will get your account blocked
If "db.properties" is exposed, anyone could access your database,change data or even delete it


### 6. How would you redesign the architecture for 100 AI requests per second?
The current system can’t handle high traffic well because each request starts a new Python process, and there is only one database connection
To improve it, we can:
* use a running Python service instead of starting a new process every time
* use multiple database connections (connection pool)
* run multiple server instances to distribute the load

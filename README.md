# Quiz Leaderboard System

This is a Java-based solution for the Quiz Leaderboard System assignment. The application integrates with the validator API to process quiz responses, aggregate scores, handle duplicate events, and submit the final leaderboard.

## Problem Overview
The objective is to consume API responses from an external system representing a quiz show, process the events correctly (deduplicating them using `roundId` + `participant`), calculate the total score for each participant, and submit a sorted leaderboard back to the validator API.

## Expected Flow Implemented
1. **Poll the Validator API**: The application makes 10 GET requests to the `/quiz/messages` endpoint (poll index 0 to 9), with a mandatory 5-second delay between each request.
2. **Collect Responses**: It parses the JSON responses and extracts the `events` array.
3. **Deduplicate Data**: It uses a `HashSet` to store a unique key (`roundId` + `-` + `participant`) for each event. If an event is received again, it is ignored.
4. **Aggregate Scores**: Valid scores are aggregated per participant in a `HashMap`.
5. **Generate Leaderboard**: The participants are sorted by their total scores in descending order.
6. **Submit Leaderboard**: A final POST request is made to `/quiz/submit` with the aggregated leaderboard and registration number.

## Requirements Checklist
- [x] 10 polls executed
- [x] 5-second delay between polls maintained
- [x] Duplicate API response data handled correctly
- [x] Leaderboard generated and sorted correctly
- [x] Submit exactly once

## Setup and Execution

### Prerequisites
- **Java 11 or higher** (The code uses the `java.net.http.HttpClient` introduced in Java 11).
- **Apache Maven** installed.

### How to Run
1. Open your terminal and navigate to the project root directory (where the `pom.xml` is located).
2. Clean and compile the project:
   ```bash
   mvn clean compile
   ```
3. Run the application:
   ```bash
   mvn exec:java
   ```

### Output
The console will display the progress of each of the 10 polls, noting any new events and ignoring any duplicates. After the 10th poll, it will display the generated leaderboard and the final response from the submission API.

Here is a sample output of the application running:
```bash
PS C:\Users\Ayushiraj\Downloads\Bajaj\QuizLeaderboardSystem> mvn exec:java
[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------< com.srm:quiz-leaderboard-system >-------------------
[INFO] Building quiz-leaderboard-system 1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- exec:3.1.0:java (default-cli) @ quiz-leaderboard-system ---    
Starting Quiz Leaderboard processing for RegNo: RA2311003010829
Polling API (Index: 0)...
  [New Event] Diana scored 200 in R1
  [New Event] Ethan scored 155 in R1
  Waiting 5 seconds before next poll...
Polling API (Index: 1)...
  [New Event] Fiona scored 180 in R1
  Waiting 5 seconds before next poll...
Polling API (Index: 2)...
  [New Event] Diana scored 95 in R2
  [New Event] Ethan scored 210 in R2
  Waiting 5 seconds before next poll...
Polling API (Index: 3)...
  [Duplicate Event Ignored] Diana in R2
  Waiting 5 seconds before next poll...
Polling API (Index: 4)...
  [New Event] Fiona scored 140 in R3
  [New Event] Diana scored 175 in R3
  Waiting 5 seconds before next poll...
Polling API (Index: 5)...
  [New Event] Fiona scored 120 in R2
  Waiting 5 seconds before next poll...
Polling API (Index: 6)...
  [New Event] Ethan scored 90 in R3
  [Duplicate Event Ignored] Diana in R1
  Waiting 5 seconds before next poll...
Polling API (Index: 7)...
  [Duplicate Event Ignored] Fiona in R3
  Waiting 5 seconds before next poll...
Polling API (Index: 8)...
  [Duplicate Event Ignored] Ethan in R2
  [Duplicate Event Ignored] Diana in R3
  Waiting 5 seconds before next poll...
Polling API (Index: 9)...
  [Duplicate Event Ignored] Fiona in R1

Generating Leaderboard...
  Diana: 470
  Ethan: 455
  Fiona: 440

Submitting Leaderboard...
Submission Response Code: 200
Submission Response Body: {"regNo":"RA2311003010829","totalPollsMade":31,"submittedTotal":1365,"attemptCount":2}
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  47.138 s
[INFO] Finished at: 2026-04-24T18:20:57+05:30
[INFO] ------------------------------------------------------------------------
```

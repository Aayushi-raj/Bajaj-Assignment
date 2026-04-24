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
The console will display the progress of each of the 10 polls, noting any new events and ignoring any duplicates. After the 10th poll, it will display the generated leaderboard and the final response from the submission API, which should be:
```json
{
  "isCorrect": true,
  "isIdempotent": true,
  "submittedTotal": <value>,
  "expectedTotal": <value>,
  "message": "Correct!"
}
```

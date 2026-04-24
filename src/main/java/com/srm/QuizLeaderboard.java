package com.srm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuizLeaderboard {
    private static final String REG_NO = "RA2311003010829";
    private static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    private static final int MAX_POLLS = 10;
    private static final int DELAY_MS = 5000;

    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();
        
        // Track seen events to deduplicate (roundId + participant)
        Set<String> seenEvents = new HashSet<>();
        // Aggregate scores per participant
        Map<String, Integer> participantScores = new HashMap<>();

        System.out.println("Starting Quiz Leaderboard processing for RegNo: " + REG_NO);

        // 1. Poll the API 10 times
        for (int pollIndex = 0; pollIndex < MAX_POLLS; pollIndex++) {
            System.out.println("Polling API (Index: " + pollIndex + ")...");
            
            try {
                String url = BASE_URL + "/quiz/messages?regNo=" + REG_NO + "&poll=" + pollIndex;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    JSONObject jsonResponse = new JSONObject(response.body());
                    if (jsonResponse.has("events")) {
                        JSONArray events = jsonResponse.getJSONArray("events");
                        
                        // 2 & 3. Process events and deduplicate
                        for (int i = 0; i < events.length(); i++) {
                            JSONObject event = events.getJSONObject(i);
                            String roundId = event.getString("roundId");
                            String participant = event.getString("participant");
                            int score = event.getInt("score");
                            
                            String uniqueKey = roundId + "-" + participant;
                            
                            if (!seenEvents.contains(uniqueKey)) {
                                seenEvents.add(uniqueKey);
                                // 4. Aggregate scores
                                participantScores.put(participant, participantScores.getOrDefault(participant, 0) + score);
                                System.out.println("  [New Event] " + participant + " scored " + score + " in " + roundId);
                            } else {
                                System.out.println("  [Duplicate Event Ignored] " + participant + " in " + roundId);
                            }
                        }
                    } else {
                        System.out.println("  No events found in this poll.");
                    }
                } else {
                    System.err.println("  Failed to fetch poll " + pollIndex + ". Status code: " + response.statusCode());
                }

                // Mandatory delay of 5 seconds between polls (except after the last one)
                if (pollIndex < MAX_POLLS - 1) {
                    System.out.println("  Waiting " + (DELAY_MS / 1000) + " seconds before next poll...");
                    Thread.sleep(DELAY_MS);
                }

            } catch (Exception e) {
                System.err.println("Error during polling " + pollIndex + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 5. Generate leaderboard sorted by totalScore descending
        System.out.println("\nGenerating Leaderboard...");
        List<Map.Entry<String, Integer>> leaderboardList = new ArrayList<>(participantScores.entrySet());
        leaderboardList.sort((a, b) -> b.getValue().compareTo(a.getValue())); // Descending order

        JSONArray leaderboardArray = new JSONArray();
        for (Map.Entry<String, Integer> entry : leaderboardList) {
            JSONObject participantObj = new JSONObject();
            participantObj.put("participant", entry.getKey());
            participantObj.put("totalScore", entry.getValue());
            leaderboardArray.put(participantObj);
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        // 6. Submit leaderboard
        System.out.println("\nSubmitting Leaderboard...");
        JSONObject submitPayload = new JSONObject();
        submitPayload.put("regNo", REG_NO);
        submitPayload.put("leaderboard", leaderboardArray);

        try {
            HttpRequest submitRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/quiz/submit"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(submitPayload.toString()))
                    .build();

            HttpResponse<String> submitResponse = client.send(submitRequest, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Submission Response Code: " + submitResponse.statusCode());
            System.out.println("Submission Response Body: " + submitResponse.body());
            
        } catch (Exception e) {
            System.err.println("Error during submission: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

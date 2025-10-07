package com.example.resumetailor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class ResumeService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${demo.mode:false}")
    private boolean demoMode;

    // Maximum safe resume text length for Groq API (adjust if needed)
    private static final int MAX_RESUME_LENGTH = 4000;

    public String generateTailoredSummary(MultipartFile resumeFile, String jobDescription) {
        try {
            String resumeText = new String(resumeFile.getBytes());

            // Truncate if too long
            if (resumeText.length() > MAX_RESUME_LENGTH) {
                resumeText = resumeText.substring(0, MAX_RESUME_LENGTH);
            }

            String prompt = "Based on the following resume and job description, generate a concise, tailored professional summary:\n\n"
                    + "Resume:\n" + resumeText + "\n\nJob Description:\n" + jobDescription;

            if (demoMode) {
                return "Demo summary for job: " + jobDescription;
            }

            JSONObject message1 = new JSONObject()
                    .put("role", "system")
                    .put("content", "You are a resume tailoring assistant.");
            JSONObject message2 = new JSONObject()
                    .put("role", "user")
                    .put("content", prompt);

            JSONObject requestBody = new JSONObject()
                    .put("model", "groq/compound-mini")
                    .put("messages", new JSONArray().put(message1).put(message2));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Authorization", "Bearer " + groqApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                return jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            } else {
                return "Error from Groq API: " + response.statusCode() + " - " + response.body();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating tailored summary: " + e.getMessage();
        }
    }
}


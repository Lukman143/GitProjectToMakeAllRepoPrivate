package com.sk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class GitProjectApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(GitProjectApplication.class, args);
    }

    @Override
    public void run(String... args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.print("GitHub username: ");
            String username = reader.readLine();

            System.out.print("GitHub personal access token: ");
            String token = reader.readLine();

            String url = "https://api.github.com/users/Lukman143/repos";

            String auth = username + ":" + token;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpClient httpClient = HttpClients.createDefault();
            HttpUriRequest request = new HttpGet(url);
            request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String responseBody = EntityUtils.toString(response.getEntity());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode repos = objectMapper.readTree(responseBody);

                for (JsonNode repo : repos) {
                    String repoName = repo.get("name").asText();
                    String privateUrl = "https://api.github.com/repos/" + username + "/" + repoName;
                    updateRepoVisibility(privateUrl, token);
                }

                System.out.println("All repositories have been made private.");
            } else {
                System.out.println("Failed to retrieve repositories. Response Code: " + response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private void updateRepoVisibility(String privateUrl, String token) {
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPatch request = new HttpPatch(privateUrl);
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            request.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            StringEntity requestBody = new StringEntity("{\"private\": true}");
            request.setEntity(requestBody);
            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                System.out.println("Repository visibility updated: " + privateUrl);
            } else {
                System.out.println("Failed to update repository visibility. Response Code: " + response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
}

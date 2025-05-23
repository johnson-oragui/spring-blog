package com.johnson.blog.service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeoLocationService {
  public String geoLocationFromIP(String ipAddress) {
    try {
      URL url = new URL("http://ip-api.com/json/" + ipAddress);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");

      try (InputStream input = connection.getInputStream()) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(input);
        return node.get("city").asText() + ", " +
            node.get("regionName").asText() + ", " +
            node.get("country").asText();
      }
    } catch (Exception e) {
      return "Unknown";
    }
  }
}

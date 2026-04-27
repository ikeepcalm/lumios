package dev.ua.ikeepcalm.lumios.telegram.campus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class CampusApiClientImpl implements CampusApiClient {

    @Value("${campus.api.url:https://api.campus.kpi.ua}")
    private String campusApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public CampusSubscriptionResult subscribe(String username, String password, String webhookUrl, String externalId) throws CampusAuthException {
        String accessToken = authenticate(username, password);
        createSubscription(accessToken, webhookUrl, externalId);
        return new CampusSubscriptionResult(accessToken);
    }

    @Override
    public void unsubscribe(String accessToken) throws CampusAuthException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> request = new HttpEntity<>(headers);
        try {
            restTemplate.exchange(campusApiUrl + "/monitoring/subscribe", HttpMethod.DELETE, request, String.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return;
            }
            throw new CampusAuthException("Campus unsubscribe failed: " + e.getStatusCode(), e);
        } catch (Exception e) {
            throw new CampusAuthException("Campus unsubscribe request failed: " + e.getMessage(), e);
        }
    }

    private String authenticate(String username, String password) throws CampusAuthException {
        Map<String, String> body = Map.of(
                "username", username,
                "password", password,
                "grant_type", "password"
        );
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(campusApiUrl + "/oauth/token", body, Map.class);
            Map<?, ?> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("access_token")) {
                throw new CampusAuthException("Campus auth response missing access_token");
            }
            return (String) responseBody.get("access_token");
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new CampusAuthException("Invalid credentials");
            }
            throw new CampusAuthException("Campus auth request failed: " + e.getStatusCode(), e);
        } catch (CampusAuthException e) {
            throw e;
        } catch (Exception e) {
            throw new CampusAuthException("Campus auth request failed: " + e.getMessage(), e);
        } finally {
            username = null;
            password = null;
        }
    }

    private void createSubscription(String accessToken, String webhookUrl, String externalId) throws CampusAuthException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of("webhook", webhookUrl, "id", externalId);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        try {
            restTemplate.postForEntity(campusApiUrl + "/monitoring/subscribe", request, String.class);
        } catch (HttpClientErrorException e) {
            throw new CampusAuthException("Campus subscribe request failed: " + e.getStatusCode(), e);
        } catch (Exception e) {
            throw new CampusAuthException("Campus subscribe request failed: " + e.getMessage(), e);
        }
    }

}

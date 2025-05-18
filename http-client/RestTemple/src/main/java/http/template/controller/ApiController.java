package http.template.controller;

import http.template.service.ExternalApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ApiController {
    @Autowired
    private ExternalApiService externalApiService;

    @GetMapping("/api/random-user")
    public ResponseEntity<?> getRandomUser() {
        try {
            return ResponseEntity.ok(externalApiService.fetchUserData());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "API 호출 실패: " + e.getMessage()));
        }
    }
}

package http.template.service;

import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class ExternalApiService {
    private static final String apiUrl = "https://randomuser.me/api/";

    @Autowired
    private RestTemplate restTemplate;

    public JSONObject fetchUserData(){
        try {
            return restTemplate.getForObject(apiUrl, JSONObject.class);
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }
}

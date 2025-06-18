package batch.project.service;

import batch.project.dto.SocialUserInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SocialLoginService {

    private final RestClient restClient;

    public SocialLoginService(RestClient restClient) {
        this.restClient = restClient;
    }

    public SocialUserInfo getGoogleUserInfo(String accessToken) {
        return restClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(SocialUserInfo.class);
    }

    public SocialUserInfo getKakaoUserInfo(String accessToken) {
        return restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(SocialUserInfo.class);
    }

    public SocialUserInfo getNaverUserInfo(String accessToken) {
        return restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(SocialUserInfo.class);
    }
}

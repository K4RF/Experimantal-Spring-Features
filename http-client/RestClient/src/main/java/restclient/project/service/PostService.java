package restclient.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import restclient.project.dto.request.PostRequest;

@Service
@RequiredArgsConstructor
public class PostService {
    private final RestClient restClient;

    public String getPostById(Long id) {
        return restClient.get()
                .uri("/posts/{id}", id)
                .retrieve()
                .body(String.class);
    }

    public String createPost(PostRequest request) {
        return restClient.post()
                .uri("/posts")
                .body(request)
                .retrieve()
                .body(String.class);
    }
}

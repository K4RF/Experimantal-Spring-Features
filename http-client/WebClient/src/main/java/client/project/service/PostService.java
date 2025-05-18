package client.project.service;

import client.project.dto.request.PostRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PostService {
    private final WebClient webClient;

    public Mono<String> getPostById(Long id) {
        return webClient.get()
                .uri("/posts/{id}", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> Mono.error(new RuntimeException("Client Error")))
                .bodyToMono(String.class);
    }

    public Mono<String> createPost(PostRequest request) {
        return webClient.post()
                .uri("/posts")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class);
    }
}

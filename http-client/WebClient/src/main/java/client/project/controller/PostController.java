package client.project.controller;

import client.project.dto.request.PostRequest;
import client.project.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping("/posts/{id}")
    public Mono<String> getPost(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    @PostMapping("/posts")
    public Mono<String> createPost(@RequestBody PostRequest request) {
        return postService.createPost(request);
    }
}

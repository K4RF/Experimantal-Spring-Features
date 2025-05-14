package restclient.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import restclient.project.dto.request.PostRequest;
import restclient.project.service.PostService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping("/posts/{id}")
    public String getPost(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    @PostMapping("/posts")
    public String createPost(@RequestBody PostRequest request) {
        return postService.createPost(request);
    }
}

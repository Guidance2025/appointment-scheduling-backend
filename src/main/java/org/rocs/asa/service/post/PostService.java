package org.rocs.asa.service.post;

import org.rocs.asa.domain.post.Post;
import org.rocs.asa.dto.CreatePostRequest;

import java.util.List;
import java.util.Map;

public interface PostService {
    Post createPost(CreatePostRequest request);
    List<Map<String, Object>> getAllPosts(int limit);
    Map<String, Object> getQuoteOfTheDay();
    Map<String, Object> getFeed(int limit);

    void deletePost(Long postId);
}
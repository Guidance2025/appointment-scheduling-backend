package org.rocs.asa.service.post;

import org.rocs.asa.domain.post.Post;

import java.util.List;

public interface PostService {
    Post createPost(Post post, Long employeeNumber);
    Post updatePost(Long postId, Post post, Long employeeNumber);
    void deletePost(Long postId, Long employeeNumber);
    List<Post> getPostsByStaff(Long employeeNumber);
    List<Post> getPostsBySection(Long sectionId);
    List<Post> getPostsByCategory(Long categoryId);
    List<Post> getAllPosts();
    Post getPostById(Long postId);
}

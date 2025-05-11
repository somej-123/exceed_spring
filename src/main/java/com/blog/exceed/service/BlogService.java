package com.blog.exceed.service;

import com.blog.exceed.dao.BlogAttachmentDao;
import com.blog.exceed.mapper.BlogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogService {
    private final BlogMapper blogMapper;

    public BlogService(BlogMapper blogMapper) {
        this.blogMapper = blogMapper;
    }

    public void saveAttachment(BlogAttachmentDao attachment) {
        blogMapper.insertBlogAttachment(attachment);
    }

    public List<BlogAttachmentDao> getAttachmentsByPostId(Long blogPostId) {
        return blogMapper.selectAttachmentsByPostId(blogPostId);
    }

    // public void addBlog(Blog blog) {
    //     blogRepository.save(blog);
    // }
}

package com.blog.exceed.service;

import com.blog.exceed.dao.BlogAttachmentDao;
import com.blog.exceed.dao.BlogCategoryDao;
import com.blog.exceed.mapper.BlogMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

    public void saveCategory(BlogCategoryDao category) {
        blogMapper.insertBlogCategory(category);
    }

    // 페이징/검색 지원 카테고리 목록 조회
    public Map<String, Object> getCategoriesPaged(int page, int size, String search) {
        int offset = (page - 1) * size;
        List<BlogCategoryDao> content = blogMapper.selectBlogCategoriesPaged(size, offset, search);
        int totalElements = blogMapper.countBlogCategoriesPaged(search);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        Map<String, Object> result = new HashMap<>();
        result.put("content", content);
        result.put("totalPages", totalPages);
        result.put("totalElements", totalElements);
        result.put("page", page);
        result.put("size", size);
        result.put("lastPage", totalPages);
        return result;
    }

    public BlogCategoryDao getCategoryById(String id) {
        return blogMapper.selectBlogCategoryById(id);
    }

    public void deleteCategory(String id) {
        blogMapper.deleteBlogCategory(id);
    }

    // public void addBlog(Blog blog) {
    //     blogRepository.save(blog);
    // }
}

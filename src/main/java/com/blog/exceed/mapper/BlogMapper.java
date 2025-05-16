package com.blog.exceed.mapper;

import com.blog.exceed.dao.BlogAttachmentDao;
import com.blog.exceed.dao.BlogCategoryDao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BlogMapper {
    // void addBlog(Blog blog);
    void insertBlogAttachment(BlogAttachmentDao attachment);
    List<BlogAttachmentDao> selectAttachmentsByPostId(Long blogPostId);
    List<BlogCategoryDao> selectBlogCategories();
    void insertBlogCategory(BlogCategoryDao category);
    List<BlogCategoryDao> selectBlogCategoriesPaged(@Param("size") int size, @Param("offset") int offset, @Param("search") String search);
    int countBlogCategoriesPaged(@Param("search") String search);
    BlogCategoryDao selectBlogCategoryById(@Param("id") String id);
    void deleteBlogCategory(@Param("id") String id);
}

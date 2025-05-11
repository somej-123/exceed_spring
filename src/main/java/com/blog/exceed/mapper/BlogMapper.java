package com.blog.exceed.mapper;

import com.blog.exceed.dao.BlogAttachmentDao;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BlogMapper {
    // void addBlog(Blog blog);
    void insertBlogAttachment(BlogAttachmentDao attachment);
    List<BlogAttachmentDao> selectAttachmentsByPostId(Long blogPostId);
}

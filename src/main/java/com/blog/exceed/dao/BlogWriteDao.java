package com.blog.exceed.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BlogWriteDao {
    private String userId;
    private String title;
    private String content;
    private String category;
    private String tags;
    private String thumbnail;
}

    

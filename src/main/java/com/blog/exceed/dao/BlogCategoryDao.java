package com.blog.exceed.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BlogCategoryDao {
    private int blogCategoryId;
    private String name;
    private String description;
    private String createdAt;
}

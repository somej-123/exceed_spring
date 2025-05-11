package com.blog.exceed.dao;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BlogAttachmentDao {
    private Long blogAttachmentId;
    private Long blogPostId;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Date uploadedAt;
}


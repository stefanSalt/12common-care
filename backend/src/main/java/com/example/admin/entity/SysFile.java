package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_file")
public class SysFile {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String originalName;

    private String storedName;

    /** Relative path on disk (e.g. yyyy/MM/dd/{id}.ext) */
    private String path;

    private Long size;

    private String contentType;

    /** PUBLIC / PRIVATE */
    private String visibility;

    private Long userId;

    @TableLogic
    private Integer deleted;
}


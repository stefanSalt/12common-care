package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String password;

    /** sys_file.id (avatar), stored as PUBLIC file */
    private Long avatarFileId;

    private String nickname;

    private String email;

    private String phone;

    /** 0-disabled 1-enabled */
    private Integer status;

    @TableLogic
    private Integer deleted;
}

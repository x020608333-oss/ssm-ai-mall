package com.shop.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String email;
    /** user 普通用户 / admin 管理员 */
    private String role;
    /** 1正常 0禁用 */
    private Integer status;
    private Date createdAt;
    private Date updatedAt;
}

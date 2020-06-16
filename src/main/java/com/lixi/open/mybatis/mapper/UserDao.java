package com.lixi.open.mybatis.mapper;

import com.lixi.open.mybatis.User;

public interface UserDao {

    // 这个地方 mybatis 做了处理，如果直接是 long 基本类型，如果为 null，就炸了
    Long count(String name);

    User find(Long id);
}

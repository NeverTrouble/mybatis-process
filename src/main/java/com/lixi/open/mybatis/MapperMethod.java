package com.lixi.open.mybatis;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class MapperMethod {
    private String method;
    private String pType;
    private String sql;

    public Object execute(Object... args) {
        // 拼接 sql，执行 sql，返回结果
        log.info("执行 method: [{}], sql: [{}]", method, String.format(sql, args));
        return null;
    }
}


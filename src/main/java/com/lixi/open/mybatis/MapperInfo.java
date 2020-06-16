package com.lixi.open.mybatis;

import lombok.Data;

import java.util.List;

@Data
public class MapperInfo {

    private String cls;
    private List<MapperMethod> methods;
}


package com.example.admin.common;

import java.util.List;
import lombok.Data;

@Data
public class PageResult<T> {
    private List<T> records;
    private long total;
    private long current;
    private long size;
}


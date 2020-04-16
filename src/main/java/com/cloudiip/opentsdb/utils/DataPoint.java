package com.cloudiip.opentsdb.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author : longwenhe
 * @date : 2020/4/15 15:09
 * @description :
 */
@Getter
@Setter
public class DataPoint {
    private String metric;
    private Float value;
    private long timestamp;
    private Map<String, String> tags;
}


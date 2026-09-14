package com.shop.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface StatsService {

    Map<String, Object> dashboard();

    List<Map<String, Object>> daily(int days);
}

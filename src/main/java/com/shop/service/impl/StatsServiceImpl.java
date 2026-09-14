package com.shop.service.impl;

import com.shop.dao.StatMapper;
import com.shop.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private StatMapper statMapper;

    @Override
    public Map<String, Object> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("todayOrderCount", statMapper.todayOrderCount());
        data.put("todaySales", statMapper.todaySales() == null ? BigDecimal.ZERO : statMapper.todaySales());
        data.put("userCount", statMapper.userCount());
        data.put("unpaidCount", statMapper.unpaidCount());
        data.put("categoryStats", statMapper.categoryStats());
        return data;
    }

    @Override
    public List<Map<String, Object>> daily(int days) {
        return statMapper.dailyStats(days);
    }
}

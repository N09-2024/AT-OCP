package com.ocp.at.service;

import com.ocp.at.dto.response.DashboardDataResponse;

public interface DashboardService {
    DashboardDataResponse getDashboardStats(String username);
}

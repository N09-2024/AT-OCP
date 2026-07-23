package com.ocp.at.service;

import com.ocp.at.entity.SystemSetting;
import com.ocp.at.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SystemSettingRepository settingRepository;

    @Value("${app.security.max-login-attempts:5}")
    private int defaultMaxLoginAttempts;

    private String get(String key, String defaultVal) {
        return settingRepository.findById(key)
            .map(SystemSetting::getValue)
            .orElse(defaultVal);
    }

    public Map<String, Object> getSettings() {
        Map<String, Object> result = new HashMap<>();
        result.put("maintenanceMode",        Boolean.parseBoolean(get("maintenanceMode",        "false")));
        result.put("sessionTimeoutMinutes",  Integer.parseInt(get("sessionTimeoutMinutes",      "60")));
        result.put("maxLoginAttempts",       Integer.parseInt(get("maxLoginAttempts",           String.valueOf(defaultMaxLoginAttempts))));
        result.put("inscriptionOuverte",     Boolean.parseBoolean(get("inscriptionOuverte",     "false")));
        result.put("emailNotifications",     Boolean.parseBoolean(get("emailNotifications",     "true")));
        result.put("retentionDays",          Integer.parseInt(get("retentionDays",              "365")));
        return result;
    }

    @Transactional
    public Map<String, Object> updateSettings(Map<String, Object> settings) {
        settings.forEach((key, value) -> {
            if (value != null) {
                settingRepository.save(new SystemSetting(key, String.valueOf(value)));
            }
        });
        return getSettings();
    }
}

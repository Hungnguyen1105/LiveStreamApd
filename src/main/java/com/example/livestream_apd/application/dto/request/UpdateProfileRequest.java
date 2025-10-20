package com.example.livestream_apd.application.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class UpdateProfileRequest {
    @Size(max = 100, message = "không được vượt quá 100 kí tự")
    private String fullName;
    @Size(max = 500, message = "Không được vượt quá 500 ký tự")
    private String bio;
    private String avatarUrl;
    private Map<String, String> socialLinks;
    private Boolean isPrivate;
    private Boolean allowDirectMessages;
    private Boolean showOnlineStatus;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean liveNotifications;
    private Boolean followNotifications;
    private Boolean messageNotifications;
}

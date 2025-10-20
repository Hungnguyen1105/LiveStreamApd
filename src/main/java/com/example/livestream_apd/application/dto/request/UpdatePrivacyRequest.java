package com.example.livestream_apd.application.dto.request;

import lombok.Data;

@Data
public class UpdatePrivacyRequest {
    private Boolean isPrivate;
    private Boolean allowDirectMessages;
    private Boolean showOnlineStatus;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean liveNotifications;
    private Boolean followNotifications;
    private Boolean messageNotifications;
}

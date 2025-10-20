package com.example.livestream_apd.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchDirectMessageRequest {
    
    private String query;
    
    @Builder.Default
    private int page = 0;
    
    @Builder.Default
    private int size = 20;
}

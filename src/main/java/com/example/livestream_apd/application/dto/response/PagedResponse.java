package com.example.livestream_apd.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponse <T> {
    private List<T> content;
    private int size;
    private int page;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}

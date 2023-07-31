package org.example.entity;

import lombok.Data;

import java.util.List;
@Data
public class SearchResponse {
    private List<Cartoon> results;
    private boolean corrected;
    private String correctedQuery;
}

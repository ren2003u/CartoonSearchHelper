package org.example.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AttributeSearchRequest {
    private Map<String, List<String>> includeAttributes;
    private Map<String, List<String>> excludeAttributes;
}

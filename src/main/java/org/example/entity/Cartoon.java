package org.example.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.security.SecureRandom;
import java.util.List;

@Data
public class Cartoon {
    private String transliterationTitle;
    private String japaneseTitle;
    private String imageUrl;
    @JsonProperty("社團")
    private List<String> community;
    @JsonProperty("作者")
    private List<String> authors;
    @JsonProperty("角色")
    private List<String> roles;
    @JsonProperty("同人")
    private List<String> fanMade;
    @JsonProperty("分類")
    private List<String> classifications;
    @JsonProperty("語言")
    private List<String> languages;
    @JsonProperty("頁數")
    private List<String> pageNumber;
    @JsonProperty("標籤")
    private List<String> labels; // Changed to match the JSON property
    private String cartoonId; // Added this field
    // You can add other fields if needed
}

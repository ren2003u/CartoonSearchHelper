package org.example.entity;

import lombok.Data;

import java.security.SecureRandom;
import java.util.List;

@Data
public class Cartoon {
    private String transliterationTitle;

    private String japaneseTitle;

    private List<String> community;

    private List<String> authors;

    private List<String> tags;

    private List<String> roles;

    private List<String> fanMade;

    private List<String> classifications;

    private Integer pageNumber;
}

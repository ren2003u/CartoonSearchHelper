package org.example.Controller;

import org.example.Service.CartoonSearchService;
import org.example.entity.Cartoon;
import org.example.entity.SearchResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/cartoons")
public class CartoonController {

    private final CartoonSearchService cartoonSearchService;

    public CartoonController(CartoonSearchService cartoonSearchService) {
        this.cartoonSearchService = cartoonSearchService;
    }

    @GetMapping("/fuzzySearch/{name}")
    public SearchResponse fuzzySearch(@PathVariable String name) throws IOException {
        return cartoonSearchService.searchCartoons(name);
    }
}

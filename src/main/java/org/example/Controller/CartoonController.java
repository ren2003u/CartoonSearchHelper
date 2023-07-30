package org.example.Controller;

import org.example.Service.CartoonSearchService;
import org.example.entity.Cartoon;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cartoons")
public class CartoonController {

    private final CartoonSearchService cartoonSearchService;

    public CartoonController(CartoonSearchService cartoonSearchService) {
        this.cartoonSearchService = cartoonSearchService;
    }

    @GetMapping("/fuzzySearch/{name}")
    public List<Cartoon> fuzzySearch(@PathVariable String name) {
        return cartoonSearchService.searchCartoons(name);
    }
}

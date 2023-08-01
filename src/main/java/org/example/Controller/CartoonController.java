package org.example.Controller;

import org.example.Service.CartoonSearchService;
import org.example.entity.AttributeSearchRequest;
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

    @GetMapping("/listAttributeAllValues/{attribute}")
    public List<String> listAttributeAllValues(@PathVariable String attribute) {
        return cartoonSearchService.getAttributeValues(attribute);
    }
    @GetMapping("/fuzzySearchAttributeValues/{attribute}/{query}")
    public List<String> fuzzySearchAttributeValues(@PathVariable String attribute, @PathVariable String query) {
        return cartoonSearchService.searchAttributeValues(attribute, query);
    }
    @PostMapping("/attributeSearch")
    public List<Cartoon> attributeSearch(@RequestBody AttributeSearchRequest request) throws IOException {
        return cartoonSearchService.searchByAttributes(request.getIncludeAttributes(), request.getExcludeAttributes());
    }
    @GetMapping("/fuzzySearch/{name}")
    public SearchResponse fuzzySearch(@PathVariable String name) throws IOException {
        return cartoonSearchService.searchCartoons(name);
    }
}

package org.example.Service;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.example.Repository.CartoonSearchRepository;
import org.example.entity.Cartoon;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartoonSearchService {

    private final CartoonSearchRepository cartoonSearchRepository;

    public CartoonSearchService(CartoonSearchRepository cartoonSearchRepository) {
        this.cartoonSearchRepository = cartoonSearchRepository;
    }

    public List<Cartoon> searchCartoons(String name) {
        // Construct your query here
        QueryBuilder query = QueryBuilders.boolQuery()
                .should(QueryBuilders.queryStringQuery("*" + name + "*").field("transliterationTitle").analyzeWildcard(true))
                .should(QueryBuilders.queryStringQuery("*" + name + "*").field("japaneseTitle").analyzeWildcard(true));
        return cartoonSearchRepository.search(query);
    }
}

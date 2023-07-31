package org.example.Service;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.example.Repository.CartoonSearchRepository;
import org.example.entity.Cartoon;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class CartoonSearchService {

    private final CartoonSearchRepository cartoonSearchRepository;
    private final SpellCheckerService spellCheckerService;

    public CartoonSearchService(CartoonSearchRepository cartoonSearchRepository, SpellCheckerService spellCheckerService) {
        this.cartoonSearchRepository = cartoonSearchRepository;
        this.spellCheckerService = spellCheckerService;
    }

    public List<Cartoon> searchCartoons(String name) throws IOException {
        String correctedName = spellCheckerService.correctSpelling(name);
        // Construct your query here
        QueryBuilder query = QueryBuilders.boolQuery()
                .should(QueryBuilders.queryStringQuery("*" + correctedName + "*").field("transliterationTitle").analyzeWildcard(true))
                .should(QueryBuilders.queryStringQuery("*" + correctedName + "*").field("japaneseTitle").analyzeWildcard(true));
        return cartoonSearchRepository.search(query);
    }
}

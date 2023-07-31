package org.example.Service;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.example.Repository.CartoonSearchRepository;
import org.example.entity.Cartoon;
import org.example.entity.SearchResponse;
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

    public SearchResponse searchCartoons(String name) throws IOException {
        QueryBuilder query = createQuery(name);
        List<Cartoon> results = cartoonSearchRepository.search(query);

        boolean isCorrected = false;
        String correctedName = name;

        if (results.isEmpty()) {
            correctedName = spellCheckerService.correctSpelling(name);
            isCorrected = !name.equals(correctedName);
            query = createQuery(correctedName);
            results = cartoonSearchRepository.search(query);
        }

        SearchResponse response = new SearchResponse();
        response.setResults(results);
        response.setCorrected(isCorrected);
        response.setCorrectedQuery(correctedName);

        return response;
    }

    private QueryBuilder createQuery(String name) {
        return QueryBuilders.boolQuery()
                .should(QueryBuilders.queryStringQuery("*" + name + "*").field("transliterationTitle").analyzeWildcard(true))
                .should(QueryBuilders.queryStringQuery("*" + name + "*").field("japaneseTitle").analyzeWildcard(true));
    }
}

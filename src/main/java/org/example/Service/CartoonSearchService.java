package org.example.Service;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.example.Repository.CartoonSearchRepository;
import org.example.entity.Cartoon;
import org.example.entity.SearchResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class CartoonSearchService {

    private final CartoonSearchRepository cartoonSearchRepository;
    private final SpellCheckerService spellCheckerService;

    public CartoonSearchService(CartoonSearchRepository cartoonSearchRepository, SpellCheckerService spellCheckerService) {
        this.cartoonSearchRepository = cartoonSearchRepository;
        this.spellCheckerService = spellCheckerService;
    }
    public List<Cartoon> searchByAttributes(Map<String, List<String>> includeAttributes, Map<String, List<String>> excludeAttributes) {
        QueryBuilder query = createAttributeQuery(includeAttributes, excludeAttributes);
        return cartoonSearchRepository.search(query);
    }

    public List<String> getAttributeValues(String attribute) {
        return cartoonSearchRepository.getAttributeValues(attribute);
    }

    public List<String> searchAttributeValues(String attribute, String query) {
        QueryBuilder queryBuilder = QueryBuilders.wildcardQuery(attribute, "*" + query + "*");
        return cartoonSearchRepository.searchAttributeValues(queryBuilder);
    }
    private QueryBuilder createAttributeQuery(Map<String, List<String>> includeAttributes, Map<String, List<String>> excludeAttributes) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        for (Map.Entry<String, List<String>> entry : includeAttributes.entrySet()) {
            String field = entry.getKey();
            List<String> values = entry.getValue();
            for (String value : values) {
                query.must(QueryBuilders.matchQuery(field, value));
            }
        }

        for (Map.Entry<String, List<String>> entry : excludeAttributes.entrySet()) {
            String field = entry.getKey();
            List<String> values = entry.getValue();
            for (String value : values) {
                query.mustNot(QueryBuilders.matchQuery(field, value));
            }
        }

        return query;
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

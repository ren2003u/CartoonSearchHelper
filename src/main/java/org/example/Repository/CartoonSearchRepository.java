package org.example.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.example.entity.Cartoon;
import org.springframework.stereotype.Repository;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CartoonSearchRepository {

    private final RestHighLevelClient client;

    public CartoonSearchRepository(RestHighLevelClient client) {
        this.client = client;
    }

    public List<Cartoon> search(QueryBuilder query) {
        SearchRequest searchRequest = new SearchRequest("cartoons");
        searchRequest.source().query(query);

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            // Parse the response and return the results
            return parseResponse(response);
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute search", e);
        }
    }
    public List<String> getAttributeValues(String attribute) {
        SearchRequest searchRequest = new SearchRequest("cartoons");
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("distinct_values").field(attribute);
        searchRequest.source().aggregation(aggregation);

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            Terms terms = response.getAggregations().get("distinct_values");
            return terms.getBuckets().stream().map(Bucket::getKeyAsString).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to get attribute values", e);
        }
    }
    public List<String> searchAttributeValues(QueryBuilder queryBuilder) {
        SearchRequest searchRequest = new SearchRequest("cartoons");
        searchRequest.source().query(queryBuilder);

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = response.getHits().getHits();

            // Use a Set to store distinct values
            Set<String> distinctValues = new HashSet<>();

            // Iterate through the hits and extract the attribute values
            for (SearchHit hit : searchHits) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                for (Map.Entry<String, Object> entry : sourceAsMap.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof List) {
                        List<?> values = (List<?>) value;
                        for (Object v : values) {
                            distinctValues.add(v.toString());
                        }
                    } else if (value != null) {
                        distinctValues.add(value.toString());
                    }
                }
            }

            return new ArrayList<>(distinctValues);
        } catch (IOException e) {
            throw new RuntimeException("Failed to search attribute values", e);
        }
    }

    private List<Cartoon> parseResponse(SearchResponse response) throws JsonProcessingException {
        // Parse the response and convert it to a list of Cartoon objects
        SearchHit[] searchHits = response.getHits().getHits();
        List<Cartoon> cartoons = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Ignore unknown properties
        for (SearchHit hit : searchHits) {
            String sourceAsString = hit.getSourceAsString();
            Cartoon cartoon = objectMapper.readValue(sourceAsString, Cartoon.class);
            cartoons.add(cartoon);
        }
        return cartoons;
    }
}

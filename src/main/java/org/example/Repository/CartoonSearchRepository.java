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

// The CartoonSearchRepository is responsible for interacting with ElasticSearch to perform search operations related to cartoons.
@Repository
public class CartoonSearchRepository {

    // The RestHighLevelClient is the main component to interact with an Elasticsearch cluster.
    // The RestHighLevelClient is thread-safe, meaning you can use a single instance across multiple threads without any issues.
    private final RestHighLevelClient client;

    // Constructor to initialize the RestHighLevelClient.
    // Spring Boot will automatically provide the configured RestHighLevelClient bean for you
    // based on the settings in application.yml.
    public CartoonSearchRepository(RestHighLevelClient client) {
        this.client = client;
    }

    // If you ever need to manually configure the RestHighLevelClient (for example, in a non-Spring Boot
    // application or if you want more control over the client's configuration), you can use the following approach:
    /*
    public CartoonSearchRepository() {
        // Manually configure the RestHighLevelClient:
        this.client = new RestHighLevelClient(
            RestClient.builder(
                new HttpHost("yourElasticsearchHost", yourElasticsearchPort, "http")
            )
        );
    }
    */
    // Note: In a real-world scenario, you'd typically choose one approach or the other, not both.
    // The above manual configuration is commented out because it's just for illustrative purposes.

    // This method performs a search on the cartoons index using the provided query.
    public List<Cartoon> search(QueryBuilder query) {
        // Create a new search request targeting the "cartoons" index in Elasticsearch.
        // In Elasticsearch, an index is similar to a table in a relational database. It's where the data (documents) are stored.
        SearchRequest searchRequest = new SearchRequest("cartoons");

        // The `source()` method is used to access the SearchSourceBuilder, which allows for further refinement of the search request.
        // The `query()` method sets the search criteria using the provided QueryBuilder.
        // The _source field in Elasticsearch is a special field that contains the original JSON object that was passed during indexing.
        // When a document is indexed, Elasticsearch stores the original JSON in the _source field and creates an inverted index from it for fast searching.
        // During a search, by default, the original JSON from the _source field is returned.
        searchRequest.source().query(query);

        try {
            // Execute the search request against Elasticsearch.
            // Execute the search request against Elasticsearch using the RestHighLevelClient.
            // The search method sends the searchRequest to Elasticsearch and retrieves the results.
            // The results are stored in a SearchResponse object, which contains details of the search hits.
            // Each hit represents a document that matched the search criteria, and you can retrieve the original JSON data from the _source field for each hit.
            // RequestOptions.DEFAULT represents the default request configurations. It's used here to specify that default settings should be used for this request.
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            // Parse the response to extract cartoon data and return the results.
            return parseResponse(response);
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute search", e);
        }
    }

    // This method retrieves distinct values for a given attribute from the cartoons index.
    public List<String> getAttributeValues(String attribute) {
        // In Elasticsearch, fields that are used for aggregations (like getting distinct values)
        // often have a ".keyword" suffix. This ".keyword" version of the field is not tokenized,
        // meaning it treats the entire field value as a single term. This behavior is crucial for
        // operations that require the entire field value to be intact, such as exact match searches,
        // sorting, and aggregations. For instance, if a field has a value "Action Adventure", without
        // ".keyword", it might be tokenized into two separate terms: "Action" and "Adventure".
        // However, with ".keyword", it's treated as the single term "Action Adventure", which is
        // especially useful for string fields that you want to aggregate on, like getting distinct values.
        String attributeKeyword = attribute + ".keyword";

        // This is the maximum number of distinct values you want to retrieve.
        // Depending on your use case, you might want to adjust this.
        int size = 1000;

        // Create a new search request for the "cartoons" index.
        SearchRequest searchRequest = new SearchRequest("cartoons");

        // Aggregations in Elasticsearch allow you to get insights from your data.
        // In this case, we're using a "terms" aggregation, which is useful for getting
        // distinct values (or terms) from a field. The aggregation is named "distinct_values".
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("distinct_values")
                .field(attributeKeyword)
                .size(size);

        // Add the aggregation to the search request.
        searchRequest.source().aggregation(aggregation);

        try {
            // Execute the search request.
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            // The response contains the results of the aggregation.
            // In this case, we're interested in the "distinct_values" aggregation.
            Terms terms = response.getAggregations().get("distinct_values");

            // Convert the results of the aggregation into a list of strings.
            // Each bucket in the terms aggregation represents a distinct value,
            // and the getKeyAsString method gives us that value.
            return terms.getBuckets().stream().map(Bucket::getKeyAsString).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to get attribute values", e);
        }
    }

    // This method searches for distinct values of a given attribute that match a specific query.
    // Note: This method is not used in the current implementation.
    public List<String> searchAttributeValues(String attribute, QueryBuilder queryBuilder) {
        SearchRequest searchRequest = new SearchRequest("cartoons");
        searchRequest.source().query(queryBuilder);

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = response.getHits().getHits();

            // Use a Set to ensure distinct values.
            Set<String> distinctValues = new HashSet<>();

            // Extract the values of the specified attribute from the search hits.
            for (SearchHit hit : searchHits) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                Object value = sourceAsMap.get(attribute);
                if (value instanceof List) {
                    List<?> values = (List<?>) value;
                    for (Object v : values) {
                        distinctValues.add(v.toString());
                    }
                } else if (value != null) {
                    distinctValues.add(value.toString());
                }
            }

            return new ArrayList<>(distinctValues);
        } catch (IOException e) {
            throw new RuntimeException("Failed to find attribute values", e);
        }
    }

    // Helper method to parse the search response and convert it to a list of Cartoon objects.
    private List<Cartoon> parseResponse(SearchResponse response) throws JsonProcessingException {
        SearchHit[] searchHits = response.getHits().getHits();
        List<Cartoon> cartoons = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        // Configure the ObjectMapper to ignore properties that are not defined in the Cartoon class.
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        for (SearchHit hit : searchHits) {
            String sourceAsString = hit.getSourceAsString();
            Cartoon cartoon = objectMapper.readValue(sourceAsString, Cartoon.class);
            cartoons.add(cartoon);
        }
        return cartoons;
    }
}

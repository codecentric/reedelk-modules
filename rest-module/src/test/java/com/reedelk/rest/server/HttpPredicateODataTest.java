package com.reedelk.rest.server;

import com.reedelk.rest.commons.AsSerializableMap;
import com.reedelk.rest.commons.QueryParameters;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.netty.http.server.HttpServerRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.assertj.core.api.Assertions.assertThat;

class HttpPredicateODataTest {

    // GET /People
    @Test
    void requestEntityCollections() {
        // Given
        String uriPattern = "/People";
        String uri = "/People";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).isEmpty();
            assertThat(pathParams).isEmpty();
        });
    }

    // GET /People('russellwhyte')
    @Test
    void requestIndividualEntityByID() {
        // Given
        String uriPattern = "/People('{ID}')";
        String uri = "/People('russellwhyte')";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).isEmpty();
            assertThat(pathParams).containsKeys("ID");
            assertThat(pathParams.get("ID")).isEqualTo("russellwhyte");
        });
    }

    // GET /Airports('KSFO')/Name
    @Test
    void requestIndividualProperty() {
        // Given
        String uriPattern = "/Airports('{ID}')/Name";
        String uri = "/Airports('KSFO')/Name";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).isEmpty();
            assertThat(pathParams).containsKeys("ID");
            assertThat(pathParams.get("ID")).isEqualTo("KSFO");
        });
    }

    // GET /Airports('KSFO')/Name/$value
    @Test
    void requestIndividualPropertyRawValue() {
        // Given
        String uriPattern = "/Airports('{ID}')/Name/$value";
        String uri = "/Airports('KSFO')/Name/$value";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).isEmpty();
            assertThat(pathParams).containsKeys("ID");
            assertThat(pathParams.get("ID")).isEqualTo("KSFO");
        });
    }

    // GET /People?$filter=FirstName eq 'Scott'
    @Test
    void requestWithFilterOnSimpleType() {
        String uriPattern = "/People";
        String uri = "/People?$filter=FirstName eq 'Scott'";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).containsKeys("$filter");
            assertThat(queryParams.get("$filter")).containsExactly("FirstName eq 'Scott'");
            assertThat(pathParams).isEmpty();
        });
    }

    // GET /Airports?$filter=contains(Location/Address, 'San Francisco')
    @Test
    void requestWithFilterOnComplexType() {
        String uriPattern = "/Airports";
        String uri = "/Airports?$filter=contains(Location/Address, 'San Francisco')";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).containsKeys("$filter");
            assertThat(queryParams.get("$filter")).containsExactly("contains(Location/Address, 'San Francisco')");
            assertThat(pathParams).isEmpty();
        });
    }

    // GET /People?$filter=Gender eq Microsoft.OData.SampleService.Models.TripPin.PersonGender'Female'
    @Test
    void requestWithFilterOnEnumProperties() {
        String uriPattern = "/People";
        String uri = "/People?$filter=Gender eq Microsoft.OData.SampleService.Models.TripPin.PersonGender'Female'";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).containsKeys("$filter");
            assertThat(queryParams.get("$filter")).containsExactly("Gender eq Microsoft.OData.SampleService.Models.TripPin.PersonGender'Female'");
            assertThat(pathParams).isEmpty();
        });
    }

    // GET /People?$expand=Trips($filter=Name eq 'Trip in US')
    @Test
    void requestWithNestedFilterInExpand() {
        // Given
        String uriPattern = "/People";
        String uri = "/People?$expand=Trips($filter=Name eq 'Trip in US')";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).containsKeys("$expand");
            assertThat(queryParams.get("$expand")).containsExactly("Trips($filter=Name eq 'Trip in US')");
            assertThat(pathParams).isEmpty();
        });
    }

    // GET /People('scottketchum')/Trips?$orderby=EndsAt desc
    @Test
    void requestWithOrderBy() {
        // Given
        String uriPattern = "/People('{ID}')/Trips";
        String uri = "/People('scottketchum')/Trips?$orderby=EndsAt desc";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).containsKeys("$orderby");
            assertThat(queryParams.get("$orderby")).containsExactly("EndsAt desc");
            assertThat(pathParams).containsKeys("ID");
            assertThat(pathParams.get("ID")).isEqualTo("scottketchum");
        });
    }

    // GET /People?$top=2
    @Test
    void requestWithTop() {
        // Given
        String uriPattern = "/People";
        String uri = "/People?$top=2";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).containsKeys("$top");
            assertThat(queryParams.get("$top")).containsExactly("2");
            assertThat(pathParams).isEmpty();
        });
    }

    // GET /People?$skip=18
    @Test
    void requestWithSkip() {
        // Given
        String uriPattern = "/People";
        String uri = "/People?$skip=18";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).containsKeys("$skip");
            assertThat(queryParams.get("$skip")).containsExactly("18");
            assertThat(pathParams).isEmpty();
        });
    }

    // GET /People?$count=true
    @Test
    void requestWithCount() {
        // Given
        String uriPattern = "/People";
        String uri = "/People?$count=true";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).containsKeys("$count");
            assertThat(queryParams.get("$count")).containsExactly("true");
            assertThat(pathParams).isEmpty();
        });
    }

    // GET /People('keithpinckney')?$expand=Friends
    @Test
    void requestWithExpand() {
        // Given
        String uriPattern = "/People('{Name}')";
        String uri = "/People('keithpinckney')?$expand=Friends";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).containsKeys("$expand");
            assertThat(queryParams.get("$expand")).containsExactly("Friends");
            assertThat(pathParams).containsKeys("Name");
            assertThat(pathParams.get("Name")).isEqualTo("keithpinckney");
        });
    }

    // GET /Airports?$select=Name, IcaoCode
    @Test
    void requestWithSelect() {
        // Given
        String uriPattern = "/Airports";
        String uri = "/Airports?$select=Name, IcaoCode";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).containsKeys("$select");
            assertThat(queryParams.get("$select")).containsExactly("Name, IcaoCode");
            assertThat(pathParams).isEmpty();
        });
    }

    // GET /People?$search=Boise
    @Test
    void requestWithSearch() {
        // Given
        String uriPattern = "/People";
        String uri = "/People?$search=Boise";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).containsKeys("$search");
            assertThat(queryParams.get("$search")).containsExactly("Boise");
            assertThat(pathParams).isEmpty();
        });
    }

    // GET /People?$filter=Emails/any(s:endswith(s, 'contoso.com'))
    @Test
    void requestWithLambdaOperator1() {
        String uriPattern = "/People";
        String uri = "/People?$filter=Emails/any(s:endswith(s, 'contoso.com'))";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).containsKeys("$filter");
            assertThat(queryParams.get("$filter")).containsExactly("Emails/any(s:endswith(s, 'contoso.com'))");
            assertThat(pathParams).isEmpty();
        });
    }

    // GET /Me/Friends?$filter=Friends/any(f:f/FirstName eq 'Scott')
    @Test
    void requestWithLambdaOperator2() {

        String uriPattern = "/Me/Friends";
        String uri = "/Me/Friends?$filter=Friends/any(f:f/FirstName eq 'Scott'";

        // Expect
        assertThatMatchesWithConditions(uriPattern, uri, (pathParams, queryParams) -> {
            assertThat(queryParams).containsKeys("$filter");
            assertThat(queryParams.get("$filter")).containsExactly("Friends/any(f:f/FirstName eq 'Scott'");
            assertThat(pathParams).isEmpty();
        });
    }

    private void assertThatMatchesWithConditions(String uriPattern, String uri, BiConsumer<Map<String, String>, Map<String, List<String>>> queryAndPathParamsConsumer) {
        HttpPredicate predicate = new HttpPredicate(uriPattern, HTTP_1_1, GET);
        HttpServerRequest request = requestWith(GET, uri);

        boolean matches = predicate.test(request);
        assertThat(matches).isTrue();

        Map<String, String> pathParams = AsSerializableMap.of(predicate.apply(request.uri()));
        HashMap<String, List<String>> queryParams = QueryParameters.from(request.uri());

        queryAndPathParamsConsumer.accept(pathParams, queryParams);
    }

    private HttpServerRequest requestWith(HttpMethod method, String uri) {
        HttpServerRequest mockRequest = Mockito.mock(HttpServerRequest.class);
        Mockito.doReturn(HTTP_1_1).when(mockRequest).version();
        Mockito.doReturn(method).when(mockRequest).method();
        Mockito.doReturn(uri).when(mockRequest).uri();
        return mockRequest;
    }
}
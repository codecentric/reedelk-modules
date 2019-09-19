package com.reedelk.rest.client;

import com.reedelk.rest.client.uri.NotEmptyURIPathComponent;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.reedelk.rest.commons.MapUtils.newMap;
import static org.assertj.core.api.Assertions.assertThat;

class NotEmptyURIPathComponentTest {

    @Test
    void shouldCorrectlyReplaceOnlyUriParams() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/api/{uriParam1}/test");

        Map<String,String> uriParams = newMap("uriParam1", "value1");
        Map<String,String> queryParams = newMap();

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected = "/api/value1/test";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCorrectlyAddOnlyQueryParams() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/api/test");

        Map<String,String> uriParams = newMap();
        Map<String,String> queryParams = newMap(
                "query1","value1",
                "query2", "value2");

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected = "/api/test?query1=value1&query2=value2";
        assertThat(actual).isEqualTo(expected);
    }

    // The uri already contains some query parameters. This test verifies
    // that query parameters from the map are added at the end of the
    // existing ones.
    @Test
    void shouldCorrectlyAppendQueryParams() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/api/test?query1=value1&query2=value2");

        Map<String,String> uriParams = newMap();
        Map<String,String> queryParams = newMap(
                "query3","value3",
                "query4", "value4");

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected = "/api/test?query1=value1&query2=value2&query3=value3&query4=value4";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCorrectlyReplaceUriParamsAndAppendQueryParams() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/api/test/{uriParam1}/{uriParam2}/{uriParam3}");

        Map<String,String> uriParams = newMap(
                "uriParam1", "value1",
                "uriParam2", "value2",
                "uriParam3", "value3");

        Map<String,String> queryParams = newMap(
                "queryParam1", "queryParamValue1",
                "queryParam2", "queryParamValue2");

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected = "/api/test/value1/value2/value3?queryParam1=queryParamValue1&queryParam2=queryParamValue2";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReplaceWithEmptyStringWhenParametersInMapDoNotMapTemplateReplacements() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/api/test/{uriParam1}/{notMapped}");

        Map<String,String> uriParams =  newMap(
                "uriParam1","value1",
                "uriParam2","value2");
        Map<String,String> queryParams = newMap();

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected =  "/api/test/value1/";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReplaceWithEmptyStringWhenUriParamsMapIsNull() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/api/{uriParam1}/");

        Map<String,String> uriParams = null;
        Map<String,String> queryParams = newMap("query1","value1");

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected = "/api//?query1=value1";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldNotThrowExceptionWhenQueryParamsMapIsNull() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/api");

        Map<String,String> uriParams = newMap();
        Map<String,String> queryParams = null;

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected = "/api";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnUriAsIsWhenUriParamsAndQueryParamsAreEmpty() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/api/test");

        Map<String,String> uriParams = newMap();
        Map<String,String> queryParams = newMap();

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected = "/api/test";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldEncodeQueryParamValueCorrectly() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/api/test");

        Map<String,String> uriParams = newMap();
        Map<String,String> queryParams = newMap("query1", "+super fancy (@#$7 query param");

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected = "/api/test?query1=%2Bsuper%20fancy%20%28%40%23%247%20query%20param";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldEncodeQueryParamKeyCorrectly() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/api");

        Map<String,String> uriParams = newMap();
        Map<String,String> queryParams = newMap("+super+fancy+$key", "value");

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected = "/api?%2Bsuper%2Bfancy%2B%24key=value";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldEncodeGivenQueryParamCorrectly() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/api?+super_fancy key=super fancy value");

        Map<String,String> uriParams = newMap();
        Map<String,String> queryParams = newMap("query1", "value1");

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected = "/api?%2Bsuper_fancy%20key=super%20fancy%20value&query1=value1";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldEncodeGivenPathParamCorrectly() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/api/+(another+ test/super");

        Map<String,String> uriParams = newMap();
        Map<String,String> queryParams = newMap();

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected = "/api/+(another+%20test/super";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldEncodePathParamCorrectly() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/api$/{path1}/{path2}");

        Map<String,String> uriParams = newMap(
                "path1", "@(value1+31 one",
                "path2", "^&81}{]");

        Map<String,String> queryParams = newMap();

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected = "/api$/@(value1+31%20one/%5E&81%7D%7B%5D";
        assertThat(actual).isEqualTo(expected);
    }

    // OData
    @Test
    void shouldOdata1() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/TripPinRESTierService/Airports('{name}')/Name");

        Map<String,String> uriParams = newMap(
                "name", "KSFO");
        Map<String,String> queryParams = newMap();

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected = "/TripPinRESTierService/Airports('KSFO')/Name";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldOdata2() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/TripPinRESTierService/People");

        Map<String,String> uriParams = newMap();
        Map<String,String> queryParams = newMap("$top", "2");

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected = "/TripPinRESTierService/People?%24top=2";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldOdata3() {
        // Given
        NotEmptyURIPathComponent NotEmptyURIPathComponent = new NotEmptyURIPathComponent("/TripPinRESTierService/People?$t+ op=asdf as+df");

        Map<String,String> uriParams = newMap();
        Map<String,String> queryParams = newMap();

        // When
        String actual = NotEmptyURIPathComponent.expand(uriParams, queryParams);

        // Then
        String expected = "/TripPinRESTierService/People?%24t%2B%20op=asdf%20as%2Bdf";
        assertThat(actual).isEqualTo(expected);
    }
}
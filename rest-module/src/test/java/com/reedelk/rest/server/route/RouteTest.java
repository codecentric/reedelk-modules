package com.reedelk.rest.server.route;

// TODO: Fixme
class RouteTest {


    /**

     @Test void shouldMatchCorrectlyMethodAndPath() {
     // Given
     Route customers = new Route(POST, "/customers/{id}/{group}", testHandler);

     // When
     boolean matches = customers.matches(POST, "/customers/843/workers");

     // Then
     assertThat(matches).isTrue();
     }

     @Test void shouldBindPathParametersCorrectly() {
     // Given
     Route customers = new Route(GET, "/customers/{id}/{group}", testHandler);
     HttpRequest request = createRequest(GET, "/customers/84234/managers");

     // When
     Map<String, String> pathParamsMap = customers.bindPathParams(request);

     // Then
     assertThat(pathParamsMap).containsKeys("id", "group");
     assertThat(pathParamsMap.get("id")).isEqualTo("84234");
     assertThat(pathParamsMap.get("group")).isEqualTo("managers");
     }

     @Test void shouldReturnCorrectRouteHandler() {
     // Given
     Route test = new Route(DELETE, "/test", testHandler);

     // When
     RouteHandler handler = test.handler();

     // Then
     assertThat(handler).isEqualTo(testHandler);
     }

     @Test void shouldReturnCorrectMethod() {
     // Given
     Route test = new Route(OPTIONS, "/test", testHandler);

     // When
     RestMethod method = test.getMethod();

     // Then
     assertThat(method).isEqualTo(OPTIONS);
     }

     @Test void shouldReturnCorrectQueryParameters() {
     // Given
     Route test = new Route(DELETE, "/test", testHandler);
     HttpRequest request = createRequest(DELETE, "/test?param1=value1&param1=value2&param2=value3");

     // When
     Map<String, List<String>> queryParametersMap = test.queryParameters(request);

     // Then
     assertThat(queryParametersMap).containsKeys("param1", "param2");
     assertThat(queryParametersMap.get("param1")).containsExactlyInAnyOrder("value1", "value2");
     assertThat(queryParametersMap.get("param2")).containsExactlyInAnyOrder("value3");
     }

     private HttpRequest createRequest(RestMethod method, String uri) {
     return new DefaultHttpRequest(HTTP_1_1, HttpMethod.valueOf(method.name()), uri);
     }
     */
}

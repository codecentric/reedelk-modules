package com.reedelk.rest.server.route;

/**
 * Request paths sample taken from:
 * https://www.odata.org/getting-started/basic-tutorial/
 */
// TODO: Fixme
class RouteODataTest {



    /**

     * <p>
     * // GET /Airports('KSFO')/Name
     * @Test void requestIndividualProperty() {
     * // Given
     * Route route = new Route(GET, "/Airports('{ID}')/Name", testHandler);
     * HttpRequest request = createRequest(GET, "/Airports('KSFO')/Name");
     * <p>
     * // When
     * boolean matches = route.matches(request);
     * Map<String, List<String>> queryParams = route.queryParameters(request);
     * Map<String, String> pathParams = route.bindPathParams(request);
     * <p>
     * // Then
     * assertThat(matches).isTrue();
     * assertThat(queryParams).isEmpty();
     * assertThat(pathParams).containsKeys("ID");
     * assertThat(pathParams.get("ID")).isEqualTo("KSFO");
     * }
     * <p>
     * // GET /Airports('KSFO')/Name/$value
     * @Test void requestIndividualPropertyRawValue() {
     * // Given
     * Route route = new Route(GET, "/Airports('{ID}')/Name/$value", testHandler);
     * HttpRequest request = createRequest(GET, "/Airports('KSFO')/Name/$value");
     * <p>
     * boolean matches = route.matches(request);
     * Map<String, List<String>> queryParams = route.queryParameters(request);
     * Map<String, String> pathParams = route.bindPathParams(request);
     * <p>
     * // Then
     * assertThat(matches).isTrue();
     * assertThat(queryParams).isEmpty();
     * assertThat(pathParams).containsKeys("ID");
     * assertThat(pathParams.get("ID")).isEqualTo("KSFO");
     * }
     * <p>
     * // GET /People?$filter=FirstName eq 'Scott'
     * @Test void requestWithFilterOnSimpleType() {
     * // Given
     * Route route = new Route(GET, "/People", testHandler);
     * HttpRequest request = createRequest(GET, "/People?$filter=FirstName eq 'Scott'");
     * <p>
     * // When
     * boolean matches = route.matches(request);
     * Map<String, List<String>> queryParams = route.queryParameters(request);
     * Map<String, String> pathParams = route.bindPathParams(request);
     * <p>
     * // Then
     * assertThat(matches).isTrue();
     * assertThat(queryParams).containsKeys("$filter");
     * assertThat(queryParams.get("$filter")).containsExactly("FirstName eq 'Scott'");
     * assertThat(pathParams).isEmpty();
     * }
     * <p>
     * // GET /Airports?$filter=contains(Location/Address, 'San Francisco')
     * @Test void requestWithFilterOnComplexType() {
     * // Given
     * Route route = new Route(GET, "/Airports", testHandler);
     * HttpRequest request = createRequest(GET, "/Airports?$filter=contains(Location/Address, 'San Francisco')");
     * <p>
     * // When
     * boolean matches = route.matches(request);
     * Map<String, List<String>> queryParams = route.queryParameters(request);
     * Map<String, String> pathParams = route.bindPathParams(request);
     * <p>
     * // Then
     * assertThat(matches).isTrue();
     * assertThat(queryParams).containsKeys("$filter");
     * assertThat(queryParams.get("$filter")).containsExactly("contains(Location/Address, 'San Francisco')");
     * assertThat(pathParams).isEmpty();
     * }
     * <p>
     * // GET /People?$filter=Gender eq Microsoft.OData.SampleService.Models.TripPin.PersonGender'Female'
     * @Test void requestWithFilterOnEnumProperties() {
     * // Given
     * Route route = new Route(GET, "/People", testHandler);
     * HttpRequest request = createRequest(GET, "/People?$filter=Gender eq Microsoft.OData.SampleService.Models.TripPin.PersonGender'Female'");
     * <p>
     * // When
     * boolean matches = route.matches(request);
     * Map<String, List<String>> queryParams = route.queryParameters(request);
     * Map<String, String> pathParams = route.bindPathParams(request);
     * <p>
     * // Then
     * assertThat(matches).isTrue();
     * assertThat(queryParams).containsKeys("$filter");
     * assertThat(queryParams.get("$filter")).containsExactly("Gender eq Microsoft.OData.SampleService.Models.TripPin.PersonGender'Female'");
     * assertThat(pathParams).isEmpty();
     * }
     * <p>
     * // GET /People?$expand=Trips($filter=Name eq 'Trip in US')
     * @Test void requestWithNestedFilterInExpand() {
     * // Given
     * Route route = new Route(GET, "/People", testHandler);
     * HttpRequest request = createRequest(GET, "/People?$expand=Trips($filter=Name eq 'Trip in US')");
     * <p>
     * // When
     * boolean matches = route.matches(request);
     * Map<String, List<String>> queryParams = route.queryParameters(request);
     * Map<String, String> pathParams = route.bindPathParams(request);
     * <p>
     * // Then
     * assertThat(matches).isTrue();
     * assertThat(queryParams).containsKeys("$expand");
     * assertThat(queryParams.get("$expand")).containsExactly("Trips($filter=Name eq 'Trip in US')");
     * assertThat(pathParams).isEmpty();
     * }
     * <p>
     * // GET /People('scottketchum')/Trips?$orderby=EndsAt desc
     * @Test void requestWithOrderBy() {
     * // Given
     * Route route = new Route(GET, "/People('{ID}')/Trips", testHandler);
     * HttpRequest request = createRequest(GET, "/People('scottketchum')/Trips?$orderby=EndsAt desc");
     * <p>
     * // When
     * boolean matches = route.matches(request);
     * Map<String, List<String>> queryParams = route.queryParameters(request);
     * Map<String, String> pathParams = route.bindPathParams(request);
     * <p>
     * // Then
     * assertThat(matches).isTrue();
     * assertThat(queryParams).containsKeys("$orderby");
     * assertThat(queryParams.get("$orderby")).containsExactly("EndsAt desc");
     * assertThat(pathParams).containsKeys("ID");
     * assertThat(pathParams.get("ID")).isEqualTo("scottketchum");
     * }
     * <p>
     * // GET /People?$top=2
     * @Test void requestWithTop() {
     * // Given
     * Route route = new Route(GET, "/People", testHandler);
     * HttpRequest request = createRequest(GET, "/People?$top=2");
     * <p>
     * // When
     * boolean matches = route.matches(request);
     * Map<String, List<String>> queryParams = route.queryParameters(request);
     * Map<String, String> pathParams = route.bindPathParams(request);
     * <p>
     * // Then
     * assertThat(matches).isTrue();
     * assertThat(queryParams).containsKeys("$top");
     * assertThat(queryParams.get("$top")).containsExactly("2");
     * assertThat(pathParams).isEmpty();
     * }
     * <p>
     * // GET /People?$skip=18
     * @Test void requestWithSkip() {
     * // Given
     * Route route = new Route(GET, "/People", testHandler);
     * HttpRequest request = createRequest(GET, "/People?$skip=18");
     * <p>
     * // When
     * boolean matches = route.matches(request);
     * Map<String, List<String>> queryParams = route.queryParameters(request);
     * Map<String, String> pathParams = route.bindPathParams(request);
     * <p>
     * // Then
     * assertThat(matches).isTrue();
     * assertThat(queryParams).containsKeys("$skip");
     * assertThat(queryParams.get("$skip")).containsExactly("18");
     * assertThat(pathParams).isEmpty();
     * }
     * <p>
     * // GET /People?$count=true
     * @Test void requestWithCount() {
     * // Given
     * Route route = new Route(GET, "/People", testHandler);
     * HttpRequest request = createRequest(GET, "/People?$count=true");
     * <p>
     * // When
     * boolean matches = route.matches(request);
     * Map<String, List<String>> queryParams = route.queryParameters(request);
     * Map<String, String> pathParams = route.bindPathParams(request);
     * <p>
     * // Then
     * assertThat(matches).isTrue();
     * assertThat(queryParams).containsKeys("$count");
     * assertThat(queryParams.get("$count")).containsExactly("true");
     * assertThat(pathParams).isEmpty();
     * }
     * <p>
     * // GET /People('keithpinckney')?$expand=Friends
     * @Test void requestWithExpand() {
     * // Given
     * Route route = new Route(GET, "/People('{Name}')", testHandler);
     * HttpRequest request = createRequest(GET, "/People('keithpinckney')?$expand=Friends");
     * <p>
     * // When
     * boolean matches = route.matches(request);
     * Map<String, List<String>> queryParams = route.queryParameters(request);
     * Map<String, String> pathParams = route.bindPathParams(request);
     * <p>
     * // Then
     * assertThat(matches).isTrue();
     * assertThat(queryParams).containsKeys("$expand");
     * assertThat(queryParams.get("$expand")).containsExactly("Friends");
     * assertThat(pathParams).containsKeys("Name");
     * assertThat(pathParams.get("Name")).isEqualTo("keithpinckney");
     * }
     * <p>
     * // GET /Airports?$select=Name, IcaoCode
     * @Test void requestWithSelect() {
     * // Given
     * Route route = new Route(GET, "/Airports", testHandler);
     * HttpRequest request = createRequest(GET, "/Airports?$select=Name, IcaoCode");
     * <p>
     * // When
     * boolean matches = route.matches(request);
     * Map<String, List<String>> queryParams = route.queryParameters(request);
     * Map<String, String> pathParams = route.bindPathParams(request);
     * <p>
     * // Then
     * assertThat(matches).isTrue();
     * assertThat(queryParams).containsKeys("$select");
     * assertThat(queryParams.get("$select")).containsExactly("Name, IcaoCode");
     * assertThat(pathParams).isEmpty();
     * }
     * <p>
     * // GET /People?$search=Boise
     * @Test void requestWithSearch() {
     * // Given
     * Route route = new Route(GET, "/People", testHandler);
     * HttpRequest request = createRequest(GET, "/People?$search=Boise");
     * <p>
     * // When
     * boolean matches = route.matches(request);
     * Map<String, List<String>> queryParams = route.queryParameters(request);
     * Map<String, String> pathParams = route.bindPathParams(request);
     * <p>
     * // Then
     * assertThat(matches).isTrue();
     * assertThat(queryParams).containsKeys("$search");
     * assertThat(queryParams.get("$search")).containsExactly("Boise");
     * assertThat(pathParams).isEmpty();
     * }
     * <p>
     * // GET /People?$filter=Emails/any(s:endswith(s, 'contoso.com'))
     * @Test void requestWithLambdaOperator1() {
     * // Given
     * Route route = new Route(GET, "/People", testHandler);
     * HttpRequest request = createRequest(GET, "/People?$filter=Emails/any(s:endswith(s, 'contoso.com'))");
     * <p>
     * // When
     * boolean matches = route.matches(request);
     * Map<String, List<String>> queryParams = route.queryParameters(request);
     * Map<String, String> pathParams = route.bindPathParams(request);
     * <p>
     * // Then
     * assertThat(matches).isTrue();
     * assertThat(queryParams).containsKeys("$filter");
     * assertThat(queryParams.get("$filter")).containsExactly("Emails/any(s:endswith(s, 'contoso.com'))");
     * assertThat(pathParams).isEmpty();
     * }
     * <p>
     * // GET /Me/Friends?$filter=Friends/any(f:f/FirstName eq 'Scott')
     * @Test void requestWithLambdaOperator2() {
     * // Given
     * Route route = new Route(GET, "/Me/Friends", testHandler);
     * HttpRequest request = createRequest(GET, "/Me/Friends?$filter=Friends/any(f:f/FirstName eq 'Scott'");
     * <p>
     * // When
     * boolean matches = route.matches(request);
     * Map<String, List<String>> queryParams = route.queryParameters(request);
     * Map<String, String> pathParams = route.bindPathParams(request);
     * <p>
     * // Then
     * assertThat(matches).isTrue();
     * assertThat(queryParams).containsKeys("$filter");
     * assertThat(queryParams.get("$filter")).containsExactly("Friends/any(f:f/FirstName eq 'Scott'");
     * assertThat(pathParams).isEmpty();
     * }
     * <p>
     * private HttpRequest createRequest(RestMethod method, String uri) {
     * return new DefaultHttpRequest(HTTP, HttpMethod.valueOf(method.name()), uri);
     * }
     */

}

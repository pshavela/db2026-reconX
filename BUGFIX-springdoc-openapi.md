# Bug: `/v3/api-docs` returns 500 (Swagger UI "Failed to load API definition")

## Symptom

Swagger UI loads at `/api/swagger-ui.html`, but shows:

```
Failed to load API definition.
Fetch error
response status is 500 /api/v3/api-docs
```

## Root cause

`backend/pom.xml` pins incompatible versions:

- `spring-boot-starter-parent` → `3.5.0` (Spring Framework 6.2.7)
- `springdoc-openapi-starter-webmvc-ui` → `2.6.0`

Spring Framework 6.2 removed the constructor `ControllerAdviceBean(Object)`.
springdoc-openapi 2.6.0 still calls it when scanning `@ControllerAdvice`
beans to build response schemas, so any request to `/v3/api-docs` throws:

```
java.lang.NoSuchMethodError: 'void org.springframework.web.method.ControllerAdviceBean.<init>(java.lang.Object)'
	at org.springdoc.core.service.GenericResponseService.lambda$getGenericMapResponse$8(GenericResponseService.java:706)
	...
```

**This is not a local/environment issue** — the version pin is committed in
`backend/pom.xml` on `main` (commit `edc78fc "Fixed initial backend errors,
backend should now work"`). Anyone who pulls `main` and boots the app hits
the same 500, regardless of machine. springdoc 2.6.0 only supports Spring
Boot up to ~3.3.x; it was never compatible with the 3.5.0 parent already in
the pom.

## Fix

Bump springdoc to a version built against Spring Framework 6.2 / Spring Boot 3.5:

```diff
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
- <version>2.6.0</version>
+ <version>2.8.9</version>
```

File: `backend/pom.xml`

## Verify

```powershell
cd backend
./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Then open `http://localhost:8080/api/swagger-ui.html` — API docs should load
without a fetch error.

## References

- https://github.com/springdoc/springdoc-openapi/issues/3041
- https://community.opengroup.org/osdu/platform/system/project-and-workflow/-/merge_requests/171

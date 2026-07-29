# Ticket ADV075

Assignee: alexandraelenadumitrescu
Status: Completed

## Problem
- MockMvc slice test proving an authenticated TRADER creating a trade gets 201 + Location + body fields

## Approach
- `@WebMvcTest(controllers = TradeController.class, excludeFilters = ...)` excluding `JwtAuthenticationFilter`
- `@MockBean` on `TradeService` and `TradeMapper`, `@WithMockUser(roles = "TRADER")`, `.with(csrf())`

## Notes
- Mocking `JwtAuthenticationFilter` directly (instead of excluding it) breaks the test: a Mockito mock of a
  `Filter` never calls `chain.doFilter(...)`, so every request gets silently swallowed before reaching the
  controller (`Handler: Type = null` in MockMvc's `print()` output, response 200 with an empty body)
- `@EnableJpaAuditing` was on `ReconxApplication` directly, which crashes any `@WebMvcTest` slice with
  "JPA metamodel must not be empty" (the slice has no `EntityManagerFactory`). Moved it to a new
  `config/JpaConfig.java` — a plain `@Configuration` class is excluded from slice scans, unlike the app's
  root `@SpringBootConfiguration` class
- `Trade` has no public setter/builder option for `id` (JPA `@GeneratedValue`); used
  `ReflectionTestUtils.setField(trade, "id", 42L)` on the mocked return value so the mapped response and
  `Location` header contain the expected id instead of `null`

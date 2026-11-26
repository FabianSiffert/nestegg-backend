# TODO

- [x] Controller-level tests for the asset endpoints, including error cases handled by `GlobalExceptionHandler`: blank name, malformed JSON, missing field, unknown ID
- [x] Logging: request logging, log levels, correlation ID per request
- [x] Catch-all exception handler returning `ProblemDetail` for unexpected errors (500); it must log the exception itself, since handled exceptions are no longer logged by Spring
- [ ] Production: consider structured JSON logging (`logging.structured.format.console`) for the server
- [ ] Consider rich domain models: move update logic into entities (e.g. `asset.update(request)`) instead of assigning fields in services
- [ ] Add a `NOTICE` file for the Apache 2.0 license
- [ ] Link inv (Pokémon card browser) to nestegg via a prefilled `/assets/new` URL handoff, see `docs/inv-integration.md`. Depends on holdings/transactions and the web client
- [ ] When adding Spring Security: enable CORS in the security filter chain (`http.cors { }`), otherwise preflight requests are rejected before reaching the MVC CORS config
- [ ] Production: add the web client's real origin to `nestegg.cors.allowed-origins` (e.g. via the `NESTEGG_CORS_ALLOWED_ORIGINS` environment variable)
- [ ] Frontend: disable submit buttons while a request is running and show a loading indicator (prevents duplicate submissions from double clicks)
- [ ] Revisit JPA relationships (`@ManyToOne`) once a cross-asset query needs asset data (activity feed, expenses overview, portfolio)

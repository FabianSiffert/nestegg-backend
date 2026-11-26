# TODO

- [ ] Controller-level tests for the asset endpoints, including error cases handled by `GlobalExceptionHandler`: blank name, malformed JSON, missing field, unknown ID
- [ ] Logging: request logging, log levels, correlation ID per request
- [ ] Catch-all exception handler returning `ProblemDetail` for unexpected errors (500); it must log the exception itself, since handled exceptions are no longer logged by Spring
- [ ] Consider rich domain models: move update logic into entities (e.g. `asset.update(request)`) instead of assigning fields in services
- [ ] Add a `NOTICE` file for the Apache 2.0 license
- [ ] Link inv (Pokémon card browser) to nestegg via a prefilled `/assets/new` URL handoff, see `docs/inv-integration.md`. Depends on holdings/transactions and the web client
- [ ] Revisit the future-date check for price points: it compares against today in UTC+14, which never rejects a valid date but accepts dates up to about one day ahead. If that becomes a problem, validate against the client's time zone instead
- [ ] Revisit JPA relationships (`@ManyToOne`) once a cross-asset query needs asset data (activity feed, expenses overview, portfolio)

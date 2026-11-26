# TODO

- [ ] Controller-level tests for the asset endpoints, including error cases handled by `GlobalExceptionHandler`: blank name, malformed JSON, missing field, unknown ID
- [ ] Logging: request logging, log levels, correlation ID per request
- [ ] Catch-all exception handler returning `ProblemDetail` for unexpected errors (500); it must log the exception itself, since handled exceptions are no longer logged by Spring
- [ ] Consider rich domain models: move update logic into entities (e.g. `asset.update(request)`) instead of assigning fields in services
- [ ] Add a `NOTICE` file for the Apache 2.0 license
- [ ] Revisit JPA relationships (`@ManyToOne`) once a cross-asset query needs asset data (activity feed, expenses overview, portfolio)

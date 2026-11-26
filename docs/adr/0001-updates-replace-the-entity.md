# 1. Updates replace the entity

Date: 2026-11-26

## Status

Accepted

## Context

A PUT copies the request fields onto an existing entity. Until now, each feature listed those fields twice: in `toEntity()` for creating, and as field-by-field assignments in the service's `update`. A field added to `toEntity()` but forgotten in `update` was silently ignored on PUT, and no test noticed. The planned external reference fields on Asset (`docs/inv-integration.md`) would have added four more chances for that mistake.

## Decision

`update` builds a fresh entity with `toEntity()`, passing the values that do not come from the request (`id`, `createdAt`, and `assetId` for prices), and stores it with `save()`. POST and PUT share one field list.

## Rejected alternatives

- **An update method on the entity** (e.g. `asset.update(name, type, currency, notes)`, the former "rich domain models" idea in `TODO.md`). The entity must not know API DTOs (`CLAUDE.md`), so the method would take plain values. The field list would then exist in three places: `toEntity()`, the method's parameters and its call.
- **An `applyTo(entity)` extension next to `toEntity()`.** Keeps both lists in one file, but they are still two lists. It makes the mistake less likely instead of impossible.

## Consequences

- A request field added to `toEntity()` works for POST and PUT alike.
- PUT is a full replace: an optional field the client omits becomes `null`.
- Values that do not come from the request must be carried over in `update`. The controller tests assert that `id` and `createdAt` survive a PUT.
- `save()` with an existing ID performs a merge, see "Replacing an entity with `save()` (merge)" in `docs/learnings.md`.

# Learnings

Notes on concepts and pitfalls encountered while building this backend.

## Spring: `@Transactional` on class vs. method

A `@Transactional` annotation on a class is a default for every method that has no annotation of its own. An annotation on a method replaces the class-level one entirely; it is not nested inside it.

```kotlin
@Service
@Transactional(readOnly = true)
class AssetService(...) {

	fun get(id: UUID): AssetResponse = ...

	@Transactional
	fun create(request: AssetRequest): AssetResponse = ...
}
```

- `get` runs in a read-only transaction, inherited from the class.
- `create` runs in a regular read-write transaction. Its own annotation overrides the class default.

Alternative: annotate every method individually and leave the class unannotated. More verbose, but each method states its behaviour directly. Both styles are common.

### What `readOnly = true` does

- Hibernate skips the flush at the end of the transaction, so no dirty checking and no snapshots of loaded entities. This saves work and memory.
- The database connection is marked read-only, so PostgreSQL rejects write statements.
- Main benefit: an accidental modification of an entity inside a read method is never written to the database.

## Hibernate: flush vs. commit

- **Modifying an entity** (`asset.name = "New"`) only changes the object in memory.
- **Flush** sends the pending SQL statements (`INSERT`, `UPDATE`, `DELETE`) to the database, but inside the open transaction. Other connections cannot see the changes yet.
- **Commit** makes the changes permanent and visible to everyone.
- **Rollback** discards everything, including statements that were already flushed.

Analogy: changing an entity is thinking of the text, flushing is typing it into an email draft, committing is pressing send, rolling back is deleting the draft.

With `@Transactional`, flush and commit happen automatically, one after the other, when the method returns. The difference only becomes visible in tests (e.g. `entityManager.flush()` in a `@DataJpaTest`, whose transaction is rolled back afterwards) or when an exception occurs between the two.

### Dirty checking

When Hibernate loads an entity, it keeps a snapshot of its state. On flush, it compares every managed entity with its snapshot and generates an `UPDATE` for each one that changed. That is why an update method can modify a loaded entity without ever calling `save`:

```kotlin
@Transactional
fun update(id: UUID, request: AssetRequest): AssetResponse {
	val asset = findAsset(id)
	asset.name = request.name
	return asset.toResponse()
}
```

No changes, no `UPDATE`.

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

## JPA: relationship (`@ManyToOne`) vs. plain foreign key ID

JPA (the standard) and Hibernate (its implementation) map classes to tables, objects to rows and fields to columns. In the database, a relationship is always just a column holding the ID of another row (`price_point.asset_id`). The question is only how that column looks in Kotlin. The database schema is identical in both variants.

**Plain ID**

```kotlin
@Column(name = "asset_id", nullable = false, updatable = false)
val assetId: UUID,
```

- `pricePoint.assetId` is just the ID. Loading the asset is an explicit call (`assetRepository.findById(...)`), as with Room.
- No lazy loading, no proxies, no N+1 queries.

**JPA relationship**

```kotlin
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "asset_id")
val asset: Asset,
```

- `pricePoint.asset.name` works directly; Hibernate loads the asset on demand.
- Enables navigation and joins in queries without hand-written SQL.
- `fetch = FetchType.LAZY` is essential: `@ManyToOne` defaults to eager loading.
- Lazy loading only works inside an open transaction; outside, accessing the proxy throws `LazyInitializationException`.
- Iterating over many entities and touching the relation triggers one extra query per entity (N+1 problem).
- Creating one requires an `Asset` instance; `assetRepository.getReferenceById(id)` returns a proxy without a `SELECT`.

**Decision for `PricePoint`:** plain ID. Prices are only ever queried per asset, and the asset ID is already known from the URL. A relationship pays off once a query spans several assets and needs their data (e.g. an activity feed with asset names). Switching later only touches Kotlin code, not the schema.

# Planned: linking inv (Pokémon card browser) to nestegg

Status: planned, not started. Agree on the URL contract before building.

## Context
- inv (inv.siffert.io) is a static Pokémon TCG card browser: React + Vite, no backend, daily snapshot of
  TCGdex data, wishlist in Firestore. It knows card identity (set, number, print, image) and reference
  prices from Cardmarket (EUR) and TCGplayer (USD).
- Those prices are unreliable. Example from the 2026-09-27 snapshot: Espeon δ (Delta Species #4) had a
  Cardmarket trend of €442.91 but a 7-day average of €46.18.
- Decision: nestegg stays the system of record for owned assets and price history, with prices entered
  by hand. inv does not get its own portfolio. inv only helps create card assets in nestegg, so the
  user doesn't have to type them in.

## Goal
Make adding a Pokémon card to nestegg one click from inv, with every field prefilled and the user
confirming before anything is saved.

## Proposed changes in nestegg, in order
1. Holdings / transactions (needed anyway). The README says position value = quantity × unit price, but
   no table stores quantity, purchase date, price paid or fees, so there is no cost basis or P&L yet.
   Design is open.
2. Generic external reference on `asset`, as a new Flyway migration (never edit V1):
   - `external_source` varchar, nullable (e.g. `inv`; later ISIN, ticker or coin id sources)
   - `external_id` varchar, nullable
   - `external_url` text, nullable
   - `image_url` text, nullable
   - Keep it generic. No Pokémon-specific columns. Condition, grade and language go into name or notes
     for now.
   - Add the fields to `AssetRequest` / `AssetResponse`.
3. Web client, when it exists: a `/assets/new` route that reads query parameters into a prefilled form.

## URL contract inv will call (open to change, agree before building)
`/assets/new?name=…&type=COLLECTIBLE&currency=EUR&externalSource=inv&externalId=…&externalUrl=…&imageUrl=…&price=…&priceDate=…`

- `name`: e.g. `Charizard ex · 151 #199`, plus ` · Reverse Holo` for a specific printing
- `externalId`: inv card id, e.g. `sv03.5-199`; for one printing `cardId#print`, e.g.
  `swsh4-25#Cracked Ice Holo`
- `externalUrl`: e.g. `https://inv.siffert.io/market?set=sv03.5&card=sv03.5-199` (holds the set id too)
- `imageUrl`: e.g. `https://assets.tcgdex.net/en/sv/sv03.5/199/high.webp`
- `currency`: EUR when inv has a Cardmarket price; USD when it only has TCGplayer
- `price`, `priceDate`: optional suggestion only

## Rules
- Never create a `price_point` from inv data automatically. A suggested price is prefilled, editable and
  saved only when the user confirms.
- No auth, CORS or API calls between the two projects for now. The URL handoff is the whole integration.

## Later, optional
- For assets with `external_source = inv`, show inv's current reference price next to manual price
  entry. inv's card data is public JSON at `https://inv.siffert.io/data/cards/<setId>.json`: an array of
  cards keyed by `id`, with `cardmarket.prices.trendPrice`, `avg7` and `avg30` (EUR), and
  `tcgplayer.prices.*.market` (USD). The set id comes from `externalUrl`.
- A one-time CSV import for a collection the user already owns.

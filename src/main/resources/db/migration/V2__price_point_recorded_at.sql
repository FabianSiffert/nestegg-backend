alter table price_point add column recorded_at timestamptz;

update price_point set recorded_at = date::timestamp at time zone 'UTC';

alter table price_point alter column recorded_at set not null;

alter table price_point drop column date;

create index price_point_asset_id_recorded_at_idx on price_point (asset_id, recorded_at);

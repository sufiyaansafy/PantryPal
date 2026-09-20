-- =====================================================================
--  SMART PANTRY MANAGER – DATABASE SCHEMA  (Supabase / PostgreSQL)
--  File 1 of 2. Run this FIRST, only once:
--  Supabase dashboard → SQL Editor → New query → paste this file → Run
--  Expected result: "Success. No rows returned"
-- =====================================================================

-- Need to start again? Remove the "--" in front of these 3 lines,
-- run them, then run this whole file again.
-- drop table if exists public.recipe_ingredients;
-- drop table if exists public.recipes;
-- drop table if exists public.pantry_items;


-- ---------------------------------------------------------------------
-- 1) PANTRY_ITEMS – the ingredients the user actually has at home.
--    The app can Create, Read, Update and Delete these rows.
-- ---------------------------------------------------------------------
create table public.pantry_items (
    id          bigint generated always as identity primary key,
    name        text          not null check (char_length(btrim(name)) between 2 and 40),
    quantity    numeric(10,2) not null check (quantity > 0),
    unit        text          not null check (unit in ('g','kg','ml','L','tsp','tbsp','cup','pcs')),
    expiry_date date,                                   -- optional (null = no expiry date)
    created_at  timestamptz   not null default now()
);

-- ---------------------------------------------------------------------
-- 2) RECIPES – the recipe library (filled by 02_seed_recipes.sql).
--    The app can only READ these rows.
-- ---------------------------------------------------------------------
create table public.recipes (
    id          bigint generated always as identity primary key,
    name        text not null unique,
    description text not null default '',
    minutes     int  not null default 15 check (minutes > 0),
    steps       text not null                           -- one step per line
);

-- ---------------------------------------------------------------------
-- 3) RECIPE_INGREDIENTS – what each recipe needs (one row per ingredient).
--    Relationship: one recipe ──< many recipe_ingredients (foreign key).
--    "on delete cascade": deleting a recipe also deletes its ingredient rows.
-- ---------------------------------------------------------------------
create table public.recipe_ingredients (
    id         bigint generated always as identity primary key,
    recipe_id  bigint        not null references public.recipes (id) on delete cascade,
    name       text          not null,
    quantity   numeric(10,2) not null check (quantity > 0),
    unit       text          not null check (unit in ('g','kg','ml','L','tsp','tbsp','cup','pcs'))
);

create index recipe_ingredients_recipe_id_idx on public.recipe_ingredients (recipe_id);


-- =====================================================================
--  ACCESS RULES
--  The app connects with the project's publishable ("anon") key, so every
--  request from the app runs as the database role called "anon".
--
--  Step A – GRANT: since May 2026, new Supabase projects no longer expose
--           new tables to the REST (Data) API automatically, so we
--           explicitly allow the anon role to use our tables.
--  Step B – ROW LEVEL SECURITY (RLS): switched on for every table, then
--           POLICIES say exactly what the anon role may do:
--           pantry = full CRUD, recipes = read only.
-- =====================================================================

-- Step A: table privileges (which tables the app may touch at all)
grant select, insert, update, delete on public.pantry_items       to anon;
grant select                         on public.recipes            to anon;
grant select                         on public.recipe_ingredients to anon;

-- Step B: row level security + policies (which rows / actions are allowed)
alter table public.pantry_items       enable row level security;
alter table public.recipes            enable row level security;
alter table public.recipe_ingredients enable row level security;

create policy "App can read pantry items"
    on public.pantry_items for select to anon using (true);
create policy "App can add pantry items"
    on public.pantry_items for insert to anon with check (true);
create policy "App can update pantry items"
    on public.pantry_items for update to anon using (true) with check (true);
create policy "App can delete pantry items"
    on public.pantry_items for delete to anon using (true);

create policy "App can read recipes"
    on public.recipes for select to anon using (true);
create policy "App can read recipe ingredients"
    on public.recipe_ingredients for select to anon using (true);

-- Note for your report: this app has no login, so every phone using it shares
-- one pantry. A production version would add Supabase Auth, a user_id column
-- and policies such as  using (auth.uid() = user_id)  so each user only sees
-- their own pantry.

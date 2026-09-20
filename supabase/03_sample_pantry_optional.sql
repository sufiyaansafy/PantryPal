-- =====================================================================
--  OPTIONAL – sample pantry items for testing (use from Day 2 onwards).
--  Run in the SQL Editor if you want items to appear in the app straight
--  away. You can edit or delete these rows later from inside the app.
-- =====================================================================
insert into public.pantry_items (name, quantity, unit, expiry_date) values
    ('Eggs',        6,   'pcs', current_date + 10),
    ('Milk',        1,   'L',   current_date + 2),
    ('Butter',      250, 'g',   current_date + 30),
    ('Mielie meal', 2.5, 'kg',  null),
    ('Salt',        500, 'g',   null),
    ('Tomato',      3,   'pcs', current_date + 1);

-- With exactly these items, the strict matcher (built on Day 3) finds:
--   can cook now:   Pap (Stiff Porridge), Scrambled Eggs
--   almost there:   Creamy Mashed Potatoes (only the potatoes are missing)

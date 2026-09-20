-- =====================================================================
--  SMART PANTRY MANAGER – RECIPE LIBRARY (20 recipes, 96 ingredient rows)
--  File 2 of 2. Run AFTER 01_schema.sql, only once:
--  SQL Editor → New query → paste this file → Run
--  The last query lists every recipe with its number of ingredients.
--
--  Units: dry food in g, liquids in ml or spoons (tsp/tbsp),
--         whole items in pcs. Water is not listed as an ingredient
--         (we assume everyone has tap water).
--
--  Make it yours: swap two or three recipes for your own family
--  favourites. Change the recipe row AND its ingredient rows, and keep
--  the recipe name spelled exactly the same in both places.
--
--  Need to reload the recipes? Run this line first, then this file again:
--  truncate public.recipes restart identity cascade;
-- =====================================================================

-- 1) The recipes -------------------------------------------------------
insert into public.recipes (name, description, minutes, steps) values
('Pap (Stiff Porridge)',
 'Classic stiff maize porridge – the base for sheba, chakalaka or mince.', 25,
 E'Bring 750 ml water and the salt to the boil in a pot.\nStir in about a third of the maize meal to make a thin porridge and cook for 5 minutes.\nAdd the rest of the maize meal and stir hard with a wooden spoon until stiff.\nCover and steam on low heat for 15–20 minutes, stirring once or twice.'),

('Tomato & Onion Sheba',
 'Sweet-and-savoury tomato relish, perfect with pap.', 20,
 E'Chop the onion and tomatoes.\nFry the onion in the oil until soft and golden.\nAdd the tomatoes, sugar and salt.\nSimmer for 10 minutes, stirring now and then, until thick.'),

('Chakalaka',
 'Spicy township-style vegetable relish with baked beans.', 25,
 E'Chop the onion and green pepper and grate the carrots.\nFry the onion in the oil for 3 minutes, then stir in the curry powder.\nAdd the carrots and green pepper and cook for 5 minutes.\nStir in the baked beans and simmer for 10 minutes. Serve hot or cold.'),

('Morogo-style Spinach',
 'Quick braised greens in the style of traditional morogo.', 15,
 E'Wash and shred the spinach, and chop the onion and tomato.\nFry the onion in the oil until soft.\nAdd the tomato and cook for 2 minutes.\nAdd the spinach and salt, cover and steam for 5 minutes until wilted.'),

('Vetkoek',
 'Golden fried dough balls – fill them with mince, cheese or jam.', 90,
 E'Mix the flour, yeast, sugar and salt in a large bowl.\nAdd about 300 ml lukewarm water and knead into a soft dough.\nCover and leave in a warm place for 1 hour until doubled in size.\nHeat the oil in a deep pot and fry small balls of dough until golden on both sides.\nDrain on paper towel and serve warm.'),

('Savoury Mince',
 'Everyday beef mince with vegetables – great on pap, rice or vetkoek.', 35,
 E'Chop the onion and tomatoes and grate the carrot.\nBrown the mince in the oil, breaking it up with a spoon.\nAdd the onion and cook for 3 minutes.\nAdd the tomatoes, carrot and salt, cover and simmer for 20 minutes.'),

('Scrambled Eggs',
 'Soft, creamy eggs ready in minutes.', 10,
 E'Whisk the eggs and milk together.\nMelt the butter in a pan over low heat.\nPour in the eggs and stir gently until just set.\nServe straight away.'),

('Cheese & Onion Omelette',
 'A filling omelette that uses up the last bit of cheese.', 10,
 E'Finely chop the onion and grate the cheese.\nWhisk the eggs well.\nMelt the butter in a pan and fry the onion for 2 minutes.\nPour in the eggs, cook until almost set, sprinkle over the cheese and fold in half.'),

('French Toast',
 'Turns stale bread into a sweet breakfast.', 15,
 E'Whisk the eggs, milk and sugar in a flat dish.\nDip each slice of bread into the mixture on both sides.\nMelt the butter in a pan and fry the slices until golden on both sides.'),

('Cheese & Tomato Toastie',
 'The classic toasted sandwich.', 10,
 E'Slice the tomato and the cheese.\nButter the outside of both slices of bread.\nFill with cheese and tomato and toast in a pan or sandwich maker until golden.'),

('South African Pancakes',
 'Thin pancakes, traditionally rolled up with cinnamon sugar.', 30,
 E'Whisk the flour, eggs, milk and salt into a smooth, runny batter.\nLet the batter rest for 10 minutes.\nHeat a little oil in a pan and pour in a thin layer of batter.\nCook until the edges lift, flip and cook for 30 seconds more. Repeat with the rest.'),

('Oats Porridge',
 'A warm, filling breakfast for cold mornings.', 10,
 E'Put the oats and milk in a small pot.\nBring to a simmer, stirring, and cook for 5 minutes until thick.\nStir in the sugar and serve.'),

('Banana Bread',
 'The best way to rescue over-ripe bananas.', 70,
 E'Heat the oven to 180 °C and grease a loaf tin.\nMash the bananas and mix in the melted butter, sugar and eggs.\nFold in the flour and baking powder until just combined.\nPour into the tin and bake for 50–60 minutes until a skewer comes out clean.'),

('Mealie Bread',
 'Moist, sweet South African corn bread.', 60,
 E'Heat the oven to 180 °C and grease a loaf tin.\nMix the creamed sweetcorn and eggs in a bowl.\nStir in the flour, baking powder, sugar and salt.\nPour into the tin and bake for about 45 minutes until golden.'),

('Egg Fried Rice',
 'Uses up leftover rice – even better with day-old rice.', 25,
 E'Cook the rice, drain it and let it cool (or use leftover rice).\nFry the chopped onion in the oil for 2 minutes.\nPush the onion aside, scramble the eggs in the pan, then mix everything together.\nAdd the rice, peas and soy sauce and stir-fry for 5 minutes.'),

('Tomato & Garlic Pasta',
 'Simple pasta with a fresh tomato sauce.', 25,
 E'Cook the pasta in boiling water until tender, then drain.\nFry the chopped onion and garlic in the oil until soft.\nAdd the chopped tomatoes and salt and simmer for 10 minutes.\nToss the pasta with the sauce and serve.'),

('Cheesy Macaroni',
 'Creamy macaroni and cheese made from scratch.', 35,
 E'Cook the macaroni until tender and drain.\nMelt the butter, stir in the flour and cook for 1 minute.\nSlowly whisk in the milk and stir until the sauce thickens.\nStir in most of the grated cheese, then the macaroni.\nTop with the remaining cheese and grill until golden.'),

('Potato Wedges',
 'Crispy oven-baked wedges with paprika.', 40,
 E'Heat the oven to 200 °C.\nCut the potatoes into wedges and toss with the oil, salt and paprika.\nSpread on a baking tray and bake for 30–35 minutes, turning once.'),

('Creamy Mashed Potatoes',
 'Smooth mash to go with any stew or mince.', 30,
 E'Peel and cut the potatoes and boil for 15–20 minutes until soft.\nDrain well.\nMash with the butter, milk and salt until smooth.'),

('Red Lentil Curry',
 'A budget-friendly curry packed with protein.', 40,
 E'Rinse the lentils.\nFry the chopped onion and garlic in the oil, then stir in the curry powder.\nAdd the chopped tomatoes, the lentils and 500 ml water.\nSimmer for about 25 minutes, stirring often, until thick and soft.');


-- 2) The ingredients, linked to each recipe by looking up its name -----
insert into public.recipe_ingredients (recipe_id, name, quantity, unit)
select r.id, v.ingredient, v.quantity, v.unit
from (values
    -- recipe name                  ingredient            qty  unit
    ('Pap (Stiff Porridge)',        'maize meal',         250, 'g'),
    ('Pap (Stiff Porridge)',        'salt',                 5, 'g'),

    ('Tomato & Onion Sheba',        'tomatoes',             3, 'pcs'),
    ('Tomato & Onion Sheba',        'onion',                1, 'pcs'),
    ('Tomato & Onion Sheba',        'cooking oil',          1, 'tbsp'),
    ('Tomato & Onion Sheba',        'sugar',                5, 'g'),
    ('Tomato & Onion Sheba',        'salt',                 2, 'g'),

    ('Chakalaka',                   'onion',                1, 'pcs'),
    ('Chakalaka',                   'carrots',              2, 'pcs'),
    ('Chakalaka',                   'green pepper',         1, 'pcs'),
    ('Chakalaka',                   'baked beans',        410, 'g'),
    ('Chakalaka',                   'cooking oil',          2, 'tbsp'),
    ('Chakalaka',                   'curry powder',         5, 'g'),

    ('Morogo-style Spinach',        'spinach',            250, 'g'),
    ('Morogo-style Spinach',        'onion',                1, 'pcs'),
    ('Morogo-style Spinach',        'tomato',               1, 'pcs'),
    ('Morogo-style Spinach',        'cooking oil',          1, 'tbsp'),
    ('Morogo-style Spinach',        'salt',                 2, 'g'),

    ('Vetkoek',                     'cake flour',         500, 'g'),
    ('Vetkoek',                     'instant yeast',       10, 'g'),
    ('Vetkoek',                     'sugar',               15, 'g'),
    ('Vetkoek',                     'salt',                 5, 'g'),
    ('Vetkoek',                     'cooking oil',        500, 'ml'),

    ('Savoury Mince',               'beef mince',         500, 'g'),
    ('Savoury Mince',               'onion',                1, 'pcs'),
    ('Savoury Mince',               'tomatoes',             2, 'pcs'),
    ('Savoury Mince',               'carrot',               1, 'pcs'),
    ('Savoury Mince',               'cooking oil',          1, 'tbsp'),
    ('Savoury Mince',               'salt',                 5, 'g'),

    ('Scrambled Eggs',              'eggs',                 3, 'pcs'),
    ('Scrambled Eggs',              'milk',                50, 'ml'),
    ('Scrambled Eggs',              'butter',              10, 'g'),

    ('Cheese & Onion Omelette',     'eggs',                 3, 'pcs'),
    ('Cheese & Onion Omelette',     'cheese',              40, 'g'),
    ('Cheese & Onion Omelette',     'onion',                1, 'pcs'),
    ('Cheese & Onion Omelette',     'butter',              10, 'g'),

    ('French Toast',                'bread slices',         4, 'pcs'),
    ('French Toast',                'eggs',                 2, 'pcs'),
    ('French Toast',                'milk',               100, 'ml'),
    ('French Toast',                'sugar',               10, 'g'),
    ('French Toast',                'butter',              20, 'g'),

    ('Cheese & Tomato Toastie',     'bread slices',         2, 'pcs'),
    ('Cheese & Tomato Toastie',     'cheese',              60, 'g'),
    ('Cheese & Tomato Toastie',     'tomato',               1, 'pcs'),
    ('Cheese & Tomato Toastie',     'butter',              10, 'g'),

    ('South African Pancakes',      'cake flour',         250, 'g'),
    ('South African Pancakes',      'eggs',                 2, 'pcs'),
    ('South African Pancakes',      'milk',               500, 'ml'),
    ('South African Pancakes',      'cooking oil',          2, 'tbsp'),
    ('South African Pancakes',      'salt',                 2, 'g'),

    ('Oats Porridge',               'oats',                80, 'g'),
    ('Oats Porridge',               'milk',               250, 'ml'),
    ('Oats Porridge',               'sugar',               10, 'g'),

    ('Banana Bread',                'bananas',              3, 'pcs'),
    ('Banana Bread',                'cake flour',         250, 'g'),
    ('Banana Bread',                'sugar',              150, 'g'),
    ('Banana Bread',                'eggs',                 2, 'pcs'),
    ('Banana Bread',                'butter',             100, 'g'),
    ('Banana Bread',                'baking powder',       10, 'g'),

    ('Mealie Bread',                'creamed sweetcorn',  410, 'g'),
    ('Mealie Bread',                'cake flour',         250, 'g'),
    ('Mealie Bread',                'baking powder',       10, 'g'),
    ('Mealie Bread',                'eggs',                 2, 'pcs'),
    ('Mealie Bread',                'sugar',               50, 'g'),
    ('Mealie Bread',                'salt',                 2, 'g'),

    ('Egg Fried Rice',              'rice',               200, 'g'),
    ('Egg Fried Rice',              'eggs',                 2, 'pcs'),
    ('Egg Fried Rice',              'onion',                1, 'pcs'),
    ('Egg Fried Rice',              'frozen peas',        100, 'g'),
    ('Egg Fried Rice',              'soy sauce',            2, 'tbsp'),
    ('Egg Fried Rice',              'cooking oil',          1, 'tbsp'),

    ('Tomato & Garlic Pasta',       'pasta',              250, 'g'),
    ('Tomato & Garlic Pasta',       'tomatoes',             4, 'pcs'),
    ('Tomato & Garlic Pasta',       'onion',                1, 'pcs'),
    ('Tomato & Garlic Pasta',       'garlic cloves',        2, 'pcs'),
    ('Tomato & Garlic Pasta',       'cooking oil',          2, 'tbsp'),
    ('Tomato & Garlic Pasta',       'salt',                 5, 'g'),

    ('Cheesy Macaroni',             'macaroni',           250, 'g'),
    ('Cheesy Macaroni',             'cheese',             150, 'g'),
    ('Cheesy Macaroni',             'milk',               500, 'ml'),
    ('Cheesy Macaroni',             'butter',              30, 'g'),
    ('Cheesy Macaroni',             'cake flour',          30, 'g'),

    ('Potato Wedges',               'potatoes',             4, 'pcs'),
    ('Potato Wedges',               'cooking oil',          3, 'tbsp'),
    ('Potato Wedges',               'salt',                 5, 'g'),
    ('Potato Wedges',               'paprika',              5, 'g'),

    ('Creamy Mashed Potatoes',      'potatoes',             5, 'pcs'),
    ('Creamy Mashed Potatoes',      'butter',              40, 'g'),
    ('Creamy Mashed Potatoes',      'milk',               100, 'ml'),
    ('Creamy Mashed Potatoes',      'salt',                 5, 'g'),

    ('Red Lentil Curry',            'red lentils',        200, 'g'),
    ('Red Lentil Curry',            'onion',                1, 'pcs'),
    ('Red Lentil Curry',            'tomatoes',             2, 'pcs'),
    ('Red Lentil Curry',            'garlic cloves',        2, 'pcs'),
    ('Red Lentil Curry',            'curry powder',        10, 'g'),
    ('Red Lentil Curry',            'cooking oil',          2, 'tbsp')
) as v (recipe, ingredient, quantity, unit)
join public.recipes r on r.name = v.recipe;


-- 3) Check: 20 rows, every recipe with 2–6 ingredients (96 in total) ---
select r.name as recipe, count(i.id) as ingredients
from public.recipes r
left join public.recipe_ingredients i on i.recipe_id = r.id
group by r.name
order by r.name;

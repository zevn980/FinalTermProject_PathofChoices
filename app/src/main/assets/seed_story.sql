-- Clear existing data
DELETE FROM choices;
DELETE FROM dialogs;

-- SEGMENT 1: THE SUMMONING
INSERT INTO dialogs (id, text) VALUES (1, 'The silver rain falls softly as you arrive at the Grand Hall of Echoes. Lord Viren stands beneath the statue of the First Arbiter.

"Traveler," he says, voice deep as thunder in a lake, "the Aetherial Scale has faltered. The prophecy has begun to stir."

He shows you a fragment of an ancient tablet:

"When the last lie is drowned, the scale shall tip. Four trials, one truth, and a price no nation can bear."');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
(1, 'Swear your aid to Viren and help investigate the prophecy.', 11),
(1, 'Slip away to the coast and seek the source of the water''s unrest.', 12),
(1, 'Attend Lady Selene''s public trial, hoping to read her intentions.', 13),
(1, 'Follow rumors of the Maskbearer in the eastern district.', 14),
(1, 'Seek the Lustrines directly and ask them about the prophecy.', 15);

-- SEGMENT 2: THE UNRAVELING

-- 2A: The Arbiter's Scroll
INSERT INTO dialogs (id, text) VALUES (11, 'You and Viren study ancient codes in the vaults below the Grand Hall, amid scrolls lined with forgotten laws.

"The Aetherial Court erased what came before. But this..." he gestures to the tablet fragment, "This is no law of ours. It''s older."

You trace the lines etched into obsidian. Some glyphs shimmer only when touched.

"These symbols... they echo Celestial script. But this line—it''s broken."

Viren grimly nods: "Yes. The prophecy was fragmented during the Founding Judgment. The founders feared what it foretold."');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
(11, 'Confront the Scale itself by requesting an Audience of Judgment.', 111),
(11, 'Seek the Lustrines for their memory of Caelondria before the courts.', 112),
(11, 'Attempt to modify the prophecy secretly.', 113);

-- 2B: Depths of Memory
INSERT INTO dialogs (id, text) VALUES (12, 'As you dive, water cradles you like silk. A harmonic resonance begins to pulse.

A shape—not of this time—emerges from the depths.

"Child of surface," the Lustrine Apparition speaks with a voice layered like many tides, "why return to the womb of truth?"

"This vision... it''s a warning. Caelondria drowning, but not from flood. From silence."

"Memory denied becomes rot. Justice performed becomes farce. The Aetherial Scale sings only its own name."');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
(12, 'Dive deeper, risking your breath, to find the source.', 121),
(12, 'Capture and analyze the harmonic energy in the water.', 122),
(12, 'Record and release the vision to the public.', 123);

-- 2C: Curtain of Smoke
INSERT INTO dialogs (id, text) VALUES (13, 'The marble amphitheater thrums with expectation. Lady Selene, draped in liquid silk, performs the accused''s soliloquy.

"O Caelondria," she declares dramatically, "do you judge me for truth or for spectacle?"

The crowd roars approval. But her eyes find yours—piercing, desperate.

"You know what this is," she whispers under her breath. "Don''t let them win."');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
(13, 'Publicly accuse her of hiding the prophecy.', 131),
(13, 'Secretly visit her backstage.', 132),
(13, 'Disrupt the trial with a false confession.', 133);

-- 2D: Mask of Iron
INSERT INTO dialogs (id, text) VALUES (14, 'In the veiled alleyways of the Eastern District, steam rises from grates like breath from a sleeping beast.

The Maskbearer steps from the dark, a cloak of woven mist around them.

"Traveler. The Scale judges, but never listens. You can change that. But not without dirtied hands."

"You''ve manipulated events. Selene, Elira, even Viren."

The Maskbearer tilts their head: "All puppets. As are we. The difference is—I see the strings."');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
(14, 'Accept their help to sabotage the Scale.', 141),
(14, 'Pretend to ally with them, but report to Elira.', 142),
(14, 'Convince the Maskbearer to delay action until you learn more.', 143);

-- 2E: Song of the Depths
INSERT INTO dialogs (id, text) VALUES (15, 'The journey through rain-fed caverns takes days. When you finally reach the submerged sanctum, the Lustrines rise from the water like memories made flesh.

"Ah," one sings, "a ripple from the surface seeks truth. But truth is a tide, not a stone."

You''re led to a pool of living memory. Visions swirl: Past cities. Past versions of Viren and Selene. Past you.

"A cycle," the Elder explains. "The same souls reshaping new masks. Break it—and we break ourselves."');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
(15, 'Ask how to break the cycle.', 151),
(15, 'Ask to join their memory stream.', 152),
(15, 'Steal a relic of memory and flee.', 153);

-- SEGMENT 3: VERDICT'S EDGE

-- 3A: Judgment Echo
INSERT INTO dialogs (id, text) VALUES (111, 'The Sanctum of Judgment. An immense, cathedral-like chamber where the Aetherial Scale hovers, suspended by streams of hydro-crystal energy.

"This is no court," Viren whispers, "This is the echo of gods who no longer speak."

The Scale''s voice resonates, mechanical yet alive: "Subject: Undefined. Role: Interfering variable. Judgment: To be Determined."

Suddenly, a light flares—the Scale begins weighing you.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
(111, 'Challenge the Scale''s authority.', 301),
(111, 'Submit to judgment, but with defiance.', 302);

-- Continue with more dialog entries and choices...
-- 3B through 4F would follow the same pattern

-- ENDINGS
INSERT INTO dialogs (id, text) VALUES 
(501, 'Ending 1: The Sovereign of Rain

Caelondria awakens beneath a sky no longer ruled by prophecy, but by understanding. The Court of the Rainlight, led by you, becomes a living tradition. Wandering judges roam from city to desert, offering empathy in place of edict.

The rain still falls. But it nourishes, not judges.'),

(502, 'Ending 2: The Hidden Verse

Caelondria thrives under a fragile peace. The courts continue their performances. Only you and Lady Selene remember the truth behind the curtain.

"If I''d spoken it aloud," she whispers, "I would''ve destroyed everything. Some truths must sing in silence."'),

(503, 'Ending 3: Judgment Broken

The Aetherial Scale shatters with a sound like thunder underwater. Panic consumes Caelondria. You survive—barely, fleeing through silver-flooded streets.

The world watches Caelondria collapse, and wonders if it too has lied to itself for too long.'),

(504, 'Ending 4: Floodlight Reborn

The city does not expand—but it heals. You and your companions lead a council of rotating citizens. Cases are judged by those with the clearest hearts, not the highest rank.

And when it rains, the people look up—not in fear, but in welcome.

Justice is no longer divine. It is human. And that is enough.');
--SEGMENT 1 – THE SUMMONING
INSERT INTO dialogs (id, text) VALUES (1,
'The silver rain falls softly as you arrive at the Grand Hall of Echoes.\n\n"Traveler," says Lord Viren, "the Aetherial Scale has faltered. The prophecy has begun to stir."\n\nHe shows you a fragment of an ancient tablet:\n"When the last lie is drowned, the scale shall tip. Four trials, one truth, and a price no nation can bear."');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
(1, 'Swear your aid to Viren and help investigate the prophecy.', 11),
(1, 'Slip away to the coast and seek the source of the water''s unrest.', 12),
(1, 'Attend Lady Selene''s public trial, hoping to read her intentions.', 13),
(1, 'Follow rumors of the Maskbearer in the eastern district.', 14),
(1, 'Seek the Lustrines directly and ask them about the prophecy.', 15);

--SEGMENT 2 – THE UNRAVELING

  -- 2A: Arbiter's Scroll
  INSERT INTO dialogs (id, text) VALUES (11, 'You and Viren study ancient codes. The prophecy wasn’t part of Caelondria''s divine law—it predates it.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (11, 'Confront the Scale itself by requesting an Audience of Judgment.', 111),
  (11, 'Seek the Lustrines for their memory of Caelondria before the courts.', 112),
  (11, 'Attempt to modify the prophecy secretly.', 113);

  -- 2B: Depths of Memory
  INSERT INTO dialogs (id, text) VALUES (12, 'The coastal waters glow blue and hum with voices. A vision seizes you: the nation drowning in silence.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (12, 'Dive deeper, risking your breath, to find the source.', 121),
  (12, 'Capture and analyze the harmonic energy in the water.', 122),
  (12, 'Record and release the vision to the public.', 123);

  -- 2C: Curtain of Smoke
  INSERT INTO dialogs (id, text) VALUES (13, 'Lady Selene dazzles the crowd, but her act falters when she sees you. Her eyes beg you to play along.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (13, 'Publicly accuse her of hiding the prophecy.', 131),
  (13, 'Secretly visit her backstage.', 132),
  (13, 'Disrupt the trial with a false confession.', 133);

  -- 2D: Mask of Iron
  INSERT INTO dialogs (id, text) VALUES (14, 'The Maskbearer speaks to you from shadows. "Do you want truth, or change?"');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (14, 'Accept their help to sabotage the Scale.', 141),
  (14, 'Pretend to ally with them, but report to Elira.', 142),
  (14, 'Convince the Maskbearer to delay action until you learn more.', 143);

  -- 2E: Song of the Depths
  INSERT INTO dialogs (id, text) VALUES (15, 'The Lustrines reveal the Prophecy is a repeating cycle—Caelondria has been reborn many times.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (15, 'Ask how to break the cycle.', 151),
  (15, 'Ask to join their memorystream.', 152),
  (15, 'Steal a relic of memory and flee.', 153);

  --SEGMENT 3 – VERDICT’S EDGE

  -- 3E: Collapse in the Crowd
  INSERT INTO dialogs (id, text) VALUES (301, 'Public panic grows. The court turns on itself. The Maskbearer declares the prophecy fulfilled.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (301, 'Take control and calm the people.', 401),
  (301, 'Let the chaos run its course.', 402);

  -- 3F: The Memory Spiral
  INSERT INTO dialogs (id, text) VALUES (302, 'You enter the Lustrines'' shared past and experience all four previous collapses.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (302, 'Rewrite the current cycle.', 403),
  (302, 'Anchor yourself in one of the past realities.', 404);

  --SEGMENT 4 – FLOODGATES

  -- 4E: Tides of Choice
  INSERT INTO dialogs (id, text) VALUES (401, 'You create a court of wandering judges. The Aetherial Scale is kept dormant.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (401, 'Instill this court across the world.', 501),
  (401, 'Keep it local, guarding Caelondria.', 502);

  -- 4F: Echoes Reclaimed
  INSERT INTO dialogs (id, text) VALUES (402, 'You now walk between past and present. Reality trembles.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (402, 'Let the worlds merge into one.', 503),
  (402, 'Sever the link and accept your own rewritten truth.', 504);

  --SEGMENT 5 – FINALE (Endings)

  --INSERT INTO dialogs (id, text) VALUES
    (501, 'Ending 1: The Sovereign of Rain\n\nViren and the Traveler rewrite Caelondria''s laws. Justice becomes empathy. The Scale is replaced by living judgment. The city thrives.'),
    (502, 'Ending 2: The Hidden Verse\n\nThe prophecy is never revealed. Lady Selene and you protect it in silence. Caelondria survives in illusion, but peace holds.'),
    (503, 'Ending 3: Judgment Broken\n\nThe Scale shatters. The city falls into chaos. You escape, barely, a witness to hubris undone.'),
    (504, 'Ending 4: Floodlight Reborn\n\nYou and Elira (or her legacy) lead a new court made by the people. Caelondria rises from below, reborn in clarity and courage.');

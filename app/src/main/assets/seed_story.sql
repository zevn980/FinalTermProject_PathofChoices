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
  INSERT INTO dialogs (id, text) VALUES (11, 'You and Viren study ancient codes. The prophecy wasn''t part of Caelondria''s divine law—it predates it.');
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
  (15, 'Ask to join their memory stream.', 152),
  (15, 'Steal a relic of memory and flee.', 153);

--SEGMENT 3 – THE DEEPER MYSTERIES (Missing dialogs added)

  -- 3A1: Audience of Judgment
  INSERT INTO dialogs (id, text) VALUES (111, 'The Aetherial Scale responds to your challenge. Its voice echoes: "You seek to judge the judge?"');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (111, 'Demand the Scale justify its prophecy.', 301),
  (111, 'Ask the Scale to show you the truth.', 302);

  -- 3A2: Lustrine Memory
  INSERT INTO dialogs (id, text) VALUES (112, 'The Lustrines show you visions of ancient Caelondria—a time before the Scale''s dominion.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (112, 'Learn the original laws of the land.', 301),
  (112, 'Witness the Scale''s first judgment.', 302);

  -- 3A3: Secret Modification
  INSERT INTO dialogs (id, text) VALUES (113, 'You attempt to alter the prophecy in secret, but the words resist your touch, burning with ancient power.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (113, 'Force the change despite the pain.', 301),
  (113, 'Retreat and seek another way.', 302);

  -- 3B1: Diving Deep
  INSERT INTO dialogs (id, text) VALUES (121, 'Beneath the waves, you find a sunken temple where the first Lustrines once dwelt.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (121, 'Enter the temple.', 301),
  (121, 'Surface and bring others.', 302);

  -- 3B2: Harmonic Analysis
  INSERT INTO dialogs (id, text) VALUES (122, 'The energy reveals a pattern—the prophecy pulses in rhythm with the city''s heartbeat.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (122, 'Disrupt the rhythm.', 301),
  (122, 'Follow the pattern to its source.', 302);

  -- 3B3: Public Revelation
  INSERT INTO dialogs (id, text) VALUES (123, 'Your vision spreads through the city like wildfire. The people demand answers from the courts.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (123, 'Lead the uprising.', 301),
  (123, 'Hide and observe the chaos.', 302);

  -- 3C1: Public Accusation
  INSERT INTO dialogs (id, text) VALUES (131, 'Your accusation shocks the crowd. Lady Selene''s mask of composure finally cracks.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (131, 'Press your advantage.', 301),
  (131, 'Offer her a chance to explain.', 302);

  -- 3C2: Backstage Secrets
  INSERT INTO dialogs (id, text) VALUES (132, 'Backstage, Selene reveals she''s been protecting the city from the prophecy''s true meaning.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (132, 'Join her in the deception.', 301),
  (132, 'Convince her to reveal the truth.', 302);

  -- 3C3: False Confession
  INSERT INTO dialogs (id, text) VALUES (133, 'Your false confession creates chaos. The real trial is forgotten as the crowd turns on you.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (133, 'Escape in the confusion.', 301),
  (133, 'Stand your ground and speak truth.', 302);

  -- 3D1: Alliance with Maskbearer
  INSERT INTO dialogs (id, text) VALUES (141, 'The Maskbearer shows you the Scale''s hidden weakness—it fears the people''s judgment.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (141, 'Help them expose the Scale.', 301),
  (141, 'Warn the Scale of the plot.', 302);

  -- 3D2: Double Agent
  INSERT INTO dialogs (id, text) VALUES (142, 'Elira trusts your report, but you sense the Maskbearer expected your betrayal.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (142, 'Prepare for the Maskbearer''s counter-move.', 301),
  (142, 'Try to genuinely ally with Elira.', 302);

  -- 3D3: Delay and Learn
  INSERT INTO dialogs (id, text) VALUES (143, 'The Maskbearer agrees to wait, but warns: "Time serves the prophecy, not us."');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (143, 'Use the time to gather allies.', 301),
  (143, 'Seek the prophecy''s origin.', 302);

  -- 3E1: Breaking the Cycle
  INSERT INTO dialogs (id, text) VALUES (151, 'The Lustrines speak of a price—someone must remember all the cycles to break them.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (151, 'Volunteer to bear the memory.', 301),
  (151, 'Find another way.', 302);

  -- 3E2: Joining the Stream
  INSERT INTO dialogs (id, text) VALUES (152, 'In the memorystream, you experience the joy and sorrow of all who came before.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (152, 'Merge completely with the stream.', 301),
  (152, 'Maintain your individual identity.', 302);

  -- 3E3: Stealing Memory
  INSERT INTO dialogs (id, text) VALUES (153, 'The stolen relic burns with ancient knowledge, but the Lustrines'' cries follow you.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (153, 'Use the relic''s power immediately.', 301),
  (153, 'Return and seek forgiveness.', 302);

  --SEGMENT 3 – VERDICT'S EDGE

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

  -- 4G: Cycle Rewriter
  INSERT INTO dialogs (id, text) VALUES (403, 'With the power to rewrite history, you feel the weight of all possible futures.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (403, 'Create a perfect world.', 501),
  (403, 'Preserve the imperfect truth.', 502);

  -- 4H: Past Anchor
  INSERT INTO dialogs (id, text) VALUES (404, 'You choose to live in one of the past cycles, but the present still calls to you.');
  INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES
  (404, 'Return to your original time.', 503),
  (404, 'Stay in the chosen past.', 504);

  --SEGMENT 5 – FINALE (Endings)

  INSERT INTO dialogs (id, text) VALUES
    (501, 'Ending 1: The Sovereign of Rain\n\nViren and the Traveler rewrite Caelondria''s laws. Justice becomes empathy. The Scale is replaced by living judgment. The city thrives.'),
    (502, 'Ending 2: The Hidden Verse\n\nThe prophecy is never revealed. Lady Selene and you protect it in silence. Caelondria survives in illusion, but peace holds.'),
    (503, 'Ending 3: Judgment Broken\n\nThe Scale shatters. The city falls into chaos. You escape, barely, a witness to hubris undone.'),
    (504, 'Ending 4: Floodlight Reborn\n\nYou and Elira (or her legacy) lead a new court made by the people. Caelondria rises from below, reborn in clarity and courage.');
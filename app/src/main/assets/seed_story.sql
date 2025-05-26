-- SEGMENT 1 – THE SUMMONING
INSERT INTO dialogs (id, text) VALUES (1, 'The silver rain falls softly as you arrive at the Grand Hall of Echoes.

**Lord Viren** emerges from the shadows, his weathered face etched with concern.

"Traveler," he says, his voice heavy with urgency, "the Aetherial Scale has faltered. The prophecy has begun to stir."

He reveals a fragment of an ancient tablet, its surface pulsing with ethereal light:

*"When the last lie is drowned, the scale shall tip. Four trials, one truth, and a price no nation can bear."*

The weight of destiny settles upon your shoulders. What path will you choose?');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (1, 'Swear your aid to Viren and help investigate the prophecy', 11);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (1, 'Slip away to the coast and seek the source of the water''s unrest', 12);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (1, 'Attend Lady Selene''s public trial, hoping to read her intentions', 13);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (1, 'Follow rumors of the Maskbearer in the eastern district', 14);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (1, 'Seek the Lustrines directly and ask them about the prophecy', 15);

-- SEGMENT 2 – THE UNRAVELING

-- 2A: Arbiter''s Scroll
INSERT INTO dialogs (id, text) VALUES (11, 'In the depths of the archive, you and **Viren** pour over ancient codes and forgotten laws.

The candlelight flickers across yellowed parchments as a startling truth emerges.

"Look here," Viren whispers, pointing to a faded inscription. "The prophecy wasn''t part of Caelondria''s divine law—it predates it entirely."

His eyes meet yours with growing alarm.

"Someone... or something... planted this prophecy long before our courts ever existed."');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (11, 'Confront the Scale itself by requesting an Audience of Judgment', 111);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (11, 'Seek the Lustrines for their memory of Caelondria before the courts', 112);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (11, 'Attempt to modify the prophecy secretly', 113);

-- 2B: Depths of Memory
INSERT INTO dialogs (id, text) VALUES (12, 'The coastal waters stretch endlessly before you, glowing with an otherworldly blue radiance.

Ancient voices hum beneath the waves, their melody both beautiful and haunting.

Suddenly, a vision crashes into your mind like a tidal wave:

*Caelondria drowning in absolute silence. Citizens frozen mid-step, their voices stolen forever. The great courts crumbling into dust.*

The vision fades, but the water''s song continues to call to you from the depths.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (12, 'Dive deeper, risking your breath, to find the source', 121);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (12, 'Capture and analyze the harmonic energy in the water', 122);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (12, 'Record and release the vision to the public', 123);

-- 2C: Curtain of Smoke
INSERT INTO dialogs (id, text) VALUES (13, 'The trial ground buzzes with anticipation as **Lady Selene** takes center stage.

Her performance is mesmerizing—silk scarves dancing through the air, illusions that dazzle the crowd. The audience is captivated.

But when her emerald eyes find yours across the plaza, something shifts.

Her confident smile falters for just a heartbeat. In that brief moment, you see raw fear—and a desperate, silent plea for help.

The trial continues around you, but you sense this is your moment to act.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (13, 'Publicly accuse her of hiding the prophecy', 131);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (13, 'Secretly visit her backstage', 132);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (13, 'Disrupt the trial with a false confession', 133);

-- 2D: Mask of Iron
INSERT INTO dialogs (id, text) VALUES (14, 'The eastern district''s narrow alleys twist like a maze in the evening mist.

Shadows shift and dance until a figure emerges from the darkness—**the Maskbearer**.

Their iron mask reflects the lamplight like a mirror, hiding their true face completely.

"So," they speak, their voice distorted but strangely compelling, "another seeker of truth arrives."

They step closer, and you feel the weight of their hidden gaze.

"Tell me, wanderer—do you want truth, or do you want change? Because in Caelondria, you cannot have both."');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (14, 'Accept their help to sabotage the Scale', 141);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (14, 'Pretend to ally with them, but report to Elira', 142);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (14, 'Convince the Maskbearer to delay action until you learn more', 143);

-- 2E: Song of the Depths
INSERT INTO dialogs (id, text) VALUES (15, 'The **Lustrines** emerge from the depths like living liquid, their forms shimmering between water and flesh.

Their eldest speaks in harmonies that resonate in your bones:

"Child of land, you seek answers to questions that have been asked many times before."

The water around you glows brighter as their revelation unfolds.

"The Prophecy is a repeating cycle. Caelondria has been reborn many times—each time forgetting, each time falling to the same fate."

The weight of countless lost civilizations presses down upon you.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (15, 'Ask how to break the cycle', 151);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (15, 'Ask to join their memorystream', 152);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (15, 'Steal a relic of memory and flee', 153);

-- SEGMENT 3 – THE DEEPER MYSTERIES

-- 3A1: Audience of Judgment
INSERT INTO dialogs (id, text) VALUES (111, 'The great chamber of the Aetherial Scale thrums with ancient power.

As you approach, the Scale itself awakens—its crystalline form pulsing with ethereal light.

**The Scale** speaks, its voice echoing from everywhere and nowhere:

"Mortal flesh seeks to judge the eternal judge? How... amusing."

The air crackles with energy as the Scale''s attention focuses entirely upon you.

"You dare question the very foundation of justice itself?"');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (111, 'Demand the Scale justify its prophecy', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (111, 'Ask the Scale to show you the truth', 302);

-- 3A2: Lustrine Memory
INSERT INTO dialogs (id, text) VALUES (112, 'The **Lustrines** surround you in a circle of flowing water and ancient wisdom.

"We will show you," their eldest intones, "the time before the Scale''s dominion."

Visions flood your mind:

*Ancient Caelondria—a time of organic justice, where communities settled disputes through understanding rather than rigid law.*

*The first arrival of the Scale, promising order but slowly stealing the people''s ability to think for themselves.*

The memories are overwhelming, beautiful, and terrifying.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (112, 'Learn the original laws of the land', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (112, 'Witness the Scale''s first judgment', 302);

-- 3A3: Secret Modification
INSERT INTO dialogs (id, text) VALUES (113, 'In the dead of night, you attempt to alter the prophecy''s ancient inscription.

Your fingers trace the glowing symbols, but the moment you try to change them, searing pain shoots through your hands.

The words resist your touch, burning with the fury of ages.

Ancient protective magic guards every letter, every meaning.

As you persist, the tablet grows hotter—warning of consequences beyond imagination.

Yet you sense that forcing this change might be possible... if you''re willing to pay the price.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (113, 'Force the change despite the pain', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (113, 'Retreat and seek another way', 302);

-- 3B1: Diving Deep
INSERT INTO dialogs (id, text) VALUES (121, 'You plunge beneath the glowing waters, your lungs burning as you descend.

The pressure builds, but your determination drives you deeper.

Through the blue luminescence, ancient structures emerge—a sunken temple where the first **Lustrines** once dwelt.

Carved into its walls are symbols that predate any known language.

As your breath runs low, you must choose: explore this sacred place or surface to safety.

The temple''s entrance beckons, promising answers... or doom.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (121, 'Enter the temple', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (121, 'Surface and bring others', 302);

-- 3B2: Harmonic Analysis
INSERT INTO dialogs (id, text) VALUES (122, 'Using an ancient resonance device, you capture the water''s mysterious energy.

The patterns that emerge are astounding—the prophecy''s rhythm matches perfectly with the city''s own heartbeat.

Every footstep, every breath, every word spoken in Caelondria pulses in time with this ancient force.

The realization hits you like a thunderbolt:

*The prophecy isn''t predicting the city''s fate—it''s actively controlling it.*

The very rhythm of life in Caelondria has been orchestrated from the beginning.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (122, 'Disrupt the rhythm', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (122, 'Follow the pattern to its source', 302);

-- 3B3: Public Revelation
INSERT INTO dialogs (id, text) VALUES (123, 'Your vision spreads through the city like wildfire.

In the marketplaces, people whisper of the drowning city. In the courts, judges pause mid-sentence as the images flood their minds.

**Elira** rushes to find you, her face pale with fury.

"What have you done?" she demands. "The people are demanding answers we don''t have!"

The crowd grows restless, their voices rising in confusion and fear.

The very foundations of order begin to crack under the weight of revealed truth.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (123, 'Lead the uprising', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (123, 'Hide and observe the chaos', 302);

-- 3C1: Public Accusation
INSERT INTO dialogs (id, text) VALUES (131, 'Your voice cuts through the trial''s pageantry like a blade.

"**Lady Selene** knows more than she reveals! She hides the prophecy''s true meaning!"

The crowd gasps. Selene''s mask of composure finally cracks, revealing the frightened woman beneath.

"You don''t understand," she pleads, her voice breaking. "I was protecting them... protecting all of us!"

The trial descends into chaos as spectators demand answers.

Guards move to restrain you, but Selene holds up a trembling hand to stop them.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (131, 'Press your advantage', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (131, 'Offer her a chance to explain', 302);

-- 3C2: Backstage Secrets
INSERT INTO dialogs (id, text) VALUES (132, 'Behind the trial''s grand stage, **Selene** waits in her private tent.

Her confident performance mask has fallen away, revealing exhausted features and worried eyes.

"I wondered when someone would see through the illusion," she says quietly.

She reveals a hidden scroll—the prophecy''s true, complete text.

"The version the courts know is a fragment. The real prophecy... it''s worse than they imagine. I''ve been protecting the city from despair."

Her hands shake as she shows you words that could shatter nations.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (132, 'Join her in the deception', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (132, 'Convince her to reveal the truth', 302);

-- 3C3: False Confession
INSERT INTO dialogs (id, text) VALUES (133, 'You leap onto the trial platform, your voice booming across the plaza.

"I confess! I am the one who awakened the prophecy!"

The crowd erupts in shocked murmurs. **Selene** stares at you in horror and confusion.

Your false confession creates chaos—the real trial is forgotten as guards rush toward you.

In the confusion, you catch Selene''s grateful but terrified gaze.

You''ve bought her time, but at what cost to yourself?');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (133, 'Escape in the confusion', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (133, 'Stand your ground and speak truth', 302);

-- 3D1: Alliance with Maskbearer
INSERT INTO dialogs (id, text) VALUES (141, 'The **Maskbearer** leads you through hidden passages beneath the city.

In a forgotten chamber, they reveal the Scale''s greatest weakness:

"It feeds on the people''s belief in absolute justice. But it fears their judgment above all else."

They show you ancient mechanisms—ways to turn the city''s own infrastructure against the Scale.

"The people created the Scale through their desire for perfect order. They can unmake it through their demand for imperfect freedom."

The plan is dangerous, but it might be Caelondria''s only hope.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (141, 'Help them expose the Scale', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (141, 'Warn the Scale of the plot', 302);

-- 3D2: Double Agent
INSERT INTO dialogs (id, text) VALUES (142, 'The **Elira** listens intently to your report about the Maskbearer''s plans.

"Excellent work," she says, but something in her eyes suggests she knows more than she''s revealing.

As you leave her chambers, you notice **the Maskbearer** waiting in the shadows.

"Did you really think I wouldn''t expect your betrayal?" they ask, seemingly amused.

"The game is more complex than you realize, double agent. Even Elira serves masters she doesn''t fully understand."

You realize you''ve walked into a web far more intricate than you imagined.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (142, 'Prepare for the Maskbearer''s counter-move', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (142, 'Try to genuinely ally with Elira', 302);

-- 3D3: Delay and Learn
INSERT INTO dialogs (id, text) VALUES (143, 'The **Maskbearer** considers your words carefully.

"Time serves the prophecy, not us," they warn, but nod slowly. "Yet perhaps delay brings wisdom."

They grant you three days to gather information before they act.

"Use this time well, seeker. When next we meet, you must choose: stand with the revolution or watch Caelondria burn."

As they fade back into the shadows, you feel the weight of borrowed time.

Three days to prevent catastrophe—or ensure it happens on your terms.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (143, 'Use the time to gather allies', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (143, 'Seek the prophecy''s origin', 302);

-- 3E1: Breaking the Cycle
INSERT INTO dialogs (id, text) VALUES (151, 'The **Lustrines** exchange glances that ripple through the water like silent conversations.

Their eldest speaks with infinite sadness:

"To break the cycle, someone must remember all the iterations—every life lost, every attempt failed, every hope crushed."

The water around you grows heavy with the weight of cosmic responsibility.

"The burden would drive mortals mad. Yet without this sacrifice, the cycle continues eternally."

They extend a tendril of living water toward you.

"Are you prepared to carry the memories of ten thousand lifetimes?"');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (151, 'Volunteer to bear the memory', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (151, 'Find another way', 302);

-- 3E2: Joining the Stream
INSERT INTO dialogs (id, text) VALUES (152, 'You allow the **Lustrines** to guide you into their collective consciousness.

The memorystream engulfs you—a torrent of experiences spanning millennia.

You feel the joy of children playing in ancient streets, the sorrow of lovers separated by war, the wisdom of elders watching civilizations rise and fall.

Every emotion, every thought, every dream of countless lives flows through you.

In this vast tapestry of existence, your individual self begins to dissolve.

Yet you sense you could return to yourself... if you choose to resist the stream''s embrace.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (152, 'Merge completely with the stream', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (152, 'Maintain your individual identity', 302);

-- 3E3: Stealing Memory
INSERT INTO dialogs (id, text) VALUES (153, 'You snatch the crystal relic from the **Lustrines''** sacred altar.

The moment your fingers close around it, ancient knowledge floods your mind—but so do their anguished cries.

"Thief! Destroyer! You know not what you steal!"

The relic burns with stolen memories, its power intoxicating yet corrupting.

As you flee through the water, their lamentations follow you like a curse.

You possess fragments of ultimate truth, but at the cost of an entire people''s trust.

The relic''s knowledge is yours—but its burden may destroy you.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (153, 'Use the relic''s power immediately', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (153, 'Return and seek forgiveness', 302);

-- SEGMENT 4 – VERDICT''S EDGE

-- 4A: Collapse in the Crowd
INSERT INTO dialogs (id, text) VALUES (301, 'The city erupts into chaos as truth collides with order.

In the grand plaza, citizens demand answers while judges flee their benches. **The Maskbearer** emerges from hiding, their voice booming across the square:

"The prophecy is fulfilled! The lies drown in truth, and the scale tips toward freedom!"

Ancient mechanisms activate throughout the city. The Aetherial Scale''s perfect order crumbles as people remember how to think for themselves.

But in the chaos, you see both hope and terror in the citizens'' faces.

Freedom is messy, dangerous, and utterly human.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (301, 'Take control and calm the people', 401);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (301, 'Let the chaos run its course', 402);

-- 4B: The Memory Spiral
INSERT INTO dialogs (id, text) VALUES (302, 'You spiral through layers of time and memory, experiencing the weight of every previous collapse.

The first Caelondria—destroyed by war.
The second—consumed by plague.
The third—lost to despair.
The fourth—erased by tyranny.

Each iteration tried to solve the problems of the last, only to create new disasters.

Now you stand at the threshold of the fifth collapse, armed with the knowledge of all previous failures.

The pattern is clear: perfection cannot be imposed, only grown.

But will Caelondria listen to lessons written in its own forgotten blood?');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (302, 'Rewrite the current cycle', 403);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (302, 'Anchor yourself in one of the past realities', 404);

-- SEGMENT 5 – THE FINAL CHOICE

-- 5A: Tides of Choice
INSERT INTO dialogs (id, text) VALUES (401, 'Your voice cuts through the chaos like a beacon.

"Citizens of Caelondria! We stand at the crossroads between tyranny and anarchy!"

The crowd slowly turns to listen as you step forward.

"We can create something new—courts of wandering judges who serve the people, not power. Justice that grows from understanding, not rigid law."

**Viren** joins you, then **Elira**, and even some of the former court officials.

Together, you forge a new system where the Aetherial Scale serves as a tool, not a master.

The transition is difficult, but hope blooms in the streets.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (401, 'Spread this new justice across the world', 501);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (401, 'Keep it local, protecting Caelondria', 502);

-- 5B: Echoes Reclaimed
INSERT INTO dialogs (id, text) VALUES (402, 'You choose to let the chaos unfold naturally.

The old order crumbles completely, but from its ashes, something unprecedented emerges.

Citizens form their own councils. Neighbors help neighbors resolve disputes. The rigid hierarchy dissolves into organic community.

You find yourself walking between the present moment and glimpses of all possible futures.

Reality trembles around you as the boundaries between what is and what could be begin to blur.

You have become a living bridge between worlds—forever changed, forever changing.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (402, 'Let the worlds merge into one', 503);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (402, 'Sever the link and accept your rewritten truth', 504);

-- 5C: Cycle Rewriter
INSERT INTO dialogs (id, text) VALUES (403, 'With the power to rewrite history flowing through you, every possible future spreads before your consciousness.

You could create a perfect world where suffering never existed.
You could preserve every life lost in previous cycles.
You could make Caelondria a paradise beyond imagination.

But as you reach for ultimate power, you pause.

The weight of playing god settles upon your shoulders like lead.

Perfect worlds lack growth. Paradise without struggle breeds stagnation.

Perhaps the greatest act of power is choosing restraint.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (403, 'Create a perfect world', 501);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (403, 'Preserve the imperfect truth', 502);

-- 5D: Past Anchor
INSERT INTO dialogs (id, text) VALUES (404, 'You choose to anchor yourself in the third iteration of Caelondria—a time of art and wonder, before despair claimed it.

Here, **Selene** performs not for trials but for joy. **Viren** studies history to understand, not to judge. The **Lustrines** swim freely in waters unpolluted by prophecy.

It''s beautiful, but something feels hollow.

You know this paradise is built on illusion. The present still calls to you, demanding resolution.

You could stay here forever in this borrowed time, or return to face the consequences of choice.

The past is seductive, but the future needs you.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (404, 'Return to your original time', 503);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (404, 'Stay in the chosen past', 504);

-- SEGMENT 6 – FINALE (Endings)

-- Ending 1: The Sovereign of Rain
INSERT INTO dialogs (id, text) VALUES (501, '**Ending 1: The Sovereign of Rain**

Years pass like seasons, each bringing new growth to the world you and **Viren** have shaped.

The Aetherial Scale now serves as a tool of understanding rather than absolute judgment. Justice has become empathy, law has become conversation, and order grows from community rather than control.

Your new courts spread across continents like ripples on water. Nations adopt the philosophy of "growing justice"—allowing legal systems to evolve with their people''s needs and dreams.

**Selene** leads a renowned school for truth-seekers, teaching others to reveal beauty in honesty. **The Maskbearer** has cast aside their iron disguise and works openly for transparency and accountability.

The silver rain still falls over Caelondria, but now it nourishes rather than warns. Gardens bloom where once only judgment grew.

Caelondria thrives as a beacon of hope across the known world—living proof that perfect systems aren''t born from rigid rules, but grown with patience, wisdom, and care.

*You have become the Sovereign of Rain, bringing growth where there was once only judgment.*

**THE END**');

-- Ending 2: The Hidden Verse
INSERT INTO dialogs (id, text) VALUES (502, '**Ending 2: The Hidden Verse**

You and **Lady Selene** make a choice that will echo through generations: the complete prophecy remains forever hidden.

Working in shadows and silence, you two guardians protect Caelondria from the crushing weight of absolute truth. Some lies, you''ve learned, preserve hope better than devastating honesty.

The city continues its imperfect dance between order and chaos, never knowing how close it came to complete destruction. Citizens live, love, and dream, blissfully unaware of the fate you''ve helped them avoid.

**Selene''s** performances become legendary, each one a subtle reminder of the beauty worth protecting. You operate from the margins of society, gently guiding decisions away from the prophecy''s dark path.

Decades pass. As you both grow old together in your secret partnership, you sometimes wonder if you chose correctly.

But watching children play in streets that might never have existed, seeing lovers meet in squares that could have been ruins, you find a deep and lasting peace.

*Some truths are too heavy for the world to bear. Sometimes, love means carrying that weight in silence.*

**THE END**');

-- Ending 3: Judgment Broken
INSERT INTO dialogs (id, text) VALUES (503, '**Ending 3: Judgment Broken**

The Aetherial Scale shatters like ancient crystal, its fragments raining down on the plaza like silver tears.

The collapse is immediate and total. Without the Scale''s binding force holding society together, Caelondria tears itself apart in a matter of days.

Neighbor turns against neighbor as old grievances surface. The courts become battlegrounds where former allies settle scores with violence rather than words. Order dissolves into pure, destructive chaos.

You escape the burning city with **Viren** and a handful of survivors, watching from distant hills as generations of progress crumble into ash and memory.

"Was it worth it?" Viren asks, his voice hollow with loss.

You don''t answer immediately. You can''t answer. The weight of what you''ve unleashed sits heavy on your shoulders.

Years later, living in exile, you help other cities avoid Caelondria''s fate. Your story becomes a cautionary tale whispered in courts across the world—a reminder that some cages exist to protect, not imprison.

*Sometimes, revolution devours its own children. Sometimes, the price of absolute truth is everything you sought to save.*

**THE END**');

-- Ending 4: Floodlight Reborn
INSERT INTO dialogs (id, text) VALUES (504, '**Ending 4: Floodlight Reborn**

You choose to remain in your own time, accepting all its imperfections and embracing all its possibilities.

With **Elira''s** partnership and the wisdom of all you''ve learned, you establish courts truly made by and for the people. Not perfect justice, but honest justice—flawed, growing, and beautifully human.

The new Caelondria rises slowly from the ashes of the old, built not on the rigid foundation of absolute law, but on the living bedrock of community consensus and shared responsibility.

**The Lustrines** emerge from the depths to share their ancient wisdom with those willing to listen. **Selene** teaches the delicate art of revealing truth through beauty and performance. **The Maskbearer** helps design systems of accountability that prevent future tyranny.

It''s messy work filled with setbacks, arguments, and failures. There are days when the old ways seem simpler, cleaner, more efficient.

But there''s also laughter echoing in the streets, genuine debate filling the halls, and the rare, precious sight of people governing themselves with wisdom born from experience.

The Aetherial Scale remains in its place of honor, but transformed—a reminder of what happens when perfection becomes more important than people, when order matters more than growth.

*Caelondria is reborn in clarity and courage, imperfect but alive, growing steadily toward the light.*

**THE END**

*Thank you for experiencing "The Aetherial Scale." Your choices have shaped not just a story, but a reflection on justice, truth, and the courage to choose imperfection over tyranny.*');
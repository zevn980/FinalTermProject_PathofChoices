-- SEGMENT 1 – THE SUMMONING
INSERT INTO dialogs (id, text) VALUES (1, 'The silver rain falls softly as you arrive at the Grand Hall of Echoes.

**Lord Viren** emerges from the shadows, his weathered face etched with concern.

"Traveler," he says, his voice heavy with urgency, "the Aetherial Scale has faltered. The prophecy has begun to stir."

He reveals a fragment of an ancient tablet, its surface pulsing with ethereal light:

*"When the last lie is drowned, the scale shall tip. Four trials, one truth, and a price no nation can bear."*

The weight of destiny settles upon your shoulders. What path will you choose?');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (1, 'Swear your aid to Viren and help investigate the prophecy', 6);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (1, 'Slip away to the coast and seek the source of the water''s unrest', 7);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (1, 'Attend Lady Selene''s public trial, hoping to read her intentions', 8);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (1, 'Follow rumors of the Maskbearer in the eastern district', 9);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (1, 'Seek the Lustrines directly and ask them about the prophecy', 10);


INSERT INTO dialogs (id, text) VALUES (6, 'Swear Your Aid to Viren.

Lord Viren’s gaze sharpens. He nods, then turns to the towering Aetherial Scale. 

"Then we begin," he says, and hands you the fragment.

By torchlight in the vaults below the Grand Hall, ancient scrolls whisper truths. 

You decipher glyphs that reference a "Lost Cycle"—a time when judgment was not bound to performance, but to memory.

"Someone... or something... planted this prophecy long before our courts ever existed."');
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (6, 'Continue', 11);


INSERT INTO dialogs (id, text) VALUES (7, 'Seek the Coast’s Unrest

You leave the city in secret, boots splashing through rivulets as silver rain turns to mist. 

The coast groans beneath a luminous tide. The water sings to you, not in words—but in grief.

A haunting vision grips you: Caelondria swallowed not by sea, but by silence.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (7, 'Continue', 12);


INSERT INTO dialogs (id, text) VALUES (8, 'Attend Lady Selene’s Trial

The marble amphitheater thrums with expectation. 

Lady Selene, draped in liquid silk, performs the accused’s soliloquy. 

Her voice dances like wind over water, but when her eyes meet yours—they tremble.

Her expression shifts: a coded plea for aid behind her act.');


INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (8, 'Continue', 13);


INSERT INTO dialogs (id, text) VALUES (9, 'Track the Maskbearer

In the veiled alleyways of the Eastern District, steam rises from grates like breath from a sleeping beast. 

You follow whispers until shadow forms resolve.

The Maskbearer steps forward. Their mask reflects your face.

"You walk between judgment and freedom. Which do you want?"');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (9, 'Continue', 14);


INSERT INTO dialogs (id, text) VALUES (10, 'Seek the Lustrines

The journey through rain-fed caverns takes days. 

When you finally reach the submerged sanctum, the Lustrines rise from the water like memories made flesh.

"Ah," one sings, "a ripple from the surface seeks truth. But truth is a tide, not a stone."');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (10, 'Continue', 15);

-- SEGMENT 2 – THE UNRAVELING

-- 2A: Arbiter''s Scroll
INSERT INTO dialogs (id, text) VALUES (11, 'Setting: Deep below the Grand Hall, amid vaults lined with ancient scrolls and forgotten laws

Viren: (lighting a blue aether-lamp)
 "The Aetherial Court erased what came before. But this…" (he gestures to the tablet fragment) "This is no law of ours. It’s older."

You trace the lines etched into obsidian. Some glyphs shimmer only when touched.

Traveler:
 "These symbols… they echo Celestial script. But this line—it’s broken."

Viren: (grimly)
 "Yes. The prophecy was fragmented during the Founding Judgment. The founders feared what it foretold."

As you work, you uncover a passage hidden in spectral ink:

"He who rewrites truth must first break it."

Traveler:
 "Then the prophecy was meant to change. To evolve."
V
iren:
 "And that frightens the Scale. Because change means fallibility."');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (11, 'Confront the Scale itself by requesting an Audience of Judgment', 111);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (11, 'Seek the Lustrines for their memory of Caelondria before the courts', 112);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (11, 'Attempt to modify the prophecy secretly', 113);

-- 2B: Depths of Memory
INSERT INTO dialogs (id, text) VALUES (12, 'Setting: Underwater caverns off the coast, glowing with ethereal blue currents. Echoes resonate like distant songs.

As you dive, water cradles you like silk. A harmonic resonance begins to pulse.

A shape—not of this time—emerges from the depths.

Lustrine Apparition: (voice layered like many tides)
 "Child of surface, why return to the womb of truth?"

Traveler:
 '"This vision… it's a warning. Caelondria drowning, but not from flood. From silence."'

Apparition:
 "Memory denied becomes rot. Justice performed becomes farce. The Aetherial Scale sings only its own name."
Suddenly, an image: Lady Selene, crying alone in her court chambers. The Maskbearer, placing a relic in a fountain. Viren, eyes blank before a mirror.

Traveler:
 "What are these? The past? The future?"

Apparition:
 "Yes." ');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (12, 'Dive deeper, risking your breath, to find the source', 121);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (12, 'Capture and analyze the harmonic energy in the water', 122);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (12, 'Record and release the vision to the public', 123);

-- 2C: Curtain of Smoke
INSERT INTO dialogs (id, text) VALUES (13, 'Setting: The Grand Amphitheater, mid-trial. The audience is breathless.

Lady Selene: (in full dramatic flair)
 '"O Caelondria, do you judge me for truth or for spectacle?"'

The crowd roars approval. But her eyes find yours—piercing, desperate.

Lady Selene: (quietly, under breath, to you)
 '"You know what this is. Don't let them win."'

Traveler: (quietly)
 '"What do you mean? What's really on trial?"'

She twirls dramatically toward the crowd.

Lady Selene:
 '"What if I told you prophecy lived? That judgment was not divine, but scripted?"'

The magistrate slams a gavel.

Magistrate:
 '"Order! Lady Selene, this is heresy!"'

Lady Selene: (to you, softly as she dragged away)
 "Find the Maskbearer. Or Viren. Or the tide itself. The curtain’s falling."');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (13, 'Publicly accuse her of hiding the prophecy', 131);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (13, 'Secretly visit her backstage', 132);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (13, 'Disrupt the trial with a false confession', 133);

-- 2D: Mask of Iron
INSERT INTO dialogs (id, text) VALUES (14, ' Setting: A rooftop in the Eastern District. Moonlight reflects off brass pipes and watery tiles.

The Maskbearer steps from the dark, a cloak of woven mist around them.

Maskbearer:
 '"Traveler. The Scale judges, but never listens. You can change that. But not without dirtied hands."'

Traveler:
 '"You''ve manipulated events. Selene, Elira, even Viren."'

Maskbearer: (tilting head)
 "All puppets. As are we. The difference is—I see the strings."

A gust of wind scatters paper talismans. On one is your face.

Maskbearer:
 "You must choose: truth that burns, or peace that blinds."');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (14, 'Accept their help to sabotage the Scale', 141);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (14, 'Pretend to ally with them, but report to Elira', 142);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (14, 'Convince the Maskbearer to delay action until you learn more', 143);

-- 2E: Song of the Depths
INSERT INTO dialogs (id, text) VALUES (15, 'Setting: The submerged sanctum of the Lustrines. Crystal water forms spires. A choir of memory surrounds you.

Lustrine Elder:
 "Ah, the Traveler dares ask the tides their secrets."

Traveler:
 "Is the prophecy real? Or just a myth?"

Lustrine Elder: (smiling sadly)
 "It is both. It is always. Caelondria has drowned before. And each time, a lie became law."

You’re led to a pool of living memory. Visions swirl: Past cities. Past versions of Viren and Selene. Past you.

Traveler:
 "What… what is this?"

Lustrine Elder:
 "A cycle. The same souls reshaping new masks. Break it—and we break ourselves."');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (15, 'Ask how to break the cycle', 151);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (15, 'Ask to join their memorystream', 152);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (15, 'Steal a relic of memory and flee', 153);

-- SEGMENT 3 – THE DEEPER MYSTERIES

-- 3A1: Audience of Judgment
INSERT INTO dialogs (id, text) VALUES (111, 'Setting: The Sanctum of Judgment. An immense, cathedral-like chamber where the Aetherial Scale hovers, suspended by streams of hydro-crystal energy.

You and Viren kneel before the divine mechanism. 
Its arms, weighted by liquid silver and gold, begin to stir.

Viren: (his voice low)
 "This is no court, Traveler. This is the echo of gods who no longer speak."

A choir of robed Arbiters flanks the chamber.

Scale Voice (mechanical, resonant):
 '"State your anomaly."'

Traveler: (stepping forward)
 '"The prophecy. It predates you. It speaks of collapse. It has already begun."'

The Scale hesitates. The water around it shudders.

Scale Voice:
 '"Truth unconfirmed. Judgment pending."'

Viren: (to you, quickly)
 '"It doesn''t recognize the prophecy. It fears irrelevance."'

Suddenly, a light flares—the Scale begins weighing you.

Scale Voice:
 '"Subject: Undefined. Role: Interfering variable. Judgment: To be Determined."'');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (111, "You’ve triggered a sequence known as Verdict’s Edge. The Scale is sentient—and unstable.", 201);

-- 3A2: Lustrine Memory
INSERT INTO dialogs (id, text) VALUES (112, 'Setting: The hidden passage beneath the Hall leads you and Viren deep into the Veiled Springs.

The Lustrines rise from their pool, curious.
L
ustrine Seer:
 '"You again, Arbiter’s ghost. What does your mortal machine want this time?"'

Viren: (bowing, uncharacteristically humble)
 "To remember what we buried."

The Seer waves a hand. A memory blossoms—a younger Viren arguing with the First Court, pleading against the creation of the Scale.

Traveler: (astonished)
 '"You were against it?"'

Viren:
 '"I was human then."'

Lustrine Seer:
 '"The truth is: Caelondria''s perfection was forged from betrayal. You both are echoes of resistance."'

The Seer offers a silver flask.

Lustrine Seer:
 '"Drink, and see what the Scale denies."'
');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (112, " You’ve been granted access to the Memory Spiral.", 206);

-- 3A3: Secret Modification
INSERT INTO dialogs (id, text) VALUES (113, 'Setting: Within the vault, by candlelight, you and Viren prepare a glyph-sculptor.

Traveler:
 '"If it can be rewritten, we might control what happens next."'

Viren: (gravely)
 '"But who decides what truth becomes?"'

You alter the prophecy fragment, changing one line:
'"“Four trials, one truth”" → "“Four trials, one choice.”"'

The air thickens. Ink seeps up from the floor. The glyphs resist.

Viren:
 '"It’s fighting back. It knows."'

Suddenly, a beam of aetherial light surges upward—a warning. The Aetherial Scale feels the alteration.
Viren:
 '"The mechanism is aware. We may have doomed or saved it. There’s no going back."'');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (113, 'You have unknowingly triggered a feedback loop within the Scale judgment protocol.', 206);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (113, 'Collapse in the Crowd, depending on whether you reveal it.', 205);

INSERT INTO dialogs (id, text) VALUES (121, 'Setting: Beneath the harmonic waters, where pressure crushes and light fails.

You inhale sharply—then plunge deeper, following the thrum of forgotten frequencies. 

Shapes shimmer: buildings of coral-glass, overgrown with memory.

You reach a massive stone locked in the seabed—an ancient version of the Aetherial Scale, long rusted and silent.

A Lustrine spirit watches.

Lustrine Warden: (echoing inside your mind)
 '"You seek the Source. But breath is time, and time runs dry."'

Traveler: (struggling for air)
 '"This machine... it came before?"'

Warden:
 "It judged stars. Then men. Then myths. Until it drowned in pride."
Your lungs burn. The Warden places a hand on your chest.
Warden:
 "To see the truth, you must die to what you are… or surface now, and forget."
You gasp—');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (121, 'The Memory Spiral (if you accept the vision)', 306);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (121, 'Collapse in the Crowd (if you return and speak of it)', 305);

-- 3B1: Diving Deep
INSERT INTO dialogs (id, text) VALUES (122, 'Setting: The beach at twilight. You set up resonance-capture crystals, aided by a wandering scholar named Seren.

Seren: (adjusting the dial)
 '"If I’m right, these waves are more than water. They''re memory—echoing judgment."'

Traveler:
 '"Why would memory resonate like music?"'

Seren: (smirking)
 '"Because Caelondria is a stage, not a city. Everything here plays a role. Even truth."'

The crystals pulse. One explodes—images spill out: Selene weeping in chains, the Maskbearer removing their mask, Elira shooting into the sky.

Seren: (whispers)
 '"This… isn’t science. It’s soulprint. These aren’t just memories. They’re potential futures."'

Traveler: (stunned)
 '"Then we’re not just watching… we’re choosing."'

 Outcome: You’ve extracted “future-memory.” The path ahead can be altered with foresight');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (122, 'Precedent Shift (optional timeline shift)', 203);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (122, 'The Memory Spiral', 206);

-- 3B2: Harmonic Analysis
INSERT INTO dialogs (id, text) VALUES (123, 'Setting: You return to the city square. A crowd gathers as you project the harmonic vision into the fountain’s surface.

The water swirls—showing destruction, silence, drowned courts.

Nobleman: (gasping)
 '"This… this is the prophecy!"'

Commoner: (crying out)
 '"They knew and lied to us!"'

Elira Valen storms in, pistol drawn.

Elira:
 '"Who showed this? WHO?"'

Traveler:
 '"I did. The people deserve truth."'

She stares at the vision—then lowers her weapon.

Elira: (softly)
 '"Then we’re out of time."'

The crowd erupts into panic.

Maskbearer (from shadows):
 "Well done, truthbearer. Now chaos may finally speak."
');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (123, 'Public unrest spreads. The illusion of order is broken', 205);

-- 3B3: Public Revelation
INSERT INTO dialogs (id, text) VALUES (131, 'Setting: The Grand Amphitheater, filled with nobles and commoners alike.

Lady Selene’s voice rings like music as she delivers a poetic soliloquy on justice. But her gaze falters when it meets yours.

Traveler (interrupting):
 '"Selene, how long have you known? The prophecy—how deep does your performance go?"'

Gasps echo. The judge-bell ceases tolling.

Lady Selene: (composed, then bitter)
 '"Such accusations… in public? Clever."'

She steps forward, arms wide like a starlet basking in moonlight.

Selene:
 '"Yes, I knew. I read it in the veins of the Scale. But truth isn’t light—it blinds. The people are not ready."'

High Arbiter Viren (emerging):
 '"And who made you their protector?"'

A ripple of argument surges through the crowd.

Selene (to you, quietly):
 '"You just shattered the illusion. Now be ready to live with what rises."'

Outcome: You’ve exposed the secret, triggering both fear and inspiration in the people.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (131, 'Collapse in the Crowd', 205);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (131, 'Precedent Shift', 203);

-- 3C1: Public Accusation
INSERT INTO dialogs (id, text) VALUES (132, 'Setting: Velvet curtains and hidden mirrors backstage. Selene sits alone, applying silver pigment to her eyes.

Selene: (without looking up)
 '"I hoped you’d come. You see more than the rest."'

Traveler:
 '"You looked at me like you knew the prophecy. I need the truth."'

She removes her headdress—underneath, her hair is streaked with blue flame, like a Lustrine’s glow.

Selene:
 '"I was born in the flood years. Half-Lustrine. I can hear the Scale’s hum when it lies."'

She hands you a crystal:

Selene:
 '"This contains its first failure. When the Scale judged an innocent child and called it balance."'

Traveler: (softly)
 '"And now?"'

Selene:
 '"We need to tip the Scale... not with weight, but with song."'

Outcome: You gain Selene’s trust and access to secret court records. A quiet alliance begins.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (132, 'Precedent Shift', 301);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (132, 'The Memory Spiral', 302);

-- 3C2: Backstage Secrets
INSERT INTO dialogs (id, text) VALUES (133, 'Setting: The trial crescendos. The judge is about to declare sentence.

You rise suddenly.

Traveler:
 '"I was the one who tampered with the prophecy!"'

Silence. The entire amphitheater turns.

Lady Selene: (startled, but playing along)
 '"My stars... I never suspected."'

Judge: (narrowing eyes)
 '"Then you shall face the Scale."'

As you’re seized, Selene slips a tiny whisper into your ear:

Selene: (softly)
 '"Smart move. Now you’re in the chamber where truths are rewritten."'

Outcome: You are sent to the heart of the Scale—its judgment vault.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (133, 'Judgment Echo', 201);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (133, 'The Memory Spiral' , 206);

-- 3C3: False Confession
INSERT INTO dialogs (id, text) VALUES (141, 'Setting: A secret chamber beneath the Eastern District. The Maskbearer removes a panel revealing the Scale’s power lines—pulsing with liquid judgment.

Maskbearer: (soft, sharp)
 '"The people worship a lie. But we can make them feel the truth."'

Traveler: (uneasy)
 '"Sabotage risks collapse. Are you sure it’s the only way?"'

Maskbearer:
 '"I was once a Judge. I saw it condemn a mother who saved her child from drowning because mercy ‘disrupted balance.’"'

You plant resonance disruptors—then step back.

Maskbearer (to you):
 "The truth doesn’t wait. Neither should we."

A surge of energy cracks the silver circuits. The Scale emits a keening whine across the city.

Outcome: The Scale is destabilized. Public hearings distort. Chaos starts to spread—but so does awareness.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (141, 'Collapse in the Crowd', 205);

-- 3D1: Alliance with Maskbearer
INSERT INTO dialogs (id, text) VALUES (142, 'Setting: After your meeting, you slip away through a whisper-tunnel and reach Elira’s hideout.

Elira: (arms folded, suspicious)
 '"So the ghost in the mask wants to burn the city?"'

Traveler:
 '"They''re planning a sabotage. You have a chance to stop it."'

Elira (pacing):
 '"And what do you want, truthseeker? Chaos? Or justice?"'

Traveler:
 '"I want to choose. Not be told."'

She nods—then hands you a silent flare.

Elira:
 '"When they move, light this. We’ll intervene. Maybe we’ll save more than the city."'

Outcome: You now walk a line between rebellion and resistance. Your choice will determine who survives the fallout.
');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (142, ' Broken Court', 204);

-- 3D2: Double Agent
INSERT INTO dialogs (id, text) VALUES (143, 'Setting: Rooftop overlooking the Aetherial Court. The Maskbearer kneels, mask in hand.

Traveler:
 '"Sabotage will only feed the fire. Wait—let me uncover what lies beneath first."'

Maskbearer (pausing):
 '"You''re not like the others. You ask, instead of preach."'

Traveler:
 '"What if I learn something that ends it without ruin?"'

Maskbearer: (after a long silence)
 '"One week. Then we flood the truth."'

Outcome: You’ve earned time and a strange trust. The Maskbearer holds the flood back—for now.
');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (143, 'The Memory Spiral', 306);


INSERT INTO dialogs (id, text) VALUES (151, 'Setting: A hidden lagoon deep beneath Caelondria. Luminescent water cradles ancient stone runes. The Lustrines drift like spirits made of sea and light.One, older than the tide, speaks.

Elder Lustrine: (softly, like water on marble)
 '"Cycles repeat because the hearts that drive them remain unchanged."'

Traveler:
 '"How do I break what was always meant to repeat?"'

The Elder draws glyphs in the water.
Elder Lustrine:
 '"To end the cycle, you must choose a moment... and anchor truth in it. But truth is heavier than stone."'

Another Lustrine approaches—a child with silver eyes.

Child Lustrine:
 '"You’ll need a memory so strong it survives forgetting."'

They guide you to the Pool of First Judgments.
Elder Lustrine:
 "Choose wisely. This truth will shape all futures."

Outcome: You gain insight into the nature of the prophecy as a memory loop. You may choose to intervene at the root.
');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (151, 'The Memory Spiral', 306);

-- 3E1: Breaking the Cycle
INSERT INTO dialogs (id, text) VALUES (152, 'Setting: You kneel before the lagoon. The Lustrines chant as their water encircles you. Your mind slips into their shared stream.

You fall through time—witnessing the first founding of Caelondria, the rise of the Aetherial Scale, the first collapse.

A city reborn in beauty… but always with judgment at its core.
You watch as each era repeats—different masks, same melody.

Traveler (in memory):
 '"We’ve never escaped. Only performed escape."'

A voice echoes—it’s Lady Selene, her younger self.

Selene (memory):
 '"If the city must collapse to learn... let it collapse beautifully."'

You wake. The Lustrines are watching.

Elder Lustrine:
 '"Now you understand. What will you do with that truth?"'

Outcome: You carry the full stream of Caelondria’s cycles within you—along with the knowledge to change one.
');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (152, 'The Memory Spiral', 306);

-- 3E2: Joining the Stream
INSERT INTO dialogs (id, text) VALUES (153, 'Setting: While the Lustrines sleep in their moon-suspended trance, you sneak through the Memory Grotto.

Your eyes lock on a glyph-crystal, still pulsing with scenes from the first Cycle’s collapse.

You hesitate—then snatch it.

Alarms of soft song rise. Water begins to swirl.

Lustrine Voice (in your mind):
 '"To steal memory is to forget yourself."'

You flee—but not unchanged. Visions assault you.

Lady Selene drowning.

Elira shot by Viren.

 The Maskbearer unmasked... as you.
You stumble into the streets of Caelondria—half-lost in time.

Outcome: You now possess forbidden memory—but reality begins to slip.
');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (153, ' Collapse in the Crowd', 205);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (153, ' Echoes Reclaimed', 306);

-- 3E3: Stealing Memory
INSERT INTO dialogs (id, text) VALUES (205, 'Setting: The Aetherial Court square. The Scale sputters sparks. Selene’s trial is interrupted. A storm gathers—not of weather, but of truth.

The people scream. Nobles shove commoners. The Scale releases a wail, discordant and unnatural.

Maskbearer (appearing above the chaos):
 '"The prophecy has been fulfilled. Not because fate demands it, but because lies can only bear so much weight."'

Lady Selene (shouting):
 '"Stop! We can still control the descent—"'

But a noble strikes her with a thrown emblem. She stumbles. The crowd surges.
');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (205, 'Take Control and Calm the People', 215);

-- SEGMENT 4 – VERDICT''S EDGE

-- 4A: Collapse in the Crowd
INSERT INTO dialogs (id, text) VALUES (215, 'Setting: The Aetherial Court square. The Scale sputters sparks. Selene’s trial is interrupted. 
A storm gathers—not of weather, but of truth.

The people scream. Nobles shove commoners. The Scale releases a wail, discordant and unnatural.

Maskbearer (appearing above the chaos):
 '"The prophecy has been fulfilled. Not because fate demands it, but because lies can only bear so much weight."'

Lady Selene (shouting):
 '"Stop! We can still control the descent—"'

But a noble strikes her with a thrown emblem. She stumbles. The crowd surges.
');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (215, 'Tides of Choice', 305);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (215, ' Let the Chaos Run Its Course', 225);

-- 4B: The Memory Spiral
INSERT INTO dialogs (id, text) VALUES (225, 'You step back, into shadow.

Traveler (softly):
 '"If they never learn, let them drown in their own judgment."'

You walk away as the court collapses. Fires spark. The Scale cracks and falls like a dying god.

Lady Selene (faintly):
 '"At least now... the curtain falls honestly."'

Elira (later):
 '"You chose silence. That too, is a judgment."'

Outcome: Caelondria descends into anarchy. Only fragments may rise again');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (225, 'Judgment Broken', 503);

-- SEGMENT 5 – THE FINAL CHOICE

-- 5A: Tides of Choice
INSERT INTO dialogs (id, text) VALUES (206, 'Setting: A dreamscape stitched from flowing memories. You float in the spiral stream of Lustrine memory, seeing Caelondria’s four rebirths.

The Elder Lustrine:
 "Each time, the city ends with the same breath. Judgment consumes itself. And yet… each cycle leaves a scar."

You witness:

First Era: Caelondria as a flood-borne village ruled by seers.


Second: A kingdom of theater-law, where truth was performance.


Third: The first Aetherial Scale, made by Selene’s ancestor.


Fourth: The current age—fractured, fragile, forgetting.
');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (206, 'Rewrite the Current Cycle', 216);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (206, 'Tides of Choice', 305);

-- 5B: Echoes Reclaimed
INSERT INTO dialogs (id, text) VALUES (216, 'You reach out to the present memory and twist it—replacing the core judgment law with empathy as equilibrium.

Traveler:
 "Let Caelondria be judged by how it lifts its weakest… not by symmetry, but by soul."

The spiral shivers—and resets. You wake in a new court. The people speak truth without fear.

Outcome: A new path is formed—justice reborn.');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (216, 'Tides of Choice', 305);

-- 5C: Cycle Rewriter
INSERT INTO dialogs (id, text) VALUES (226, 'You grip the memory of the Second Era—the poetic court where trials were plays.

Traveler (whispering):
 '"Let that be my truth."'

You awaken... but it’s not the same Caelondria.

Lady Selene is a priestess-poet. Elira is a rebel bard. The Maskbearer never wore a mask.

Selene:
 '"We knew you''d return. The audience awaits."'

Outcome: You live in an echo—a Caelondria that once was. But others begin to hear the song of change again.
');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (226, ' Echoes Reclaimed', 306);

-- 5D: Past Anchor
INSERT INTO dialogs (id, text) VALUES (203, 'Setting: A hidden lounge beneath the Grand Court stage. Selene slumps into a chair, makeup streaked, breath ragged.

Lady Selene:
 "You saw what they wanted: a villain to cheer against. Truth doesn’t sell tickets."

Traveler:
 '"You faltered today. The prophecy haunts you too, doesn’t it?"'

Lady Selene: (rises, eyes hardening)
 '"I am the prophecy, in a way. My blood wrote the curtain of law—but I tire of the script."'

She opens a case—a sealed document written in old seer glyphs.
Selene:
 "This is the original law. It was meant to adapt. Instead, it calcified."');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (203, 'The Hidden Verse', 502);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (203, 'Help her reinterpret the law in secret, using art and performance', 223);


INSERT INTO dialogs (id, text) VALUES (213, 'Together, you rewrite the court’s plays. Trials become open dialogues. Her performances embed subtle new precedents.
Selene (on stage):
 "Justice is not a hammer—it is a mirror. And today… we clean it."
The people begin to repeat her words outside the theater. Court changes begin quietly.

Outcome: A soft revolution begins—truth in metaphor, power in poetry.
');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (213, 'Tides of Choice', 305);

INSERT INTO dialogs (id, text) VALUES (201, 'Setting: Viren and you descend into the ruins beneath the First Tribunal. Ancient laws etched in coral and bone.

Viren (quietly):
 "Justice once meant something else. Before the Scale... there was the Oath of Waters."

You discover that the Scale was created to simulate balance, not define it. Its original purpose was empathic adjustment—but it was corrupted by rigid doctrine.

Traveler:
 "So the prophecy is a warning against calcification."

Viren:
 '"No. It's a cry for rebirth."'');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (201, 'The Hidden Verse.', 502);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (201, 'Decommission the Scale and propose a tribunal system', 221);

INSERT INTO dialogs (id, text) VALUES (221, 'You gather former judges, rebels, and even commoners. Together, you draft a new system of mobile justice.

Viren:
 "It will take years to stabilize."

Traveler:
 "Let’s begin with one voice. One case."

You oversee the first case judged not by law—but by listening.

Outcome: The Aetherial Scale is shelved. Judgment becomes a collective act.
');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (221, 'Tides of Choice', 301);

INSERT INTO dialogs (id, text) VALUES (202, 'Setting: Your lab shimmers with spectral water. Sound and memory twist in harmonic coils.
Suddenly, the energy bursts. Visions of drowned Caelondria flood your senses. The melody is not of sorrow—but of echoes denied.
Elira (rushing in):
 "You weren’t supposed to tap the Source yet!"
Traveler:
 "It’s not just memory. It’s potential… unformed futures."
The vision tears open a pocket reality. You fall into a version of Caelondria where you died—and another took your place.
');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (202, 'The Hidden Verse', 502);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (202, ' Accept the rift—walk both timelines.', 222);

INSERT INTO dialogs (id, text) VALUES (222, 'You step through. One Caelondria where the rebellion never began; another where the Scale never existed.

Alternate Selene (whispering):
 "I remember you. From a dream."

You now drift between both, bringing whispers of truth from one to the other.

Outcome: You become a bridge of possibility.

');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (222, 'Echoes Reclaimed', 306);


INSERT INTO dialogs (id, text) VALUES (204, 'Setting: The court’s central pylon explodes as the Maskbearer tries to act—but Elira’s forces intercept. Chaos—then silence.

You and Elira stand over the collapsed dais. The Maskbearer lies wounded.

Maskbearer:
 "You said you sought truth... and yet you chose to be a weapon."

Elira (firmly):
 "No. We chose when the flood would come. And how many it would drown."

Traveler (quietly):
 "Was there ever a clean path?"
');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (204, 'The Hidden Verse', 502);
INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (204, ' Leave with the Maskbearer and try to reshape the movement underground.', 224);

INSERT INTO dialogs (id, text) VALUES (224, 'You vanish with them into Caelondria’s under-depths. The rebellion lives—but not in flame. In whispers.

Maskbearer (soft):
 "We lost the war... but maybe we can still win the story."

Outcome: A hidden movement reforms—ready for a new truth.


');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (222, 'Echoes Reclaimed', 306);

INSERT INTO dialogs (id, text) VALUES (305, 'Setting: Caelondria now stands in a delicate balance. The Aetherial Scale is dormant, possibly for good. A new tribunal is formed—wandering judges led by you and those you’ve influenced.

Viren steps into the circle, unarmored. Selene carries a new script. Elira sharpens not a weapon—but her words.

Citizens gather under the Rainlight Arches to witness the first true public forum

You and your companions take the Court of the Flood beyond Caelondria’s borders. Tribunals walk from tide to desert, offering judgment based on lived truth.

Lustrines bless your cause.

Maskbearer (appearing once):
 "Perhaps this... was the truth I sought."

Justice becomes a conversation, not a decree. People in foreign lands begin echoing Caelondria’s model

');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (305, 'The Sovereign of Rain', 501);

INSERT INTO dialogs (id, text) VALUES (306, 'Setting: You now walk in multiple timelines—bridging pasts, presents, and potential futures. Only you remember what could have been. Or what almost was.

The Maskbearer follows, spectral now. Elira’s echo hums in one ear; Selene’s poetry lives in your shadow.

The city shifts around you.

Elders speak of dreams where you appear.

Lustrines call this state “The Drownless Drift.

You choose one timeline—perhaps the poetic court, or the rebellion-led city—and sever the bridges.

Your companions do not remember. But they recognize the change in your eyes.

Maskbearer (vanishing):
 "So be it. A single truth is better than a thousand forgotten."

You walk alone for a time, but peace follows you.

');

INSERT INTO choices (dialog_id, choice_text, next_dialog_id) VALUES (306, ' Floodlight Reborn', 501);
-- SEGMENT 6 – FINALE (Endings)

-- Ending 1: The Sovereign of Rain
INSERT INTO dialogs (id, text) VALUES (501, '**Ending 1: The Sovereign of Rain**

Caelondria awakens beneath a sky no longer ruled by prophecy, but by understanding. The Court of the Rainlight, led by you, becomes a living tradition. Wandering judges roam city to desert, mountain to forest, offering empathy in place of edict.

Viren now walks among the people, sharing stories instead of verdicts.

Viren:
 “I thought myself forged by law. But law is a cage unless opened by compassion.”

"Selene performances become rituals of healing—each play retelling a past injustice, then resolving it anew through the audience’s voice.

Elira, if alive, leads the envoy of judges across the continent. If gone, her name is a vow whispered before every case.

The Aetherial Scale, long silent, now rests in a garden where children ask what it once was—and are met not with reverence, but reflection.

You are known not as a savior, but as a listener.
The rain still falls. But it nourishes, not judges.

**THE END**');

-- Ending 2: The Hidden Verse
INSERT INTO dialogs (id, text) VALUES (502, '**Ending 2: The Hidden Verse**

Caelondria thrives under a fragile peace. The courts continue their performances. The people believe justice remains divine. And perhaps it does—in a quiet, softened way.

Only you, and perhaps Lady Selene, remember the truth behind the curtain.

Selene (in a rare moment of honesty):

 “If I’d spoken it aloud, I would’ve destroyed everything. Some truths must sing in silence.”

You serve not as an arbiter, but as a steward of balance. You guide trials gently, nudge verdicts toward kindness. But never speak of the prophecy.

The Maskbearer disappears. Elira becomes myth or memory. Viren... remains in the shadows of the law he once served.

The Aetherial Scale remains untouched—but no longer cruel.

You occasionally visit the coast. The Lustrines sing a verse just for you—one no one else hears:

“When the truth 

**THE END**');

-- Ending 3: Judgment Broken
INSERT INTO dialogs (id, text) VALUES (503, '**Ending 3: Judgment Broken**

The Aetherial Scale shatters with a sound like thunder underwater.

Panic consumes Caelondria. Courts collapse into riots. Judges flee or fall. The Maskbearer declares the prophecy fulfilled and vanishes into firelight.

You survive—barely. You flee through silver-flooded streets, watching once-beautiful towers fall like drowned statues.

Viren is never seen again.

Elira dies leading civilians to safety, unless you’d warned her. If saved, she becomes a voice in the refugee lands, refusing to return.

Lady Selene remains in the Grand Hall, performing as the city burns—her final act unending.

You carry the last memory of what could have been.

The Lustrines sing dirges. They do not intervene.

The world watches Caelondria collapse, and wonders if it too has lied to itself for too long.

**THE END**');

-- Ending 4: Floodlight Reborn
INSERT INTO dialogs (id, text) VALUES (504, '**Ending 4: Floodlight Reborn**

The city does not expand—but it heals.

You and Elira (or her legacy) lead a council of rotating citizens. Cases are judged by those with the clearest hearts, not the highest rank.

Selene’s theatre becomes a public court. Art fuels justice. Lustrine memory flows freely to those who ask—not to command the past, but to understand it.

Viren becomes Caelondria’s first historian of law. “Not to preserve it,” he says, “but to remind us what we overcame.”

The Aetherial Scale rests atop a fountain in the square. Not revered. Not feared.

Children dance around it during festivals.

And when it rains, the people look up—not in fear, but in welcome.

Justice is no longer divine. It is human. And that is enough.

**THE END**

*Thank you for experiencing "The Aetherial Scale." Your choices have shaped not just a story, but a reflection on justice, truth, and the courage to choose imperfection over tyranny.*');
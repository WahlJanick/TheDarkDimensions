# The Dark Dimensions — Version 0.36.0

Eigenständiger Mod für **Minecraft 26.3**, **NeoForge 26.3.0.23-beta** und **Java 25**.

## Unveröffentlicht — Spell-Meisterschaften und eigenes Boss-Skin

Jeder der **39 Spells** hat zwei wählbare Meisterschaften auf **Stufe 5**. Im Spellbook den Zauber anklicken, dann einen seiner zwei kleinen Zweige auswählen. Die erste Wahl ist kostenlos; ein späterer Wechsel kostet im Survival einen Chronus Infused Ingot. Creative kann frei wechseln. Beispiel: Feuerstoß wird zum wandbegrenzten, durchdringenden Glutstrahl oder zum breiten Flammenfächer. Die Wahl gehört dem einzelnen Zauber im jeweiligen Buch, bleibt beim Übertragen und Speichern erhalten und funktioniert auch aus Nebenhand oder Zubehörslot. Ohne gewählte Meisterschaft behalten alte Bücher ihr bisheriges Verhalten.

Die Elementbäume haben längere Ketten und fünf Querverbindungen: Inferno, Heilquelle, Gewitterfeld, Gestohlener Moment und Dimensionsfalte verlangen Fortschritt in zwei Zweigen. Alle fehlenden Voraussetzungen stehen im Hover. Vorhandene erlernte Zauber bleiben erhalten. Das verschiebbare Fenster nutzt weiterhin das originale Advancement-Fenster, Stone-Hintergrund, Vanilla-Frames, Item-Icons und Tooltips; grüne beziehungsweise graue Umrandungen zeigen den Stand. Rechtsziehen verschiebt den Baum, das Mausrad bewegt ihn vertikal. **1–5** weist weiterhin den Spellwheel-Slot zu.

Eine kleine originale Minecraft-Cooldown-Leiste unter dem Fadenkreuz zeigt die gemeinsame Spell-Abklingzeit. Beim Ende blinkt sie kurz XP-grün und ein leiser Kristallton signalisiert Bereitschaft. Ein bereits bereites Buch, Login oder erneutes Ausrüsten löst keinen Bereitschaftston aus. Partikelringe passen zu den tatsächlichen Wirkbereichen; Blutmagie und Bloodgrief benutzen rote Tropfen. Blutung hat eigene fallende Blutpartikel anstelle der Potion-Spiralen und rote Herzen. Schaden, tödlicher Verlauf und Milch-Immunität bleiben erhalten.

Der Ashbound Regent verwendet die eigene editierbare **64×64-PNG** `src/main/resources/assets/thedarkdimensions/textures/entity/ashbound_regent.png`. Ausgangspunkt ist eine unveränderte Kopie der bisherigen Chronus-Mage-Textur. Änderungen an dieser Boss-Datei verändern den Mage nicht und bleiben bei Ressourcen-Generierung erhalten. Generator der neuen Magietexte: `tools/generate_spell_mastery.py`. Alle 78 Optionen stehen in [docs/spell_masteries.md](docs/spell_masteries.md). Für diese Änderungen wurde noch **kein neues Release-JAR** gebaut; die veröffentlichte Version 0.36.0 enthält sie noch nicht.

Validierung: **252 Integrationstests** mit Better Combat und Trinkets Updated bestanden; zusätzlich **18 Meisterschaftstests ohne beide optionalen Mods**. Die Tests prüfen alle 78 Menüentscheidungen, kostenlose Erstwahl und Wechselkosten, echte Item-Speicherung, alte Bücher und ungültige Daten, verkettete Voraussetzungen, Strahl/Fächer mit Wandgrenzen, Markierungsbesitz, Nebenhand und Zubehörbuch, Blitz-Falloff, einmaligen Schutz, Feldreichweite nach Buchwechsel, Ankerheilung, Nachbildablauf, sichere Portaldurchgänge, Cooldown-Bereitschaft und rote tödliche Blutung. Protokolle: `.dev/all-tests-mastery.log`, `.dev/mastery-without-addons.log`; Ressourcenprüfung: `tools/verify_spell_mastery.py`. Visueller Ingame-Check des erweiterten Baums, der HUD-Leiste und der Tropfen steht noch aus.

## Neu in 0.36.0 — Rückenplätze, Gleiter und Creative

Im Creative lernt ein Klick auf einen Spelltree-Knoten den Zauber sofort; weitere Klicks erhöhen seine Stufe bis 5, ohne Einsätze, Vorgänger oder Ingots. Die Fortschritte werden normal im Buch gespeichert. Der Nexus-Altar beschwört im Creative auch außerhalb der Dimension und ohne Sigils einen wiederholbaren Test-Regent. Dieser vergibt keine finale Progressionsbelohnung. Der Regent hat leere Hände; auch beim Laden alter Bosse wird das frühere gehaltene Item entfernt.

Mit **Trinkets Updated** gibt es sechs passende Nebenplätze: Spellbook, Fokus, Talisman, Windstep Sandals neben den Füßen sowie Werkzeug und Gleiter neben der Brustplatte. Die Plätze verwenden vorhandene Minecraft-Symbole beziehungsweise das vordefinierte Gleitersymbol. **X** tauscht Haupthand und Rückenwerkzeug, einstellbar unter Steuerung. Leerhand holt das Werkzeug hervor; ein leerer Rückenplatz verstaut es. Zustand, Name, Verzauberungen und Perks bleiben erhalten. Das Werkzeug wird mit seinem eigenen Itemmodell am Rücken angezeigt; beim Verstauen aktiviert es keine zusätzlichen Kampfattribute. Der Gleiterplatz funktioniert zusammen mit einer Brustplatte. Ohne Trinkets können beide neuen Gleiter weiterhin im normalen Brustslot verwendet werden.

**Wool Glider:** 128 Haltbarkeit, vier statt einem Verschleißpunkt pro Flugsekunde und kein Raketenantrieb. Ein abgenutzter Gleiter bleibt reparierbar; Leder repariert ihn. Rezept: `·S· / SWS / WLW` mit drei normalen Stöcken (S), drei Wolle beliebiger Farbe (W) und einem Leder (L). **Devil Wings:** unzerstörbar und beim Gleiten in Lava gelten Luft-Flugbewegungen. Feuer-/Lavaschaden bleibt ohne separaten Schutz erhalten; die vollständige Blood-Netherite-Rüstung mit Devils Armor schützt weiterhin wie zuvor. Rezept: `SDS / BEB / ·T·` mit zwei Soul Cores, Devil’s Eye, zwei Blood Netherite Ingots, Elytra und Devil’s Tear. Beide verwenden die unveränderten gelieferten 16×16-Item- und 64×32-Flügeltexturen. JEI und native Tooltips erklären die Ausrüstung. Generator: `tools/generate_back_equipment.py`.

Validierung: **234 Server-Integrationstests** mit Better Combat und Trinkets Updated bestanden. Der zusätzliche Start ohne beide optionalen Mods besteht die elf neuen Tests; die fünf reinen Trinkets-Prüfungen werden dabei übersprungen. Native Client-Codecs prüfen beide Itemdefinitionen und Flügel-Equipment-Layer, der Rücken-Renderer-Einstieg wurde gegen die tatsächliche Clientklasse geprüft. Die gelieferten Texturen werden bytegenau und in Originalgröße geprüft. Protokolle: `.dev/all-tests-0.36.0.log`, `.dev/back-without-trinkets-0.36.0.log`, `.dev/models-0.36.0.log`. Ein visueller Ingame-Check der neuen Rückenanzeige steht noch aus.

## Neu in 0.35.1 — Minecraft-Oberflächen

Alle vom Mod eingeführten Menüs verwenden Minecraft-UI-Elemente. Der Spelltable hat einen grauen Vanilla-Containerrahmen, originale 18×18-Slots, einen Minecraft-Rezeptpfeil und eine normale Schaltfläche. Die beiden Eingaben, Ausgabe und das Spielerinventar sind passend zum kompakteren 176×184-Layout angeordnet; Bedienhinweise stehen im nativen Tooltip. Das Spellwheel zeigt seine fünf radialen Plätze als normale Inventar-Slots mit der originalen Hotbar-Auswahlmarkierung. Titel und Beschriftungen verwenden die Minecraft-Schrift und die normalen grauen Container-Textfarben.

Das Spellbook verwendet das originale **252×140-Fortschrittsfenster**, die echten Fortschrittsreiter und den gekachelten Stone-Hintergrund. Die drei Skillreihen passen vollständig ins Fenster. Die zuvor gewünschten grünen/grauen Statusumrandungen und Hover-Anforderungen bleiben vorhanden. Der Runic Binding Bench verwendet bereits den originalen Grindstone-Bildschirm; Blast Machine, Enderchest, Bestiary und Field Journal verwenden native Ofen-, Kisten- beziehungsweise Buchoberflächen.

Die Oberflächen referenzieren Minecraft-Texturen direkt, sodass Resource Packs auch deren Rahmen und Slots ändern können. Neue UI-Texte sind auf Deutsch und Englisch verfügbar. `tools/generate_vanilla_ui.py` aktualisiert ausschließlich diese Texte; die bestehende Ressourcen-Kette ruft ihn am Ende ebenfalls auf. Die dauerhafte Benutzer-Vorgabe für weitere Mod-Menüs steht in `AGENTS.md`.

Validierung 0.35.1: **223 bestehende Server-Integrationstests** mit Better Combat und Trinkets Updated bestanden. Sauberer Release-Build und vollständige JAR-Prüfung bestanden; neue UI-Texte in beiden Sprachen enthalten. Protokolle: `.dev/all-tests-0.35.1.log`, `.dev/release-0.35.1.log`, `.dev/verify-0.35.1.log`. Visueller Ingame-Check steht noch aus.

## Neu in 0.35.0 — Verzweigte Spelltrees

**39 Zauber statt 24:** 15 neue Fähigkeiten erweitern die vorhandenen Bücher. Jeder freigeschaltete Skill kostet weiterhin **einen Chronus Infused Ingot**; der Vorgänger muss die angezeigte Stufe durch erfolgreiche Einsätze erreicht haben. Die Schwellen bleiben bei **8 / 20 / 40 / 70 Einsätzen** für Stufen 2 / 3 / 4 / 5. Vorhandene gelernte Fähigkeiten, Einsätze, Buchidentitäten und fünf Slotbelegungen bleiben erhalten. Auch bereits gelernte Fähigkeiten mit jetzt geänderter Voraussetzung bleiben benutzbar. Die bisherigen 24 IDs werden nicht verschoben; neue Fortschritte verwenden zusätzlich eine 64-Bit-Speicherung.

| Element | Neue Zweige zusätzlich zu den bisherigen Zaubern |
| --- | --- |
| Feuer | Feuerstoß → Glutzeichen → Inferno; Feuerstoß → Aschenschritt → Flammenring. Feuerschutz bildet einen eigenen kurzen Zweig. |
| Wasser | Wasserheilung → Gezeitenstoß → Frostfessel; Wasserheilung → Wasserschleier → Heilquelle beziehungsweise Wasserschutz. |
| Blitz | Blitzschlag → Statische Ladung → Gewitterfeld; Blitzschlag → Blitzschritt → Donnersprung. Kettenblitz ist ein eigener direkter Zweig. |
| Chronus | Dash → Rücksprung → Zeitanker; Dash → Zeitfeld → Trägheitsfeld; Dash → Nachbild → Gestohlener Moment. |
| Ender | Enderchest → Rissschritt → Leerenzug; Enderchest → Enderschutz → Dimensionsfalte. |

Blut-, Seelen- und Elemental-Bücher behalten ihre drei bekannten Zauber. Die exakten Voraussetzungen stehen im Hover: üblicherweise Vorgänger auf Stufe 2, für fortgeschrittene Kontrolle Stufe 3 und für Inferno, Heilquelle, Gewitterfeld, Gestohlener Moment und Dimensionsfalte Stufe 4.

**Bedienung:** Ein Elementbuch zeigt seinen verzweigten Baum. Das Master-Buch hat acht Reiter mit Buch-Icons; alle Elemente teilen sich weiterhin fünf frei gemischte Spellwheel-Slots. Erlernte oder aktuell bezahlbare Knoten sind XP-grün umrandet, andere grau. Namen, Wirkung, Stufe, Einsätze und fehlende Bedingungen erscheinen im umbrochenen Hover. Klicken lernt; über einem Knoten **1–5** drücken weist den Slot zu. Das Spellwheel verwendet die jeweiligen Skill-Icons, damit mehrere Zauber desselben Elements erkennbar bleiben. Öffnen, Spellwheel und Wirken behalten die einstellbaren Tasten sowie Curios-/Trinkets-Slots.

**Kombinationen:** Glutzeichen reagieren auf drei eigene Feuertreffer, auf Stufe 5 auf zwei; Inferno löst eigene Markierungen im Sichtbereich aus. Aschenschritt hinterlässt eine kurze, rein visuelle Bodenspur mit Schaden für Gegner. Statische Ladung priorisiert Kettenblitz-Ziele und ermöglicht Gewitterfelder mit einer Sekunde Vorwarnung. Kettenblitze suchen jeden nächsten Gegner vom letzten Treffer aus und verlieren je Sprung 20 % Schaden; auf Stufe 5 kommt ein Sprung dazu. Wasserschleier schützt vor genau einem Treffer. Heilquellen wirken auf den Zaubernden und verbündete Teammitglieder.

**Zeit und Raum:** Zeitanker setzt einen sicheren Rückkehrpunkt für 12–16 Sekunden; erneut wirken kehrt zurück. Setzen hat eine Sekunde gemeinsamen Cooldown, Rückkehr einen längeren. Nachbild hinterlässt für 2–3 Sekunden eine sichtbare Partikelsilhouette mit temporärem nativem Ablenkungsziel. Das Ziel wird niemals in Chunks gespeichert; Ablauf, Tod oder Dimensionswechsel räumen es auf. Bosse werden nicht umgelenkt. Trägheitsfelder bremsen nahe Gegner und feindliche Projektile. Gestohlener Moment bereitet für acht Sekunden einen Perfect Dodge vor: Der nächste erfolgreiche Zauber erhält 35 % weniger Cooldown, auf Stufe 5 50 %. Die Vorbereitung hat eine Sekunde gemeinsamen Cooldown und zwölf Sekunden interne Erholung; Buchwechsel vervielfacht den Bonus nicht. Leerenzug bündelt Gegner, ohne Bosse zu verschieben. Dimensionsfalte öffnet zwei Durchgänge in derselben Dimension für 6–8 Sekunden, bis zwölf Blöcke entfernt. Auf eine sichere Bodenoberseite zielen und im Ring schleichen; jeder Benutzer hat eine kurze Durchgangssperre gegen sofortiges Zurückspringen.

Alle neuen Zauber haben native Partikel und Elementklänge. Stufe 5 fügt besondere Boni hinzu: Glutzeichen lösen früher aus, Inferno erhält einen kleinen Nachbarimpuls, Aschespuren verlangsamen, Frostfesseln erreichen Nebenziele, Wasserschleier schützt stärker, Heilquelle reinigt einmal, statische Ladung springt auf einen Nachbarn, Gewitterfelder erhalten einen kleinen Nachbarblitz, Zeitanker entfernt Schwäche/Slowness, Dimensionsfalte gibt kurz Tempo. Die übrigen neuen Zauber verstärken Radius oder Kontrollwirkung. Vanilla-Boss-Immunitäten werden berücksichtigt. Wände, gefährliche Böden, Flüssigkeiten, Weltgrenze und abgelehnte Teleport-Events verhindern unsichere Anker-/Portalreisen. Effekte verändern keine Blöcke und laden keine Chunks. Arkane Impulse zählen beim Regent als Magie. JEI erklärt Skillzweige und Kombinationen. Generator: `tools/generate_skilltrees.py`.

Validierung 0.35.0: **223 Server-Integrationstests** mit Better Combat und Trinkets Updated bestanden. 17 neue Tests prüfen die Baumstruktur ohne überlappende Knoten, alte/hohe Skill-IDs und echte Item-Speicherung, Voraussetzungen und Kosten, fünf gemischte Slots, Glutzeichen/Inferno, Frost- und Boss-Immunitäten, einmaligen Wasserschleier, Heilfeld, statische Markierungen und aufeinanderfolgende Kettenblitze, angekündigte Gewitterfelder, Zeitanker mit blockiertem Ziel, Nachbild mit echter Mob-KI und nicht speicherbarem Ziel, Projektilbremsung, tatsächlichen Perfect Dodge mit genau einem Folge-Cooldown-Bonus, sichere gepaarte Durchgänge, Donnersprung-Aufprall, Aschespuren und Teleport-Schutzereignisse. Protokolle: `.dev/all-tests-0.35.0.log`, `.dev/release-0.35.0.log`, `.dev/verify-0.35.0.log`. Visueller Client-Playtest steht noch aus.

## Neu in 0.34.0 — Mindshift und Crimson Rupture

**Mindshifter:** neues formloses Rezept aus **End Crystal + Basic Katana + Shulker Shell**. Die Klinge gehört nun zum End: als Fundwaffe nur in End Cities mit 4 % Chance; die bisherigen Fundorte in der Burned Dimension entfallen. Reparatur mit Shulker Shells. Die Nahkampfwerte bleiben bei 8,5 Schaden, 1,5 Tempo und +2 Reichweite; die früheren passiven Slow-/Weakness-Treffer entfallen.

**Rechtsklick:** ein echtes Shulker-Projektil mit nativer Zielverfolgung, leuchtendem Vanilla-Modell und fünf Basisschaden. Ein erfolgreicher, nicht tödlicher Treffer tauscht die Positionen von Schütze und Gegner. Es wird keine Levitation angewendet. Die Schuss-Abklingzeit beträgt fünf Sekunden, der Verbrauch drei Haltbarkeit. Der Cooldown gehört dem Spieler, bleibt nach Waffenwechsel und Tod erhalten und blockiert das Parieren nicht: **Schleichen + Rechtsklick** pariert mit Mindshifter. Andere Katanas behalten ihre bisherige Bedienung.

Die Zielerfassung reicht 24 Blöcke, der Positionstausch höchstens 32. Beide Körper müssen an den jeweils anderen Platz passen, geladenen Boden haben und innerhalb der Weltgrenze bleiben. Flüssigkeiten, gefährliche Blöcke, Portale, Reittiere und abgelehnte Teleport-Events verhindern den Tausch. Beide Positionen werden vorab geprüft; bei einem abgelehnten Tausch erfolgt keine einseitige Versetzung. Treffer verursachen weiterhin ihren normalen Schaden. Vanilla-Dragon und Wither können nicht versetzt werden; der Regent bleibt innerhalb seiner Arena. Projektile speichern ihren eigenen Entity-Typ und ihr natives Ziel, wodurch sie nach einem Reload keine Vanilla-Levitationskugeln werden. Flugdauer höchstens vier Sekunden.

Zwei kurze optionale PAL-Animationen begleiten Schuss und Tausch: Klinge anheben, nach vorn schnippen und mit einem kurzen Ender-Nachbild wieder stabilisieren. Dazu Portal-/End-Rod-Partikel an beiden Positionen. Ohne PAL bleiben Vanilla-Handbewegung, Projektil und räumliche Effekte vorhanden. Die gelieferten Modelle und Texturen bleiben erhalten.

**Hellsblade – Crimson Rupture:** gewöhnliche Treffer setzen sichtbare rote Risse auf die dem Angreifer zugewandte Körperseite. Bis zu zwei Risse gehören dem jeweiligen Angreifer und verfallen nach vier Sekunden. Der dritte Kombotreffer bricht sie für **2–4 Magieschaden** auf; zwei vorbereitete Risse geben vier Schaden. Der Impuls verbraucht die Risse, wiederholte Callbacks derselben Schwungphase erzeugen weder zusätzliche Ladungen noch weitere Detonationen. Kein eingebautes Anzünden, kein Schaden über Zeit und keine Blockzerstörung. Werte, Rezept, Kistenfundorte, Parieren und Better-Combat-Kombos bleiben erhalten. Generator: `tools/generate_mindshift.py`.

Validierung 0.34.0: alle **206 Server-Integrationstests** mit Better Combat und Trinkets Updated bestanden. Die neun neuen Tests decken Bedienung, gemeinsamen Cooldown/Respawn, Parieren während der Abklingzeit, tatsächlichen nativen Homing-Flug, Schaden und beidseitigen Tausch ohne Levitation, blockierte und abgelehnte Teleports, tödliche Treffer, Entity-Speicherung/Neuladen, Reichweite und Flüssigkeiten, Riss-Eigentümer/Verfall sowie PAL-Animationsladen ab. Die bisherigen Rezept-/Nahkampftests prüfen die neuen Regeln. Sauberer Release-Build und vollständige Paketprüfung bestanden; Originaltexturen/-Modelle bleiben erhalten und Testklassen sind nicht im JAR. Protokolle: `.dev/all-tests-0.34.0.log`, `.dev/release-0.34.0.log`, `.dev/verify-0.34.0.log`. Visueller Client-Playtest steht noch aus.

## Neu in 0.33.0 — Relic Katanas und der Weg ins End

Die drei gelieferten Ordner `Basic Katana`, `Mindshifter Katana` und `Hells Blade` sind integriert. Das Basic Katana verwendet das eiserne Design; alle sieben Materialdesigns bleiben im Quellarchiv erhalten. Modelle, UVs, Ansichts-Transformationen, PNGs und Animation-Metadaten werden in Originalgröße übernommen. Mindshifter und Hellsblade haben ihre gelieferten Nebenhandmodelle, auch bei linkshändigen Spielern. Native Emissionsflächen ersetzen die Resource-Pack-Erweiterung; Hellsblades acht 32×32-Frames laufen mit dem originalen Timing von zwei Ticks. Die beiden übrigen Ordner enthalten statische Modelle/Texturen. CIT/OptiFine ist dafür nicht erforderlich. Generator: `tools/generate_relic_katanas.py`.

| Katana | Schaden / Tempo / Haltbarkeit | Feste Eigenschaft |
| --- | --- | --- |
| Basic Katana | 5 / 1,6 / 480 | Normale Reichweite; Parry, Perfect Parry, Dodge und Slashing. |
| Mindshifter | 8,5 / 1,5 / 1450 | +2 Reichweite, Slowness I für 1,5 Sekunden bei Treffern; dritter Treffer: **Mind Fracture**, vier Sekunden Weakness I. |
| Hellsblade | 9,5 / 1,4 / 1800 | +2 Reichweite, zwei Sekunden Feuer am Ziel; dritter Treffer: **Crimson Discharge**, zwei zusätzliche Magieschaden mit rotem Impuls. |

Alle sieben Katanas haben das bisherige Better-Combat-Kombo- und Parry-System. Zusatzeffekte werden nur bei bestätigten Treffern ausgelöst; mehrere Treffer derselben Schwungphase erzeugen keinen zweiten Impuls. Die beiden festen Spezial-Perks erscheinen unter dem Itemnamen. Slashing/Master Parrying sowie Runen und Luminous Coating funktionieren auf den neuen Klingen ebenfalls.

**Rezeptbasis:** Basic Katana aus zwei Iron Ingots diagonal über einem Improved Stick. Die Anordnung entspricht den bisherigen Katana-Rezepten. Bei Frostgleam, Bloodgrief und Souleater ersetzt ein Basic Katana den bisherigen Improved Stick. Stormveil besteht nun aus Basic Katana + Lightning Core + Solar Essence. Mindshifter: Basic Katana + zwei Chronus Fragments + zwei Chronus Infused Ingots + Ender Pearl, formlos. Hellsblade: Basic Katana + zwei Fire Cores + Redstone Block + Netherite Scrap, formlos.

**Seltene vollständige Fundwaffen:** Mindshifter in Chronus-Sanctum-Kisten mit 4 %, Sunken Archives mit 1,5 %, versteckten Kammern mit 0,5 % und End Cities mit 2 %. Hellsblade in Netherfestungen mit 2 %, Bastionsschatzkisten mit 3 % und Elemental Shrines mit 2,5 %. Pro erfolgreichem Wurf genau eine vollständige Waffe; gewöhnliche Kistenbeute bleibt erhalten. Der seltene Beute-Klang greift beim Aufheben.

**Verbindliche Progression:** Nether → Burned Dimension → Cinder Crucible, Sunken Reliquary und Luminous Hollow → Enderaugen → End. Der Astral Crystal Wand wird vor dem End aus **Fire Core / Ghast Tear / Improved Stick** senkrecht hergestellt. Nach Abschluss aller drei unterschiedlichen Prüfungen ist die Enderaugen-Herstellung dauerhaft für die gesamte Welt freigeschaltet, auch kooperativ und über Dimensionswechsel/Serverneustarts hinweg. Ein Enderauge benötigt **Ender Pearl + Blaze Powder + Essentialium**. Crafting Table und Crafter prüfen dieselbe Weltfreischaltung; der Crafter behält kein veraltetes negatives Rezept im Cache. Frühere abgeschlossene Prüfungen werden aus vorhandenen Spieler- und Offline-Belegen übernommen. Field Journal, JEI-Hinweise und ein neuer Fortschritt erklären den Weg. Die drei Sigils bleiben weiterhin für den Resonance Nexus erhalten.

Validierung 0.33.0: alle **197 Server-Integrationstests** mit Better Combat und Trinkets Updated bestanden. Die neun neuen Tests prüfen Rezeptbasis, feste Werte, Reichweite und Verzauberungen, die tatsächlichen Drei-Treffer-Procs, Parieren, seltene Beute in nativen Kisten, Crafting-/Crafter-Sperre, dauerhafte Weltfreischaltung und alte Offline-Belege. Native Client-Codecs prüfen drei Itemdefinitionen, fünf Modelle, Handvarianten, Emissionsflächen und das Original-Animationstiming. Release-Build und vollständige Paketprüfung bestanden; Quelltextur-Bytes und ursprüngliche Modellgeometrie/UVs/Ansichten stimmen mit dem Archiv überein. Keine Testklassen im JAR. Protokolle: `.dev/all-tests-0.33.0.log`, `.dev/models-0.33.0.log`, `.dev/release-0.33.0.log`, `.dev/verify-0.33.0.log`. Ein visueller Playtest im laufenden Minecraft-Client steht noch aus.

## Neu in 0.32.0 — The Last Resonance

**Treffer- und Belohnungsgefühl:** Perfect Parry, Perfect Dodge, alle vier Runen-Procs und Prüfungsabschlüsse haben kurze eigene Sounds und Partikel. Sounds verwenden Minecraft-Klänge, normale Lautstärkeregler und Untertitel. Ein kurzer persönlicher Klang begleitet seltene/epische aufgehobene Beute, Trial Sigils und Solar Essence. Mehrfachmeldungen innerhalb derselben Aktion werden gedrosselt. Abschlussklänge sind persönlich, damit Gruppen sie nicht mehrfach übereinander hören. Better Combat verwendet beim dritten Schlag aller vier Katanas den kräftigeren Finisher-Sound; die bisherigen Ausholanimationen, Trefferfenster und Schadenswerte bleiben erhalten. Ohne Better Combat funktionieren bestätigte Parry-, Runen- und Belohnungseffekte ebenfalls.

**Finale Arena: Resonance Nexus.** Selten in Ash Wastes, Charred Forest, Echo Fields und Crystal Hollows. Das versiegelte Eingangstor oder der zentrale Resonance Altar nehmen die drei Trial Sigils nacheinander entgegen, nachdem der jeweilige Spieler die passende Prüfung abgeschlossen hat. Sigils werden nicht verbraucht; die drei Einstimmungen und das offene Eingangstor werden dauerhaft gespeichert. Das Field Journal am Altar speichert den Ort. Danach mit leerer Hand anklicken: vier Sekunden Vorwarnung, dann erscheint **The Ashbound Regent**. Spieler in der Arena treten beim Start bei; später können sie am Altar beitreten.

| Boss-Telegraph | Reaktion |
| --- | --- |
| Blauer Ausholhieb | Parierbar; Perfect Parry bringt den Boss ins Taumeln. |
| Roter, auf dem Boden fixierter Kreis | Wegbewegen oder Dodge. Der Riss umgeht Parieren und normale Schilde. |
| Goldene Schutzphase | Physischer Schaden um 70 % verringert; Magie bricht den Schutz sofort und öffnet vierzig Ticks lang ein Fenster mit 20 % mehr Schaden. Der Schutz endet auch ohne Magie nach kurzer Zeit. |

Der Regent hat drei Phasen und 260 Lebenspunkte, plus 60 für jeden zusätzlichen anwesenden Spieler bis maximal 440. Seine Angriffe bleiben angekündigt und werden in der letzten Phase schneller. Hiebe verursachen vor Rüstung und Schwierigkeit 10/12/14 Schaden, Risse 14/16/18. Er bleibt im Arenabereich. Nach 15 Sekunden ohne lebende Teilnehmer wird der Versuch aufgeräumt; Tor und Einstimmungen bleiben erhalten. Entladene Chunks pausieren den Controller, statt die Welt dauerhaft geladen zu halten. Laufende Kämpfe speichern Boss-UUID und Teilnehmer; nach einem Reload bleibt derselbe Boss mit seinem gespeicherten Leben bestehen.

**Belohnung:** jeder Teilnehmer erhält einmal pro Welt ein Regent’s Heart, einen Fortschritt und dauerhaft **Resonant Step**. Ein Perfect Dodge gibt dann Tempo I für drei Sekunden, mit acht Sekunden Abklingzeit. Kein zusätzlicher Konterschlag oder Rohschaden. Fähigkeit und Abklingzeit bleiben nach Tod und Dimensionswechsel erhalten. Offline-Belohnungen werden gespeichert und beim nächsten Login zugestellt. Ein abgeschlossener Nexus startet keinen weiteren Boss; weitere Nexus-Arenen duplizieren keine persönlichen Belohnungen.

**Kleine Entdeckungen:** verlassene Dark-Acacia-Lager mit kaltem Lagerfeuer und Vorräten; kleine versteckte Schatznischen in bestehenden Höhlen; beschädigte Brücken, die nur bei tatsächlich vorhandenem Geländespalt entstehen; seltene Calcite-/Amethyst-Formationen mit leuchtenden Spitzen. Platzierungen prüfen vorher den gesamten kleinen Grundriss und ersetzen keine belegten Blöcke oder Flüssigkeiten. Beute liefert normale Materialien und selten Chronus-Fragmente, keine Sigils oder Boss-Freischaltungen. Neue Orte entstehen in neu generierten Chunks. Arena und neue Block-/Item-Modelle verwenden vorhandene Texturen in Originalgröße.

Playtest: `/locate structure thedarkdimensions:resonance_nexus` innerhalb der Burned Dimension; dort drei Sigils am Eingangstor oder Altar einstimmen. Die Prüfungserfolge sind weiterhin Voraussetzung. Optik, Lautstärke und Kampfgefühl benötigen zusätzlich einen Client-Playtest.

Validierung 0.32.0: alle **188 Server-Integrationstests** mit Better Combat und Trinkets Updated bestanden, davon 17 neue für Finale, Belohnungen und Entdeckungsorte. Zusätzlich bestehen alle sechs Regent-Modellposen jeweils am Anfang, in der Mitte und am Ende sowie die bisherigen Mob-Modellprüfungen. Minecraft-Sound-Aliase und native Modelltexturen wurden gegen die lokalen 26.3-Ressourcen geprüft. Ressourcen-Generator: `tools/generate_finale.py`.

Sauberer Release-Build und vollständige Paketprüfung von `thedarkdimensions-0.32.0.jar` bestanden; keine Server- oder Modelltestklassen im JAR. Prüfprotokolle: `.dev/all-tests-0.32.0.log`, `.dev/models-0.32.0.log`, `.dev/release-0.32.0.log`, `.dev/verify-0.32.0.log`.

## Neu in 0.31.0 — Ancient Resonance

Drei seltene Prüfungsstätten verbinden Höhlen, Waffen-Perks und Magie. Jede hat drei unterschiedliche Grundrisse mit zufälliger Ausrichtung. Ein eigener nativer Structure-Typ sucht vor dem Platzieren nach einem tatsächlich vorhandenen Höhlenboden mit mindestens drei Luftblöcken darüber und dem passenden Höhlenbiom. Bestehende Atmosphäre, Höhlengeneratoren, Dark Acacia, Hard Stone und Mob-Skins bleiben erhalten.

| Prüfung | Fundort / Aktivierung | Ablauf |
| --- | --- | --- |
| Cinder Crucible | Fire Cave / Fire Core | Drei Siegel, danach drei angekündigte Wellen mit Fire Mage, Ashen Wraith und zuletzt Cinder Broodmother. Angekündigte Flammenkreise verursachen vier Feuerschaden. |
| Sunken Reliquary | Water Cave / Water Core | Drei Siegel in verbundenen Kammern, danach zwei Sentinel-Wellen. Trockene Wege, erhöhte Lufttaschen und kurze angekündigte Wasserimpulse; Respiration ist keine Voraussetzung. |
| Luminous Hollow | Glowing Cave / Lightning Core | Fünf Lichtzeichen mit ein, zwei oder drei sichtbaren Strichen, Ortsmarkierung, Tonhöhe und Text. Fehler beschwören höchstens drei gleichzeitige Wraiths; richtige Schritte bleiben erhalten. |

**Start:** am Dormant Runestone mit dem passenden Core interagieren. Erster Klick zeigt den Verbrauch, ein zweiter innerhalb von fünf Sekunden bestätigt. Ein Core je Prüfungsort; Wiederholungsversuche kosten keinen weiteren. Benachbarte Spieler nehmen beim Start teil; weitere Spieler können dem laufenden Versuch am Runenstein beitreten. Gegnerzahl steigt moderat und bleibt begrenzt. Nach zehn Sekunden ohne anwesenden lebenden Teilnehmer wird der Versuch zurückgesetzt; Siegel, richtige Lichtschritte und vollständig besiegte Wellen bleiben erhalten. Eine unvollständige Welle wird erneut gestartet.

**Speicherung und Beute:** BlockEntity-Daten speichern Identität, Phase, Siegel, Lichtschritte, Wellen, Teilnehmer und Gegner-UUIDs. Entladene Arenen werden nicht dauerhaft geladen gehalten. Ein zusätzliches SavedData-Ledger speichert besiegte Gegner, ausstehende Offline-Belohnungen und Beute-Belege. Jeder Ort lässt sich nur einmal abschließen; pro Element und Spieler gibt es insgesamt genau ein Trial Sigil. Jeder beteiligte Spieler erhält eigene Materialien, offline gegangene Teilnehmer beim nächsten Login. Prüfungsgegner geben keine separate Beute oder Erfahrung; dadurch lassen sich Fehlversuche nicht farmen. Ashfall erhöht nur die gewöhnliche Materialbeute, niemals Sigils oder Freischaltungen.

**Runic Binding Bench:** zwei Eingaben und ein Ergebnis, mit dem nativen Grindstone-Hintergrund. Rezept `ERE / MSM / HHH`: E = Essentialium, R = Empty Runestone, M = Metalinium Ingot, S = Spelltable, H = Hard Stone. Eine Waffe plus freigeschaltete Rune ergibt dieselbe Waffe mit genau einer Rune. Name, Verzauberungen, Verschleiß, Luminous Coating und andere Komponenten bleiben erhalten. Eine bereits gebundene Rune lässt sich nicht überschreiben. Waffe + Chronus Orb entfernt die Rune, verbraucht einen Orb und gibt die Rune zurück; bei vollem Inventar wird nur die zurückgegebene Rune fallengelassen. Eingaben werden erst bei der tatsächlichen Entnahme verbraucht; Schließen gibt ausschließlich Eingaben zurück.

| Rune | Formloses Rezept, jeweils zusätzlich Empty Runestone | Wirkung / eigener Cooldown |
| --- | --- | --- |
| Ember | Fire Core + Soul Essence | Nahkampftreffer entzündet kurz / 6 Sekunden |
| Tide | Water Core + Japanese Essence | Nahkampftreffer löscht eigenes Feuer, heilt nicht / 8 Sekunden |
| Radiance | Glowing Honey + Essentialium | Leuchtendes Ziel drei Sekunden um 15 % langsamer / 6 Sekunden |
| Chronus | Chronus Orb + Chronus Infused Metalinium Ingot | Perfect Dodge verkürzt einen laufenden Chronus-Cooldown um eine Sekunde / 10 Sekunden |

Crafting und Binden werden serverseitig durch die entsprechende Prüfung freigeschaltet, Chronus durch alle drei. Sigils allein lassen sich nicht als Fortschrittsersatz handeln. Cooldowns gelten pro Angreifer, auch nach Waffenwechsel; Flächentreffer können denselben Effekt nicht mehrfach auslösen. Radiance läuft nach Luminous Coating, sodass die Kombination bereits beim gleichen Treffer wirkt. Chronus benötigt einen ausgewählten, erlernten Chronus-Zauber und einen zuletzt tatsächlich gewirkten Chronus-Zauber; Buchwechsel reduziert keine Feuer-/Wasser-/anderen Element-Cooldowns. Gemeinsame Buch-Cooldowns bleiben konsistent und werden nie negativ. Die Nebenhand-Abfrage liest den gespeicherten Inventarslot, damit Better Combats Zweihand-Darstellung das Spellbook nicht ausblendet.

**Ashbound Field Journal:** formlos Book + Ashes + Empty Runestone. Normale Minecraft-Buchansicht, getrennt vom Bestiary: entdeckte Gebiete/Höhlen, gefundene Prüfungen, Abschlussstatus, Runen und ein nächster sinnvoller Schritt. Unbekannte Prüfungen zeigen nur Hinweise. Mit dem Journal am Runenstein interagieren, um bewusst einen Wegpunkt zu speichern; alternativ in der Nähe schleichen und das Journal öffnen. Gespeicherte Punkte zeigen Dimension und Koordinaten. Keine automatische Karte und keine Minimap-Pflicht. Fortschritt und Wegpunkte sind Spieler-Attachments mit Copy-on-Death und bleiben bei Dimensionswechsel erhalten.

**Ashfall:** Runensteine und Eingangsmarkierungen senden bei nahen Spielern begrenzte zusätzliche Partikel. Lichtzeichen werden deutlicher, aktive Feuer-/Wasserprüfungen haben etwas häufigere angekündigte Umgebungsereignisse. Die Prüfungen sind bei jedem Wetter abschließbar. Effekte ändern keine Umgebungsblöcke; Prüfungsprojektile können keine Blöcke entzünden.

Die neuen Modelle verwenden vorhandene 16×16-Texturen und native Geometrie. Es werden keine vorhandenen PNGs skaliert oder Mob-Texturen ersetzt. DE/EN-Texte, native Crafting-/Rezeptbuch-Anzeigen für JEI/EMI und zusätzliche JEI-Hinweise sind enthalten. Generator: `tools/generate_ancient_resonance.py`.

Validierung: alle **171 Server-Integrationstests** mit Better Combat und Trinkets Updated bestanden, davon 13 neue Tests für Ancient Resonance. Geprüft sind unter anderem vollständige Einzelspieler-Prüfungen, kooperative Skalierung mit Offline-Teilnehmer, BlockEntity-/Ledger-Neuladen, Wiederholungsversuche, Weltgenerierung an Höhlenböden, tatsächliche Crafting-Sperren, Inventartransaktionen, Waffenerhaltung, Perfect Dodge und Journal-Fortschritt nach Tod und Dimensionswechsel. Release-Build und Paketprüfung für `thedarkdimensions-0.31.0.jar` bestanden; Testklassen sind nicht enthalten. Ein visueller Test im laufenden Minecraft-Client ist noch nicht erfolgt.

Für den Playtest in neu generierten Bereichen der Burned Dimension:

```text
/locate structure thedarkdimensions:cinder_crucible
/locate structure thedarkdimensions:sunken_reliquary
/locate structure thedarkdimensions:luminous_hollow
```

## Vorherige Änderungen in 0.30.5

**Warrior Axes:** Normale Äxte aller sieben Vanilla-Stufen lassen sich mit Warrior Axe Upgrade Template + Warrior Stick am Schmiedetisch umwandeln. Das neue Template ist craftbar mit vier Iron Ingots, zwei beliebigen Blood (Creeperblood oder Zombieblood), zwei Leather und einem Improved Stick: `IBI / LSL / IBI`. Direkte Rezepte bleiben für Holz bis Diamond erhalten. Netherite Warrior Axe ist nur über Smithing erhältlich: entweder aus einer normalen Netherite Axe mit Warrior-Template oder aus einer Diamond Warrior Axe mit Vanilla Netherite Upgrade Template + Netherite Ingot. Verschleiß, Name und Verzauberungen bleiben erhalten.

**Magiebücher:** Blank Spellbook ist formlos aus Book + Empty Runestone + Metalinium Ingot craftbar. Ein Core am Spelltable bestimmt erst das Element. Master Spellbook: `ECE / CBC / MCM`, E = Essentialium, C = Elemental Core, B = Blank Spellbook, M = Chronus Infused Metalinium Ingot. Vier Cores, zwei Essentialium und zwei Infused Ingots ergeben ein Buch ohne voreingestelltes Element; weitere Elemente werden am Spelltable gebunden.

**Stormveil und Hammer:** Stormveil braucht nur Solar Essence + Lightning Core + Improved Stick. Thundergod’s Hammer: `DBD / LSL / _S_`, D = Diamond, B = Diamond Block, L = Lightning Core, S = Improved Stick. Nur die Awakened-Version ermöglicht Gleiten, in Haupt- oder Nebenhand. Awakening bleibt Lightning Core als Template + Hammer + Lightning Spellbook.

**Zombieblood:** Ein Drop mit 5 % Chance bei allen Zombie-Unterklassen (auch Mod-Zombies), Zombie Horse, Zombie Nautilus und Zoglin. Blood Essence akzeptiert ein Creeperblood oder Zombieblood + Gold Ingot; das zusätzliche Doppelrezept nimmt zwei beliebige Blood + zwei Gold Ingots für zwei Essences, auch gemischt. Andere Blutrezepte behalten ihre bisherigen Zutaten.

Validierung: alle 158 Server-Integrationstests mit Better Combat und Trinkets Updated bestanden, einschließlich aller Zombie-Varianten, gemischtem Blood-Crafting, sieben Warrior-Smithing-Stufen, Netherite-Komponentenerhaltung und Awakened-only-Gleiten. Der bestehende Höhlen-Spawntest hatte im ersten Gesamtlauf einen intermittierenden Fehler; isoliert und im erneuten Gesamtlauf bestanden. Ein visueller Minecraft-Client-Test ist noch nicht erfolgt.

Ressourcen regenerieren mit `tools/generate_progression_rebalance.py`. Template-Icon: 16×16, generiert mit dem eingebauten Imagegen-Werkzeug; Original, finale PNG und Bildvorgabe liegen in `tools/texture_sources/progression_rebalance/`.

## Vorherige Änderungen in 0.30.4

**Hollow Watcher:** Der originale Slenderman-Skin speichert den weißen Kopf als RGB-Weiß mit Alpha 0. Der Renderer verwendet dafür jetzt den opaken RGB-Pass, ohne die PNG zu verändern. Der bisher verwendete Eyes-Skin ist vollständig schwarz und opak; dieser Overlay-Layer ist entfernt. Das unbenutzte innere Hutmodell wird ausgeblendet. So bleiben Kopf und Anzug sichtbar.

**Sentinel und Whisperer:** Die sichtbaren Sentinel-Gelenke übernehmen alle drei Rotationsachsen der nativen Skelettbewegung, einschließlich korrekter Beinphasen und Zauberhaltung. Der Whisperer verwendet die ursprünglichen Kopf-/Blattgleichungen mit Zeit in Sekunden, native Illager-Beinbewegungen und Armkompensation; Zaubern hebt beide Arme wie beim Evoker. Bonewings ursprünglicher 45-Grad-Schädel wird nicht mehr zusätzlich mit der Flugneigung verdreht.

**Veilwood Spider und Modellbasis:** Die Kampfgestalt bleibt entsprechend dem Originalmodell zehn Sekunden nach Aggression erhalten, statt bei jedem Umschalten eines Nahkampf-Ziels zurückzuspringen. Alle Pack-Modelle erzeugen ihre Vanilla-Gelenke direkt aus den nativen Mesh-Definitionen. Dadurch benutzt der Import keine möglicherweise bereits von anderen Modellmods ersetzte Vanilla-Layer. Texturen und Quellgeometrie bleiben unverändert; ETF/EMF sind weiterhin keine Voraussetzung.

Validierung: native Modelltests mit zusätzlichen 24 Geh-/Zauber-/Flugneigungs-Frames für Sentinel, Whisperer und Bonewing; Prüfung des Watcher-Skins, des opaken Render-Passes und seiner sechs Körperteile. Modellvorschauen von vorn und oben kontrolliert. Ein visueller Test im laufenden Minecraft-Client ist noch nicht erfolgt. Build und Paketprüfung für `thedarkdimensions-0.30.4.jar`.

## Vorherige Änderungen in 0.30.3

**Pack-Mob-Modelle:** Die vorige UV-Prüfung verglich individuelle CEM-Flächen irrtümlich mit Minecrafts Box-UV-Flächen. Das ließ einen echten Formatfehler passieren: CEM `uvUp`/`uvDown` und `uvWest`/`uvEast` gehören auf andere physische Flächen; die horizontalen Flächen beginnen außerdem an anderen Ecken. Der Import berücksichtigt jetzt die CEM-Zuordnung, die ursprüngliche Eckreihenfolge und UV-Spiegelungen einschließlich Seitenwechsel. Geometrie, Texturen, Pixelgröße und originale UV-Rechtecke bleiben unverändert.

**Körpervarianten:** Der Import verwendet ausschließlich die im Pack ausdrücklich angegebenen Sichtbarkeitsregeln. Dune-Stalker-Beine heißen im Original zwar `leg*_chill`, haben aber keine Aggro-Umschaltung; sie bleiben jetzt auch im Kampf sichtbar. Der Kopf wechselt weiterhin. Bei der Veilwood-Spinne bleibt der gemeinsame Kopf-Drehknoten sichtbar, während Kopf und Beine korrekt ihre Varianten wechseln. Sentinel-Wasser-/Trockenvarianten bleiben getrennt.

**Bewegung und Ursprung:** Die drei Spinnen-/Skorpion-Modelle bekommen Minecrafts echte achtbeinige Spinnenbewegung statt eines humanoiden X-Achsen-Schwungs. Bonewing und Lantern Wisp nutzen die native Phantom-Hierarchie und Flügel-/Schwanzbewegung. Der Phantom-Renderer-Ursprung wird übernommen; Bonewing berücksichtigt auch die native Größe und Flugneigung. Der Wisp gleicht die tatsächliche Kopfrotation für seine Billboard-Fläche aus. Fire Mage und Chronus Mage bleiben auf dem unveränderten Vanilla-Illager-Modell mit ihren originalen 64×64-Skins.

Validierung: alle acht importierten Modelle in normalen und Kampfzuständen nativ gerendert; beide Mages in verschränkter und zaubernder Pose. Neue unabhängige Prüfungen kontrollieren CEM-Flächen und alle vier Eck-UVs für ungespiegelte, U-, V- und UV-gespiegelte Flächen, dünne Flügel, Sichtbarkeitswechsel sowie Spinnen-/Phantom-Bewegung über zwölf wechselnde Frames und Flieger-Ursprünge. Vorder-/Rückansichten reproduzierbar mit `tools/render_mob_model_previews.py` nach `runMobModelTests -PmodelTests`. Ein visueller Test im laufenden Minecraft-Client ist weiterhin nicht erfolgt. Formatreferenz: [EMFs CEM-Flächenimplementierung](https://github.com/Traben-0/Entity_Model_Features/blob/master/src/main/java/traben/entity_model_features/models/parts/EMFModelPartCustom.java).

## Vorherige Änderungen in 0.30.2

**Improving Templates** lassen sich nur duplizieren. Das erste findet man in Netherfestungs-Kisten (20 %) oder Bastionskisten (20 %, Schatzkisten 35 %). Die ursprüngliche Crafting-Rezeptur und die Fundstelle im Elementarschrein sind entfernt. Duplizieren bleibt: sieben Diamonds + vorhandenes Template + Crying Obsidian → zwei Templates. Die Nether-Beute wird zusätzlich zur normalen Vanilla-Beute eingefügt.

**Bloodgrief** benötigt insgesamt zwei Creeperblood: pro Blood Essence jetzt ein Creeperblood + ein Gold Ingot, weiterhin mit Water Core zu Blood Core kombinieren; zwei Blood Cores und Improved Stick ergeben die Klinge.

**Burning Stick:** nur Flint and Steel + normaler Stock, formlos. Das Feuerzeug kommt mit drei zusätzlichen Haltbarkeitspunkten Verschleiß zurück. Name und Verzauberungen bleiben erhalten; bei erschöpfter Haltbarkeit zerbricht es nach diesem Craft. Das Rezept hat die normale Rezeptbuch-Anzeige und Platzierung für Rezeptanzeiger.

**Blood Netherite Ingot:** vier Creeperblood + zwei Netherite Ingots → ein Blood Netherite Ingot, formlos.

Aktuelle Ressourcen regenerieren mit `tools/generate_progression_rebalance.py`.

Validierung: alle **155 Integrationstests** mit Better Combat und Trinkets Updated bestanden. Neue Prüfungen decken echte Netherkisten mit unveränderter normaler Beute, fehlendes Initial-Crafting und funktionierende Template-Duplikation sowie Rezeptbuch-Anzeige, Feuerzeug-Rückgabe, Komponenten-Erhaltung und Zerbrechen bei erschöpfter Haltbarkeit ab. Bestehende Katana- und Blood-Netherite-Rezepttests prüfen die günstigeren Mengen. Der lange Chronus-Mage-Dash-Test hält seinen Testchunk nun unabhängig von benachbarten Strukturtests aktiv; Mob-Spielverhalten ist unverändert.

## Vorherige Änderungen in 0.30.1

**Warrior Stick:** eigenes 16×16-Icon mit grauem Schaft ähnlich dem Improved Stick, dunkelroter Griffwicklung und grauem unteren Ende. Die drei Rottöne stammen direkt aus dem originalen Warrior-Axe-Griff. Das bisherige durchgehende Braun-Tinting ist entfernt. Imagegen-Vorgabe: grauer Schaft, teilweise rote Griffwicklung, transparente Minecraft-Pixelgrafik; das Ergebnis wurde auf native Pixelgröße reduziert und an die Originalpalette angepasst.

**Normaler Katana-Parry:** verhindert den vollständigen parierbaren frontalen Nahkampftreffer und gibt kein Konterfenster. Das gilt auch mit Master Parrying. Ein neuer angenommener Parry-Versuch räumt alle früheren Konter- und Master-Parrying-Gelegenheiten auf; ein normaler Parry kann kein restliches Fenster eines älteren Perfect Parry übernehmen. Perfect Parry und Perfect Dodge behalten ihre eigenen Konterregeln, Master Parrying wirkt weiterhin ausschließlich nach Perfect Parry.

Validierung: alle 38 Katana-Integrationstests mit Better Combat und Trinkets Updated bestanden, einschließlich zwei neuer Tests für vollständigen Schutz ohne Master-Parrying-Konter und das Entfernen alter Perfect-Parry-Fenster. Die Release-Prüfung kontrolliert Originalgröße, Transparenz, graue Pixel, die passende rote Axtpalette und das entfernte globale Tinting. Aktuelle Ressourcen regenerieren mit `tools/generate_warrior_stick.py`.

## Vorherige Änderungen in 0.30.0

**Katana-Enchantments:** Frostgleam, Bloodgrief, Souleater und Stormveil unterstützen **Slashing I–III** anstelle von Sharpness, Smite, Bane of Arthropods, Fire Aspect, Knockback und Sweeping Edge. Der native Schadensbonus beträgt +1,5 / +2 / +2,5, exakt wie Sharpness II / III / IV. Unbreaking, Mending, Looting und passende Utility-Enchants bleiben möglich. Die Beschränkung gilt am Enchanting Table und am Amboss. Alte Sharpness-Enchants werden im Inventar zu Slashing umgestellt (Level minus eins, begrenzt auf I–III); die anderen gesperrten Kampfenchants werden entfernt. Verschleiß, Coating und Utility-Enchants bleiben erhalten.

**Master Parrying:** eine seltene Katana-Verzauberung mit genau einer Stufe. Sie ist nur im Enchanting Table erhältlich, ausschließlich bei Angeboten mit 30 Level Kosten. Enchantability erhöht diese Voraussetzung nicht; Bücher sind ebenfalls auf echte 30-Level-Angebote beschränkt. Kein Handel und keine zufällige Beute. Das über den Table erhaltene Enchanted Book kann am Amboss verwendet werden.

Nach einem Perfect Parry besteht **30 % Chance auf einen zweiten Konter**. Erst wenn der erste Konter tatsächlich Schaden verursacht und das Ziel noch lebt, wird der zweite bereit. Bei nativen Angriffen geschieht das erst nach Ende des ersten Angriffs, sodass mehrere Schadensereignisse desselben Schlages den Bonus nicht mehrfach verwenden. Beide Konter bleiben im ursprünglichen Ein-Sekunden-Fenster und gelten gegen denselben Angreifer. Ein Fehlschlag verbraucht die Gelegenheit; ein dritter Bonus ist ausgeschlossen. Perfect Dodges erhalten weiterhin maximal einen Konter.

**Stick-Texturen:** Der Improved Stick (`reinforced_stick`) hat jetzt vollständig neutrale Graustufen. Originale 16×16-Größe, Pixelpositionen und Transparenz bleiben erhalten. Der Burned Stick (`improved_stick`, historischer gespeicherter Item-ID) behält die bisherige braune Textur. Aktuelle Ressourcen regenerieren mit `tools/generate_katana_enchantments.py`.

Die [Rezeptprüfung](RECIPE_REVIEW.md) beschreibt Kostenlücken und konkrete Verbesserungsvorschläge. Vier ausgewählte Änderungen sind seit 0.30.2 umgesetzt.

Validierung: alle **150 Integrationstests** mit Better Combat und Trinkets Updated bestanden. Zwölf neue Prüfungen decken alle vier Katanas, die nativen Enchanting-/Anvil-Regeln, exakte Sharpness-Schadensäquivalenz, den echten 30-Level-Filter einschließlich Bücher, Gewicht und Erwerbsquellen, Migration bestehender Waffen, die 30-%-Wahrscheinlichkeit, echte zweite Konter einschließlich nativer Angriffs-Callbacks, Fehlschläge und Ablauf sowie den fehlenden Bonus nach Perfect Dodge ab. Die Release-Prüfung kontrolliert Enchantment-Daten, Mixins und vollständig entsättigte Stick-Pixel bei erhaltener Originalgröße und Alpha.

## Vorherige Änderungen in 0.29.0

**The Ashen Sovereign:** ein persistenter Katana-Boss mit 120 Leben, eigener Bossleiste und drei Kampfphasen. Er verwendet unverändert die originale `illager/vindicator.png` aus deinem Texturepack auf Minecrafts nativem Illager-Modell; sein Souleater wird mit den normalen Hand-Transforms gehalten. Blau angekündigte Hiebe sind parierbar. Rote schwere Angriffe umgehen Deckung und müssen ausgewichen werden. Erfolgreiches Parieren unterbricht ihn und öffnet das bestehende Ein-Schlag-Konterfenster. Unter 80 und 40 Leben wird er schneller und setzt häufiger schwere Hiebe ein. Spätere Phasen enthalten eine kurze frontale Deckung; flankieren oder seinen nächsten Hieb abwarten.

Der Boss bewacht den neuen **Ashen Dojo** in Ash Wastes, Charred Forest und Veilwood der Burned Dimension. Die verwitterte Ruine besitzt eine offene Kampffläche, Dark-Acacia-Bauteile, Soul-Lanterns, eine Beutekiste und lesbare Duellhinweise. Pro Dojo erscheint genau ein Boss. Er despawnt nicht mit Entfernung und wird nach einem Sieg nicht neu erzeugt. Neue Dojos entstehen in neu generierten Chunks.

Beute: garantiert ein abgenutzter **Souleater**, **2 Soul Cores**, **8–16 Ashes**, dazu Erfahrung und das Challenge-Advancement „Das letzte Duell“. Beobachtungen und tatsächliche Drops werden im Bestiarium aufgezeichnet.

Zum Testen in der Burned Dimension: `/locate structure thedarkdimensions:ashen_dojo`. Zum direkten Beschwören: `/summon thedarkdimensions:ashen_sovereign`; außerdem gibt es ein Spawn-Ei im Kreativtab. Alle aktuellen Ressourcen werden mit `tools/generate_katana_boss.py` regeneriert. Ein visueller Playtest im laufenden Minecraft-Client steht noch aus.

Validierung: alle 138 Integrationstests mit Better Combat und Trinkets Updated bestanden. Die neuen Tests prüfen native Boss-Attribute und Phasenwechsel, angekündigten echten Trefferschaden, Perfect Parry und Konter, nicht parierbare schwere Angriffe, Dodges, echte Beute und Advancement sowie native Dojo-Platzierung und Jigsaw-Generierung. Der aus Struktur-NBT geladene Boss behält Katana, Leben und das ausgeschaltete Ausrüstungsdroppen. Die native Modellprüfung besteht für alle sechs Boss-Haltungen jeweils zu Beginn, in der Mitte und am Ende der Bewegung sowie für die acht bisherigen Pack-Modelle. Die Release-Prüfung sichert die bytegenaue originale Boss-Textur, Struktur-Ressourcen, Biome, Beute und Übersetzungen.

## Vorherige Änderungen in 0.28.0

**Runestone-Crafting:** Ein **Leerer Runestone** entsteht aus einem Diamond in der Mitte und vier Stone oben, unten, links und rechts. Im Kreativtab ist er als native Steintafel sichtbar. Die Vanilla-Texturen behalten ihre ursprünglichen 16×16-Pixel.

| Ergebnis | Mitte | Zutaten außen | Anordnung |
| --- | --- | --- | --- |
| Leerer Runestone | Diamond | 4 Stone | Kreuz |
| Fire Core | Leerer Runestone | 8 Blaze Powder | Vollständiger Ring |
| Water Core | Leerer Runestone | 4 Water Buckets | Kreuz |
| Lightning Core | Leerer Runestone | 4 Lightning Rods | Kreuz |

Alle vier Wassereimer geben beim Craften je einen leeren Eimer zurück. Lightning Rods können jede Oxidationsstufe haben, auch gewachst; gemischte Varianten funktionieren im selben Rezept. Die bisherigen Fire-Core- und Lightning-Core-Rezepte werden ersetzt. Bestehende Core-Items und Mob-Drops bleiben erhalten. Regeneriere alle aktuellen Ressourcen mit `tools/generate_core_recipes.py`.

Validierung: alle 131 Integrationstests mit Better Combat und Trinkets Updated bestanden. Neue Prüfungen decken die exakten Rezepte, fehlende/falsche Zutaten, das Entfernen alter Rezepte, vier Eimer-Rückgaben sowie alle acht Blitzableiter-Varianten und gemischte Varianten ab. JEI zeigt zusätzliche Hinweise für Runestone und alle drei Cores; die nativen Crafting-Rezepte werden automatisch erkannt.

## Vorherige Änderungen in 0.27.0

**Ein-Schlag-Konter:** Nach Perfect Parry oder Perfect Dodge bleibt maximal eine Sekunde für genau einen Angriffsversuch. Ein Fehlschlag, ein Angriff auf einen anderen Gegner oder ein geblockter/unverwundbarer Treffer verbraucht die Gelegenheit ebenfalls. Der +3-Schadensbonus und der thematische Effekt können nur einmal gegen den auslösenden Angreifer wirken, auch wenn Better Combat mehrere Treffer im selben Servertick auswertet. Die Bereitschaftsmeldung verschwindet beim Verbrauch oder Ablauf.

**Portalzugang nach End Cities:** Der Astral Crystal Wand wird jetzt senkrecht aus **Dragon Head → End Crystal → Improved Stick** gecraftet. Den Drachenkopf holst du von einem End-City-Schiff. Das alte Rezept mit Flint and Steel ist entfernt; Fire Cores aktivieren weiterhin keine Portale. Der Wand verbessert aktive Netherportale oder aktiviert einen vollständigen leeren Obsidianrahmen und hält 64 Aktivierungen.

**Importierte Mob-Modelle:** Der native CEM-Importer erhält jetzt die Phantom-Hierarchie von Kopf, Flügeln, Flügelspitzen und Schwanz. Spinnen-Gelenke bewegen sich nur einmal, statt gleichzeitig Gelenk und Geometrievarianten zu drehen. Bei der Broodmother werden Beine nur einmal auf die Originalskalierung verkleinert. Gespiegelte einzelne Texturflächen behalten ihre UV-Ausrichtung und Flächennormalen; abweichende Texturmaße einzelner Modellteile werden berücksichtigt. Sentinel-Arme drehen sich am ursprünglichen Gelenk, Whisperer-Arme erhalten ihre ursprüngliche Ruhehaltung. Kopfbewegungen und leichte Körperbewegungen werden auf die vorgesehenen Teile angewendet. Alle acht Pack-Geometrien, Texturen und ihre ursprünglichen Größen bleiben erhalten.

Validierung: alle 128 Integrationstests mit Better Combat und Trinkets Updated bestanden, zusätzlich alle 13 Katana-Verteidigungstests ohne optionale Mods. Alle acht Modelle sind in Ruhe und Kampf über Minecrafts native ModelParts geprüft und als Texturvorschau gerendert. Zusätzliche Prüfungen sichern die Phantom-Hierarchie, einmalige Bewegung/Skalierung und UVs/Flächennormalen gespiegelter Würfel einschließlich eigener Teil-Texturmaße. Die Release-Prüfung vergleicht alle acht Geometrien einschließlich Koordinaten, Drehpunkten und UV-Rechtecken mit den Originalen. Ein visueller Playtest im laufenden Minecraft-Client steht noch aus. Formatreferenz: [OptiFine CEM-Teile](https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/cem_part.txt).

## Vorherige Änderungen in 0.26.0

**Katana-Parieren und Ausweichen:** Frostgleam, Bloodgrief, Souleater und Stormveil unterstützen ein Timing-System mit normalen und perfekten Aktionen. Die Auswertung läuft auf dem Server und funktioniert auch mit Better Combat.

| Aktion | Steuerung und Timing | Wirkung |
| --- | --- | --- |
| Deckung | Rechtsklick halten, Treffer nach dem Parry-Fenster | 60 % weniger frontaler Nahkampfschaden; kostet Haltbarkeit |
| Parry | Rechtsklick weniger als 8 Ticks vor dem Treffer | Stoppt einen frontalen Nahkampftreffer und unterbricht kurz die Nahkampfangriffe des Gegners |
| Perfect Parry | Treffer innerhalb der ersten 3 Ticks nach Rechtsklick | Längere Unterbrechung, blaue Funken und Konterfenster |
| Dodge | Linke Alt-Taste plus Bewegungsrichtung; ohne Bewegung rückwärts | Vier kurze Schritte, insgesamt etwa 2,6 Blöcke, mit 8 Ticks Schutz gegen gegnerische Treffer |
| Perfect Dodge | Ein gegnerischer Treffer wird innerhalb der ersten 2 Dodge-Ticks vermieden | Goldene Funken und Konterfenster gegen den Angreifer |

Ein Tick entspricht bei normaler Servergeschwindigkeit 50 ms. Das Konterfenster hält zwei Sekunden: Beende die Deckung und treffe denselben Angreifer mit dem Katana. Der nächste erfolgreiche Nahkampftreffer bekommt +3 Schaden und einen thematischen Effekt: Frostgleam verstärkt Frosted, Bloodgrief verlängert Blutung, Souleater verstärkt Desorientierung und Stormveil erzeugt einen kleinen Kettenblitz. Der Bonus wird einmal verbraucht; andere Gegner profitieren nicht davon.

Ravager, Iron Golem, Warden und Cinder Broodmother sind nicht parierbar. Rote Partikel markieren solche Gegner, wenn sie dich angreifen. Katana-Deckung hält auch keine Projektile auf; dafür ist Ausweichen gedacht. Schaden durch Fallenstürze, Lava, Feuer-Dauerwirkung und andere Umweltquellen bleibt bestehen. Schweres Geschoss- und Nahkampftiming lässt sich durch Dodges vermeiden.

Die Dodge-Taste ist unter **Steuerung → The Dark Dimensions – Kampf** einstellbar. Die Parry-Erholung dauert 12 Ticks, Dodge-Erholung 35 Ticks. Halten erneuert kein Perfect-Fenster. Waffenwechsel setzt die Erholung nicht zurück. Dodges brauchen Boden, freie Kollisionsfläche und mindestens zwei Hungerpunkte; abgewiesene Aktionen verbrauchen keine Erholung. Beim Ablegen des Katanas oder Dimensionswechsel endet der Schutz. Windstep kann nicht mit einem aktiven Dodge gestapelt werden.

Mit Player Animation Library sind kurze Parry- und vier Sidestep-Animationen enthalten. Die Deckung benutzt zusätzlich Minecrafts normale Blockhaltung. Es gibt native Partikel, positionierte Sounds und kurze Meldungen über der Hotbar; kein eigenes Kampfmenü. Es wird keine globale Zeitlupe ausgelöst. Neue Ressourcen regenerieren mit `tools/generate_katana_defense.py`. Ein visueller Playtest im laufenden Client steht noch aus.

Validierung: alle 124 Integrationstests mit Better Combat und Trinkets Updated bestanden, zusätzlich alle neun Katana-Verteidigungstests ohne optionale Mods. Geprüft sind Treffer-Timing, echte Schadensereignisse, thematische Konter, frontale Deckung und rückwärtige Treffer, schwere Gegner und Projektile, Mummy-Treffereffekte, Dodge-Bewegung, Kollisionen, Hunger, Waffenwechsel und Erholung. Mit PAL laden alle fünf neuen Animationen über den nativen Animationsloader. Die Release-Prüfung kontrolliert Ressourcen, Übersetzungen und das Fehlen gebündelter Test- oder PAL-Klassen.

## Vorherige Änderungen in 0.25.0

**Chronus Sanctum:** halbverschüttete Ruine in den Oberflächenbiomen der Burned Dimension. Eine kleine Essentialium-Geode, schwebender Kristallschmuck, leise Kristallklänge, ein versiegelter Seitenraum und echte Pfeilfallen führen zum **Sanctum Keeper**. Der stärkere Chronus Mage hat 44 Leben, Zeitprojektile, einen sicheren Dash und ein sichtbar angekündigtes Zeitfeld. Chests liefern garantiert 1–2 Chronus Fragments; der Keeper hinterlässt 2–3 Fragmente und einen Chronus Orb. Lesbare Aufzeichnungen geben Hinweise auf die weitere Reise.

**Sunken Archive:** unterirdische Schatzkammer in Water Caves mit Prismarine, Wasserbecken, zwei persistenten Drowned Sentinels, lesbarer Aufzeichnung und zwei Chests. Water Cores liegen mit 20 % Chance in jeder Chest, neben Lapis, Prismarine, Eisen und gelegentlich Essentialium oder einem blanken Spellbook.

Die Strukturen unterstützen `/locate structure thedarkdimensions:chronus_sanctum` und `/locate structure thedarkdimensions:sunken_archive`. Sie generieren in neuen Chunks; führe die Befehle in der Burned Dimension aus.

**Timeworn Hourglass:** Chronus-Fokus mit 15 % kürzeren Cooldowns für Dash, Zeitfeld und Rücksprung. Wirkt in der Nebenhand oder im Focus-Slot von Curios/Trinkets Updated. Stapelt nicht mit dem Astral Prism. Rezept im Crafting Table:

```text
--- Glass ---
Chronus Fragment | Chronus Infused Metalinium Ingot | Chronus Fragment
--- Improved Stick ---
```

**Ashbound Bestiary:** shapeless aus einem Book, einer Ashes und einer Feather. Rechtsklick öffnet die normale Minecraft-Buchansicht. Beobachte Kreaturen in Sichtweite bis 24 Blöcke Entfernung, um Lebensraum und Angriffe zu erfassen. Drops erscheinen erst nach beobachteter Beute, eigene Kills werden gezählt. Die persönlichen Aufzeichnungen bleiben nach dem Tod erhalten; jedes Öffnen aktualisiert das Buch. Vierzehn Kreaturen einschließlich Sanctum Keeper sind enthalten.

**Fortschritt:** elf eigene Advancements begleiten die Reise von Ashes über Portal, Dimension, Geoden und Ruinen bis zu Zaubern, Mini-Bossen, Hourglass und vollständigem Bestiarium.

**Mob-Begegnungen:** Angriffshaltungen verwenden jetzt synchronisierte Entity-Daten, sodass die Kampfpose auch auf Clients stimmt. Chronus Mage und Drowned Sentinel kündigen Projektile 15 Ticks vorher mit Pose und Sound an; der Briar Whisperer hebt beim Wurzelangriff die Arme. Die bestehenden seltenen Spawngewichte und lokalen Gegnerlimits bleiben aktiv.

Ressourcen regenerieren mit `tools/generate_exploration.py`. Die Texturen des Nutzers bleiben unverändert. Neue Item-Icons sind 16×16. Visuelles Playtesting im laufenden Client steht noch aus.

Validierung: alle 115 Integrationstests mit Better Combat und Trinkets Updated bestanden. Neue Prüfungen decken native Strukturgenerierung, Loot, lesbare Lore, echte Pfeilfallen, persistente Wächter, Bestiarium-Speicherung und echte Kill-Ereignisse, Advancements, Fokus-Rezept und Trinkets-Integration sowie das Chronus-Zeitfeld ab.

## Vorherige Änderungen in 0.24.0

Zehn Originaltexturen aus **Other Mob textures** sind unverändert übernommen. Der vorhandene Fire Mage nutzt jetzt `evoker.png` auf Vanilla-Illager-Geometrie. Neun zusätzliche Mobs haben eigene Spawn-Eier, Attribute, Angriffe und Beute. Acht eigene Formen aus dem Pack werden als native Minecraft-Modelle geladen; OptiFine ist dafür nicht nötig. Pack-Credits bleiben in den Modelldateien erhalten. Pack-Animationen werden durch passende Lauf-, Flügel-, Kampf- und Sichtbarkeitsanimationen des Mods ersetzt.

| Mob | Spawngebiet | Verhalten und Beute |
| --- | --- | --- |
| Fire Mage | Fire Caves | Bestehende Feuerangriffe und seltene Fire Cores; neue Evoker-Optik |
| Chronus Mage | Bei Essentialium-Geoden in der Burned Dimension | Langsame Zeitprojektile, kurze Verlangsamung, sicherer Rückwärts-Dash mit Cooldown; Ashes und 40 % Chronus Orb |
| Ashen Wraith | Oberfläche der Burned Dimension, häufiger bei Ashfall | Schwebender Nahkämpfer; 1–3 Ashes |
| Bonewing | Selten über offenen Flächen der Burned Dimension | Kreist und kündigt Sturzangriffe hörbar an; unabhängig von Schlafmangel; Bones und selten Phantom Membrane |
| Veilwood Spider | Veilwood | Kleine Giftspinne mit drei Sekunden Gift; String und gelegentlich Spider Eye |
| Drowned Sentinel | Water Caves | Wasserprojektile; Bones und 8 % Water Core |
| Cinder Broodmother | Selten in Fire Caves | Feuerfester Mini-Boss mit 60 Leben; Flammenkreis warnt vor einer örtlichen Eruption; ein Fire Core |
| Lantern Wisp | Glowing Caves | Friedlicher schwebender Lichtgeist, weicht Spielern aus; kein Kill-Loot |
| Briar Whisperer | Overworld-Dschungel und Sümpfe | Sichtbar angekündigter Wurzelangriff mit kurzer Verlangsamung; Vines und Glow Berries |
| Dune Stalker | Overworld-Wüsten im Dunkeln | Skorpion mit drei Sekunden Gift; Ashes und gelegentlich Spider Eye |

Die Broodmother spawnt einzeln und mit geringer Gewichtung; eine weitere entsteht nicht innerhalb von 64 Blöcken. Zusätzliche Dimensions-Spawns beachten Schwierigkeit, Mob-Spawn-Gamerules, lokale Gegnerlimits und geladene Chunks. Der Hollow Watcher bleibt auf einen Spawn pro Waldgebiet beschränkt, auch wenn dort jetzt Spinnen leben.

Alle neuen Mobs lassen sich zum Playtesten über ihre Spawn-Eier im Kreativmenü oder `/summon thedarkdimensions:<mob_id>` aufrufen. Beispiele: `chronus_mage`, `ashen_wraith`, `bonewing`, `cinder_broodmother`, `lantern_wisp`, `briar_whisperer`, `dune_stalker`.

Ressourcen regenerieren mit `tools/generate_mob_expansion.py`. Native Modellprüfungen: `gradlew runMobModelTests -PmodelTests`. Die Meshes wurden zusätzlich lokal als Texturvorschau gerendert; ein visueller Playtest im laufenden Minecraft-Client steht noch aus.

Validierung: 109 vollständige Integrationstests mit Better Combat und Trinkets Updated bestanden, plus der zusätzliche Test für Chronus-Dash, freie Landeposition und Cooldown. Alle acht importierten Modelle sind im Ruhezustand und im Kampfzustand geprüft. Release-JAR-Prüfung kontrolliert Originaltexturen bytegenau, Spawngebiete, Modelle und das Fehlen von Testklassen.

## Vorherige Änderungen in 0.23.0

**Veilwood** ist ein dichter Wald in der Burned Dimension: hohe Dark-Acacia-Bäume, überlappende unregelmäßige Kronen, dunkles Licht und dichter Nebel. Einzelne Waldgebiete sind räumlich getrennt. Beim Erkunden erscheint genau ein **Hollow Watcher** pro Gebiet; der Spawn wird gespeichert und nach seinem Tod nicht wiederholt. Seine Originaltexturen bleiben unverändert auf dem Vanilla-Enderman-Modell. Er hinterlässt 2–4 Ashes und mit 25 % Chance einen Chronus Orb. Neue Waldgebiete erscheinen in neuen Chunks. Finden mit `/locate biome thedarkdimensions:veilwood`.

Katana-Finisher zeigen passende Partikel und Sounds: Frostgleam Eis und Schnee, Bloodgrief weinrote Schnittspuren, Souleater Seelenflammen, Stormveil elektrische Funken. Die bestehenden Combo-Effekte und Schadenswerte bleiben erhalten. Devil’s Charm zeigt jetzt einen bösen grünen Emerald. Burned Hoglin und Burned Pigman wurden entfernt.

102 Integrationstests mit Better Combat und Trinkets Updated bestanden, einschließlich Biome-Codec, Waldgrenzen, Nebel und gespeichertem Einmal-Spawn. Die Darstellung im laufenden Client wurde noch nicht visuell geprüft. Ressourcen regenerieren mit `tools/generate_veilwood.py`, damit auch die Entfernung alter Mob-Ressourcen angewendet wird.

## Vorherige Änderungen in 0.22.0

Der dritte Katana-Treffer nutzt einen zweihändigen horizontalen Hieb mit größerer Körperdrehung beim Ausholen und schärferem Übergang zum Schlag. Die Griffhaltung und Handpositionen stammen zur Laufzeit aus der installierten Better-Combat-Animation; auch Ressourcenpaket-Overrides werden beim Reload berücksichtigt. Die ersten beiden Schläge und die bestehenden Combo-Boni bleiben erhalten.

Das Spellbook-Menü zeigt ausschließlich einen Skilltree im Stil der Minecraft-Advancements: Vanilla-Item-Icons, Stein-Hintergrund, graue beziehungsweise XP-grüne Rahmen und Verbindungslinien. Ein Element-Buch zeigt seine drei aufeinander aufbauenden Skills; das Masterbuch zeigt alle acht Elementzweige. Erlernte Skills und Skills mit erfüllten Voraussetzungen einschließlich vorhandenem Chronus Infused Ingot sind grün, andere grau. Hover zeigt Name, Wirkung, fehlendes Element, erforderliche Vorgänger-Stufe und Materialkosten. Bei erlernten Skills zeigt der Tooltip Stufe und Fortschritt. Einen lernbaren Skill anklicken; über einem erlernten Icon **1–5** drücken, um ihn einem Spellwheel-Slot zuzuweisen. Alternativ zuerst das Icon anklicken, dann eine Zahl drücken. Keine permanenten Lern-, Element- oder Slot-Buttons und kein sichtbares Inventar.

Alle 24 Zauber haben passende Vanilla-Partikel und positionierte Sounds. Beispiele: Flammenstoß und Feuerringe, Wasserheilung und Wasserwellen, sichtbare Verbindungen beim Kettenblitz, violette Chronus-Spuren und Rücksprung, weinrote Blutentzug-Linien, Seelenflammen, Ender-Partikel und gemischte Element-Effekte. Bewegungszauber zeigen ihre tatsächlichen Start-/Endpunkte; Kettenblitze folgen den getroffenen Gegnern. Effekte werden vom Server an Spieler in Reichweite gesendet und erst nach einem erfolgreichen Zauber ausgelöst. Blockierte Zauber erzeugen keine Partikel oder Sounds. Partikelzahlen sind begrenzt.

99 Integrationstests mit Trinkets Updated und Better Combat bestanden. Dazu gehören das Parsen der angepassten nativen Katana-Animation mit allen ursprünglichen Bone-Kanälen und ein echter Netzwerk-Test für Partikel-/Sound-Pakete bei erfolgreicher Heilung sowie fehlende Effekte bei blockiertem Zauber. Skill-Menü und Animation wurden noch nicht visuell im Client geprüft.

## Spielen

Das JAR aus `build/libs` in den `mods`-Ordner einer passenden NeoForge-Installation kopieren. Auf einem Server brauchen Server und Clients den Mod. Neue Biome erscheinen in neu generierten Chunks; eine neue Testwelt ist empfehlenswert.

- **Mummies:** wickelbandartige Untote mit deiner Originaltextur. Spawnen in Vanilla-Wüsten anstelle von Husks und droppen 1–3 Ashes, mit Looting mehr. The Burned, Water Husk und Black Desert sind entfernt.
- **Burning Stick:** shapeless aus 1 Feuerzeug und 1 Stock. Im Wasser wird er zum Burned Stick. Das Feuerzeug kommt mit drei Punkten Verschleiß zurück und zerbricht erst bei erschöpfter Haltbarkeit. Der Stock zeigt Flammen; in Haupt- oder Nebenhand verursacht er einmal pro Sekunde 1 Punkt Feuerschaden. Kreativmodus und Feuerresistenz schützen wie üblich.
- **Improved Stick:** Burned Stick + normaler Stock, formlos.
- **Verstärkte Diamantwerkzeuge:** normale Diamantrezepte mit Improved Sticks statt normalen Stöcken. Schwert, Spitzhacke, Axt, Schaufel und Hacke behalten Diamantwerte und bekommen in der Haupthand **+2 Blöcke Abbau- und Angriffsreichweite**. Die Reichweite verschwindet beim Wechseln des Items.
- **Fire Core:** Crafting-Tisch-Rezept mit 8 Blaze Powder um einen leeren Runestone:

  ```text
  B B B
  B R B
  B B B
  ```

  `B` = Blaze Powder, `R` = Leerer Runestone.
- **Improved Nether Portal:** Rechtsklick mit dem Astral Crystal Wand auf einen vollständigen leeren Obsidianrahmen oder die aktive Portalfläche. Aktiviert die gesamte Fläche und erhält ihre Ausrichtung. Der Wand hat 64 Aktivierungen Haltbarkeit; ein Fire Core aktiviert kein Portal mehr. Ein beschädigter Rahmen lässt das Portal zerfallen.

## Burned Realm

### Normale Höhlen, Geoden und Spelltable (0.5.0)

Neben den drei Elementarhöhlen gibt es **Normal Caves**, einen neutralen Untergrundtyp mit natürlicher Minecraft-Höhlenform. Neben Vanilla-Erzen und ihren Hard-Stone-Varianten bleiben diese beiden magischen Rohstoffe erhalten:

- **Essentialium Gem Crystals:** dunkelblau eingefärbte originale Amethyst-Cluster. Sie entstehen in Geoden zwischen Y −52 und 24, mit Basalt-/Calcit-Hülle, Essentialium-Innenwand und Budding Essentialium. Die Geoden verwenden Minecrafts Geodenalgorithmus, im Mittel ein Platzierungsversuch je 16 Chunks. Knospen wachsen über drei Stufen zum vollständigen Cluster; der Budding-Block lässt sich wie Vanilla Budding Amethyst nicht abbauen. Ein voller Cluster gibt mit Spitzhacke vier Essentialium, sonst zwei. Fortune und Silk Touch funktionieren.
- **Chronus Orb Ore:** schwarzes Loch in verbranntem Stein, eigene 16×16-Textur. Erzadern zwischen Y −56 und 16, sieben Platzierungsversuche je Chunk, Größe fünf. Mit mindestens einer Eisenspitzhacke erhält man Chronus Orbs; Fortune erhöht die Menge, Silk Touch erhält den Erzblock. Ein Chronus Orb und ein Metalinium Ingot ergeben einen Chronus Infused Metalinium Ingot.

Burned Coal Ore, Burned Iron Ore, Improved Amethyst Crystal Ore und zufällige Ember-Stone-Erzadern generieren nicht mehr. Ember Stone bleibt als Fire-Cave-Material erhalten. Die alten Erz-IDs bleiben für vorhandene Spielstände registriert, sind aber im Kreativtab ausgeblendet. Bereits erzeugte Erzblöcke werden nicht automatisch aus alten Chunks entfernt. Improved Amethyst ist weiterhin über sein Crafting-Rezept erhältlich.

**Spelltable:** drei Essentialium, zwei Bücher und vier normale Obsidianblöcke:

```text
E E E
B O B
O O O
```

Rechtsklick öffnet die Zauberauswahl: Spell Book, ein Lapis und einen Chronus Infused Metalinium Ingot einlegen und einen der vier Zauber wählen. Die Zutaten werden jeweils einmal verbraucht; es kostet keine Level. Beim Schließen erhält man übrige Zutaten und ein noch nicht abgeholtes Ergebnis zurück.

Die festen Modblöcke sind außerdem in Minecrafts 26.3-Tags für bewegungsblockierende Blöcke aufgenommen. Ohne diese Tags meldete die Worldgen-Höhenkarte den verbrannten Boden als leer und verhinderte normale Erzplatzierungen. Der Integrationstest prüft jetzt die Höhenkarte und tatsächlich gesetzte Chronus-Erzblöcke.

### Elementarhöhlen und Schrein (0.4.0)

In neuen Chunks gibt es drei Untergrundbiome mit Minecrafts natürlicher Noise-Höhlengenerierung, Cave-/Deep-Cave-Carvern und Schluchten. Seit 0.4.1 werden keine identischen Kammern oder geraden Durchgänge mehr gebaut: Dekoration färbt ausschließlich die vorhandenen Höhlenwände und Böden und verteilt Materialien, Kristalle und Bewohner zufällig. Dekoration reicht ungefähr von Y −60 bis 40.

Fire-, Glowing- und Water-Crystals verwenden die originale Amethyst-Cluster-Textur und deren Form, orange, gelb bzw. blau eingefärbt. Sie wachsen an allen sechs Seiten, sind wasserloggbar und fallen ohne Halteblock ab. Sämtliche Mobs verwenden die wirklichen Vanilla-Modelle samt Original-PNGs und unveränderten UV-Rastern; Farbfilter erzeugen die Elementar- bzw. Burned-Farben. Baby- und Bee-Zustandsvarianten bleiben Vanilla-konform.

- **Fire Cave:** rötlicher Fire Cave Stone, leuchtende Fire Crystals und schädlicher Ember Stone. Der feuerfeste **Fire Mage** schießt kleine Feuerbälle; beim Tod dropt er mit **10 %** Wahrscheinlichkeit einen Fire Core und außerdem Ashes.
- **Water Cave:** blaugrauer Water Cave Stone, Water Crystals und Wasserbecken. Mummies droppen hier zusätzlich zu Ashes mit 15 % Wahrscheinlichkeit einen Water Core.
- **Glowing Cave:** goldener Glowing Cave Stone, Glowing Crystals, viel Glowstone und leuchtende Honigblöcke. **Glowing Bees** droppen einen Glowing Honey. Erwachsene, friedliche Bienen lassen sich auch mit einer Glasflasche ernten, einmal alle 30 Sekunden. Trinken gibt **60 Sekunden Glowing**, Nahrung und eine leere Glasflasche zurück.
- **Elementarschrein:** seltene oberirdische Ruine mit drei Elementarkristallen, Runenstein-Pfeilern, einer Beutetruhe und einem Fire Mage als Wächter. Die Truhe kann Honig, Improved Amethyst und Fire/Water Cores enthalten. Finden: `/execute in thedarkdimensions:burned_realm run locate structure thedarkdimensions:elemental_shrine`.

**Lightning Core:** ein normaler Blitzableiter direkt über einer Improved Echo Shard, beide in derselben Spalte im Crafting-Tisch. Ergibt einen Lightning Core.

**Elemental Core:** zwei Lightning Cores, zwei Fire Cores und zwei Water Cores beliebig auf sechs Felder verteilen. Ergibt einen Elemental Core für die Zaubermechanik.

Die beiden nachgereichten Diamond-Pickaxe-/Shovel-Texturen sind unverändert übernommen. Alle acht neuen Höhlen-/Schreinblöcke, vier Items und drei Bewohner haben passende Texturen. Neue Inhalte sind im Kreativtab; Standardrezepte werden von JEI/EMI erkannt.

Das Improved Nether Portal führt jetzt in das **Burned Realm**. Das automatisch erzeugte Ankunftsportal führt zur Ausgangsdimension zurück. Portalposition und Ausgangsdimension werden pro Entity gespeichert; auch die Reise aus dem Nether wird unterstützt. Portale haben die normale Aufwärmzeit und einen Cooldown. Bei dichtem Wald oder steilem Gelände wird nötigenfalls eine erhöhte Landeplattform erzeugt.

- **Landschaft:** Overworld-artige Berge, Täler, Gewässer und Noise-Höhlen zwischen Y −64 und 319. Vanilla-Grass, Dirt, Gravel, Stone und Bedrock bilden das Gelände. Unter Y 0 ersetzt Hard Stone Deepslate; zwischen Y 0 und 8 liegt der natürliche Übergang. Heller aschgrauer Himmel und Nebel, Aschepartikel und dunkleres Gras.
- **Drei Biome:** Ash Wastes, Charred Forest mit verbrannten Bäumen und Crystal Hollows mit leuchtenden Kristallsäulen. Kristalle können auch in Höhlen wachsen.
- **Hollow Watcher:** ein dauerhafter Wächter pro Veilwood-Gebiet, mit Ashes und gelegentlichen Chronus Orbs als Beute.
- **Rohstoffe:** Essentialium-Geoden und Chronus Orb Ore ersetzen die früheren Erzvorkommen; Details oben.
- **Glutadern:** Ember Stone leuchtet und verursacht wie Magmablöcke Schaden beim Darüberlaufen. Schleichen schützt.
- **Baumaterialien:** Burned Logs ergeben vier Burned Planks; zwei Planks übereinander ergeben vier Improved Sticks. Planks funktionieren auch in Vanilla-Rezepten wie Werkbank und Truhe. Burned Cobblestone funktioniert für Steinwerkzeuge und Öfen; Einschmelzen ergibt Burned Stone.

## Echo-Portal und neue Rezepte

**Improved Amethyst:** eine Amethystscherbe in die Mitte, einen Diamanten darunter und sieben Glasscheiben in alle übrigen Felder:

```text
P P P
P A P
P D P
```

`P` = Glasscheibe, `A` = Amethystscherbe, `D` = Diamant. Alternativ aus dem Kristallerz abbauen.

**Improved Echo Shard:** acht Improved Amethyst um eine normale Echoscherbe:

```text
A A A
A E A
A A A
```

`A` = Improved Amethyst, `E` = Echoscherbe.

Im Burned Realm entstehen **Portalruinen** mit einem vollständigen Improved-Obsidian-Rahmen und einem leuchtenden **Echo Relic**. Das Relikt mit einer Diamantspitzhacke abbauen, um eine normale Echoscherbe zu bekommen; mit Silk Touch bleibt es als Relikt erhalten. Improved Obsidian ist im Survivalmodus unzerstörbar und besitzt weder Item noch Rezept.

Rechtsklick mit dem Improved Echo Shard auf den gefundenen Rahmen aktiviert das **Echo Portal** und verbraucht eine Scherbe. Erneutes Klicken verbraucht keine weitere. Das Portal führt in die **Echo Depths**, ein erstes begehbares Kristallreich mit dunklem Terrain und zusätzlichen Kristallsäulen. Sein Ankunftsportal führt zurück ins Burned Realm. Normale Netherportale behalten ihr übliches Ziel.

Für den Playtest die alte Mod-JAR durch `thedarkdimensions-0.8.1.jar` ersetzen. Texturen und Rezepte funktionieren auch in bestehenden Welten. Neue Biome, natürliche Elementarhöhlen und Schreine erscheinen in neu generierten Chunks; vorhandene Gebiete werden nicht rückwirkend verändert. Eine neue Testwelt vermeidet alte Biome an neu berechneten `/locate`-Positionen.

## Schmiedetisch und Speere

Alle sechs Werkzeugarten (Schwert, Spitzhacke, Axt, Schaufel, Hacke und Speer) unterstützen diese drei Wege:

| Schablone | Basis | Zusatz | Ergebnis |
|---|---|---|---|
| Improving Template | normales Diamantwerkzeug | Improved Stick | Improved Diamond Tool |
| Improving Template | normales Netheritewerkzeug | Improved Stick | Perfected Netherite Tool |
| normale Netherite-Upgradeschablone | Improved Diamond Tool | Netheritebarren | Perfected Netherite Tool |

Die bisherigen Diamant-Craftingrezepte mit Improved Sticks bleiben verfügbar. Der Diamantspeer wird mit einem Diamanten rechts oben und zwei Improved Sticks diagonal nach links unten gecraftet. Beim Schmieden bleiben Verzauberungen, Name und absolute Abnutzung der Basis erhalten. Verbesserte und perfektionierte Werkzeuge bekommen weiterhin genau +2 Blöcke Reichweite in der Haupthand. Netheritevarianten haben normale Netheritewerte und sind feuerfest.

**Improving Template finden:** Netherfestung (20 %) oder Bastion (20 %, Schatzkiste 35 %). Es gibt keine Rezeptur zum erstmaligen Craften.

Eine vorhandene Schablone kann wie eine Vanilla-Schablone dupliziert werden: `DTD / DOD / DDD`, mit sieben Diamanten, einer Improving Template und einem weinenden Obsidian; ergibt zwei Schablonen.

**Double Spear am Schmiedetisch:** Improving Template links, Perfected Netherite Spear in der Mitte und Improved Diamond Spear rechts. Alle drei Inputs werden verbraucht. Verzauberungen, Name und Abnutzung des Netheritespeers bleiben erhalten; die Werte des Diamantspeers werden nicht zusätzlich übertragen.

- 8 Punkte unmodifizierter Stichschaden statt 5 beim Netheritespeer.
- Kinetischer Schadensmultiplikator 1,5 statt 1,2.
- Verzauberbarkeit 25 statt 15.
- Native Speer-Angriffsreichweite 6,5 Blöcke im Survivalmodus und 8,5 im Kreativmodus; jeweils +2 gegenüber dem normalen Speer. Die Mindestdistanz bleibt 2 Blöcke. Zusätzlich +2 Abbaureichweite beim Halten.

## Texturen

Die bereitgestellten Texturen aus `TheDarkDimensionsResouces` wurden unverändert übernommen und passend zu den bestehenden Item-IDs benannt. Die Datei `double_spear.png` war intern eine Paint.NET-Datei mit zwei Ebenen; sie wurde als echte PNG mit Transparenz exportiert. Das Original bleibt unter `tools/texture_sources/double_spear_original.pdn` erhalten. Alle Speere haben eigene Inventar- und In-Hand-Texturen mit Minecrafts nativer Speerdarstellung.

Die fehlenden Diamantspitzhacken-/Schaufeltexturen wurden ergänzt. Alle festen Dimensionsblöcke sowie Black Sand haben jetzt hellere, unterschiedliche Oberflächen: Kiesel, Erdkrümel, Steinplatten, Mauerfugen, Holzmaserung und farbige Erze. Baumstämme haben zusätzlich eine eigene Stirnseite. Die Grundhelligkeit der dunklen Dimensionen wurde leicht erhöht. Die Bildquellen, Zuordnung und verwendeten imagegen-Prompts stehen in `tools/texture_sources`.

## JEI und EMI

JEI ist optional. Alle Craftingrezepte erscheinen automatisch; die eigene JEI-Integration erklärt auch die neuen Kristalle und Echo-Portale. Entwickelt mit JEI 31.7.0.44 für 26.3. Im früheren Nutzer-Log scheiterte JEI 31.8.0.48 mit NeoForge 26.3.0.37-beta an `ModConfig.Type.SERVER`; diese konkrete Kombination sollte bis zu einem passenden JEI-Update ohne JEI getestet werden. Der Mod startet ohne JEI.

Die Craftingrezepte verwenden Minecrafts Standard-Rezeptformat und benötigen keine spezielle EMI-Rezeptklasse. **EMI bietet in seinem offiziellen Repository derzeit keine 26.3-Version an** (Stand 30.09.2026). Ein EMI-Laufzeittest und eine spezielle EMI-Anzeige der Wasser-/Portalinteraktionen stehen deshalb aus. Es wird kein inkompatibles EMI-1.21.1-JAR eingebunden.

## Entwickeln

```powershell
.\gradlew.bat build
.\gradlew.bat runClient -PwithJei
.\gradlew.bat runGameTestServer -PgameTests
```

42 Minecraft-Integrationstests prüfen die Progression einschließlich Spelltable, Rezepten, Metalinium-Drops, Fortune-Grenze, Zauber-Cooldowns, Feuerstoß, Blitzschlag und persönlicher Enderchest. Neue Tests prüfen das genaue Spelltable-Rezept, Geoden und Chronus-Erz, alle Essentialium-Wachstumsstufen, Drops und Silk Touch. Zusätzlich wird geprüft, dass die Dekoration die vorhandene Höhlenform erhält und Kristalle an allen sechs Seiten halten bzw. ohne Halteblock abfallen. Sie prüfen die bisherige Progression, Werkzeug-Schmiedewege, den Double Spear, beide Core-Rezepte, Honig-Effekt und Flaschenrückgabe, lebende Bienen samt Ernte-Cooldown, alle drei Höhlen samt Bewohnern und Glowstone sowie die Schreinvorlage mit Beutetruhe und Wächter. Je 1000 Loot-Würfe ergaben 93 Fire Cores, 138 Water Cores und 1000 Glowing Honey. Minecrafts tatsächliche Biomsuche findet Black Desert bei Seed 0, 42 und 2026 innerhalb ihres normalen Suchradius. Der GameTest-Server erhält dafür ein eigenes erweitertes Flat-Preset. Testquellen, Teststruktur und Testpreset werden ausschließlich mit `-PgameTests` eingebunden und gehören nicht ins Release-JAR. Die grafische Darstellung im Client und JEI/EMI-Laufzeit sind damit nicht überprüft.

`tools/generate_katanas.py` regeneriert alle acht Kapitel mit Python und Pillow und wendet die archivierten Nutzer-/Bildtexturen zuletzt an. Die vorhandene Black-Desert-Biomdatei und `tools/templates/overworld_noise.json` sind Vorlagen. Loot wird beim Export in das neue 26.3-Format mit einzelnen Condition-/Modifier-Holdern übersetzt. Das Generator-Script benötigt den ursprünglichen externen Ressourcenordner nicht mehr.

Hilfreiche Testbefehle:

```text
/locate biome thedarkdimensions:black_desert
/execute in minecraft:overworld run locate biome thedarkdimensions:black_desert
/summon thedarkdimensions:the_burned
/give @s thedarkdimensions:burned_stick 16
/give @s thedarkdimensions:fire_core
/give @s thedarkdimensions:improving_template
/give @s thedarkdimensions:perfected_netherite_spear
/give @s thedarkdimensions:improved_diamond_spear
/execute in thedarkdimensions:burned_realm run tp @s 0 180 0
/locate biome thedarkdimensions:charred_forest
/give @s thedarkdimensions:improved_echo_shard
/summon thedarkdimensions:hollow_watcher
/place feature thedarkdimensions:echo_ruin
/place feature thedarkdimensions:fire_cave
/place feature thedarkdimensions:water_cave
/place feature thedarkdimensions:glowing_cave
/locate biome thedarkdimensions:glowing_cave
/locate structure thedarkdimensions:elemental_shrine
/give @s thedarkdimensions:glowing_honey
```

Den Dimensions-Teleportbefehl zum schnellen Testen im Kreativmodus ausführen. Für `/place feature ...echo_ruin` braucht es oberhalb des Meeresspiegels einen freien Platz mit festem Boden für eine etwa 8 × 7 Blöcke große Ruine.

Der Mod überschreibt keine komplette Overworld-Dimension oder Vanilla-Weltgenerator-Datei. Zwei kleine Mixins ergänzen die Wüsten-Klimaparameter und die Materialregeln für die Sandoberfläche. Andere Mods, die genau diese Weltgenerierung ersetzen, sollten separat zusammen getestet werden.


### Zauber und Metalinium (0.6.0)

- Elemental Core + Buch → leeres Spell Book (formlos).
- Metalinium Ore und Deepslate Metalinium Ore generieren ausschließlich in neuen Overworld-Chunks zwischen Y −56 und 48. Acht Aderversuche pro Chunk, Adergröße acht. Mindestens Steinspitzhacke: 3–6 Nuggets, Fortune III bis neun; höhere Fortune-Stufen bleiben auf neun begrenzt. Silk Touch erhält das Erz.
- Neun Metalinium Nuggets im 3×3-Raster → Metalinium Ingot.
- Metalinium Ingot + Chronus Orb → Chronus Infused Metalinium Ingot (formlos).
- Spelltable: Spell Book + ein Lapis + ein Infused Ingot → Zauber wählen, ohne Levelkosten.
- Rechtsklick mit dem fertigen Buch wirkt den Zauber. Bücher bleiben erhalten. Während eines Cooldowns sind alle vier Zauber gesperrt.

| Zauber | Wirkung | Cooldown |
| --- | --- | --- |
| Feuerstoß | Kleiner Feuerball in Blickrichtung; trifft und entzündet wie Vanilla | 2 Sekunden |
| Blitzschlag | Anvisierter Block bis 16 Blöcke entfernt; sechs Schaden im Umkreis von zwei Blöcken, außer dem Anwender | 5 Sekunden |
| Portable Enderchest | Öffnet die tatsächliche persönliche Enderchest mit denselben gespeicherten Items | 3 Sekunden |
| Wasserheilung | Heilt drei Herzen und löscht Feuer | 8 Sekunden |

Metalinium und Zauberbücher verwenden die Vanilla-Formen mit eigenen Farbtönen. Die gelieferten Diamond-Tool-Texturen bleiben unverändert. Die Spelltable-Oberfläche ist kompiliert, aber noch nicht visuell im Client überprüft.


### Leuchtende Oberflächen und klare Texturen (0.7.0, enthalten in 0.8.0)

Natürliche Kristallfelsen generieren in neu erzeugten Oberflächen-Chunks von Ash Wastes, Charred Forest, Crystal Hollows und Echo Fields. Sie mischen Vanilla-Glowstone mit Glowstone Vein Stone (Licht 12), Glowstone Crystal Block (15) und Pale Glowstone (10). Ein Platzierungsversuch ungefähr alle drei Chunks, mit zufälligem Umriss und verschieden hohen Spitzen. Die Felsen passen sich trockenem Gelände an und ersetzen keine Bäume oder Portalrahmen. Alle drei neuen Steine können mit einer Spitzhacke abgebaut und als Lichtquelle platziert werden.

Cores und alle fünf Magiebücher verwenden klare 16×16-Sprites. Gelände-Blöcke verwenden unveränderte Vanilla-Pixelraster mit eigenen Modellfarben; keine verzerrten Atlas-Ausschnitte. Dirt, Gravel, Stein, Deepslate, Cobblestone, Holz und Bricks haben unterscheidbare Muster. Elementarkristalle behalten die Vanilla-Amethyst-Texturen und Modelle. Neue Lichtstein-Texturen sind ebenfalls exakt 16×16. Texturen und Prompts sind unter `tools/texture_sources/clean_07/` und `tools/texture_sources/generated/clean_07/` archiviert. Erst `tools/prepare_clean_assets.py`, dann `tools/generate_katanas.py` ausführen, um alle Exporte zu regenerieren. Built-in Imagegen wurde für die neuen Cores, Bücher und Lichtsteine verwendet.

### Drei Katanas (0.8.0)

| Neuer Name | Vorlage | Schaden | Angriffe/s | Haltbarkeit | Reparatur |
| --- | --- | --- | --- | --- | --- |
| Frostgleam | Algidgleam | 6 | 1,5 | 750 | Japanese Essence |
| Bloodgrief | Bloodygrief | 8 | 1,5 | 1250 | Blood Core |
| Souleater | Faintquasar | 11,5 | 1,6 | 2100 | Soul Core |

Alle Katanas sind echte Schwerter, akzeptieren Schwertverzauberungen und besitzen durch den Improved Stick +2 Blöcke Reichweite. Souleater ist feuerfest. Die gelieferten animierten Blockbench-Modelle und fünf 32×32-Texturframes wurden samt Hand-/Inventaransicht übernommen; die PNGs und Animationseinstellungen bleiben byte-identisch. Sie funktionieren als registrierte Mod-Items ohne OptiFine oder das zusätzliche Ressourcenpaket. Originale sind unter `tools/texture_sources/katanas/` archiviert. Das mitgelieferte Ressourcenpaket nennt Fikket als Urheber.

Die Zutaten-Rezepte sind formlos:

- Cherry Sapling + Water Core → Japanese Essence.
- 1 Creeperblood oder Zombieblood + 1 Gold Ingot → Blood Essence. Zwei beliebige Blood (auch gemischt) + zwei Gold Ingots → zwei Essences. Bloodgrief braucht insgesamt zwei Blood.
- Blood Essence + Water Core → Blood Core.
- Soul Sand + Blackstone → Soul Essence.
- Soul Essence + Fire Core → Soul Core.

Besiegte Creeper droppen zusätzlich zum Vanilla-Loot mit 5 % Wahrscheinlichkeit genau ein Creeperblood, sofern Mob-Drops aktiviert sind. Explodierende Creeper erzeugen keine zusätzliche Beute.

Bloodgrief und Souleater verwenden diese diagonale Anordnung:

```text
· · E
· E ·
S · ·
```

`S` ist ein Improved Stick. `E` ist für Bloodgrief Blood Core und für Souleater Soul Core. Frostgleam braucht seit 0.12.1 eine Japanese Essence oben rechts, einen Blue-Ice-Block in der Mitte und einen Improved Stick unten links:

```text
· · E
· I ·
S · ·
```

`E` = Japanese Essence, `I` = Blue Ice, `S` = Improved Stick. Die Rezepte erscheinen in der Vanilla-Rezeptliste und den üblichen JEI/EMI-Crafting-Ansichten. Spell Core heißt jetzt Elemental Core; seine gespeicherte Item-ID bleibt für vorhandene Welten erhalten.

42 Integrationstests bestehen, einschließlich der fünf Essenz-/Core-Rezepte, drei Katana-Rezepte, echten Creeperblood-Drops, Schwerter-Verzauberungen, Schaden, Haltbarkeit, +2 Reichweite und Lichtfelsen. Die Katana-Darstellung und Ressourcen im Minecraft-Client wurden nicht visuell geprüft.


### Katana-Treffereffekte (0.8.1)

- Frostgleam: 6 Schaden, 1,5 Angriffe/s. Frosted hält vier Sekunden: 80 % weniger Bewegungstempo, ein halber Schadenspunkt pro Sekunde und Schneepartikel.
- Bloodgrief: 8 Schaden, 1,5 Angriffe/s. Blood hält fünf Sekunden: ein Schadenspunkt alle 25 Ticks, einschließlich tödlicher Blutung. Milch entfernt andere Effekte, aber keine Blutung; Blood läuft trotzdem natürlich ab. Die normalen Herzen erscheinen während Blood weinrot, inklusive halber Herzen und Hardcore-/Blinkvarianten. Absorptionsherzen bleiben gold.
- Souleater: 11,5 Schaden; bisheriges Tempo 1,6 bleibt bestehen. Soulless hält drei Sekunden: Mobs verlieren ihr Ziel und ihre Wegfindung, Spieler erhalten kurz Übelkeit als Desorientierung.
- Creeperblood: genau ein Item mit 5 % Chance, ohne zusätzlichen Looting-Bonus.

42 Server-Integrationstests bestanden, darunter Schaden und Tempo, alle Treffer-Effekte, Frost-Verlangsamung und leichter Schaden, tödliche Blutung, tatsächlicher Milchkonsum, natürliches Effekt-Ende und ein deterministischer Vergleich von 2000 Drop-Versuchen gegen die 5-%-Schwelle. Der weinrote HUD-Herzfarbton ist implementiert und kompiliert, aber noch nicht visuell im Client geprüft.

### Wurf-, Fisch- und Warrior-Äxte, Donnerhammer (0.9.0)

Throwable Axe: Rechtsklick in die Luft wirft die Eisen-Axt. Drei Eisenbarren und ein gewöhnlicher Stock, im 2×2-Raster `II / IS`. Nahkampfwerte einer normalen Eisen-Axt, Wurftreffer acht Schaden. Die Axt bleibt nach Block- und Mobtreffern erhalten und lässt sich wie ein Pfeil aufsammeln; Name, Verzauberungen und Abnutzung bleiben erhalten. Ein Wurf kostet im Survivalmodus einen Haltbarkeitspunkt. Kreativwürfe erzeugen keine zusätzlichen aufsammelbaren Waffen. Der Griff der 16×16-Eisen-Axt-Textur wurde mit Built-in Imagegen verkürzt; Original, Export und Prompt stehen unter `tools/texture_sources/armory/`.

Fish-Axes gibt es in Holz, Stein, Kupfer, Eisen, Gold, Diamant und Netherite. Schmiedetisch: Improving Template + Axt + Fisch. Cod, Salmon, Tropical Fish, Pufferfish sowie die beiden gekochten Fischsorten funktionieren. Normale, Warrior- und vorhandene verbesserte Äxte sind als Ausgangsitem möglich. Das Ergebnis hat die normalen Axtwerte dieser Stufe, einschließlich Abbauen, Entrinden und Kupfer-Schaben. Name, Verzauberungen und Abnutzung werden übernommen. Die sieben gelieferten PNGs aus `fish-axes/` sind unverändert übernommen, auch wenn sie dort Pickaxe-Dateinamen haben.

Warrior-Axes haben zwei zusätzliche Schadenspunkte gegenüber ihrer normalen Materialstufe, 15 % schnelleres Abbauen und durch den Improved Stick zwei zusätzliche Blöcke Reichweite in der Haupthand. Haltbarkeit und Angriffstempo entsprechen der normalen Axt. Ihre Texturen stammen unverändert aus `Axes/`; für die fehlende Kupfervariante wird die Eisenform mit einer Kupfer-Modellfarbe verwendet. Warrior Stick wird formlos aus Leder, Improved Stick und einem Creeperblood gefertigt.

```text
M B M
M S ·
· W ·
```

`M` = Werkzeugmaterial, `B` = Materialblock, `S` = Improved Stick, `W` = Warrior Stick. Holz verwendet Planks und einen Log; Stein verwendet ein Vanilla-Steinwerkzeugmaterial und einen normalen Steinblock. Die übrigen craftbaren Stufen verwenden Barren beziehungsweise Diamanten und den passenden kompakten Block. Netherite wird nur am Schmiedetisch verbessert: normale Netherite Axe + Warrior-Template + Warrior Stick oder Diamond Warrior Axe + Netherite Upgrade Template + Netherite Ingot.

Thundergod's Hammer: zehn Nahkampfschaden, ein Angriff pro Sekunde, 1600 Haltbarkeit, Reparatur mit Eisenbarren und zwei zusätzliche Blöcke Reichweite in der Haupthand. Die gelieferte 16×16-Trident-Itemtextur aus `Great Hammer/` wird unverändert als Hammertextur verwendet.

```text
D B D
L S L
· S ·
```

`D` = Diamond, `B` = Diamond Block, `L` = Lightning Core, `S` = Improved Stick. Erst den Awakened Hammer in Haupt- oder Nebenhand halten und in der Luft erneut die Sprungtaste drücken: normales Elytra-Gleiten ohne Brust-Elytra. Beim Gleiten nutzt sich der Hammer wie eine Elytra ab; Weglegen oder ausgeschöpfte Haltbarkeit beendet die Flugfähigkeit.

Schmiedetisch: **Lightning Core im Template-Feld + Hammer + Lightning Spell Book** → Thundergod's Hammer — Awakened. Alle drei Zutaten werden verwendet. Rechtsklick auf einen anvisierten lebenden Gegner bis 24 Blöcke entfernt erzeugt einen sichtbaren Blitz und acht Blitz-Schadenspunkte. Wände blockieren das Anvisieren; fünf Sekunden Cooldown. Der Blitzbuch-Cooldown wird ebenfalls gesetzt. Ohne gültiges Ziel wird kein Cooldown ausgelöst. Das Upgrade erhält Name, Verzauberungen und Abnutzung.

Alle neuen Rezepte sind native Crafting-/Smithing-Rezepte für Rezeptanzeiger. JEI enthält zusätzlich Hinweise für Fish-Axes und Hammer. `tools/generate_armory.py` regeneriert die Ressourcen einschließlich vorheriger Kapitel. 48 Server-Integrationstests bestehen, darunter alle sieben Materialstufen mit allen sechs Fischzutaten, Upgrade-Erhaltung, Smithing-Template-Feld, Axtwerte, physische Wurfaxt-Pickups und Flugfähigkeit inklusive Nebenhand, Weglegen und defektem Hammer. Der Client wurde noch nicht visuell geprüft.

### Chronus Ingot und Blood Netherite (0.10.0)

Der bisherige Essentialium Infused Metalinium Ingot heißt jetzt **Chronus Infused Metalinium Ingot**. Seine neue transparente 16×16-Textur zeigt dunkles Metall mit einem violetten Chronus-Kern. Rezept und Verwendung im Spelltable bleiben Metalinium Ingot + Chronus Orb. Die alte interne Item-ID wird für bestehende Inventare beibehalten; Namen und JEI-Hinweise sind angepasst.

**Blood Netherite Ingot:** vier einzelne Creeperblood-Zutatenfelder und zwei Netherite Ingots im Crafting-Tisch, formlos, ergeben genau einen Ingot. Neun Ingots ergeben einen Blood Netherite Block; der Block lässt sich wieder in neun Ingots zerlegen.

**Blood Netherite Upgrading Template:** zusätzlich im Loot aller vier Bastion-Truhentypen. Eine Vorlage garantiert pro Schatztruhe; in Bridge-, Hoglin-Stable- und Other-Truhen 10 % Chance auf eine Vorlage. Bestehender Vanilla-Loot bleibt erhalten. Bereits geöffnete Truhen erhalten nachträglich keine neuen Items.

Das rot-goldene Template lässt sich mit sieben Diamanten und einer Netherwarze duplizieren:

```text
D T D
D N D
D D D
```

`D` = Diamant, `T` = Blood Netherite Upgrading Template, `N` = Nether Wart. Ergebnis: zwei Templates, wobei die eingesetzte Vorlage verbraucht wird.

**Schmiedetisch:** Blood Netherite Upgrading Template + Netherite-Ausrüstung + Blood Netherite Ingot. Das funktioniert für Helm, Brustplatte, Hose, Stiefel, Schwert, Spitzhacke, Axt, Schaufel, Hacke und die Netherite Warrior Axe. Die fünf Perfected Netherite Tools können ebenfalls verbessert werden und behalten ihre +2 Blöcke Reichweite. Blood-Netherite-Werkzeuge lassen sich alternativ mit Improving Template + Improved Stick mit einem verbesserten Griff versehen. Namen, Abnutzung und Verzauberungen werden übernommen; Rüstung behält vorhandene Trims.

| Rüstungsteil | Rüstungspunkte | Knockback-Resistenz |
| --- | --- | --- |
| Helm | 5 | 0,15 |
| Brustplatte | 10 | 0,15 |
| Hose | 8 | 0,15 |
| Stiefel | 5 | 0,15 |

Jeder Teil hat zwei Rüstungspunkte mehr als Netherite; Toughness bleibt drei pro Teil. Waffen/Werkzeuge bekommen gegenüber ihrer Netherite-Version einen zusätzlichen Schadenspunkt und Werkzeug-Abbaustärke zehn statt neun. Warrior Axe behält ihren zusätzlichen Schaden und 15-%-Abbaubonus. Angriffstempo, Haltbarkeit und Enchantability entsprechen Netherite. Reparatur mit Blood Netherite Ingots; Ausrüstung und Ingots sind feuerfest.

Alle neuen Blood-Netherite-IDs und Texturdateien beginnen mit `red_netherite_`; die Anzeige heißt Blood Netherite. Die gelieferten Item-, Block- und modernen Rüstungs-PNGs aus `red-netherite/` wurden unverändert und ohne Vanilla-Überschreibungen übernommen. Das alte Ressourcenpaket ist nicht zusätzlich erforderlich. Für Template und Chronus Ingot wurden Built-in Imagegen und ein 16×16-Export verwendet; Originale und Prompts sind unter `tools/texture_sources/blood_netherite/` archiviert. Ressourcen mit `tools/generate_blood_netherite.py` regenerieren.

52 Server-Integrationstests bestehen, einschließlich der genauen Rezeptmengen, aller Upgrade-Pfade, Namen/Verzauberungen/Abnutzung, Materialwerte, Reparatur, Rüstungstrims und 500 tatsächlicher Loot-Rolls je Bastion-Truhentyp. Die Darstellung im Minecraft-Client wurde noch nicht visuell geprüft.

### Spelltable-Texturen und Devil's Trident (0.11.0)

Die Spelltable-Oberfläche verwendet jetzt die gelieferten Oberseiten- und Seiten-PNGs aus `Spell_table/`, ohne Farbfilter. Die beiden `_e`-Texturen sind als zusätzliche, volleuchtende Modellebenen übernommen; OptiFine ist dafür nicht erforderlich. Unterseite, Blockform und Zauberauswahl bleiben erhalten. Die vier Originaltexturen sind unter `tools/texture_sources/spelltable/` archiviert.

**Devil's Trident:** Seit 0.12.0 Devil's Tear im Template-Feld + normaler Trident + Devil's Eye im Schmiedetisch. Namen, Verzauberungen und Abnutzung bleiben erhalten. Er hat acht Grundschaden im Nahkampf und beim Werfen, wie ein normales Netherite-Schwert. Angriffstempo bleibt Trident-Tempo: 1,1 Angriffe/s. 2031 Haltbarkeit, Enchantability 15, Reparatur mit Blood Netherite Ingot und feuerfest.

Erfolgreiche Nahkampf- und Wurftreffer verursachen zusätzlich fünf Sekunden **Blood** und vier Sekunden Feuer. Blood ist derselbe tödliche, nicht durch Milch heilbare Effekt wie bei Bloodgrief. Schutz und Feuerresistenz wirken wie üblich. Werfen, Loyalty-Rückkehr, Channeling, Impaling, Riptide und Wiederaufsammeln verwenden die Vanilla-Trident-Logik.

Beim Beginn eines erfolgreichen Rechtsklick-Benutzens, erfolgreichen Werfen oder Nahkampftreffen erhält der Träger fünf Sekunden **Devil's Charm**. Erwachsene Villager und Villagerbabys innerhalb von acht Blöcken geraten in Panik und wählen einen Fluchtweg. Die Wirkung wird alle fünf Ticks aufgefrischt; die Angst-Erinnerung läuft nach Ende des Effekts wieder aus. Nur das Halten des Items löst keine Angst aus. Villager erhalten dadurch keinen Schaden.

Die gelieferten Inventar- und Entity-Texturen aus `TheDevilsTrident/` sind unverändert. Die CEM-Boxgeometrie wurde mit ihren UV-Offsets in ein natives 32×32-Modell übertragen, für Handansicht, Aufladeanimation und geworfenen Trident. Normale Tridents behalten ihre Vanilla-Darstellung. Die Variante wird per NeoForge-Datenattachment zum Client synchronisiert und beim Laden wieder aus dem gespeicherten Item bestimmt. Modelle und Quellen sind unter `tools/texture_sources/devils_trident/` archiviert; das Ressourcenpaket nennt Torrezx als Urheber. Auch `Spell_table/` nennt Torrezx.

Alle Ressourcen mit `tools/generate_devil.py` regenerieren. 56 Server-Integrationstests prüfen unter anderem Smithing-Erhaltung, Netherite-Schwert-Schaden, Trident-Verzauberungen, echte Wurftreffer mit Blutung/Feuer, Loyalty-Rückkehr und Pickup sowie tatsächliches Weglaufen von Erwachsenen und Kindern und natürliches Angst-Ende. Client-Code und Ressourcen sind geprüft; die Darstellung wurde noch nicht visuell im Minecraft-Client getestet.

### Devil's Armor, Lava-Riptide und neue Upgrade-Zutaten (0.12.0)

**Devil's Armor I** lässt sich ausschließlich auf Blood Netherite Helmet, Chestplate, Leggings und Boots anwenden. Erhältlich am Verzauberungstisch, als verzaubertes Buch, im Villager-Handel und im regulären zufälligen Verzauberungs-Loot. Es hat eine Stufe, Gewicht zwei und Verzauberungskosten ab 25. Bücher lassen sich am Amboss anwenden; die Verzauberung kann mit den üblichen Rüstungsverzauberungen kombiniert werden.

Der Set-Bonus greift nur, wenn alle vier richtigen Blood-Netherite-Teile getragen werden und jedes Devil's Armor trägt. Vollständiger Bonus: immun gegen Feuer, Brennen, Lava, Magmablock und andere Schadensquellen mit dem Vanilla-Feuer-Tag. Normale Angriffe und anderer Schaden treffen weiterhin. In Lava ermöglicht das Set wasserähnliche Bewegung und Sprint-Schwimmen; normale Lava wird dadurch nicht zu Wasser. Entfernen oder Entzaubern eines Teils deaktiviert den Bonus sofort.

**Riptide in Lava:** Mit einem Riptide-verzauberten Devil's Trident kann man bei tatsächlichem Lavakontakt aufladen und sich wie im Wasser wegschleudern. Riptide-Level, Bewegung, Aufladezeit und Haltbarkeitsverbrauch bleiben Vanilla. Das benötigt den Rüstungsbonus nicht, schützt aber allein nicht vor Lavaschaden. Normale Tridents bleiben auf Wasser/Regen beschränkt; trockene Luft aktiviert weiterhin kein Riptide.

**Devil's Tear:** acht Creeperblood um eine Ghast Tear. **Devil's Eye:** acht Creeperblood um ein Eye of Ender. Beide Rezepte sind 3×3 und ergeben ein Item:

```text
B B B
B X B
B B B
```

`B` = Creeperblood, `X` = Ghast Tear beziehungsweise Eye of Ender. Beide neuen Icons sind transparente 16×16-PNGs, per Built-in Imagegen erzeugt; Originale und Prompts liegen unter `tools/texture_sources/infernal/`.

Neues Trident-Upgrade: **Devil's Tear im Template-Feld + Trident im Basisfeld + Devil's Eye im Zutatenfeld**. Alle Zutaten werden verbraucht; das Ergebnis ist ein Devil's Trident mit übernommenen Namen, Verzauberungen und Abnutzung. Das frühere Blood-Netherite-Template/Ingot-Rezept ist entfernt. Vorhandene Devil's Tridents bleiben erhalten.

Aktuelle Ressourcen mit `tools/generate_elemental_stone.py` regenerieren. 60 Server-Integrationstests bestehen: vollständige und unvollständige Sets, erlaubte Verzauberungs-Items und Bezugsquellen, echte Schadensquellen, tatsächliche Lava-Schwimmpose und Bewegungsphysik, tatsächlicher Riptide-Start in Lava, Vanilla-/Trockenluft-Grenzen, neue Zutatenrezepte und Template-Slot. Die neuen Icons wurden geprüft; Client-Schwimmsteuerung ist implementiert und kompiliert, aber noch nicht visuell getestet.

### Dreistufige Stock-Herstellung (0.13.0)

Flint and Steel + normaler Stock → **Burning Stick** (formlos, Feuerzeug verbraucht drei Haltbarkeitspunkte, verursacht beim Halten Feuerschaden). Ins Wasser werfen → **Burned Stick**. Burned Stick + normaler Stock → **Improved Stick** (formlos, ein Ergebnis). Die bisherigen Griff-Rezepte für Werkzeuge, Katanas, Warrior Stick und Hammer benötigen nun diesen neuen Improved Stick. Burned Planks ergeben weiterhin Burned Sticks, die anschließend mit normalen Stöcken verbessert werden müssen.

Gespeicherte Items bleiben erhalten: `burned_stick` ist weiterhin das heiße Item (Anzeigename Burning Stick), `improved_stick` das abgekühlte Item (Anzeigename Burned Stick). Der neue Improved Stick trägt intern die ID `reinforced_stick`. Zum Regenerieren sämtlicher aktueller Ressourcen `tools/generate_elemental_stone.py` verwenden.

### Dark Acacia (0.13.0)

Dark Acacia ersetzt das Aussehen und den Namen von Burned Log und Burned Planks, behält aber ihre gespeicherten Block-IDs. Alle 21 gelieferten Holztexturen sind unverändert in 16×16 übernommen. Bretter zeigen acht örtlich stabile Varianten. Die Bäume in der Dimension verwenden die neuen Stämme; Setzlinge wachsen auf Burned Dirt und normaler Erde zu diesen Bäumen. Seit 0.14.0 verwenden die Bäume Vanilla-Acacia-Blätter; deren Setzlinge lassen sich mit Ashes in Dark-Acacia-Setzlinge umwandeln.

Zusätzlich gibt es Holzblöcke mit Rinde auf allen Seiten, entrindete Stämme und Holzblöcke, Treppen, Stufen, Türen, Falltüren sowie zwei geschnitzte Varianten. Holzbauteile verwenden die normalen Minecraft-Holzrezepte. Äxte entrinden Stämme und Holz per Rechtsklick. Stamm + Feuerstein ergibt den geschnitzten Block; dieser ergibt formlos den halbgeschnitzten Block. Die zweite geschnitzte Textur bildet jeweils die Seiten. Die Textur `acacia_double.png` wird für Doppelstufen verwendet. Originale sind unter `tools/texture_sources/user_resources/` archiviert.

### Gelieferte Core-, Essenz- und Buchtexturen (0.12.2)

Zwölf Texturen aus `../TheDarkDimensions NutzeDieseResourcen!` sind unverändert in ihrer gelieferten 32×32-Auflösung übernommen: Fire, Water, Lightning, Elemental, Blood und Soul Core; Japanese, Blood und Soul Essence; Feuer-, Wasser- und Blitz-Zauberbuch. Diese ersetzen die bisherigen Sprites und Modellfarben. Für den Elemental Core wird `elemental_core_matching_style.png` verwendet; die alternative `spell_core.png` bleibt archiviert. Originale und Dateizuordnung liegen unter `tools/texture_sources/user_resources/`. Zum Regenerieren aller aktuellen Ressourcen `tools/generate_elemental_stone.py` ausführen. Die Release-Prüfung vergleicht jede importierte PNG bytegenau mit der Vorlage.

### Helle Asche-Atmosphaere (0.13.1)

Der Burned Realm besitzt einen hellen aschgrauen Himmel und warmgrauen Nebel. Umgebungslicht steigt von 0,12 auf 0,35, der visuelle Himmelslichtfaktor von 0,15 auf 0,85 und die Nebelweite von 110 auf 192 Bloecke. Alle drei Oberflaechenbiome und die vier Hoehlenbiome verwenden dieselben Atmosphaerenfarben, damit Biome die helleren Dimensionsfarben nicht mit alten schwarzen Werten ueberschreiben. Zum Regenerieren aller Ressourcen `tools/generate_elemental_stone.py` verwenden.

### Mummies, Vanilla-Terrain und Hard Stone (0.14.0)

Mummies verwenden die unveränderte 64×64-Textur `mummy.png` und das Vanilla-Zombie-Modell. Sie verbrennen nicht in der Sonne und verwandeln sich im Wasser nicht. In Vanilla-Wüsten ersetzt die natürliche Spawnliste Husks durch Mummies. The Burned und Water Husk samt Spawn-Eiern und Loot-Tabellen sind entfernt; Black Desert und seine Overworld-Generierungs-Mixins ebenfalls.

Die Dimension verwendet die normalen Overworld-Oberflächenregeln und Vanilla-Grass, Dirt, Gravel, Stone und Bedrock. Dark Acacia bleibt erhalten; seine Bäume verwenden Vanilla-Acacia-Blätter. Grass ist olivgrün (#4d6338), Laub dunkelgrün (#56643c). Dark-Acacia-Setzlinge lassen sich aus Acacia Sapling und Ashes formlos craften. Die helle Asche-Atmosphäre bleibt erhalten. Elementarhöhlen, Lichtfelsen, Essentialium-Geoden und Chronus-Erz bleiben ebenfalls erhalten.

Hard Stone ersetzt Deepslate im unteren natürlichen Höhenverlauf. Härte 4,5 statt Stone 1,5 / Deepslate 3; Erzvarianten haben Härte 5,5. Coal, Copper, Iron, Gold, Redstone, Lapis, Diamond und Emerald verwenden Vanilla-Erzverteilungen und Vanilla-Drops, Fortune und Silk Touch. Alle neun Blöcke verwenden je vier deiner unveränderten 16×16-Texturen, positionsabhängig ausgewählt. Die Originale sind unter `tools/texture_sources/mummy_hard_stone/` archiviert.

Astral Crystal Wand, Crafting-Tisch:

```text
D
E
S
```

D = Dragon Head aus einem End-City-Schiff, E = End Crystal, S = Improved Stick. Rechtsklick auf einen vollständigen leeren Obsidianrahmen oder ein aktives Netherportal aktiviert das verbesserte Portal; der Wand verliert einen Haltbarkeitspunkt. Er bleibt erhalten und lässt sich 64-mal verwenden. Ein erneuter Klick auf ein bereits verbessertes Portal verbraucht keine Haltbarkeit. Der Fire Core bleibt Crafting-Zutat für Magie und Waffen.

Die Terrain-Änderungen gelten für neu generierte Chunks. Bestehende Terrainblöcke werden nicht automatisch ersetzt. Die alten Terrain-Block-IDs bleiben für gespeicherte Blöcke registriert und sind aus dem Kreativtab ausgeblendet. Zum Regenerieren aller aktuellen Ressourcen `tools/generate_elemental_stone.py` verwenden.

63 Minecraft-Integrationstests bestanden, darunter die reale Wüsten-Spawnliste mit Mummies statt Husks, echte Terrain-Generation mit Stone/Hard Stone ohne Deepslate, Mummy-Ashes und biomeabhängige Water-Core-Drops, Fortune/Silk Touch für alle acht Hard-Stone-Erze, tatsächliche Diamanterz-Platzierungen und Wand-Aktivierung für beide Portalachsen sowie leere und beschädigte Rahmen. Client-Darstellung noch nicht visuell getestet.


### Solar progression (0.15.0)

Mummy-Treffer geben 8 Sekunden Hunger I und 4 Sekunden Gift I. Zusätzlich zu Ashes hat jede Mummy eine feste 5-%-Chance auf genau eine Solar Essence, unabhängig von Looting. Water-Cave-Mummies behalten ihren bisherigen Water-Core-Drop.

Pocket Dimension Key im Crafting-Tisch:

```text
· N ·
· N ·
G E G
```

N = Netherite Ingot, G = Gold Ingot, E = Solar Essence. Rechtsklick in die Luft öffnet pro Spieler einen eigenen, dauerhaft gespeicherten Raum mit genau 10×10×10 nutzbaren Blöcken. Boden, Decke und Wände sind hell und unzerstörbar. Der Schlüssel wird nicht verbraucht; erneuter Rechtsklick führt zur vorherigen Position und Dimension zurück. Bauten bleiben bei Wiederbesuchen erhalten. Spieler erhalten getrennte Räume; `/pocket exit` ist der Notausgang bei verlorenem Schlüssel. Technisch liegen die privaten Räume in einer gemeinsamen leeren Pocket-Dimension mit gespeicherter UUID-Zuordnung. Weder Betten noch Respawn Anchors setzen dort einen Spawnpunkt.

Solar Forge im Smithing Table: Template Solar Essence, Basis Blast Furnace, Material Coal Block. Sie verarbeitet die normalen Blasting-Rezepte doppelt so schnell wie ein Vanilla-Hochofen. Menü, Hopper, Brennstoffe und XP bleiben erhalten; Brenndauer und Verarbeitungszeit halbieren sich gemeinsam, sodass der Verbrauch pro Erz gleich bleibt. Die goldene Oberseite und gemeißelten Seiten kennzeichnen die Maschine.

Astral Crystal Wand jetzt senkrecht im Crafting-Tisch: Flint and Steel oben, End Crystal in der Mitte, Improved Stick unten. Portalaktivierung und Haltbarkeit bleiben erhalten.

Stormveil wird formlos aus je einer Solar Essence, Lightning Core und Improved Stick gecraftet. 9 Angriffsschaden, 1,5 Angriffstempo, +2 Blöcke Reichweite, 1600 Haltbarkeit und Verzauberbarkeit 20. Jeder erfolgreiche Nahkampftreffer verursacht zusätzlich 1 Punkt Blitzschaden (vor Rüstung); er löst keinen brennenden Welt-Blitz aus. Damit liegt Stormveil zwischen Bloodgrief und Souleater. Reparatur mit Solar Essence. Das Originalmodell, alle PNGs und Animationszeiten aus Lightning Blade sind archiviert unter `tools/texture_sources/stormveil/`; OptiFine ist nicht erforderlich. Die emissiven Animationsflächen und eigenen Schlag-/Krit-Partikel sind ebenfalls übernommen und verändern keine Vanilla-Waffe.

Die Solar Essence verwendet deine unveränderte `Solar_Essence.png`, archiviert unter `tools/texture_sources/solar/solar_essence_supplied.png`. Der Schlüssel-Icon ist mit dem eingebauten `image_gen.imagegen` erstellt und als 32×32-Pixel-Item exportiert. Originale und Prompts: `tools/texture_sources/solar/`. Die Schlüsselgrafik zeigt einen Netherite-Schaft und einen goldenen Solar-Griff entsprechend dem Rezept.

68 Minecraft-Integrationstests bestanden, einschließlich tatsächlicher Mummy-Treffereffekte, 10.000 Loot-Würfen, Rezepten und Smithing-Slots, Vergleich der realen Schmelzzeiten, Blitz-Bonusschaden durch Nahkampf-Immunitätsframes, echter Dimensionswechsel/Rückkehr, 1000 freier Rauminnenblöcke und Erhalt gebauter Blöcke beim Wiederbesuch. Die Datenspeicherung ist zusätzlich per Codec-Roundtrip geprüft. Client-Darstellung noch nicht visuell im Spiel getestet.


### Ressourcen-Update (0.15.1)

Stormveil verwendet `Lightning Blade/optifine/cit/kaminari_pack/kaminari.json` mit allen 360 Modellteilen, den ursprünglichen Hand-/Inventar-Transformationen und drei emissiven Overlays. Der Export enthält 25 nicht zugewiesene `#missing`-Flächen; diese werden beim Import weggelassen, damit keine pink-schwarze Fehlertextur entsteht. Die zugewiesenen Texturen, UVs und Animationen bleiben unverändert. OptiFine ist nicht nötig. Die Quelle entspricht der zuvor archivierten Modellversion. Deine unveränderte 48×48-Textur `Solar_Essence.png` ist ebenfalls im neuen Release enthalten. Modell-Koordinaten, UV-Rotationen und Texturverweise werden bei der Release-Prüfung kontrolliert.


### Natürliches Terrain und variable Baumkronen (0.16.0)

Burned Stone, Burned Deepslate und Burned Dirt sind als eigene Blöcke und Items samt Modellen, Texturen und Loot-Tabellen entfernt. Alte gespeicherte IDs werden über Registry-Aliase zu Stone, Hard Stone beziehungsweise Dirt aufgelöst. Das obere Terrain besteht aus Vanilla-Grass/Dirt und Stone; im tiefen Bereich ersetzt Hard Stone Deepslate. Die acht Hard-Stone-Erzvarianten und ihre Verteilungen bleiben erhalten. Die natürlichen Oberflächenregeln setzen Grass auf Dirt; alte Burned-Dirt-Blöcke werden zu Dirt.

Realm-Gras ist schwach gesättigtes Grün (#687568), Laub gedämpftes Grün (#5f705b). Dark-Acacia-Kronen variieren mit dem Welt-Seed in Höhe, Breite, Tiefe, Versatz, seitlichen Ästen und zufälligen Randblättern. Die komplette Baumform wird vor Platzierung auf freien Raum geprüft; natürliche Blätter bekommen einen Abstand zu ihren tragenden Logs. Vorhandene Bäume behalten ihre Form, neue Bäume und Setzlinge verwenden die neue Generation.

Zum Regenerieren der aktuellen Ressourcen `tools/generate_elemental_stone.py` verwenden.

70 Minecraft-Integrationstests bestanden, einschließlich Terrain-Generierung, aller Hard-Stone-Erze, alter Block-/Item-ID-Migration samt serialisierten Blockzuständen, Baumkronen-Variation über 16 Seeds, Reproduzierbarkeit und Setzlingswachstum. Client-Darstellung noch nicht visuell im Spiel geprüft.


### Elementare Hard-Stone-Texturen (0.17.0)

Water Stone, Fire Stone und Glowing Stone verwenden alle vier Original-Hard-Stone-Varianten mit dezenten blauen, warmen beziehungsweise gelben Akzenten. Nur die vier grauen Stein-Palettenfarben werden verändert; Erzfarben bleiben pixelgenau erhalten. Die ursprünglichen Bildmaße bleiben erhalten, ohne Skalierung. Die Ganzblock-Tönung ist für diese Blöcke entfernt. Insgesamt 108 Texturen für die drei Steinarten und ihre je acht Erzarten mit vier Varianten. Die Original-Hard-Stone-Dateien bleiben unverändert.

Freigelegte Vanilla- und Hard-Stone-Erze an den Elementarhöhlenwänden werden passend zur Höhle umgestellt. Erzart und Drops bleiben gleich; Fortune, Silk Touch, Mining-Tool-Anforderungen und XP entsprechen den Hard-Stone-Erzvarianten. Erze werden nicht durch Glowstone, Magma, Wasser oder Honey-Bodenakzente ersetzt. Die Umstellung geschieht bei neuer Höhlendekoration. Bereits vorhandene Elementar-Steinblöcke zeigen direkt die neuen Texturen.

72 Minecraft-Integrationstests bestanden. Neue Tests bestätigen die erhaltenen Erzarten an Wänden und Böden aller drei Höhlen, Drops/Fortune/Silk Touch für alle 24 Erzblöcke sowie korrekte Tool-Anforderungen. Die Pixelprüfung kontrolliert alle 108 Texturen auf Originalgröße und unveränderte Erzpixel. Client-Darstellung noch nicht visuell im Spiel geprüft.


### Ashfall und Katana-Kombos (0.18.0)

Im Burned Realm fällt alle 8–15 Minuten nach Ende des vorigen Ereignisses für 2–4 Minuten Asche. Oberhalb freier Himmelsflächen erscheinen Aschepartikel; die drei Elementarkristalle und alle Essentialium-Knospen/Cluster leuchten um bis zu vier Lichtstufen stärker (Maximum 15). Der Wetterplan bleibt beim Speichern erhalten. Chunk-Aktualisierungen laufen schrittweise und laden keine zusätzlichen Chunks. Nach Wetterende fällt die Lichtstärke wieder zurück.

Während Ashfall erscheinen gelegentlich zusätzliche Mummies an freien Oberflächen, mit einem Gegnerlimit und unter Beachtung von Schwierigkeit und Mob-Spawn-Spielregeln. Fire-Core-Chancen steigen von 10 % auf ungefähr 19 %, Water Cores bei Water-Cave-Mummies von 15 % auf ungefähr 27,75 %, Solar Essence von 5 % auf ungefähr 9,75 %. Glowing Bees droppen eine zusätzliche Glowing Honey; ihre Flaschenernte ist alle 15 statt 30 Sekunden möglich. Außerhalb dieser Dimension bleiben die bisherigen Chancen unverändert.

Katanas erhalten einen Finisher: Frostgleam verstärkt Frosted für zwei Sekunden zu einem kurzen vollständigen Bewegungsstopp, Bloodgrief erhöht Blutung auf acht Sekunden, Souleater verlängert Soulless auf 4,5 Sekunden. Stormveil schickt einen sichtbaren Kettenblitz auf bis zu zwei weitere feindliche Mobs in jeweils vier Blöcken Entfernung; jeder erhält zwei Punkte Basisschaden, Rüstung wirkt weiterhin. Sichtkontakt ist erforderlich; passive Tiere und Verbündete werden ausgelassen.

Ohne Better Combat zählt der Server drei erfolgreiche Treffer mit höchstens zwei Sekunden Abstand. Ein Flächentreffer zählt einmal. Wechsel der Waffe/Dimension und längere Pausen setzen die Folge zurück; der Finisher fügt zwei zusätzliche Basisschadenspunkte hinzu. Mit **Better Combat 3.2.2+26.3** sind alle vier Katanas zweihändig, haben zwei Seitenschnitte und als dritte Attacke einen eigenen schweren Abwärtsschlag mit 25 % höherem Schaden. Effekte folgen dabei der serverseitigen Better-Combat-Attackenphase; wie dort üblich zählen auch verfehlte Attacken für die animierte Schlagfolge. Reichweite wird nicht doppelt erhöht. Better Combat ist optional; seine Player Animation Library und Cloth Config werden zusätzlich benötigt.

Zum Testen mit Cheats: `/ashfall start`, `/ashfall stop`, `/ashfall status`. Start/Stop steuern die Burned Dimension auch aus anderen Dimensionen; Status zeigt das Wetter in der aktuellen Dimension. Der aktuelle Ressourcen-Generator ist `tools/generate_ashfall.py`. Die optionalen lokalen Test-Abhängigkeiten liegen in `.dev/compat` und werden mit `-PwithBetterCombat` geladen; sie werden nicht in das Mod-JAR eingebettet.

77 Minecraft-Integrationstests bestanden, sowohl ohne Better Combat als auch mit dem lokalen `bettercombat-neoforge-3.2.2+26.3.jar`, Player Animation Library 1.2.7 und Cloth Config 26.3.159. Neue Tests prüfen Wetterbegrenzung, Zeitplan und Persistenz, erhöhte und wiederhergestellte Kristall-Lichtstärke, Bonus-Honey nur in Ashfall, drei Treffer und Kombo-Reset, Flächentreffer, Kettenblitz-Ziele sowie alle vier Better-Combat-Waffenkonfigurationen und das tatsächliche Parsen der eigenen Animation. Die Client-Darstellung wurde noch nicht visuell im Spiel geprüft.


### Mummy-Ruinen und Pocket-Besuche (0.19.0)

In neu erzeugten Overworld-Wüsten entstehen kleine, teilweise verschüttete Sandstein-Grabkammern. Eine Treppe und ein alter Sandsteinbogen markieren den Zugang. Zwei Mummies bewachen zwei Truhen; eine Pfeilfalle und eine einzelne TNT-Falle mit Stein-Druckplatten lassen sich umgehen. Der Papyrus in den Truhen enthält Hinweise zu Asche, dem Crystal Wand und dem Dimensionsportal. Jede Truhe enthält Ashes und einfache Grabbeigaben, mit 5 % Chance eine Solar Essence und mit **3 % Chance** eine Pocket-Key-Upgrade-Vorlage. Die Ruinen sind reguläre Minecraft-Strukturen und lassen sich mit `/locate structure thedarkdimensions:mummy_ruins` finden. Bereits generierte Chunks erhalten keine nachträglichen Ruinen.

Im Schmiedetisch: **Pocket-Key-Upgrade-Vorlage + normaler Pocket Dimension Key + Netheriteblock → Improved Pocket Dimension Key**. Die Vorlage und der Block werden verbraucht. Die Vorlage ist nur als Ruinen-Loot erhältlich. Der verbesserte Key hat die gleiche Form mit Netherite-Akzenten statt Gold. Er öffnet einen eigenen 20×20×20-Raum in einer separaten Void-Dimension; der normale 10×10×10-Raum und dessen Inhalt bleiben erhalten. Es gibt pro Spieler eine private Raumzuordnung pro Größe. Auf Wunsch kann ein neuer normaler Key mit dem bisherigen Rezept hergestellt werden, um den kleinen Raum weiterhin zu betreten. Ein Wechsel zwischen Räumen erhält den ursprünglichen Rückkehrpunkt außerhalb der Pocket-Dimensionen.

**Rückkehr-Anker:** vier Obsidian um eine Enderperle (` O ` / `OEO` / ` O `). Im Pocket-Raum platzieren und mit leerer Haupthand rechtsklicken, um ohne Key zurückzukehren. Jeder Besucher landet an seinem eigenen gespeicherten Ausgangspunkt. Der Anker kann abgebaut und erneut platziert werden; seine Verknüpfung ergibt sich immer aus dem tatsächlichen Raum, in dem er steht. Er setzt keinen Respawnpunkt. `/pocket exit` bleibt als Notausgang verfügbar.

**Foreign Key:** günstiger als der normale Key, mit zwei Eisenbarren übereinander und unten Goldbarren–Enderperle–Goldbarren (` I ` / ` I ` / `GEG`). Er verwendet die ursprüngliche Key-Textur mit violetten Akzenten. Mit dem Foreign Key in der Haupthand einen Rückkehr-Anker rechtsklicken: der Key speichert den Besitzer und die Größe dieses Raums dauerhaft. Der Besitzer kann einen solchen verknüpften Key einem Freund geben. Rechtsklick mit dem Key öffnet den verknüpften Raum; ein weiterer Rechtsklick im selben Raum führt zurück. Der Key funktioniert auch bei einem offline befindlichen Besitzer und als Backup für den eigenen Raum. Unverknüpfte Keys erzeugen keine Räume. Es ist möglich, einen Key am Anker erneut zu verknüpfen.

Eigene Räume, Besucherzuordnung und persönliche Rückkehrpunkte werden serverseitig gespeichert. Gäste überschreiben nie den Rückkehrpunkt des Besitzers. Ein Besucher, der die Raumgrenzen verlässt, wird in den tatsächlich besuchten Raum zurückgesetzt. Alte Speicherstände ohne Raumgröße/Besucherfelder werden als normale 10×10×10-Räume weitergeführt. Verknüpfte Foreign Keys behalten ihre Daten beim Speichern oder Weitergeben. Der aktuelle Ressourcen-Generator ist `tools/generate_ruins.py`.

82 Minecraft-Integrationstests bestanden, mit und ohne Better Combat 3.2.2+26.3. Neue Tests prüfen Crafting und Schmiedetisch-Slots, exakt 8.000 freie Blöcke im separaten 20×20×20-Raum, Erhalt des alten Rauminhalts und alter Speicherstände, individuelle Rückkehrpunkte bei Raumwechseln und Freundesbesuchen, Foreign-Key-Speicherung, Offline-Besuche und Raumgrenzen. Die Ruinen-Tests prüfen natürliche Jigsaw-Generierung unter der Oberfläche, zwei bewachte Loot-Truhen, Mummy-Persistenz, die Ausrichtung und tatsächlich ausgelöste Pfeilfalle sowie 2.000 deterministische Truhen-Loot-Rolls. Die Release-Prüfung bestätigt Originalmaße/Alpha der Keys, separate Dimensionsdateien, Vorlage-Rezept und Loot-Chancen. Client-Darstellung noch nicht visuell im Spiel geprüft.


### Elemental Spellbooks und Chronus (0.20.0)

Am Spelltable: blankes Spellbook + ein Core → Buch des entsprechenden Elements. Keine Lapis- oder XP-Kosten. Neue Bücher haben noch keine erlernten Skills. Jeder Skill kostet beim Erlernen genau einen Chronus Infused Metalinium Ingot. Der zweite Skill verlangt seinen Vorgänger auf Stufe 2; der dritte verlangt seinen Vorgänger auf Stufe 3. Durch 8 / 20 / 40 / 70 erfolgreiche Einsätze steigt jeder einzelne Zauber auf Stufe 2 / 3 / 4 / 5. Maximalstufe 5; fehlgeschlagene oder durch Cooldown blockierte Versuche zählen nicht. Höhere Stufen verstärken Wirkung/Reichweite/Dauer und reduzieren den Cooldown um bis zu 20 %. Lernen verbraucht auch im Creative-Modus einen Ingot.

Standardtasten: **B** öffnet den Skilltree, **V halten** öffnet das Spellwheel (Maus bewegen und loslassen, oder Slot 1–5/Klick), **R** wirkt den ausgewählten Zauber. Alle Tasten sind in den Steuerungseinstellungen veränderbar. Im Skilltree einen erlernten Zauber wählen und auf einen der fünf Slots legen. Ein Rechtsklick mit dem Buch wirkt ebenfalls den ausgewählten Skill. Die Fortschritte, Auswahl und Slots bleiben im jeweiligen Buch gespeichert und erhalten sich beim Weitergeben und Speichern. Bisherige Bücher behalten ihren bisherigen Grundzauber.

| Element | Skills in Reihenfolge |
| --- | --- |
| Feuer | Feuerstoß, Feuerring, Feuerschutz |
| Wasser | Wasserheilung, Wasserwelle, Wasserschutz |
| Blitz | Blitzschlag, Kettenblitz, Blitzschritt |
| Chronus | Chronus-Dash, Zeitfeld, Rücksprung |
| Blut | Blutschnitt, Blutentzug, Blutpanzer |
| Seele | Seelenstoß, Seelenfurcht, Seelensicht |
| Ender | Portable Enderchest, Enderschritt, Enderschutz |
| Elemental | Elementstoß, Elementbarriere, Elementflut |

**Chronus Core:** Elemental Core + Chronus Orb, shapeless. **Ender Core:** Soul Core + Enderperle, shapeless. Dash und Enderschritt stoppen vor festen Blöcken; Rücksprung führt zur freien Position von vor ungefähr drei Sekunden in derselben Dimension. Er benötigt vorhandene Positionshistorie; Tod und Dimensionswechsel löschen diese. Zeitfeld verlangsamt feindliche Gegner in der Umgebung. Flächenzauber treffen keine passiven Tiere, Mitspieler oder Verbündeten.

**Elemental Master Spellbook:** vier Elemental Cores, zwei Essentialium und zwei Chronus Infused Metalinium Ingots um ein blankes Spellbook; Muster `ECE / CBC / MCM` (E = Essentialium, C = Core, B = Buch, M = Ingot). Das Masterbuch startet ohne Elemente. Am Spelltable Masterbuch + Core bindet ein zusätzliches Element und erhält bisherige Skills, Stufen und Slots. Erneutes Freischalten desselben Elements wird ohne Verbrauch abgelehnt. Alle freigeschalteten Elemente können zusammen in einem gemischten Spellwheel liegen.

Optional **Curios 17.0.0-beta.2+26.3**: ein eigener Spellbook-Slot akzeptiert Element-Bücher und Masterbücher. Die Tasten bedienen das ausgerüstete Buch auch mit einer Waffe in der Hand. Priorität bei mehreren Büchern: Haupthand → Nebenhand → Curios-Slot. Ein gemeinsamer Cooldown verhindert Umgehen durch Buchwechsel. Curios bleibt optional und wird nicht eingebettet. Lokale Testflags: `-PwithCurios` und `-PwithBetterCombat`. Aktueller Asset-Generator: `tools/generate_magic.py`.

89 Minecraft-Integrationstests bestanden, sowohl ohne optionale Mods als auch mit Curios 17.0.0-beta.2+26.3 und Better Combat 3.2.2+26.3. Neue Tests prüfen Skill-Kosten und Vorgänger, fünf Slots, Buchwechsel, Master-Elemente und Rezepte, tatsächliches Leveln bis Stufe 5, gemeinsame Cooldowns, Speicherung/alte Bücher, Dash-Kollisionen, Rücksprung und Tod sowie Zaubern und Fortschritt direkt im Curios-Slot. Client-Oberflächen wurden noch nicht visuell im Spiel geprüft.


### Luminous Coating und neue Accessoires (0.21.0)

**Luminous Coating:** shapeless aus Glowing Honey + Essentialium + Amethystsplitter. Im Craftingtable eine Waffe mit einem Coating kombinieren. Der dauerhafte Perk steht grau unter den Verzauberungen. Ein erfolgreicher Treffer gibt dem Gegner 8 Sekunden Glowing. Schaden, Haltbarkeit, Name, Verzauberungen und sämtliche weiteren Itemkomponenten bleiben erhalten; doppelte Beschichtung wird abgelehnt. Funktioniert mit Nahkampfwaffen, Bögen/Armbrüsten, Speeren, Tridents, Katanas und Wurfäxten. Pfeile verwenden die Beschichtung ihrer ursprünglichen Schusswaffe, auch nach einem Waffenwechsel. Zauberschaden erbt die Beschichtung einer gehaltenen Waffe nicht. Weitere Mods werden über native Waffenkomponenten/Angriffsschaden oder den erweiterbaren Itemtag `thedarkdimensions:coatable_weapons` erkannt; Projektile müssen ihre tatsächliche Waffe über den üblichen Minecraft-Hook bereitstellen.

**Astral Prism:** zwei Essentialium, zwei Metalinium Ingots und ein Improved Amethyst. Muster ` E ` / `MAM` / ` E `, E = Essentialium, M = Metalinium Ingot, A = Improved Amethyst. Im Curios-Fokus-Slot oder in der Nebenhand: 5 % kürzerer Zauber-Cooldown. Mehrere Prismen geben keinen mehrfachen Bonus. Wirkt zusätzlich zur normalen Cooldown-Verbesserung durch Zauberstufen.

**Sakura-Talisman:** Muster ` S ` / `EJE` / ` S `, S = String, E = Japanese Essence, J = Cherry Sapling. Im Curios-Talisman-Slot oder in der Nebenhand verlängert er das Zeitfenster zwischen erfolgreichen Katana-Treffern von 2 auf 3 Sekunden. Kein zusätzlicher Rohschaden. Die verlängerte Effekt-Kombo wird auch mit Better Combat gezählt; dessen vorhandene Waffenanimationen bleiben bestehen. Bei zweihändigen Katanas wird die tatsächlich belegte Nebenhand für Accessoires berücksichtigt.

**Ronin-Bandage:** Japanese Essence + Paper + String, shapeless, ergibt vier Bandagen. Benutzung dauert 1,6 Sekunden, funktioniert auch bei voller Hungerleiste und verbraucht eine Bandage. Gibt Regeneration II für 5 Sekunden: bis zu 2 Herzen Heilung über Zeit. Entfernt Frosted, aber weder Blood noch andere Effekte. Die Regeneration stapelt sich nicht mit weiteren Bandagen.

**Windstep-Sandalen:** Muster `J J` / `L L` / `S S`, J = Japanese Essence, L = Leather, S = Improved Stick. Leichte Leder-Rüstung mit 240 Haltbarkeit. Ausgerüstet im Stiefel-Slot: Standardtaste **G** für einen kurzen Bodendash bis 3 Blöcke, mit 5 Sekunden serverseitigem Cooldown und 1 Haltbarkeit Verbrauch pro erfolgreichem Dash. Taste in den Steuerungseinstellungen umstellbar. Stoppt vor Wänden, läuft nicht in ungeladene Chunks; keine wiederholten Luft-Dashes. Kein Spellbook dafür nötig.

Curios und Trinkets Updated sind optional. Beide stellen einen Fokus- und einen Talisman-Slot zur Verfügung; ohne Accessoire-Mod wird die Nebenhand benutzt. Aktueller Ressourcen-Generator: `tools/generate_trinkets.py` (erzeugt auch die bisherigen Ressourcen). Alle fünf neuen Item-Icons sind 16×16. Client-Darstellung noch nicht visuell im Spiel geprüft.

96 Minecraft-Integrationstests bestanden, mit und ohne Curios/Better Combat. Die neuen Prüfungen decken alle registrierten Waffen und ihren Perk-Tooltip, Komponenten-Erhalt, Nahkampf und Projektil-Herkunft, Tridents/Wurfäxte, Rezepte, echte Regeneration und selektives Entfernen von Frosted, Fokus ohne Mehrfachbonus, Curios-Slots, verlängerte Katana-Kombos sowie Sandalen-Ausrüstung, Kollision, Cooldown und Haltbarkeitsverbrauch ab.

### Trinkets Updated Compatibility (0.21.1)

Unterstützt **Trinkets Updated 4.2.0+26.3** direkt über dessen native NeoForge-API (`trinkets_updated`). Kein Curios erforderlich. Im Trinkets-Inventar befinden sich drei eigene Slots am Brust-Slot: **Spellbook**, **Fokus** und **Talisman**. Spellbook akzeptiert alle acht Element-Bücher und das Masterbuch; Fokus akzeptiert Astral Prism, Talisman akzeptiert Sakura-Talisman. Die neuen Slots haben keine kosmetischen Zweitplätze.

Ein ausgerüstetes Buch wird über **B** geöffnet, **V** öffnet das Spellwheel und **R** wirkt den ausgewählten Zauber. Alle Tasten sind umstellbar. Skills, Stufen und die fünf ausgewählten Zauber werden direkt im ausgerüsteten Buch gespeichert. Entfernen/Austauschen des Buchs schließt die Bindung des Skill-Menüs. Astral Prism gibt im Fokus-Slot −5 % Cooldown; Sakura verlängert im Talisman-Slot das Katana-Kombofenster auf 3 Sekunden. Doppelte Accessoires stapeln ihre Boni nicht.

Buchpriorität: Haupthand → Nebenhand → Curios → Trinkets Updated. Ohne Accessoire-Mod bleiben gehaltene Bücher und Nebenhand-Accessoires nutzbar. Beide APIs sind optional und werden nicht in die Mod-JAR eingebettet. Lokaler Testflag: `-PwithTrinkets`; kombinierbar mit `-PwithBetterCombat`.

98 Minecraft-Integrationstests bestanden mit Trinkets Updated 4.2.0+26.3 und Better Combat 3.2.2+26.3 sowie ohne optionale Mods. Zusätzliche Trinkets-Tests prüfen die echten Datapack-Slots und ihre Itemfilter, Zaubern ohne gehaltenes Buch, Fortschritt im ausgerüsteten Buch, Skill-Menü und fünften Spellwheel-Slot, Buchwechsel und die beiden Accessoire-Boni ohne Stapelung. Ohne Trinkets werden die zwei spezifischen Tests übersprungen; der übrige Funktionsumfang läuft unverändert. Client-Oberflächen noch nicht visuell geprüft.



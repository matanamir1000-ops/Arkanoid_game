# Master Refactoring & Feature Blueprint — Arkanoid

**Repository:** `matanamir1000-ops/Arkanoid_game` (branch `main`)
**Created:** 2026-08-03
**Status:** Awaiting approval to begin Phase 1

---

## Context

The codebase is a completed BIU OOP Assignment 5 Arkanoid implementation: a single hardcoded level, a monolithic `Game.run()` loop, three balls, no lives, and console-printed win/loss messages. It works, but every structural seam needed for growth is missing — there is no way to express "a level", no way to run a screen that isn't the game itself, and no way for a block to be anything other than "one hit, then gone".

This blueprint converts it into a multi-level arcade game with an animation framework, a lives system, special block types, falling power-ups, a particle engine, and persistent high scores — without violating the course's OOP constraints.

### Decisions taken (2026-08-03)

| Question | Decision | Consequence |
|---|---|---|
| Ass5 grading constraints | **Free to evolve** — assignment submitted, deadline 2026-06-14 passed | `Game` may be renamed, console win/loss strings may be replaced, block colour rule may be dropped |
| Ball-adopts-block-colour rule | **Remove entirely** | Every hit registers. Prerequisite for multi-hit / steel / exploding blocks |
| Checkstyle enforcement | **Stay clean every phase** | Each phase gates on a zero-error checkstyle run |
| Scope emphasis | **Core engine first** | Phases 0–6 are the approved execution scope. Phases 7–13 are planned but deferred |

---

## Verified Baseline

These were confirmed by reading the actual files and disassembling `biuoop-1.4.jar`. Several contradict common assumptions about this project — **read this section before writing code.**

### `build.xml` needs zero edits for the entire refactor

It contains exactly one class reference:

```xml
<java classname="Ass5Game" classpath="${classpath}" fork="true"/>
```

`javac srcdir="src"` compiles all subdirectories recursively. New packages, new classes, renamed classes — none of it touches the build file. **The only immovable constraint is that `Ass5Game` stays in the default package at `src/Ass5Game.java`.**

### Checkstyle: what is actually enforced

`MagicNumber` and `HiddenField` are **commented out** (`biuoop.xml:156`, `biuoop.xml:161`). Inline numeric literals are permitted — this substantially reduces the verbosity cost of the particle and visual-effects work.

Rules that **are** live and are easy to trip in new code:

- **`RedundantModifier`** — do **not** write `public` on interface method declarations. Every interface skeleton in this document omits it deliberately.
- **`InterfaceIsType`** — no constants-only interfaces. Shared constants live in a `final` class with a private constructor.
- **`VisibilityModifier`** — no `public`/`protected`/package-private fields. This is the rule that makes `Block` subclassing awkward and steers Phase 7 toward composition.
- **`JavadocParagraph`** — multi-paragraph Javadoc requires `<p>` tags.
- `FinalClass`, `MethodLength` (150), `ParameterNumber` (7), `MissingJavadocMethod`, `MissingJavadocType`, `AvoidStarImport`, `UnusedImports`, `TodoComment`, `LineLength` (120), `FileTabCharacter`, trailing-whitespace regex.
- `DesignForExtension` is **not** enabled.

### Two pre-existing checkstyle errors

```
src\game\Game.java:206:56: Expected @param tag for 'remover'. [JavadocMethod]
src\game\Game.java:206:77: Expected @param tag for 'tracker'. [JavadocMethod]
```

Phase 0 fixes these so that "checkstyle is clean" becomes a usable binary gate for every later phase.

### `biuoop` API limits that drive design

- **`DrawSurface` has no alpha channel.** `setColor` throws `AlphaChannelNotSupportedException`. Fading **must** be RGB interpolation toward the background colour, using the 3-arg `new Color(r, g, b)`. Never the 4-arg constructor.
- **`drawText(int x, int y, String text, int fontSize)`** — no font family, no style, no text measurement API. Glow is layered draws at pixel offsets. Centring requires a width heuristic.
- **All draw coordinates are `int`.** Particle positions must be stored as `double` and cast at draw time, or sub-pixel motion quantises to zero.
- **`KeyboardSensor` has no key-release event.** Detecting "pressed and released" requires manual edge-detection state. This is the entire reason `KeyPressStoppableAnimation` exists.
- **Unmapped character keys work.** `KeyboardSensorImpl.keyPressed` falls back to `String.valueOf(e.getKeyChar())` for keys outside its code map, so `isPressed("p")` is legitimate — but it is case-sensitive, so pause checks must be `isPressed("p") || isPressed("P")`.

**Key budget** (7 documented constants + verified char keys):

| Action | Key |
|---|---|
| Paddle move | `LEFT_KEY` / `RIGHT_KEY` |
| Pause | `"p"` / `"P"` |
| Dismiss pause and end screens | `SPACE_KEY` |
| Fire laser | `UP_KEY` |

Using `UP_KEY` for the laser rather than `SPACE_KEY` removes the fire-vs-dismiss ambiguity entirely.

### Rename blast radius

`Game` appears at **19 sites across 9 files**: `Ass5Game.java`, `game/Game.java`, `game/GameItem.java`, `game/BlockRemover.java`, `game/BallRemover.java`, `sprites/Ball.java`, `sprites/Block.java`, `sprites/Paddle.java`, `sprites/ScoreIndicator.java`.

### `.gitignore` hazards

- **`*.md` is ignored** (`.gitignore:17`) with only `!README.md` re-included. **This file will not be committed** until Phase 0 adds `!Plan.md`.
- `*.txt` is ignored — correct and desirable for `highscores.txt`. Verified that no level-data `.txt` files are needed; every level is a Java class.
- `out/` (IntelliJ output) reported inconsistently between tooling runs. Phase 0 adds an explicit `out/` entry — idempotent and harmless either way.

---

## Executive Architectural Blueprint

### Target package layout

```
src/Ass5Game.java          default package — NEVER moves; becomes the composition root
src/geometry/              Point, Line, Rectangle, Velocity, Geometry (new: shared epsilon)
src/collision/             Collidable, CollisionInfo, GameEnvironment, HitListener, HitNotifier
src/game/                  GameLevel, GameFlow, SpriteCollection, Counter, Sprite, GameItem,
                           BlockRemover, BallRemover, ScoreTrackingListener, ExplosionListener
src/animation/      NEW    Animation, AnimationRunner, KeyPressStoppableAnimation,
                           CountdownAnimation, PauseScreen, GameOverScreen, WinScreen,
                           HighScoresAnimation, TextRenderer
src/levels/         NEW    LevelInformation, Level1..Level5, backgrounds
src/sprites/               Ball, Block, Paddle, ScoreIndicator, LivesIndicator,
                           LevelNameIndicator, Laser, BlockBehavior + impls, BlockFactory
src/particles/      NEW    Particle, ParticleSystem, ParticleEmitter + impls, ColorFade
src/powerups/       NEW    PowerUp, FallingPowerUp, Catcher, ExtraBall, PaddleExpansion,
                           LaserPaddle, SlowBall, PowerUpDropper
src/highscores/     NEW    ScoreInfo, HighScoresTable
```

### Patterns applied

| Pattern | Where | Why |
|---|---|---|
| **Observer** | `HitListener` / `HitNotifier` (existing), extended with `ExplosionListener`, `ParticleHitListener`, `PowerUpDropper` | All world side-effects of a block being hit, without the block knowing about the world |
| **Strategy** | `BlockBehavior` composed into `Block`; `ParticleEmitter`; `PowerUp` | Varying behaviour without subclassing and without type checks |
| **Decorator** | `KeyPressStoppableAnimation` wrapping any `Animation` | Adds dismiss-on-keypress to screens that don't know about input |
| **Template / inversion of control** | `AnimationRunner.run(Animation)` | One timing loop drives every screen including the game itself |
| **Composite** | `SpriteCollection`, `ParticleSystem` | A collection of sprites that is itself a sprite |
| **Factory** | `BlockFactory` | Keeps level definitions declarative and under `MethodLength` |

### How type checks are avoided

The rule is: **never ask what an object is; ask what state it is in, or tell it to act.**

- A listener does not ask "is this a steel block?" — it asks `beingHit.isDestroyed()`.
- The win condition does not count block types — `LevelInformation.numberOfBlocksToRemove()` simply excludes unbreakable blocks.
- A power-up does not ask "did I hit a paddle?" — it asks a `Catcher` whether it `catches(area)`.
- Applying a power-up is not a switch on its type — it is `effect.apply(level)`.

---

## Execution Scope

**Phases 0–6 are approved for execution now** (core engine). Phases 7–13 are fully planned below but deferred pending review of the core engine.

---

## Git Workflow (every phase)

Each phase is self-contained, compiles, runs, and is playable. No phase is left uncommitted.

**Three gates before every commit:**

| Gate | Command | Pass condition |
|---|---|---|
| **G1 Compile** | `ant compile` | `BUILD SUCCESSFUL` |
| **G2 Run** | `ant run` | Game launches, is playable, closes cleanly |
| **G3 Checkstyle** | see below | `Audit done.` with 0 errors |

Checkstyle on Windows PowerShell:

```powershell
$f = Get-ChildItem src -Filter *.java -Recurse | ForEach-Object { $_.FullName }
java -jar checkstyle-8.44-all.jar -c biuoop.xml $f
```

**Commit sequence per phase:**

```powershell
git add src/ .gitignore
git status                      # confirm no out/ or bin/ artifacts staged
git commit -m "<phase message>"
git push origin main
git tag -a phase-N -m "Phase N - <summary>"
git push origin phase-N
```

Because all three gates run before every commit, **every commit on `main` is a
working, playable state.** There is no point in the history where the project is
broken. That is what makes rolling back safe.

### Two build gotchas (learned in Phase 2)

**`ant compile` alone hides rename and signature errors.** `build.xml` puts `bin`
on the compile classpath, so stale `.class` files from the previous build still
resolve. Renaming `Game` produced only 3 errors incrementally but 18 after
`ant clean`. **Any phase that renames a type or changes a signature must run
`ant clean` before trusting the error list** — that includes Phase 3's constructor
changes and Phase 4's `LevelInformation` wiring.

**PowerShell 5.1 mangles `git commit -m` messages containing double quotes.**
Even inside a single-quoted here-string, embedded `"` breaks native-command
argument passing and git receives fragments as pathspecs. Write the message to a
file and use `git commit -F <file>` whenever the message quotes a string literal.

## Rolling Back a Phase

Each phase is one atomic, tagged commit, so any phase can be undone.

**Mid-phase, before the commit** — nothing has entered history:

```powershell
git restore src/
```

**The most recent phase, already pushed** — preferred, because history is preserved
and no force push is needed:

```powershell
git revert phase-N
git push origin main
```

**Erasing a phase entirely** — rewrites history, requires a force push. Safe only
because this is a single-contributor repository, and never to be run without
explicit confirmation:

```powershell
git reset --hard phase-<N-1>
git push --force origin main
```

**Reverting an earlier phase after later phases were built on it is the one risky
case.** The dependencies in *Ordering Risks* below are not all compile-time — some
are behavioural. Reverting Phase 0 after Phase 7 exists still compiles, but
reintroduces the `ConcurrentModificationException` that dense explosion clusters
trigger. Check the Ordering Risks table before reverting anything that is not the
most recent phase.

### Phase tags

| Tag | Commit | Phase |
|---|---|---|
| `phase-0` | `5f0167b` | Hygiene, seams, repo config |
| `phase-1` | `324b70f` | Remove the colour-match gate |
| `phase-2` | `57e5399` | Rename `Game` to `GameLevel` |

---

# Approved Scope — Phases 0 through 6

## Phase 0 — Hygiene, seams, repo config

Zero behaviour change. Establishes the clean-checkstyle baseline that all later gates depend on.

**Modify**

- `src/game/Game.java` — add the two missing `@param` tags at line 206. Closes the pre-existing checkstyle failure.
- `src/collision/GameEnvironment.java` — `getClosestCollision` must iterate a defensive copy:
  ```java
  for (Collidable collidable : new ArrayList<Collidable>(this.collidables)) {
  ```
  Today this is latent — nothing mutates the list inside that loop. Phase 7's chain explosions and Phase 9's power-ups make removals happen during collision resolution. One line now removes an entire class of future `ConcurrentModificationException`.
- `src/geometry/Point.java`, `Line.java`, `sprites/Ball.java`, `Block.java`, `Paddle.java` — delete the five duplicated `COMPARISON_THRESHOLD = 0.000001` declarations and reference one source.
- `.gitignore` — add `!Plan.md` (so this file is tracked) and an explicit `out/` entry.

**Create**

- `src/geometry/Geometry.java`
  ```java
  public final class Geometry {
      private Geometry() { }
      public static double epsilon();   // returns 1.0E-6
  }
  ```
  A **method**, not a `public static final` field — `VisibilityModifier` is live and its handling of public static final fields is version-sensitive. A static accessor is unambiguously safe.

**Verification** — Checkstyle drops from 2 errors to 0. Gameplay pixel-identical: 57 blocks, 3 balls, same bounce behaviour, same win/loss strings. `git status` shows `Plan.md` as trackable.

**Commit** — `chore: fix checkstyle errors, consolidate epsilon, harden gitignore`

---

## Phase 1 — Remove the colour-match rule

Isolated behaviour change, its own commit so it is trivially revertible.

**Modify**

- `src/sprites/Block.java` — delete the `ballColorMatch(hitter)` gate and the `recolorsHitter` flag. The tail of `hit(...)` becomes an unconditional `this.notifyHit(hitter);`. The reflection math at lines 79–89 is **not touched**.
- `src/game/Game.java` — drop the `setRecolorsHitter(true)` call in `createBlockPattern`.

**Why this must happen before special blocks:** with the rule in place, a ball adopts a block's colour on hit #1, so hit #2 on a multi-hit block is a silent no-op. The feature is unimplementable until this is gone.

**Verification** — Every block dies in one hit regardless of ball colour. Balls stay white. Level still clears at 57 blocks and scores 285 + 100 bonus.

**Commit** — `refactor: remove colour-match gate so every block hit registers`

---

## Phase 2 — `Game` → `GameLevel`

Mechanical rename, standalone commit, **zero logic changes**.

**Rationale for doing it now:** the blast radius is 19 sites in 9 files today. After Phase 9 adds ~20 classes calling `addToGame(...)`, it roughly triples. The cost is strictly monotonic. And a class named `Game` sitting beside `GameFlow`, where `Game` is actually *one level*, is a permanent readability trap.

**Procedure — do not run a blind find/replace.** `Game` is a substring of `GameEnvironment`, `GameItem`, `Ass5Game`, `addToGame`, `removeFromGame`, and the literal `"Game Over.\n..."`.

1. `git mv src/game/Game.java src/game/GameLevel.java`
2. Change only the class declaration and the constructor name.
3. Run `ant compile`. Fix each of the ~18 errors javac reports, one at a time. **Javac is the checklist.**
4. Update `GameItem.addToGame(GameLevel game)` and all four implementors, plus both `removeFromGame` overloads. `GameItem` is a local invention, not spec-declared, so its signature is free to change.
5. `Ass5Game.main` becomes `GameLevel game = new GameLevel();`

Keep the **method** names `addToGame` / `removeFromGame` — they stay accurate and renaming them is pure churn.

**Verification** — `git diff --stat` shows exactly 9 files and the diff contains zero logic changes. Behaviour identical.

**Commit** — `refactor: rename Game to GameLevel across all call sites`

---

## Phase 3 — Animation framework + GUI lifetime inversion

Highest-leverage structural phase.

**The critical, easy-to-miss part:** `GUI` construction must move out of `initialize()`. Today line 101 does `this.gui = new GUI("Arkanoid", 800, 600)` inside `initialize()`. With multiple levels, that opens a new window per level. The `GUI` is created once in `Ass5Game` and injected.

**Create**

- `src/animation/Animation.java`
  ```java
  public interface Animation {
      void doOneFrame(DrawSurface d);
      boolean shouldStop();
  }
  ```
- `src/animation/AnimationRunner.java`
  ```java
  public class AnimationRunner {
      public AnimationRunner(GUI gui, int framesPerSecond);
      public void run(Animation animation);
  }
  ```
  The body is the timing loop lifted **verbatim** from `Game.run()` lines 246–257 — `System.currentTimeMillis()` delta, `sleeper.sleepFor`. The 60 FPS pacing is moved, not rewritten.
- `src/animation/KeyPressStoppableAnimation.java`
  ```java
  public class KeyPressStoppableAnimation implements Animation {
      public KeyPressStoppableAnimation(KeyboardSensor sensor, String key, Animation animation);
      void doOneFrame(DrawSurface d);
      boolean shouldStop();
  }
  ```
  The decorator exists because biuoop has **no key-release event**. Constructor sets `isAlreadyPressed = true`. Each frame:
  ```
  decorated.doOneFrame(d);
  if (sensor.isPressed(key)) {
      if (!isAlreadyPressed) { stop = true; }
  } else {
      isAlreadyPressed = false;
  }
  ```
  **Without the `true` initialiser, holding SPACE through a level clear instantly blows past the win screen.** This is the single most common bug in this refactor.
- `src/animation/PauseScreen.java` — `shouldStop()` always returns `false`. It is always wrapped: `new KeyPressStoppableAnimation(sensor, SPACE_KEY, new PauseScreen())`.

**Modify**

- `src/game/GameLevel.java`
  - `implements Animation`
  - Constructor becomes `GameLevel(AnimationRunner runner, KeyboardSensor keyboard, GUI gui)`. Keyboard is passed explicitly so `createPaddle()` no longer reaches through `this.gui`.
  - `doOneFrame(DrawSurface d)` = draw sprites → `notifyAllTimePassed()` → pause check
  - `shouldStop()` = `!this.running`, set false when either counter hits zero
  - `run()` becomes a thin wrapper: `this.running = true; this.runner.run(this);` then the existing win/loss printouts and `gui.close()`
- `src/Ass5Game.java` — creates `GUI`, `AnimationRunner`, `GameLevel`; calls `initialize()` and `run()`

**Pause wiring inside `doOneFrame`:**
```java
if (this.keyboard.isPressed("p") || this.keyboard.isPressed("P")) {
    this.runner.run(new KeyPressStoppableAnimation(
            this.keyboard, KeyboardSensor.SPACE_KEY, new PauseScreen()));
}
```
This is a reentrant call into the runner from inside a frame. It is the standard course pattern and is safe — there is no threading.

**Verification** — Game plays identically. Press `p` → pause overlay; SPACE resumes and does **not** immediately re-pause. Hold SPACE while pressing `p` → screen still appears and requires a fresh press.

**Commit** — `feat: add Animation framework and refactor GameLevel into an Animation`

---

## Phase 4 — `LevelInformation` and level extraction

**Create**

- `src/levels/LevelInformation.java`
  ```java
  public interface LevelInformation {
      int numberOfBalls();
      java.util.List<Velocity> initialBallVelocities();
      int paddleSpeed();
      int paddleWidth();
      String levelName();
      Sprite getBackground();
      java.util.List<Block> blocks();
      int numberOfBlocksToRemove();
  }
  ```
  **Contract to document in the Javadoc:** `blocks()` must return a freshly constructed list of freshly constructed `Block`s on every call. `GameLevel.initialize()` mutates blocks — it adds listeners, and from Phase 7, hit-point state. A cached list would resurrect half-destroyed blocks when replaying a level after losing a life.
- `src/levels/Level1DirectHit.java` — port of the current 57-block staircase, so this phase stays behaviour-preserving.
- `src/sprites/LevelNameIndicator.java` — `implements Sprite, GameItem`.

**Modify**

- `src/game/GameLevel.java` — `createBlockPattern` / `createBalls` / `createPaddle` / `createBackground` are deleted, replaced by reads off the level info. **`remainingBlocks` is initialised from `levelInfo.numberOfBlocksToRemove()`, not from `blocks().size()`.** This is the seam that makes unbreakable blocks possible in Phase 7 with no type check anywhere.
- `src/Ass5Game.java` — passes `new Level1DirectHit()`

**Verification** — Identical gameplay to Phase 3. Level name renders in the top strip. `numberOfBlocksToRemove()` returns 57 and clearing still wins.

**Commit** — `feat: introduce LevelInformation and extract level 1 definition`

---

## Phase 5 — Lives, `GameFlow`, countdown, multi-level

**Create**

- `src/game/GameFlow.java`
  ```java
  public class GameFlow {
      public GameFlow(AnimationRunner runner, KeyboardSensor keyboard, GUI gui);
      public void runLevels(java.util.List<LevelInformation> levels);
  }
  ```
  Owns the two persistent `Counter`s — `score` and `lives` (start 3). Per level:
  ```
  level = new GameLevel(info, runner, keyboard, gui, score, lives);
  level.initialize();
  while (level.getRemainingBalls() > 0 && level.getRemainingBlocks() > 0) {
      level.playOneTurn();
      if (level.getRemainingBalls() == 0) { lives.decrease(1); }
  }
  if (lives.getValue() == 0) { break; }
  ```
- `src/animation/CountdownAnimation.java`
  ```java
  public class CountdownAnimation implements Animation {
      public CountdownAnimation(double numOfSeconds, int countFrom, SpriteCollection gameScreen);
  }
  ```
  `doOneFrame` calls `gameScreen.drawAllOn(d)` but **never `notifyAllTimePassed()`** — the level is frozen beneath the numbers. Time by wall clock, not frame counting, because the runner's real FPS drifts. Guard the off-by-one: the "1" must display for its full slice before `shouldStop()` flips, or there is a visible flash.
- `src/sprites/LivesIndicator.java` — `implements Sprite, GameItem`

**Modify**

- `src/game/GameLevel.java` — add `playOneTurn()` (create balls, reposition paddle, run countdown, then `runner.run(this)`), `createBallsOnTopOfPaddle()`, `getRemainingBalls()`, `getRemainingBlocks()`, and a `private List<Ball> balls` with `addBall` / `removeBall` / `getBalls`. **That ball list is what Phase 9's SlowBall and ExtraBall act on, and it is why `Ball` never needs a back-reference to the level.**
- `src/game/BallRemover.java` — `hitEvent` must also call `level.removeBall(hitter)`. Otherwise the level's ball list leaks dead balls and SlowBall mutates ghosts.
- `src/sprites/Paddle.java` — add `resetPosition()`; take speed from `LevelInformation.paddleSpeed()` instead of the hardcoded `STEP = 7`.
- `src/Ass5Game.java` — builds `List<LevelInformation>`, calls `GameFlow.runLevels`

**Verification** — Ball falls → 3-2-1 countdown → ball respawns on paddle → lives 3→2. Third loss ends the game. Score persists across a level transition. Level 1 clear advances to a (duplicated for now) Level 2.

**Commit** — `feat: add lives system, GameFlow level sequencing, and countdown animation`

---

## Phase 6 — End screens

**Create**

- `src/animation/WinScreen.java`, `src/animation/GameOverScreen.java` — both `shouldStop()` returns `false`; both wrapped in `KeyPressStoppableAnimation(sensor, SPACE_KEY, ...)` by `GameFlow`.

**Modify**

- `src/game/GameFlow.java` — shows the screens; the `+100` level-clear bonus moves here, awarded once per level cleared.
- `src/game/GameLevel.java` — the `System.out.println` win/loss calls move to `GameFlow`. Since Ass5 constraints are lifted, the strings are now free to change; **recommend keeping them anyway** as a cheap headless smoke-test signal.

**Verification** — Lose 3 lives → animated game-over screen → SPACE → window closes. Same for the win path. Bonus awarded exactly once per level.

**Commit** — `feat: replace terminal-only endings with animated win and game over screens`

---

# Deferred Roadmap — Phases 7 through 13

Fully designed, not yet approved for execution.

## Phase 7 — Special blocks via composed `BlockBehavior`

### Recommendation: Strategy (composition), not subclassing

1. **`VisibilityModifier` is live**, so a subclass cannot receive `protected Rectangle shape`. A hierarchy would need protected accessors on everything, growing `Block`'s public surface solely for subclasses.
2. **`Block.hit()` holds delicate edge-threshold reflection math.** Subclasses would duplicate it or call `super.hit(...)` — but `super.hit()` already fires `notifyHit`, so an override wanting to decide "do I die?" *before* notifying must restructure the parent anyway. Composition keeps that math in exactly one place, untouched.
3. **Behaviours compose.** A block that is both multi-hit *and* explosive is `MultiHitBehavior(3)` plus an `ExplosionListener`, not a `MultiHitExplodingBlock` class. Subclassing gives the classic combinatorial explosion.
4. **Precedent already exists** — `setRecolorsHitter(boolean)` was a degenerate hand-rolled strategy flag. This generalises rather than inventing a new idiom.

**Create (`src/sprites/`)**

```java
public interface BlockBehavior {
    void registerHit(Ball hitter);
    boolean isDestroyed();
    java.awt.Color displayColor(java.awt.Color baseColor);
    int pointValue();
}
```

- `PlainBehavior` — dies on first hit, 5 points. Preserves current scoring.
- `MultiHitBehavior(int hitPoints, Color[] palette)` — decrements; `displayColor` returns `palette[hitPoints - 1]`; destroyed at ≤ 0; 5 points per hit, +10 on the kill.
- `SteelBehavior` — no-op hit, never destroyed, 0 points. **Steel blocks are simply excluded from `numberOfBlocksToRemove()`**, so the win condition is unaffected without anyone checking a type.

**Modify `src/sprites/Block.java`** — add `private final BlockBehavior behavior` and a 3-arg constructor; the existing 2-arg constructor delegates with `new PlainBehavior()`, keeping borders and the death region untouched. Add `applyDamage(Ball)`, `isDestroyed()`, `getPointValue()`, `getCenter()`. `drawOn` uses `behavior.displayColor(this.color)`.

**Modify `src/game/BlockRemover.java`** — the entire special-block system reduces to a three-line guard:
```java
public void hitEvent(Block beingHit, Ball hitter) {
    if (!beingHit.isDestroyed()) { return; }
    beingHit.removeFromGame(this.level);
    beingHit.removeHitListener(this);
    this.remainingBlocks.decrease(1);
}
```

**Exploding blocks are a `HitListener`, not a behaviour.** Side effects on the *world* belong in listeners, matching the existing idiom; behaviours own only the block's own state.

`src/game/ExplosionListener.java` — on hit, iterate a defensive copy of neighbours and call `applyDamage` on each within radius, which re-enters that neighbour's own listeners so chains happen naturally.

**Two hazards:**
- **Unbounded recursion.** A ring of exploding blocks chains infinitely. Guard with a re-entrancy flag plus a static depth cap (≈4), and skip neighbours already `isDestroyed()`.
- **Mutation during dispatch.** `applyDamage` → `notifyHit` → `BlockRemover` → `removeCollidable` happens while `Ball.applyCollisionOffset` is executing. Phase 0's defensive copy is what makes this provably safe rather than accidentally safe.

`src/sprites/BlockFactory.java` — static `plain` / `multiHit` / `steel` / `exploding` factories keep level classes declarative and under `MethodLength`.

**Commit** — `feat: add multi-hit, steel, and exploding blocks via composed BlockBehavior`

## Phase 8 — Particle system

**Key decision: particles do NOT live in `SpriteCollection`.** One `ParticleSystem` is the only registered `Sprite`; it owns a `List<Particle>` internally and reaps dead ones with explicit `Iterator.remove()`. This sidesteps self-removal-during-iteration entirely, keeps `SpriteCollection` from churning hundreds of entries per second, and makes the hard cap enforceable in one place.

- `Particle implements Sprite` — fields `double x, y, dx, dy` (**double**, cast at draw time), `int life, maxLife`, `Color baseColor`, `int size`.
- `ColorFade.towards(Color from, Color to, double t)` — **the alpha workaround.** Linear RGB interpolation toward the background, clamped to `[0,255]`, 3-arg `Color` constructor only. `t = 1.0 - (life / (double) maxLife)`.
  *Accepted trade-off:* this only reads as fading against a uniform background. With the Phase 10 starfield, stars show through as hard pixels rather than blending. It is the only option this API allows.
- `ParticleSystem implements Sprite, GameItem` — `MAX_PARTICLES = 400`; `emit` is a **no-op when full** (drop-newest beats drop-oldest: it stops a chain explosion from visually erasing the burst that started it). Added to the level **last** so it draws on top.
- `ParticleEmitter` interface with `RadialBurstEmitter`, `TrailEmitter`, `FireworkEmitter`.
- `ParticleHitListener` — fires a burst when `beingHit.isDestroyed()`. **Depends on Phase 7.**
- `BallTrail implements Sprite` — holds a `Ball` and a `ParticleSystem`. **`Ball` itself is not modified at all.**

**Commit** — `feat: add particle system with emitters for block bursts and ball trails`

## Phase 9 — Power-ups

**Paddle detection without `instanceof`: a narrow `Catcher` interface.**

```java
public interface Catcher {
    boolean catches(geometry.Rectangle area);
}
```

The falling object holds a `Catcher`, not a `Paddle`, so it is structurally incapable of type-checking anything.

**Why this beats reading `paddle.getCollisionRectangle()` directly:** that method returns only `visibleSlice()`, so during a screen-wrap the wrapped half is invisible to any external AABB test and catches would silently fail there. `Paddle.catches(...)` can check the visible slice **and** the ghost — it is the only place with that knowledge.

**Why not make the power-up a `Collidable`:** it would then be hit by *balls*, and `Collidable.hit` returns a `Velocity`, which is meaningless for a pickup.

- `PowerUp` interface — `apply(GameLevel)`, `getColor()`, `getSymbol()`. Application is polymorphic; nothing switches on type.
- `FallingPowerUp implements Sprite, GameItem` — one concrete falling object parameterised by a `PowerUp` effect.
- `ExtraBall`, `PaddleExpansion`, `LaserPaddle`, `SlowBall`.
- `PowerUpDropper implements HitListener` — probabilistic spawn on block destruction.
- **`Laser extends Ball`** — see the `HitListener` discussion below.

**Required `Paddle` fixes in this same commit:**
- Clamp width to `MAX_PADDLE_WIDTH = 300`. **`handleScreenWrap()` breaks if width ≥ `INNER_WIDTH` (760):** both the `x < INNER_LEFT` and `x + w > INNER_RIGHT` branches become true simultaneously and the first `return`s early, silently dropping one slice.
- **Expand symmetrically** (`upperLeft.x -= delta / 2`), then re-run wrap logic in the same call. Growing from the upper-left lurches the paddle right by the full delta in one frame and can cross a wrap boundary with a stale ghost.
- **Call `handleScreenWrap()` unconditionally at the end of `timePassed()`.** It is currently only reachable from `moveLeft`/`moveRight`. A power-up that expands the paddle while the player isn't holding a key would never refresh the ghost. **This fix must land in the same commit as `expand()`.**
- Add `removeFromGame(GameLevel)` calling `destroyGhost()` — otherwise a paddle that dies mid-wrap leaves an orphaned invisible `Collidable` that balls bounce off in the next level.

*Bounce physics scale for free:* `hit()` computes `regionWidth` off the logical rectangle and `PaddleGhost.hit()` translates back into the parent frame. Both are width-relative, so a wider paddle keeps five proportional regions with no changes.

**Commit** — `feat: add falling power-ups with extra ball, expansion, laser, and slow ball`

## Phase 10 — Visual polish

- `TextRenderer` (final, private ctor) — glow is **layered draws at pixel offsets**, outermost and dimmest first: halo (≈70% toward background) at (±3,0),(0,±3),(±2,±2); mid (≈35%) at (±1,0),(0,±1); core at (0,0). Also `centeredX(...)` — there is **no text-measurement API**, so approximate with `text.length() * fontSize * 0.55`. Every centred string routes through this one method so the fudge factor is tunable in one place.
- Pulsing — `double p = (Math.sin(frame * RATE) + 1.0) / 2.0` driving colour lerp or font size. **Pulse colour for centred text, size only for left-anchored text**, since the length heuristic makes size-pulsing jitter horizontally.
- `StarfieldBackground`, `GridBackground` — seeded `Random` so layouts are stable per level.
- Fireworks on `WinScreen`, crumbling blocks on `GameOverScreen`.

*Performance note:* glow multiplies `drawText` calls ~11×. If 60 FPS drops with starfield + particles + glow simultaneously, reduce halo ring count first.

**Commit** — `feat: add neon text rendering, pulsing effects, and animated backgrounds`

## Phase 11 — High score persistence

- `ScoreInfo`, `HighScoresTable` (`add`, `getRank`, `load`, `save`, `loadFromFile`), `HighScoresAnimation`.
- Format: `name|score` per line via `BufferedReader`/`PrintWriter`. Sanitise `|` and newlines out of names on the way in.
- **Missing file:** check `file.exists()` first and return an empty table — do not rely on catching `FileNotFoundException`, because a permission-denied path needs the same graceful degradation.
- **Corrupt file must never prevent startup:** catch `IOException`, log to `System.err`, return an empty table. Skip malformed lines individually (`NumberFormatException` around `parseInt`).
- Close streams in `finally` with a null check and nested try/catch. Avoid try-with-resources to stay in course canon.
- Sorted insert via a manual loop into a ≤10-element `ArrayList` — **not** `Collections.sort` with a `Comparator`.
- Name prompt via `gui.getDialogManager().showQuestionDialog(...)`.
- `highscores.txt` is correctly covered by the `*.txt` ignore rule.

**Commit** — `feat: add high score table with file persistence`

## Phase 12 — Level pack

Five levels mixing plain, multi-hit, steel, and exploding blocks with distinct backgrounds, ball counts, paddle widths and speeds.

**Most likely bug in this phase:** a mismatch between `numberOfBlocksToRemove()` and the actual breakable count either hangs the level forever or ends it early. Verify each level explicitly.

**Commit** — `feat: add five distinct levels with mixed block types`

## Phase 13 — Final hardening

```powershell
Get-ChildItem src -Filter *.java -Recurse |
    Select-String -Pattern "instanceof|getClass\(|isInstance|awt\.Graphics"
```
Must return nothing. Also grep for `->` and `.stream(` (no lambdas/streams), and confirm every `new Color(` has 3 arguments. Then `ant clean; ant compile; ant run` from a clean tree, and regenerate `diagram.pdf`.

**Commit** — `chore: final checkstyle pass and constraint verification`

---

# Deep Dives

## The `HitListener.hitEvent(Block, Ball)` signature problem

**The design does not require changing it, and it should not be changed.**

The composition approach keeps exactly one concrete `Block` class, so `beingHit` is always correctly typed and all three existing listeners keep compiling — only their bodies change.

**Where it actually strains: the laser.** `hitEvent` demands a non-null `Ball hitter`, but a laser bolt is not a ball. Three options were considered:

1. **Pass `null`.** Audited: `BlockRemover` and `ScoreTrackingListener` never dereference `hitter`; only `BallRemover` does, and lasers travel upward so they never touch the death region. Currently safe — but a landmine for any listener added later.
2. **`Laser extends Ball` — recommended.** A laser genuinely *is* a small fast projectile with a position and velocity. It overrides `timePassed()` to travel straight up, tests rectangle overlap against `level.getBlocks()`, calls `block.applyDamage(this)`, and removes itself. It is never registered in `remainingBalls` and never travels downward, so `BallRemover` never sees it. Zero nulls, zero signature change, zero new API surface.
3. **Widen `HitListener` to a `Hitter` interface — rejected.** It would cascade into `Collidable.hit(Ball, Point, Velocity)` because the two are coupled through `Block.hit → notifyHit`, for no benefit that option 2 doesn't already deliver.

**If a non-`Block` notifier is ever needed** (ball-hit-paddle telemetry, power-up catch events): do **not** widen `HitListener`. Add a parallel `CollisionListener` interface. Additive, costs nothing, leaves the existing contract untouched.

## How `Ball` gets level access — it doesn't

**Invert the dependency.** `Ball` is not modified at all in Phases 8 or 9.

- **Ball trail particles** → `BallTrail` holds `(Ball, ParticleSystem)`. The observer holds the observed; the observed holds nothing.
- **Block-destruction particles** → `ParticleHitListener`. The existing listener wiring already delivers `(Block, Ball)`.
- **SlowBall / ExtraBall** → act on `GameLevel`'s `List<Ball>` from Phase 5. `PowerUp.apply(GameLevel)` iterates `level.getBalls()`.

`Ball` keeps exactly one outward reference — `private GameEnvironment environment`, which already exists.

*The tempting alternative*, adding `private GameLevel level` to `Ball.addToGame`, is one line and works, but it means every ball must be told about level teardown or it holds a stale reference across lives, it invites `Ball` to grow level-manipulation logic that belongs in listeners, and it deepens the `sprites ↔ game` cycle. Don't add to it when the observer inversion is free.

---

# Ordering Risks

Violating these forces rework.

| Must precede | Reason |
|---|---|
| **Phase 0 → 7** | `getClosestCollision` iterating the live list is only *latently* safe. Chain explosions remove collidables during collision resolution. Reversed, you get an intermittent `ConcurrentModificationException` that reproduces only on dense clusters — the worst kind to debug. |
| **Phase 1 → 7** | The colour-match gate makes multi-hit blocks literally unimplementable. |
| **Phase 2 → everything** | 19 rename sites now vs. ~60 after Phase 9. Cost is strictly monotonic. |
| **Phase 3 → 5** | `GameFlow` needs `AnimationRunner`; `playOneTurn()` needs `Animation`. More subtly, **the GUI-lifetime inversion is in Phase 3** — attempt multi-level first and every level opens a new window. |
| **Phase 4 → 5** | `runLevels(List<LevelInformation>)` has no argument type otherwise. |
| **Phase 4 → 7** | `numberOfBlocksToRemove()` is the seam that lets steel blocks exist. Reversed, the win counter derives from `blocks().size()` and steel blocks make levels unwinnable. |
| **Phase 5 → 9** | `ExtraBall` and `SlowBall` both need the level-owned ball list and ball counter. |
| **Phase 7 → 8** | `ParticleHitListener` is gated on `isDestroyed()`. Without it, multi-hit blocks emit a full destruction burst on every non-fatal hit. |
| **Phase 5 → 11** | High scores are meaningless without a cross-level persistent score counter. |

**Safely reorderable:** 8 ↔ 10, 11 ↔ 12.

**Subtlest coupling — Phase 9 depends on a Phase 9-internal fix.** `handleScreenWrap()` is only reachable from `moveLeft`/`moveRight`, but `PaddleExpansion` mutates width from outside the movement path. The unconditional-call fix must land in the same commit as `expand()`, or the power-up produces a ghost desync that appears only when the player is standing still at a screen edge.

---

# Critical Files

| File | Role in the refactor |
|---|---|
| `src/game/Game.java` → `GameLevel.java` | The monolithic `run()` (lines 226–260) splits into `Animation`/`AnimationRunner`; `initialize()` (lines 100–224) is gutted by `LevelInformation`; the GUI construction at line 101 must move out |
| `src/sprites/Block.java` | Receives the `BlockBehavior` strategy; the reflection math at lines 79–89 must stay untouched |
| `src/sprites/Paddle.java` | Expansion, `Catcher`, laser firing, and the `handleScreenWrap`/`PaddleGhost` fixes at lines 190–249 |
| `src/collision/HitListener.java` | The `hitEvent(Block, Ball)` signature that survives the whole refactor unchanged |
| `src/game/BlockRemover.java` | Where a three-line `isDestroyed()` guard replaces all type checking |
| `src/Ass5Game.java` | Stays in the default package; becomes the GUI/runner/flow composition root |

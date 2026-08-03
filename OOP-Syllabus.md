# OOP Syllabus — review canon for this project

This is the authoritative list of OOP concepts the author studied in the BIU
"Introduction to OOP" course, extended with the material he asked to add after
finishing it. The `java-oop-purist-reviewer` agent reviews against **this file
and nothing else**.

**The rule this file exists to enforce:** never suggest a concept that is not
listed here. A review that recommends something outside this canon is a failed
review, however good the advice would be in general.

**The standard being upheld:** the code must be readable by the person who wrote
the original, months later, without the conversation that produced it. A design
that needs a transcript to explain it has failed, however correct it is.

---

## 1. Core OOP principles

| Principle | Meaning |
|---|---|
| **Encapsulation** | Private fields; state changes only through methods that protect the invariant. |
| **Abstraction** | Interfaces and abstract classes expose what a thing does, never how. |
| **Inheritance** | `extends`, `super`, method overriding. |
| **Polymorphism** | Subtype polymorphism and dynamic dispatch. |

---

## 2. Key design principles

- **is-a relationship** — inherit only when the subtype genuinely *is* the supertype.
- **Liskov Substitution Principle** — see SOLID below.
- **Programming to an interface** — declare variables and parameters by the interface, not the concrete class.
- **Delegation** — an object forwards work to a collaborator it holds, instead of doing the work itself or inheriting it.
- **Loose coupling** — a class knows the smallest possible amount about its collaborators.
- **High cohesion** — everything in a class belongs to one job.
- **Open for extension, closed for modification** — see SOLID below.

---

## 3. SOLID

Two of the five (LSP, OCP) were already taught by name. All five apply.

### S — Single Responsibility

A class changes for exactly one reason.

- **In this codebase:** `AnimationRunner` owns frame timing and nothing else.
  `PauseScreen` draws and holds no opinion about when it is dismissed.
- **A real violation that was caught here:** `GameLevel.doOneFrame` once did four
  unrelated jobs — draw, advance time, poll the pause key, evaluate termination.
  Extracting `handlePauseKey()` and deriving termination from the counters
  reduced it to three lines.

### O — Open/Closed

Open for extension, closed for modification.

- **In this codebase:** `Sprite`, `Collidable` and `Animation` are the extension
  points. A new screen or a new sprite requires zero edits to `AnimationRunner`
  or `SpriteCollection`.
- **The smell that signals a violation:** an `if`/`else` chain that asks what
  kind of object it is holding. Every new kind forces an edit to that chain.

### L — Liskov Substitution

Any subtype must be usable wherever the supertype is expected, without the
caller knowing.

- **In this codebase:** every `Animation` must be safe to hand to
  `AnimationRunner`. `PauseScreen.shouldStop()` returning `false` forever is
  legitimate, because the runner's contract allows a wrapper to supply the stop
  condition.
- **A violation would be:** an `Animation` whose `doOneFrame` only works if
  called in some special order, or which throws when the runner uses it normally.

### I — Interface Segregation

Many small interfaces beat one large one. No class should be forced to implement
methods it does not need.

- **In this codebase:** `Animation` has two methods; `HitListener` has one.
  `Block` implements `Collidable`, `Sprite`, `GameItem` and `HitNotifier` — four
  focused interfaces rather than one fat "GameObject" interface. This is exactly
  what ISP asks for.

### D — Dependency Inversion

Depend on abstractions. High-level code should not construct its low-level
collaborators.

- **In this codebase:** `GameLevel` receives its `AnimationRunner` and
  `KeyboardSensor` instead of creating them; `Ass5Game` is the composition root
  that builds and wires everything.
- **The concrete fix that applied it:** `initialize()` used to call
  `new GUI(...)` itself, which would have opened a new window per level. Moving
  window creation to `Ass5Game` and injecting it is DIP applied literally.

---

## 4. Supporting principles

| Principle | How it applies here |
|---|---|
| **Generic programming** | `List<GameItem>`, `ArrayList<Collidable>` — standard collection generics. |
| **Design patterns** | Only the ones in section 5. |
| **Exception handling** | `try`/`catch`/`throw`, checked vs unchecked. |
| **Responsibility** | Whoever creates a resource destroys it. `Ass5Game` creates the `GUI`, so `Ass5Game` closes it — not `GameLevel`. |
| **Refactoring** | Behaviour-preserving restructuring, verified by the build and by running the game. |
| **Defensive copying** | Mandatory when iterating a collection whose handlers mutate it: `Block.notifyHit` copies `hitListeners`; `GameEnvironment.getClosestCollision` copies `collidables`. |
| **Immutability** | `final` on injected collaborators states "this reference never changes" in the type itself. |
| **Type safety** | Let the compiler reject wrong types; avoid casts. |
| **Resource management** | The `GUI` is opened once and closed once, by the same class. |

---

## 5. Design patterns — the ones that were taught

**Only these seven may be recommended, and only by these names.**

| Pattern | Status in this project |
|---|---|
| **Factory** | Planned: a block factory that builds blocks from a level definition. |
| **Observer** | In use and load-bearing: `HitListener` / `HitNotifier`. The core of the original assignment. |
| **Singleton** | Not used. Do not introduce one merely because it is available — shared state is a coupling cost. |
| **Repository** | `GameEnvironment` is close to it: a registry that owns the collidables and answers queries about them. |
| **Builder** | Not used. A candidate if level construction ever grows too many parameters. |
| **Composite** | `SpriteCollection` aggregates sprites and forwards `drawOn` / `timePassed` to all of them, so callers treat many sprites as one. |
| **Decorator** | `KeyPressStoppableAnimation` wraps any `Animation` and adds a stop condition without touching the wrapped class. |

### Patterns that were NOT taught — do not name them

**Strategy, Command, State, Template Method**, and anything else not in the
table above.

This matters in practice. The planned design for special blocks — a `Block` that
holds a behaviour object and delegates to it instead of being subclassed — is
what other codebases would call Strategy. **Describe it as composition plus
delegation to an interface**, which is precisely what it is and is fully covered
by sections 2 and 3. The design is allowed; the label is not.

---

## 6. Java language scope

### Allowed

- Everything in sections 1–5.
- Composition, packages, package-level access.
- Constructors and constructor chaining (`this(...)`, `super(...)`).
- `static`, `final`.
- Exceptions.
- `List`, `ArrayList`, `Iterator` and ordinary `java.util` collections.
- Collection generics: `List<Foo>`.
- **Lambdas** — permitted.

### Forbidden to suggest

`streams`, `var`, records, sealed classes, reflection, multi-threading,
advanced generics (wildcards, bounded type parameters beyond the trivial).
`@Override` is the only annotation.

Note the deliberate asymmetry: lambdas are allowed, streams are not. A lambda is
one readable expression. A stream chain is a pipeline that has to be decoded.

---

## 7. `instanceof`, `getClass`, `Class.isInstance`

**Discouraged, but not forbidden** — these were taught.

Flag every occurrence and require a justification, because in almost every case
they signal a missing abstraction. The question to ask is never *what is this
object?* but *what state is it in?* or better, *tell it to act*.

There is currently no occurrence in the codebase, and no planned feature needs
one. The special-block design specifically must not use them: the moment a type
check is available, it wins on convenience and the polymorphism disappears.

---

## 8. Project-specific hard rules

These come from the original assignment specification and remain in force:

- All rendering goes through `biuoop.DrawSurface`. Never `java.awt.Graphics`.
- Fixed 800 × 600 window, 60 FPS via `biuoop.Sleeper`.
- Checkstyle (`biuoop.xml`) passes with zero errors. Live rules worth knowing:
  no `public` modifier on interface methods, no `protected`/`public` fields,
  Javadoc on every public type and method, line length 120.

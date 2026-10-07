# CSE 443 coursework (Java, Gebze Technical University)

Originally written and submitted as coursework in 2021. Revised in 2026: the fixes are listed at the end.

Four assignments, each built around design patterns. Plain Java, no external libraries.
Each assignment has its own `src/`, a UML diagram and the written report.

| Assignment | Part | Pattern(s) | Where |
|---|---|---|---|
| HW01 | PART1 | Strategy (`LinearEquationsSolver`: Gaussian elimination, matrix inversion) | `HW01/src/PART1` |
| HW01 | PART2 | Observer (`Contents` notifies subscribed `User`s) | `HW01/src/PART2` |
| HW01 | PART3 | Decorator (`Suit` + stackable `Accessory` weapons) | `HW01/src/PART3` |
| HW02 | part1 | Singleton | `HW02/src/part1` |
| HW02 | part2 | Iterator (spiral and anti-clockwise traversal) | `HW02/src/part2` |
| HW02 | part3 | State and Observer (traffic light) | `HW02/src/part3` |
| HW02 | part4 | Proxy (dynamic proxy around a database table) | `HW02/src/part4` |
| Midterm | MidTermPack | Abstract Factory (mobile phone factories per region) | `Midterm/src/MidTermPack` |
| Midterm | MidTermPack2 | Adapter (modern payment to turbo payment) | `Midterm/src/MidTermPack2` |
| Midterm | MidTermPack3 | Command with undo | `Midterm/src/MidTermPack3` |
| Midterm | MidTermPack4 | Template Method (Fourier and cosine transform) | `Midterm/src/MidTermPack4` |
| Final | all | MVC, Observer, Mediator, Memento, Strategy, Producer-Consumer, Singleton | `Final/src` |

## Final: concurrent epidemic simulation

People walk on a map, collide, infect each other, queue for a limited number of hospital places
and either get treated or die while waiting. Swing GUI with start, stop, resume, undo and
"add people" controls.

| Pattern | Classes |
|---|---|
| MVC | `ModelInterface`/`ModelClass`, `GUI`, `ControllerInterFace`/`ControllerClass` |
| Observer | `GUIMapObserver` (the GUI) registers with the model and is notified after every step |
| Mediator | `Mediator`/`MediatorClass` detects collisions and applies the infection rules, people never talk to each other directly |
| Memento | `Memento` stores immutable `Person.State` snapshots; stop, resume and undo restore them |
| Strategy | `Waiting` with `CollisionWaiting` and `HospitalWaiting` as interchangeable waiting behaviours |
| Producer-Consumer | `Producer`, `Consumer` and the bounded buffer `HospitalQueue` (one `ReentrantLock`, two `Condition`s, timed wait for patience) |
| Singleton | `HospitalQueue` (one hospital per simulation) |

Concurrency notes: the people list is guarded by one lock in `ModelClass`; the hospital queue owns its
own lock and conditions; collision waiting and hospital waiting run on short-lived threads.

### Build and run

```
cd Final
javac -d out src/*.java
java -cp out Main
```

### Checks

```
cd Final
javac -d out src/*.java test/SmokeTest.java
java -Djava.awt.headless=true -cp out SmokeTest
```

`SmokeTest` covers the snapshot behaviour of `Memento`, the bounded queue (blocking, timeout, hand-over)
and the collision test.

## Other assignments

```
cd HW01        # or HW02, Midterm
javac -d out $(find src -name '*.java')
```

Each `Main` class lives in its own package (`PART1`, `part2`, `MidTermPack3`, ...).

## Changes after the course

The code was submitted in 2021. Later fixes, all in `Final`:

- `Memento` stored references to the live list instead of copies, and removed the oldest backup
  instead of the newest. It now stores immutable snapshots.
- `undo` released its lock twice. Locking now uses `try/finally` everywhere.
- The static lock, queue and hospital size shared by `Producer`, `Consumer` and `ModelClass`
  moved into `HospitalQueue`.
- Collision detection built nested lists for a 5x5 square and only compared edges. It is now an
  overlap test on both axes.
- Compiled classes, jars and duplicated source folders were removed from the repository.

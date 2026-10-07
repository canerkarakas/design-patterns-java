import java.util.Vector;

/**
 * Headless checks for the parts that were wrong or untested before:
 * Memento snapshots, HospitalQueue (bounded buffer + patience) and collision detection.
 * Run: javac -d out src/*.java test/SmokeTest.java && java -cp out SmokeTest
 */
public class SmokeTest {
    private static int failures = 0;

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) failures++;
    }

    public static void main(String[] args) throws Exception {
        mementoKeepsIndependentSnapshots();
        mementoReturnsLatestFirst();
        hospitalQueueBlocksWhenFullAndTimesOut();
        hospitalQueueHandsOverToConsumer();
        collisionNeedsOverlapOnBothAxes();
        if (failures > 0) {
            System.out.println(failures + " check(s) failed");
            System.exit(1);
        }
        System.out.println("all checks passed");
    }

    private static void mementoKeepsIndependentSnapshots() {
        Person p = new Person();
        p.setX_coordinate(10);
        Vector<Person> people = new Vector<>();
        people.add(p);
        Memento memento = new Memento();
        memento.addBackup(people);
        p.setX_coordinate(999);          // change after the backup
        p.restore(memento.getLastState().get(0));
        check("Memento: restore brings back the saved position", p.getX_coordinate() == 10);
    }

    private static void mementoReturnsLatestFirst() {
        Person p = new Person();
        Vector<Person> people = new Vector<>();
        people.add(p);
        Memento memento = new Memento();
        p.setX_coordinate(1);
        memento.addBackup(people);
        p.setX_coordinate(2);
        memento.addBackup(people);
        p.restore(memento.getLastState().get(0));
        boolean latest = p.getX_coordinate() == 2;
        p.restore(memento.getLastState().get(0));
        check("Memento: backups come back newest first", latest && p.getX_coordinate() == 1 && memento.isEmpty());
    }

    private static void hospitalQueueBlocksWhenFullAndTimesOut() throws Exception {
        HospitalQueue queue = HospitalQueue.getInstance();
        queue.setCapacity(1);
        Person first = new Person();
        Person second = new Person();
        second.setWaitingHospitalTime(150); // 150 ms patience
        boolean firstIn = queue.admit(first);
        long start = System.nanoTime();
        boolean secondIn = queue.admit(second);
        long millis = (System.nanoTime() - start) / 1_000_000;
        check("HospitalQueue: first person is admitted", firstIn);
        check("HospitalQueue: second person gives up after its patience (~150 ms)", !secondIn && millis >= 100 && millis < 2000);
        queue.next(); // drain
    }

    private static void hospitalQueueHandsOverToConsumer() throws Exception {
        HospitalQueue queue = HospitalQueue.getInstance();
        queue.setCapacity(5);
        Person person = new Person();
        final Person[] got = new Person[1];
        Thread consumer = new Thread(() -> {
            try {
                got[0] = queue.next();
            } catch (InterruptedException ignored) { }
        });
        consumer.start();
        Thread.sleep(100);               // consumer is blocked on an empty queue
        queue.admit(person);
        consumer.join(2000);
        check("HospitalQueue: blocked consumer receives the admitted person", got[0] == person);
    }

    private static void collisionNeedsOverlapOnBothAxes() {
        Mediator mediator = new MediatorClass();
        Person a = new Person();
        Person b = new Person();
        a.setMediator(mediator);
        b.setMediator(mediator);
        a.setX_coordinate(100); a.setY_coordinate(100);
        b.setX_coordinate(103); b.setY_coordinate(102);   // overlapping squares
        mediator.walk(a);
        check("Mediator: overlapping people collide (both become invisible while waiting)", a.getInvisible() && b.getInvisible());

        Person c = new Person();
        Person d = new Person();
        Mediator other = new MediatorClass();
        c.setMediator(other);
        d.setMediator(other);
        c.setX_coordinate(100); c.setY_coordinate(100);
        d.setX_coordinate(103); d.setY_coordinate(300);   // same x band, far apart in y
        other.walk(c);
        check("Mediator: people far apart in y do not collide", !c.getInvisible() && !d.getInvisible());
    }
}

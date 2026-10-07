import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bounded buffer between sick people (producers) and hospital treatment (consumers).
 * Lock and both conditions live here, so Producer and Consumer share no static fields.
 * There is one hospital per simulation, hence the singleton accessor.
 */
public final class HospitalQueue {
    private static final HospitalQueue INSTANCE = new HospitalQueue();

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private final LinkedList<Person> waiting = new LinkedList<>();
    private volatile int capacity;

    private HospitalQueue() { }

    public static HospitalQueue getInstance() {
        return INSTANCE;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Producer side: waits for a free place for at most the person's remaining patience.
     * @return true if the person got a place, false if the patience ran out (the person dies)
     */
    public boolean admit(Person person) throws InterruptedException {
        lock.lock();
        try {
            while (waiting.size() >= capacity) {
                if (!notFull.await(person.getWaitingHospitalTime(), TimeUnit.MILLISECONDS)) {
                    return false;
                }
            }
            waiting.add(person);
            notEmpty.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** Consumer side: blocks until somebody is waiting, then takes the first person. */
    public Person next() throws InterruptedException {
        lock.lock();
        try {
            while (waiting.isEmpty()) {
                notEmpty.await();
            }
            Person person = waiting.removeFirst();
            notFull.signal();
            return person;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return waiting.size();
        } finally {
            lock.unlock();
        }
    }
}

/** Puts one sick person into the hospital queue; the person dies if no place frees up in time. */
public class Producer implements Runnable {
    private final Person person;

    public Producer(Person person) {
        this.person = person;
    }

    @Override
    public void run() {
        try {
            if (!HospitalQueue.getInstance().admit(person)) {
                person.setDie(true);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

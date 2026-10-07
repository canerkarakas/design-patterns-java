/** Waiting strategy for an infected person: queue for the hospital (producer) and get treated (consumer). */
public class HospitalWaiting implements Waiting {
    private static final int PATIENCE_STEP_MILLIS = 25000;

    private final Person person;

    public HospitalWaiting(Person person) {
        this.person = person;
    }

    @Override
    public void waiting(int waitingTime) {
        new Thread(this::producerConsumer).start();
    }

    private void producerConsumer() {
        person.setQueueAtHospital(true);
        if (person.getDie())
            return;
        person.setInvisible(false);
        try {
            Thread.sleep(PATIENCE_STEP_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        person.setWaitingHospitalTime(person.getWaitingHospitalTime() - 25);
        if (person.getWaitingHospitalTime() <= 0) {
            person.setDie(true);
        } else {
            new Thread(new Producer(person)).start();
            new Thread(new Consumer()).start();
        }
    }
}

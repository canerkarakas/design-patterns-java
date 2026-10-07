import java.util.Random;

/** Treats the next person in the hospital queue, then sends the person back to the map healthy. */
public class Consumer implements Runnable {
    private static final int TREATMENT_MILLIS = 10000;

    @Override
    public void run() {
        try {
            Person person = HospitalQueue.getInstance().next();
            person.goHospital();
            Thread.sleep(TREATMENT_MILLIS);
            Random random = new Random();
            person.setX_coordinate(random.nextInt(1000));
            person.setY_coordinate(random.nextInt(600));
            person.setDirection(random.nextInt(4));
            person.setSituation(0);
            person.setQueueAtHospital(false);
            person.setInvisible(false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

public class ModelClass implements ModelInterface{
    private Vector<GUIMapObserver> guiMapObservers = new Vector<>();
    private Vector<Person> people = new Vector<>();
    private boolean start = false;
    private boolean stop = false;
    private boolean undo = false;
    private Mediator mediator = new MediatorClass();
    private ReentrantLock mutexPeople = new ReentrantLock();



    public boolean getStart(){ return start; }
    public boolean getStop(){ return stop; }

    @Override
    public ReentrantLock getMutexPeople() {
        return mutexPeople;
    }

    @Override
    public void initialize(Memento memento) {
        if (people.size() > 0){
            if (!start) {
                System.out.println("start " + people.size());
                start = true;
                mutexPeople.lock();
                Random random = new Random();
                people.get(random.nextInt(people.size())).setSituation(1);
                memento.addBackup(this.people);
                HospitalQueue.getInstance().setCapacity(people.size() / 100);
                mutexPeople.unlock();
                notifyGuiMapObservers();
                Worker worker = new Worker(this, 1000); //for timer
            }
            else System.out.println("Again start. Dont do that");
        }
        else System.out.println("please enter people number" +
                " and bulk them or click start after increase button");
    }

    @Override
    public void resume(Memento memento) {
        if (stop){
            System.out.println("resume");
            restoreLastBackup(memento);
            stop = false;
            undo = false;
        }
        else{
            if (start){
                System.out.println("hala resume");
            }
            else System.out.println("please start action dont click resume");
        }
    }

    @Override
    public void stop(Memento memento) {
        if (!stop){
            if (start){
                mutexPeople.lock();
                memento.addBackup(people);
                mutexPeople.unlock();
                System.out.println("stop");
                stop = true;
            }
            else
                System.out.println("please start action dont click stop");
        }
        else System.out.println("hala stop");

    }

    @Override
    public void undo(Memento memento) {
        if (!undo){
            if (start){
                restoreLastBackup(memento);
                notifyGuiMapObservers();
            }
            else System.out.println("please resume action dont click undo");
        }
        else System.out.println("resume bas");
    }

    @Override
    public void addBulk(int NOP, Memento memento) {
        mutexPeople.lock();
        for (int i = 0; i<NOP; i++) {
            Person p = new Person();
            p.setMediator(mediator);
            people.add(p);
        }
        HospitalQueue.getInstance().setCapacity(people.size() / 100);
        memento.addBackup(this.people);
        mutexPeople.unlock();
        if (start)
            notifyGuiMapObservers();
        System.out.println(people.size());
    }

    @Override
    public void registerObserver(GUIMapObserver o) {
        guiMapObservers.add(o);
    }

    @Override
    public void removeObserver(GUIMapObserver o) {
        guiMapObservers.removeElement(o);
    }


    @Override
    public void increaseNOP(Memento memento) {
        Person p = new Person();
        mutexPeople.lock();
        p.setMediator(mediator);
        people.add(p);
        memento.addBackup(this.people);
        mutexPeople.unlock();
        if (start)
            notifyGuiMapObservers();
        System.out.println(people.size());
    }

    @Override
    public Vector<Person> getPeople() {
        return people;
    }


    /** Puts the people back into the state of the most recent backup (if there is one). */
    private void restoreLastBackup(Memento memento) {
        mutexPeople.lock();
        try {
            if (memento.isEmpty())
                return;
            List<Person.State> states = memento.getLastState();
            for (int i = 0; i < Math.min(states.size(), people.size()); i++)
                people.get(i).restore(states.get(i));
        } finally {
            mutexPeople.unlock();
        }
    }

    public void notifyGuiMapObservers(){
        for (GUIMapObserver guiMapObserver : guiMapObservers)
            guiMapObserver.updateMap();
    }
}

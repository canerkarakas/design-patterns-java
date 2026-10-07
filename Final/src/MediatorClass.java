import java.util.concurrent.CopyOnWriteArrayList;

public class MediatorClass implements Mediator{
    private static final int SIZE = 5; // side of a person's square on the map
    private final CopyOnWriteArrayList<Person> people = new CopyOnWriteArrayList<>();

    @Override
    public void walk(Person person) {
        for (Person p: people) {
            if (p != person && !p.getInvisible()) {
                if (isCollision(p, person)){
                    int maxC = Math.max(p.getC(), person.getC());
                    setInvisiblePeople(p, person);
                    if (p.getSituation()==1){
                        if (person.getSituation()!=1){
                            setInfectPeople(p, person);
                            person.setP(collisionInfectProb(person, p, maxC));
                            if (!p.isQueueAtHospital())
                                p.setWaiting(p.getWaitingHospital());
                            else
                                p.setWaiting(p.getWaitingCollision());
                            person.setWaiting(person.getWaitingHospital());
                        }
                        else{
                            p.setWaiting(p.getWaitingCollision());
                            person.setWaiting(person.getWaitingCollision());
                        }
                    }
                    else{
                        if(person.getSituation()==1){
                            setInfectPeople(p, person);
                            p.setP(collisionInfectProb(person, p, maxC));
                            p.setWaiting(p.getWaitingHospital());
                            if (!person.isQueueAtHospital())
                                person.setWaiting(person.getWaitingHospital());
                            else
                                person.setWaiting(person.getWaitingCollision());
                        }
                        else{
                            p.setWaiting(p.getWaitingCollision());
                            person.setWaiting(person.getWaitingCollision());
                        }
                    }
                    p.waiting(maxC);
                    person.waiting(maxC);
                    break;
                }
            }
        }
    }

    @Override
    public void addPerson(Person person){
        people.add(person);
    }

    private double collisionInfectProb(Person person, Person p, int maxC){
        int minD = Math.min(p.getD(), person.getD());
        return Math.min(Person.R * (1 + (((double) maxC) * 0.1))
                * p.getM() * person.getM() * (1 - (((double) minD) * 0.1)), 1);
    }

    /** Two 5x5 squares collide when they overlap on both axes. */
    private boolean isCollision(Person person, Person otherPerson){
        return Math.abs(person.getX_coordinate() - otherPerson.getX_coordinate()) < SIZE
                && Math.abs(person.getY_coordinate() - otherPerson.getY_coordinate()) < SIZE;
    }

    private void setInvisiblePeople(Person person, Person person2){
        person.setInvisible(true);
        person2.setInvisible(true);
    }

    private void setInfectPeople(Person person, Person person2){
        person.setSituation(1);
        person2.setSituation(1);
    }

}

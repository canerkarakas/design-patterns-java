import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Caretaker of the simulation snapshots. Each backup is a list of immutable
 * Person.State copies, so later changes to the people never alter an old backup.
 */
public class Memento {
    private final LinkedList<List<Person.State>> backups = new LinkedList<>();

    public void addBackup(List<Person> people) {
        List<Person.State> snapshot = new ArrayList<>(people.size());
        for (Person person : people) {
            snapshot.add(person.save());
        }
        backups.addLast(snapshot);
    }

    public boolean isEmpty() {
        return backups.isEmpty();
    }

    /** Removes and returns the most recent backup. */
    public List<Person.State> getLastState() {
        return backups.removeLast();
    }
}

package ch04.people;

import java.util.List;

public interface PeopleRepository {
    List<String> getPeople();

    List<String> getPeople(int page);
}

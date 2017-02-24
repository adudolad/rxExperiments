package ch06;

import java.util.List;

public interface DbRepository {
    void storeOne(String record);

    void storeAll(List<String> records);
}

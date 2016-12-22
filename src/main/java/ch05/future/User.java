package ch05.future;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.RandomUtils;

import ch04.utils.FutureUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class User {
    private long id;
    private String name;

    public User findById(final long id) {
        int timeout = RandomUtils.nextInt(1000, 3000);
        log.info("Waiting for a User [{}]", timeout);
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            log.error("Error while creating an user", e);
            return null;
        }

        return User.builder().id(id).build();
    }

    public CompletableFuture<User> findByIdAsync(final long id) {
        return CompletableFuture.supplyAsync(() -> findById(id));
    }

    public Observable<User> rxFindById(final long id) {
        return FutureUtil.observe(findByIdAsync(id));
    }

    public Observable<User> rxFindById2(final long id) {
        return Observable.defer(() -> Observable.fromCallable(() -> findById(id)));
    }
}

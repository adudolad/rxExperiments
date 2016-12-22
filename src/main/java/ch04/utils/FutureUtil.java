package ch04.utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import rx.Observable;

public final class FutureUtil {
    private FutureUtil() { }

    public static <T> Observable<T> observe(final CompletableFuture<T> future) {
        return Observable.create(subscriber -> {
                future.whenComplete((value, exception) -> {
                    if (exception != null) {
                        subscriber.onError(exception);
                    } else {
                        subscriber.onNext(value);
                        subscriber.onCompleted();
                    }
                });
            });
    }

    // When an observable emits exactly one event, like an method invocation or request/response pattern
    // NOTE: There is a side effect here: it subscribes to Observable, thus forcing evaluation amd computation of COLD
    // Observables
    // Moreover, each invocation of this transformation will subscribe AGAIN !
    public static <T> CompletableFuture<T> toFuture(final Observable<T> observable) {
        final CompletableFuture<T> promise = new CompletableFuture<>();
        observable.single().subscribe(promise::complete, promise::completeExceptionally);

        return promise;
    }

    public static <T> CompletableFuture<List<T>> toFutureList(final Observable<T> observable) {
        return toFuture(observable.toList());
    }

}

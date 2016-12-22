package ch05.single;

import java.io.IOException;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import lombok.extern.slf4j.Slf4j;

import rx.Single;
import rx.SingleSubscriber;

import rx.schedulers.Schedulers;

@Slf4j
public class SingleTest {

    private AsyncHttpClient asyncHttpClient;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();

        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        asyncHttpClient = new AsyncHttpClient();
    }

    @Test
    public void testSimpleSingle() {
        Single<String> single = Single.just("Hello world");
        single.subscribe(x -> log.info("Result is [{}]", x));

        Single<Instant> error = Single.error(new RuntimeException("Oopsss"));

        error.observeOn(Schedulers.io()).subscribe(x -> log.info("Result is [{}]", x), er -> log.error("ERROR", er));
    }

    @Test
    public void testAsyncHttpClient() {
        Single<Addresses> httpExample = fetch(
                "http://restsn02.zalando:39100/api/address-suggestions?zip=1077JH&country_code=NL").flatMap(
                this::body2);
        final Addresses b = httpExample.toBlocking().value();
        log.info("Result is [{}]", b);
    }

    private Single<String> body(final Response response) {
        return Single.create(subscriber -> {
                try {
                    subscriber.onSuccess(response.getResponseBody());
                } catch (IOException e) {
                    subscriber.onError(e);
                }

                ;
            });
    }

    private Single<Addresses> body2(final Response response) {
        return Single.fromCallable(response::getResponseBody).<Addresses>map(resp -> {
                try {
                    return mapper.readValue(resp, Addresses.class);
                } catch (IOException e) {
                    log.error("ERROR", e);
                    return new Addresses();
                }
            });
    }

    private Single<Response> fetch(final String address) {
        return Single.create(singleSubscriber -> {
                asyncHttpClient.prepareGet(address).execute(handler(singleSubscriber));
            });
    }

    private AsyncCompletionHandler handler(final SingleSubscriber<? super Response> subscriber) {
        return new AsyncCompletionHandler() {
            @Override
            public Object onCompleted(final Response response) throws Exception {
                subscriber.onSuccess(response);
                return response;
            }

            public void onThrowable(final Throwable t) {
                subscriber.onError(t);
            }
        };
    }
}

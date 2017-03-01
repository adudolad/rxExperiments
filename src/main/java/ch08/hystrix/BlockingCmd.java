package ch08.hystrix;

import java.io.IOException;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BlockingCmd extends HystrixCommand<String> {
    private long delay;

    public BlockingCmd() {
        super(HystrixCommandGroupKey.Factory.asKey("TestGroup"));
        this.delay = 0L;

    }

    public BlockingCmd(final long delay) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("TestGroup")).andCommandPropertiesDefaults(
                HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(1000)));
        this.delay = delay;
    }

    @Override
    protected String run() throws IOException, InterruptedException {
        log.info("Sleeping ...");
        Thread.sleep(delay);
        log.info("Starting command ...");

// final URL url = new URL("http://example.com");
// try(InputStream inputStream = url.openStream()) {
// final String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
// log.info("Cmd result is [{}]", result);
// return result;
// }
        final String result = "GOT COMMAND RESULT !!!";
        log.info("Command returns [{}]", result);
        return result;
    }

    @Override
    protected String getFallback() {
        return "RESULT STATIC FALLBACK !!!";
    }

}

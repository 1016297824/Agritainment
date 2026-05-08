package com.agritainment.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MdcTaskDecorator - 异步线程MDC传播")
class MdcTaskDecoratorTest {

    private MdcTaskDecorator decorator;

    @BeforeEach
    void setUp() {
        decorator = new MdcTaskDecorator();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Nested
    @DisplayName("decorate")
    class Decorate {

        @Test
        @DisplayName("MDC上下文传播到新线程")
        void propagatesMdcToNewThread() throws Exception {
            MDC.put("requestId", "test-request-123");
            MDC.put("userId", "42");

            AtomicReference<String> requestIdInThread = new AtomicReference<>();
            AtomicReference<String> userIdInThread = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            Runnable original = () -> {
                requestIdInThread.set(MDC.get("requestId"));
                userIdInThread.set(MDC.get("userId"));
                latch.countDown();
            };

            Runnable decorated = decorator.decorate(original);
            Thread thread = new Thread(decorated);
            thread.start();
            latch.await();
            thread.join();

            assertThat(requestIdInThread.get()).isEqualTo("test-request-123");
            assertThat(userIdInThread.get()).isEqualTo("42");
        }

        @Test
        @DisplayName("新线程执行完毕后清除MDC")
        void clearsMdcAfterExecution() throws Exception {
            MDC.put("requestId", "test-123");

            CountDownLatch latch = new CountDownLatch(1);
            Runnable original = latch::countDown;
            Runnable decorated = decorator.decorate(original);

            Thread thread = new Thread(decorated);
            thread.start();
            latch.await();
            thread.join();

            CountDownLatch checkLatch = new CountDownLatch(1);
            AtomicReference<String> mdcAfter = new AtomicReference<>();
            Thread checkThread = new Thread(() -> {
                mdcAfter.set(MDC.get("requestId"));
                checkLatch.countDown();
            });
            checkThread.start();
            checkLatch.await();
            checkThread.join();

            assertThat(mdcAfter.get()).isNull();
        }

        @Test
        @DisplayName("父线程MDC为空时不报错")
        void noMdcContext_noError() throws Exception {
            AtomicReference<String> requestIdInThread = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            Runnable original = () -> {
                requestIdInThread.set(MDC.get("requestId"));
                latch.countDown();
            };

            Runnable decorated = decorator.decorate(original);
            Thread thread = new Thread(decorated);
            thread.start();
            latch.await();
            thread.join();

            assertThat(requestIdInThread.get()).isNull();
        }
    }
}

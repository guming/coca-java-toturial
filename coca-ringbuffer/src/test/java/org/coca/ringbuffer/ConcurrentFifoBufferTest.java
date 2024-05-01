package org.coca.ringbuffer;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrentFifoBufferTest {
    @Test
    public void testCircularFifoBuffer() {
        FifoBuffer<Exception> exceptionBuffer = new ConcurrentFifoBuffer<>(4);

        assertThat(exceptionBuffer.size()).isZero();
        assertThat(exceptionBuffer.isEmpty()).isTrue();
        assertThat(exceptionBuffer.isFull()).isFalse();
        exceptionBuffer.add(new IllegalArgumentException("bla bla"));
        assertThat(exceptionBuffer.size()).isEqualTo(1);
        exceptionBuffer.add(new IOException("bla bla"));
        assertThat(exceptionBuffer.size()).isEqualTo(2);
        exceptionBuffer.add(new IllegalStateException("bla bla"));
        assertThat(exceptionBuffer.size()).isEqualTo(3);
        assertThat(exceptionBuffer.isFull()).isFalse();
        assertThat(exceptionBuffer.isEmpty()).isFalse();
        exceptionBuffer.add(new UnknownHostException("bla bla"));
        assertThat(exceptionBuffer.size()).isEqualTo(4);
        assertThat(exceptionBuffer.isFull()).isTrue();

        List<Exception> bufferedExceptions = exceptionBuffer.toList();

        assertThat(bufferedExceptions).hasSize(4);
        assertThat(bufferedExceptions.get(0)).isInstanceOf(IllegalArgumentException.class);
        assertThat(bufferedExceptions.get(1)).isInstanceOf(IOException.class);
        assertThat(bufferedExceptions.get(2)).isInstanceOf(IllegalStateException.class);
        assertThat(bufferedExceptions.get(3)).isInstanceOf(UnknownHostException.class);

        assertThat(exceptionBuffer.toStream()).hasSize(4);
        assertThat(exceptionBuffer.toStream()).hasOnlyElementsOfTypes(IllegalArgumentException.class,
                IOException.class, IllegalStateException.class, UnknownHostException.class);

        // The size must still be 4, because the FifoBuffer capacity is 4
        exceptionBuffer.add(new IOException("bla bla"));
        assertThat(exceptionBuffer.size()).isEqualTo(4);

        exceptionBuffer.add(new IOException("bla bla"));
        assertThat(exceptionBuffer.size()).isEqualTo(4);

        exceptionBuffer.add(new IOException("bla bla"));
        assertThat(exceptionBuffer.size()).isEqualTo(4);

        assertThat(exceptionBuffer.take().get()).isInstanceOf(UnknownHostException.class);
        assertThat(exceptionBuffer.take().get()).isInstanceOf(IOException.class);
        assertThat(exceptionBuffer.take().get()).isInstanceOf(IOException.class);
        assertThat(exceptionBuffer.take().get()).isInstanceOf(IOException.class);
        assertThat(exceptionBuffer.take()).isEmpty();
    }
}
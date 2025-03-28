package pl.stalostech.drongarazowy.uklad.czytnik;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.stalostech.drongarazowy.uklad.model.MPU6050Record;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Consumer;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MPU6050RecordReaderTest {

    private MPU6050RecordReader reader;

    @Mock
    private Consumer<MPU6050Record> listenerMock;

    @Captor
    private ArgumentCaptor<MPU6050Record> recordCaptor;

    @BeforeEach
    void setup() {
        reader = spy(new MPU6050RecordReader());
        doNothing().when(reader).connect(115200);
    }

    @Test
    void testListen_ValidData() {
        String data = "100,200,300,400,500,600\n";
        InputStream inputStream = new ByteArrayInputStream(data.getBytes());
        doReturn(inputStream).when(reader).getSerialPortInputStream();

        Thread thread = reader.listen(listenerMock);
        thread.start();

        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            fail("Wątek został przerwany.");
        }

        verify(listenerMock, times(1)).accept(recordCaptor.capture());
        MPU6050Record record = recordCaptor.getValue();

        assertEquals(100, record.ax());
        assertEquals(200, record.ay());
        assertEquals(300, record.az());
        assertEquals(400, record.gx());
        assertEquals(500, record.gy());
        assertEquals(600, record.gz());
    }

    @Test
    void testListen_InvalidDataFormat() {
        String data = "100,abc,300,400,500,600\n";
        InputStream inputStream = new ByteArrayInputStream(data.getBytes());

        doReturn(inputStream).when(reader).getSerialPortInputStream();

        Thread thread = reader.listen(listenerMock);
        thread.start();

        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            fail("Wątek został przerwany.");
        }

        verify(listenerMock, never()).accept(any());
    }

    @Test
    void testListen_InvalidNumberOfValues() {
        String data = "100,200,300\n";
        InputStream inputStream = new ByteArrayInputStream(data.getBytes());

        doReturn(inputStream).when(reader).getSerialPortInputStream();

        Thread thread = reader.listen(listenerMock);
        thread.start();

        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            fail("Wątek został przerwany.");
        }

        verify(listenerMock, never()).accept(any());
    }

    @Test
    void testListen_EndOfStream() {
        String data = "";
        InputStream inputStream = new ByteArrayInputStream(data.getBytes());

        doReturn(inputStream).when(reader).getSerialPortInputStream();

        Thread thread = reader.listen(listenerMock);
        thread.start();

        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            fail("Wątek został przerwany.");
        }

        verify(listenerMock, never()).accept(any());
    }

}

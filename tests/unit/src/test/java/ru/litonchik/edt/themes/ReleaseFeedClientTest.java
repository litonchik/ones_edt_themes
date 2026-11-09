package ru.litonchik.edt.themes;

import static org.junit.Assert.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.junit.*;
import ru.litonchik.edt.themes.model.*;

public class ReleaseFeedClientTest {
    private ServerSocket server;private ExecutorService threads;
    private final AtomicInteger status=new AtomicInteger(200),delay=new AtomicInteger();private final AtomicReference<byte[]> body=new AtomicReference<>(ReleaseInfoTest.feed("0.1.31").getBytes(StandardCharsets.UTF_8));
    @Before public void setup() throws Exception {
        server=new ServerSocket(0,50,InetAddress.getByName("127.0.0.1"));threads=Executors.newCachedThreadPool(r -> {var t=new Thread(r);t.setDaemon(true);return t;});
        threads.execute(() -> {while(!server.isClosed())try{var socket=server.accept();threads.execute(() -> {try(socket){socket.setSoTimeout(2000);var reader=new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.US_ASCII));for(String line;(line=reader.readLine())!=null && !line.isEmpty();){}if(delay.get()>0)Thread.sleep(delay.get());var bytes=body.get();var output=socket.getOutputStream();output.write(("HTTP/1.1 "+status.get()+" Result\r\nLocation: https://example.com/\r\nConnection: close\r\nContent-Length: "+bytes.length+"\r\n\r\n").getBytes(StandardCharsets.US_ASCII));output.write(bytes);output.flush();}catch(Exception ignored){}});}catch(IOException ignored){break;}});
    }
    @After public void cleanup() throws Exception {if(server!=null)server.close();if(threads!=null)threads.shutdownNow();}
    private ReleaseFeedClient client(int timeout){return new ReleaseFeedClient(URI.create("http://127.0.0.1:"+server.getLocalPort()+"/feed"),uri -> (HttpURLConnection)uri.toURL().openConnection(Proxy.NO_PROXY),timeout);}
    @Test public void readsRealHttpResponse() throws Exception {assertEquals("0.1.31",client(1000).read(() -> false).version().toString());}
    @Test public void refusesRedirectsAndHttpErrors(){for(int value:new int[]{301,302,403,404,429,500}){status.set(value);assertThrows(IOException.class,() -> client(1000).read(() -> false));}}
    @Test public void capsResponseSize(){body.set(new byte[8193]);assertThrows(IOException.class,() -> client(1000).read(() -> false));}
    @Test public void malformedJsonIsNotAnUpdate(){body.set("not JSON".getBytes(StandardCharsets.UTF_8));assertThrows(IOException.class,() -> client(1000).read(() -> false));}
    @Test public void rejectsInvalidUtf8(){body.set(new byte[]{(byte)0xc3,0x28});assertThrows(IOException.class,() -> client(1000).read(() -> false));}
    @Test public void boundsNetworkWait(){delay.set(1500);long started=System.nanoTime();assertThrows(SocketTimeoutException.class,() -> client(100).read(() -> false));assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-started)<1200);}
    @Test public void cancelledBeforeRequestDoesNoIo(){var calls=new AtomicInteger();var client=new ReleaseFeedClient(URI.create("https://example.com/"),uri -> {calls.incrementAndGet();throw new IOException();},1000);assertThrows(InterruptedIOException.class,() -> client.read(() -> true));assertEquals(0,calls.get());}
    @Test public void cancellationAfterConnectStopsBodyRead(){var count=new AtomicInteger();assertThrows(InterruptedIOException.class,() -> client(1000).read(() -> count.incrementAndGet()>1));}
    @Test public void unsafeEndpointsAreRejected(){for(String value:new String[]{"http://example.com/","https://user:secret@example.com/","file:///x","https://example.com/#fragment"})assertThrows(IllegalArgumentException.class,() -> new ReleaseFeedClient(URI.create(value),uri -> null,100));}
}

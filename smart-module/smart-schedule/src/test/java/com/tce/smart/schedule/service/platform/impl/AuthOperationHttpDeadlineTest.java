package com.tce.smart.schedule.service.platform.impl;

import com.tce.smart.schedule.config.AuthOperationDeadlineClient;
import feign.*;
import org.junit.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** 仅回环地址的真实Apache HTTP栈：持续分块不能续命，取消必须释放实际池槽。 */
public class AuthOperationHttpDeadlineTest {
    @Test public void dripBodyCannotExtendDeadlineAndLeaseIsActuallyReleased() throws Exception {boundedResponse(true);}
    @Test public void hangingHeadersAreAbortedAndNotRetried() throws Exception {boundedResponse(false);}
    private void boundedResponse(boolean drip) throws Exception {
        try(Loopback server=new Loopback(drip);AuthOperationDeadlineClient client=new AuthOperationDeadlineClient(1,80,150,1000,250,uri->uri)) {
            ExecutorService worker=Executors.newSingleThreadExecutor();
            try {
                Future<Response> response=worker.submit(()->client.execute(request(server.port()),new Request.Options(2000,5000)));
                Assert.assertTrue(server.entered.await(1,TimeUnit.SECONDS));expectFailure(response,900);
                Assert.assertEquals("总截止时间后真实连接池租约必须释放",0,client.leasedConnections());
                Assert.assertEquals("结果未知不得底层重试",1,server.accepted.get());
            } finally {client.close();worker.shutdownNow();Assert.assertTrue(worker.awaitTermination(2,TimeUnit.SECONDS));}
        }
    }
    @Test public void exhaustedPoolHasAcquireDeadlineAndCancelledRequestFreesIt() throws Exception {
        try(Loopback server=new Loopback(false);AuthOperationDeadlineClient client=new AuthOperationDeadlineClient(1,80,150,1000,400,uri->uri)) {
            ExecutorService workers=Executors.newFixedThreadPool(2);
            try {
                Future<Response> first=workers.submit(()->client.execute(request(server.port()),new Request.Options(2000,5000)));Assert.assertTrue(server.entered.await(1,TimeUnit.SECONDS));
                Future<Response> second=workers.submit(()->client.execute(request(server.port()),new Request.Options(2000,5000)));
                expectFailure(second,250);Assert.assertFalse("池获取超时不能靠首请求结束后才返回",first.isDone());
                expectFailure(first,900);Assert.assertEquals(0,client.leasedConnections());Assert.assertEquals(1,server.accepted.get());
                server.healthy=true;
                try(Response ok=client.execute(request(server.port()),new Request.Options(2000,5000))) {Assert.assertEquals(200,ok.status());}
                Assert.assertEquals(0,client.leasedConnections());Assert.assertEquals(2,server.accepted.get());
            } finally {client.close();workers.shutdownNow();Assert.assertTrue(workers.awaitTermination(2,TimeUnit.SECONDS));}
        }
    }
    private static void expectFailure(Future<?> request,long millis) throws Exception {
        try {request.get(millis,TimeUnit.MILLISECONDS);Assert.fail("超时请求应失败");}
        catch(ExecutionException expected) {Assert.assertTrue(expected.getCause() instanceof IOException);}
        catch(TimeoutException late) {Assert.fail("实际HTTP超过总截止时间后仍在运行");}
    }
    private static Request request(int port) {return Request.create(Request.HttpMethod.GET,"http://127.0.0.1:"+port+"/controlled",Collections.emptyMap(),(byte[])null,StandardCharsets.UTF_8);}
    private static final class Loopback implements AutoCloseable {
        final ServerSocket listener;final ExecutorService workers=Executors.newFixedThreadPool(3);final Set<Socket> sockets=ConcurrentHashMap.newKeySet();
        final CountDownLatch entered=new CountDownLatch(1),stop=new CountDownLatch(1);final AtomicInteger accepted=new AtomicInteger();final boolean drip;volatile boolean healthy;
        Loopback(boolean drip) throws IOException {
            this.drip=drip;listener=new ServerSocket(0,8,InetAddress.getByName("127.0.0.1"));
            workers.submit(()->{while(!listener.isClosed())try {Socket socket=listener.accept();sockets.add(socket);accepted.incrementAndGet();workers.submit(()->serve(socket));}catch(IOException closed){break;}});
        }
        int port(){return listener.getLocalPort();}
        void serve(Socket socket) {
            try(Socket owned=socket) {
                BufferedReader input=new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.US_ASCII));String line;while((line=input.readLine())!=null&&!line.isEmpty()) { }
                OutputStream output=socket.getOutputStream();entered.countDown();
                if(healthy){output.write("HTTP/1.1 200 OK\r\nContent-Length: 2\r\nConnection: close\r\n\r\nOK".getBytes(StandardCharsets.US_ASCII));output.flush();return;}
                if(drip){output.write("HTTP/1.1 200 OK\r\nContent-Length: 999\r\n\r\n".getBytes(StandardCharsets.US_ASCII));while(!stop.await(40,TimeUnit.MILLISECONDS)){output.write('x');output.flush();}}
                else stop.await(5,TimeUnit.SECONDS);
            }catch(Exception ignored) { }finally{sockets.remove(socket);}
        }
        @Override public void close() throws Exception {stop.countDown();listener.close();for(Socket socket:sockets)socket.close();workers.shutdownNow();Assert.assertTrue(workers.awaitTermination(2,TimeUnit.SECONDS));}
    }
}

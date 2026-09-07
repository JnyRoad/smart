package com.tce.smart.schedule.config;

import feign.*;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import java.io.*;
import java.net.URI;
import java.net.SocketTimeoutException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.*;
import java.util.function.Function;

/** 权限专用独立连接池；响应在返回Feign之前完整读取，避免解码阶段越过请求边界。 */
public final class AuthOperationDeadlineClient implements Client,Closeable {
    private final PoolingHttpClientConnectionManager pool=new PoolingHttpClientConnectionManager();
    private final CloseableHttpClient http;
    private final Function<URI,URI> resolver;
    private final int deadlineMillis;
    private final Set<HttpUriRequest> active=ConcurrentHashMap.newKeySet();
    private final ScheduledThreadPoolExecutor timer=new ScheduledThreadPoolExecutor(1,r->{Thread t=new Thread(r,"auth-http-deadline");t.setDaemon(true);return t;});
    public AuthOperationDeadlineClient(int connections,int acquireMillis,int connectMillis,int readMillis,int deadlineMillis,Function<URI,URI> resolver) {
        if(connections<1 || acquireMillis<1 || connectMillis<1 || readMillis<1 || deadlineMillis<1)throw new IllegalArgumentException("HTTP额度必须为正数");
        this.resolver=resolver;this.deadlineMillis=deadlineMillis;timer.setRemoveOnCancelPolicy(true);
        pool.setMaxTotal(connections);pool.setDefaultMaxPerRoute(connections);
        RequestConfig config=RequestConfig.custom().setConnectionRequestTimeout(acquireMillis).setConnectTimeout(connectMillis).setSocketTimeout(readMillis).build();
        http=HttpClients.custom().setConnectionManager(pool).setDefaultRequestConfig(config).disableAutomaticRetries().disableRedirectHandling().build();
    }
    @Override public Response execute(Request request,Request.Options ignored) throws IOException {
        RequestBuilder builder=RequestBuilder.create(request.httpMethod().name()).setUri(resolver.apply(URI.create(request.url())));
        for(Map.Entry<String,Collection<String>> entry:request.headers().entrySet())if(!"Content-Length".equalsIgnoreCase(entry.getKey()))for(String value:entry.getValue())builder.addHeader(entry.getKey(),value);
        if(request.body()!=null)builder.setEntity(new ByteArrayEntity(request.body()));
        HttpUriRequest outgoing=builder.build();
        AtomicBoolean expired=new AtomicBoolean();active.add(outgoing);
        ScheduledFuture<?> cancellation=timer.schedule(()->{expired.set(true);outgoing.abort();},deadlineMillis,TimeUnit.MILLISECONDS);
        try(CloseableHttpResponse incoming=http.execute(outgoing)) {
            ByteArrayOutputStream buffer=new ByteArrayOutputStream();
            if(incoming.getEntity()!=null)try(InputStream input=incoming.getEntity().getContent()) {
                byte[] chunk=new byte[8192];int n;while((n=input.read(chunk))!=-1) {
                    if(buffer.size()+n>8*1024*1024)throw new IOException("权限HTTP响应超过8MiB上限");buffer.write(chunk,0,n);
                }
            }
            Map<String,Collection<String>> headers=new LinkedHashMap<>();for(Header h:incoming.getAllHeaders())headers.computeIfAbsent(h.getName(),key->new ArrayList<>()).add(h.getValue());
            if(expired.get())throw new SocketTimeoutException("权限HTTP超过总截止时间");
            return Response.builder().request(request).status(incoming.getStatusLine().getStatusCode()).reason(incoming.getStatusLine().getReasonPhrase()).headers(headers).body(buffer.toByteArray()).build();
        } finally {
            cancellation.cancel(false);active.remove(outgoing);outgoing.abort();
        }
    }
    public int leasedConnections() {return pool.getTotalStats().getLeased();}
    @Override public void close() throws IOException {for(HttpUriRequest request:active)request.abort();timer.shutdownNow();http.close();pool.close();}
}

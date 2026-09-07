package com.tce.smart.schedule.config;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.netflix.loadbalancer.*;
import feign.*;
import feign.codec.*;
import org.apache.http.conn.util.InetAddressUtils;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.context.annotation.*;
import java.net.*;
import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/** 独立连接池和实际请求取消覆盖连接获取、响应头及完整响应体；不复用旧任务客户端。 */
@Configuration
public class AuthOperationHttpConfiguration {
    public static final int CONNECT_MILLIS=2000,READ_MILLIS=5000,ACQUIRE_MILLIS=1000,DEADLINE_MILLIS=7000;
    @Bean(destroyMethod="close")
    public HttpResources authOperationHttpResources(SpringClientFactory factory) {
        // 只读取发现客户端维护的本地健康实例快照，请求路径不发起同步发现或DNS。
        ILoadBalancer balancer=factory.getLoadBalancer(ServiceNameConstants.SMART_DISPATCHER);
        AtomicInteger sequence=new AtomicInteger();
        return new HttpResources(new AuthOperationDeadlineClient(64,ACQUIRE_MILLIS,CONNECT_MILLIS,READ_MILLIS,DEADLINE_MILLIS,uri->{
            List<Server> servers=balancer.getReachableServers();
            if(servers==null || servers.isEmpty())throw new IllegalStateException("权限调度没有健康的dispatcher实例");
            Server server=servers.get(Math.floorMod(sequence.getAndIncrement(),servers.size()));
            String host=server.getHost();
            if(!InetAddressUtils.isIPv4Address(host) && !InetAddressUtils.isIPv6Address(host))throw new IllegalStateException("权限dispatcher发现地址必须为IP，避免不可取消DNS越过总截止时间");
            try {return new URI(server.getScheme()==null?uri.getScheme():server.getScheme(),null,host,server.getPort(),uri.getPath(),uri.getQuery(),null);}
            catch(URISyntaxException e){throw new IllegalArgumentException("权限dispatcher发现地址不合法",e);}
        }));
    }
    @Bean("authOperationRemoteDispatcher")
    public RemoteDispatcherService authOperationRemoteDispatcher(FeignContext context,HttpResources resources) {
        return dispatcher(context,resources.client);
    }
    public RemoteDispatcherService dispatcher(FeignContext context,Client client) {
        String name=ServiceNameConstants.SMART_DISPATCHER;
        Feign.Builder builder=Feign.builder().client(client).encoder(context.getInstance(name,Encoder.class))
            .decoder(context.getInstance(name,Decoder.class)).contract(context.getInstance(name,Contract.class))
            .options(options()).retryer(Retryer.NEVER_RETRY);
        Map<String,RequestInterceptor> interceptors=context.getInstances(name,RequestInterceptor.class);
        if(interceptors!=null)builder.requestInterceptors(interceptors.values());
        return builder.target(RemoteDispatcherService.class,"http://"+name);
    }
    /** 只暴露资源生命周期，不实现Client，避免普通Feign继承专用dispatcher路由。 */
    public static final class HttpResources implements Closeable {
        private final AuthOperationDeadlineClient client;
        private HttpResources(AuthOperationDeadlineClient client) {this.client=client;}
        @Override public void close() throws IOException {client.close();}
    }
    public static Request.Options options() {return new Request.Options(CONNECT_MILLIS,READ_MILLIS);}
}

package com.tce.smart.schedule.config;

import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.sun.net.httpserver.HttpServer;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import feign.Client;
import feign.Contract;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.junit.Test;
import org.springframework.cloud.netflix.ribbon.DefaultServerIntrospector;
import org.springframework.cloud.netflix.ribbon.ServerIntrospector;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.*;
import org.springframework.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.*;
import org.springframework.core.env.MapPropertySource;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.HttpHost;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import static org.junit.Assert.*;

/** 用真实容器和两个独占回环端点验证专用HTTP资源不会污染普通Feign装配。 */
public class AuthOperationHttpConfigurationTest {
    @Test public void enabledSchedulerMustPreserveOrdinaryFeignRoute() throws Exception { assertRoutes(true); }
    @Test public void disabledSchedulerMustPreserveOrdinaryFeignRoute() throws Exception { assertRoutes(false); }

    private void assertRoutes(boolean enabled) throws Exception {
        try (Fixture fixture=new Fixture(enabled)) {
            assertEquals("ordinary",fixture.context.getBean(OtherService.class).probe());
            assertEquals(1,fixture.ordinaryHits.get());
            assertEquals(0,fixture.dispatcherHits.get());
            Client global=fixture.context.getBean(Client.class);
            assertTrue(global instanceof LoadBalancerFeignClient);
            FeignContext children=fixture.context.getBean(FeignContext.class);
            assertSame(global,children.getInstance("other-service",Client.class));
            assertSame(global,children.getInstance(ServiceNameConstants.SMART_DISPATCHER,Client.class));
            assertEquals(1,fixture.context.getBeansOfType(Client.class).size());
            RemoteDispatcherService dedicated=fixture.context.getBean("authOperationRemoteDispatcher",RemoteDispatcherService.class);
            assertTrue(proxyClient(dedicated) instanceof AuthOperationDeadlineClient);
            assertNotSame(global,proxyClient(dedicated));
            dedicated.eventHandle("{}","Y");
            assertEquals(1,fixture.dispatcherHits.get());
            assertEquals(1,fixture.ordinaryHits.get());
        }
    }

    @Test public void closingApplicationMustCloseDedicatedPoolAndTimer() throws Exception {
        try (Fixture fixture=new Fixture(false)) {
            RemoteDispatcherService dedicated=fixture.context.getBean("authOperationRemoteDispatcher",RemoteDispatcherService.class);
            AuthOperationDeadlineClient client=(AuthOperationDeadlineClient)proxyClient(dedicated);
            dedicated.eventHandle("{}","Y");
            ScheduledThreadPoolExecutor timer=(ScheduledThreadPoolExecutor)field(client,"timer");
            PoolingHttpClientConnectionManager pool=(PoolingHttpClientConnectionManager)field(client,"pool");
            assertFalse(timer.isShutdown());
            assertEquals(0,client.leasedConnections());
            fixture.context.close();
            assertTrue(timer.isShutdown());
            assertTrue(timer.getQueue().isEmpty());
            assertEquals(0,client.leasedConnections());
            try {
                pool.requestConnection(new HttpRoute(new HttpHost("127.0.0.1",fixture.dispatcher.getAddress().getPort())),null);
                fail("关闭容器后专用连接池仍可分配连接");
            } catch (IllegalStateException expected) {
                // 真实连接池关闭后必须拒绝再次分配。
            }
        }
    }

    private static Client proxyClient(Object proxy) throws Exception {
        Map<?,?> methods=(Map<?,?>)field(Proxy.getInvocationHandler(proxy),"dispatch");
        return (Client)field(methods.values().iterator().next(),"client");
    }
    private static Object field(Object value,String name) throws Exception {
        Field field=value.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(value);
    }

    @FeignClient(name="other-service")
    public interface OtherService { @GetMapping("/probe") String probe(); }

    @Configuration
    @EnableFeignClients(clients=OtherService.class)
    public static class FeignFixtures {
        @Bean public Contract contract() {return new SpringMvcContract();}
        @Bean public Encoder encoder() {return new Encoder.Default();}
        @Bean public Decoder decoder() {
            return (response,type)->type==String.class?new Decoder.Default().decode(response,type):null;
        }
    }

    /** 服务发现只替换为本地静态快照；Feign和Ribbon的条件装配及请求执行均使用实际实现。 */
    private static class Fixture implements AutoCloseable {
        final AtomicInteger ordinaryHits=new AtomicInteger(),dispatcherHits=new AtomicInteger();
        final HttpServer ordinary=server("ordinary",ordinaryHits),dispatcher=server("dispatcher",dispatcherHits);
        final AnnotationConfigApplicationContext context=new AnnotationConfigApplicationContext();
        Fixture(boolean enabled) throws Exception {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("fixture",
                Collections.<String,Object>singletonMap("smart.auth-scheduler.enabled",enabled)));
            context.getBeanFactory().registerSingleton("ribbonClientFactory",new SpringClientFactory() {
                @Override public ILoadBalancer getLoadBalancer(String name) {
                    return new StaticBalancer(ServiceNameConstants.SMART_DISPATCHER.equals(name)?dispatcher:ordinary);
                }
                @Override public IClientConfig getClientConfig(String name) {return DefaultClientConfigImpl.getClientConfigWithDefaultValues(name);}
                @Override public <C> C getInstance(String name,Class<C> type) {
                    if(type==ServerIntrospector.class)return type.cast(new DefaultServerIntrospector());
                    throw new IllegalArgumentException("夹具不支持该Ribbon依赖："+type);
                }
            });
            context.register(FeignFixtures.class,AuthOperationHttpConfiguration.class,
                FeignAutoConfiguration.class,FeignRibbonClientAutoConfiguration.class,
                org.springframework.cloud.commons.httpclient.HttpClientConfiguration.class);
            try {context.refresh();}catch(RuntimeException e){close();throw e;}
        }
        public void close() {context.close();ordinary.stop(0);dispatcher.stop(0);}
    }
    private static HttpServer server(String body,AtomicInteger hits) throws Exception {
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/",exchange->{
            hits.incrementAndGet();byte[] bytes=body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200,bytes.length);
            try(java.io.OutputStream output=exchange.getResponseBody()){output.write(bytes);}
            exchange.close();
        });
        server.start();return server;
    }
    private static class StaticBalancer implements ILoadBalancer {
        final Server server;
        StaticBalancer(HttpServer endpoint){server=new Server("127.0.0.1",endpoint.getAddress().getPort());}
        public void addServers(List<Server> servers){throw new UnsupportedOperationException();}
        public Server chooseServer(Object key){return server;}
        public void markServerDown(Server ignored){}
        public List<Server> getServerList(boolean available){return getAllServers();}
        public List<Server> getReachableServers(){return getAllServers();}
        public List<Server> getAllServers(){return Collections.singletonList(server);}
    }
}

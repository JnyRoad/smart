package com.tce.smart.platform.controller.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tce.smart.platform.service.print.PrintApiException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.UUID;

/** 在 JSON 反序列化之前限制实际请求字节，覆盖缺少 Content-Length 的请求。 */
@Component
public class PrintRequestFilter extends OncePerRequestFilter {
    private static final int MAX_BODY = 3 * 1024 * 1024;
    @Override protected boolean shouldNotFilter(HttpServletRequest request) { return !request.getServletPath().startsWith("/print/v1/"); }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String requestId = UUID.randomUUID().toString(); request.setAttribute("print.requestId", requestId); response.setHeader("X-Request-Id", requestId);
        if (!"POST".equals(request.getMethod()) && !"PATCH".equals(request.getMethod())) { chain.doFilter(request, response); return; }
        // 图片由资源服务先鉴权，再按20MiB流式限制读取，避免套用JSON的3MiB限制及重复缓冲。
        if ("POST".equals(request.getMethod()) && ("/print/v1/resources".equals(request.getServletPath()) || "/print/v1/resources/".equals(request.getServletPath()))) { chain.doFilter(request,response); return; }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(); byte[] chunk = new byte[8192]; int count;
        InputStream inputBody = request.getInputStream();
        while ((count = inputBody.read(chunk)) != -1) {
            if (buffer.size() + count > MAX_BODY) {
                response.setStatus(422); response.setContentType("application/json;charset=UTF-8"); new ObjectMapper().writeValue(response.getOutputStream(), PrintApiAdvice.error(new PrintApiException(422, "PAYLOAD_LIMIT_EXCEEDED", "请求正文超出限制"), requestId).getBody()); return;
            }
            buffer.write(chunk, 0, count);
        }
        final byte[] body = buffer.toByteArray();
        chain.doFilter(new HttpServletRequestWrapper(request) {
            @Override public ServletInputStream getInputStream() {
                ByteArrayInputStream input = new ByteArrayInputStream(body);
                return new ServletInputStream() {
                    @Override public boolean isFinished() { return input.available() == 0; }
                    @Override public boolean isReady() { return true; }
                    @Override public void setReadListener(ReadListener listener) { throw new UnsupportedOperationException("打印 JSON 接口只使用同步读取"); }
                    @Override public int read() { return input.read(); }
                    @Override public int read(byte[] bytes, int offset, int length) { return input.read(bytes, offset, length); }
                };
            }
            @Override public BufferedReader getReader() { return new BufferedReader(new InputStreamReader(getInputStream(), java.nio.charset.StandardCharsets.UTF_8)); }
        }, response);
    }
}

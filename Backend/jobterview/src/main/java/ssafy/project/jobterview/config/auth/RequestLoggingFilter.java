package ssafy.project.jobterview.config.auth;

import org.springframework.stereotype.Component;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 로깅 작업 수행 (예: 로깅 라이브러리 사용)
        System.out.println("Incoming request: " + request);

        // 다음 필터로 체인을 진행하거나 요청을 처리합니다.
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 초기화 로직 (필요하다면 구현)
    }

    @Override
    public void destroy() {
        // 필터가 파기될 때 호출될 메서드 (필요하다면 구현)
    }
}
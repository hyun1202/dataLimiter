package example.alarm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 모든 origin 허용 (개발용, 운영에서는 특정 도메인으로 제한 권장)
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        
        // 허용할 HTTP 메서드
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 허용할 헤더
        config.addAllowedHeader("*");
        
        // 노출할 헤더 (클라이언트에서 접근 가능한 헤더)
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        
        // preflight 요청 캐시 시간 (초)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
package com.block.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

 //   private final SubscriptionInterceptor subscriptionInterceptor;
    private final AppProperties           appProperties;

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(subscriptionInterceptor)
//                .addPathPatterns("/api/v1/**")
//                .excludePathPatterns("/api/v1/auth/**", "/api/v1/subscription/**",
//                        "/api/v1/platform/**", "/api/v1/support/**", "/api/v1/announcements/**");
//    }

    /**
     * Expose user-uploaded files (e.g. clinic logos) at /uploads/**.
     * Files are written to {@code app.uploads.dir} by the upload endpoints.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadsRoot = Paths.get(appProperties.getUploads().getDir()).toAbsolutePath().normalize();
        try { Files.createDirectories(uploadsRoot); } catch (Exception ignored) { /* serve handler is idempotent */ }
        String location = uploadsRoot.toUri().toString();  // e.g. file:/abs/path/uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}

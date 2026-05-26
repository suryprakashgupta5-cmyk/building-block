package com.block.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Pagination pagination = new Pagination();
    private Uploads uploads = new Uploads();

    @Getter
    @Setter
    public static class Jwt {
        private String secret="mD7kP9xQ2vN5wR8tY3uA6bC1dE4fG0hJ2kL";
        private long accessTokenExpiryMs = 18_000_000L; // 5 hours
        private int    refreshTokenExpiryDays = 7;
    }

    @Getter
    @Setter
    public static class Uploads {
        /** Filesystem directory where user-uploaded files (clinic logos, etc.) are written. */
        private String dir = "./uploads";
        /** Max accepted clinic-logo file size in bytes (default 2 MB). */
        private long maxLogoSizeBytes = 2L * 1024L * 1024L;
    }

    @Getter
    @Setter
    public static class Cors {

        private List<String> allowedOrigins = List.of("http://localhost:4200", 

                 "http://192.168.1.151:4200", "http://192.168.1.151:4200/",
                 "http://192.168.1.158:4200", "http://192.168.1.158:4200/",
                 "http://mediomenadmin.appdemo.in","http://mediomenadmin.appdemo.in/",
                 "http://mediomen.appdemo.in","http://mediomen.appdemo.in/");
    }
   
    
    @Getter
    @Setter
    public static class Pagination {
        private int defaultPageSize = 20;
        private int maxPageSize     = 100;
    }
}

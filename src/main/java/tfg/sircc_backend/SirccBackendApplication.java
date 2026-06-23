// SirccBackendApplication.java
package tfg.sircc_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class SirccBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SirccBackendApplication.class, args);
    }
}
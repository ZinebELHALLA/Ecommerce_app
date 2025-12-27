package ma.ensaj.inventoryservice;

import ma.ensaj.inventoryservice.entities.Inventory;
import ma.ensaj.inventoryservice.repositories.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(InventoryServiceApplication.class, args);
    }
//    @Bean
//    CommandLineRunner initData(InventoryRepository repo) {
//        return args -> {
//            repo.save(new Inventory(null, "iphone_13", 100));
//            repo.save(new Inventory(null, "galaxy_s22", 0));
//            repo.save(new Inventory(null, "macbook_air", 50));
//        };
//    }


}

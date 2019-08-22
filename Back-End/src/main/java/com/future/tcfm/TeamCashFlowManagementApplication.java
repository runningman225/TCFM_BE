package com.future.tcfm;

import com.future.tcfm.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class TeamCashFlowManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(TeamCashFlowManagementApplication.class, args);
    }

}

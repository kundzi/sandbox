package com.crm.crm.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoLoader implements CommandLineRunner {

    private final ContactRepository repository;

    @Autowired
    public DemoLoader(ContactRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        repository.save(new Contact(
                "First 1", "Last 1", "email1@example.com"
        ));
        repository.save(new Contact(
                "First 2", "Last 2", "email2@example.com"
        ));
    }
}

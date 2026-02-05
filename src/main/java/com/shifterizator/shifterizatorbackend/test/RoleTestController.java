package com.shifterizator.shifterizatorbackend.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class RoleTestController {

    @GetMapping("/superadmin")
    public String superadmin() {
        return "SUPERADMIN OK";
    }

    @GetMapping("/companyadmin")
    public String companyadmin() {
        return "COMPANYADMIN OK";
    }

    @GetMapping("/shiftmanager")
    public String shiftmanager() {
        return "SHIFTMANAGER OK";
    }

    @GetMapping("/readonlymanager")
    public String readonlymanager() {
        return "READONLYMANAGER OK";
    }

    @GetMapping("/employee")
    public String employee() {
        return "EMPLOYEE OK";
    }
}

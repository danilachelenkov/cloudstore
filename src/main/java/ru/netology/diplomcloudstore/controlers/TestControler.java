package ru.netology.diplomcloudstore.controlers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestControler {

    @GetMapping("/cloud/data")
    public String getData(){
        return "data";
    }
}

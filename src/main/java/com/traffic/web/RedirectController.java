package com.traffic.web;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.traffic.service.LinkService;

@RestController
public class RedirectController {

    private final LinkService linkService;

    public RedirectController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping("/{slug:[A-Za-z0-9]{6,8}}")
    public ResponseEntity<Void> redirect(@PathVariable String slug) {
        return linkService
                .resolve(slug)
                .map(target -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setLocation(URI.create(target));
                    return new ResponseEntity<Void>(null, headers, HttpStatus.FOUND);
                })
                .orElseGet(() -> new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }
}

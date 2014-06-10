/*
 * Copyright (C) 2007-2013, GoodData(R) Corporation. All rights reserved.
 */
package net.javacrumbs.listenablefuture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import static java.lang.String.format;


@Controller
@EnableAutoConfiguration
public class ListenableFutureAsyncController {
    // Let's use Apache Async HTTP client
    private final AsyncRestTemplate restTemplate = new AsyncRestTemplate(new HttpComponentsAsyncClientHttpRequestFactory());

    @RequestMapping("/")
    @ResponseBody
    DeferredResult<String> home() {
        // Create DeferredResult with timeout 5s
        final DeferredResult<String> result = new DeferredResult<>(5000);

        // Let's call the backend
        ListenableFuture<ResponseEntity<String>> future = restTemplate.getForEntity("http://www.google.com", String.class);
        future.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
            @Override
            public void onSuccess(ResponseEntity<String> response) {
                // Will be called in HttpClient thread
                log("Success");
                result.setResult(response.getBody());
            }

            @Override
            public void onFailure(Throwable t) {
                result.setErrorResult(t.getMessage());
            }
        });
        // Return the thread to servlet container, the response will be processed by another thread.
        return result;
    }

    public static void log(Object message) {
        System.out.println(format("%s %s ", Thread.currentThread().getName(), message));
    }

    // That's all you need to start the application
    public static void main(String[] args) throws Exception {
        SpringApplication.run(ListenableFutureAsyncController.class, args);
    }
}
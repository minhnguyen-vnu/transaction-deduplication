package com.testingservice.adapater.outbound.http;

import com.testingservice.config.TestingProperties;
import com.testingservice.domain.model.Transaction;
import com.testingservice.domain.port.outbound.BPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class HttpBClient implements BPort {
    private final RestTemplate http;
    private final TestingProperties props;

    @Override
    public String execute(Transaction tx) {
        var url = props.getB().getBaseUrl() + props.getB().getPath();
        try {
            ResponseEntity<String> resp = http.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(tx, json()), String.class);
            String s = resp.getBody()==null ? "" : resp.getBody();
            return s.contains("SUCCESS") ? "SUCCESS" : s.contains("TIMEOUT") ? "TIMEOUT" : "UNKNOWN";
        } catch (Exception e) {
            return "ERROR";
        }
    }
    private static HttpHeaders json(){ var h=new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON); return h; }
}

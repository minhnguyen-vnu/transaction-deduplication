package com.testingservice.adapater.outbound.http;

import com.testingservice.config.TestingProperties;
import com.testingservice.domain.model.Transaction;
import com.testingservice.domain.port.outbound.DedupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class HttpDedupClient implements DedupPort {

    private final RestTemplate http;
    private final TestingProperties props;

    @Override
    public boolean isAllowed(Transaction tx) {
        Map<String,Object> body = Map.of("requestPayload", tx);
        var url = props.getDedup().getBaseUrl() + props.getDedup().getPath();
        try {
            ResponseEntity<String> resp = http.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, json()), String.class);
            String s = resp.getBody();
            return s != null && s.contains("\"ALLOW\"");
        } catch (Exception e) {
            return false;
        }
    }
    private static HttpHeaders json(){ var h=new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON); return h; }

}

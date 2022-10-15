package ru.freemiumhosting.master.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Service
public class DockerAPI {
    private final String dockerURI="http://ptsv2.com/t/n0xqj-1665816377/post";//Вставить ссылку
    public void postLink(String link)
    {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForEntity(dockerURI,link, String.class);

        //System.out.println(result);
    }
}

package com.bugboard.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
* espone la cartella locale udloads come risorsa statica
* raggiungibile tramite GET /uploads/{filename}
* in questo modo il frontend può scaricare le immagini 
* allegate alle issue senza doverle memorizzare nel database
*/

@Configuration

public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/");
    }
}

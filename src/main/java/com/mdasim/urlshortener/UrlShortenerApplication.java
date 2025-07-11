package com.mdasim.urlshortener;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.*;
import java.net.URI;

@SpringBootApplication
public class UrlShortenerApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(UrlShortenerApplication.class, args);
		System.out.println("🟢 Application started. Controller should be active.");
	}

	@Override
	public void run(String... args) throws Exception {
		String url = "http://localhost:8080/";
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(new URI(url));
				System.out.println("🌐 Browser opened: " + url);
			} else {
				System.out.println("❗ Desktop not supported. Open manually: " + url);
			}
		} catch (Exception e) {
			System.out.println("❗ Failed to open browser: " + e.getMessage());
		}
	}
}

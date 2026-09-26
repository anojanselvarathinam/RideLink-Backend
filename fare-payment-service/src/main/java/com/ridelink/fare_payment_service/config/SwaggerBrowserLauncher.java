package com.ridelink.fare_payment_service.config;

import java.awt.Desktop;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Developer convenience (mirrors account-service's SwaggerBrowserLauncher so
 * every service behaves the same): opens the Swagger UI in the default
 * browser as soon as the service has finished starting, at
 * http://localhost:&lt;port&gt;/swagger-ui.html.
 *
 * It runs only when BOTH are true, so {@code mvn test} and CI never open a
 * browser window:
 * <ul>
 *   <li>the {@code ridelink.swagger.auto-open} property is {@code true}
 *       (enabled in application.properties, disabled by the test suite), and</li>
 *   <li>a real web server is running ({@code local.server.port} is set -
 *       test contexts use Spring's MOCK environment, where it is absent).</li>
 * </ul>
 *
 * The tab is opened with the Windows shell first ({@code explorer.exe} - the
 * same approach as account-service) and falls back to the portable Java
 * {@link Desktop} API, because the JVM may run in a session where the AWT
 * desktop is unavailable. If neither works the failure is only logged and the
 * API docs stay reachable manually.
 */
@Component
public class SwaggerBrowserLauncher {

	private static final Logger log = LoggerFactory.getLogger(SwaggerBrowserLauncher.class);

	private final Environment environment;

	public SwaggerBrowserLauncher(Environment environment) {
		this.environment = environment;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void openSwaggerUi() {
		boolean enabled = Boolean.parseBoolean(environment.getProperty("ridelink.swagger.auto-open", "false"));
		String port = environment.getProperty("local.server.port");
		if (!enabled || port == null) {
			return;
		}
		String url = "http://localhost:" + port + "/swagger-ui.html";
		if (openInBrowser(url)) {
			log.info("Swagger UI opened in the default browser: {}", url);
		} else {
			log.warn("Could not open the browser automatically. Open {} manually.", url);
		}
	}

	private boolean openInBrowser(String url) {
		try {
			// Windows shell first - works even when AWT cannot reach a desktop.
			new ProcessBuilder("explorer.exe", url).start();
			return true;
		} catch (Exception ex) {
			log.debug("explorer.exe could not open {}: {}", url, ex.toString());
		}
		try {
			Desktop.getDesktop().browse(URI.create(url));
			return true;
		} catch (Exception ex) {
			log.debug("Desktop.browse could not open {}: {}", url, ex.toString());
			return false;
		}
	}
}

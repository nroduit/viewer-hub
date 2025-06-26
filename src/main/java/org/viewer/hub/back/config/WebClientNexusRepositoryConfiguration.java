package org.viewer.hub.back.config;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.viewer.hub.back.enums.RepositoryAuthType;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.Objects;

/**
 * Configure the different web clients for the Nexus Repository
 */
@Configuration
@Slf4j
public class WebClientNexusRepositoryConfiguration {

	@Value("${weasis.repository.url}")
	private String repositoryUrl;

	@Value("${weasis.repository.search-assets-api}")
	private String repositorySearchAssetsApi;

	@Value("${weasis.repository.download-assets-api}")
	private String repositoryDownloadAssetsApi;

	@NotNull
	@Value("${weasis.repository.authentication.type:NONE}")
	private RepositoryAuthType authenticationType;

	@Value("${weasis.repository.authentication.basic.login}")
	private String basicAuthLogin;

	@Value("${weasis.repository.authentication.basic.password}")
	private String basicAuthPwd;

	/**
	 * Bean used to look for assets repository
	 * @return WebClient built
	 */
	@Bean
	WebClient webClientSearchAssetsRepository() {
		return webClientBuilder().baseUrl("%s%s".formatted(repositoryUrl, repositorySearchAssetsApi)).build();
	}

	/**
	 * Bean used to download assets in the defined repository
	 * @return WebClient built
	 */
	@Bean
	WebClient webClientDownloadAssetsRepository() {
		final int size = (int) DataSize.ofMegabytes(100).toBytes();
		final ExchangeStrategies strategies = ExchangeStrategies.builder()
			.codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
			.build();

		return webClientBuilder().exchangeStrategies(strategies)
			// https://stackoverflow.com/questions/45539521/nexus-artifact-download-using-curl
			// https://stackoverflow.com/questions/47655789/how-to-make-reactive-webclient-follow-3xx-redirects
			.clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
			.baseUrl("%s%s".formatted(repositoryUrl, repositoryDownloadAssetsApi))
			.build();
	}

	/**
	 * Builder of a webClient depending on the authentication type selected
	 * @return WebClient Builder
	 */
	private WebClient.Builder webClientBuilder() {
		WebClient.Builder webClientBuilder;
		if (Objects.equals(authenticationType, RepositoryAuthType.BASIC)) {
			webClientBuilder = WebClient.builder()
				.defaultHeaders(header -> header.setBasicAuth(basicAuthLogin, basicAuthPwd))
				.filter(this.logRequest())
				.filter(this.logResponse());
		}
		else {
			webClientBuilder = WebClient.builder().filter(this.logRequest()).filter(this.logResponse());
		}
		return webClientBuilder;
	}

	/**
	 * Log requests
	 * @return ExchangeFilterFunction
	 */
	private ExchangeFilterFunction logRequest() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			StringBuilder builder = new StringBuilder("Request: \n");
			clientRequest.headers()
				.forEach((name, values) -> values
					.forEach(value -> builder.append(name).append(":").append(value).append("\n")));
			LOG.debug(builder.toString());
			return Mono.just(clientRequest);
		});
	}

	/**
	 * Log responses
	 * @return ExchangeFilterFunction
	 */
	private ExchangeFilterFunction logResponse() {
		return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
			StringBuilder builder = new StringBuilder("Response: \n");
			clientResponse.headers()
				.asHttpHeaders()
				.forEach((name, values) -> values
					.forEach(value -> builder.append(name).append(":").append(value).append("\n")));
			LOG.debug(builder.toString());
			return Mono.just(clientResponse);
		});
	}

}

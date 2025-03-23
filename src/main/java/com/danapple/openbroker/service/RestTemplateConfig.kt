import com.danapple.openbroker.repository.ConfigurationRepository
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(configurationRepository: ConfigurationRepository): RestTemplate {
        // Retrieve the brokerId from the configuration
        val brokerId = getBrokerIdFromConfig(configurationRepository)

        val httpClient = HttpClients.createDefault() // Apache HttpClient 5
        val factory = HttpComponentsClientHttpRequestFactory(httpClient)

        val restTemplate = RestTemplate(factory)

        // Add interceptor to set the brokerId as a cookie in every request
        restTemplate.interceptors.add { request, body, execution ->
            // Set the brokerId as a cookie in the request
            request.headers.add(HttpHeaders.COOKIE, "brokerId=$brokerId")

            return@add execution.execute(request, body)
        }

        return restTemplate
    }

    private fun getBrokerIdFromConfig(configurationRepository: ConfigurationRepository): String {
        // Retrieve the brokerId from the configuration table
        val config = configurationRepository.getBrokerId()
        return config ?: throw IllegalStateException("Broker ID not found in configuration")
    }
}

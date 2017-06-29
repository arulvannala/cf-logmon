package org.cloudfoundry.rivendell

import org.assertj.core.api.Assertions.assertThat
import org.cloudfoundry.rivendell.support.getHtml
import org.cloudfoundry.rivendell.support.xpath
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.CounterService
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StatisticsUiTest {
    @LocalServerPort
    private var port: Int = -1

    private val baseUrl
        get() = "http://localhost:$port/"

    @Autowired
    private lateinit var counterService: CounterService

    @Autowired
    private lateinit var http: TestRestTemplate

    @Test
    fun theApp_shouldDisplayNumberOfWrites() {
        var body = http.getForObject(baseUrl, String::class.java).getHtml()
        var titleNodes = body.xpath("//div[@class='metric metric-total-writes']")
        assertThat(titleNodes.length).isEqualTo(1)
        assertThat(titleNodes.item(0).textContent).isEqualToIgnoringCase("0")

        counterService.increment("rivendell.logs.written")
        counterService.increment("rivendell.logs.written")
        counterService.increment("rivendell.logs.written")
        counterService.increment("rivendell.logs.written")
        counterService.increment("rivendell.logs.written")

        body = http.getForObject(baseUrl, String::class.java).getHtml()
        titleNodes = body.xpath("//div[@class='metric metric-total-writes']")
        assertThat(titleNodes.item(0).textContent).isEqualToIgnoringCase("5")
    }

    @Test
    fun theApp_shouldDisplayNumberOfReads() {
        var body = http.getForObject(baseUrl, String::class.java).getHtml()
        var titleNodes = body.xpath("//div[@class='metric metric-total-reads']")
        assertThat(titleNodes.length).isEqualTo(1)
        assertThat(titleNodes.item(0).textContent).isEqualToIgnoringCase("0")

        counterService.increment("rivendell.logs.read")
        counterService.increment("rivendell.logs.read")
        counterService.increment("rivendell.logs.read")

        body = http.getForObject(baseUrl, String::class.java).getHtml()
        titleNodes = body.xpath("//div[@class='metric metric-total-reads']")
        assertThat(titleNodes.item(0).textContent).isEqualToIgnoringCase("3")
    }
}
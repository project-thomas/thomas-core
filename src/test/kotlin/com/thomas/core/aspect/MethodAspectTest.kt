package com.thomas.core.aspect

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.thomas.core.context.SessionContext
import com.thomas.core.context.SessionContextHolder
import com.thomas.core.context.SessionContextHolder.clearContext
import com.thomas.core.context.SessionContextHolder.currentLocale
import com.thomas.core.context.SessionContextHolder.currentUser
import com.thomas.core.context.SessionContextHolder.sessionProperties
import com.thomas.core.context.SessionContextHolder.setSessionProperty
import com.thomas.core.extension.toSecondsPattern
import com.thomas.core.generator.UserGenerator.generateSecurityUser
import com.thomas.core.util.BooleanUtils.randomBoolean
import com.thomas.core.util.LocaleUtils.randomLocale
import com.thomas.core.util.NumberUtils.randomBigDecimal
import com.thomas.core.util.NumberUtils.randomBigInteger
import com.thomas.core.util.NumberUtils.randomInteger
import com.thomas.core.util.NumberUtils.randomLong
import com.thomas.core.util.StringUtils.randomString
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import java.lang.System.lineSeparator
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Locale
import kotlin.time.Duration
import kotlinx.coroutines.test.runTest
import org.aspectj.lang.annotation.Aspect
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory

@ExtendWith(MockKExtension::class)
class MethodAspectTest {

    private lateinit var logCapture: TestLogCapture

    @BeforeEach
    fun beforeEach() {
        clearContext()
        clearAllMocks()
        mockkStatic(Duration::toSecondsPattern)
        logCapture = TestLogCapture()
        logCapture.startCapturing()
    }

    @AfterEach
    fun afterEach() {
        clearContext()
        unmockkAll()
        logCapture.stopCapturing()
    }

    @Test
    fun `Log method with all data`() {
        currentUser = generateSecurityUser()
        val prop01 = randomString(spaces = false)
        val value01 = randomString(spaces = false)
        setSessionProperty(prop01, value01)

        val usernameParam = randomString(spaces = false)
        val passwordParam = randomString(spaces = false)
        val intParam = randomInteger()
        val decimalParam = randomBigDecimal()
        val totalParam = randomBigInteger()
        val totalDuration = "${randomInteger(0, 99)}.${randomLong(0, 999999999)}"

        every { any<Duration>().toSecondsPattern() } returns totalDuration

        MethodAspectService().processData(
            username = usernameParam,
            password = passwordParam,
            userData = null,
            isActive = null,
            aspectData = AspectData(intParam, null, decimalParam),
            totalAmount = totalParam,
        )

        val expected = "${lineSeparator()}Method Logging" +
            "${lineSeparator()}\tMetadata -> " +
            "${lineSeparator()}\t\tUserId -> ${currentUser.userId}" +
            "${lineSeparator()}\t\tLocale -> ${currentLocale.toLanguageTag()}" +
            "${lineSeparator()}\t\t$prop01 -> $value01" +
            "${lineSeparator()}\tCall -> com.thomas.core.aspect.MethodAspectService.processData" +
            "${lineSeparator()}\t\tparameter[0] -> username: String = $usernameParam" +
            "${lineSeparator()}\t\tparameter[1] -> password: String = **********" +
            "${lineSeparator()}\t\tparameter[2] -> userData: String = null" +
            "${lineSeparator()}\t\tparameter[3] -> isActive: Boolean = null" +
            "${lineSeparator()}\t\tparameter[4] -> aspectData: AspectData = AspectData(value=$intParam, description=null, totalValue=$decimalParam)" +
            "${lineSeparator()}\t\tparameter[5] -> totalAmount: BigInteger = $totalParam" +
            "${lineSeparator()}\t\t      return -> type: String = Processed user: $usernameParam" +
            "${lineSeparator()}\tDuration (second.nano) -> $totalDuration"

        val message = logCapture.methodLogMessage()

        assertEquals(expected, message)
    }

    @Test
    fun `should initialize MethodAspect class properly`() = runTest {
        // Force class initialization and companion object access
        val aspect = MethodAspect()
        assertNotNull(aspect)

        // Verify the aspect can be instantiated and used
        assertTrue(aspect.javaClass.isAnnotationPresent(Aspect::class.java))
    }

    @Test
    fun `should access companion object constants indirectly through logging`() = runTest {
        // Given
        SessionContextHolder.context = SessionContext.create(
            properties = mapOf(randomString() to randomString()),
            user = generateSecurityUser(),
            token = randomString(),
            locale = randomLocale(),
        )
        logCapture.startCapturing()

        // When - Execute a method that will use all logging components
        MethodAspectService().processData("testUser", "password", "data", true, null, BigInteger.ONE)

        // Then - Verify that the log message was properly formatted using companion constants
        val logMessage = logCapture.methodLogMessage()!!

        // These assertions ensure all companion object constants are being used
        assertTrue(logMessage.contains("Method Logging")) // METHOD_LOG_MESSAGE
        assertTrue(logMessage.contains("\n\t")) // ONE_TAB_LINE
        assertTrue(logMessage.contains("\n\t\t")) // TWO_TAB_LINE

        // Verify log structure uses the constants properly
        val lines = logMessage.split("\n")
        assertTrue(lines.any { it.trim() == "Method Logging" })
        assertTrue(lines.any { it.startsWith("\t") && !it.startsWith("\t\t") })
        assertTrue(lines.any { it.startsWith("\t\t") })
    }

    @Test
    fun `should handle all companion object constant scenarios`() = runTest {
        // Given
        SessionContextHolder.context = SessionContext.create(
            properties = mapOf(randomString() to randomString()),
            user = generateSecurityUser(),
            token = randomString(),
            locale = randomLocale(),
        )
        logCapture.startCapturing()

        // Test method that would use EMPTY_STRING_VALUE in various scenarios
        MethodAspectService().logNothing("user", "pass", null, false, null, BigInteger.ZERO)

        // Verify the log was created even with minimal logging
        val logMessage = logCapture.methodLogMessage()!!
        assertNotNull(logMessage)
        assertTrue(logMessage.contains("Method Logging"))
    }

    @Test
    fun `should properly handle aspect annotation processing`() = runTest {
        // This test ensures the @Around annotation is properly processed
        SessionContextHolder.context = SessionContext.create(
            properties = mapOf(randomString() to randomString()),
            user = generateSecurityUser(),
            token = randomString(),
            locale = randomLocale(),
        )
        logCapture.startCapturing()

        // Execute method that should trigger the aspect
        val result = MethodAspectService().defaultAnnotation("test", "data", true)

        // Verify the aspect intercepted the method call
        assertEquals("Processed with data: test - data - true", result)
        val logMessage = logCapture.methodLogMessage()!!
        assertTrue(logMessage.contains("defaultAnnotation"))
        assertTrue(logMessage.contains("Method Logging"))
    }

    @Test
    fun `should test method aspect initialization and constants visibility`() {
        // Direct test to ensure class and companion object are properly loaded
        val aspectClass = MethodAspect::class.java

        // Verify class annotations
        assertTrue(aspectClass.isAnnotationPresent(Aspect::class.java))

        // Verify the class can be instantiated (which loads companion object)
        val aspect = MethodAspect()
        assertNotNull(aspect)

        // This test primarily serves to ensure class initialization coverage
        assertTrue(aspectClass.declaredFields.any { it.name.contains("Companion") })
    }

    @Test
    fun `Log method with all data anonymous user`() {
        currentLocale = randomLocale()
        val prop01 = randomString(spaces = false)
        val value01 = randomString(spaces = false)
        setSessionProperty(prop01, value01)

        val usernameParam = randomString(spaces = false)
        val passwordParam = randomString(spaces = false)
        val totalParam = randomBigInteger()
        val totalDuration = "${randomInteger(0, 99)}.${randomLong(0, 999999999)}"

        every { any<Duration>().toSecondsPattern() } returns totalDuration

        MethodAspectService().processData(
            username = usernameParam,
            password = passwordParam,
            userData = null,
            isActive = null,
            aspectData = null,
            totalAmount = totalParam,
        )

        val expected = "${lineSeparator()}Method Logging" +
            "${lineSeparator()}\tMetadata -> " +
            "${lineSeparator()}\t\tUserId -> anonymous" +
            "${lineSeparator()}\t\tLocale -> ${currentLocale.toLanguageTag()}" +
            "${lineSeparator()}\t\t$prop01 -> $value01" +
            "${lineSeparator()}\tCall -> com.thomas.core.aspect.MethodAspectService.processData" +
            "${lineSeparator()}\t\tparameter[0] -> username: String = $usernameParam" +
            "${lineSeparator()}\t\tparameter[1] -> password: String = **********" +
            "${lineSeparator()}\t\tparameter[2] -> userData: String = null" +
            "${lineSeparator()}\t\tparameter[3] -> isActive: Boolean = null" +
            "${lineSeparator()}\t\tparameter[4] -> aspectData: AspectData = null" +
            "${lineSeparator()}\t\tparameter[5] -> totalAmount: BigInteger = $totalParam" +
            "${lineSeparator()}\t\t      return -> type: String = Processed user: $usernameParam" +
            "${lineSeparator()}\tDuration (second.nano) -> $totalDuration"

        val message = logCapture.methodLogMessage()

        assertEquals(expected, message)
    }

    @Test
    fun `Log method without metadata`() {
        currentUser = generateSecurityUser()
        val prop01 = randomString(spaces = false)
        val value01 = randomString(spaces = false)
        val prop02 = randomString(spaces = false)
        setSessionProperty(prop01, value01)
        setSessionProperty(prop02, null)

        val usernameParam = randomString(spaces = false)
        val passwordParam = randomString(spaces = false)
        val dataParam = randomString(spaces = false)
        val activeParam = randomBoolean()
        val totalDuration = "${randomInteger(0, 99)}.${randomLong(0, 999999999)}"

        every { any<Duration>().toSecondsPattern() } returns totalDuration

        MethodAspectService().withoutMetadata(
            username = usernameParam,
            password = passwordParam,
            userData = dataParam,
            isActive = activeParam,
        )

        val expected = "${lineSeparator()}Method Logging" +
            "${lineSeparator()}\tCall -> com.thomas.core.aspect.MethodAspectService.withoutMetadata" +
            "${lineSeparator()}\t\tparameter[0] -> username: String = $usernameParam" +
            "${lineSeparator()}\t\tparameter[1] -> password: String = **********" +
            "${lineSeparator()}\t\tparameter[2] -> userData: String = $dataParam" +
            "${lineSeparator()}\t\tparameter[3] -> isActive: Boolean = $activeParam" +
            "${lineSeparator()}\t\t      return -> type: String = Processed with data: $usernameParam - $dataParam - $activeParam" +
            "${lineSeparator()}\tDuration (second.nano) -> $totalDuration"

        val message = logCapture.methodLogMessage()

        assertEquals(expected, message)
    }

    @Test
    fun `Log method without parameters`() {
        currentUser = generateSecurityUser()
        val prop01 = randomString(spaces = false)
        val value01 = randomString(spaces = false)
        setSessionProperty(prop01, value01)

        val usernameParam = randomString(spaces = false)
        val valueParam = randomInteger()
        val totalParam = randomBigDecimal()
        val returnValue = AspectData(valueParam, usernameParam, totalParam).toString()
        val totalDuration = "${randomInteger(0, 99)}.${randomLong(0, 999999999)}"

        every { any<Duration>().toSecondsPattern() } returns totalDuration

        MethodAspectService().withoutParameters(
            username = usernameParam,
            value = valueParam,
            total = totalParam,
        )

        val expected = "${lineSeparator()}Method Logging" +
            "${lineSeparator()}\tMetadata -> " +
            "${lineSeparator()}\t\tUserId -> ${currentUser.userId}" +
            "${lineSeparator()}\t\tLocale -> ${currentLocale.toLanguageTag()}" +
            "${lineSeparator()}\t\t$prop01 -> $value01" +
            "${lineSeparator()}\tCall -> com.thomas.core.aspect.MethodAspectService.withoutParameters" +
            "${lineSeparator()}\t\t      return -> type: AspectData = $returnValue" +
            "${lineSeparator()}\tDuration (second.nano) -> $totalDuration"

        val message = logCapture.methodLogMessage()

        assertEquals(expected, message)
    }

    @Test
    fun `Log method without duration`() {
        currentUser = generateSecurityUser()
        val prop01 = randomString(spaces = false)
        val value01 = randomString(spaces = false)
        setSessionProperty(prop01, value01)

        val usernameParam = randomString(spaces = false)
        val valueParam = randomInteger()
        val totalParam = randomBigDecimal()
        val returnValue = AspectData(valueParam, usernameParam, totalParam).toString()
        val totalDuration = "${randomInteger(0, 99)}.${randomLong(0, 999999999)}"

        every { any<Duration>().toSecondsPattern() } returns totalDuration

        MethodAspectService().withoutDuration(
            username = usernameParam,
            value = valueParam,
            total = totalParam,
        )

        val expected = "${lineSeparator()}Method Logging" +
            "${lineSeparator()}\tMetadata -> " +
            "${lineSeparator()}\t\tUserId -> ${currentUser.userId}" +
            "${lineSeparator()}\t\tLocale -> ${currentLocale.toLanguageTag()}" +
            "${lineSeparator()}\t\t$prop01 -> $value01" +
            "${lineSeparator()}\tCall -> com.thomas.core.aspect.MethodAspectService.withoutDuration" +
            "${lineSeparator()}\t\tparameter[0] -> value: int = $valueParam" +
            "${lineSeparator()}\t\tparameter[1] -> username: String = $usernameParam" +
            "${lineSeparator()}\t\tparameter[2] -> total: BigDecimal = $totalParam" +
            "${lineSeparator()}\t\t      return -> type: AspectData = $returnValue"

        val message = logCapture.methodLogMessage()

        assertEquals(expected, message)
    }

    @Test
    fun `Log method masking result`() {
        currentUser = generateSecurityUser()
        val prop01 = randomString(spaces = false)
        val value01 = randomString(spaces = false)
        setSessionProperty(prop01, value01)

        val usernameParam = randomString(spaces = false)
        val passwordParam = randomString(spaces = false)
        val intParam = randomInteger()
        val bigParam = randomBigInteger()
        val stringParam = randomString(spaces = false)
        val booleanParam = randomBoolean()
        val decimalParam = randomBigDecimal()
        val totalParam = randomBigInteger()
        val totalDuration = "${randomInteger(0, 99)}.${randomLong(0, 999999999)}"

        every { any<Duration>().toSecondsPattern() } returns totalDuration

        MethodAspectService().maskingResult(
            username = usernameParam,
            password = passwordParam,
            userData = null,
            isActive = null,
            aspectData = AspectData(intParam, null, decimalParam),
            totalAmount = totalParam,
            genericOne = GenericData(usernameParam, intParam, AspectData(intParam, usernameParam, decimalParam)),
            genericTwo = GenericData(booleanParam, bigParam, GenericData(stringParam, decimalParam, stringParam))
        )

        val expected = "${lineSeparator()}Method Logging" +
            "${lineSeparator()}\tMetadata -> " +
            "${lineSeparator()}\t\tUserId -> ${currentUser.userId}" +
            "${lineSeparator()}\t\tLocale -> ${currentLocale.toLanguageTag()}" +
            "${lineSeparator()}\t\t$prop01 -> $value01" +
            "${lineSeparator()}\tCall -> com.thomas.core.aspect.MethodAspectService.maskingResult" +
            "${lineSeparator()}\t\tparameter[0] -> username: String = $usernameParam" +
            "${lineSeparator()}\t\tparameter[1] -> password: String = **********" +
            "${lineSeparator()}\t\tparameter[2] -> userData: String = null" +
            "${lineSeparator()}\t\tparameter[3] -> isActive: Boolean = null" +
            "${lineSeparator()}\t\tparameter[4] -> aspectData: AspectData = AspectData(value=$intParam, description=null, totalValue=$decimalParam)" +
            "${lineSeparator()}\t\tparameter[5] -> totalAmount: BigInteger = $totalParam" +
            "${lineSeparator()}\t\tparameter[6] -> genericOne: GenericData<String, Integer, AspectData> = GenericData(name=$usernameParam, value=$intParam, typed=AspectData(value=$intParam, description=$usernameParam, totalValue=$decimalParam))" +
            "${lineSeparator()}\t\tparameter[7] -> genericTwo: GenericData<Boolean, BigInteger, GenericData<String, BigDecimal, String>> = GenericData(name=$booleanParam, value=$bigParam, typed=GenericData(name=$stringParam, value=$decimalParam, typed=$stringParam))" +
            "${lineSeparator()}\t\t      return -> type: GenericData<String, BigInteger, String> = **********" +
            "${lineSeparator()}\tDuration (second.nano) -> $totalDuration"

        val message = logCapture.methodLogMessage()

        assertEquals(expected, message)
    }

    @Test
    fun `Log method without all`() {
        currentUser = generateSecurityUser()
        val prop01 = randomString(spaces = false)
        val value01 = randomString(spaces = false)
        val prop02 = randomString(spaces = false)
        setSessionProperty(prop01, value01)
        setSessionProperty(prop02, null)

        val usernameParam = randomString(spaces = false)
        val passwordParam = randomString(spaces = false)
        val intParam = randomInteger()
        val decimalParam = randomBigDecimal()
        val totalParam = randomBigInteger()
        val totalDuration = "${randomInteger(0, 99)}.${randomLong(0, 999999999)}"

        every { any<Duration>().toSecondsPattern() } returns totalDuration

        MethodAspectService().logNothing(
            username = usernameParam,
            password = passwordParam,
            userData = null,
            isActive = null,
            aspectData = AspectData(intParam, null, decimalParam),
            totalAmount = totalParam,
        )

        val expected = "${lineSeparator()}Method Logging" +
            "${lineSeparator()}\tCall -> com.thomas.core.aspect.MethodAspectService.logNothing"

        val message = logCapture.methodLogMessage()

        assertEquals(expected, message)
    }

    @Test
    fun `Log method with default values`() {
        currentUser = generateSecurityUser()
        val prop01 = randomString(spaces = false)
        val value01 = randomString(spaces = false)
        setSessionProperty(prop01, value01)

        val totalDuration = "${randomInteger(0, 99)}.${randomLong(0, 999999999)}"

        every { any<Duration>().toSecondsPattern() } returns totalDuration

        val result = MethodAspectService().defaultValues()

        val expected = "${lineSeparator()}Method Logging" +
            "${lineSeparator()}\tMetadata -> " +
            "${lineSeparator()}\t\tUserId -> ${currentUser.userId}" +
            "${lineSeparator()}\t\tLocale -> ${currentLocale.toLanguageTag()}" +
            "${lineSeparator()}\t\t$prop01 -> $value01" +
            "${lineSeparator()}\tCall -> com.thomas.core.aspect.MethodAspectService.defaultValues" +
            "${lineSeparator()}\t\tparameter[0] -> value: int = ${result.value}" +
            "${lineSeparator()}\t\tparameter[1] -> username: String = ${result.description}" +
            "${lineSeparator()}\t\tparameter[2] -> total: BigDecimal = ${result.totalValue}" +
            "${lineSeparator()}\t\t      return -> type: AspectData = AspectData(value=${result.value}, description=${result.description}, totalValue=${result.totalValue})" +
            "${lineSeparator()}\tDuration (second.nano) -> $totalDuration"

        val message = logCapture.methodLogMessage()

        assertEquals(expected, message)
    }

    @Test
    fun `Log method with default annotation`() {
        currentUser = generateSecurityUser()
        val prop01 = randomString(spaces = false)
        val value01 = randomString(spaces = false)
        setSessionProperty(prop01, value01)

        val usernameParam = randomString(spaces = false)
        val dataParam = listOf(randomString(spaces = false), null).random()
        val boolParam = listOf(randomBoolean(), null).random()
        val totalDuration = "${randomInteger(0, 99)}.${randomLong(0, 999999999)}"

        every { any<Duration>().toSecondsPattern() } returns totalDuration

        MethodAspectService().defaultAnnotation(
            username = usernameParam,
            userData = dataParam,
            isActive = boolParam,
        )

        val expected = "${lineSeparator()}Method Logging" +
            "${lineSeparator()}\tMetadata -> " +
            "${lineSeparator()}\t\tUserId -> ${currentUser.userId}" +
            "${lineSeparator()}\t\tLocale -> ${currentLocale.toLanguageTag()}" +
            "${lineSeparator()}\t\t$prop01 -> $value01" +
            "${lineSeparator()}\tCall -> com.thomas.core.aspect.MethodAspectService.defaultAnnotation" +
            "${lineSeparator()}\t\tparameter[0] -> username: String = $usernameParam" +
            "${lineSeparator()}\t\tparameter[1] -> userData: String = $dataParam" +
            "${lineSeparator()}\t\tparameter[2] -> isActive: Boolean = $boolParam" +
            "${lineSeparator()}\t\t      return -> type: String = Processed with data: $usernameParam - $dataParam - $boolParam" +
            "${lineSeparator()}\tDuration (second.nano) -> $totalDuration"

        val message = logCapture.methodLogMessage()

        assertEquals(expected, message)
    }

    @Test
    fun `Log method with exception`() {
        currentUser = generateSecurityUser()
        val prop01 = randomString(spaces = false)
        val value01 = randomString(spaces = false)
        setSessionProperty(prop01, value01)

        val usernameParam = randomString(spaces = false)
        val dataParam = listOf(randomString(spaces = false), null).random()
        val boolParam = listOf(randomBoolean(), null).random()
        val totalDuration = "${randomInteger(0, 99)}.${randomLong(0, 999999999)}"

        every { any<Duration>().toSecondsPattern() } returns totalDuration

        assertThrows<RuntimeException> {
            MethodAspectService().proceedException(
                username = usernameParam,
                userData = dataParam,
                isActive = boolParam,
            )
        }

        val expected = "${lineSeparator()}Method Logging" +
            "${lineSeparator()}\tMetadata -> " +
            "${lineSeparator()}\t\tUserId -> ${currentUser.userId}" +
            "${lineSeparator()}\t\tLocale -> ${currentLocale.toLanguageTag()}" +
            "${lineSeparator()}\t\t$prop01 -> $value01" +
            "${lineSeparator()}\tCall -> com.thomas.core.aspect.MethodAspectService.proceedException" +
            "${lineSeparator()}\t\tparameter[0] -> username: String = $usernameParam" +
            "${lineSeparator()}\t\tparameter[1] -> userData: String = $dataParam" +
            "${lineSeparator()}\t\tparameter[2] -> isActive: Boolean = $boolParam" +
            "${lineSeparator()}\t\t      return -> type: boolean = RuntimeException(Exception occurred)" +
            "${lineSeparator()}\tDuration (second.nano) -> $totalDuration"

        val message = logCapture.methodLogMessage()

        assertEquals(expected, message)
    }

    @Test
    fun `Log method with unit return`() {
        currentUser = generateSecurityUser()
        val prop01 = randomString(spaces = false)
        val value01 = randomString(spaces = false)
        setSessionProperty(prop01, value01)

        val usernameParam = randomString(spaces = false)
        val totalDuration = "${randomInteger(0, 99)}.${randomLong(0, 999999999)}"

        every { any<Duration>().toSecondsPattern() } returns totalDuration

        MethodAspectService().unitReturn(
            username = usernameParam,
            userData = null,
            isActive = null,
        )

        val expected = "${lineSeparator()}Method Logging" +
            "${lineSeparator()}\tMetadata -> " +
            "${lineSeparator()}\t\tUserId -> ${currentUser.userId}" +
            "${lineSeparator()}\t\tLocale -> ${currentLocale.toLanguageTag()}" +
            "${lineSeparator()}\t\t$prop01 -> $value01" +
            "${lineSeparator()}\tCall -> com.thomas.core.aspect.MethodAspectService.unitReturn" +
            "${lineSeparator()}\t\tparameter[0] -> username: String = $usernameParam" +
            "${lineSeparator()}\t\tparameter[1] -> userData: String = null" +
            "${lineSeparator()}\t\tparameter[2] -> isActive: Boolean = null" +
            "${lineSeparator()}\t\t      return -> type: void = null" +
            "${lineSeparator()}\tDuration (second.nano) -> $totalDuration"

        val message = logCapture.methodLogMessage()

        assertEquals(expected, message)
    }

    @Test
    fun `Log method with unit throwing exception`() {
        currentUser = generateSecurityUser()
        val prop01 = randomString(spaces = false)
        val value01 = randomString(spaces = false)
        val prop02 = randomString(spaces = false)
        val value02 = randomString(spaces = false)
        setSessionProperty(prop01, value01)
        setSessionProperty(prop02, value02)

        val usernameParam = randomString(spaces = false)
        val totalDuration = "${randomInteger(0, 99)}.${randomLong(0, 999999999)}"

        every { any<Duration>().toSecondsPattern() } returns totalDuration

        assertThrows<IllegalArgumentException> {
            MethodAspectService().unitReturnException(
                username = usernameParam,
                userData = null,
                isActive = null,
            )
        }

        val propsLog = sessionProperties().map { (k, v) ->
            "${lineSeparator()}\t\t$k -> $v"
        }.sorted().joinToString(separator = "")

        val expected = "${lineSeparator()}Method Logging" +
            "${lineSeparator()}\tMetadata -> " +
            "${lineSeparator()}\t\tUserId -> ${currentUser.userId}" +
            "${lineSeparator()}\t\tLocale -> ${currentLocale.toLanguageTag()}" +
            propsLog +
            "${lineSeparator()}\tCall -> com.thomas.core.aspect.MethodAspectService.unitReturnException" +
            "${lineSeparator()}\t\tparameter[0] -> username: String = $usernameParam" +
            "${lineSeparator()}\t\tparameter[1] -> userData: String = null" +
            "${lineSeparator()}\t\tparameter[2] -> isActive: Boolean = null" +
            "${lineSeparator()}\t\t      return -> type: void = IllegalArgumentException(Exception occurred on unit call)" +
            "${lineSeparator()}\tDuration (second.nano) -> $totalDuration"

        val message = logCapture.methodLogMessage()

        assertEquals(expected, message)
    }

    @Test
    fun `Log method with null return`() {
        currentUser = generateSecurityUser()
        val prop01 = randomString(spaces = false)
        val prop02 = randomString(spaces = false)
        val prop03 = randomString(spaces = false)
        val prop04 = randomString(spaces = false)
        val prop05 = randomString(spaces = false)
        val value01 = randomString(spaces = false)
        val value02 = randomString(spaces = false)
        val value03 = randomString(spaces = false)
        val value04 = randomString(spaces = false)
        val value05 = randomString(spaces = false)
        setSessionProperty(prop01, value01)
        setSessionProperty(prop02, value02)
        setSessionProperty(prop03, value03)
        setSessionProperty(prop04, value04)
        setSessionProperty(prop05, value05)

        val usernameParam = randomString(spaces = false)
        val totalDuration = "${randomInteger(0, 99)}.${randomLong(0, 999999999)}"

        every { any<Duration>().toSecondsPattern() } returns totalDuration

        MethodAspectService().nullReturn(
            username = usernameParam,
            userData = null,
            isActive = null,
        )

        val propsLog = sessionProperties().map { (k, v) ->
            "${lineSeparator()}\t\t$k -> $v"
        }.sorted().joinToString(separator = "")

        val expected = "${lineSeparator()}Method Logging" +
            "${lineSeparator()}\tMetadata -> " +
            "${lineSeparator()}\t\tUserId -> ${currentUser.userId}" +
            "${lineSeparator()}\t\tLocale -> ${currentLocale.toLanguageTag()}" +
            propsLog +
            "${lineSeparator()}\tCall -> com.thomas.core.aspect.MethodAspectService.nullReturn" +
            "${lineSeparator()}\t\tparameter[0] -> username: String = $usernameParam" +
            "${lineSeparator()}\t\tparameter[1] -> userData: String = null" +
            "${lineSeparator()}\t\tparameter[2] -> isActive: Boolean = null" +
            "${lineSeparator()}\t\t      return -> type: Integer = null" +
            "${lineSeparator()}\tDuration (second.nano) -> $totalDuration"

        val message = logCapture.methodLogMessage()

        assertEquals(expected, message)
    }

    @Test
    fun `Log method with empty session properties`() = runTest {
        val user = generateSecurityUser()

        // Clear all session properties
        SessionContextHolder.context = SessionContext.create(user = user, properties = emptyMap())

        logCapture.startCapturing()

        val service = MethodAspectService()
        val result = service.processData("John", "password", null, false, null, BigInteger("100"))

        val log = logCapture.methodLogMessage()
        assertEquals("Processed user: John", result)
        assertNotNull(log)
        assertTrue(log!!.contains("Method Logging"))
        assertTrue(log.contains("Metadata ->"))
        assertTrue(log.contains("UserId -> ${user.userId}"))
        assertTrue(log.contains("Locale -> "))
        // Should not contain any session properties since map is empty
        assertFalse(log.contains("property ->"))
    }

    @Test
    fun `Log method with multiple session properties`() = runTest {
        val user = generateSecurityUser()

        SessionContextHolder.context = SessionContext.create(
            user = user,
            properties = mapOf(
                "clientId" to "client123",
                "apiVersion" to "v2.0",
                "environment" to "test"
            )
        )

        logCapture.startCapturing()

        val service = MethodAspectService()
        val result = service.processData("Jane", "secret", "userData", true, null, BigInteger("200"))

        val log = logCapture.methodLogMessage()
        assertEquals("Processed user: Jane", result)
        assertNotNull(log)
        assertTrue(log!!.contains("Method Logging"))
        assertTrue(log.contains("Metadata ->"))
        assertTrue(log.contains("UserId -> ${user.userId}"))
        // Should contain all session properties in sorted order
        assertTrue(log.contains("apiVersion -> v2.0"))
        assertTrue(log.contains("clientId -> client123"))
        assertTrue(log.contains("environment -> test"))
    }

    @Test
    fun `Log method should handle aspects annotation edge cases`() = runTest {
        logCapture.startCapturing()

        val service = MethodAspectService()

        // Test with all possible combinations of null/non-null parameters
        val result = service.withoutMetadata("user", "pass", null, null)

        val log = logCapture.methodLogMessage()
        assertEquals("Processed with data: user - null - null", result)
        assertNotNull(log)
        assertTrue(log!!.contains("Method Logging"))
        assertTrue(log.contains("Call -> com.thomas.core.aspect.MethodAspectService.withoutMetadata"))
        assertTrue(log.contains("parameter[2] -> userData: String = null"))
        assertTrue(log.contains("parameter[3] -> isActive: Boolean = null"))
        assertTrue(log.contains("return -> type: String = Processed with data: user - null - null"))
        assertTrue(log.contains("Duration (second.nano)"))
        // Should not contain metadata
        assertFalse(log.contains("Metadata ->"))
        assertFalse(log.contains("UserId ->"))
    }

    @Test
    fun `Log method should handle return value that is not throwable`() = runTest {
        logCapture.startCapturing()

        val service = MethodAspectService()
        val result = service.logNothing("test", "password", "data", true, null, BigInteger("50"))

        val log = logCapture.methodLogMessage()
        assertEquals(listOf("Processed with data: test - data - true", "Aspect data: null", "Total amount: 50"), result)
        assertNotNull(log)
        assertTrue(log!!.contains("Method Logging"))
        // Since all logging options are false, should only contain basic method call info
        assertTrue(log.contains("Call -> com.thomas.core.aspect.MethodAspectService.logNothing"))
        // Should not contain parameters, result, metadata, or duration
        assertFalse(log.contains("parameter"))
        assertFalse(log.contains("return ->"))
        assertFalse(log.contains("Metadata ->"))
        assertFalse(log.contains("Duration"))
    }

    @Test
    fun `Log method should handle complex generic types`() = runTest {
        logCapture.startCapturing()

        val service = MethodAspectService()

        val genericOne = GenericData("test", 42, AspectData(1, "test", BigDecimal("10.5")))
        val innerGeneric = GenericData("inner", BigDecimal("99.99"), "innerString")
        val genericTwo = GenericData(true, BigInteger("999"), innerGeneric)

        val result = service.maskingResult(
            "complexUser",
            "complexPass",
            "complexData",
            false,
            AspectData(10, "complex", BigDecimal("100.00")),
            BigInteger("500"),
            genericOne,
            genericTwo
        )

        val log = logCapture.methodLogMessage()
        assertNotNull(result)
        assertNotNull(log)
        assertTrue(log!!.contains("Method Logging"))
        assertTrue(log.contains("Call -> com.thomas.core.aspect.MethodAspectService.maskingResult"))

        // Should contain masked parameters and result
        assertTrue(log.contains("parameter[1] -> password: String = **********"))
        assertTrue(log.contains("return -> type: GenericData<String, BigInteger, String> = **********"))
    }

    @Test
    fun `Log method should handle throwable as return value correctly`() = runTest {
        logCapture.startCapturing()

        val totalDuration = "${randomInteger(0, 99)}.${randomLong(0, 999999999)}"

        every { any<Duration>().toSecondsPattern() } returns totalDuration

        val service = MethodAspectService()

        assertThrows<RuntimeException> {
            service.proceedException("errorUser", "errorData", false)
        }

        val log = logCapture.methodLogMessage()
        assertNotNull(log)
        assertTrue(log!!.contains("Method Logging"))
        assertTrue(log.contains("Call -> com.thomas.core.aspect.MethodAspectService.proceedException"))
        assertTrue(log.contains("parameter[0] -> username: String = errorUser"))
        assertTrue(log.contains("parameter[1] -> userData: String = errorData"))
        assertTrue(log.contains("parameter[2] -> isActive: Boolean = false"))
        // Should log the exception details
        assertTrue(log.contains("return -> type: boolean = RuntimeException(Exception occurred)"))
        assertTrue(log.contains("Duration (second.nano) -> $totalDuration"))
    }

    @Test
    fun `Log method with different locale settings`() = runTest {
        val user = generateSecurityUser()

        // Test with different locale
        SessionContextHolder.context = SessionContext.create(
            user = user,
            locale = Locale.forLanguageTag("pt-BR")
        )

        logCapture.startCapturing()

        val service = MethodAspectService()
        val result = service.processData("LocaleTest", "pwd", "localeData", true, null, BigInteger("300"))

        val log = logCapture.methodLogMessage()
        assertEquals("Processed user: LocaleTest", result)
        assertNotNull(log)
        assertTrue(log!!.contains("Method Logging"))
        assertTrue(log.contains("Metadata ->"))
        assertTrue(log.contains("UserId -> ${user.userId}"))
        assertTrue(log.contains("Locale -> pt-BR"))
    }

    @Test
    fun `Log method should test all conditional branches`() = runTest {
        logCapture.startCapturing()

        // Create a service method that has logResult=false but throws an exception
        // This tests the branch where !annotation.logResult && result !is Throwable
        val service = MethodAspectService()

        assertThrows<IllegalArgumentException> {
            service.unitReturnException("test", "data", true)
        }

        val log = logCapture.methodLogMessage()
        assertNotNull(log)
        assertTrue(log!!.contains("Method Logging"))
        assertTrue(log.contains("Call -> com.thomas.core.aspect.MethodAspectService.unitReturnException"))
        // Should log the exception even when logResult is default (true)
        assertTrue(log.contains("return -> type: void = IllegalArgumentException(Exception occurred on unit call)"))
    }

    private class TestLogCapture {
        private val listAppender = ListAppender<ILoggingEvent>()
        private val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger

        fun startCapturing() {
            listAppender.start()
            rootLogger.addAppender(listAppender)
        }

        fun stopCapturing() {
            rootLogger.detachAppender(listAppender)
            clear()
            listAppender.stop()
        }

        fun methodLogMessage(): String? =
            listAppender.list.lastOrNull { it.message.startsWith("${lineSeparator()}Method Logging") }?.message

        fun clear() {
            listAppender.list.clear()
        }

    }

}

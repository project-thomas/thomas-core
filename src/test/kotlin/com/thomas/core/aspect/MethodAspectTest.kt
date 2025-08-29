package com.thomas.core.aspect

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
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
import kotlin.time.Duration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

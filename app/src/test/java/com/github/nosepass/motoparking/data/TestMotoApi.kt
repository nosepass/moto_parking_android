package com.github.nosepass.motoparking.data

import com.github.nosepass.motoparking.data.MotoApi.LoginParameters
import com.github.nosepass.motoparking.di.DaggerTestComponent
import io.reactivex.observers.TestObserver
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Okio
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

/**
 * Test that I've set up Gson properly.
 */
class TestMotoApi {
    val mockWebServer = MockWebServer()
    @Inject
    lateinit var api: MotoApi

    @Before
    @Throws(Exception::class)
    fun setUpMocks() {
        mockWebServer.start()
        val url = mockWebServer.url("")

        val component = DaggerTestComponent.builder()
                .baseUrl(url.toString())
                .build()
        component.inject(this)
    }

    @Test
    @Throws(Exception::class)
    fun login() {
        mockWebServer.enqueue(MockResponse().setBody(loadFile("login.json")))
        val test = TestObserver<User>()
        api.login(LoginParameters("jdoe", "password", Build()))
                .subscribe(test)
        val request = mockWebServer.takeRequest(1, SECONDS)
        test.assertNoErrors()

        // test request json body
        val requestJson = JSONObject(request.body.readUtf8())
        assertTrue(requestJson.has("credentials"))
        assertTrue(requestJson.has("phone_info"))
        assertEquals("jdoe", requestJson.getJSONObject("credentials").getString("nickname"))
        assertEquals("password", requestJson.getJSONObject("credentials").getString("password"))
        assertEquals("aw heck", requestJson.getJSONObject("phone_info").getString("device_id"))
        assertEquals("model", requestJson.getJSONObject("phone_info").getString("model"))
        val buildJson = JSONObject(requestJson.getJSONObject("phone_info").getString("build_json"))
        assertEquals("model", buildJson.getString("MODEL"))

        // test response parse
        test.assertValue({ it.id == 1L && "jdoe" == it.nickname })
    }

    fun loadFile(file: String): String {
        return Okio.buffer(Okio.source(
                javaClass.classLoader.getResourceAsStream(file)
        )).readUtf8()
    }

    class Build {
        companion object {
            val MODEL = "model"
            val SERIAL = "aw heck"
        }
    }
}

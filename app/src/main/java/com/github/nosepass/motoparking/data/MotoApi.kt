package com.github.nosepass.motoparking.data


import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.*
import java.util.*

interface MotoApi {
    /**
     * Hit the login url to start a session.
     */
    @POST("/login.json")
    fun login(@Body params: LoginParameters): Single<User>

    /**
     * Retrieve a list of relevant parking spots from the server to store locally.
     */
    @GET("/parking_spots.json")
    fun getSpots(): Single<List<ParkingSpot>>

    /**
     * Create a spot
     */
    @POST("/parking_spots.json")
    fun create(@Body params: ParkingSpotBody): Single<ParkingSpot>

    /**
     * Update a spot
     */
    @PUT("/parking_spots/{id}.json")
    fun update(@Path("id") id: String, @Body params: ParkingSpotBody): Single<ParkingSpot>

    @DELETE("/parking_spots/{id}.json")
    fun delete(@Path("id") id: String): Single<JsonElement>

    /**
     * Update a user
     */
    @PUT("/users/{id}.json")
    fun update(@Path("id") id: Long, @Body params: ParkingSpotBody): Single<ParkingSpot>

    class LoginParameters(nickname: String, password: String, buildInfo: Any) {
        val credentials: Credentials
        val phoneInfo: PhoneInfo
        @Suppress("unused")
        val createdAt: Date = Date()
        init {
            credentials = Credentials(nickname, password)
            phoneInfo = PhoneInfo(buildInfo)
        }
    }

    @Suppress("unused")
    class Credentials(val nickname: String, val password: String)

    @Suppress("unused")
    class PhoneInfo(buildInfo: Any,
                    buildJsonObj: JsonObject = GsonBuilder()
                            // include static fields
                            .excludeFieldsWithModifiers()
                            .create().toJsonTree(buildInfo).asJsonObject,
                    val deviceId: String = buildJsonObj.get("SERIAL").asString,
                    val model: String = buildJsonObj.get("MODEL").asString,
                    val buildJson: String = buildJsonObj.toString()) {
    }

    /**
     * Wrapper to format json with a parking_spot root key that Rails likes.
     */
    class ParkingSpotBody(val parkingSpot: ParkingSpot)
}

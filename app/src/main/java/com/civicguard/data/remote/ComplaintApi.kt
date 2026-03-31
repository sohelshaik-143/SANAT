package com.civicguard.data.remote

import com.civicguard.data.remote.dto.ComplaintResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ComplaintApi {

    @GET("api/complaints/my")
    suspend fun getMyComplaints(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<Map<String, Any>> // Backend returns Page object

    @Multipart
    @POST("api/complaints")
    suspend fun submitComplaint(
        @Part("complaint") complaint: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ComplaintResponse>

    @GET("api/complaints/{id}")
    suspend fun getComplaint(@Path("id") id: String): Response<ComplaintResponse>
}

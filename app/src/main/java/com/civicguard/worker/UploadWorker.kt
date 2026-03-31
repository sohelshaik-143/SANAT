package com.civicguard.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.civicguard.data.remote.ComplaintApi
import com.civicguard.data.remote.dto.ComplaintRequest
import com.civicguard.util.FileUtil
import com.squareup.moshi.Moshi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val api: ComplaintApi,
    private val moshi: Moshi
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: return Result.failure()
        val description = inputData.getString("description") ?: return Result.failure()
        val category = inputData.getString("category") ?: return Result.failure()
        val imageUriString = inputData.getString("imageUri") ?: return Result.failure()
        val latitude = inputData.getDouble("latitude", 0.0)
        val longitude = inputData.getDouble("longitude", 0.0)
        val pincode = inputData.getString("pincode") ?: ""
        val city = inputData.getString("city") ?: ""
        val state = inputData.getString("state") ?: ""

        return try {
            val imageUri = Uri.parse(imageUriString)
            val compressedFile = FileUtil.compressImage(applicationContext, imageUri)
            
            val request = ComplaintRequest(
                title = title,
                description = description,
                category = category,
                latitude = latitude,
                longitude = longitude,
                pincode = pincode,
                city = city,
                state = state
            )

            val json = moshi.adapter(ComplaintRequest::class.java).toJson(request)
            val jsonBody = json.toRequestBody("application/json".toMediaTypeOrNull())
            
            val imageBody = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", compressedFile.name, imageBody)

            val response = api.submitComplaint(jsonBody, imagePart)
            
            if (response.isSuccessful) {
                Result.success(workDataOf("ticket" to response.body()?.ticketNumber))
            } else {
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}

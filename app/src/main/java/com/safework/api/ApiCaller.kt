package com.safework.api

import ApiService
import android.util.Log
import com.safework.models.IssueModel
import com.safework.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File

object ApiCaller {
    private val api = RetrofitManager.createService(serviceClass = ApiService::class.java)


    fun loginUser(
        email: String,
        password: String,
        callback: (Result<LoginResponse>) -> Unit)
    {
        val requestBody = User.LoginUserModel(
            email = email,
            password = password
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.loginRequest(requestBody)
                callback(Result.success(response))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }

    fun registerUser(
        username: String,
        email: String,
        password: String,
        callback: (Result<SingUpResponse>) -> Unit
    ) {
        val requestBody = User.SignUpUserModel(
            username = username,
            email = email,
            password = password
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.registerRequest(requestBody)
                withContext(Dispatchers.Main) {
                    callback(Result.success(response))
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    if (e.code() == 409) {
                        callback(Result.failure(Exception("Falha, EMAIL já está em uso.")))
                    }
                    else {
                        callback(Result.failure(Exception("Erro desconhecido")))
                    }
                }
            }
        }
    }

    fun getIssuesByUserId(
        userId: String,
        length: Int,
        callback: (Result<List<IssueModel>>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getIssuesByUserId(
                    userId = userId,
                    length = length
                )
                withContext(Dispatchers.Main) {
                    callback(Result.success(response))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(Result.failure(e))
                }
            }
        }
    }

    fun getIssues(
        length: Int,
        callback: (Result<List<IssueModel>>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getIssues(length = length)
                withContext(Dispatchers.Main) {
                    callback(Result.success(response.problemas))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(Result.failure(e))
                }
            }
        }
    }

    fun uploadImage(
        issueId: String,
        imageFile: File,
        callback: (Result<String>) -> Unit
    ) {
        val requestFile: RequestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart: MultipartBody.Part =
            MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

        val issueIdPart: RequestBody = issueId.toRequestBody("text/plain".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.uploadImage(issueIdPart, imagePart)
                withContext(Dispatchers.Main) {
                    callback(Result.success(response))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(Result.failure(e))
                    println("Erro ao fazer upload da imagem: ${e.localizedMessage}")
                    e.printStackTrace()
                }
            }
        }
    }

    fun registerIssue(
        issue: IssueModel,
        callback: (Result<RegisterIssueResponse>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.reisterIssueRequest(issue)
                withContext(Dispatchers.Main) {
                    callback(Result.success(response))
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                        callback(Result.failure(Exception("Erro desconhecido, ${e.message}")))
                }
            }
        }
    }

    fun deleteIssue(id: String, onResult: (Boolean) -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                api.deleteIssue(id)
                Log.d("ApiService", "Issue deleted successfully")
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Error deleting issue", e)
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun getUserRoleById(id: String, callback: (Result<RoleResponse>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val startTime = System.currentTimeMillis()
                val response = api.getUserRole(id)
                val endTime = System.currentTimeMillis()

                Log.d("UserRole", "Tempo de resposta: ${(endTime - startTime)}ms")

                withContext(Dispatchers.Main) {
                    callback(Result.success(response))
                }
            } catch (e: Exception) {
                Log.e("UserRole", "Erro ao buscar permissão", e)
                withContext(Dispatchers.Main) {
                    callback(Result.failure(e))
                }
            }
        }
    }
}
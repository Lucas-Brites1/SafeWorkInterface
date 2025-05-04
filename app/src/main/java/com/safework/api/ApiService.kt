
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import com.safework.api.LoginResponse
import com.safework.api.RegisterIssueResponse
import com.safework.api.SingUpResponse
import com.safework.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("usuarios")
    suspend fun getUsuarios(): List<User>

    @POST("usuarios/login")
    suspend fun loginRequest(@Body request: User.LoginUserModel): LoginResponse

    @POST("usuarios/registro")
    suspend fun registerRequest(@Body request: User.SignUpUserModel): SingUpResponse

    @GET("problemas/usuarios/{userId}")
    suspend fun getIssuesByUserId(
        @Path("userId") userId: String,
        @Query("length") length: Int
    ): List<IssueModel>

    @Multipart
    @POST("problemas/imagens/upload")
    suspend fun uploadImage(
        @Part("issueId") issueId: RequestBody,
        @Part image: MultipartBody.Part
    ): String

    @POST("problemas/registro")
    suspend fun reisterIssueRequest(@Body request: IssueModel): RegisterIssueResponse
}


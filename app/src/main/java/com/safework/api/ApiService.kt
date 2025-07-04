
import com.safework.api.IssuesResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import com.safework.api.LoginResponse
import com.safework.api.RegisterIssueResponse
import com.safework.api.RoleResponse
import com.safework.api.SingUpResponse
import com.safework.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {
    @POST("usuarios/login")
    suspend fun loginRequest(@Body request: User.LoginUserModel): LoginResponse

    @POST("usuarios/registro")
    suspend fun registerRequest(@Body request: User.SignUpUserModel): SingUpResponse

    @GET("/usuarios/busca/permissao/{id}")
    suspend fun getUserRole(@Path("id") id: String): RoleResponse

    @GET("problemas/usuarios/{userId}")
    suspend fun getIssuesByUserId(
        @Path("userId") userId: String,
        @Query("length") length: Int
    ): List<IssueModel>

    @GET("/problemas/busca/{issueId}")
    suspend fun getIssueById(
        @Path("issueId") issueId: String
    ): IssueModel

    @GET("problemas/imagens/{issueId}")
    suspend fun getImageBytesByIssueId(@Path("issueId") issueId: String): ResponseBody

    @GET("/problemas")
    suspend fun getIssues(
        @Query("length") length: Int
    ): IssuesResponse

    @Multipart
    @POST("problemas/imagens/upload")
    suspend fun uploadImage(
        @Part("issueId") issueId: RequestBody,
        @Part image: MultipartBody.Part
    ): String

    @POST("problemas/registro")
    suspend fun reisterIssueRequest(@Body request: IssueModel): RegisterIssueResponse

    @DELETE("problemas/{id}")
    suspend fun deleteIssue(@Path("id") id: String)

    @GET("problemas/busca/limite")
    suspend fun getIssuesByDateRange(
        @Query("start") startDate: String,
        @Query("end") endDate: String
    ): List<IssueModel>

}

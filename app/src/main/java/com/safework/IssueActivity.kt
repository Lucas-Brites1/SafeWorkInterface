package com.safework

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.material.textfield.TextInputEditText
import com.safework.api.ApiCaller
import com.safework.api.ApiCaller.registerIssue
import com.safework.api.ApiCaller.uploadImage

import com.safework.models.IssueLevel
import com.safework.models.IssueModel
import com.safework.models.IssueStatus
import com.safework.models.IssueUser
import com.safework.models.LocationModel
import com.safework.utils.MapsUtils
import com.safework.utils.ViewUtils
import com.safework.utils.onClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.bson.types.ObjectId
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class IssueActivity : AppCompatActivity(), OnMapReadyCallback {
    private val PERMISSION_REQUEST_CODE = 1001
    private val REQUEST_CODE_PICK_IMAGE = 1002

    private var selectedImageUri: Uri? = null // Para armazenar a URI da imagem selecionada


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.issues_layout)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? com.google.android.gms.maps.SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Configurar a barra de status transparente
        ViewUtils.transparentBar(this)
        ViewUtils.configSpinner(this)

        val activity = this@IssueActivity

        val uploadButton = findViewById<ImageButton>(R.id.uploadImageButton)
        uploadButton.setOnClickListener {
            verifyPermission(activity, PERMISSION_REQUEST_CODE)
        }

        val variables = ViewUtils.getIntentVariables(
            this,
            listOf("userId", "username", "email")
        )
        val userId = variables[0]["userId"] as String
        val username = variables[1]["username"] as String
        val email = variables[2]["email"] as String

        findViewById<Button>(R.id.btnSubmit).onClick {
            val titleInput = findViewById<TextInputEditText>(R.id.inputTitle)
            val descriptionInput = findViewById<TextInputEditText>(R.id.inputDescription)
            val commentsInput = findViewById<TextInputEditText>(R.id.inputComments)
            val urgencySpinner = findViewById<Spinner>(R.id.spinnerUrgency)



            MapsUtils.getUserLocation(
                context = this,
                onLocationFound = { userLoc ->
                    val title = titleInput.text.toString().trim()
                    val description = descriptionInput.text.toString().trim()
                    val comments = commentsInput.text.toString().trim()
                    var urgency = urgencySpinner.selectedItem.toString().uppercase()
                    if (urgency == "MÉDIO") {
                        urgency = "MEDIO"
                    }
                    val createdAt = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault()).format(Date())
                    val newIssueId = ObjectId().toHexString().toString()

                    val newIssue = IssueModel(
                        _id = newIssueId,
                        title = title,
                        description = description,
                        comments = if (comments.isEmpty()) null else comments,
                        level = IssueLevel.valueOf(urgency),
                        status = IssueStatus.PENDENTE,
                        user = IssueUser(
                            userId = userId,
                            username = username,
                            email = email
                        ),
                        mapLocal = LocationModel(
                            latitude = userLoc.latitude,
                            longitude = userLoc.longitude
                        ),
                        createdAt = createdAt
                    )

                    registerIssue(newIssue) { result ->
                        result.onSuccess { response ->
                            val issueId = newIssueId
                            ViewUtils.showNotification(this, "Reclamação registrada com sucesso!")
                            Log.d("DEBUG", "issueID: $issueId")
                            Log.d("DEBUG", "Uri: ${selectedImageUri.toString()}")

                            if (selectedImageUri != null) {
                                sendImageToAPI(selectedImageUri!!, issueId, this)
                            } else {
                                ViewUtils.delayThen(
                                    action = {
                                        ViewUtils.changeActivity<HomeActivity>(this, variables)
                                    }, ViewUtils.Seconds(q = 3)
                                )
                            }
                        }
                        result.onFailure { error ->
                            ViewUtils.showNotification(this, "Erro ao registrar: ${error.message}",
                                ViewUtils.NotificationType.ERROR)
                        }
                    }

                },
                onError = {
                    Toast.makeText(this, "Não foi possível obter a localização", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun verifyPermission(activity: Activity, permissionCode: Int) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Para Android 13 (SDK 33) ou superior
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_MEDIA_IMAGES)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), permissionCode
                )

            } else {
                openGallery(activity)
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Para versões acima de Android 6.0 (Marshmallow)
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), permissionCode
                )
            } else {
                openGallery(activity)
            }
        } else {
            // Para versões abaixo do Marshmallow
            openGallery(activity)
        }
    }

    // Função para abrir a galeria
    private fun openGallery(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    // Função para lidar com o resultado da permissão
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openGallery(this) // Se a permissão for concedida, abrir a galeria
            } else {
                Toast.makeText(this, "Permissão negada para acessar as imagens.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função para lidar com o resultado da seleção de imagem
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                this.selectedImageUri = selectedImageUri;
                previewImage(selectedImageUri)
            }
        }
    }

    // Função para exibir a imagem selecionada no preview
    private fun previewImage(imageUri: Uri) {
        val imageView = findViewById<ImageView>(R.id.imagePreview)
        imageView.setImageURI(imageUri)
        imageView.visibility = android.view.View.VISIBLE
    }

    private fun sendImageToAPI(imageUri: Uri, issueId: String, context: Context) {
        val file = File(getRealPathFromURI(imageUri))
        val activity = (context as Activity)

        uploadImage(issueId, file) { result ->
            CoroutineScope(Dispatchers.Main).launch {
                result.fold(
                    onSuccess = { response ->
                        ViewUtils.showNotification(context, "Imagem enviada com sucesso!")
                        val variables = ViewUtils.getIntentVariables(
                            activity,
                            listOf("userId", "username", "email")
                        )
                        ViewUtils.delayThen(
                            action = {
                                ViewUtils.changeActivity<HomeActivity>(context, variables)
                            }, ViewUtils.Seconds(q = 3)
                        )
                    },
                    onFailure = { error ->
                        ViewUtils.showNotification(context, "Erro ao tentar salvar imagem...",
                            ViewUtils.NotificationType.ERROR)
                        error.printStackTrace()
                    }
                )
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val path = cursor?.getString(columnIndex!!) ?: ""
        cursor?.close()
        return path
    }


    override fun onMapReady(googleMap: GoogleMap) {
        MapsUtils.handleMapReady(
            googleMap = googleMap,
            context = this,
            onLocationReady = {
                    userLocation ->
                MapsUtils.moveCamera(googleMap, userLocation)
                MapsUtils.addMarker(googleMap, userLocation, "Voce esta aqui!")
            },
            onError = {
                ViewUtils.showNotification(this, "Aconteceu um erro ao tentar carregar o mapa.",
                    ViewUtils.NotificationType.ERROR)
            }
        )
    }
}

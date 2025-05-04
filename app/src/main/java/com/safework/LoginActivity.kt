package com.safework

import ApiService
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.safework.api.ApiCaller
import com.safework.utils.ViewUtils
import com.safework.utils.onClick

//OnMapReadyCallback
class LoginActivity : AppCompatActivity() {
    //lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)
        ViewUtils.transparentBar(context = this)
        sendToSignUpActivity(context = this)

        val emailInput = findViewById<EditText>(R.id.editEmail)
        val senhaInput = findViewById<EditText>(R.id.editSenha)
        val loginBtn = findViewById<Button>(R.id.btnLogin)

        loginBtn.onClick{
            val email = emailInput.text.toString()
            val senha = senhaInput.text.toString()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                ApiCaller.loginUser(email, senha) { result ->
                    result.fold(
                        onSuccess = { loginResponse ->
                            var variables: MutableList<Map<String, Any>> = mutableListOf()

                            variables.addAll(
                                listOf(
                                    mapOf("userId" to loginResponse.infos.userId),
                                    mapOf("email" to loginResponse.infos.email),
                                    mapOf("username" to loginResponse.infos.username)
                                )
                            )

                            ViewUtils.changeActivity<HomeActivity>(
                                context = this,
                                variables = variables
                            )
                            Log.d("API_RESPONSE", loginResponse.infos.toString())
                            finish()
                        },
                        onFailure = { exception ->
                            runOnUiThread {
                                ViewUtils.showNotification(this,
                                    "Erro no login. Tente novamente!",
                                    type = ViewUtils.NotificationType.ERROR)
                            }
                        }
                    )
                }
            } else {
                ViewUtils.showNotification(this, "Por favor, preencha todos os campos!")
            }
        }
    }


    /*override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        MapsUtils.handleMapReady(
            googleMap = mMap,
            context = this,
            onLocationReady = { latLng ->
                MapsUtils.addMarker(mMap, latLng, "Você está aqui!")
                MapsUtils.moveCamera(mMap, latLng)
            },
            onError = {
                ViewUtils.showNotification(this, "Erro ao obter localização.")
            }
        )
    }
    */
}

internal fun sendToSignUpActivity(context: Context) {
    (context as Activity).findViewById<TextView>(R.id.linkCriarConta).onClick {
        ViewUtils.changeActivity<SignUpActivity>(
            context = context
        )
    }
}
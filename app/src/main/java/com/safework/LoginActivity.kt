package com.safework

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.safework.api.ApiCaller
import com.safework.utils.ViewUtils
import com.safework.utils.onClick

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        ViewUtils.transparentBar(context = this)
        sendToSignUpActivity(context = this)

        val emailInput = findViewById<EditText>(R.id.editEmail)
        val senhaInput = findViewById<EditText>(R.id.editSenha)
        val loginBtn = findViewById<Button>(R.id.btnLogin)

        loginBtn.onClick {
            val email = emailInput.text.toString()
            val senha = senhaInput.text.toString()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                ApiCaller.loginUser(email, senha) { result ->
                    result.fold(
                        onSuccess = { loginResponse ->
                            val userId = loginResponse.infos.userId
                            val email = loginResponse.infos.email
                            val username = loginResponse.infos.username

                            val variables = mutableListOf(
                                mapOf("userId" to userId),
                                mapOf("email" to email),
                                mapOf("username" to username)
                            )

                            // MOVEMOS ESSA LÓGICA PARA DENTRO DO CALLBACK
                            ApiCaller.getUserRoleById(userId) { roleResult ->
                                roleResult.fold(
                                    onSuccess = { role ->
                                        Log.d("UserRole", "Permissão: ${role.role}")

                                        if (role.role == "USER") {
                                            ViewUtils.changeActivity<HomeActivity>(
                                                context = this,
                                                variables = variables
                                            )
                                        } else {
                                            ViewUtils.changeActivity<AdminActivity>(
                                                context = this,
                                                variables = variables
                                            )
                                        }

                                        Log.d("API_RESPONSE", loginResponse.infos.toString())
                                        finish()
                                    },
                                    onFailure = { error ->
                                        Log.e("UserRole", "Erro ao buscar permissão", error)
                                        runOnUiThread {
                                            ViewUtils.showNotification(
                                                this,
                                                "Erro ao verificar permissão do usuário.",
                                                type = ViewUtils.NotificationType.ERROR
                                            )
                                        }
                                    }
                                )
                            }
                        },
                        onFailure = { exception ->
                            runOnUiThread {
                                ViewUtils.showNotification(
                                    this,
                                    "Erro no login. Tente novamente!",
                                    type = ViewUtils.NotificationType.ERROR
                                )
                            }
                        }
                    )
                }
            } else {
                ViewUtils.showNotification(this, "Por favor, preencha todos os campos!")
            }
        }
    }
}

internal fun sendToSignUpActivity(context: Context) {
    (context as Activity).findViewById<TextView>(R.id.linkCriarConta).onClick {
        ViewUtils.changeActivity<SignUpActivity>(context = context)
    }
}

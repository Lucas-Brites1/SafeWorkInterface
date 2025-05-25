package com.safework

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.safework.api.ApiCaller
import com.safework.models.User
import com.safework.utils.ViewUtils
import com.safework.utils.onClick
import kotlinx.coroutines.time.delay
import kotlin.time.Duration.Companion.seconds

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_layout)
        ViewUtils.transparentBar(context = this)
        sendBackToLogin(context = this);

        this.findViewById<Button>(R.id.btnCadastrar).onClick {
            val firstName = this.findViewById<EditText>(R.id.editNome).text.toString()
            val secondName = this.findViewById<EditText>(R.id.editSobrenome).text.toString()
            val email = this.findViewById<EditText>(R.id.editEmail).text.toString()
            val password = this.findViewById<EditText>(R.id.editSenha).text.toString()
            val name = "$firstName $secondName"

            val validatingNewUser = UserValidator.isValid(user = User(
                username = name,
                email = email,
                password = password
            ))

            if(!validatingNewUser.success) {
                ViewUtils.showNotification(this, validatingNewUser.message)
            }

            ApiCaller.registerUser(
                username = name,
                email = email,
                password = password
            ) { result ->
                runOnUiThread {
                    result.fold(
                        onSuccess = { registerResponse ->
                            ViewUtils.clearInputs(
                                this@SignUpActivity.findViewById<EditText>(R.id.editNome),
                                this@SignUpActivity.findViewById<EditText>(R.id.editSobrenome),
                                this@SignUpActivity.findViewById<EditText>(R.id.editEmail),
                                this@SignUpActivity.findViewById<EditText>(R.id.editSenha)
                            )
                            ViewUtils.showNotification(this@SignUpActivity, registerResponse.mensagem!!)
                            ViewUtils.delayThen(
                                { ViewUtils.changeActivity<LoginActivity>(context = this)}
                            , ViewUtils.Seconds(3))
                        },
                        onFailure = { exception ->
                            ViewUtils.showNotification(this, exception.message.toString(), ViewUtils.NotificationType.ERROR)
                        }
                    )
                }
            }
        }
    }

    internal fun sendBackToLogin(context: Context) {
        (context as Activity).findViewById<TextView>(R.id.linkLogin).onClick {
            ViewUtils.changeActivity<LoginActivity>(
                context = context
            )
        }
    }
}

object UserValidator {

    class Response(val success: Boolean, val message: String)

    fun isValid(user: User): Response {
        val names = user.username.split(" ")

        val firstName = names.getOrNull(0) ?: ""
        val secondName = names.getOrNull(1) ?: ""

        return when {
            firstName.isBlank() -> {
                Response(success = false, message = "Validação falhou: PRIMEIRO NOME está vazio")
            }
            secondName.isBlank() -> {
                Response(success = false, message = "Validação falhou: SOBRENOME está vazio")
            }
            user.email.isBlank() -> {
                Response(success = false, message = "Validação falhou: EMAIL está vazio")
            }
            !user.email.contains("@") || !user.email.contains(".") -> {
                Response(success = false, message = "Validação falhou: EMAIL deve conter '@' e '.'")
            }
            user.password.isBlank() -> {
                Response(success = false, message = "Validação falhou: PASSWORD está vazia")
            }
            user.password.length < 6 -> {
                Response(success = false, message = "Validação falhou: PASSWORD deve ter pelo menos 6 caracteres")
            }
            else -> Response(success = true, message = "Usuário válido")
        }
    }


}



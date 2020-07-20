package com.example.drone.ui.authentication

import android.os.Bundle
import android.view.View
import org.json.JSONObject
import com.example.drone.R
import android.widget.Toast
import android.content.Intent
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import androidx.appcompat.app.AppCompatActivity
import com.example.drone.ui.session.LiveActivity
import com.example.drone.data.manager.UserManager
import com.android.volley.toolbox.JsonObjectRequest
import com.example.drone.data.AppConfig.Companion.API_URL
import com.example.drone.data.api.factory.UserFactory
import com.example.drone.data.api.factory.VolleyFactory
import kotlinx.android.synthetic.main.activity_registration.*

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        setupSignUpButton()
        setupLoginTextView()
    }

    private fun updateSignUpForm(enable: Boolean) {
        val visibility = if (enable) View.INVISIBLE else View.VISIBLE
        button_sign_up.isEnabled = enable
        edit_text_email.isEnabled = enable
        edit_text_pseudo.isEnabled = enable
        edit_text_password.isEnabled = enable
        progress_bar_sign_up.visibility = visibility
    }
    private fun setupSignUpButton() {
        val url = "${API_URL}/user/cmd/sign-up"
        button_sign_up.setOnClickListener {
            val email = edit_text_email.text.toString().trim()
            val pseudo = edit_text_pseudo.text.toString().trim()
            val password = edit_text_password.text.toString().trim()
            val map = HashMap<String,String>()
            map["email"] = email
            map["pseudo"] = pseudo
            map["password"] = password
            if (email.isEmpty()) {
                edit_text_email.error = "Email doit être rempli."
                edit_text_email.requestFocus()
                return@setOnClickListener
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edit_text_email.error = "Email ne respecte pas le bon format."
                edit_text_email.requestFocus()
                return@setOnClickListener
            }
            if (pseudo.isEmpty()) {
                edit_text_pseudo.error = "Le pseudo doit être rempli."
                edit_text_pseudo.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                edit_text_password.error = "Le mot de passe doit être rempli."
                edit_text_password.requestFocus()
                return@setOnClickListener
            }
            updateSignUpForm(false)
            val parameters = JSONObject(map as Map<String, String>)
            val volleyRequest = Volley.newRequestQueue(this)
            val jsonObjectRequest = object : JsonObjectRequest(Method.POST, url, parameters, Response.Listener { jsonResponse ->
                updateSignUpForm(true)
                val user = UserFactory.mapFromJSON(jsonResponse)
                Toast.makeText(this, "Bonjour ${user.pseudo} !", Toast.LENGTH_LONG).show()
                UserManager.login(user)
                val intent = Intent(this, LiveActivity::class.java)
                startActivity(intent)
            }, Response.ErrorListener { error ->
                updateSignUpForm(true)
                val message = VolleyFactory.getMessage(error)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }) {
                override fun getBodyContentType(): String {
                    return "application/json"
                }
            }
            volleyRequest.add(jsonObjectRequest)
        }

    }
    private fun setupLoginTextView() {
        text_view_login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}

package com.example.drone.ui.authentication

import android.widget.*
import android.os.Bundle
import android.view.View
import org.json.JSONObject
import com.example.drone.R
import android.content.Intent
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import androidx.appcompat.app.AppCompatActivity
import com.example.drone.ui.session.LiveActivity
import com.example.drone.data.manager.UserManager
import com.android.volley.toolbox.JsonObjectRequest
import com.example.drone.data.AppConfig.Companion.API_URL
import com.example.drone.data.api.factory.UserFactory
import kotlinx.android.synthetic.main.activity_login.*
import com.example.drone.data.api.factory.VolleyFactory

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //val intent = Intent(this, LiveActivity::class.java)
        //startActivity(intent)

        setupLoginButton()
        setupSignUpTextView()
    }

    private fun updateLoginForm(enable: Boolean) {
        val visibility = if (enable) View.INVISIBLE else View.VISIBLE
        button_login.isEnabled = enable
        edit_text_pseudo.isEnabled = enable
        edit_text_password.isEnabled = enable
        progress_bar_login.visibility = visibility
    }
    private fun setupLoginButton() {
        val url = "${API_URL}/user/cmd/sign-in"
        button_login.setOnClickListener {
            val pseudo = edit_text_pseudo.text.toString().trim()
            val password = edit_text_password.text.toString().trim()
            val map = HashMap<String,String>()
            map["pseudo"] = pseudo
            map["password"] = password
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
            updateLoginForm(false)
            val parameters = JSONObject(map as Map<String, String>)
            val volleyRequest = Volley.newRequestQueue(this)
            val jsonObjectRequest = object : JsonObjectRequest(Method.POST, url, parameters, Response.Listener { jsonResponse ->
                updateLoginForm(true)
                val user = UserFactory.mapFromJSON(jsonResponse)
                Toast.makeText(this, "Bonjour ${user.pseudo} !", Toast.LENGTH_LONG).show()
                UserManager.login(user)
                val intent = Intent(this, LiveActivity::class.java)
                startActivity(intent)
            }, Response.ErrorListener { error ->
                updateLoginForm(true)
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
    private fun setupSignUpTextView() {
        text_view_sign_up.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }
}

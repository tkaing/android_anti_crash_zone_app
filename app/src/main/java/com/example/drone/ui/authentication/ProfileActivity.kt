package com.example.drone.ui.authentication

import android.os.Bundle
import android.view.View
import com.example.drone.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.drone.ui.session.LiveActivity
import com.example.drone.data.storage.UserStorage
import com.example.drone.ui.session.HistoryActivity
import com.example.drone.data.storage.SessionStorage
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.text_view_live
import kotlinx.android.synthetic.main.activity_profile.text_view_title

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setupSessionButton()
        setupHistoryButton()
        setupTitleTextView()
        updateLiveTextView()
    }

    private fun setupTitleTextView()
    {
        text_view_title.text = "${UserStorage.loggedUser.pseudo}  🚁️"
        text_view_title.setOnClickListener {
            UserStorage.loggedUser = UserStorage.defaultUser
            SessionStorage.currentSession = SessionStorage.defaultSession
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateLiveTextView()
    {
        if (SessionStorage.isActive()) {
            text_view_live.visibility = View.VISIBLE
            text_view_live.setOnClickListener {
                val intent = Intent(this, LiveActivity::class.java)
                startActivity(intent)
            }
        } else {
            text_view_live.visibility = View.INVISIBLE
        }
    }

    private fun setupSessionButton() {
        if (SessionStorage.isActive()) {
            button_session.text = "En cours  👉"
        }
        button_session.setOnClickListener {
            val intent = Intent(this, LiveActivity::class.java)
            startActivity(intent)
        }
    }
    private fun setupHistoryButton() {
        button_history.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }
}
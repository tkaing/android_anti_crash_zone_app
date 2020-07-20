package com.example.drone.ui.session

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.drone.R
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.drone.data.AppConfig.Companion.API_URL
import com.example.drone.data.api.factory.SessionFactory
import com.example.drone.data.api.factory.VolleyFactory
import com.example.drone.data.api.model.Session
import com.example.drone.data.storage.SessionStorage
import com.example.drone.data.storage.UserStorage
import com.example.drone.ui.authentication.ProfileActivity
import com.example.drone.ui.list.SessionAdapter
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.activity_live.text_view_live

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        setupRecyclerView()
        setupTitleTextView()
        updateLiveTextView()
    }

    private fun setupTitleTextView()
    {
        text_view_title.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
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


    private fun setupRecyclerView()
    {
        val sessionsArray: MutableList<Session> = mutableListOf()
        fetchSessions()
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = SessionAdapter(sessionsArray)
    }

    private fun fetchSessions()
    {
        val loggedUser = UserStorage.loggedUser
        val url = "${API_URL}/session/query/list/user/${loggedUser.id}"
        val volleyRequest = Volley.newRequestQueue(this)
        val jsonArrayRequest = object :
            JsonArrayRequest(Method.GET, url, null, Response.Listener { jsonResponse ->
                val sessionArray = SessionFactory.mapFromJSONArray(jsonResponse)
                recycler_view.adapter = SessionAdapter(sessionArray.asReversed())
            }, Response.ErrorListener { error ->
                val message = VolleyFactory.getMessage(error)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }) {
            override fun getBodyContentType(): String {
                return "application/json"
            }
        }
        volleyRequest.add(jsonArrayRequest)
    }
}

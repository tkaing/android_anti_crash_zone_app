package com.example.drone.ui.list

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.drone.R
import com.example.drone.data.api.model.Session
import com.example.drone.ui.authentication.ProfileActivity
import com.example.drone.ui.session.HistoryActivity
import com.example.drone.ui.session.SessionActivity
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.cell_session.*


class SessionAdapter(val items: MutableList<Session>) : RecyclerView.Adapter<SessionAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view), LayoutContainer {

        override val containerView: View?
            get() = itemView

        fun bindSession(session: Session) {
            text_view_id.text = "Session N°${session.id}"
            text_view_date.text = session.date
            text_view_time.text = session.time
            this.itemView.setOnClickListener {
                val intent = Intent(this.itemView.context, SessionActivity::class.java)
                intent.putExtra("sessionId", session.id)
                startActivity(this.itemView.context, intent, null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.cell_session))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindSession(items[position])
    }
}
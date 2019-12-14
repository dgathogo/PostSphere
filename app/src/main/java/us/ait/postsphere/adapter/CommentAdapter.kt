package us.ait.postsphere.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.comment_row.view.*
import us.ait.postsphere.R
import us.ait.postsphere.data.Comment

class CommentAdapter(
    private val uid: String,
    private val comments: List<Comment>
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    @SuppressLint("WrongConstant")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]
        holder.tvAuthor.text = comment.commentAuthor
        holder.tvBody.text = comment.commentBody
        val childLayoutManager = LinearLayoutManager(holder.rvComments.context, LinearLayout.VERTICAL, false)

        holder.rvComments.apply {
            layoutManager = childLayoutManager
            adapter = CommentAdapter(uid, comment.comments)
            setRecycledViewPool(viewPool)
        }
        if (comment.commentId == uid) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
//                removeComment(holder.adapterPosition)
            }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAuthor: TextView = itemView.tvAuthor
        val tvBody: TextView = itemView.tvBody
        val btnDelete: Button = itemView.btnDelete
        val rvComments: RecyclerView = itemView.rvComments
    }


}
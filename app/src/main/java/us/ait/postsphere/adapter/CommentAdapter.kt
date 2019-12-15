package us.ait.postsphere.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.comment_row.view.*
import us.ait.postsphere.ForumActivity
import us.ait.postsphere.R
import us.ait.postsphere.data.Comment
import us.ait.postsphere.data.Post

class CommentAdapter(
    private val uid: String
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private var commentsList = mutableListOf<Comment>()
    private var commentKeys = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = commentsList[position]
        holder.tvAuthor.text = comment.author
        holder.tvBody.text = comment.text

        if (comment.uid == uid) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                removeComment(holder.adapterPosition)
            }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
    }

    fun addComment(comment: Comment, key: String) {
        commentsList.add(comment)
        commentKeys.add(key)
        notifyDataSetChanged()
    }

    private fun removeComment(position: Int) {
        FirebaseFirestore.getInstance().collection("comments").document(
            commentKeys[position]
        ).delete()
        commentsList.removeAt(position)
        commentKeys.removeAt(position)
    }

    fun removeCommentByKey(key: String) {
        val index = commentKeys.indexOf(key)
        if (index != -1) {
            commentsList.removeAt(index)
            commentKeys.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAuthor: TextView = itemView.tvAuthor
        val tvBody: TextView = itemView.tvBody
        val btnDelete: ImageButton = itemView.btnDelete
    }


}
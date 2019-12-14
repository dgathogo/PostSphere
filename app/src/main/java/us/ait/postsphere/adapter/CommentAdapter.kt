package us.ait.postsphere.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.comment_row.view.*
import us.ait.postsphere.ForumActivity
import us.ait.postsphere.R
import us.ait.postsphere.data.Comment
import us.ait.postsphere.data.Post

class CommentAdapter(
    private val context: Context,
    private val uid: String,
    private val parentPost: Post?,
    private val parentComment: Comment?
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private var commentsList = mutableListOf<Comment>()
    private var commentKeys = mutableListOf<String>()
    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = commentsList[position]
        holder.tvAuthor.text = comment.commentAuthor
        holder.tvBody.text = comment.commentBody
        val childLayoutManager = LinearLayoutManager(
            holder.rvComments.context,
            RecyclerView.VERTICAL, false
        )
        val childAdapter = CommentAdapter(context, uid, null, comment)
        childAdapter.addAll(comment.comments)

        holder.rvComments.apply {
            layoutManager = childLayoutManager
            adapter = childAdapter
            setRecycledViewPool(viewPool)
        }

        if (comment.commentId == uid) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                removeComment(holder.adapterPosition)
            }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
        holder.btnComment.setOnClickListener {
            (context as ForumActivity).showCommentDialog()
        }
    }

    fun addAll(comments: List<Comment>) {
        commentsList.addAll(comments)

        for (item in commentsList) {
            commentKeys.add(item.commentId)
        }
    }

    fun addComment(comment: Comment, key: String) {
        commentsList.add(comment)
        commentKeys.add(key)
    }

    fun removeComment(position: Int) {
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
        val btnDelete: Button = itemView.btnDelete
        val btnComment: Button = itemView.btnComment
        val rvComments: RecyclerView = itemView.rvComments
    }


}
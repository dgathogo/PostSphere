package us.ait.postsphere.adapter

//import com.bumptech.glide.Glide
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.post_row.view.*
import us.ait.postsphere.R
import us.ait.postsphere.data.Comment

class CommentAdapter(
    private val context: Context,
    private val uid: String
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private var commentsList = mutableListOf<Comment>()
    private var commentsKeys = mutableListOf<String>()

    private var lastIndex = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_row, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int = commentsList.size

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastIndex) {
            val animation = AnimationUtils.loadAnimation(
                context,
                android.R.anim.slide_in_left
            )
            viewToAnimate.startAnimation(animation)
            lastIndex = position
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = commentsList[position]
        holder.tvBody.text = comment.commentBody
        setAnimation(holder.itemView, position)


        // if this is my comment message
        if (comment.commentId == uid) {
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
        commentsKeys.add(key)
        notifyDataSetChanged()
    }

    private fun removeComment(index: Int) {
        FirebaseFirestore.getInstance().collection("comments").document(
            commentsKeys[index]
        ).delete()

        commentsList.removeAt(index)
        commentsKeys.removeAt(index)
        notifyItemRemoved(index)
    }


    fun removeCommentByKey(key: String) {
        val index = commentsKeys.indexOf(key)
        if (index != -1) {
            commentsList.removeAt(index)
            commentsKeys.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBody = itemView.tvBody
        val btnDelete = itemView.btnDelete
        val btnComment = itemView.btnComment
    }
}
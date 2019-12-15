package us.ait.postsphere

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.new_comment_dialog.view.*
import us.ait.postsphere.data.Comment

class CommentDialog : DialogFragment() {

    interface CommentHandler {
        fun commentCreated(item: Comment)

        fun commentUpdated(item: Comment)
    }

    private lateinit var commentHandler: CommentHandler

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is CommentHandler) {
            commentHandler = context
        } else {
            throw RuntimeException(
                getString(R.string.interface_not_implemented)
            )
        }
    }

    private lateinit var etCommentText: EditText

    var isEditMode: Boolean = true

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)

        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle(getString(R.string.new_comment))

        val rootView = requireActivity().layoutInflater.inflate(
            R.layout.new_comment_dialog, null
        )

        etCommentText = rootView.etComment2
        builder.setView(rootView)

        isEditMode = (arguments != null) && arguments!!.containsKey(ForumActivity.KEY_COMMENT)

        if (isEditMode) {
            builder.setTitle(getString(R.string.edit_comment))
            var comment = arguments?.getSerializable(ForumActivity.KEY_COMMENT) as Comment

            etCommentText.setText(comment.text)
        }

        builder.setPositiveButton(getString(R.string.save)) { _, _ -> }

        return builder.create()
    }

    override fun onResume() {
        super.onResume()

        val positiveButton = (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            if (etCommentText.text.isNotEmpty()) {
                if (isEditMode) {
                    handleCommentEdit()
                } else {
                    handleCommentCreate()
                }

                (dialog as AlertDialog).dismiss()
            } else {
                etCommentText.error = getString(R.string.error_empty_field)
            }
        }
    }

    private fun handleCommentEdit() {
        val commentToEdit = arguments?.getSerializable(
            ForumActivity.KEY_COMMENT
        ) as Comment
        commentToEdit.text = etCommentText.text.toString()

        commentHandler.commentUpdated(commentToEdit)
    }

    private fun handleCommentCreate() {
        commentHandler.commentCreated(
            Comment(
                FirebaseAuth.getInstance().currentUser!!.uid,
                FirebaseAuth.getInstance().currentUser!!.displayName!!,
                etCommentText.text.toString()
            )
        )
    }
}
package us.ait.postsphere

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.new_comment_dialog.view.*
import us.ait.postsphere.data.Comment

class CommentDialog : DialogFragment() {


    private lateinit var etCommentText: EditText

    private var isEditMode: Boolean = false

    private var key: String? = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)

        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle(getString(R.string.new_comment))

        val rootView = requireActivity().layoutInflater.inflate(
            R.layout.new_comment_dialog, null
        )

        etCommentText = rootView.etComment
        builder.setView(rootView)

        isEditMode = (arguments != null) && arguments!!.containsKey(ForumActivity.KEY_COMMENT)

        if (isEditMode) {
            builder.setTitle(getString(R.string.edit_comment))
            var comment = arguments?.getSerializable(ForumActivity.KEY_COMMENT) as Comment
            key = arguments?.getString(ForumActivity.KEY_KEY)
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
                    (context as PostDetailActivity).updateComment(
                        etCommentText.text.toString(),
                        key!!
                    )

                } else {
                    (context as PostDetailActivity).postComment(etCommentText.text.toString())
                }
                (dialog as AlertDialog).dismiss()
            } else {
                etCommentText.error = getString(R.string.error_empty_field)
            }
        }
    }
}
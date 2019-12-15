package us.ait.postsphere.data

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class Comment(
    var uid: String = "",
    var author: String = "",
    var text: String = ""
) :Serializable
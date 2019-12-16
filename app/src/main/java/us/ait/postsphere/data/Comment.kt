package us.ait.postsphere.data

import java.io.Serializable

data class Comment(
    var uid: String = "",
    var author: String = "",
    var text: String = ""
) : Serializable
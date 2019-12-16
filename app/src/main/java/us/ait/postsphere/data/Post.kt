package us.ait.postsphere.data

import java.io.Serializable

data class Post(
    var uid: String = "",
    var author: String = "",
    var title: String = "",
    var body: String = "",
    var imgUrl: String = "",
    var comments: MutableList<Comment> = ArrayList()
) : Serializable



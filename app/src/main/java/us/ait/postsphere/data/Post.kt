package us.ait.postsphere.data

data class Post(
    var postId: String = "",
    var postAuthor: String = "",
    var postTitle: String = "",
    var postBody: String = "",
    var imgUrl: String = ""
//    var postComments: MutableList<Comment>
)


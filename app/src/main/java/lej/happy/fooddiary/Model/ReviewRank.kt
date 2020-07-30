package lej.happy.fooddiary.Model

import lej.happy.fooddiary.DB.Entity.Post

data class ReviewRank (
    var post: Post,
    var num: Int = 0,
    var best: Int = 0,
    var good: Int = 0,
    var bad: Int = 0

)

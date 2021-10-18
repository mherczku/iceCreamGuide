package hu.hm.icguide.ui.detail

import hu.hm.icguide.models.Comment
import hu.hm.icguide.models.Shop

data class DetailViewState(
    val comments: MutableList<Comment> = mutableListOf(),
    val shop : Shop = Shop()
)

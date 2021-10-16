package hu.hm.icguide.ui.detail

import hu.hm.icguide.models.Comment

data class DetailViewState(
    val comments: MutableList<Comment> = mutableListOf()
)

package com.vbank.vidyovideoview.model


data class TokenParams (val customerName : String?, val deviceToken : String?,val customerKeyNb : Int?=0,
                        val branchKeyNb : Int? = 0,val bankerKeyNb: Int?=0)

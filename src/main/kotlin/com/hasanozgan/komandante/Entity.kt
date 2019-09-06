package com.hasanozgan.komandante

import java.util.*

typealias EntityID = UUID

interface Entity {
    fun EntityID(): EntityID
}
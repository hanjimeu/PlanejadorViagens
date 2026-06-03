package com.example.ponytravelplanner2.db

data class Post(
    val id: Int,
    val texto: String,
    val usuarioId: Int = 0,
    val viagemId: Int? = null
)
package com.autofix.logserviceapp

data class ChatMessage(
    val message: String,
    val isUser: Boolean // true = pesan user, false = pesan bot
)
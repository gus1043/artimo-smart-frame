package com.example.artimo_smart_frame

data class SmartThingsCommand(
    val commands: List<Command>
)

data class Command(
    val component: String,
    val capability: String,
    val command: String,
    val arguments: List<Any>?
)


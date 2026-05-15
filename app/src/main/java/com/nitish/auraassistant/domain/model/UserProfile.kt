package com.nitish.auraassistant.domain.model

data class UserProfile (
    val id: Int = 1 ,
    val name: String = "" ,
    val age: String = "" ,
    val phone: String = "" ,
    val personalityTraits: List<String> = emptyList() ,
    val isOnboardingComplete : Boolean = false

)


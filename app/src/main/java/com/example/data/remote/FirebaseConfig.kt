package com.example.data.remote

object FirebaseConfig {
    const val API_KEY = "AIzaSyCso9amrk2TA2dqe-rNC-pI1655BOCMe6U"
    const val PROJECT_ID = "giveaway-5569c"
    const val AUTH_DOMAIN = "giveaway-5569c.firebaseapp.com"
    const val DATABASE_URL = "https://giveaway-5569c-default-rtdb.firebaseio.com"
    const val STORAGE_BUCKET = "giveaway-5569c.firebasestorage.app"

    const val AUTH_SIGN_UP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$API_KEY"
    const val AUTH_SIGN_IN_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$API_KEY"
    const val AUTH_RESET_PASSWORD_URL = "https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=$API_KEY"
}

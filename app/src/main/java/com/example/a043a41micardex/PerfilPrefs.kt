package com.example.a043a41micardex

import android.content.Context

class PerfilPrefs(context: Context) {

    private val prefs = context.getSharedPreferences("perfil", Context.MODE_PRIVATE)

    init {
        // Datos permanentes: siempre se dejan guardados los valores de Alumno.kt
        prefs.edit()
            .putString("nombre", Alumno.NOMBRE)
            .putString("numero_control", Alumno.NUMERO_CONTROL)
            .apply()
    }

    val nombre: String
        get() = prefs.getString("nombre", Alumno.NOMBRE) ?: Alumno.NOMBRE

    val numeroControl: String
        get() = prefs.getString("numero_control", Alumno.NUMERO_CONTROL) ?: Alumno.NUMERO_CONTROL
}
package com.example.a043a41micardex

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materias")
data class Materia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val semestre: Int,
    val calNormal: Double? = null,
    val calRepeticion: Double? = null,
    val calEspecial: Double? = null
) {
    // Calificación que cuenta: la última oportunidad capturada
    val calFinal: Double?
        get() = calEspecial ?: calRepeticion ?: calNormal
}

const val CALIFICACION_MINIMA = 70.0
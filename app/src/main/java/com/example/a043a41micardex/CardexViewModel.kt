package com.example.a043a41micardex

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CardexViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).materiaDao()
    private val prefs = PerfilPrefs(app)

    val nombreAlumno: String = prefs.nombre
    val numeroControl: String = prefs.numeroControl

    val materias = dao.observarTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val promedioGeneral = dao.promedioGeneral()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val promediosSemestre = dao.promediosPorSemestre()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun normalizar(cal: Double?): Double? =
        cal?.let { if (it < CALIFICACION_MINIMA) 0.0 else it }

    fun guardarMateria(materia: Materia) {
        val ajustada = materia.copy(
            calNormal = normalizar(materia.calNormal),
            calRepeticion = normalizar(materia.calRepeticion),
            calEspecial = normalizar(materia.calEspecial)
        )
        viewModelScope.launch {
            if (ajustada.id == 0) dao.insertar(ajustada) else dao.actualizar(ajustada)
        }
    }

    fun eliminarMateria(materia: Materia) {
        viewModelScope.launch { dao.eliminar(materia) }
    }
}
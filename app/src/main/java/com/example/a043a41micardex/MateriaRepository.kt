package com.example.a043a41micardex

import kotlinx.coroutines.flow.Flow

class MateriaRepository(private val dao: MateriaDao) {

    val materias: Flow<List<Materia>> = dao.observarTodas()
    val promedioGeneral: Flow<Double?> = dao.promedioGeneral()
    val promediosSemestre: Flow<List<PromedioSemestre>> = dao.promediosPorSemestre()

    suspend fun insertar(materia: Materia) = dao.insertar(materia)
    suspend fun actualizar(materia: Materia) = dao.actualizar(materia)
    suspend fun eliminar(materia: Materia) = dao.eliminar(materia)
}
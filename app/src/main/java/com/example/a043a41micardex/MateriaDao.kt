package com.example.a043a41micardex

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class PromedioSemestre(val semestre: Int, val promedio: Double?)

@Dao
interface MateriaDao {

    @Query("SELECT * FROM materias ORDER BY semestre, nombre")
    fun observarTodas(): Flow<List<Materia>>

    @Query("SELECT AVG(COALESCE(calEspecial, calRepeticion, calNormal)) FROM materias")
    fun promedioGeneral(): Flow<Double?>

    @Query(
        """
        SELECT semestre, AVG(COALESCE(calEspecial, calRepeticion, calNormal)) AS promedio
        FROM materias
        GROUP BY semestre
        ORDER BY semestre
        """
    )
    fun promediosPorSemestre(): Flow<List<PromedioSemestre>>

    @Insert
    suspend fun insertar(materia: Materia)

    @Update
    suspend fun actualizar(materia: Materia)

    @Delete
    suspend fun eliminar(materia: Materia)
}
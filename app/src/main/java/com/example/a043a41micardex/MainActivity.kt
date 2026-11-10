package com.example.a043a41micardex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CardexScreen()
                }
            }
        }
    }
}

// ---------- Utilidades ----------

fun formatoCal(v: Double?): String = when {
    v == null -> "—"
    v % 1.0 == 0.0 -> v.toInt().toString()
    else -> v.toString()
}

fun formatoPromedio(v: Double?): String = v?.let { "%.2f".format(it) } ?: "—"

private fun calValida(texto: String): Boolean {
    if (texto.isBlank()) return true // vacío = sin calificación
    val n = texto.replace(',', '.').toDoubleOrNull() ?: return false
    return n in 0.0..100.0
}

private fun parseCal(texto: String): Double? =
    texto.replace(',', '.').toDoubleOrNull()

// ---------- Pantalla única ----------

@Composable
fun CardexScreen(vm: CardexViewModel = viewModel()) {
    val materias by vm.materias.collectAsState()
    val general by vm.promedioGeneral.collectAsState()
    val porSemestre by vm.promediosSemestre.collectAsState()

    var mostrarDialogo by remember { mutableStateOf(false) }
    var materiaEnEdicion by remember { mutableStateOf<Materia?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                materiaEnEdicion = null
                mostrarDialogo = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar materia")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { PerfilCard(nombre = vm.nombreAlumno, control = vm.numeroControl) }
            item { PromediosCard(general, porSemestre) }
            item {
                Text("Materias", style = MaterialTheme.typography.titleMedium)
            }
            if (materias.isEmpty()) {
                item { Text("Aún no has registrado materias. Usa el botón + para agregar una.") }
            }
            materias.groupBy { it.semestre }.forEach { (semestre, lista) ->
                item(key = "semestre_$semestre") {
                    Text(
                        "Semestre $semestre",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(lista, key = { it.id }) { materia ->
                    MateriaItem(
                        materia = materia,
                        onEditar = {
                            materiaEnEdicion = materia
                            mostrarDialogo = true
                        },
                        onEliminar = { vm.eliminarMateria(materia) }
                    )
                }
            }
        }
    }

    if (mostrarDialogo) {
        MateriaDialog(
            inicial = materiaEnEdicion,
            onDismiss = { mostrarDialogo = false },
            onGuardar = {
                vm.guardarMateria(it)
                mostrarDialogo = false
            }
        )
    }
}

@Composable
fun PerfilCard(nombre: String, control: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(nombre, style = MaterialTheme.typography.titleMedium)
            Text("No. de control: $control")
        }
    }
}

@Composable
fun PromediosCard(general: Double?, porSemestre: List<PromedioSemestre>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Promedio general: ${formatoPromedio(general)}",
                style = MaterialTheme.typography.titleLarge
            )
            porSemestre.forEach {
                Text("Semestre ${it.semestre}: ${formatoPromedio(it.promedio)}")
            }
        }
    }
}

@Composable
fun MateriaItem(materia: Materia, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(materia.nombre, style = MaterialTheme.typography.titleSmall)
                Text(
                    "Normal: ${formatoCal(materia.calNormal)}  ·  " +
                            "Repetición: ${formatoCal(materia.calRepeticion)}  ·  " +
                            "Especial: ${formatoCal(materia.calEspecial)}",
                    style = MaterialTheme.typography.bodySmall
                )
                val calFinal = materia.calFinal
                val reprobada = calFinal != null && calFinal < CALIFICACION_MINIMA
                Text(
                    text = when {
                        calFinal == null -> "Calificación final: —"
                        reprobada -> "Calificación final: ${formatoCal(calFinal)} · REPROBADO"
                        else -> "Calificación final: ${formatoCal(calFinal)} · Aprobado"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (reprobada) MaterialTheme.colorScheme.error else Color.Unspecified
                )
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

@Composable
fun MateriaDialog(inicial: Materia?, onDismiss: () -> Unit, onGuardar: (Materia) -> Unit) {
    var nombre by remember { mutableStateOf(inicial?.nombre ?: "") }
    var semestre by remember { mutableStateOf(inicial?.semestre?.toString() ?: "") }
    var normal by remember { mutableStateOf(inicial?.calNormal?.let { formatoCal(it) } ?: "") }
    var repeticion by remember { mutableStateOf(inicial?.calRepeticion?.let { formatoCal(it) } ?: "") }
    var especial by remember { mutableStateOf(inicial?.calEspecial?.let { formatoCal(it) } ?: "") }

    val sem = semestre.toIntOrNull()
    val valido = nombre.isNotBlank() &&
            sem != null && sem in 1..15 &&
            calValida(normal) && calValida(repeticion) && calValida(especial)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (inicial == null) "Nueva materia" else "Editar materia") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la materia") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = semestre,
                    onValueChange = { semestre = it },
                    label = { Text("Semestre (1-8)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = normal,
                    onValueChange = { normal = it },
                    label = { Text("Calif. Normal (0-100)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = repeticion,
                    onValueChange = { repeticion = it },
                    label = { Text("Calif. Repetición (0-100)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = especial,
                    onValueChange = { especial = it },
                    label = { Text("Calif. Especial (0-100)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valido,
                onClick = {
                    onGuardar(
                        Materia(
                            id = inicial?.id ?: 0,
                            nombre = nombre.trim(),
                            semestre = sem!!,
                            calNormal = parseCal(normal),
                            calRepeticion = parseCal(repeticion),
                            calEspecial = parseCal(especial)
                        )
                    )
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
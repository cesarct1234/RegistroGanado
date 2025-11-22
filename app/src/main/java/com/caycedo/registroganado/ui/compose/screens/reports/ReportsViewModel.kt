package com.caycedo.registroganado.ui.compose.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caycedo.registroganado.ui.compose.screens.Produccion
import com.caycedo.registroganado.ui.compose.screens.animals.Animal
import com.caycedo.registroganado.ui.compose.screens.supplies.Supply
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportsViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance()

    // Totales
    private val _totalAnimales = MutableStateFlow(0)
    val totalAnimales: StateFlow<Int> = _totalAnimales

    private val _totalInsumos = MutableStateFlow(0)
    val totalInsumos: StateFlow<Int> = _totalInsumos

    private val _totalProduccion = MutableStateFlow(0)
    val totalProduccion: StateFlow<Int> = _totalProduccion

    private val _promedioLeche = MutableStateFlow(0.0)
    val promedioLeche: StateFlow<Double> = _promedioLeche

    // Gráficas
    private val _distribucionRaza = MutableStateFlow<Map<String, Int>>(emptyMap())
    val distribucionRaza: StateFlow<Map<String, Int>> = _distribucionRaza

    private val _distribucionSexo = MutableStateFlow<Map<String, Int>>(emptyMap())
    val distribucionSexo: StateFlow<Map<String, Int>> = _distribucionSexo

    private val _graficaInsumos = MutableStateFlow<Map<String, Int>>(emptyMap())
    val graficaInsumos: StateFlow<Map<String, Int>> = _graficaInsumos

    private val _produccionPorAnimal = MutableStateFlow<Map<String, Double>>(emptyMap())
    val produccionPorAnimal: StateFlow<Map<String, Double>> = _produccionPorAnimal

    private val _vacunas = MutableStateFlow<Map<String, Int>>(emptyMap())
    val vacunas: StateFlow<Map<String, Int>> = _vacunas

    private val _estadoReproductivo = MutableStateFlow<Map<String, Int>>(emptyMap())
    val estadoReproductivo: StateFlow<Map<String, Int>> = _estadoReproductivo

    private val _aptoConsumo = MutableStateFlow<Map<String, Int>>(emptyMap())
    val aptoConsumo: StateFlow<Map<String, Int>> = _aptoConsumo






    init {
        cargarAnimales()
        cargarInsumos()
        cargarProduccion()
    }

    private fun cargarAnimales() {
        viewModelScope.launch {
            db.getReference("animales_global")
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        var total = 0
                        var litrosTotales = 0.0

                        val razas = mutableMapOf<String, Int>()
                        val sexos = mutableMapOf<String, Int>()
                        val produccionAnimal = mutableMapOf<String, Double>()
                        val vacunasMap = mutableMapOf<String, Int>()
                        val estadoRepMap = mutableMapOf<String, Int>()
                        var aptos = 0
                        var noAptos = 0


                        for (child in snapshot.children) {
                            val a = child.getValue(Animal::class.java) ?: continue
                            total++

                            // Producción promedio
                            val leche = a.produccionLeche.toDoubleOrNull() ?: 0.0
                            litrosTotales += leche
                            produccionAnimal[a.nombre] = leche

                            // Raza
                            razas[a.raza] = (razas[a.raza] ?: 0) + 1

                            // Sexo
                            sexos[a.sexo] = (sexos[a.sexo] ?: 0) + 1

                            // Vacunas (cuenta cuántas vacunas se aplican por tipo)
                            a.vacunaciones.split(",").forEach { v ->
                                val vTrim = v.trim()
                                if (vTrim.isNotEmpty()) vacunasMap[vTrim] = (vacunasMap[vTrim] ?: 0) + 1
                            }

// Estado reproductivo
                            estadoRepMap[a.estadoReproductivo] =
                                (estadoRepMap[a.estadoReproductivo] ?: 0) + 1

// Apto consumo
                            if (a.aptoConsumo) aptos++ else noAptos++
                        }

                        _totalAnimales.value = total
                        _promedioLeche.value = if (total > 0) litrosTotales / total else 0.0

                        _distribucionRaza.value = razas
                        _distribucionSexo.value = sexos
                        _produccionPorAnimal.value = produccionAnimal
                        _vacunas.value = vacunasMap
                        _estadoReproductivo.value = estadoRepMap
                        _aptoConsumo.value = mapOf("Aptos" to aptos, "No aptos" to noAptos)

                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun cargarInsumos() {
        viewModelScope.launch {
            db.getReference("insumos")
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        var count = 0
                        val mapa = mutableMapOf<String, Int>()

                        for (child in snapshot.children) {
                            val s = child.getValue(Supply::class.java) ?: continue
                            count++
                            mapa[s.nombre] = s.cantidad.toIntOrNull() ?: 0
                        }

                        _totalInsumos.value = count
                        _graficaInsumos.value = mapa
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun cargarProduccion() {
        viewModelScope.launch {
            db.getReference("produccion")
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        var count = 0
                        for (child in snapshot.children) {
                            val p = child.getValue(Produccion::class.java)
                            if (p != null) count++
                        }
                        _totalProduccion.value = count
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }
}

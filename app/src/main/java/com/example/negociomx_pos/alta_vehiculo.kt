package com.example.negociomx_pos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.negociomx_pos.BE.*
import com.example.negociomx_pos.DAL.DALVehiculo
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class alta_vehiculo : AppCompatActivity() {

    // ✅ REFERENCIAS A LOS CONTROLES
    private lateinit var txtVIN: TextInputEditText
    private lateinit var txtMotor: TextInputEditText
    private lateinit var spinnerMarca: AutoCompleteTextView
    private lateinit var spinnerModelo: AutoCompleteTextView
    private lateinit var txtAnio: TextInputEditText
    private lateinit var spinnerTransmision: AutoCompleteTextView
    private lateinit var spinnerDireccion: AutoCompleteTextView
    private lateinit var txtVersion: TextInputEditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button
    private lateinit var tvStatus: TextView

    // ✅ INSTANCIA DEL DAL
    private val dalVehiculo = DALVehiculo()

    // ✅ LISTAS PARA LOS SPINNERS
    private var listaMarcas = mutableListOf<Marca>()
    private var listaModelos = mutableListOf<Modelo>()
    private var listaTransmisiones = mutableListOf<Transmision>()
    private var listaDirecciones = mutableListOf<DireccionVehiculo>()

    // ✅ ADAPTADORES PARA LOS SPINNERS
    private lateinit var adapterMarcas: ArrayAdapter<Marca>
    private lateinit var adapterModelos: ArrayAdapter<Modelo>
    private lateinit var adapterTransmisiones: ArrayAdapter<Transmision>
    private lateinit var adapterDirecciones: ArrayAdapter<DireccionVehiculo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alta_vehiculo)

        // ✅ INICIALIZAR CONTROLES
        inicializarControles()

        // ✅ CONFIGURAR EVENTOS
        configurarEventos()

        // ✅ CARGAR DATOS INICIALES
        cargarDatosIniciales()
    }

    /**
     * ✅ INICIALIZAR TODAS LAS REFERENCIAS A LOS CONTROLES
     */
    private fun inicializarControles() {
        try {
            txtVIN = findViewById(R.id.txtVIN)
            txtMotor = findViewById(R.id.txtMotor)
            spinnerMarca = findViewById(R.id.spinnerMarca)
            spinnerModelo = findViewById(R.id.spinnerModelo)
            txtAnio = findViewById(R.id.txtAnio)
            spinnerTransmision = findViewById(R.id.spinnerTransmision)
            spinnerDireccion = findViewById(R.id.spinnerDireccion)
            txtVersion = findViewById(R.id.txtVersion)
            btnGuardar = findViewById(R.id.btnGuardar)
            btnCancelar = findViewById(R.id.btnCancelar)
            tvStatus = findViewById(R.id.tvStatus)

            Log.d("AltaVehiculo", "✅ Controles inicializados correctamente")
        } catch (e: Exception) {
            Log.e("AltaVehiculo", "❌ Error inicializando controles: ${e.message}")
            mostrarMensaje("Error inicializando la pantalla", true)
        }
    }

    /**
     * ✅ CONFIGURAR TODOS LOS EVENTOS DE LOS CONTROLES
     */
    private fun configurarEventos() {
        // ✅ EVENTO DEL BOTÓN GUARDAR
        btnGuardar.setOnClickListener {
            guardarVehiculo()
        }

        // ✅ EVENTO DEL BOTÓN CANCELAR
        btnCancelar.setOnClickListener {
            finish() // Cierra la actividad y regresa a la anterior
        }

        // ✅ EVENTO CUANDO CAMBIA LA MARCA - ACTUALIZAR MODELOS
        spinnerMarca.setOnItemClickListener { _, _, position, _ ->
            val marcaSeleccionada = listaMarcas[position]
            cargarModelosPorMarca(marcaSeleccionada.IdMarcaAuto)
            Log.d("AltaVehiculo", "✅ Marca seleccionada: ${marcaSeleccionada.Nombre}")
        }
    }

    /**
     * ✅ CARGAR TODOS LOS DATOS INICIALES DE LOS SPINNERS
     */
    private fun cargarDatosIniciales() {
        lifecycleScope.launch {
            try {
                mostrarMensaje("Cargando datos...", false)

                // ✅ PROBAR CONEXIÓN PRIMERO
            /*   if (!dalVehiculo.probarConexion()) {
                    mostrarMensaje("Error de conexión a la base de datos", true)
                    return@launch
                }*/

                // ✅ CARGAR MARCAS
                listaMarcas.clear()
                listaMarcas.addAll(dalVehiculo.obtenerMarcas())
                adapterMarcas = ArrayAdapter(this@alta_vehiculo, android.R.layout.simple_dropdown_item_1line, listaMarcas)
                spinnerMarca.setAdapter(adapterMarcas)

                // ✅ CARGAR TRANSMISIONES
                listaTransmisiones.clear()
                listaTransmisiones.addAll(dalVehiculo.obtenerTransmisiones())
                adapterTransmisiones = ArrayAdapter(this@alta_vehiculo, android.R.layout.simple_dropdown_item_1line, listaTransmisiones)
                spinnerTransmision.setAdapter(adapterTransmisiones)

                // ✅ CARGAR DIRECCIONES
                listaDirecciones.clear()
                listaDirecciones.addAll(dalVehiculo.obtenerDirecciones())
                adapterDirecciones = ArrayAdapter(this@alta_vehiculo, android.R.layout.simple_dropdown_item_1line, listaDirecciones)
                spinnerDireccion.setAdapter(adapterDirecciones)

                // ✅ INICIALIZAR ADAPTER DE MODELOS VACÍO
                listaModelos.clear()
                adapterModelos = ArrayAdapter(this@alta_vehiculo, android.R.layout.simple_dropdown_item_1line, listaModelos)
                spinnerModelo.setAdapter(adapterModelos)

                mostrarMensaje("Datos cargados correctamente", false)
                ocultarMensaje()

                Log.d("AltaVehiculo", "✅ Datos iniciales cargados: ${listaMarcas.size} marcas, ${listaTransmisiones.size} transmisiones, ${listaDirecciones.size} direcciones")

            } catch (e: Exception) {
                Log.e("AltaVehiculo", "❌ Error cargando datos iniciales: ${e.message}")
                mostrarMensaje("Error cargando datos: ${e.message}", true)
            }
        }
    }

    /**
     * ✅ CARGAR MODELOS SEGÚN LA MARCA SELECCIONADA
     */
    private fun cargarModelosPorMarca(idMarca: Int) {
        lifecycleScope.launch {
            try {
                // ✅ LIMPIAR SELECCIÓN ACTUAL DE MODELO
                spinnerModelo.setText("", false)

                // ✅ CARGAR NUEVOS MODELOS
                listaModelos.clear()
                listaModelos.addAll(dalVehiculo.obtenerModelosPorMarca(idMarca))
                adapterModelos.notifyDataSetChanged()

                Log.d("AltaVehiculo", "✅ Modelos cargados para marca $idMarca: ${listaModelos.size} modelos")

            } catch (e: Exception) {
                Log.e("AltaVehiculo", "❌ Error cargando modelos: ${e.message}")
                mostrarMensaje("Error cargando modelos: ${e.message}", true)
            }
        }
    }

    /**
     * ✅ VALIDAR Y GUARDAR EL VEHÍCULO
     */
    private fun guardarVehiculo() {
        lifecycleScope.launch {
            try {
                // ✅ VALIDAR CAMPOS OBLIGATORIOS
                if (!validarCampos()) {
                    return@launch
                }

                mostrarMensaje("Guardando vehículo...", false)

                // ✅ OBTENER VALORES DE LOS CONTROLES
                val vin = txtVIN.text.toString().trim().uppercase()
                val motor = txtMotor.text.toString().trim().uppercase()
                val anio = txtAnio.text.toString().trim().toInt()
                val version = txtVersion.text.toString().trim()

                // ✅ OBTENER IDs DE LOS SPINNERS
                val marcaSeleccionada = obtenerMarcaSeleccionada()
                val modeloSeleccionado = obtenerModeloSeleccionado()
                val transmisionSeleccionada = obtenerTransmisionSeleccionada()
                val direccionSeleccionada = obtenerDireccionSeleccionada()

                if (marcaSeleccionada == null || modeloSeleccionado == null ||
                    transmisionSeleccionada == null || direccionSeleccionada == null) {
                    mostrarMensaje("Por favor selecciona todos los campos requeridos", true)
                    return@launch
                }

                // ✅ INSERTAR EN LA BASE DE DATOS
                val resultado = dalVehiculo.insertarVehiculo(
                    vin = vin,
                    motor = motor,
                    idMarca = marcaSeleccionada.IdMarcaAuto,
                    idModelo = modeloSeleccionado.IdModelo,
                    anio = anio,
                    idTransmision = transmisionSeleccionada.IdTransmision,
                    idDireccion = direccionSeleccionada.IdDireccionVehiculo,
                    version = version
                )

                if (resultado) {
                    mostrarMensaje("✅ Vehículo guardado exitosamente", false)
                    limpiarFormulario()

                    // ✅ OPCIONAL: CERRAR LA ACTIVIDAD DESPUÉS DE 2 SEGUNDOS
                    // Handler(Looper.getMainLooper()).postDelayed({ finish() }, 2000)
                } else {
                    mostrarMensaje("❌ Error al guardar el vehículo", true)
                }

            } catch (e: Exception) {
                Log.e("AltaVehiculo", "❌ Error guardando vehículo: ${e.message}")
                mostrarMensaje("Error: ${e.message}", true)
            }
        }
    }

    /**
     * ✅ VALIDAR QUE TODOS LOS CAMPOS ESTÉN COMPLETOS
     */
    private fun validarCampos(): Boolean {
        // ✅ VALIDAR VIN
        if (txtVIN.text.toString().trim().length != 17) {
            mostrarMensaje("El VIN debe tener exactamente 17 caracteres", true)
            txtVIN.requestFocus()
            return false
        }

        // ✅ VALIDAR MOTOR
        if (txtMotor.text.toString().trim().isEmpty()) {
            mostrarMensaje("El número de motor es obligatorio", true)
            txtMotor.requestFocus()
            return false
        }

        // ✅ VALIDAR MARCA
        if (spinnerMarca.text.toString().trim().isEmpty()) {
            mostrarMensaje("Debes seleccionar una marca", true)
            spinnerMarca.requestFocus()
            return false
        }

        // ✅ VALIDAR MODELO
        if (spinnerModelo.text.toString().trim().isEmpty()) {
            mostrarMensaje("Debes seleccionar un modelo", true)
            spinnerModelo.requestFocus()
            return false
        }

        // ✅ VALIDAR AÑO
        val anioTexto = txtAnio.text.toString().trim()
        if (anioTexto.isEmpty()) {
            mostrarMensaje("El año es obligatorio", true)
            txtAnio.requestFocus()
            return false
        }

        try {
            val anio = anioTexto.toInt()
            if (anio < 1900 || anio > 2030) {
                mostrarMensaje("El año debe estar entre 1900 y 2030", true)
                txtAnio.requestFocus()
                return false
            }
        } catch (e: NumberFormatException) {
            mostrarMensaje("El año debe ser un número válido", true)
            txtAnio.requestFocus()
            return false
        }

        // ✅ VALIDAR TRANSMISIÓN
        if (spinnerTransmision.text.toString().trim().isEmpty()) {
            mostrarMensaje("Debes seleccionar una transmisión", true)
            spinnerTransmision.requestFocus()
            return false
        }

        // ✅ VALIDAR DIRECCIÓN
        if (spinnerDireccion.text.toString().trim().isEmpty()) {
            mostrarMensaje("Debes seleccionar una dirección", true)
            spinnerDireccion.requestFocus()
            return false
        }

        // ✅ VALIDAR VERSIÓN
        if (txtVersion.text.toString().trim().isEmpty()) {
            mostrarMensaje("La versión es obligatoria", true)
            txtVersion.requestFocus()
            return false
        }

        return true
    }

    /**
     * ✅ OBTENER LA MARCA SELECCIONADA
     */
    private fun obtenerMarcaSeleccionada(): Marca? {
        val textoSeleccionado = spinnerMarca.text.toString()
        return listaMarcas.find { it.Nombre == textoSeleccionado }
    }

    /**
     * ✅ OBTENER EL MODELO SELECCIONADO
     */
    private fun obtenerModeloSeleccionado(): Modelo? {
        val textoSeleccionado = spinnerModelo.text.toString()
        return listaModelos.find { it.Nombre == textoSeleccionado }
    }

    /**
     * ✅ OBTENER LA TRANSMISIÓN SELECCIONADA
     */
    private fun obtenerTransmisionSeleccionada(): Transmision? {
        val textoSeleccionado = spinnerTransmision.text.toString()
        return listaTransmisiones.find { it.Nombre == textoSeleccionado }
    }

    /**
     * ✅ OBTENER LA DIRECCIÓN SELECCIONADA
     */
    private fun obtenerDireccionSeleccionada(): DireccionVehiculo? {
        val textoSeleccionado = spinnerDireccion.text.toString()
        return listaDirecciones.find { it.Nombre == textoSeleccionado }
    }

    /**
     * ✅ LIMPIAR TODOS LOS CAMPOS DEL FORMULARIO
     */
    private fun limpiarFormulario() {
        txtVIN.setText("")
        txtMotor.setText("")
        spinnerMarca.setText("", false)
        spinnerModelo.setText("", false)
        txtAnio.setText("")
        spinnerTransmision.setText("", false)
        spinnerDireccion.setText("", false)
        txtVersion.setText("")

        // ✅ LIMPIAR LISTA DE MODELOS
        listaModelos.clear()
        adapterModelos.notifyDataSetChanged()
    }

    /**
     * ✅ MOSTRAR MENSAJE DE ESTADO
     */
    private fun mostrarMensaje(mensaje: String, esError: Boolean) {
        tvStatus.text = mensaje
        tvStatus.setTextColor(if (esError) resources.getColor(android.R.color.holo_red_light) else resources.getColor(android.R.color.white))
        tvStatus.visibility = View.VISIBLE
    }

    /**
     * ✅ OCULTAR MENSAJE DE ESTADO
     */
    private fun ocultarMensaje() {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            tvStatus.visibility = View.GONE
        }, 3000) // Ocultar después de 3 segundos
    }
}

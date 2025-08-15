package com.example.negociomx_pos

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.adapters.ClienteAdapter
import com.example.negociomx_pos.room.db.POSDatabase
import com.example.negociomx_pos.room.entities.Cliente
import com.example.negociomx_pos.room.entities.DocDet
import com.example.negociomx_pos.room.enums.RfcGenericoEnum
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class cliente_activity : AppCompatActivity() {

    lateinit var base: POSDatabase

    lateinit var txtNombre: EditText
    lateinit var txtRfc: EditText
    lateinit var txtCP: EditText
    lateinit var txtTelefono: EditText
    lateinit var txtEmail: EditText
    lateinit var chkGenerico: CheckBox
    lateinit var chkActivo: CheckBox
    lateinit var chkPredeterminado: CheckBox
    lateinit var lblEncabezado: TextView
    lateinit var rv: RecyclerView
    lateinit var imgNuevo: ImageView
    lateinit var imgAdeudo: ImageView
    lateinit var imgRegresar: ImageView

    var idCliente: Int = 0
    lateinit var listaClientes: List<Cliente>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cliente)

        base = POSDatabase.getDatabase(applicationContext)

        txtNombre = findViewById(R.id.txtNombreCliente)
        txtCP = findViewById(R.id.txtCPCliente)
        txtEmail = findViewById(R.id.txtEmailCliente)
        txtTelefono = findViewById(R.id.txtTelefonoCliente)
        txtRfc = findViewById(R.id.txtRfcCliente)
        chkPredeterminado = findViewById(R.id.chkPredeterminadoClienteAlta)
        chkActivo = findViewById(R.id.chkActivoCliente)
        chkGenerico = findViewById(R.id.chkRfcGenericoCliente)

        imgRegresar = findViewById(R.id.btnRegresarCliente)
        imgNuevo = findViewById(R.id.btnNuevoClienteAlta)
        imgAdeudo = findViewById(R.id.btnAdeudoClienteAlta)
        lblEncabezado = findViewById(R.id.lblEncabezadoCliente)
        rv = findViewById(R.id.rvCliente)

        imgAdeudo.isVisible = false
        if (idCliente > 0) imgAdeudo.isVisible = true

        imgNuevo.isVisible = false
        lblEncabezado.setText("Cliente -> Nuevo")

        val btnGuardar = findViewById<ImageView>(R.id.btnGuardarCliente)

        chkPredeterminado.isChecked = false
        chkActivo.isChecked = true
        chkActivo.isEnabled = false
        chkGenerico.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                txtRfc.setText("XAXX010101000")
                txtRfc.isEnabled = false

                txtTelefono.requestFocus()
            } else {
                txtRfc.setText("")
                txtRfc.isEnabled = true
            }
        }

        imgRegresar.setOnClickListener {
            finish()
        }
        btnGuardar.setOnClickListener {
            guardaCliente()
        }
        imgNuevo.setOnClickListener {
            nuevoCliente()
        }
        imgAdeudo.setOnClickListener {
            adeudoCliente()
        }

        muestraListaClientes()

        txtNombre.requestFocus()
    }

    private fun adeudoCliente() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pregunta")
        builder.setMessage("Desea ver los pagos y Adeudo del Cliente ?")
            .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.item_adeudo_cliente)

                val lblNombreCliente: TextView = dialog.findViewById(R.id.lblNombreClienteAdeudoCliente)
                val lblTotalAdeudo: TextView = dialog.findViewById(R.id.lblTotalAdeudoCliente)
                val txtMontoPago: EditText = dialog.findViewById(R.id.txtMontoPagoAdeudoCliente)
                val cmbTipoPago: Spinner = dialog.findViewById(R.id.cmbTipoPagoAdeudoCliente)
                val btnGuardar: ImageView = dialog.findViewById(R.id.btnGuardarPagoAdeudoCliente)
                val btnRegresar: ImageView = dialog.findViewById(R.id.btnRegresarAdeudoCliente)

                txtMontoPago.requestFocus()
                btnGuardar.setOnClickListener {
                    var montoPago: Float = 0F
                    var idTipoPago: Short = 0

                    if (txtMontoPago.text.isNotEmpty() == true) montoPago = txtMontoPago.text.toString().toFloat()
                    if (montoPago <= 1F) {
                        txtMontoPago.error = "La cantidad no puede ser menor a 1"
                    } else if (idTipoPago <= 0) {
                        lblNombreCliente.error = "No se puede poner el Precio de venta menor o igual 0"
                    } else {
                        Toast.makeText(
                            applicationContext, "Se ha Realizado el pago correctamente",
                            Toast.LENGTH_LONG
                        ).show()

                        dialog.dismiss()
                    }
                }
                btnRegresar.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            })
            .setNegativeButton("CANCELAR", { dialog, id ->

            })
        builder.show()
    }

    private fun nuevoCliente() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pregunta")
        builder.setMessage("Hay un Cliente modificandose. Aun asi desea continuar?")
            .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                imgNuevo.isVisible = false
                imgAdeudo.isVisible = false

                limpiarControles()
            })
            .setNegativeButton("CANCELAR", { dialog, id ->

            })
        builder.show()
    }

    private fun guardaCliente() {
        if (txtNombre.text.toString().length < 5)
            txtNombre.error = "Es necesario suministrar un nombre minimo de 5 letras"
        else if (txtRfc.text.toString().length < 10)
            txtRfc.error = "Es necesario un RFC valida de 10 caracteres"
        else if (txtTelefono.text.toString().length < 5)
            txtTelefono.error = "Es necesario suministrar un telefono valido"
        else {
            val scope = MainScope()

            val nombre = txtNombre.text.toString()
            val telefonos = txtTelefono.text.toString()
            val emails = txtEmail.text.toString()
            var rfc = txtRfc.text.toString()
            val activo = chkActivo.isChecked
            var codigoPostal: Int = 0
            var existe: Boolean = false
            var predeterminado: Boolean = chkPredeterminado.isChecked
            if (txtCP.text.isNotEmpty())
                codigoPostal = txtCP.text.toString().toInt()

            if (rfc.isEmpty())
                rfc = RfcGenericoEnum.Nacional.toString()

            fun asyncFun() = scope.launch {
                if (idCliente == 0) {
                    val find = base.clienteDAO().getByNombreAndRfc(nombre, rfc)
                    existe = if (find.value != null) true else false
                    if (existe) {
                        existe = true
                        Toast.makeText(
                            applicationContext,
                            "El cliente ya existe",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        if (predeterminado)
                            base.clienteDAO().updateAllPredeterminado(0)

                        base.clienteDAO().insert(
                            Cliente(
                                Nombre = nombre,
                                RazonSocial = nombre,
                                Activo = activo,
                                Predeterminado = predeterminado,
                                CodigoPostal = codigoPostal,
                                Email = emails,
                                Telefonos = telefonos,
                                Rfc = rfc,
                                Contactos = "",
                            )
                        )
                    }
                } else {
                    if (predeterminado)
                        base.clienteDAO().updateAllPredeterminado(0)

                    base.clienteDAO().update(
                        Cliente(
                            IdCliente = idCliente,
                            Rfc = rfc,
                            Nombre = nombre,
                            RazonSocial = nombre,
                            Activo = activo,
                            CodigoPostal = codigoPostal,
                            Predeterminado = predeterminado,
                            Email = emails,
                            Telefonos = telefonos
                        )
                    )
                }

                if (!existe) {
                    Toast.makeText(
                        applicationContext,
                        "Se ha guardado el Clientes satisfactoriamente",
                        Toast.LENGTH_LONG
                    ).show()

                    limpiarControles()
                }
            }

            asyncFun()
        }
    }

    private fun muestraListaClientes() {
        GlobalScope.launch {
            var lista = base.clienteDAO().getAll(null)

            runOnUiThread {
                lista.observe(this@cliente_activity) {
                    listaClientes = arrayListOf()
                    listaClientes = it

                    val adaptador = ClienteAdapter(listaClientes, { onItemSelected(it) })

                    rv.layoutManager = LinearLayoutManager(applicationContext)
                    rv.adapter = adaptador
                }
            }
        }
    }

    private fun onItemSelected(cliente: Cliente) {
        if (idCliente > 0) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pregunta")
            builder.setMessage("Cliente modificandose. Aun asi desea continuar?")
                .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                    imgNuevo.isVisible = true
                    muestraDatosCliente(cliente)
                })
                .setNegativeButton("CANCELAR", { dialog, id ->

                })
            builder.show()
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pregunta")
            builder.setMessage("Desea modificar el cliente: ${cliente.Nombre} ?")
                .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                    imgNuevo.isVisible = true
                    imgAdeudo.isVisible = true
                    muestraDatosCliente(cliente)
                })
                .setNegativeButton("CANCELAR", { dialog, id ->

                })
            builder.show()
        }
    }

    private fun muestraDatosCliente(cliente: Cliente) {
        idCliente = cliente.IdCliente
        lblEncabezado.setText("Cliente -> Edición")
        idCliente = cliente.IdCliente

        txtNombre.setText(cliente.Nombre)
        txtTelefono.setText("")
        if (cliente.Telefonos.isNotEmpty())
            txtTelefono.setText(cliente.Telefonos)

        txtEmail.setText("")
        if (cliente.Email.isNotEmpty())
            txtEmail.setText(cliente.Email)

        txtCP.setText("")
        if (cliente.CodigoPostal > 0)
            txtCP.setText(cliente.CodigoPostal.toString())

        chkPredeterminado.isChecked = cliente.Predeterminado

        chkGenerico.isChecked = cliente.Rfc.equals("XAXX010101000")
        chkActivo.isEnabled = true
        chkActivo.isChecked = cliente.Activo
    }

    private fun limpiarControles() {
        idCliente = 0

        txtRfc.text?.clear()
        txtNombre.text?.clear()
        txtEmail.text?.clear()
        txtTelefono.text?.clear()
        txtCP.text?.clear()

        chkPredeterminado.isChecked = false
        chkActivo.isChecked = false
        chkGenerico.isChecked = false
        imgAdeudo.isVisible = false
        lblEncabezado.setText("Cliente -> Nuevo")

        txtNombre.requestFocus()
    }
}
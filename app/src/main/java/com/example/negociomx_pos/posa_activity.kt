package com.example.negociomx_pos

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.icu.text.DecimalFormat
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.adapters.ArticuloVentaPOSAdapter
import com.example.negociomx_pos.adapters.SpinnerAdapter
import com.example.negociomx_pos.room.BLL.BLLDoc
import com.example.negociomx_pos.room.BLL.BLLUtil
import com.example.negociomx_pos.room.db.POSDatabase
import com.example.negociomx_pos.room.entities.Cliente
import com.example.negociomx_pos.room.entities.DocDet
import com.example.negociomx_pos.room.entities.Documento
import com.example.negociomx_pos.room.entities.DocumentoDetalle
import com.example.negociomx_pos.room.entities.Impuesto
import com.example.negociomx_pos.room.entities.ItemSpinner
import com.example.negociomx_pos.room.entities.PagoDocumento
import com.example.negociomx_pos.room.entities.TipoPago
import com.example.negociomx_pos.room.enums.TipoDocumentoEnum
import com.example.negociomx_pos.room.enums.TipoStatusNotaVentaEnum
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class posa_activity : AppCompatActivity() {
    lateinit var txtNombreFilter: EditText
    lateinit var base: POSDatabase

    var folioNotaVenta:String=""
    var idDocumentoPendiente:Int?=null

    private lateinit var rvArticulosVenta:RecyclerView
    lateinit var listaVenta:List<DocDet>
    lateinit var listaImpuestos:List<Impuesto>
    lateinit var listaCliente:List<Cliente>
    lateinit var cmbCliente:Spinner
    lateinit var cmbImpuesto:Spinner
    lateinit var imgNuevoArticulo:ImageView
    lateinit var imgBuscarArticulos:ImageView
    lateinit var imgGuardarVenta:ImageView
    lateinit var imgRegresarPOS:ImageView
    lateinit var lblImpuesto:TextView
    lateinit var lblCliente:TextView
    lateinit var lblEncListaArticulos:TextView
    lateinit var lblIVAVenta:TextView
    lateinit var lblTotalVenta:TextView
    lateinit var lblFolioVentaPOS:TextView
    val dec = DecimalFormat("#,##0.00")
    lateinit var bll: BLLDoc
    val pago:PagoDocumento?=null
    var subtotal:Float=0F
    var iva:Float=0F
    var total:Float=0F
    lateinit var bllUtil:BLLUtil

    private  val  startForResult=
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {resul->
            if(resul.resultCode== RESULT_OK)
            {
                var intent=resul.data
                var actualiza: Boolean = intent?.getBooleanExtra("agrega", false)!!
                if (actualiza) {
                    val idArticulo: Int = intent?.getIntExtra("idArticulo", 0)!!
                    var cantidad: Float = intent?.getFloatExtra("cantidad", 0F)!!

                    var find: DocDet? = null
                    listaVenta.forEach() {
                        if (it.IdArticulo == idArticulo)
                            find = it
                    }

                    if (find != null) {
                        cantidad=find!!.Cantidad!!+cantidad

                        find!!.Cantidad = cantidad
                        find!!.Importe = find!!.Cantidad!! * find!!.PrecioUnitario!!
                    } else {
                        val nombre: String = intent?.getStringExtra("nombre")!!
                        val precioVenta: Float = intent?.getFloatExtra("precioVenta", 0F)!!
                        val consecutivo = (listaVenta.size + 1)
                        val importe = cantidad * precioVenta

                        var articulo: DocDet = DocDet(
                            IdArticulo = idArticulo,
                            NombreArticuloServicio = nombre,
                            PrecioUnitario = precioVenta,
                            Cantidad = cantidad,
                            Importe = importe,
                            Consecutivo = consecutivo
                        )

                        listaVenta += articulo
                    }

                    muestraArticulosVenta()

                    txtNombreFilter.text?.clear()
                    txtNombreFilter.requestFocus()
                } else {
                    txtNombreFilter.text?.clear()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posa)
        base=POSDatabase.getDatabase(applicationContext)

        bll= BLLDoc()
        listaVenta= arrayListOf()
        bllUtil= BLLUtil()

        lblIVAVenta=findViewById(R.id.lblIvaVentaPOS)
        lblTotalVenta=findViewById(R.id.lblTotalVentaPOS)
        lblEncListaArticulos=findViewById(R.id.lblEncListaArticuloPOS)
        lblCliente=findViewById(R.id.lblClientePOS)
        lblImpuesto=findViewById(R.id.lblImpuestoPOS)
        lblFolioVentaPOS=findViewById(R.id.lblFolioVentaPOS)
        imgGuardarVenta=findViewById(R.id.btnGuardarVentaPOS)
        imgBuscarArticulos=findViewById(R.id.imgBuscarPOS)
        imgNuevoArticulo=findViewById(R.id.imgAgregaNuevoArticuloPOS)
        imgRegresarPOS=findViewById(R.id.btnRegresarVentaPOS)
        cmbCliente=findViewById(R.id.cmbClientePOS)
        cmbImpuesto=findViewById(R.id.cmbImpuestoPOS)
        txtNombreFilter=findViewById(R.id.txtClaveCodigoNombreArticuloPOS)
        rvArticulosVenta=findViewById(R.id.rvArticulosVentaPOS)
        txtNombreFilter.requestFocus()
        txtNombreFilter.setOnKeyListener({ v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER ) {
                return@setOnKeyListener true
            }
            false
        })

        cmbImpuesto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(applicationContext, "onNothingSelected", Toast.LENGTH_SHORT).show()
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                var cad=position.toString()
                if(cmbImpuesto.getItemAtPosition(position)!=null) {
                    var find: ItemSpinner = cmbImpuesto.getItemAtPosition(position) as ItemSpinner

                    CalculaTotalesVenta()
                }
            }
        }
        lblEncListaArticulos.setText("Lista de Articulos")
        imgBuscarArticulos.setOnClickListener{
            val nombreFilter=txtNombreFilter.text.toString()
            ejecutaConsultaArticulosPOS(nombreFilter)
        }
        imgNuevoArticulo.setOnClickListener{
            AgregaNuevoArticulo()
        }
        imgRegresarPOS.setOnClickListener{
            intent.putExtra("idDocumentoPendiente",idDocumentoPendiente)
            setResult(RESULT_OK, intent)
            finish()
        }
        imgGuardarVenta.setOnClickListener{
            if(listaVenta==null || listaVenta.count()==0)
            {
                bllUtil.MessageShow(this,"Aceptar","","Es necesario seleccionar Articulo.",
                    "Aviso")
                {
                    txtNombreFilter.requestFocus()
                }
            }
            else {
                ejecutaRelacionarCobroVenta()
            }
        }

        muestraClientesPOS()
        muestraImpuestosPOS()
        muestraArticulosVenta()

        if(intent.extras?.isEmpty==false)
        {
            idDocumentoPendiente= intent.extras?.getInt("idDocumentoPendiente")

            if(idDocumentoPendiente!=null && idDocumentoPendiente!!>0) {
                var idUsuario:Int=ParametrosSistema.usuarioLogueado.Id!!.toInt()

                val  scope1= MainScope()
                fun asyncFunc()= scope1.launch {
                    val find = base.documentoDAO().getAllDocDet(idDocumentoPendiente!!, null,null)
                    if (find != null) {
                        var idDocMin:Int=find[0].IdDoc
                        folioNotaVenta=find[0].FolioDocumento

                        lblFolioVentaPOS.setText(folioNotaVenta)
                        listaVenta = find.filter { it.IdDoc==idDocMin && ((it.IdArticulo!=null && it.IdArticulo>0) || it.IdServicio!=null) }
                    }
                }
                asyncFunc()
            }
            else
                consultaDocumentoCreadoPorUsuario()
        }
        else
            consultaDocumentoCreadoPorUsuario()
    }

    private fun ejecutaRelacionarCobroVenta()
    {
        if (listaVenta==null || listaVenta.count()==0)
        {
            txtNombreFilter.error="Es necesario agregar Articulos a la Venta."
        }
        else if (cmbCliente.selectedItem == null)
        {
            lblCliente.error="Es necesario un Cliente de la lista."
        }
        else if (cmbImpuesto.selectedItem == null)
        {
            lblImpuesto.error="Es necesario tener un Impuesto valido."
        }
        else {
            realizaCobroVenta()
            {
                cobroRealizado->
                if (cobroRealizado)
                    guardaVenta()
            }
        }
    }

    private fun consultaDocumentoCreadoPorUsuario()
    {
        if(idDocumentoPendiente==null || idDocumentoPendiente!!<=0)
        {
            var idUsuario:Int=ParametrosSistema.usuarioLogueado.Id!!.toInt()
            var idStatus:Int=TipoStatusNotaVentaEnum.Creada.value.toInt()
            val  scope1= MainScope()
            fun asyncFuncion()= scope1.launch {
                try {
                    var findD = base.documentoDAO().getAllDocDet(null, idUsuario,idStatus)
                    if (findD == null || findD.count()==0) {
                        creaNuevoDocumento()
                    }
                    else {
                        var idDocMin:Int=findD[0].IdDoc
                        folioNotaVenta=findD[0].FolioDocumento
                        listaVenta = findD.filter { it.IdDoc==idDocMin && ((it.IdArticulo!=null && it.IdArticulo>0) || it.IdServicio!=null) }
                        idDocumentoPendiente=idDocMin

                        lblFolioVentaPOS.setText(folioNotaVenta)
                    }
                }catch (ex:Exception)
                {
                    var mensaje=ex.toString()
                    if(mensaje.isEmpty()==false)
                    {

                    }
                }
            }
            asyncFuncion()

        }
    }

    private fun creaNuevoDocumento()
    {
        var doc:Documento
        val scope = MainScope()
        fun asyncFun() = scope.launch {
            var consecutivoFolio:Int=1
            var find=base.documentoDAO().getLastConsecutivoFolioDocumento()
            if (find != null)
                consecutivoFolio = find + 1

            var prefijoFolio = ParametrosSistema.cfgNV.PrefijoFolioNV
            var folioDocumento = prefijoFolio + consecutivoFolio.toString()
            var idTipoDocumento: Int = TipoDocumentoEnum.NotaVenta.value.toInt()
            var idUsuario: Int = ParametrosSistema.usuarioLogueado.Id!!.toInt()
            var idStatus: Int = TipoStatusNotaVentaEnum.Creada.value.toInt()
            try {
                lblFolioVentaPOS.setText(folioDocumento)

                doc = Documento(
                    0,
                    null,
                    folioDocumento,
                    consecutivoFolio,
                    prefijoFolio,
                    0,
                    idTipoDocumento,
                    0F,
                    0F,
                    0F,
                    "",
                    0,
                    0F,
                    0,
                    "",
                    0,
                    0,
                    0F,
                    0F,
                    idStatus,
                    idUsuario,
                    false,
                    true
                )
                var idDocumento: Long = base.documentoDAO().insert(doc)
                idDocumentoPendiente = idDocumento.toInt()
            } catch (ex: Exception) {

            }
        }
        asyncFun()
    }

    private fun realizaCobroVenta(onFinishCobroVenta:(Boolean)->Unit) {
        val dialog= Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.item_cobro_ventaa)

        val lblTotalAPagar:TextView=dialog.findViewById(R.id.lblTotalAPagarCobroVentaA)
        val txtMontoPago:EditText=dialog.findViewById(R.id.txtMontoPagoCobroVentaA)
        val btnGuardar: ImageView =dialog.findViewById(R.id.btnGuardarCobroVentaA)
        val btnRegresar: ImageView =dialog.findViewById(R.id.btnRegresarCobroVentaA)
        val chkCredito:CheckBox=dialog.findViewById(R.id.chkCreditoCobroVentaA)
        val cmbTipoPago:Spinner=dialog.findViewById(R.id.cmbTipoPagoCobroVentaA)

        muestraTiposPagoCombo(cmbTipoPago)

        val cad:String=dec.format(total)
        lblTotalAPagar.setText(cad)
        txtMontoPago.requestFocus()
        chkCredito.setOnCheckedChangeListener{
                buttonView, isChecked ->
            if(isChecked) {
                txtMontoPago.isEnabled=false
                txtMontoPago.text?.clear()
                cmbTipoPago.isEnabled=false
            }
            else {
                cmbTipoPago.isEnabled=true
                txtMontoPago.isEnabled=true
                txtMontoPago.requestFocus()
            }
        }

        btnGuardar.setOnClickListener{
            var selTP:ItemSpinner?=null
            var idTipoPago:Int=0
            if(cmbTipoPago.selectedItem!=null) {
                selTP = cmbTipoPago.selectedItem as ItemSpinner
                idTipoPago=selTP.Valor
            }
            var montoTotalVenta:Float=0F
            if(txtMontoPago.text.isNotEmpty()==true) montoTotalVenta=txtMontoPago.text.toString().toFloat()
            if( montoTotalVenta<total && chkCredito.isChecked==false)
            {
                txtMontoPago.error="El pago no debe ser superior al Total de la Venta"

                txtMontoPago.requestFocus()
                txtMontoPago.setSelection(txtMontoPago.text.length)
            }
            else if(selTP==null || idTipoPago==0)
            {
                txtMontoPago.error="El Tipo de pago debe ser una valido de la lista."

                cmbTipoPago.requestFocus()
            }
            else {
                dialog.dismiss()

                onFinishCobroVenta(true)
            }
        }
        btnRegresar.setOnClickListener{
            dialog.dismiss()
            onFinishCobroVenta(false)
        }

        dialog.show()
    }

    private fun muestraTiposPagoCombo(cmb:Spinner) {
        val listaTipos:List<TipoPago>

        val scope = MainScope()
        fun asyncFun() = scope.launch {
            var listaTipos= base.tipoPagoDAO().getByFilters(true)

            var adapter = bllUtil.convertListTipoPagoToListSpinner(applicationContext, listaTipos)
            cmb.adapter=adapter
        }
        asyncFun()
    }

    private fun AgregaNuevoArticulo() {
        val intent=Intent(this,articulo_alta_activity::class.java   )
        intent.putExtra("nuevo",true)
        startForResult.launch(intent)
    }

    private fun guardaVenta() {
        val selCli:ItemSpinner
        val selImp: ItemSpinner

        if (listaVenta==null || listaVenta.count()==0)
        {
            txtNombreFilter.error="Es necesario agregar Articulos a la Venta."
        }
        else if (cmbCliente.selectedItem == null)
        {
            lblCliente.error="Es necesario un Cliente de la lista."
        }
        else if (cmbImpuesto.selectedItem == null)
        {
            lblImpuesto.error="Es necesario tener un Impuesto valido."
        }
        else
        {
            selCli = cmbCliente.selectedItem as ItemSpinner
            selImp=cmbImpuesto.selectedItem as ItemSpinner
            val scope = MainScope()
            fun asyncFun() = scope.launch {
                var idCliente = selCli.Valor
                var idImpuesto = selImp.Valor
                var tasaImpuesto = selImp.FloalVal

                try {
                    var idDocumento:Int=0
                    var detalle:MutableList<DocumentoDetalle>
                    detalle= arrayListOf()

                    if(idDocumentoPendiente!=null && idDocumentoPendiente!!>0)idDocumento=idDocumentoPendiente!!
                    var doc: Documento =bll.asignaDocumento(idDocumento,folioNotaVenta, idCliente, idImpuesto, tasaImpuesto,
                        listaVenta)
                    if(idDocumentoPendiente!=null && idDocumentoPendiente!!>0)
                    {
                        base.documentoDAO().update(doc)
                        var detallesNuevos:List<DocDet>
                        detallesNuevos=listaVenta.filter { it.IdDocDet==0 }
                        var  detallesExistentes:List<DocDet>
                        detallesExistentes=listaVenta.filter { it.IdDocDet!=0 }

                        if(detallesNuevos!=null && detallesNuevos.count()>0)
                        {
                            var detalle1= bll.asignaDocumentoDetalles(idDocumento.toInt(), idImpuesto, tasaImpuesto, detallesNuevos)
                            base.documentoDAO().insertAll(detalle1)
                            detalle1.forEach{ detalle.add(it)}
                        }
                        if(detallesExistentes!=null && detallesExistentes.count()>0)
                        {
                            var detalle1= bll.asignaDocumentoDetalles(idDocumento.toInt(), idImpuesto, tasaImpuesto,
                                detallesExistentes)
                            base.documentoDAO().updateAll(detalle1)
                            detalle1.forEach{ detalle.add(it)}
                        }
                    }
                    else {
                        idDocumento = base.documentoDAO().insert(doc).toInt()
                        var detalle1= bll.asignaDocumentoDetalles(idDocumento.toInt(), idImpuesto, tasaImpuesto, listaVenta)
                        detalle= detalle1 as MutableList<DocumentoDetalle>
                    }

                    detalle.forEach {
                        base.documentoDAO().updateExistenciaArticulo(it.IdArticulo,it.Cantidad)
                    }
                }
                catch (ex:Exception)
                {

                }

                Toast.makeText(
                    applicationContext, "Se realizo la Venta correctamente",
                    Toast.LENGTH_LONG
                ).show()

                limpiarControles()
            }
            asyncFun()
        }
    }

    private fun limpiarControles() {
        listaVenta= arrayListOf()
        muestraArticulosVenta()
        CalculaTotalesVenta()

        idDocumentoPendiente=null
        lblEncListaArticulos.setText("Lista de Articulos")
        cmbCliente.setSelection(0)
        cmbImpuesto.setSelection(0)

        consultaDocumentoCreadoPorUsuario()
    }

    private fun muestraArticulosVenta() {
        val adaptador = ArticuloVentaPOSAdapter(listaVenta){
            detalle,comando -> onItemSelected(detalle,comando)
        }

        rvArticulosVenta.layoutManager = LinearLayoutManager(applicationContext)
        rvArticulosVenta.adapter = adaptador

        CalculaTotalesVenta()
    }

    private fun CalculaTotalesVenta()
    {
        var totalArticulos:Int=0
        subtotal=0F
        iva=0F
        total=0F

        if(listaVenta!=null && listaVenta.count()>0)
        {
            var imp:ItemSpinner?=null
            if(cmbImpuesto.selectedItem!=null)imp= cmbImpuesto.selectedItem as ItemSpinner
            var impuestoPartida:Float=0F

            listaVenta.forEach{
                totalArticulos+=it.Cantidad!!.toInt()
                subtotal+=it.Importe!!
                impuestoPartida=0F
                if(imp!=null && imp.FloalVal>0)
                    impuestoPartida=(imp.FloalVal/100F)*it.Importe!!
                iva+=impuestoPartida
            }

            total=subtotal+iva
        }

        lblEncListaArticulos.setText("Lista de Articulos (${totalArticulos})")
        lblTotalVenta.setText("Total: $ "+dec.format(total))
        lblIVAVenta.setText("IVA:$ "+dec.format(iva))
    }

    private fun onItemSelected(detalle: DocDet,comando:Int) {
        var mensaje:String="Desea eliminar el Articulo: ${detalle.NombreArticuloServicio} ?"
        if(comando==2)
            mensaje="Desea modificar la Partida: ${detalle.NombreArticuloServicio} ?"

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pregunta")
        builder.setMessage(mensaje)
            .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                if(comando==1) {
                    listaVenta -= detalle

                    val find = listaVenta.filter { it.IdArticulo == detalle.IdArticulo }
                    if (find != null) {
                        listaVenta -= find
                        muestraArticulosVenta()
                    }
                }
                else
                {
                    val dialog= Dialog(this)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setContentView(R.layout.item_preciocantidad)

                    val txtCantidad:EditText=dialog.findViewById(R.id.txtCantidadCantidad)
                    val txtPV:EditText=dialog.findViewById(R.id.txtPrecioVentaCantidad)
                    val btnSumar:ImageView=dialog.findViewById(R.id.btnSumarCantidad)
                    val btnRestar: ImageView =dialog.findViewById(R.id.btnRestarCantidad)
                    val btnAceptar: Button =dialog.findViewById(R.id.btnAceptarCantidad)
                    val btnCancelar: Button =dialog.findViewById(R.id.btnCancelarCantidad)

                    txtPV.setText(detalle.PrecioUnitario.toString())
                    txtCantidad.setText("%.0f".format(detalle.Cantidad))

                    txtCantidad.requestFocus()
                    txtCantidad.setSelection(txtCantidad.text.length)

                    btnRestar.setOnClickListener{
                        var cantCad:String=txtCantidad.text.toString()

                        if(cantCad.isNotEmpty()==true)
                        {
                            var cant:Float=cantCad.toFloat()
                            if (cant>1) {
                                cant-=1
                                txtCantidad.setText(cant.toString())
                            }
                            txtCantidad.setSelection(txtCantidad.text.length)
                        }
                    }
                    btnSumar.setOnClickListener{
                        var cantCad:String=txtCantidad.text.toString()

                        if(cantCad.isNotEmpty()==true)
                        {
                            var cant:Float=cantCad.toFloat()+1
                            txtCantidad.setText("%.0f".format(cant))
                        }
                        else
                            txtCantidad.setText("1")
                        txtCantidad.setSelection(txtCantidad.text.length)
                    }
                    btnAceptar.setOnClickListener{
                        var cantidad:Float=0F
                        var precioVenta:Float=0F

                        if(txtCantidad.text.isNotEmpty()==true)cantidad= txtCantidad.text.toString().toFloat()
                        if(txtPV.text.isNotEmpty()==true) precioVenta=txtPV.text.toString().toFloat()
                        if( cantidad<=1F)
                        {
                            txtCantidad.error="La cantidad no puede ser menor a 1"
                        }
                        else if( precioVenta<=0F)
                        {
                            txtPV.error="No se puede poner el Precio de venta menor o igual 0"
                        }
                        else {
                            var find: DocDet? =
                                listaVenta.firstOrNull { it.IdArticulo == detalle.IdArticulo }

                            if (find != null) {
                                find.Cantidad = cantidad
                                find.PrecioUnitario = precioVenta
                                find.Importe = find.Cantidad!! * find.PrecioUnitario!!

                                muestraArticulosVenta()
                                CalculaTotalesVenta()

                                Toast.makeText(
                                    applicationContext, "Se ha actualizado la Partida",
                                    Toast.LENGTH_LONG
                                ).show()

                                dialog.dismiss()
                            }
                        }
                    }
                    btnCancelar.setOnClickListener{
                        dialog.dismiss()
                    }

                    dialog.show()
                }
            })
            .setNegativeButton("CANCELAR", { dialog, id ->

            })
        builder.show()
    }

    private fun muestraClientesPOS() {
        GlobalScope.launch {
            val lista = base.clienteDAO().getAll(true)

            runOnUiThread {
                lista.observe(this@posa_activity)
                {
                    try {
                        listaCliente = arrayListOf()
                        listaCliente = it

                        val transform: (Cliente) -> (ItemSpinner) = {
                            ItemSpinner(it.IdCliente.toInt(), it.Nombre)
                        }
                        val result = listaCliente.map(transform).toList()

                        val adapterStatus = SpinnerAdapter(
                            applicationContext, result, R.layout.item_spinner_status,
                            R.id.lblDisplayStatus
                        )
                        cmbCliente.adapter = adapterStatus
                    }
                    catch (ex:Exception)
                    {
                        val cad=ex.toString()
                    }
                }
            }
        }
    }

    private fun muestraImpuestosPOS() {
        GlobalScope.launch {
            val lista = base.impuestoDAO().getAll()

            runOnUiThread {
                lista.observe(this@posa_activity)
                {
                    try {
                        listaImpuestos = arrayListOf()
                        listaImpuestos = it

                        val transform: (Impuesto) -> (ItemSpinner) = {
                            ItemSpinner(it.IdImpuesto.toInt(), it.Nombre, FloalVal =it.Tasa)
                        }
                        val result = listaImpuestos.map(transform).toList()

                        val adapter = SpinnerAdapter(
                            applicationContext, result, R.layout.item_spinner_status,
                            R.id.lblDisplayStatus
                        )
                        cmbImpuesto.adapter = adapter
                    }
                    catch (ex:Exception)
                    {
                        val cad=ex.toString()
                    }
                }
            }
        }
    }

    private fun ejecutaConsultaArticulosPOS(nombreFilter: String) {
        val intent= Intent(this,consulta_articulo_posa_activity::class.java   )
        intent.putExtra("nombreFilter",nombreFilter)
        startForResult.launch(intent)
    }
}
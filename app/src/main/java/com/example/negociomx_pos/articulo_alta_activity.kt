package com.example.negociomx_pos

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.adapters.SpinnerAdapter
import com.example.negociomx_pos.room.BLL.BLLUtil
import com.example.negociomx_pos.room.db.POSDatabase
import com.example.negociomx_pos.room.entities.Articulo
import com.example.negociomx_pos.room.entities.Categoria
import com.example.negociomx_pos.room.entities.ItemSpinner
import com.example.negociomx_pos.room.entities.Status
import com.example.negociomx_pos.room.entities.UnidadMedida
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.random.Random

class articulo_alta_activity : AppCompatActivity() {

    lateinit var base: POSDatabase

    lateinit var lblEncArticulo:TextView
    lateinit var txtNombre:EditText
    lateinit var txtCB:EditText
    lateinit var txtPV:EditText
    lateinit var txtExistencia:EditText
    lateinit var cmbUM:Spinner
    lateinit var cmbStatus:Spinner
    lateinit var cmbCategoria:Spinner
    lateinit var lblCategoria:TextView
    lateinit var imgNuevaCategoria:ImageView
    lateinit var imgNuevoArticulo:ImageView
    lateinit var imgRegresarArticulo:ImageView
    lateinit var nombreFotoArchivo:String
    lateinit var imgFoto:ImageView
    var tipoArticulo:Int=1

    var nuevo:Boolean=false
    lateinit var bitmap: Bitmap
    lateinit var listaCategorias:List<Categoria>
    lateinit var listaUnidadesMedida:List<UnidadMedida>

    lateinit var bllUtil: BLLUtil
    var idArticulo:Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articulo_alta)

        base=POSDatabase.getDatabase(applicationContext)

        lblEncArticulo = findViewById(R.id.lblEncabezadoAltaArticulo)
        lblCategoria = findViewById(R.id.lblCategoriaAltaArticulo)
        txtNombre = findViewById<EditText>(R.id.txtNombreArticulo)
        txtCB = findViewById<EditText>(R.id.txtCodigoBarraArticulo)
        txtPV = findViewById<EditText>(R.id.txtPrecioVentaArticulo)
        txtExistencia = findViewById<EditText>(R.id.txtExistenciaArticulo)
        cmbUM = findViewById<Spinner>(R.id.cmbUnidadMedidaArticulo)
        cmbCategoria = findViewById(R.id.cmbCategoriaArticulo)
        cmbStatus = findViewById(R.id.cmbStatusArticulo)
        imgNuevaCategoria = findViewById(R.id.imgAgregaNuevaCategoriaAlta)
        imgNuevoArticulo = findViewById(R.id.imgNuevoArticuloAlta)
        imgRegresarArticulo = findViewById(R.id.imgRegresarArticuloAlta)
        imgFoto = findViewById(R.id.imgFotoArticulo)

        imgNuevoArticulo.isVisible = false
        bllUtil = BLLUtil()
        nombreFotoArchivo = ""
        cmbStatus.isEnabled = false
        nuevo = false

        lblEncArticulo.setText("Articulo -> Nuevo")

        val btnTomarFoto = findViewById<Button>(R.id.btnTomarFotoArticulo)
        val btnGuardaArticulo = findViewById<ImageView>(R.id.imgGuardarArticulo)
        val btnCerrar = findViewById<Button>(R.id.btnCerrarArticulo)

        var activo = Status(1,null, "Activo")
        var inactivo = Status(0,null, "Inactivo")
        val valores = mutableListOf(activo, inactivo)
        val transform: (Status) -> (ItemSpinner) = {
            ItemSpinner(it.IdStatus.toInt(), it.Nombre)
        }
        val result = valores.map(transform).toList()
        val adapterStatus = SpinnerAdapter(
            this, result, R.layout.item_spinner_status,
            R.id.lblDisplayStatus
        )
        cmbStatus.adapter = adapterStatus

        btnTomarFoto.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("nombreFotoArchivo", nombreFotoArchivo)
            startForResult.launch(intent)
        }
        btnGuardaArticulo.setOnClickListener {
            guardaArticulo()
        }
        imgRegresarArticulo.setOnClickListener {
            finish()
        }
        imgNuevoArticulo.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pregunta")
            builder.setMessage("Hay un Articulo modificandose. Aun asi desea continuar?")
                .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->

                    imgNuevoArticulo.isVisible = false
                    limpiarControles()
                })
                .setNegativeButton("CANCELAR", { dialog, id ->

                })
            builder.show()
        }
        btnCerrar.setOnClickListener {
            finish()
        }
        imgNuevaCategoria.setOnClickListener {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.categoria_dialog)

            val txtNombreD: EditText = dialog.findViewById(R.id.txtNombreCategoriaDialog)
            val chkActivaD: CheckBox = dialog.findViewById(R.id.chkActivaCategoriaDialog)
            val chkPredeterminadaD: CheckBox =
                dialog.findViewById(R.id.chkPredeterminadaCategoriaDialog)
            val btnGuardarD: ImageView = dialog.findViewById(R.id.btnGuardarCategoriaDialog)
            val btnCerrarD: Button = dialog.findViewById(R.id.btnCerrarCategoriaDialog)

            chkActivaD.isChecked = true
            chkActivaD.isEnabled = false
            txtNombreD.requestFocus()

            btnGuardarD.setOnClickListener {
                val nombre = txtNombreD.text.toString()
                if (nombre.isEmpty()) {

                } else {
                    val find = base.categoriaDAO().getByNombre(nombre)
                    if (find.value == null) {
                        val scope = MainScope()

                        fun asyncFun() = scope.launch()
                        {
                            base.categoriaDAO().insert(
                                Categoria(
                                    Nombre = nombre,
                                    Activa = chkActivaD.isChecked,
                                    Predeterminada = chkPredeterminadaD.isChecked
                                )
                            )

                            dialog.dismiss()

                            actualizaComboCategorias()
                        }
                        asyncFun()
                    } else {
                        Toast.makeText(
                            applicationContext, "La Categoria ya existe en el sistema",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            btnCerrarD.setOnClickListener {
                dialog.dismiss()
                actualizaComboCategorias()
            }

            dialog.show()
        }

        muestraUnidadesMedida()
        muestraCategorias()

        txtNombre.requestFocus()
    }

    private fun actualizaComboCategorias()
    {
        muestraCategorias()
    }

    private fun muestraUnidadesMedida() {
        var idEmpresa:Int?
        idEmpresa=ParametrosSistema.cfg.IdEmpresa!!.toInt()
        GlobalScope.launch {
            val lista = base.unidadMedidaDAO().getAll(idEmpresa,null)

            runOnUiThread {
                lista.observe(this@articulo_alta_activity)
                {
                    try {
                        listaUnidadesMedida = arrayListOf()
                        listaUnidadesMedida = it

                        val transform: (UnidadMedida) -> (ItemSpinner) = {
                            ItemSpinner(it.IdUnidadMedida.toInt(), it.Nombre)
                        }
                        val result = listaUnidadesMedida.map(transform).toList()

                        val adapterStatus = SpinnerAdapter(
                            applicationContext, result, R.layout.item_spinner_status,
                            R.id.lblDisplayStatus
                        )
                        cmbUM.adapter = adapterStatus

                        cargaDatosEnviadosActivity()
                    }
                    catch (ex:Exception)
                    {
                        val cad=ex.toString()
                    }
                }
            }
        }
    }

    private fun muestraCategorias() {
        GlobalScope.launch {
            val lista = base.categoriaDAO().getAll(true)

            runOnUiThread {
                lista.observe(this@articulo_alta_activity)
                {
                    try {
                        listaCategorias = arrayListOf()
                        listaCategorias = it

                        val transform: (Categoria) -> (ItemSpinner) = {
                            ItemSpinner(it.IdCategoria.toInt(), it.Nombre)
                        }
                        val result = listaCategorias.map(transform).toList()

                        val adapterCategoria = SpinnerAdapter(
                            applicationContext, result, R.layout.item_spinner_status,
                            R.id.lblDisplayStatus
                        )
                        cmbCategoria.adapter = adapterCategoria
                    }
                    catch (ex:Exception)
                    {
                        val cad=ex.toString()
                    }
                    finally {
                        revisaDatosArticuloConsultado()
                    }
                }
            }
        }
    }

    private fun revisaDatosArticuloConsultado() {
        if(intent.extras?.isEmpty==false)
        {
            tipoArticulo= intent.extras?.getInt("tipoArticulo",1)!!
            nuevo= intent.extras?.getBoolean("nuevo",false)!!
            if(nuevo) {

            }
            else {
                txtNombre.setText(intent.extras?.getString("nombre", ""))
                txtCB.setText(intent.extras?.getString("codigoBarra", ""))

                var pv:Float=intent.extras?.getFloat("precioVenta", 0F)!!
                if(pv>0F) txtPV.setText(pv.toString())
                var exis:Float=intent.extras?.getFloat("existencia", 0F)!!
                if(exis>0F)txtExistencia.setText(exis.toString())

                idArticulo = intent.extras?.getInt("idArticulo", 0)!!
                var idUnidadMedida: Short = intent.extras?.getShort("idUnidadMedida", 0)!!
                var idStatus: Int = intent.extras?.getInt("idStatus", 0)!!
                var idCategoria: Int = intent.extras?.getInt("idCategoria", 0)!!
                nombreFotoArchivo = intent.extras?.getString("nombreFotoArchivo")!!

                if (nombreFotoArchivo.isNotEmpty()) {
                    val bllUtil = BLLUtil()
                    val bitmap = bllUtil.getBitmapFromFilename(nombreFotoArchivo)
                    imgFoto.setImageBitmap(bitmap)
                }

                cmbStatus.setSelection(0)
                if (idStatus == 0)
                    cmbStatus.setSelection(1)

                var pos: Int = -1
                for (i in 0..(cmbCategoria.count - 1)) {
                    val find = cmbCategoria.getItemAtPosition(i) as ItemSpinner
                    if (find.Valor == idCategoria) {
                        pos = i.toInt()
                        break
                    }
                }
                imgNuevoArticulo.isVisible = true

                if (pos >= 0)
                    cmbCategoria.setSelection(pos)
            }
        }
    }

    private fun cargaDatosEnviadosActivity() {
        if(intent.extras?.isEmpty==false)
        {
            nuevo= intent.extras?.getBoolean("nuevo",false)!!

            txtNombre.setText(intent.extras?.getString("nombre"))
            txtCB.setText(intent.extras?.getString("codigoBarra"))
            var pv:Float=intent.extras?.getFloat("precioVenta")!!
            if(pv>0F)txtPV.setText(pv.toString())

            var exis:Float=intent.extras?.getFloat("existencia")!!
            if(exis>0F)txtExistencia.setText(exis.toString())

            var idUnidadMedida:Int=intent.extras?.getInt("idUnidadMedida")!!
            var idStatus:Int=intent.extras?.getInt("idStatus")!!

            cmbStatus.isEnabled=true
            cmbStatus.setSelection(0)
            if(nuevo) cmbStatus.isEnabled=false
            else if(idStatus==0) cmbStatus.setSelection(1)

            var pos:Int=-1
            for (i in 0..listaUnidadesMedida.count()-1)
            {
                val um:UnidadMedida=listaUnidadesMedida.get(i)
                if(um.IdUnidadMedida==idUnidadMedida)
                {
                    pos=i
                    break
                }
            }
            if(pos>-1)
                cmbUM.setSelection(pos)

            idArticulo=intent.extras?.getInt("idArticulo")!!
        }
    }

    private fun guardaArticulo() {
        val precioVentaCad=txtPV.text.toString()
        var precioVenta:Float=0F
        var selCat:ItemSpinner?=null

        if(cmbCategoria.selectedItem!=null)
           selCat= cmbCategoria.selectedItem as ItemSpinner

        if (precioVentaCad.isEmpty()==false)
            precioVenta = precioVentaCad.toFloat();
        if (txtNombre.text.toString().length < 5) {
            txtNombre.error="Debe suministrar un nombre mayor a 5 caracteres"
        }
        else if(txtPV.text.toString().isEmpty())
        {
            txtPV.error="Debe suministrar el Precio de venta del Articulo"
        }
        else if(selCat==null)
        {
             lblCategoria.error="Debe suministrar una Categoria valida"
        }
        else if(precioVenta<=0F)
        {
            txtPV.error="Debe suministrar un Precio de venta valido mayor a Cero"
        }
        else{
            val existencia=txtExistencia.text.trim().toString().toFloat();
            val selStatus= cmbStatus.selectedItem as ItemSpinner
            val idStatus=selStatus.Valor
            val selUM= cmbUM.selectedItem as ItemSpinner
            val idUnidadMedida=selUM.Valor
            val manejaCB=txtCB.text.trim().length>0
            //val fechaAlta=Timestamp( System.currentTimeMillis())
            val codigoBarra=txtCB.text.toString()
            val nombre=txtNombre.text.toString()
            var nombreCorto=nombre
            val idCategoria:Int=selCat.Valor
            val idTipoProducto=1
            var existe:Boolean=false

            if(nombreCorto.length>30)
                nombreCorto=nombreCorto.substring(0,30)

            if(idArticulo==0) {
                val find = base.articuloDAO().getByNombreOrCodigoBarra(nombre, codigoBarra)
                find.observe(this@articulo_alta_activity, Observer {
                    existe = if (it != null) true else false
                    if (existe) {
                        Toast.makeText(
                            applicationContext,
                            "El articulo con Codigo=$codigoBarra o Nombre=$nombre ya existe",
                            Toast.LENGTH_LONG
                        ).show()
                    } else
                    {
                        lifecycleScope.launch  {
                            val id:Long=base.articuloDAO().insert(
                                Articulo(
                                    Nombre = nombre,
                                    PrecioVenta = precioVenta,
                                    Existencia = existencia,
                                    IdStatus = idStatus,
                                    IdCategoria = idCategoria,
                                    CodigoBarra = codigoBarra,
                                    ManejaCodigoBarra = manejaCB,
                                    IdUnidadMedida = idUnidadMedida,
                                    NombreArchivoFoto = nombreFotoArchivo
                                )
                            )

                            Toast.makeText(
                                this@articulo_alta_activity,
                                "Se ha guardado el Articulo en el sistema",
                                Toast.LENGTH_LONG
                            ).show()

                            if(nuevo)
                            {
                                intent.putExtra("agrega",true)
                                intent.putExtra("nombre",nombre)
                                intent.putExtra("cantidad",1F)
                                intent.putExtra("precioVenta",precioVenta)
                                intent.putExtra("idArticulo",id.toInt())
                                setResult(RESULT_OK, intent)
                                finish()
                            }
                            else
                                limpiarControles()
                        }
                    }
                })
            }
            else
            {
                lifecycleScope.launch  {
                    base.articuloDAO().update(
                        Articulo(
                            IdArticulo = idArticulo,
                            Nombre = nombre,
                            PrecioVenta = precioVenta,
                            Existencia = existencia,
                            IdStatus = idStatus,
                            IdCategoria = idCategoria,
                            CodigoBarra = codigoBarra,
                            ManejaCodigoBarra = manejaCB,
                            IdUnidadMedida = idUnidadMedida,
                            NombreArchivoFoto = nombreFotoArchivo
                        )
                    )

                    Toast.makeText(
                        this@articulo_alta_activity,
                        "Se ha actualizado el Articulo en el sistema",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent()
                    intent.putExtra( "actualiza", true)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    private fun limpiarControles() {
        txtNombre.text?.clear()
        txtCB.text?.clear()
        txtPV.text?.clear()
        txtExistencia.text?.clear()

        imgNuevoArticulo.isVisible=false
        cmbStatus.isEnabled=false
        cmbStatus.setSelection(0)
        cmbUM.setSelection(0)
        lblEncArticulo.setText("Articulo -> Nuevo")
        imgFoto.setImageBitmap(null)

        txtNombre.requestFocus()
    }

    private val startForResult=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode== Activity.RESULT_OK)
        {
            val intent=result.data

            nombreFotoArchivo=intent?.extras?.getString("nombreFotoArchivo")!!

            if(nombreFotoArchivo.isNotEmpty()==true) {
                val bitmap = bllUtil.getBitmapFromFilename(nombreFotoArchivo)
                imgFoto.setImageBitmap(bitmap)
            }
        }
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private fun writeToFile(scaledBitmap: Bitmap,f:File): String {
        f.createNewFile();
        val bos: ByteArrayOutputStream = ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);

        val fos: FileOutputStream = FileOutputStream(f)
        fos.write(bos.toByteArray());
        fos.flush();
        fos.close();

        return f.absolutePath
    }

    private fun GuardarImagen() {
        try {
            val cw=ContextWrapper(applicationContext)
            val path = cw.getDir("imgNegocioMX", Context.MODE_PRIVATE)

            var fOut: OutputStream? = null
            val counter = Random.nextInt()
            var nombreArchivo="articulo_$counter.jpg"
            if(nombreFotoArchivo.isNotEmpty())
                nombreArchivo=nombreFotoArchivo

            val file = File(path,nombreArchivo)
            nombreFotoArchivo=file.absolutePath

            val ancho=bitmap.width
            val alto=bitmap.height

            bitmap= bitmap.rotate(90F)

            imgFoto.setImageBitmap(bitmap)

            writeToFile(bitmap,file)
        }
        catch (ex:Exception)
        {
            ex.printStackTrace()
        }
    }
}
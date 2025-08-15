/*package com.example.negociomx_pos

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
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
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.negociomx_pos.BE.ArticuloActNube
import com.example.negociomx_pos.BE.ArticuloNube
import com.example.negociomx_pos.BE.CategoriaNube
import com.example.negociomx_pos.BE.UsuarioNube
import com.example.negociomx_pos.DAL.DALArticulo
import com.example.negociomx_pos.DAL.DALCategoria
import com.example.negociomx_pos.DAL.DALUnidadMedida
import com.example.negociomx_pos.DAL.DALUsuario
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.adapters.SpinnerAdapter
import com.example.negociomx_pos.databinding.ActivityArticuloNubeBinding
import com.example.negociomx_pos.room.BLL.BLLUtil
import com.example.negociomx_pos.room.entities.Categoria
import com.example.negociomx_pos.room.entities.ItemSpinner
import com.example.negociomx_pos.room.entities.Status
import com.example.negociomx_pos.room.entities.UnidadMedida
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.random.Random

class articulo_nube_activity : AppCompatActivity() {
    lateinit var binding:ActivityArticuloNubeBinding
    var tipoArticulo:Int=1

    lateinit var dalArt: DALArticulo
    lateinit var dalCat:DALCategoria
    lateinit var dalUM:DALUnidadMedida
    lateinit var bllUtil: BLLUtil
    lateinit var dalUsu:DALUsuario

    var idArticulo:Int=0

    var nuevo:Boolean=false

    lateinit var nombreFotoArchivo:String
    lateinit var bitmap: Bitmap
    lateinit var listaUsuariosNube:List<UsuarioNube>
    lateinit var listaCategorias:List<Categoria>
    lateinit var listaUnidadesMedida:List<UnidadMedida>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityArticuloNubeBinding.inflate((layoutInflater))
        setContentView(binding.root)

        tipoArticulo=1
        dalCat=DALCategoria()
        dalArt=DALArticulo()
        dalUsu=DALUsuario()
        dalUM= DALUnidadMedida()
        nombreFotoArchivo=""
        bllUtil= BLLUtil()

        leeUsuariosNube()
        binding.apply {
            imgNuevoArticuloAltaNube.isVisible = false
            cmbStatusArticuloNube.isEnabled = false
            nuevo = false

            lblEncabezadoAltaArticuloNube.setText("Articulo -> Nuevo")

            binding.pbGuardarArticuloNube.isVisible=false
            var activo = Status(1, null,"Activo")
            var inactivo = Status(0, null,"Inactivo")
            val valores = mutableListOf(activo, inactivo)
            val transform: (Status) -> (ItemSpinner) = {
                ItemSpinner(it.IdStatus.toInt(), it.Nombre)
            }
            val result = valores.map(transform).toList()
            val adapterStatus = SpinnerAdapter(
                applicationContext, result, R.layout.item_spinner_status,
                R.id.lblDisplayStatus
            )
            cmbStatusArticuloNube.adapter = adapterStatus

            btnTomarFotoArticuloNube.setOnClickListener {
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.putExtra("nombreFotoArchivo", nombreFotoArchivo)
                startForResult.launch(intent)
            }
            imgGuardarArticuloNube.setOnClickListener {
                guardaArticuloNube()
            }
            imgRegresarArticuloAltaNube.setOnClickListener {
                finish()
            }
            imgNuevoArticuloAltaNube.setOnClickListener {
                val builder = AlertDialog.Builder(applicationContext)
                builder.setTitle("Pregunta")
                builder.setMessage("Hay un Articulo modificandose. Aun asi desea continuar?")
                    .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->

                        imgNuevoArticuloAltaNube.isVisible = false
                        limpiarControles()
                    })
                    .setNegativeButton("CANCELAR", { dialog, id ->

                    })
                builder.show()
            }

            imgAgregaNuevaCategoriaAltaNube.setOnClickListener {
                try {
                    val dialog = Dialog(this@articulo_nube_activity)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setContentView(R.layout.categoria_dialog)

                    val txtNombreD: EditText = dialog.findViewById(R.id.txtNombreCategoriaDialog)
                    val chkActivaD: CheckBox = dialog.findViewById(R.id.chkActivaCategoriaDialog)
                    val chkPredeterminadaD: CheckBox =
                        dialog.findViewById(R.id.chkPredeterminadaCategoriaDialog)
                    val btnGuardarCategoriaNueba: ImageView =
                        dialog.findViewById(R.id.btnGuardarCategoriaDialog)
                    val btnCerrarD: Button = dialog.findViewById(R.id.btnCerrarCategoriaDialog)

                    chkActivaD.isChecked = true
                    chkActivaD.isEnabled = false
                    txtNombreD.requestFocus()

                    btnGuardarCategoriaNueba.setOnClickListener {
                        val nombre = txtNombreD.text.toString()
                        var idEmpresaNube = ParametrosSistema.cfg.IdEmpresa

                        if (nombre.isEmpty() == true) {
                        } else {
                            var categoria = CategoriaNube(
                                "0", null, nombre, chkActivaD.isChecked,
                                chkPredeterminadaD.isChecked, idEmpresaNube, 0, "1111000000"
                            )

                            dalCat.insert(categoria) { res ->
                                if (res != "") {
                                    dialog.dismiss()

                                    actualizaComboCategorias()
                                }
                            }
                        }
                    }
                    btnCerrarD.setOnClickListener {
                        dialog.dismiss()
                        actualizaComboCategorias()
                    }

                    dialog.show()
                }
                catch (ex:Exception)
                {
                    var mensaje=ex.toString()
                    if(mensaje.isNotEmpty())
                    {
                        Toast.makeText(
                            applicationContext,"Se ha guardado la Categoria en la Nube corrctamente",
                            Toast.LENGTH_LONG
                        ).show()

                        finish()
                    }
                }
            }

            txtNombreArticuloNube.requestFocus()
        }

        muestraUnidadesMedida()
        muestraCategorias()
    }

    private fun leeUsuariosNube() {
        listaUsuariosNube= arrayListOf()

        var idEmpresaNube=ParametrosSistema.cfg.IdEmpresa
        dalUsu.getAllUsuarios(idEmpresaNube,true){
            res, mensajeError->
            if(res!=null)
                listaUsuariosNube=res
        }
    }

    private fun actualizaComboCategorias()
    {
        muestraCategorias()
    }

    private fun muestraUnidadesMedida() {
        val idEmpresa=ParametrosSistema.cfg.IdEmpresa
        dalUM.getAllByIdEmpresa(idEmpresa){
            res->
            if(res!=null)
            {
                var adapter= bllUtil.convertListUnidadMedidaToListSpinner(applicationContext, res)
                binding.cmbUnidadMedidaArticuloNube.adapter=adapter
            }
        }
    }

    private fun muestraCategorias() {
        val idEmpresa=ParametrosSistema.cfg.IdEmpresa
        dalCat.getAll(idEmpresa,true) { res ->
            if (res != null) {
                var adapter:android.widget.SpinnerAdapter= bllUtil.convertListCategoriaToListSpinner(applicationContext, res)
                binding.cmbCategoriaArticuloNube.adapter = adapter
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
                binding.txtNombreArticuloNube.setText(intent.extras?.getString("nombre", ""))
                binding.txtCodigoBarraArticuloNube.setText(intent.extras?.getString("codigoBarra", ""))

                var pv:Float=intent.extras?.getFloat("precioVenta", 0F)!!
                if(pv>0F) binding.txtPrecioVentaArticuloNube.setText(pv.toString())
                var exis:Float=intent.extras?.getFloat("existencia", 0F)!!
                if(exis>0F)binding.txtExistenciaArticuloNube.setText(exis.toString())

                idArticulo = intent.extras?.getInt("idArticulo", 0)!!
                var idUnidadMedida: Short = intent.extras?.getShort("idUnidadMedida", 0)!!
                var idStatus: Int = intent.extras?.getInt("idStatus", 0)!!
                var idCategoria: Int = intent.extras?.getInt("idCategoria", 0)!!
                nombreFotoArchivo = intent.extras?.getString("nombreFotoArchivo")!!

                if (nombreFotoArchivo.isNotEmpty()) {
                    val bllUtil = BLLUtil()
                    val bitmap = bllUtil.getBitmapFromFilename(nombreFotoArchivo)
                    binding.imgFotoEncArticuloNube.setImageBitmap(bitmap)
                }

                binding.cmbStatusArticuloNube.setSelection(0)
                if (idStatus == 0)
                    binding.cmbStatusArticuloNube.setSelection(1)

                var pos: Int = -1
                for (i in 0..(binding.cmbCategoriaArticuloNube.count - 1)) {
                    val find = binding.cmbCategoriaArticuloNube.getItemAtPosition(i) as ItemSpinner
                    if (find.Valor == idCategoria) {
                        pos = i.toInt()
                        break
                    }
                }
                binding.imgFotoArticuloNube.isVisible = true

                if (pos >= 0)binding.cmbCategoriaArticuloNube.setSelection(pos)
            }
        }
    }

    private fun cargaDatosEnviadosActivity() {
        if(intent.extras?.isEmpty==false)
        {
            nuevo= intent.extras?.getBoolean("nuevo",false)!!

            binding.txtNombreArticuloNube.setText(intent.extras?.getString("nombre"))
            binding.txtCodigoBarraArticuloNube.setText(intent.extras?.getString("codigoBarra"))
            var pv:Float=intent.extras?.getFloat("precioVenta")!!
            if(pv>0F)binding.txtPrecioVentaArticuloNube.setText(pv.toString())

            var exis:Float=intent.extras?.getFloat("existencia")!!
            if(exis>0F)binding.txtExistenciaArticuloNube.setText(exis.toString())

            var idUnidadMedida:Int=intent.extras?.getInt("idUnidadMedida")!!
            var idStatus:Int=intent.extras?.getInt("idStatus")!!

            binding.cmbStatusArticuloNube.isEnabled=true
            binding.cmbStatusArticuloNube.setSelection(0)
            if(nuevo) binding.cmbStatusArticuloNube.isEnabled=false
            else if(idStatus==0) binding.cmbStatusArticuloNube.setSelection(1)

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
            if(pos>-1)binding.cmbUnidadMedidaArticuloNube.setSelection(pos)

            idArticulo=intent.extras?.getInt("idArticulo")!!
        }
    }

    private fun guardaArticuloNube() {
        val precioVentaCad=binding.txtPrecioVentaArticuloNube.text.toString()
        var precioVenta:Float=0F
        var selCat: ItemSpinner?=null

        if(binding.cmbCategoriaArticuloNube.selectedItem!=null)
            selCat= binding.cmbCategoriaArticuloNube.selectedItem as ItemSpinner

        if (precioVentaCad.isEmpty()==false)
            precioVenta = precioVentaCad.toFloat();
        if (binding.txtNombreArticuloNube.text.toString().length < 5) {
            binding.txtNombreArticuloNube.error="Debe suministrar un nombre mayor a 5 caracteres"
        }
        else if(binding.txtPrecioVentaArticuloNube.text.toString().isEmpty())
        {
            binding.txtPrecioVentaArticuloNube.error="Debe suministrar el Precio de venta del Articulo"
        }
        else if(selCat==null)
        {
            binding.lblCategoriaAltaArticuloNube.error="Debe suministrar una Categoria valida"
        }
        else if(precioVenta<=0F)
        {
            binding.txtPrecioVentaArticuloNube.error="Debe suministrar un Precio de venta valido mayor a Cero"
        }
        else{
            val existencia=binding.txtExistenciaArticuloNube.text.trim().toString().toFloat();
            val selStatus= binding.cmbStatusArticuloNube.selectedItem as ItemSpinner
            val idStatus=selStatus.Valor.toString()
            val selUM= binding.cmbUnidadMedidaArticuloNube.selectedItem as ItemSpinner
            val idUnidadMedida=selUM.Valor.toString()
            val manejaCB=binding.txtCodigoBarraArticuloNube.text.trim().length>0
            val codigoBarra=binding.txtCodigoBarraArticuloNube.text.toString()
            val nombre=binding.txtNombreArticuloNube.text.toString()
            var nombreCorto=nombre
            val idCategoria:String=selCat.Valor.toString()
            val idTipoProducto:Int=1
            var existe:Boolean=false

            if(nombreCorto.length>30) nombreCorto=nombreCorto.substring(0,30)

            if(idArticulo==0) {
                if(nombreCorto.length>30)nombreCorto=nombreCorto.substring(0,30)
                var idMarca:String?=null
                var articulo:ArticuloNube
                var clave:String=""
                val idEmpresaNube:String=ParametrosSistema.cfg.IdEmpresa.toString()

                articulo=ArticuloNube("0","0",nombre,clave,nombreCorto,idUnidadMedida,codigoBarra,"",
                    idMarca,idCategoria,existencia ,0F,0F,0,0,
                    0,false,0F,precioVenta,0F,0F,
                    0F,"",idStatus,idTipoProducto,manejaCB,idEmpresaNube)

                binding.pbGuardarArticuloNube.isVisible=true
                desactivaControlesGuardar(false)
                dalArt.insert(articulo){
                        res->
                    if(res!="")
                    {
                        if(listaUsuariosNube!=null && listaUsuariosNube.count()>0)
                        {
                            listaUsuariosNube.forEach{
                                    a->
                                var idArticuloNube=res
                                var idUsuario=a.Id!!
                                var act=ArticuloActNube("0",false,"","",
                                    idArticuloNube,idEmpresaNube,idUsuario)
                                dalArt.insertArtAct(act){
                                        res1->
                                    if(res1!=null)
                                    {
                                        Toast.makeText(
                                            applicationContext,"El articulo:"+ nombre+" se ha guardado correctamente en la Nube.",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        limpiarControles()
                                    }
                                }
                            }

                            binding.pbGuardarArticuloNube.isVisible=true
                            desactivaControlesGuardar(true)
                        }
                        else
                        {
                            binding.pbGuardarArticuloNube.isVisible=true
                            desactivaControlesGuardar(true)
                        }
                    }
                    else
                    {
                        binding.pbGuardarArticuloNube.isVisible=true
                        desactivaControlesGuardar(true)
                    }
                }
            }
            else
            {
            }
        }
    }

    private fun desactivaControlesGuardar(activarDesactivar:Boolean) {
        if(activarDesactivar) {
            binding.imgGuardarArticuloNube.isVisible = false
            binding.imgNuevoArticuloAltaNube.isEnabled = false
            binding.imgRegresarArticuloAltaNube.isEnabled = false
        }
        else
        {
            binding.imgGuardarArticuloNube.isVisible = true
            binding.imgNuevoArticuloAltaNube.isEnabled = true
            binding.imgRegresarArticuloAltaNube.isEnabled = true
        }
    }

    private fun limpiarControles() {
        binding.apply {
            txtNombreArticuloNube.text?.clear()
            txtCodigoBarraArticuloNube.text?.clear()
            txtPrecioVentaArticuloNube.text?.clear()
            txtExistenciaArticuloNube.text?.clear()

            imgNuevoArticuloAltaNube.isVisible = false
            cmbStatusArticuloNube.isEnabled = false
            cmbStatusArticuloNube.setSelection(0)
            cmbUnidadMedidaArticuloNube.setSelection(0)
            lblEncabezadoAltaArticuloNube.setText("Articulo -> Nuevo")
            imgFotoArticuloNube.setImageBitmap(null)
            pbGuardarArticuloNube.isVisible=false

            binding.txtNombreArticuloNube.requestFocus()
        }
    }

    private val startForResult=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode== Activity.RESULT_OK)
        {
            val intent=result.data

            nombreFotoArchivo=intent?.extras?.getString("nombreFotoArchivo")!!

            if(nombreFotoArchivo.isNotEmpty()==true) {
                val bitmap = bllUtil.getBitmapFromFilename(nombreFotoArchivo)
                binding.imgFotoArticuloNube.setImageBitmap(bitmap)
            }
        }
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private fun writeToFile(scaledBitmap: Bitmap,f: File): String {
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
            val cw= ContextWrapper(applicationContext)
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

            binding.imgFotoEncArticuloNube.setImageBitmap(bitmap)

            writeToFile(bitmap,file)
        }
        catch (ex:Exception)
        {
            ex.printStackTrace()
        }
    }
}*/
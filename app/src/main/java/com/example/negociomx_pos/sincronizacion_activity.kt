package com.example.negociomx_pos

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.Window
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.negociomx_pos.BE.DispositivoAcceso
import com.example.negociomx_pos.BE.DispositivoAccesoUnico
import com.example.negociomx_pos.BE.EmpresaDispositivoAcceso
import com.example.negociomx_pos.BE.EmpresaNube
import com.example.negociomx_pos.BE.Intento
import com.example.negociomx_pos.DAL.DALDispotivioAcceso
import com.example.negociomx_pos.DAL.DALEmpresa
import com.example.negociomx_pos.adapters.DispositivoAccesoAdapter
import com.example.negociomx_pos.adapters.SpinnerAdapter
import com.example.negociomx_pos.databinding.ActivitySincronizacionBinding
import com.example.negociomx_pos.room.entities.ItemSpinner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class sincronizacion_activity : AppCompatActivity() {

    private var dal=DALDispotivioAcceso()
    private var dalEmp=DALEmpresa()
    lateinit var binding:ActivitySincronizacionBinding
    var dispositivoConsultado:DispositivoAcceso?=null

    lateinit var firebaseAuth:FirebaseAuth
    lateinit var authStateListener: AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        binding=ActivitySincronizacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEmpresasSincronizacion.setOnClickListener{
            val intent= Intent(this,empresa_nube_activity::class.java   )
            intent.putExtra("nombreFotoArchivo","")
            startForResult.launch(intent)
        }
        binding.btnRegresarSincronizacion.setOnClickListener{
            finish()
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_PHONE_STATE)) {
            } else { ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_PHONE_STATE), 2) } }

        loguearUsuario("adm@gmail.com","adm2520MX#..")
    }

    fun loguearUsuario(email:String, pwd:String)
    {
        firebaseAuth.signInWithEmailAndPassword(email, pwd)
            .addOnCompleteListener(this){  task ->
                if(task.isSuccessful)
                {
                    var usuarioLogueado=firebaseAuth.currentUser

                    val idDispositivo=android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID)
                    var imei:String=""
                    try {
                        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                        imei = tm.getImei().toString()
                    }
                    catch (ex:Exception)
                    {

                    }
                    val fecha:LocalDateTime= LocalDateTime.now()
                    val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.US)
                    val fechaIntento: String = dtf.format(fecha)
                    dal.getByIdDispositivo(idDispositivo){
                            res -> run {
                        if(res==null) {
                            dal.insert(
                                DispositivoAcceso(
                                    IdDispositivo = idDispositivo,
                                    FechaAlta = fechaIntento,
                                    Imei = imei,
                                    Activo = false
                                )
                            )
                        }
                        else{
                            dal.insertIntento(Intento(IdDispositivo = res.IdDispositivo, FechaIntento = fechaIntento,)){
                                    res1->run {
                                if(res1.isNotEmpty()==true)
                                {

                                }
                                else{

                                }
                            }
                            }
                        }
                    }
                    }

                    muestraDispositivos()
                }
                else
                {
                    Toast.makeText(applicationContext, "Es necesario loguearse en el Sistema.",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                }
            }
    }

    fun muestraDispositivos()
    {
        dal.getAll({lista -> onFinishListener(lista)})
    }

    private fun onFinishListener(lista: List<DispositivoAccesoUnico>) {
        val adaptador = DispositivoAccesoAdapter(lista) { comando, dispositivo -> onItemSelected(comando, dispositivo) }

        binding.rvDispositivosSync.layoutManager = LinearLayoutManager(applicationContext)
        binding.rvDispositivosSync.adapter = adaptador
    }

    private fun onItemSelected(comando:Int, dispositivo:DispositivoAccesoUnico) {
        if(comando==1)
        {
        }
        else if(comando==2)
        {
            dalEmp.getByFilters(null){
                    res -> run {
                VerDialogoDetallesDispositivo(dispositivo, res!!)
            }
            }
        }
    }

    private fun VerDialogoDetallesDispositivo(dispositivo:DispositivoAccesoUnico, lista:List<EmpresaNube>) {
        val dialog= Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dispositivoacceso_dialog)

        val chkActivo: CheckBox =dialog.findViewById(R.id.chkActivoDispositivoDialog)
        val cmbEmpresa: Spinner =dialog.findViewById(R.id.cmbEmpresaDispositivoDialog)
        val btnGuardar: ImageView =dialog.findViewById(R.id.btnActualizaDatosDispositivoDialog)
        val btnRegresar: ImageView =dialog.findViewById(R.id.btnRegresarDispositivoDialog)

        val transform: (EmpresaNube) -> (ItemSpinner) = {
            ItemSpinner(it.Id!!.toInt(), it.RazonSocial)
        }

        var lista1:List<EmpresaNube>
        var entro=false
        lista1= arrayListOf()

        var contador:Int=0
        var posSeleccionada:Int=-1;
        var  idEmpresaActual=dispositivo.IdEmpresa

        lista.forEach{
            val item=EmpresaNube(Id = it.Id, IdTipoContribuyente = it.IdTipoContribuyente, IdRegimenFiscal = it.IdRegimenFiscal,
                Activa = it.Activa, Email = it.Email, Telefonos = it.Telefonos, Predeterminada = it.Predeterminada,
                PaginaWeb = it.PaginaWeb, CodigoPostal = it.CodigoPostal, Rfc = it.Rfc, RazonSocial = it.RazonSocial,
                NombreComercial = it.NombreComercial)
            if(item!=null)
            {
                if(!entro)
                {
                    lista1.add(EmpresaNube(Id = "", IdRegimenFiscal = "0", IdTipoContribuyente = "0", Activa = true, Email = "",
                        Telefonos = "", Predeterminada = false, PaginaWeb = "", CodigoPostal = 0, Rfc = "", RazonSocial = "Seleccione...",
                        NombreComercial = "Seleccione..."))
                    entro=true
                }
                if(idEmpresaActual==it.Id)posSeleccionada=contador

                lista1.add(item)
            }
            contador++
        }
        if(posSeleccionada>=0)posSeleccionada++

        val result = lista1.map(transform).toList()

        val adapterCategoria = SpinnerAdapter(
            applicationContext, result, R.layout.item_spinner_status,
            R.id.lblDisplayStatus
        )
        cmbEmpresa.adapter = adapterCategoria
        if(posSeleccionada>=0)
            cmbEmpresa.setSelection(posSeleccionada)

        var activoActual=dispositivo.Activo

        if(activoActual!=null)
            chkActivo.isChecked=activoActual==true

        cmbEmpresa.requestFocus()
        btnGuardar.setOnClickListener{
            var idEmpresa:String?=null
            if(cmbEmpresa.selectedItem!=null)
            {
                var selEmp=cmbEmpresa.selectedItem as ItemSpinner
                idEmpresa=selEmp.Valor.toString()
            }
            var activo=chkActivo.isChecked

            if( idEmpresa!=idEmpresaActual || activo!=activoActual)
            {
                if(idEmpresa==null)idEmpresa=null
                val entidad=DispositivoAcceso(IdDispositivo = dispositivo.IdDispositivo, Activo = activo,
                    IdEmpresa = idEmpresa, FechaAlta = dispositivo.FechaAlta, Imei = dispositivo.Imei)
                dal.update(entidad) {
                        res-> run {
                    Toast.makeText(applicationContext, "Se ha Realizado el pago correctamente",
                        Toast.LENGTH_LONG
                    ).show()

                    dialog.dismiss()
                }
                }
            }
        }
        btnRegresar.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
    }

    private val startForResult=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode== Activity.RESULT_OK)
        {
            val intent=result.data

            var idEmpresa:String =intent?.extras?.getString("idEmpresa","")!!

            if(idEmpresa!="") {
                var eda=EmpresaDispositivoAcceso(IdEmpresa = idEmpresa, IdDispositivoAcceso = dispositivoConsultado?.IdDispositivo)

                dalEmp.insertEDA(eda){
                        res-> run{
                    if(res.isNotEmpty()==true)
                    {

                    }
                    else{

                    }
                }
                }
            }
        }
    }
}
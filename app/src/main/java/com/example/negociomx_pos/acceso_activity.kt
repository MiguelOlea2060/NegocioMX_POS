package com.example.negociomx_pos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.negociomx_pos.BE.DispositivoAcceso
import com.example.negociomx_pos.BE.Intento
import com.example.negociomx_pos.BE.UsuarioNube
import com.example.negociomx_pos.DAL.DALDispotivioAcceso
import com.example.negociomx_pos.DAL.DALUsuario
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.Utils.negociomx_posApplication.Companion.prefs
import com.example.negociomx_pos.databinding.ActivityAccesoBinding
import com.example.negociomx_pos.room.BLL.BLLUtil
import com.example.negociomx_pos.room.db.POSDatabase
import com.example.negociomx_pos.room.entities.Admins.Empresa
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class acceso_activity : AppCompatActivity() {
    lateinit var binding: ActivityAccesoBinding
    lateinit var dal:DALDispotivioAcceso
    lateinit var dalUsu:DALUsuario

    lateinit var base: POSDatabase
    lateinit var bllUtil: BLLUtil

    private  val  startForResult=
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {resul->
            if(resul.resultCode== Activity.RESULT_OK)
            {
                var intent=resul.data
                var cerrarSesion= intent?.getBooleanExtra("cerrarSesion",false)!!
                if(cerrarSesion) {
                    finishAffinity()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAccesoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        base = POSDatabase.getDatabase(applicationContext)
        dalUsu=DALUsuario()
        bllUtil=BLLUtil()
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_PHONE_STATE)) {
            } else { ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_PHONE_STATE), 2) } }

        ParametrosSistema.firebaseAuth= FirebaseAuth.getInstance()
        dal=DALDispotivioAcceso()

        var recordarAcceso=prefs.getRecordarAcceso()
        var usernameGuardado:String= prefs.getUsername()
        var pwdGuardado= prefs.getPassword()

        if(recordarAcceso)
        {
            binding.chkRecordarAcceso.isChecked=recordarAcceso
            binding.txtUsuarioEmailAcceso.setText(usernameGuardado)
            binding.txtContrasenaAcceso.setText(pwdGuardado)
        }

        apply {
            binding.lblRegistrarUsuarioAcceso.setOnClickListener{
                registrarUsuarioNuevo()
            }
            binding.btnIngresarAcceso.setOnClickListener {
                var nombreUsuarioEmail=binding.txtUsuarioEmailAcceso.text.toString()
                var pwd =binding. txtContrasenaAcceso.text.toString()
                if (nombreUsuarioEmail.isEmpty() == true) {
                    binding. txtUsuarioEmailAcceso.error="Es necesario suministrar el nombre de Usuario o Email"
                } else if (pwd.isEmpty() == true) {
                    binding. txtContrasenaAcceso.error="Es necesario suministrar la contraseña"
                } else {
                    loguearUsuario(nombreUsuarioEmail, pwd) { usuarioLogueado ->
                        if (usuarioLogueado == true) {
                            prefs.saveRecordarAcceso(binding.chkRecordarAcceso.isChecked)
                            prefs.saveUsername(nombreUsuarioEmail)
                            prefs.savePassword(pwd)

                            ParametrosSistema.usuarioLogueado.IdRol="5"
                            //ParametrosSistema.usuarioLogueado

                            val intent = Intent(applicationContext, menu_principal_activity::class.java)
                            startForResult.launch(intent)
                        } else {
                            bllUtil.MessageShow(this, "El usuario o contraseña son incorrectas", "Aviso") { res -> }
                        }
                    }
                }
            }
        }
    }

    private fun registrarUsuarioNuevo() {
        val intent= Intent(this,usuario_nuevo_activity::class.java)
        startActivity(intent)
    }

    fun loguearUsuario(email:String, pwd:String,onLoginFinish: (Boolean) -> Unit)
    {
        ParametrosSistema.firebaseAuth.signInWithEmailAndPassword(email, pwd)
            .addOnCompleteListener(this){  task ->
                if(task.isSuccessful)
                {
                    getUsuarioNubeByEmail(email) {
                            usuario, logueado->
                        if(usuario!=null) {
                            if(usuario.CuentaVerificada!=true)
                            {
                                bllUtil.MessageShow(this, "La cuenta no se encuentra verificada. Comunicarse con el Administrador", "Aviso")
                                { res -> }
                            }
                            else if(usuario.Activo!=true)
                            {
                                bllUtil.MessageShow(this, "La cuenta no se encuentra Activa. Comunicarse con el Administrador", "Aviso")
                                { res -> }
                            }
                            else {
                                ParametrosSistema.firebaseUser =
                                    ParametrosSistema.firebaseAuth.currentUser!!
                                ParametrosSistema.usuarioLogueado = usuario

                                val idDispositivo = android.provider.Settings.Secure.getString(
                                    contentResolver,
                                    android.provider.Settings.Secure.ANDROID_ID
                                )
                                var imei: String = ""
                                try {
                                    val tm =
                                        getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                                    imei = tm.getImei().toString()
                                } catch (ex: Exception) {

                                }

                                val fecha: LocalDateTime = LocalDateTime.now()
                                val dtf: DateTimeFormatter =
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.US)
                                val fechaIntento: String = dtf.format(fecha)
                                dal.getByIdDispositivo(idDispositivo) { res ->
                                    run {
                                        if (res == null) {
                                            dal.insert(
                                                DispositivoAcceso(
                                                    IdDispositivo = idDispositivo,
                                                    FechaAlta = fechaIntento,
                                                    Imei = imei,
                                                    Activo = false
                                                )
                                            )
                                        } else {
                                            dal.insertIntento(
                                                Intento(
                                                    IdDispositivo = res.IdDispositivo,
                                                    FechaIntento = fechaIntento,
                                                )
                                            ) { res1 ->
                                                run {
                                                    if (res1.isNotEmpty() == true) {

                                                    } else {

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                onLoginFinish(true)
                            }
                        }
                        else
                            onLoginFinish(false)
                    }
                    //onLoginFinish(true)
                }
                else
                    onLoginFinish(false)
            }
    }

    fun getUsuarioNubeByEmail(email: String, onFinishEmailUsuarioNube: (UsuarioNube?, Boolean) -> Unit)
    {
        dalUsu.getUsuarioByEmail(email){
                res->
            if(res==null)
                onFinishEmailUsuarioNube(null, false)
            else
                onFinishEmailUsuarioNube(res, true)
        }
    }
}

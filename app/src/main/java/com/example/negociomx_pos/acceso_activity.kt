package com.example.negociomx_pos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.negociomx_pos.BE.DispositivoAcceso
import com.example.negociomx_pos.BE.Intento
import com.example.negociomx_pos.BE.Usuario
import com.example.negociomx_pos.BE.UsuarioNube
import com.example.negociomx_pos.DAL.DALDispotivioAcceso
import com.example.negociomx_pos.DAL.DALUsuario
import com.example.negociomx_pos.DAL.DALUsuarioSQL
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.Utils.negociomx_posApplication.Companion.prefs
import com.example.negociomx_pos.databinding.ActivityAccesoBinding
import com.example.negociomx_pos.room.BLL.BLLUtil
import com.example.negociomx_pos.room.db.POSDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.xmlbeans.impl.tool.Extension.Param
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class acceso_activity : AppCompatActivity() {
    lateinit var binding: ActivityAccesoBinding
    lateinit var dal:DALDispotivioAcceso
    lateinit var dalUsu:DALUsuario
    lateinit var dalUsuSql:DALUsuarioSQL
    lateinit var base: POSDatabase
    lateinit var bllUtil: BLLUtil


    private var loginInProgress = false
    private val mainHandler = Handler(Looper.getMainLooper())

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
        Log.d("AccesoActivity", "🚀 INICIANDO ACTIVIDAD DE ACCESO")

        base = POSDatabase.getDatabase(applicationContext)
        dalUsu=DALUsuario()
        dalUsuSql=DALUsuarioSQL()
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
                IngresarAcceso()
            }
        }
    }

    private fun IngresarAcceso()
    {
        if (loginInProgress) {
            Log.w("AccesoActivity", "⚠️ Login ya en progreso, ignorando clic")

            return
        }

        Log.d("AccesoActivity", "🔘 BOTÓN INGRESAR PRESIONADO")

        var nombreUsuarioEmail=binding.txtUsuarioEmailAcceso.text.toString()
        var pwd =binding. txtContrasenaAcceso.text.toString()

        Log.d("AccesoActivity", "📧 Email ingresado: '$nombreUsuarioEmail'")
        Log.d("AccesoActivity", "🔐 Password ingresado: '${if(pwd.isNotEmpty()) "***" else "VACÍO"}'")

        if (nombreUsuarioEmail.isEmpty() == true) {
            Log.w("AccesoActivity", "⚠️ Email vacío")
            binding. txtUsuarioEmailAcceso.error="Es necesario suministrar el nombre de Usuario o Email"
        } else if (pwd.isEmpty() == true) {
            Log.w("AccesoActivity", "⚠️ Password vacío")
            binding. txtContrasenaAcceso.error="Es necesario suministrar la contraseña"
        } else {
            Log.d("AccesoActivity", "✅ Datos válidos, iniciando proceso de login")

            // MARCAR LOGIN EN PROGRESO
            loginInProgress = true

            // DESHABILITAR BOTÓN PARA EVITAR MÚLTIPLES CLICS
            binding.btnIngresarAcceso.isEnabled = false
            binding.btnIngresarAcceso.text = "Ingresando..."

            // ✅ TIMEOUT GLOBAL EXTENDIDO A 60 SEGUNDOS
            val loginTimeoutRunnable = Runnable {
                Log.e("AccesoActivity", "⏰ TIMEOUT GLOBAL: Login tardó más de 60 segundos")
                resetLoginUI()
                bllUtil.MessageShow(this, "Tiempo de espera agotado. Verifique su conexión a internet y que el Servidor de base de datos este configurado correctamente.", "Error") { res -> }
            }

            mainHandler.postDelayed(loginTimeoutRunnable, 60000) // 60 segundos

            loguearUsuario(nombreUsuarioEmail, pwd) { usuario, usuarioLogueado ->
                mainHandler.removeCallbacks(loginTimeoutRunnable)

                if (usuarioLogueado == true) {
                    Log.d("AccesoActivity", "🎉 LOGIN EXITOSO")
                    prefs.saveRecordarAcceso(binding.chkRecordarAcceso.isChecked)
                    prefs.saveUsername(nombreUsuarioEmail)
                    prefs.savePassword(pwd)

                    ParametrosSistema.usuarioLogueado=usuario!!
                    ParametrosSistema.usuarioLogueado.IdRol=5

                    mainHandler.post {
                        val intent = Intent(applicationContext, menu_principal_activity::class.java)
                        startForResult.launch(intent)
                        resetLoginUI()
                    }
                } else {
                    Log.e("AccesoActivity", "❌ LOGIN FALLIDO")
                    mainHandler.post {
                        resetLoginUI()
                        bllUtil.MessageShow(this, "El usuario o contraseña son incorrectas, o hay un problema de conectividad con el Servidor de base de datos.", "Aviso") { res -> }
                    }
                }
            }
        }

    }
    private fun resetLoginUI() {
        loginInProgress = false
        binding.btnIngresarAcceso.isEnabled = true
        binding.btnIngresarAcceso.text = "Ingresar"
    }

    private fun registrarUsuarioNuevo() {
        val intent= Intent(this,usuario_nuevo_activity::class.java)
        startActivity(intent)
    }

    fun loguearUsuario(email:String, pwd:String,onLoginFinish: (Usuario?, Boolean) -> Unit)
    {
        //CoroutineScope(Dispatchers.Main).launch {
        GlobalScope.launch{
            try {
                val usuario = dalUsuSql.getUsuarioByEmailAndPassword(email, pwd,)
                Log.d("AccesoActivity", "📊 Resultado búsqueda Database: ${usuario != null}")

                if (usuario != null) {
                    // ✅ VALIDAR ESTADO DEL USUARIO
                    val cuentaVerificada = usuario.CuentaVerificada
                    val activo = usuario.Activo

                    if (cuentaVerificada != true) {
                        Log.w("AccesoActivity", "⚠️ Cuenta no verificada")
                        mainHandler.post {
                            bllUtil.MessageShow(
                                this@acceso_activity,
                                "La cuenta no se encuentra verificada. Comunicarse con el Administrador",
                                "Aviso"
                            ) { res -> }
                        }
                        onLoginFinish(null, false)
                    }

                    if (activo != true) {
                        Log.w("AccesoActivity", "⚠️ Cuenta no activa")
                        mainHandler.post {
                            bllUtil.MessageShow(
                                this@acceso_activity,
                                "La cuenta no se encuentra Activa. Comunicarse con el Administrador",
                                "Aviso"
                            ) { res -> }
                        }
                        onLoginFinish(null, false)
                    }
                    onLoginFinish(usuario, true)
                } else {
                    onLoginFinish(null, false)
                }
            } catch (e: Exception) {
                Log.e("AccesoActivity", "💥 Error en login: ${e.message}")
                bllUtil.MessageShow(this@acceso_activity, "Error de conexión: ${e.message}",
                    "Error") { res -> }
                onLoginFinish(null, false)
            }
        }
    }

    fun getUsuarioNubeByEmail(email: String, onFinishEmailUsuarioNube: (UsuarioNube?, Boolean) -> Unit)
    {
        Log.d("AccesoActivity", "🔍 Buscando usuario por email: '$email'")

        dalUsu.getUsuarioByEmail(email) { res ->
            Log.d("AccesoActivity", "📊 Resultado búsqueda usuario: ${res != null}")

            if (res == null) {
                Log.w("AccesoActivity", "❌ Usuario no encontrado")
                onFinishEmailUsuarioNube(null, false)
            } else {
                Log.d("AccesoActivity", "✅ Usuario encontrado: ${res.Email}")
                onFinishEmailUsuarioNube(res, true)
            }
        }
    }
}

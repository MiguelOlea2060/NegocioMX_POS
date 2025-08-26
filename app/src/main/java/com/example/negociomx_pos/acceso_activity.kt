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
import com.example.negociomx_pos.BE.UsuarioNube
import com.example.negociomx_pos.DAL.DALDispotivioAcceso
import com.example.negociomx_pos.DAL.DALUsuario
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.Utils.negociomx_posApplication.Companion.prefs
import com.example.negociomx_pos.databinding.ActivityAccesoBinding
import com.example.negociomx_pos.room.BLL.BLLUtil
import com.example.negociomx_pos.room.db.POSDatabase
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class acceso_activity : AppCompatActivity() {
    lateinit var binding: ActivityAccesoBinding
    lateinit var dal:DALDispotivioAcceso
    lateinit var dalUsu:DALUsuario

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
                if (loginInProgress) {
                    Log.w("AccesoActivity", "⚠️ Login ya en progreso, ignorando clic")
                    return@setOnClickListener
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
                        bllUtil.MessageShow(this, "Tiempo de espera agotado. Verifique su conexión a internet y que Firebase esté configurado correctamente.", "Error") { res -> }
                    }

                    mainHandler.postDelayed(loginTimeoutRunnable, 60000) // 60 segundos

                    loguearUsuario(nombreUsuarioEmail, pwd) { usuarioLogueado ->
                        // Cancelar timeout global
                        mainHandler.removeCallbacks(loginTimeoutRunnable)

                        Log.d("AccesoActivity", "🏁 Resultado login: $usuarioLogueado")

                        if (usuarioLogueado == true) {
                            Log.d("AccesoActivity", "🎉 LOGIN EXITOSO")
                            prefs.saveRecordarAcceso(binding.chkRecordarAcceso.isChecked)
                            prefs.saveUsername(nombreUsuarioEmail)
                            prefs.savePassword(pwd)

                            ParametrosSistema.usuarioLogueado.IdRol="5"

                            mainHandler.post {
                                val intent = Intent(applicationContext, menu_principal_activity::class.java)
                                startForResult.launch(intent)
                                resetLoginUI()
                            }
                        } else {
                            Log.e("AccesoActivity", "❌ LOGIN FALLIDO")
                            mainHandler.post {
                                resetLoginUI()
                                bllUtil.MessageShow(this, "El usuario o contraseña son incorrectas, o hay un problema de conectividad con Firebase.", "Aviso") { res -> }
                            }
                        }
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

    fun loguearUsuario(email:String, pwd:String,onLoginFinish: (Boolean) -> Unit)
    {
        Log.d("AccesoActivity", "🔐 INICIANDO LOGIN CON URL ESPECÍFICA")
        Log.d("AccesoActivity", "📧 Email: '$email'")

        // ✅ PASO 1: BÚSQUEDA CON URL ESPECÍFICA
        Log.d("AccesoActivity", "🔍 PASO 1: Búsqueda con URL específica")
        getUsuarioNubeByEmail(email) { usuario, logueado ->
            Log.d("AccesoActivity", "📊 Resultado búsqueda Database: ${usuario != null}")

            if (usuario != null) {
                Log.d("AccesoActivity", "✅ Usuario encontrado en Database")
                Log.d("AccesoActivity", "👤 Usuario: ${usuario.Email}")
                Log.d("AccesoActivity", "🟢 Activo: ${usuario.Activo}")
                Log.d("AccesoActivity", "✅ Verificado: ${usuario.CuentaVerificada}")
                Log.d("AccesoActivity", "🆔 ID: ${usuario.Id}")
                Log.d("AccesoActivity", "👤 Nombre: ${usuario.NombreCompleto}")

                // ✅ VALIDAR ESTADO DEL USUARIO
                val cuentaVerificada = usuario.CuentaVerificada
                val activo = usuario.Activo

                if (cuentaVerificada != true) {
                    Log.w("AccesoActivity", "⚠️ Cuenta no verificada")
                    mainHandler.post {
                        bllUtil.MessageShow(this, "La cuenta no se encuentra verificada. Comunicarse con el Administrador", "Aviso") { res -> }
                    }
                    onLoginFinish(false)
                    return@getUsuarioNubeByEmail
                }

                if (activo != true) {
                    Log.w("AccesoActivity", "⚠️ Cuenta no activa")
                    mainHandler.post {
                        bllUtil.MessageShow(this, "La cuenta no se encuentra Activa. Comunicarse con el Administrador", "Aviso") { res -> }
                    }
                    onLoginFinish(false)
                    return@getUsuarioNubeByEmail
                }

                // ✅ PASO 2: AUTENTICAR CON FIREBASE AUTH
                Log.d("AccesoActivity", "🔐 PASO 2: Autenticando con Firebase Auth")
                autenticarConFirebaseAuth(email, pwd, usuario, onLoginFinish)

            } else {
                Log.e("AccesoActivity", "❌ Usuario no encontrado en Database")
                Log.e("AccesoActivity", "💡 Verifica conectividad y configuración de Firebase")
                mainHandler.post {
                    bllUtil.MessageShow(this, "Usuario no encontrado. Verifique su conexión a internet y configuración de Firebase.", "Error") { res -> }
                }
                onLoginFinish(false)
            }
        }
    }

    private fun autenticarConFirebaseAuth(email: String, pwd: String, usuario: UsuarioNube, onLoginFinish: (Boolean) -> Unit) {
        Log.d("AccesoActivity", "🔐 Iniciando Firebase Auth para: $email")

        try {
            // ✅ TIMEOUT PARA FIREBASE AUTH
            val authTimeoutRunnable = Runnable {
                Log.e("AccesoActivity", "⏰ TIMEOUT: Firebase Auth tardó más de 30 segundos")
                mainHandler.post {
                    bllUtil.MessageShow(this, "Timeout en autenticación. Verifique su conexión a internet.", "Error") { res -> }
                }
                onLoginFinish(false)
            }

            mainHandler.postDelayed(authTimeoutRunnable, 30000) // 30 segundos para auth

            ParametrosSistema.firebaseAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this) { task ->
                    // Cancelar timeout de auth
                    mainHandler.removeCallbacks(authTimeoutRunnable)

                    Log.d("AccesoActivity", "📊 Firebase Auth completado. Exitoso: ${task.isSuccessful}")

                    if (task.isSuccessful) {
                        Log.d("AccesoActivity", "🎉 Firebase Auth exitoso para: $email")

                        // CONFIGURAR PARÁMETROS DEL SISTEMA
                        ParametrosSistema.firebaseUser = ParametrosSistema.firebaseAuth.currentUser!!
                        ParametrosSistema.usuarioLogueado = usuario

                        Log.d("AccesoActivity", "✅ Sesión configurada exitosamente")
                        Log.d("AccesoActivity", "👤 Usuario logueado: ${usuario.NombreCompleto}")
                        Log.d("AccesoActivity", "🏢 Rol: ${usuario.IdRol}")

                        onLoginFinish(true)
                    } else {
                        Log.e("AccesoActivity", "❌ Firebase Auth falló: ${task.exception?.message}")
                        mainHandler.post {
                            bllUtil.MessageShow(this, "Error de autenticación: ${task.exception?.message}", "Error") { res -> }
                        }
                        onLoginFinish(false)
                    }
                }
                .addOnFailureListener { exception ->
                    // Cancelar timeout de auth
                    mainHandler.removeCallbacks(authTimeoutRunnable)

                    Log.e("AccesoActivity", "💥 Firebase Auth error: ${exception.message}")
                    mainHandler.post {
                        bllUtil.MessageShow(this, "Error de conexión: ${exception.message}", "Error") { res -> }
                    }
                    onLoginFinish(false)
                }
        } catch (ex: Exception) {
            Log.e("AccesoActivity", "🔥 Excepción en Firebase Auth: ${ex.message}")
            mainHandler.post {
                bllUtil.MessageShow(this, "Error inesperado: ${ex.message}", "Error") { res -> }
            }
            onLoginFinish(false)
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

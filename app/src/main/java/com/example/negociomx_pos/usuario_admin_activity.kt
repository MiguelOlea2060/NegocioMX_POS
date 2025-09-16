package com.example.negociomx_pos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.negociomx_pos.BE.UsuarioNube
import com.example.negociomx_pos.DAL.DALUsuario
import com.example.negociomx_pos.adapters.SpinnerAdapter
import com.example.negociomx_pos.adapters.UsuarioNubeAdapter
import com.example.negociomx_pos.databinding.ActivityUsuarioAdminBinding
import com.example.negociomx_pos.databinding.ActivityUsuariosBinding
import com.example.negociomx_pos.room.BLL.BLLUtil
import com.example.negociomx_pos.room.entities.Admins.Rol
import com.example.negociomx_pos.room.entities.ItemSpinner

class usuario_admin_activity : AppCompatActivity() {
    lateinit var binding:ActivityUsuarioAdminBinding
    lateinit var dalUsu:DALUsuario

    lateinit var listaUsuarios:List<UsuarioNube>
    lateinit var bllUtil:BLLUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityUsuarioAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dalUsu=DALUsuario()
        bllUtil=BLLUtil()

        var listaRoles: List<Rol>
        listaRoles = arrayListOf()

        listaRoles.add(Rol(IdRol = 0, Nombre = "Seleccione..."))
        listaRoles.add(Rol(IdRol = 2, Nombre = "Admin"))
        listaRoles.add(Rol(IdRol = 3, Nombre = "Ventas"))
        listaRoles.add(Rol(IdRol = 4, Nombre = "Supervisor"))
        listaRoles.add(Rol(IdRol = 5, Nombre = "Cliente"))

        var adapter: SpinnerAdapter
        adapter = bllUtil.convertListRolToListSpinner(this, listaRoles)

        binding.apply {
            cmbRolUsuarioUsuarioAdmin.adapter = adapter

            progressAltaUsuarioAdmin.isVisible = false
            chkActivoUsuarioAdmin.isChecked = true
            chkActivoUsuarioAdmin.isEnabled = false
            btnNuevoUsuarioAdmin.isVisible = false
            btnRegresarUsuarioAdmin.setOnClickListener {
                finish()
            }
            btnGuardarUsuarioAlta.setOnClickListener {
                var nombreCompleto = txtNombreCompletoUsuarioUsuarios.text.toString()
                var contrasena = txtContrasenaUsuarioAdmin.text.toString()
                var contrasena1 = txtRepetirContrasenaUsuarioAdmin.text.toString()
                var email = txtEmailUsuarioUsuarioAdmin.text.toString()
                var idEmpresa: String? = null

                var idRol: String? = null
                var selRol = cmbRolUsuarioUsuarioAdmin.selectedItem as ItemSpinner
                if (selRol.Valor != 0) idRol = selRol.Valor.toString()

                if (nombreCompleto.isEmpty() == true)
                    txtEmailUsuarioUsuarioAdmin.error = "Debe suministrar el Nombre completo"
                else if (email.isEmpty() == true)
                    txtEmailUsuarioUsuarioAdmin.error = "Debe suministrar un Email"
                else if (contrasena.isEmpty() || contrasena1.isEmpty())
                    txtContrasenaUsuarioAdmin.error = "La contraseñas no deben estar vacias"
                else if (!contrasena.equals(contrasena1))
                    txtContrasenaUsuarioAdmin.error = "Las contraseñas no coinciden"
                else if (contrasena.length <= 5)
                    txtContrasenaUsuarioAdmin.error = "La debe ser minimo de 6 catacteres"
                else if (idRol == null || idRol.isEmpty() == true)
                    lblRolUsuarioAdmin.error = "Es necesario seleccionar un Rol"
                else {
                    dalUsu.getUsuarioByEmail(email) { res: UsuarioNube? ->
                        if (res != null) {
                            bllUtil.MessageShow(
                                this@usuario_admin_activity, "El correo ya existe en el Sistema",
                                "Aviso"
                            ) {}
                            txtEmailUsuarioUsuarioAdmin.requestFocus()
                        } else {
                            btnGuardarUsuarioAlta.isVisible = false

                            progressAltaUsuarioAdmin.isVisible = true

                            var activo = chkActivoUsuarioAdmin.isChecked

                            var usuario = UsuarioNube(
                                IdEmpresa = idEmpresa,
                                IdRol = idRol,
                                NombreCompleto = nombreCompleto,
                                Email = email,
                                CuentaVerificada = false,
                                Password = contrasena,
                                Activo = activo
                            )
                            dalUsu.insert(usuario) { insertResult: String ->
                                runOnUiThread {
                                    limpiaControles()
                                }
                            }
                        }
                    }
                }
            }
        }
        muestraListaUsuarios()
    }

    private fun muestraListaUsuarios() {
        dalUsu.getAllUsuarios { usuarios: List<UsuarioNube> ->
            runOnUiThread {
                listaUsuarios = usuarios

                val adaptador = UsuarioNubeAdapter(listaUsuarios) { usuario -> onItemSelected(usuario) }

                binding.rvUsuarioAdmin.layoutManager = LinearLayoutManager(applicationContext)
                binding.rvUsuarioAdmin.adapter = adaptador
            }
        }
    }

    private fun limpiaControles() {
        binding.apply {
            progressAltaUsuarioAdmin.isVisible = false

            btnGuardarUsuarioAlta.isVisible = true
            txtNombreCompletoUsuarioUsuarios.text?.clear()
            txtEmailUsuarioUsuarioAdmin.text?.clear()
            txtNombreCompletoUsuarioUsuarios.text?.clear()
            txtNombreCompletoUsuarioUsuarios.text?.clear()
            txtContrasenaUsuarioAdmin.text?.clear()
            txtRepetirContrasenaUsuarioAdmin.text?.clear()

            if (cmbRolUsuarioUsuarioAdmin.count > 0) cmbRolUsuarioUsuarioAdmin.setSelection(0)

            txtNombreCompletoUsuarioUsuarios.requestFocus()
        }
    }

    private fun onItemSelected(usuario: UsuarioNube) {
        // Implementar lógica de selección
    }

}
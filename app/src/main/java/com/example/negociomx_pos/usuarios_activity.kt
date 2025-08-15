package com.example.negociomx_pos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.negociomx_pos.BE.UsuarioNube
import com.example.negociomx_pos.DAL.DALEmpresa
import com.example.negociomx_pos.DAL.DALUsuario
import com.example.negociomx_pos.adapters.SpinnerAdapter
import com.example.negociomx_pos.adapters.UsuarioNubeAdapter
import com.example.negociomx_pos.databinding.ActivityUsuariosBinding
import com.example.negociomx_pos.room.BLL.BLLUtil
import com.example.negociomx_pos.room.entities.Admins.Rol
import com.example.negociomx_pos.room.entities.ItemSpinner

class usuarios_activity : AppCompatActivity() {

    lateinit var binding: ActivityUsuariosBinding
    lateinit var dalUsu: DALUsuario
    lateinit var dalEmp: DALEmpresa

    lateinit var listaUsuarios: List<UsuarioNube>

    lateinit var bllUtil: BLLUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dalUsu = DALUsuario()
        dalEmp = DALEmpresa()

        bllUtil = BLLUtil()

        muestraEmpresasNube()

        var listaRoles: List<Rol>
        listaRoles = arrayListOf()

        listaRoles.add(Rol(IdRol = 0, Nombre = "Seleccione..."))
        listaRoles.add(Rol(IdRol = 1, Nombre = "SA"))
        listaRoles.add(Rol(IdRol = 2, Nombre = "Admin"))
        listaRoles.add(Rol(IdRol = 3, Nombre = "Ventas"))
        listaRoles.add(Rol(IdRol = 4, Nombre = "Supervisor"))
        listaRoles.add(Rol(IdRol = 5, Nombre = "Cliente"))

        var adapter: SpinnerAdapter
        adapter = bllUtil.convertListRolToListSpinner(this, listaRoles)

        binding.apply {
            cmbRolUsuarioUsuarios.adapter = adapter

            progressAltaUsuario.isVisible = false
            chkActivoEmpresaNube.isChecked = true
            chkActivoEmpresaNube.isEnabled = false
            btnNuevoUsuarioAlta.isVisible = false
            btnRegresarUsuarios.setOnClickListener {
                finish()
            }
            btnGuardarUsuarioAlta.setOnClickListener {
                var nombreCompleto = txtNombreCompletoUsuarioUsuarios.text.toString()
                var contrasena = txtContrasenaUsuarios.text.toString()
                var contrasena1 = txtRepetirContrasenaUsuarios.text.toString()
                var email = txtEmailUsuarioUsuarios.text.toString()
                var idEmpresa: String? = null
                var selEmp = cmbEmpresaUsuarioUsuarios.selectedItem as ItemSpinner
                if (selEmp.Valor != 0) idEmpresa = selEmp.Valor.toString()

                var idRol: String? = null
                var selRol = cmbRolUsuarioUsuarios.selectedItem as ItemSpinner
                if (selRol.Valor != 0) idRol = selRol.Valor.toString()

                if (nombreCompleto.isEmpty() == true)
                    txtEmailUsuarioUsuarios.error = "Debe suministrar el Nombre completo"
                else if (email.isEmpty() == true)
                    txtEmailUsuarioUsuarios.error = "Debe suministrar un Email"
                else if (contrasena.isEmpty() || contrasena1.isEmpty())
                    txtContrasenaUsuarios.error = "La contraseñas no deben estar vacias"
                else if (!contrasena.equals(contrasena1))
                    txtContrasenaUsuarios.error = "Las contraseñas no coinciden"
                else if (contrasena.length <= 5)
                    txtContrasenaUsuarios.error = "La debe ser minimo de 6 catacteres"
                else if (idRol == null || idRol.isEmpty() == true)
                    lblRolUsuario.error = "Es necesario seleccionar un Rol"
                else if (idEmpresa == null || idEmpresa.isEmpty() == true)
                    lblEmpresaUsuarioUsuarios.error = "Es necesario seleccionar una Empresa"
                else {
                    dalUsu.getUsuarioByEmail(email) { res: UsuarioNube? ->
                        if (res != null) {
                            bllUtil.MessageShow(
                                this@usuarios_activity, "El correo ya existe en el Sistema",
                                "Aviso"
                            ) {}
                            txtEmailUsuarioUsuarios.requestFocus()
                        } else {
                            btnGuardarUsuarioAlta.isVisible = false

                            progressAltaUsuario.isVisible = true

                            var activo = chkActivoEmpresaNube.isChecked

                            var usuario = UsuarioNube(
                                IdEmpresa = idEmpresa, IdRol = idRol, NombreCompleto = nombreCompleto,
                                Email = email, CuentaVerificada = false, Password = contrasena, Activo = activo
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

                binding.rvUsuarios.layoutManager = LinearLayoutManager(applicationContext)
                binding.rvUsuarios.adapter = adaptador
            }
        }
    }

    private fun onItemSelected(usuario: UsuarioNube) {
        // Implementar lógica de selección
    }

    private fun limpiaControles() {
        binding.apply {
            progressAltaUsuario.isVisible = false

            btnGuardarUsuarioAlta.isVisible = true
            txtNombreCompletoUsuarioUsuarios.text?.clear()
            txtEmailUsuarioUsuarios.text?.clear()
            txtNombreCompletoUsuarioUsuarios.text?.clear()
            txtNombreCompletoUsuarioUsuarios.text?.clear()
            txtContrasenaUsuarios.text?.clear()
            txtRepetirContrasenaUsuarios.text?.clear()

            if (cmbRolUsuarioUsuarios.count > 0) cmbRolUsuarioUsuarios.setSelection(0)
            if (cmbEmpresaUsuarioUsuarios.count > 0) cmbEmpresaUsuarioUsuarios.setSelection(0)

            txtNombreCompletoUsuarioUsuarios.requestFocus()
        }
    }

    private fun muestraEmpresasNube() {
        dalEmp.getByFilters(null) { lista ->
            runOnUiThread {
                var adapter = bllUtil.convertListEmpresaToListSpinner(this, lista!!)
                binding.cmbEmpresaUsuarioUsuarios.adapter = adapter
            }
        }
    }
}

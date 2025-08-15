package com.example.negociomx_pos

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.negociomx_pos.BE.EmpresaNube
import com.example.negociomx_pos.DAL.DALEmpresa
import com.example.negociomx_pos.adapters.EmpresaAdapter
import com.example.negociomx_pos.databinding.ActivityEmpresaNubeBinding
import com.example.negociomx_pos.room.BLL.BLLUtil

class empresa_nube_activity : AppCompatActivity() {

    private var dal= DALEmpresa()
    lateinit var binding:ActivityEmpresaNubeBinding
    var muestraAsignar:Boolean=false
    lateinit var bllUtil: BLLUtil

    var idEmpresaNube:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityEmpresaNubeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bllUtil=BLLUtil()
        muestraAsignar=false
        binding.llExpandableContent.isVisible=false
        binding.btnNuevaEmpresaAlta.setOnClickListener{
            binding.llExpandableContent.isVisible=true
            binding.txtRazonSocialEmpresaNube.requestFocus()
            binding.btnNuevaEmpresaAlta.isVisible=false
        }
        binding.chkRfcGenericoEmpresaNube.setOnCheckedChangeListener{ buttonView, isChecked ->
            if(isChecked) {
                binding.txtRfcCliente.setText("XAXX010101000")
                binding.txtRfcCliente.isEnabled=false
            }
            else {
                binding.txtRfcCliente.setText("")
                binding.txtRfcCliente.isEnabled=true
            }
        }
        binding.btnGuardarEmpresaNube.setOnClickListener{
            guardaEmpresaNube()
        }
        binding.btnRegresarEmpresaNube.setOnClickListener{
            finish()
        }
        binding.chkActivoEmpresaNube.isChecked=true
        binding.chkActivoEmpresaNube.isEnabled=false

        if(intent.extras?.isEmpty==false)
        {
            muestraAsignar= intent.getBooleanExtra("muestraAsignar",false)
        }

        muestraEmpresas()
    }

    private fun guardaEmpresaNube() {
        if(binding.txtRazonSocialEmpresaNube.text.isEmpty()==true)
        {
            binding.txtRazonSocialEmpresaNube.error="Es necesario suministrar la Razón social"
        }
        else if (binding.txtNombreEmpresaNube.text.isEmpty()==true)
        {
            binding.txtNombreEmpresaNube.error="Es necesario suministrar el Nombre comercial"
        }
        else if(binding.txtRfcCliente.text.isEmpty()==true)
        {
            binding.txtRfcCliente.error="Es necesario suministrar el R.F.C."
        }
        else{
            var nombreComercial:String
            var razonSocial:String
            var rfc:String
            var email:String
            var telefono:String
            var activa:Boolean
            var predeterminada:Boolean
            var CP:Int=0
            binding.apply {
                nombreComercial=txtNombreEmpresaNube.text.toString()
                razonSocial=txtRazonSocialEmpresaNube.text.toString()
                rfc=txtRfcCliente.text.toString()
                email=txtEmailCliente.text.toString()
                telefono=txtTelefonoEmpresaNube.text.toString()
                activa=chkActivoEmpresaNube.isChecked
                predeterminada=chkPredeterminadoEmpresaNube.isChecked

                if(txtCPEmpresaNube.text.isNotEmpty() && txtCPEmpresaNube.text.toString().toInt()>0)
                    CP=txtCPEmpresaNube.text.toString().toInt()
            }
            val rfcRazonSocial:String=rfc+"|"+razonSocial

            if(idEmpresaNube!=null && idEmpresaNube?.isEmpty()==false)
            {
                var empresa: EmpresaNube
                empresa = EmpresaNube(
                    NombreComercial = nombreComercial,
                    RazonSocial = razonSocial,
                    Id = idEmpresaNube,
                    Rfc = rfc,
                    Activa = activa,
                    IdRegimenFiscal = "1",
                    IdTipoContribuyente = "1",
                    Predeterminada = predeterminada,
                    PaginaWeb = "",
                    Contactos = "",
                    CodigoPostal = CP,
                    Email = email,
                    Telefonos = telefono
                )
                dal.update(empresa) {
                    onCompleteSaveEmpresa(it)

                    Toast.makeText(
                        applicationContext,
                        "La Empresa se ha actualizado correctamente.",
                        Toast.LENGTH_LONG
                    ).show()

                    limpiarControles()
                }
            }
            else
            {
                dal.getByRfcRazonSocial(rfcRazonSocial) { item ->
                    run {
                        if (item != null) {
                            Toast.makeText(
                                applicationContext, "El RFC y la Razón social ya existen",
                                Toast.LENGTH_LONG
                            ).show()

                            binding.txtRazonSocialEmpresaNube.requestFocus()
                        } else {
                            var empresa: EmpresaNube
                            empresa = EmpresaNube(
                                NombreComercial = nombreComercial,
                                RazonSocial = razonSocial,
                                Rfc = rfc,
                                Activa = activa,
                                IdRegimenFiscal = "1",
                                IdTipoContribuyente = "1",
                                Predeterminada = predeterminada,
                                PaginaWeb = "",
                                Contactos = "",
                                CodigoPostal = CP,
                                Email = email,
                                Telefonos = telefono
                            )
                            dal.insert(empresa) {
                                    onCompleteSaveEmpresa(it)

                                    Toast.makeText(
                                        applicationContext,
                                        "La Empresa se ha guardado correctamente.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    limpiarControles()
                                }
                        }
                    }
                }
            }
        }
    }

    private fun onCompleteSaveEmpresa(it: String) {
        var  cad=it

        muestraEmpresas()
    }

    private fun limpiarControles()
    {
        binding.apply {
            txtCPEmpresaNube.text?.clear()
            txtNombreEmpresaNube.text?.clear()
            txtRazonSocialEmpresaNube.text?.clear()
            txtEmailCliente.text?.clear()
            txtRfcCliente.text?.clear()

            btnNuevaEmpresaAlta.isVisible=true
            llExpandableContent.isVisible=false
        }
    }

    fun muestraEmpresas()
    {
        var lista=dal.getByFilters(null, { lista -> onFinishGetEmpresasListener(lista)})
    }

    private fun onFinishGetEmpresasListener(lista: List<EmpresaNube>?) {
        val adaptador = EmpresaAdapter(lista!!,muestraAsignar) { comando, empresa -> onItemSelected(comando, empresa) }

        binding.apply {
            rvEmpresaNube.layoutManager = LinearLayoutManager(applicationContext)
            rvEmpresaNube.adapter = adaptador

            var totalEmpresas:Int=0
            if(lista!=null && lista.count()>0)
                totalEmpresas=lista.count()

            lblListaEmpresasNube.setText("Total de Empresas ("+totalEmpresas.toString()+")")
        }
    }

    private fun onItemSelected(comando: Int, empresa: EmpresaNube) {
        if(comando==1) {
            intent.putExtra("idEmpresa", empresa.Id)
            setResult(RESULT_OK, intent)
            finish()
        }
        else if (comando==2)
        {
            bllUtil.MessageShow(this,"Desea modificar la Empresa: "+empresa.RazonSocial+" ?",
                "Pregunta"){
                res->
                if(res==1)
                {
                   idEmpresaNube=empresa.Id!!

                   binding.apply {
                       llExpandableContent.isVisible=true

                       txtRazonSocialEmpresaNube.setText(empresa.RazonSocial)
                       txtRfcCliente.setText(empresa.Rfc)
                       txtTelefonoEmpresaNube.setText(empresa.Telefonos)
                       txtNombreEmpresaNube.setText(empresa.NombreComercial)
                       txtEmailCliente.setText(empresa.Email)
                       if(empresa.CodigoPostal>0)
                           txtCPEmpresaNube.setText(empresa.CodigoPostal.toString())

                       chkActivoEmpresaNube.isEnabled=true
                       chkActivoEmpresaNube.isChecked=empresa.Activa
                       chkPredeterminadoEmpresaNube.isChecked=false

                       var generico:Boolean=empresa.Rfc.equals("XAXX010101000")
                       chkRfcGenericoEmpresaNube.isChecked= generico
                   }
                }
            }
        }
    }
}
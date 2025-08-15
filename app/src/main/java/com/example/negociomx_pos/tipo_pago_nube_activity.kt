package com.example.negociomx_pos

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.negociomx_pos.BE.TipoPagoNube
import com.example.negociomx_pos.DAL.DALTipoPago
import com.example.negociomx_pos.adapters.TipoPagoAdapter
import com.example.negociomx_pos.databinding.ActivityTipoPagoNubeBinding
import com.example.negociomx_pos.room.db.POSDatabase
import com.example.negociomx_pos.room.entities.TipoPago

class tipo_pago_nube_activity : AppCompatActivity() {

    lateinit var binding:ActivityTipoPagoNubeBinding

    lateinit var base: POSDatabase

    var idTipoPago: Int = 0
    lateinit var dalTP:DALTipoPago
    lateinit var listaTiposPago: List<TipoPago>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityTipoPagoNubeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dalTP= DALTipoPago()

        binding.apply {
            chkActivoTipoPagoNube.isChecked=true
            chkActivoTipoPagoNube.isEnabled=false

            btnGuardarTipoPagoNube.setOnClickListener{
                guardaTipoPagoNube()
            }
            btnRegresarTipoPagoNube.setOnClickListener{
                finish()
            }
        }

        MuestraTiposPago()
    }

    private fun MuestraTiposPago() {
        var lista=dalTP.getAll("") {
            lista -> onFinishGetTiposPagoListener(lista)
        }
    }

    private fun onFinishGetTiposPagoListener(lista: List<TipoPagoNube>?) {
        var lista1:List<TipoPago>
        lista1= arrayListOf()

        if(lista!=null)
        {
            lista.forEach{
                var item=TipoPago(Nombre = it.Nombre, Clave = it.Clave, Credito = it.Credito,
                    Pagado = it.Pagado, Activo = it.Activo, ConBanco = it.ConBanco, Predeterminado = it.Predeterminado,
                    DineroVirtual = it.DineroVirtual)

                lista1.add(item)
            }
        }

        val adaptador = TipoPagoAdapter(lista1) { comando, tipo -> onItemSelected(comando, tipo) }

        binding.apply {
            binding.rvTipoPagoNube.layoutManager = LinearLayoutManager(applicationContext)
            binding.rvTipoPagoNube.adapter = adaptador

            var totalTipos:Int=0
            if(lista!=null && lista.count()>0) totalTipos=lista.count()

           binding.lblEncListaTiposPagoNube.setText("Total de Tipos ("+totalTipos.toString()+")")
        }
    }

    private fun onItemSelected(comando: Int, tipo: TipoPago) {
        if(comando==1)
        {
            idTipoPago=tipo.IdTipoPago

            binding.apply {
                txtClaveTipoPagoNube.setText(tipo.Clave)
                txtNombreTipoPagoNube.setText(tipo.Nombre)

                chkActivoTipoPagoNube.isEnabled=true
                chkActivoTipoPagoNube.isChecked=tipo.Activo
                chkPredeterminadoTipoPagoNube.isChecked=tipo.Predeterminado
                chkCreditoTipoPagoNube.isChecked=tipo.Credito
                chkPagadoTipoPagoNube.isChecked=tipo.Pagado
            }
        }
        else
        {

        }
    }

    private fun guardaTipoPagoNube() {
        if(binding.txtNombreTipoPagoNube.text.isEmpty()==true)
        {
            binding.txtNombreTipoPagoNube.error="Es necesario suministrar el Nombre"
        }
        else if (binding.txtClaveTipoPagoNube.text.isEmpty()==true)
        {
            binding.txtClaveTipoPagoNube.error="Es necesario suministrar el Nombre comercial"
        }
        else{
            var nombre:String
            var razonSocial:String
            var clave:String
            var activo:Boolean
            var predeterminado:Boolean
            var credito:Boolean
            var autorizadoSAT:Boolean
            var pagado:Boolean

            binding.apply {
                nombre=txtNombreTipoPagoNube.text.toString()
                clave=txtClaveTipoPagoNube.text.toString()
                activo=chkActivoTipoPagoNube.isChecked
                predeterminado=chkPredeterminadoTipoPagoNube.isChecked
                credito=chkCreditoTipoPagoNube.isChecked
                autorizadoSAT=chkAutorizadoSATTipoPagoNube.isChecked
                pagado=chkPagadoTipoPagoNube.isChecked
            }
            if(idTipoPago>0)
            {
                var tipo: TipoPagoNube
                tipo = TipoPagoNube(Nombre = nombre, Clave = clave, Activo = activo, Credito = credito,
                    Predeterminado = predeterminado, Pagado = pagado)
                dalTP.update(tipo) {
                    onCompleteSaveEmpresa(it)

                    Toast.makeText(
                        applicationContext,
                        "El tipo de pago se ha actualizado correctamente.",
                        Toast.LENGTH_LONG
                    ).show()

                    limpiarControles()
                }
            }
            else
            {
                dalTP.getByClave(clave) { item ->
                    run {
                        if (item != null) {
                            Toast.makeText(
                                applicationContext, "La clave del tipo de pago ya existen",
                                Toast.LENGTH_LONG
                            ).show()

                            binding.txtClaveTipoPagoNube.requestFocus()
                        } else {
                            var tipo: TipoPagoNube
                            tipo = TipoPagoNube(Nombre = nombre, Clave = clave, Activo = activo, Predeterminado = predeterminado,
                            Credito = credito, Pagado = pagado)
                            dalTP.insert(tipo) {
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


    }

    private fun limpiarControles()
    {
        binding.apply {
            txtClaveTipoPagoNube.text?.clear()
            txtNombreTipoPagoNube.text?.clear()
            txtClaveTipoPagoNube.text?.clear()
            txtNombreTipoPagoNube.text?.clear()

            txtNombreTipoPagoNube.requestFocus()
        }
    }

}
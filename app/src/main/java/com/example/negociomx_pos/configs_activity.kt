package com.example.negociomx_pos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.negociomx_pos.BE.EmpresaNube
import com.example.negociomx_pos.DAL.DALEmpresa
import com.example.negociomx_pos.adapters.SpinnerAdapter
import com.example.negociomx_pos.databinding.ActivityConfigsBinding
import com.example.negociomx_pos.room.BLL.BLLUtil
import com.example.negociomx_pos.room.db.POSDatabase
import com.example.negociomx_pos.room.entities.ItemSpinner
import com.example.negociomx_pos.room.entities.TipoPago
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class configs_activity : AppCompatActivity() {
    lateinit var binding:ActivityConfigsBinding

    lateinit var listaTiposPago:List<TipoPago>
    lateinit var listaEmpresas:List<EmpresaNube>
    lateinit var base: POSDatabase
    lateinit var dalEmp:DALEmpresa
    lateinit var bllUtil: BLLUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityConfigsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bllUtil=BLLUtil()
        dalEmp=DALEmpresa()
        base=POSDatabase.getDatabase(applicationContext)

        binding.apply {
            btnGuardarConfigs.setOnClickListener{
                guardaConfiguraciones()
            }
            btnRegresarConfigs.setOnClickListener{
                finish()
            }
        }

        muestraEmpresas()
        muestraTiposPago()
    }

    private fun muestraTiposPago() {
        GlobalScope.launch {
            val lista = base.tipoPagoDAO().getAll(true)

            runOnUiThread {
                lista.observe(this@configs_activity)
                {
                    try {
                        listaTiposPago = arrayListOf()
                        listaTiposPago = it

                        val transform: (TipoPago) -> (ItemSpinner) = {
                            ItemSpinner(it.IdTipoPago.toInt(), it.Nombre)
                        }
                        val result = listaTiposPago.map(transform).toList()

                        val adapter = SpinnerAdapter(
                            applicationContext, result, R.layout.item_spinner_status,
                            R.id.lblDisplayStatus
                        )
                        binding.cmbTipoPagoPreConfig.adapter = adapter
                    }
                    catch (ex:Exception)
                    {
                        val cad=ex.toString()
                    }
                    finally {
                    }
                }
            }
        }
    }

    private fun muestraEmpresas() {
        dalEmp.getByFilters (null) {
            res->
                try {
                    listaEmpresas = arrayListOf()
                    if(res!=null)listaEmpresas = res!!

                    var adapter= bllUtil.convertListEmpresaToListSpinner(applicationContext,listaEmpresas)
                    binding.cmbTipoPagoPreConfig.adapter = adapter
                }
                catch (ex:Exception)
                {
                    val cad=ex.toString()
                }
                finally {
                }
        }
    }

    private fun guardaConfiguraciones() {

    }
}
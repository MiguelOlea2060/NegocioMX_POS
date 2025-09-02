package com.example.negociomx_pos

import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Window
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.adapters.SpinnerAdapter
import com.example.negociomx_pos.adapters.VentasAAdapter
import com.example.negociomx_pos.databinding.ActivityVentasaBinding
import com.example.negociomx_pos.room.BLL.BLLUtil
import com.example.negociomx_pos.room.daos.DoctoVenta
import com.example.negociomx_pos.room.db.POSDatabase
import com.example.negociomx_pos.room.entities.Cliente
import com.example.negociomx_pos.room.entities.Documento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class ventasa_activity : AppCompatActivity() {
    lateinit var listaVentas:List<DoctoVenta>
    private  lateinit var base: POSDatabase
    private lateinit var rv: RecyclerView
    lateinit var listaClientes:ArrayList<Cliente>
    lateinit var imgFechaInicio:ImageView
    lateinit var imgFechaFin:ImageView
    lateinit var btnRegresar:ImageView

    private lateinit var binding:ActivityVentasaBinding

    var fechaInicioCad:String=""
    val fechaFinCad:String=""
    var fechaInicioLong:Long=0
    var fechaFinLog:Long=0
    lateinit var bllUtil:BLLUtil
    lateinit var cmbClientes:Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityVentasaBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_ventasa)
        setContentView(binding.root)

        base = POSDatabase.getDatabase(applicationContext)

        listaClientes= arrayListOf()
        bllUtil= BLLUtil()

        btnRegresar=findViewById(R.id.btnRegresarVentasA)
        imgFechaInicio=findViewById(R.id.imgFechaInicioVentasA)
        imgFechaFin=findViewById(R.id.imgFechaFinVentasA)
        cmbClientes=findViewById(R.id.cmbClienteVentasA)
        rv=findViewById(R.id.rvVentasA)

        btnRegresar.setOnClickListener{
            finish()
        }
        imgFechaInicio.setOnClickListener{
            val dialog= Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.item_calendario)

            val calendario: CalendarView =dialog.findViewById(R.id.dtFechaCal)
            val imgAceptar:ImageView=dialog.findViewById(R.id.imgAceptarCalendario)
            val imgCancelar:ImageView=dialog.findViewById(R.id.imgCancelarCalendario)
            if(fechaInicioCad.isEmpty()==false)
            {
                val selectedDate = fechaInicioCad
                calendario.setDate(
                    SimpleDateFormat("dd/MM/yyyy").parse(selectedDate).getTime(),
                    true,
                    true
                )
                calendario.setDate(fechaInicioLong,true,true)
            }
            var formato:DecimalFormat =DecimalFormat("00")
            var valor:Long=calendario.date
            calendario.setOnDateChangeListener{ view,year, month,dayOfMonth->
                fechaInicioCad=formato.format(dayOfMonth)+"/"+ formato.format(month+1) +"/"+ year.toString()
            }
            imgAceptar.setOnClickListener{
                dialog.dismiss()
            }
            imgCancelar.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
        imgFechaFin.setOnClickListener{
            val dialog= Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.item_calendario)

            val calendario: CalendarView =dialog.findViewById(R.id.dtFechaCal)
            val imgAceptar:ImageView=dialog.findViewById(R.id.imgAceptarCalendario)
            val imgCancelar:ImageView=dialog.findViewById(R.id.imgCancelarCalendario)
            if(fechaFinLog>0)calendario.setDate(fechaFinLog)

            imgAceptar.setOnClickListener{
                fechaFinLog= calendario.date
                dialog.dismiss()
            }
            imgCancelar.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        muestraClientesPOS()
        //muestraListaVentas("")
    }

    private fun muestraClientesPOS() {
        val scope= MainScope()
        fun asyncFun() = scope.launch {
            try {
                var it = base.clienteDAO().getByFilters(null)

                var default=Cliente(0,null,"Todos","Todos",
                    "","","",0,0,0F,"")

                listaClientes.add(default)
                listaClientes.addAll(it)

                var adapter:SpinnerAdapter=bllUtil.convertListClienteToListSpinner(applicationContext,listaClientes)
                cmbClientes.adapter = adapter
            }
            catch (ex:Exception)
            {
                val cad=ex.toString()
            }
        }
        asyncFun()
    }

    private fun muestraListaVentas(nombre:String) {
        var nombreFilter:String=nombre
        if(nombreFilter.isEmpty()==false)
            nombreFilter="%"+nombreFilter+"%"

        fun ImprimeTicketVenta(docto: Documento?) {
            lifecycleScope.launch(Dispatchers.IO) {
                printTcp()
            }
        }

        fun onItemSelected(docto: DoctoVenta, comando:Int) {
            if(comando==1)
            {

            }
            else if(comando==2)
            {
                var docto:Documento?=null

                ImprimeTicketVenta(docto)
            }
        }

        val scope= MainScope()
        fun asyncFun() = scope.launch {
            val lista = base.documentoDAO().getAllDoctos()

            lista.observe(this@ventasa_activity)
            {
                if(it!=null && it.isNotEmpty())
                {
                    listaVentas = arrayListOf()
                    listaVentas = it

                    val adaptador = VentasAAdapter(listaVentas){ docto,comando-> onItemSelected(docto,comando) }

                    rv.layoutManager = LinearLayoutManager(applicationContext)
                    rv.adapter = adaptador
                }
                else
                {
                    finish()
                }
            }
        }
        asyncFun()
    }

    private suspend fun printTcp() {
        try {
            var ipMiniPrinter:String="192.168.1.110"
            var puertoMiniPrinter:Int=9100
            /*printer =
                CoroutinesEscPosPrinter(
                    TcpConnection(
                        ipMiniPrinter,
                        puertoMiniPrinter
                    ).apply { connect(this@ventasa_activity) }, 203, 48f, 32
                )*/

//             this.printIt(new TcpConnection(ipAddress.getText().toString(), Integer.parseInt(portAddress.getText().toString())));
//            AsyncTcpEscPosPrint(this).execute(printer.setTextToPrint(test))

        } catch (e: NumberFormatException) {
            AlertDialog.Builder(this)
                .setTitle("Invalid TCP port address")
                .setMessage("Port field must be a number.")
                .show()
            e.printStackTrace()
        }
    }

    /*==============================================================================================
======================================BLUETOOTH PART============================================
==============================================================================================*/
    private val PERMISSION_BLUETOOTH = 1

/*    private fun printBluetooth() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH),
                PERMISSION_BLUETOOTH
            )
        } else {
            // this.printIt(BluetoothPrintersConnections.selectFirstPaired());
            AsyncBluetoothEscPosPrint(this).execute(this.getAsyncEscPosPrinter(null))
        }
    }*/

/*    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                PERMISSION_BLUETOOTH -> printBluetooth()
            }
        }
    }*/

    private val body: String
        get() = "[L]\n" +
                "[L]    <b>Pizza</b>[R][R]3[R][R]55 $\n" +
                "[L]      + Olive[R][R]1 $\n" +
                "[L]      + Cheese[R][R]5 $\n" +
                "[L]      + Mushroom[R][R]7 $\n" +
                "[L]\n" +
                "[L]    <b>Burger</b>[R][R]7[R][R]43.54 $\n" +
                "[L]      + Cheese[R][R]3 $\n" +
                "[L]\n" +
                "[L]    <b>Shawarma</b>[R][R]2[R][R]4 $\n" +
                "[L]      + Garlic[R][R]0.5 $\n" +
                "[L]\n" +
                "[L]    <b>Steak</b>[R][R]3[R][R]75 $\n" +
                "[L]\n" +
                "[R] PAYMENT METHOD :[R]Visa\n"

    private val customer: String
        get() =
            "[C]================================\n" +
                    "[L]\n" +
                    "[L]<b>Delivery</b>[R]5 $\n" +
                    "[L]\n" +
                    "[L]<u><font color='bg-black' size='tall'>Customer :</font></u>\n" +
                    "[L]Name : Mohammad khair\n" +
                    "[L]Phone : 00962787144627\n" +
                    "[L]Area : Khalda\n" +
                    "[L]street : testing street\n" +
                    "[L]building : 9\n" +
                    "[L]Floor : 2\n" +
                    "[L]Apartment : 1\n" +
                    "[L]Note : This order is just for testing\n"

}
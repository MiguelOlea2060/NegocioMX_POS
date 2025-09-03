package com.example.negociomx_pos

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.negociomx_pos.BE.ArticuloActNube
import com.example.negociomx_pos.BE.ArticuloNube
import com.example.negociomx_pos.BE.CategoriaNube
import com.example.negociomx_pos.BE.CfgNVNube
import com.example.negociomx_pos.BE.CfgNube
import com.example.negociomx_pos.BE.EmpresaNube
import com.example.negociomx_pos.BE.MarcaNube
import com.example.negociomx_pos.BE.TipoPagoNube
import com.example.negociomx_pos.BE.UnidadMedidaNube
import com.example.negociomx_pos.DAL.DALArticulo
import com.example.negociomx_pos.DAL.DALCategoria
import com.example.negociomx_pos.DAL.DALCfg
import com.example.negociomx_pos.DAL.DALCfgNV
import com.example.negociomx_pos.DAL.DALEmpresa
import com.example.negociomx_pos.DAL.DALMarca
import com.example.negociomx_pos.DAL.DALTipoPago
import com.example.negociomx_pos.DAL.DALUnidadMedida
import com.example.negociomx_pos.DAL.DALUsuario
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.room.BLL.BLLUtil
import com.example.negociomx_pos.room.db.POSDatabase
import com.example.negociomx_pos.room.entities.Admins.CfgNV
import com.example.negociomx_pos.room.entities.Admins.Config
import com.example.negociomx_pos.room.entities.Admins.Empresa
import com.example.negociomx_pos.room.entities.Articulo
import com.example.negociomx_pos.room.entities.Categoria
import com.example.negociomx_pos.room.entities.Marca
import com.example.negociomx_pos.room.entities.TipoPago
import com.example.negociomx_pos.room.entities.UnidadMedida
import com.example.negociomx_pos.room.enums.TipoUsoSistemaEnum
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class menu_principal_activity : AppCompatActivity() {

    lateinit var bllUtil: BLLUtil
    lateinit var dalCfg: DALCfg
    lateinit var dalCfgNV: DALCfgNV
    lateinit var dalArt: DALArticulo
    lateinit var dalUM:DALUnidadMedida
    lateinit var dalMar:DALMarca
    lateinit var dalCat:DALCategoria
    lateinit var dalTP:DALTipoPago
    lateinit var dalEmp:DALEmpresa
    lateinit var dalUsu:DALUsuario
    lateinit var base: POSDatabase

    var idDocumentoPendiente:Int?=null
    var empresaNube: EmpresaNube?=null

    lateinit var listaMarcasNubeTotal:List<MarcaNube>
    lateinit var listaCategoriasNubeTotal:List<CategoriaNube>
    lateinit var listaUMNubeTotal:List<UnidadMedidaNube>
    lateinit var listaTiposPagoNubeTotal:List<TipoPagoNube>

    lateinit var listaArticuloNubeAActualizar: List<ArticuloNube>
    lateinit var listaArticulosNube: List<ArticuloNube>
    lateinit var listaArtsAct: List<ArticuloActNube>

    lateinit var listaUMlocales:List<UnidadMedida>
    lateinit var listaMarcasLocales:List<Marca>
    lateinit var listaCategoriasLocales:List<Categoria>
    lateinit var listaTiposLocales:List<TipoPago>

    lateinit var listaUMNube:List<UnidadMedidaNube>
    lateinit var listaMarcasNube:List<MarcaNube>
    lateinit var listaCategoriasNube:List<CategoriaNube>
    lateinit var listaTipospagoNube:List<TipoPagoNube>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_principal)

        bllUtil = BLLUtil()
        base = POSDatabase.getDatabase(applicationContext)
        dalCfg = DALCfg()
        dalUM= DALUnidadMedida()
        dalCfgNV = DALCfgNV()
        dalArt = DALArticulo()
        dalMar=DALMarca()
        dalCat=DALCategoria()
        dalTP=DALTipoPago()
        dalEmp=DALEmpresa()
        dalUsu=DALUsuario()

        idDocumentoPendiente=null
        empresaNube=null
        listaArticuloNubeAActualizar = arrayListOf()
        listaArtsAct = arrayListOf()
        listaArticulosNube = arrayListOf();
        listaCategoriasNube= arrayListOf()
        listaTipospagoNube= arrayListOf()

        val btnAltaArticulo = findViewById<Button>(R.id.btnAltaArticulo)
        val btnUm = findViewById<Button>(R.id.btnUM)
        val btnCategoria = findViewById<Button>(R.id.btnCategoria)
        val btnCliente = findViewById<Button>(R.id.btnCliente)
        val btnConsultaArticulo = findViewById<Button>(R.id.btnConsultaArticulo)
        val btnPOS = findViewById<Button>(R.id.btnPos)
        val btnVentas = findViewById<Button>(R.id.btnVentas)
        val btnImpuesto = findViewById<Button>(R.id.btnImpuesto)
        val btnTipoPago = findViewById<Button>(R.id.btnTipoPago)
        val btnSincronizacion = findViewById<Button>(R.id.btnSincronizacion)
        val btnLogout = findViewById<ImageView>(R.id.imgLoginLogoutMenuPrincipal)
        val btnUsuarios = findViewById<Button>(R.id.btnUsuarios)
        val btnEmpresas = findViewById<Button>(R.id.btnEmpresas)
        val btnConfigs = findViewById<Button>(R.id.btnConfiguraciones)
        val btnRadmin = findViewById<Button>(R.id.btnRadminVPN)
        val btnConsultaPaso1 = findViewById<Button>(R.id.btnConsultaPaso1)
        val btnConsultaPaso2 = findViewById<Button>(R.id.btnConsultaPaso2)
        val btnPaso1SOC =findViewById<Button>(R.id.btnPaso1SOC)
        val btnVehiculo = findViewById<Button>(R.id.btnVehiculo)
        val btnPaso2Accesorios=findViewById<Button>(R.id.btnPaso2Accesorios)
        val btnPaso3Repuve =findViewById<Button>(R.id.btnPaso3Repuve)
        val btnConsultaPaso3 =findViewById<Button>(R.id.btnConsultaPaso3)

        getEmpresaNubeCfgNubeCfgNVNube()

        var visibleBtns=!ParametrosSistema.usuarioLogueado.IdRol.equals("5")
        btnPOS.isVisible=visibleBtns
        btnCategoria.isVisible=visibleBtns
        btnUsuarios.isVisible=visibleBtns
        btnUm.isVisible=visibleBtns
        btnAltaArticulo.isVisible=visibleBtns
        btnConsultaArticulo.isVisible=visibleBtns
        btnVentas.isVisible=visibleBtns
        btnImpuesto.isVisible=visibleBtns
        btnTipoPago.isVisible=visibleBtns
        btnCliente.isVisible=visibleBtns
        btnSincronizacion.isVisible=visibleBtns
        btnConfigs.isVisible=visibleBtns
        btnRadmin.isVisible=visibleBtns
        btnEmpresas.isVisible=visibleBtns
        btnVehiculo.isVisible=visibleBtns


        btnUm.setOnClickListener {
            val intent = Intent(this, unidadmedida_activity::class.java)
            startActivity(intent)
        }
        btnCategoria.setOnClickListener {
            val intent = Intent(this, categoria_activity::class.java)
            startActivity(intent)
        }
        btnAltaArticulo.setOnClickListener {
            seleccionPantallaArticuloAEjecutar()
        }
        btnConsultaArticulo.setOnClickListener {
            val intent = Intent(this, articulo_consulta_activity::class.java)
            startActivity(intent)
        }
        btnPOS.setOnClickListener {
            val intent = Intent(this, posa_activity::class.java)
            intent.putExtra("idDocumentoPendiente",idDocumentoPendiente)
            startForResult.launch(intent)
        }
        btnVentas.setOnClickListener {
            val intent = Intent(this, ventasa_activity::class.java)
            startActivity(intent)
        }
        btnImpuesto.setOnClickListener {
            val intent = Intent(this, impuesto_activity::class.java)
            startActivity(intent)
        }
        btnLogout.setOnClickListener {
            bllUtil.MessageShow(
                this,
                "Pregunta",
                "En realidad desea salir de la Aplicación ?"
            ) { res ->
                if (res == 1) {
                    ParametrosSistema.firebaseAuth.signOut()

                    intent.putExtra("cerrarSesion", true)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
        btnTipoPago.setOnClickListener {
            if(ParametrosSistema.TipoUsoSistema== TipoUsoSistemaEnum.OnLine)
            {
                val intent = Intent(this, tipopago_activity::class.java)
                startActivity(intent)
            }
            else
            {
                val intent = Intent(this, tipo_pago_nube_activity::class.java)
                startActivity(intent)
            }
        }
        btnSincronizacion.setOnClickListener {
            val intent = Intent(this, sincronizacion_activity::class.java)
            startActivity(intent)
        }

        btnCliente.setOnClickListener {
            val intent = Intent(this, cliente_activity::class.java)
            startActivity(intent)
        }
        btnEmpresas.setOnClickListener {
            val intent = Intent(this, empresa_nube_activity::class.java)
            startActivity(intent)
        }
        btnUsuarios.setOnClickListener {
            val intent = Intent(this, usuarios_activity::class.java)
            startActivity(intent)
        }
        btnConfigs.setOnClickListener {
            val intent = Intent(this, configs_activity::class.java)
            startActivity(intent)
        }
        btnRadmin.setOnClickListener {
            val intent = Intent(this, vpn_connect_activity::class.java)
            startActivity(intent)
        }
        btnPaso1SOC.setOnClickListener{
            val intent = Intent(this, Paso1SOC_Activity::class.java)
            startActivity(intent)
        }
        btnPaso2Accesorios.setOnClickListener{
            val intent = Intent(this, Paso2SOC_Activity::class.java)
            startActivity(intent)
        }
        btnPaso3Repuve.setOnClickListener{
            val intent = Intent(this, Paso3Repuve_Activity::class.java)
            startActivity(intent)
        }
        btnConsultaPaso1.setOnClickListener{
            val intent = Intent(this, ConsultaPaso1Soc_Activity::class.java)
            startActivity(intent)
        }
        btnConsultaPaso2.setOnClickListener{
            val intent = Intent(this, ConsultaPaso2_Activity::class.java)
            startActivity(intent)
        }
        btnConsultaPaso3.setOnClickListener{
            val intent = Intent(this, ConsultaPaso3Repuve_Activity::class.java)
            startActivity(intent)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
        btnVehiculo.setOnClickListener {
            val intent = Intent(this, alta_vehiculo::class.java)
            startActivity(intent)
        }
    }

    private fun verificarCatalogosDeNubePorActualizarLocalmente() {
        validaArtsNubeConLocal (){
            val idEmpresa = ParametrosSistema.cfg.IdEmpresa
            val idUsuario = ParametrosSistema.usuarioLogueado.Id

            try {
                dalArt.getArticuloActByFilter(idEmpresa, idUsuario, false) { res ->
                    listaArtsAct = arrayListOf()
                    if (res != null && res.count() > 0) {
                        listaArtsAct = res

                        var idsArticulo: List<String>
                        idsArticulo = arrayListOf()

                        idsArticulo = res.map { it.IdArticulo } as List<String>

                        dalArt.getByIdEmpresa(idEmpresa, idsArticulo) { res ->
                            if (res != null && res.count() > 0) {
                                listaArticuloNubeAActualizar = res
                                abreActivityArtActNubeToLocal()
                            }
                            else
                                ejecutaSeleccionSistemaOnlineUOffline()
                        }
                    }
                    else
                        ejecutaSeleccionSistemaOnlineUOffline()
                }
            }
            catch (ex:Exception)
            {
            }
        }
    }

    private fun ejecutaSeleccionSistemaOnlineUOffline() {
        bllUtil.MessageShow(this,"Aceptar","",
            "Desea seguir trabajando con el sistema OFFLINE","Pregunta",){
                res->
            if(res==1)
            {
                ParametrosSistema.TipoUsoSistema=TipoUsoSistemaEnum.OnLine
            }
            else
            {
                ParametrosSistema.TipoUsoSistema=TipoUsoSistemaEnum.OnLine
            }
        }
    }

    private  fun abreActivityArtActNubeToLocal() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.activity_sync_catalogos)

        val btnActConfig: ImageView = dialog.findViewById(R.id.imgActualizarConfigSync)
        val btnActConfigNV: ImageView = dialog.findViewById(R.id.imgActualizarConfigNVSync)
        val btnActEmp: ImageView = dialog.findViewById(R.id.imgActualizarEmpresaSync)
        val btnActUM: ImageView = dialog.findViewById(R.id.imgActualizarUnidadesMedidaSync)
        val btnActMarca: ImageView = dialog.findViewById(R.id.imgActualizarMarcasSync)
        val btnActCategoria: ImageView = dialog.findViewById(R.id.imgActualizarCategoriasSync)
        val btnActTipos: ImageView = dialog.findViewById(R.id.imgActualizarTiposPagoSync)
        val btnActArt: ImageView = dialog.findViewById(R.id.imgActualizarArticulosSync)

        val lblCatalogoCfg:TextView=dialog.findViewById(R.id.lblCatalogoConfigSync)
        val lblCatalogoCfgNV:TextView=dialog.findViewById(R.id.lblCatalogoConfigNVSync)
        val lblCatalogoEmp:TextView=dialog.findViewById(R.id.lblCatalogoEmpresaSync)
        val lblCatalogoUM:TextView=dialog.findViewById(R.id.lblCatalogoUnidadesMedidaSync)
        val lblCatalogoMarca:TextView=dialog.findViewById(R.id.lblCatalogoMarcasSync)
        val lblCatalogoCategoria:TextView=dialog.findViewById(R.id.lblCatalogoCategoriasSync)
        val lblCatalogoTP:TextView=dialog.findViewById(R.id.lblCatalogoTiposPagoSync)
        val lblCatalogoArt:TextView=dialog.findViewById(R.id.lblCatalogoArticulosSync)

        btnActConfig.isVisible=false
        btnActConfigNV.isVisible=false
        btnActEmp.isVisible=false
        btnActUM.isVisible=false
        btnActMarca.isVisible=false
        btnActCategoria.isVisible=false
        btnActTipos.isVisible=false
        btnActArt.isVisible=false

        lblCatalogoEmp.setText("1. Datos de Empresa (...)_")
        lblCatalogoCfg.setText("2. Configuracion global (...)")
        lblCatalogoCfgNV.setText("3. Configuracion de N.V. (...)")
        lblCatalogoUM.setText("4. Catalogo de U.M. (...)")
        lblCatalogoMarca.setText("5. Catalogo de Marcas (...)")
        lblCatalogoCategoria.setText("6. Catalogo de Categorias (...)")
        lblCatalogoTP.setText("7. Catalogo de Tipos de pago (...)")
        lblCatalogoArt.setText("8. Catalogo de Articulos (...)")

        obtenUMsNube () { res ->
            if (res != null) {
                listaUMNube = res!!
                listaUMNubeTotal=res!!
            }
            val resultado= base.unidadMedidaDAO().getAll(null, null)
            val scope = MainScope()
            fun asyncFun() = scope.launch {
                resultado.observe(this@menu_principal_activity)
                {
                        res1 ->
                    if (res1 != null) {
                        listaUMlocales = res1

                        var listaAux:ArrayList<UnidadMedidaNube>
                        listaAux= arrayListOf()
                        if (listaUMlocales != null && listaUMlocales.count()>0) {
                            listaUMNube.forEach {
                                var find = listaUMlocales.filter { a -> a.IdNube.toString().equals(it.Id) }.firstOrNull()
                                if(find==null)
                                    listaAux.add(it)
                            }
                            listaUMNube=listaAux
                        }
                    }

                    if (listaUMNube != null && listaUMNube.count() > 0) {
                        var totalUM = listaUMNube.count()
                        lblCatalogoUM.setText("3. Catalogo de U.M. ($totalUM)")
                        btnActUM.isVisible = true
                    } else {
                        lblCatalogoUM.setText("3. Catalogo de U.M. (Listo)")
                    }
                }
            }
            asyncFun()
        }

        obtenMarcasNube(){
                res->
            if (res != null) {
                listaMarcasNube = res!!
                listaMarcasNubeTotal=res!!
            }
            val resultado= base.marcaDAO().getAll(null)
            val scope = MainScope()
            fun asyncFun() = scope.launch {
                resultado.observe(this@menu_principal_activity)
                {
                        res1 ->
                    if (res1 != null) {
                        listaMarcasLocales = res1

                        var listaAux:ArrayList<MarcaNube>
                        listaAux= arrayListOf()
                        if (listaMarcasLocales != null && listaMarcasLocales.count()>0) {
                            listaMarcasNube.forEach {
                                var find = listaMarcasLocales.filter { a -> a.IdNube.toString().equals(it.Id) }.firstOrNull()
                                if(find==null) {
                                    listaAux.add(it)
                                }
                            }
                            listaMarcasNube=listaAux
                        }
                    }

                    if (listaMarcasNube != null && listaMarcasNube.count() > 0) {
                        var totalRegistros = listaMarcasNube.count()
                        lblCatalogoMarca.setText("5. Catalogo de Marcas ($totalRegistros)")
                        btnActMarca.isVisible = true
                    } else {
                        lblCatalogoMarca.setText("5. Catalogo de Marcas (Listo)")
                    }
                }
            }
            asyncFun()
        }

        obtenCategoriasNube(){
                res->
            if (res != null) {
                listaCategoriasNube = res!!
                listaCategoriasNubeTotal=res!!
            }
            val resultado= base.categoriaDAO().getAll(null)
            val scope = MainScope()
            fun asyncFun() = scope.launch {
                resultado.observe(this@menu_principal_activity)
                {
                        res1 ->
                    if (res1 != null) {
                        listaCategoriasLocales = res1

                        var listaAux:ArrayList<CategoriaNube> = arrayListOf()
                        if (listaCategoriasLocales != null && listaCategoriasLocales.count()>0) {
                            listaCategoriasNube.forEach {
                                var find = listaCategoriasLocales.filter { a -> a.IdNube.toString().equals(it.Id) }.firstOrNull()
                                if(find==null)
                                    listaAux.add(it)
                            }
                            listaCategoriasNube=listaAux
                        }
                    }

                    if (listaCategoriasNube != null && listaCategoriasNube.count() > 0) {
                        var totalRegistros = listaCategoriasNube.count()
                        lblCatalogoCategoria.setText("6. Catalogo de Categorias ($totalRegistros)")
                        btnActCategoria.isVisible = true
                    } else {
                        lblCatalogoCategoria.setText("6. Catalogo de Categorias (Listo)")
                    }
                }
            }
            asyncFun()
        }

        obtenTiposPagoNube(){
                res->
            if (res != null) {
                listaTipospagoNube = res!!
                listaTiposPagoNubeTotal=res!!
            }
            val resultado= base.tipoPagoDAO().getAll(null)
            val scope = MainScope()
            fun asyncFun() = scope.launch {
                resultado.observe(this@menu_principal_activity)
                {
                        res1 ->
                    if (res1 != null) {
                        listaTiposLocales = res1

                        var listaAux:ArrayList<TipoPagoNube> = arrayListOf()
                        if (listaTiposLocales != null && listaTiposLocales.count()>0) {
                            listaTipospagoNube.forEach {
                                var find = listaTiposLocales.filter { a -> a.IdNube.toString().equals(it.Id) }.firstOrNull()
                                if(find==null)
                                    listaAux.add(it)
                            }
                            listaTipospagoNube=listaAux
                        }
                    }

                    if (listaTipospagoNube != null && listaTipospagoNube.count() > 0) {
                        var totalRegistros = listaTipospagoNube.count()
                        lblCatalogoTP.setText("7. Catalogo de Tipos de pago ($totalRegistros)")
                        btnActTipos.isVisible = true
                    } else {
                        lblCatalogoTP.setText("7. Catalogo de Tipos de pago (Listo)")
                    }
                }
            }
            asyncFun()
        }

        obtenArticulosNube (){
                res->
            if (res != null) listaArticulosNube = res!!
            val resultado= base.articuloDAO().getAll("")
            val scope = MainScope()
            fun asyncFun() = scope.launch {
                resultado.observe(this@menu_principal_activity)
                {
                        res1 ->
                    var listaLocal:List<Articulo> = arrayListOf()
                    if (res1 != null) {
                        listaLocal = res1

                        var listaAux:ArrayList<ArticuloNube> = arrayListOf()
                        if (listaLocal != null && listaLocal.count()>0) {
                            listaArticulosNube.forEach {
                                var find = listaLocal.filter { a -> a.IdNube.toString().equals(it.Id) }.firstOrNull()
                                if(find==null)
                                    listaAux.add(it)
                            }
                            listaArticulosNube=listaAux
                        }
                    }

                    if (listaArticulosNube != null && listaArticulosNube.count() > 0) {
                        var totalRegistros = listaArticulosNube.count()
                        lblCatalogoArt.setText("7. Catalogo de Articulos ($totalRegistros)")
                        btnActArt.isVisible = true
                    } else {
                        lblCatalogoArt.setText("7. Catalogo de Articulos (Listo)")
                    }
                }
            }
            asyncFun()
        }

        if(empresaNube!=null)
        {
            var idEmpresaNube=ParametrosSistema.empresaNube.Id!!.toInt()
            obtenEmpresaLocal (idEmpresaNube) {
                ParametrosSistema.empresaLocal=it
                var empresaLocal = ParametrosSistema.empresaLocal
                if (empresaLocal == null) {
                    btnActEmp.isVisible = true
                    lblCatalogoEmp.setText("1. Datos de Empresa (1)")
                } else {
                    lblCatalogoEmp.setText("1. Datos de Empresa (Listo)")
                }
            }
        }

        var config=ParametrosSistema.cfg
        if(config!=null)
        {
            var idCfgNube=config.IdCfg
            obtenCfgLocal (idCfgNube!!.toInt()) {
                ParametrosSistema.cfgLocal=it
                var cfgLocal = ParametrosSistema.cfgLocal
                if (cfgLocal == null) {
                    btnActConfig.isVisible = true
                    lblCatalogoCfg.setText("2. Configuración global (1)")
                } else {
                    lblCatalogoCfg.setText("2. Configuración global (Listo)")
                }
            }
        }

        var configNV=ParametrosSistema.cfgNV
        if(configNV!=null)
        {
            var idCfgNVNube=configNV.IdCfgNV
            obtenCfgNVLocal (idCfgNVNube!!.toInt()) {
                ParametrosSistema.cfgNVLocal=it
                var cfbNVLocal = ParametrosSistema.cfgNVLocal
                if (cfbNVLocal == null) {
                    btnActConfigNV.isVisible = true
                    lblCatalogoCfgNV.setText("3. Configuración de N.V. (1)")
                } else {
                    lblCatalogoCfgNV.setText("3. Configuración de N.V. (Listo)")
                }
            }
        }

        btnActEmp.setOnClickListener {
            actualizaEmpresaNubeALocal(empresaNube!!)
            {
                    actualizada,mensajeError->
                if(actualizada) {
                    btnActEmp.isVisible = false
                    lblCatalogoEmp.setText("1. Datos de Empresa (Listo)")
                }
            }
        }
        btnActConfig.setOnClickListener {
            actualizaConfigsLocalmente()
            {
                    idCfg,agregado->
                if(agregado) {
                    if(agregado)
                    {
                        lblCatalogoCfg.setText("2. Configuracion global (Listo)")
                        btnActConfig.isVisible=false
                    }
                }
            }
        }
        btnActConfigNV.setOnClickListener {
            var cfgNVNube=ParametrosSistema.cfgNV
            var idCfgNube=ParametrosSistema.cfg.IdCfg!!.toInt()

            val scope= MainScope()
            fun asyncFun() = scope.launch {
                var find = base.configDAO().getByFilters(null, idCfgNube, null)
                runOnUiThread {
                    find.observe(this@menu_principal_activity) {
                        var cfg=it
                        actualizaConfigNVLocalmente(cfg) { res ->
                            if (res) {
                                btnActConfigNV.isVisible = false
                                lblCatalogoCfgNV.setText("3. Configuración de N.V. (Listo)")
                            } else {
                                lblCatalogoCfgNV.setText("3. Configuración de N.V. (Error)")
                            }
                        }
                    }
                }
            }
            asyncFun()
        }

        btnActUM.setOnClickListener{
            actualizarUMNubeALocal(){
                    res,mensajeError->
                if(res)
                {
                    btnActUM.isVisible=false
                    lblCatalogoUM.setText("4. Catalogo de U.M. (Listo)")
                }
            }
        }
        btnActMarca.setOnClickListener{
            actualizarMarcasNubeALocal(){
                    res,mensajeError->
                if(res)
                {
                    btnActMarca.isVisible=false
                    lblCatalogoMarca.setText("5. Catalogo de Marcas (Listo)")
                }
            }
        }
        btnActCategoria.setOnClickListener{
            actualizarCategoriasNubeALocal(){
                    res,mensajeError->
                if(res)
                {
                    btnActCategoria.isVisible=false
                    lblCatalogoCategoria.setText("6. Catalogo de Categorias (Listo)")
                }
            }
        }
        btnActTipos.setOnClickListener{
            actualizarTiposPagoNubeALocal(){
                    res,mensajeError->
                if(res)
                {
                    btnActTipos.isVisible=false
                    lblCatalogoTP.setText("7. Catalogo de Tipos de pago (Listo)")
                }
            }
        }
        btnActArt.setOnClickListener {
            actualizarArticulosNubeALocal(){
                    res,mensajeError->
                if(res)
                {
                    if(listaArtsAct!=null && listaArtsAct.count()>0)
                    {
                        dalArt.updateArtAct(listaArtsAct){
                                res->
                            if(res)
                            {
                                dialog.dismiss()
                            }
                            else
                            {

                            }
                        }
                    }

                    btnActArt.isVisible=false
                    lblCatalogoArt.setText("8. Catalogo de Articulos (Listo)")
                }
            }
        }
        dialog.show()
    }

    fun obtenEmpresaLocal(idEmpresaNube:Int, onFinishReadEmpresaNube:(Empresa?)->Unit) {
        val find = base.empresaDAO().getByFilters(null, idEmpresaNube)
        runOnUiThread {
            find.observe(this@menu_principal_activity) {
                onFinishReadEmpresaNube(it)
            }
        }
    }

    fun obtenCfgNVLocal(idCfgNVNube:Int, onFinishReadEmpresaNube:(CfgNV?)->Unit) {
        val find = base.cfgNVDAO().getByFilters(null,idCfgNVNube)
        runOnUiThread {
            find.observe(this@menu_principal_activity) {
                onFinishReadEmpresaNube(it)
            }
        }
    }

    fun obtenCfgLocal(idCfgNVNube:Int, onFinishReadNube:(Config?)->Unit) {
        val find = base.configDAO().getByIdNube(idCfgNVNube)
        runOnUiThread {
            find.observe(this@menu_principal_activity) {
                onFinishReadNube(it)
            }
        }
    }

    private fun actualizarUMNubeALocal(onFinishActualizacion: (Boolean, String) -> Unit) {
        var lista:List<UnidadMedida>
        lista=listaUMNube.map {
            UnidadMedida(it.Id!!.toInt(),it.Id!!.toInt(),it.Nombre!!,it.Abreviatura!!,it.Activa!!,it.IdEmpresaNube!!.toInt())
        }
        val scope = MainScope()
        fun asyncFun() = scope.launch {
            try {
                base.unidadMedidaDAO().insertAll(lista)
                onFinishActualizacion(true, "")
            }
            catch (ex:Exception)
            {
                onFinishActualizacion(false, ex.toString())
            }
        }
        asyncFun()
    }

    private fun actualizarMarcasNubeALocal(onFinishActualizacion: (Boolean,String) -> Unit) {
        var lista:List<Marca>
        lista=listaMarcasNube.map {
            Marca(it.Id!!.toInt(),it.Id!!.toInt(),it.Nombre!!,it.Activa!!,it.Predeterminada!!,it.SinMarca,it.IdEmpresa!!.toInt())
        }
        val scope = MainScope()
        fun asyncFun() = scope.launch {
            try {
                base.marcaDAO().insertAll(lista)
                onFinishActualizacion(true, "")
            }
            catch (ex:Exception)
            {
                onFinishActualizacion(false, ex.toString())
            }
        }
        asyncFun()
    }

    private fun actualizarCategoriasNubeALocal(onFinishActualizacion: (Boolean,String) -> Unit) {
        var lista:List<Categoria>
        lista=listaCategoriasNube.map {
            Categoria(it.Id!!.toInt(),it.Id!!.toInt(),it.Nombre!!,it.Activa!!,it.Predeterminada!!,it.Orden!!.toShort(),it.IdEmpresaNube!!.toInt())
        }
        val scope = MainScope()
        fun asyncFun() = scope.launch {
            try {
                base.categoriaDAO().insertAll(lista)
                onFinishActualizacion(true, "")
            }
            catch (ex:Exception)
            {
                onFinishActualizacion(false, ex.toString())
            }
        }
        asyncFun()
    }

    private fun actualizarTiposPagoNubeALocal(onFinishActualizacion: (Boolean,String) -> Unit) {
        var lista:List<TipoPago>
        lista=listaTipospagoNube.map {
            TipoPago(it.Id!!.toInt(),it.Id!!.toInt(),it.Clave,it.Nombre,it.Pagado,it.ConBanco,it.Credito,it.DineroVirtual,it.Predeterminado,
                it.Activo,it.IdEmpresaNube!!.toInt())
        }
        val scope = MainScope()
        fun asyncFun() = scope.launch {
            try {
                base.tipoPagoDAO().insertAll(lista)
                onFinishActualizacion(true, "")
            }
            catch (ex:Exception)
            {
                onFinishActualizacion(false, ex.toString())
            }
        }
        asyncFun()
    }

    private fun actualizaEmpresaNubeALocal(empresa: EmpresaNube, onFinishActMarcas: (Boolean,String) -> Unit) {
        try {
            var nueva: Empresa
            nueva = Empresa(
                empresa.Id!!.toInt(),
                empresa.Id!!.toInt(),
                empresa.Rfc,
                empresa.RazonSocial,
                empresa.NombreComercial,
                empresa.Telefonos,
                empresa.Contactos,
                empresa.Email,
                empresa.PaginaWeb,
                empresa.CodigoPostal,
                empresa.IdRegimenFiscal!!.toShort(),
                empresa.IdTipoContribuyente!!.toShort(),
                empresa.Activa,
                empresa.Predeterminada
            )
            val scope = MainScope()
            fun asyncFun() = scope.launch {
                try {
                    base.empresaDAO().insert(nueva)
                    onFinishActMarcas(true, "")
                }
                catch (ex1:Exception)
                {
                    onFinishActMarcas(false, ex1.toString())
                }
            }
            asyncFun()
        }
        catch (ex:Exception)
        {
            onFinishActMarcas(false, ex.toString())
        }
    }

    private fun obtenCfgGlobalNube(onFinishReadConfig: (Config?) -> Unit) {
        var idNube:Int?
        idNube=ParametrosSistema.cfg.IdCfg!!.toInt()
        var idEmpresa:String
        idEmpresa=ParametrosSistema.cfg.IdEmpresa!!
        val scope = MainScope()
        fun asyncFun() = scope.launch {
            var item=base.configDAO().getByFilters(null,idNube,idEmpresa)
            onFinishReadConfig(item.value)
        }
        asyncFun()
    }

    private fun obtenUMsNube(onFinishReadConfig: (List<UnidadMedidaNube>?) -> Unit) {
        var idEmpresa:String
        idEmpresa=ParametrosSistema.cfg.IdEmpresa!!

        dalUM.getAll(idEmpresa){
            onFinishReadConfig(it)
        }
    }

    private fun obtenMarcasNube(onFinishReadConfig: (List<MarcaNube>?) -> Unit) {
        var idEmpresa:String
        idEmpresa=ParametrosSistema.cfg.IdEmpresa!!

        dalMar.getAll(idEmpresa,null){
            onFinishReadConfig(it)
        }
    }

    private fun obtenCategoriasNube(onFinishReadCategorias: (List<CategoriaNube>?) -> Unit) {
        var idEmpresa:String
        idEmpresa=ParametrosSistema.cfg.IdEmpresa!!

        dalCat.getAll(idEmpresa,null){
            onFinishReadCategorias(it)
        }
    }

    private fun obtenTiposPagoNube(onFinishReadTipos: (List<TipoPagoNube>?) -> Unit) {
        var idEmpresa:String
        idEmpresa=ParametrosSistema.cfg.IdEmpresa!!

        dalTP.getAll(idEmpresa){
            onFinishReadTipos(it)
        }
    }

    private fun obtenArticulosNube(onFinishRead: (List<ArticuloNube>?) -> Unit) {
        var idEmpresa:String
        idEmpresa=ParametrosSistema.cfg.IdEmpresa!!

        dalArt.getAll(){
            onFinishRead(it)
        }
    }

    private fun validaArtsNubeConLocal(onFinishReadTipos: (List<Articulo>?) -> Unit) {
        var idEmpresa:String
        idEmpresa=ParametrosSistema.cfg.IdEmpresa!!
        val scope = MainScope()
        fun asyncFun() = scope.launch {
            var item=base.articuloDAO().getAll("")

            onFinishReadTipos(item.value)
        }
        asyncFun()
    }

    fun actualizarArticulosNubeALocal(onFinishActualizacion: (Boolean,String) -> Unit) {
        var lista:List<Articulo>
        lista=listaArticulosNube.map {
            var idUnidadMedida:Int=1
            var idCategoria:Int=1
            var idMarca:Int=1

            var findUM=listaUMNubeTotal.filter {a-> it.IdUnidadMedida==a.IdLocal }.firstOrNull()
            if(findUM!=null)idUnidadMedida=findUM.Id!!.toInt()
            var findC=listaCategoriasNubeTotal.filter { a->it.IdCategoria==a.IdLocal }.firstOrNull()
            if(findC!=null)idCategoria=findC.Id!!.toInt()
            var findM=listaMarcasNubeTotal.filter { a->it.IdMarca==a.IdLocal }.firstOrNull()
            if(findM!=null)idMarca=findM.Id!!.toInt()

            Articulo(0,it.Id!!.toInt(),it.Clave!!,it.Nombre!!,it.NombreCorto!!,idUnidadMedida,
                it.CodigoBarra!!,it.CodigoBarraPaquete!!, idMarca,idCategoria,it.Existencia!!, it.Apartados!!,
                it.CantidadBloqueada!!,it.CantidadPiezasUnidad!!.toShort(), it.CantidadPiezasUnidadVendidas!!.toShort(),
                it.CantidadPiezasUnidadBloqueadas!!.toShort(),it.ArticuloFicticio!!,it.PrecioCompra!!,it.PrecioVenta!!,
                it.PrecioVentaPieza!!, it.PrecioVentaMayoreo1!!, it.PrecioVentaMayoreo2!!, it.NombreArchivoFoto!!,
                it.IdStatus!!.toInt(), it.IdTipoProducto!!, it.ManejaCodigoBarra!!,it.IdEmpresa!!.toInt())
        }
        val scope = MainScope()
        fun asyncFun() = scope.launch {
            try {
                base.articuloDAO().insertAll(lista)
                onFinishActualizacion(true, "")
            }
            catch (ex:Exception)
            {
                onFinishActualizacion(false, ex.toString())
            }
        }
        asyncFun()
    }

    fun actualizaConfigsLocalmente(onFinishAct: (Int, Boolean) -> Unit) {
        var  cfg:CfgNube= ParametrosSistema.cfg

        val scope = MainScope()
        fun asyncFun() = scope.launch {
            try {
                base.configDAO().insert(Config(0.toInt(),cfg.IdCfg!!.toInt(),cfg.IdCfgLocal,cfg.IdEmpresa!!.toInt(),
                    cfg.Predeterminada,cfg.ConsecutivoFolioPago, cfg.PrefijoFolioPago,cfg.NombreTipoPagoPredeterminado,cfg.Activa))
                onFinishAct(cfg.IdCfg!!.toInt(), true)
            }
            catch (ex:Exception)
            {
                onFinishAct(0,false)
            }
        }
        asyncFun()
    }

    fun actualizaConfigNVLocalmente(config: Config, onFinishActNV: (Boolean) -> Unit) {
        var cfgNV:CfgNVNube=ParametrosSistema.cfgNV

        val scope = MainScope()
        fun asyncFun() = scope.launch {
            try {
                var idCfg = config.IdCfg.toString()
                base.cfgNVDAO().insert(
                    CfgNV(
                        0,
                        cfgNV.IdCfgNV!!.toInt(),
                        cfgNV.IdCfgNVLocal!!.toInt(),
                        idCfg,
                        cfgNV.ConsecutivoFolioNV,
                        cfgNV.PrefijoFolioNV,
                        cfgNV.IdTipoPagoPredeterminado.toString(),
                        cfgNV.NombreTipoPagoPredeterminado
                    ))
                onFinishActNV(true)
            }
            catch (ex:Exception)
            {
                onFinishActNV(false)
            }
        }
        asyncFun()
    }

    private fun getEmpresaNubeCfgNubeCfgNVNube() {
        var idEmpresa=ParametrosSistema.usuarioLogueado.IdEmpresa

        dalEmp.getByFilters(idEmpresa){
                res->
            empresaNube=null
            if(res!=null && res.count()>0) {
                empresaNube=res[0]
            }
            if(empresaNube!=null) ParametrosSistema.empresaNube=empresaNube!!

            dalCfg.getAll(idEmpresa){
                    res->
                if(res!=null && res.count()>0)
                {
                    var  item=res[0]
                    ParametrosSistema.cfg=res[0]
                    dalCfgNV.getAll(item.IdCfg!!){
                            res1->
                        if(res1!=null)
                            ParametrosSistema.cfgNV=res1
                        //verificarCatalogosDeNubePorActualizarLocalmente();
                    }
                }
                else
                {
                    bllUtil.MessageShow(this,"Aceptar","","No tiene una Configuración en el sistema",
                        "Aviso"){
                    }
                }
            }
        }
    }

    private fun seleccionPantallaArticuloAEjecutar() {
        bllUtil.MessageShow(this,"Articulo Nube","Articulo Local","Opcion a Ejecutar",
            "Pregunta")
        {
                res->
            if(res==2)
                ejecutaPantallaArticuloLocal()
            //   else
            // ejecutaPantallaArticuloNube()
        }
    }

    /*  private fun ejecutaPantallaArticuloNube()
      {
          val intent= Intent(this,articulo_nube_activity::class.java)
          intent.putExtra("tipoArticulo",2)
          startActivity(intent)
      }*/

    private fun ejecutaPantallaArticuloLocal()
    {
        val intent= Intent(this,articulo_alta_activity::class.java)
        intent.putExtra("tipoArticulo",2)
        startActivity(intent)
    }

    private val startForResult=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode== Activity.RESULT_OK)
        {
            val intent=result.data

            idDocumentoPendiente=intent?.extras?.getInt("idDocumentoPendiente")!!

            if(idDocumentoPendiente!=null && idDocumentoPendiente!!>0) {
            }
        }
    }
}
package com.example.negociomx_pos.room.BLL

import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.example.negociomx_pos.BE.CategoriaNube
import com.example.negociomx_pos.BE.EmpresaNube
import com.example.negociomx_pos.BE.UnidadMedidaNube
import com.example.negociomx_pos.R
import com.example.negociomx_pos.adapters.SpinnerAdapter
import com.example.negociomx_pos.room.entities.Admins.Rol
import com.example.negociomx_pos.room.entities.Cliente
import com.example.negociomx_pos.room.entities.ItemSpinner
import com.example.negociomx_pos.room.entities.TipoPago
import java.io.File


public class BLLUtil {
    fun convertListRolToListSpinner(context: Context, lista: List<Rol>):SpinnerAdapter {
        val transform: (Rol) -> (ItemSpinner) = {
            ItemSpinner(it.IdRol.toInt(), it.Nombre)
        }
        val result = lista.map(transform).toList()

        var adapter = SpinnerAdapter(
            context, result, R.layout.item_spinner_status,
            R.id.lblDisplayStatus
        )

        return adapter
    }

    fun convertListEmpresaToListSpinner(context: Context, lista: List<EmpresaNube>):SpinnerAdapter {
        val transform: (EmpresaNube) -> (ItemSpinner) = {
            ItemSpinner(it.Id?.toInt()!!, it.RazonSocial)
        }
        val result = lista.map(transform).toList()

        var adapter = SpinnerAdapter(
            context, result, R.layout.item_spinner_status,
            R.id.lblDisplayStatus
        )

        return adapter
    }

    fun convertListUnidadMedidaToListSpinner(context: Context, lista: List<UnidadMedidaNube>):SpinnerAdapter {
        val transform: (UnidadMedidaNube) -> (ItemSpinner) = {
            ItemSpinner(it.Id?.toInt()!!, it.Nombre+" ("+it.Abreviatura+")")
        }
        val result = lista.map(transform).toList()

        var adapter = SpinnerAdapter(
            context, result, R.layout.item_spinner_status,
            R.id.lblDisplayStatus
        )

        return adapter
    }

    fun convertListTipoPagoToListSpinner(context: Context, lista: List<TipoPago>?):SpinnerAdapter {
        val transform: (TipoPago) -> (ItemSpinner) = {
            ItemSpinner(it.IdTipoPago?.toInt()!!, it.Nombre+")")
        }

        var result:List<ItemSpinner> = arrayListOf()
        if(lista!=null) result = lista.map(transform).toList()

        var adapter = SpinnerAdapter(
            context, result, R.layout.item_spinner_status,
            R.id.lblDisplayStatus
        )

        return adapter
    }

    fun convertListClienteToListSpinner(context: Context, lista: List<Cliente>?):SpinnerAdapter {
        val transform: (Cliente) -> (ItemSpinner) = {
            ItemSpinner(it.IdCliente?.toInt()!!, it.Nombre)
        }

        var result:List<ItemSpinner> = arrayListOf()
        if(lista!=null) result = lista.map(transform).toList()

        var adapter = SpinnerAdapter(
            context, result, R.layout.item_spinner_status,
            R.id.lblDisplayStatus
        )

        return adapter
    }

    fun convertListCategoriaToListSpinner(context: Context, lista: List<CategoriaNube>):SpinnerAdapter {
        val transform: (CategoriaNube) -> (ItemSpinner) = {
            ItemSpinner(it.Id?.toInt()!!, it.Nombre!!)
        }
        val result = lista.map(transform).toList()

        var adapter = SpinnerAdapter(
            context, result, R.layout.item_spinner_status,
            R.id.lblDisplayStatus
        )

        return adapter
    }

    fun MessageShow(context: Context, textoAceptar:String, textoCancelar:String, mensaje:String, encabezado:String,
                    onClickEventListener:(Int) -> Unit) {
        val builder = AlertDialog.Builder(context)

        builder.setTitle(encabezado)
        builder.setMessage(mensaje)
            .setPositiveButton(textoAceptar, DialogInterface.OnClickListener { dialog, id ->
                onClickEventListener(1)
            })
            .setNegativeButton(textoCancelar, { dialog, id ->
                onClickEventListener(2)
            })
        builder.show()

    }

    fun MessageShow(context: Context, mensaje:String, encabezado:String, onClickEventListener:(Int) -> Unit) {
        val builder = AlertDialog.Builder(context)

        builder.setTitle(encabezado)
        builder.setMessage(mensaje)
            .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                onClickEventListener(1)
            })
            .setNegativeButton("CANCELAR", { dialog, id ->
                onClickEventListener(2)
            })
        builder.show()
    }

    public fun getUriRealPath(context: Context, uri: Uri): String? {
        var filePath: String? = ""
        if (isContentUri(uri)) {
            filePath = if (isGooglePhotoDoc(uri.authority)) {
                uri.lastPathSegment
            } else {
                getFileRealPath(context.contentResolver, uri, null)
            }
        } else if (isFileUri(uri)) {
            filePath = uri.path
        } else if (DocumentsContract.isDocumentUri(context, uri)) {
            // Get uri related document id
            val documentId = DocumentsContract.getDocumentId(uri)
            // Get uri authority
            val uriAuthority = uri.authority
            if (isMediaDoc(uriAuthority)) {
                val idArr = documentId.split(":".toRegex()).toTypedArray()
                if (idArr.size == 2) {
                    // First item is document type
                    val docType = idArr[0]
                    // Second item is document real id
                    val realDocId = idArr[1]
                    // Get content uri by document type
                    var mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    when (docType) {
                        "image" -> {
                            mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        }
                        "video" -> {
                            mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        }
                    }
                    // Get where clause with real document id.
                    val whereClause = MediaStore.Images.Media._ID + " = " + realDocId
                    filePath = getFileRealPath(
                        context.contentResolver,
                        mediaContentUri,
                        whereClause
                    )
                }
            } else if (isDownloadDoc(uriAuthority)) {
                // Build download uri.
                val downloadUri = Uri.parse("content://downloads/public_downloads")
                // Append download document id at uri end.
                val downloadUriAppendId =
                    ContentUris.withAppendedId(downloadUri, java.lang.Long.valueOf(documentId))
                filePath = getFileRealPath(
                    context.contentResolver,
                    downloadUriAppendId,
                    null
                )
            } else if (isExternalStoreDoc(uriAuthority)) {
                val idArr = documentId.split(":".toRegex()).toTypedArray()
                if (idArr.size == 2) {
                    val type = idArr[0]
                    val realDocId = idArr[1]
                    if ("primary".equals(type, ignoreCase = true)) {
                        filePath = Environment.getExternalStorageDirectory()
                            .toString() + "/" + realDocId
                    }
                }
            }
        }
        return filePath
    }

    private fun isContentUri(uri: Uri): Boolean {
        var isContentUri = false
        val uriSchema = uri.scheme
        if ("content".equals(uriSchema, ignoreCase = true)) {
            isContentUri = true
        }
        return isContentUri
    }

    private fun isFileUri(uri: Uri): Boolean {
        var isFileUri = false
        val uriSchema = uri.scheme
        if ("file".equals(uriSchema, ignoreCase = true)) {
            isFileUri = true
        }
        return isFileUri
    }

    private fun isExternalStoreDoc(uriAuthority: String?): Boolean {
        return "com.android.externalstorage.documents" == uriAuthority
    }

    private fun isDownloadDoc(uriAuthority: String?): Boolean {
        return "com.android.providers.downloads.documents" == uriAuthority
    }

    private fun isMediaDoc(uriAuthority: String?): Boolean {
        return "com.android.providers.media.documents" == uriAuthority
    }

    private fun isGooglePhotoDoc(uriAuthority: String?): Boolean {
        return "com.google.android.apps.photos.content" == uriAuthority
    }

    private fun getFileRealPath(
        contentResolver: ContentResolver,
        uri: Uri,
        whereClause: String?
    ): String {
        var filePath = ""
        // Query the uri with condition
        val cursor = contentResolver.query(uri, null, whereClause, null, null)
        if (cursor != null) { // if cursor is not null
            if (cursor.moveToFirst()) {
                // Get columns name by uri type.
                var columnName = MediaStore.Images.Media.DATA
                when (uri) {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI -> {
                        columnName = MediaStore.Images.Media.DATA
                    }
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI -> {
                        columnName = MediaStore.Video.Media.DATA
                    }
                }
                // Get column index.
                val filePathColumnIndex = cursor.getColumnIndex(columnName)
                // Get column value which is the uri related file local path.
                filePath = cursor.getString(filePathColumnIndex)
            }
            cursor.close()
        }
        return filePath
    }

    public fun getBitmapFromFilename(nombreArchivoFoto:String):Bitmap?
    {
        var bitmap:Bitmap?=null

        if(nombreArchivoFoto.isNotEmpty()) {
            val imgFile = File(nombreArchivoFoto)
            if(imgFile.exists())
                bitmap= BitmapFactory.decodeFile(imgFile.absolutePath)
        }

        return bitmap
    }
}
package com.example.negociomx_pos.room.BLL

import com.example.negociomx_pos.room.entities.DocDet
import com.example.negociomx_pos.room.entities.Documento
import com.example.negociomx_pos.room.entities.DocumentoDetalle
import com.example.negociomx_pos.room.enums.TipoDocumentoEnum

public class BLLDoc {

    public fun asignaDocumentoDetalles(idDocumento:Int, idImpuesto:Int, tasaImpuesto:Float, detalles:List<DocDet>)
    :List<DocumentoDetalle>
    {
        var dets:List<DocumentoDetalle>
        var montoImpuestoPartida:Float=0F

        dets= arrayListOf()

        for (d in detalles) {
            montoImpuestoPartida=0F
            if(tasaImpuesto>0F)
                montoImpuestoPartida= (tasaImpuesto/100.0F)*d.Importe!!
            val det=DocumentoDetalle(IdDocumento = idDocumento, IdArticulo = d.IdArticulo!!, IdDocumentoDetalle = 0,
                PrecioUnitario = d.PrecioUnitario!!, Importe = d.Importe!!, Consecutivo = d.Consecutivo!!, Cantidad = d.Cantidad!!,
                IdImpuesto = idImpuesto, TasaImpuesto = tasaImpuesto, MontoImpuesto = montoImpuestoPartida)

            dets = dets.plus(det)
        }
        return dets
    }

    public fun asignaDocumento(idDocumento: Int, folioDocumento:String, idCliente:Int, idImpuesto:Int, tasaImpuesto:Float, detalles:List<DocDet>):Documento
    {
        var iva:Float=0F
        var subtotal:Float=0F
        var total:Float=0F
        var montoImpuestoPartida:Float=0F
        var doc:Documento

        for (d in detalles) {
            subtotal+=d.Importe!!
            montoImpuestoPartida=0F
            if(tasaImpuesto>0F)
                montoImpuestoPartida= (tasaImpuesto/100.0F)*d.Importe!!
            iva+=montoImpuestoPartida
        }
        total=subtotal+iva

        val idTDNotaVenta=TipoDocumentoEnum.NotaVenta.value.toInt()
        val idTipoPago:Int=1
        val idTipoDescuento:Byte=0

        doc=Documento(IdDocumento = idDocumento, Folio = folioDocumento, IdCliente = idCliente, IdTipoDocumento = idTDNotaVenta,
            Subtotal = subtotal, IVA = iva, Total = total, IdImpuesto = idImpuesto, TasaImpuesto = tasaImpuesto,
            IdTipoPago = idTipoPago.toInt(), CodigoBarra = "", IdTipoDescuento = idTipoDescuento, IdDescuento = 0,
            TasaDescuento = 0F, MontoDescuento = 0F, IdUsuarioAtendio = 0,Pagado = true,Activo = true)

        return doc
    }
}
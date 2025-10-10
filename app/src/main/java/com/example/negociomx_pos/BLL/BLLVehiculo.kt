package com.example.negociomx_pos.BLL

import com.example.negociomx_pos.BE.ConsultaPaso2Item
import com.example.negociomx_pos.BE.Paso1SOCItem
import com.example.negociomx_pos.BE.PasoNumLogVehiculo
import com.example.negociomx_pos.BE.Vehiculo
import com.example.negociomx_pos.BE.VehiculoPaso1
import kotlin.Short

class BLLVehiculo {
    fun convertToVehiculo(v1:VehiculoPaso1):Vehiculo
    {
        var v:Vehiculo=Vehiculo(
            Id=v1.Id,
            IdPaso1LogVehiculo = v1.IdPaso1LogVehiculo,
            IdEmpresa = v1.IdEmpresa,
            Odometro = v1.Odometro,
            VIN = v1.VIN,
            TipoVehiculo = v1.TipoVehiculo,
            TipoCombustible = v1.TipoCombustible,
            ColorInterior = v1.ColorInterior,
            ColorExterior = v1.ColorExterior,
            FechaCreacion = v1.FechaCreacion,
            FechaModificacion = v1.FechaModificacion,
            FechaActualizacion = v1.FechaActualizacion,
            Bateria = v1.Bateria,
            Modelo = v1.Modelo,
            NumeroSerie = v1.NumeroSerie,
            Activo = v1.Activo,
            ModoTransporte = v1.ModoTransporte,
            Vez =v1.Vez,
            IdPasoNumLogVehiculoNotificacion = v1.IdPasoNumLogVehiculoNotificacion,
            IdPasoNumLogVehiculo = v1.IdPasoNumLogVehiculo
        )

        return v
    }

    fun asignaPasoNumLogVehiculo(a:Paso1SOCItem):PasoNumLogVehiculo
    {
        var b=PasoNumLogVehiculo(
            Paso = 1,
            VIN = a.VIN,
            IdVehiculo = a.IdVehiculo,
            IdModelo = a.IdModelo,
            IdMarca = a.IdMarca,
            IdPasoNumLogVehiculo = a.IdPaso1LogVehiculo,
            IdColor = a.IdColor,
            IdColorInterior = a.IdColorInterior,
            Anio = a.Anio,
            Modelo = a.Modelo,
            Marca = a.Marca,
            CantidadFotos = a.CantidadFotos,
            ColorExterior = a.ColorExterior,
            ColorInterior = a.ColorInterior,
            FechaAltaFoto1 = a.FechaAltaFoto1,
            FechaAltaFoto2 = a.FechaAltaFoto2,
            FechaAltaFoto3 = a.FechaAltaFoto3,
            FechaAltaFoto4 = a.FechaAltaFoto4,
            TieneFoto1 = true,
            TieneFoto2 = true,
            TieneFoto3 = true,
            TieneFoto4 = true,

            NombreArchivoFoto1 = a.NombreArchivoFoto1,
            NombreArchivoFoto2 = a.NombreArchivoFoto2,
            NombreArchivoFoto3 = a.NombreArchivoFoto3,
            NombreArchivoFoto4 = a.NombreArchivoFoto4



        )
        return b
    }

    fun asignaPasoNumLogVehiculo(a:List<ConsultaPaso2Item>):MutableList<PasoNumLogVehiculo>
    {
        var lista:MutableList<PasoNumLogVehiculo>

        lista= mutableListOf()
        a.forEach { a ->
            var b = PasoNumLogVehiculo(
                Paso = 2,
                VIN = a.VIN,
                IdVehiculo = a.IdVehiculo,
                IdModelo = a.IdModelo,
                IdMarca = a.IdMarca,
                IdPasoNumLogVehiculo = a.IdPaso2LogVehiculo,
                IdColor = a.IdColor,
                IdColorInterior = a.IdColorInterior,
                Anio = a.Anio,
                Modelo = a.Modelo,
                Marca = a.Marca,
                CantidadFotos = a.CantidadFotos,
                ColorExterior = a.ColorExterior,
                ColorInterior = a.ColorInterior,
                FechaAltaFoto1 = a.FechaAltaFoto1,
                FechaAltaFoto2 = a.FechaAltaFoto2,
                FechaAltaFoto3 = a.FechaAltaFoto3,
                FechaAltaFoto4 = a.FechaAltaFoto4,
                TieneFoto1 = a.TieneFoto1,
                TieneFoto2 = a.TieneFoto2,
                TieneFoto3 = a.TieneFoto3,
                TieneFoto4 = a.TieneFoto4,

                NombreArchivoFoto1 = a.NombreArchivoFoto1,
                NombreArchivoFoto2 = a.NombreArchivoFoto2,
                NombreArchivoFoto3 = a.NombreArchivoFoto3,
                NombreArchivoFoto4 = a.NombreArchivoFoto4
            )
            lista.add(b)
        }
        return lista
    }
}
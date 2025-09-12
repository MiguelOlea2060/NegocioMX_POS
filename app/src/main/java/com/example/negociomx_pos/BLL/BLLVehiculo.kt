package com.example.negociomx_pos.BLL

import com.example.negociomx_pos.BE.Vehiculo
import com.example.negociomx_pos.BE.VehiculoPaso1

class BLLVehiculo {
    fun convertToVehiculo(v1:VehiculoPaso1):Vehiculo
    {
        var v:Vehiculo=Vehiculo(
            Id=v1.Id,
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
            ModoTransporte = v1.ModoTransporte
        )

        return v
    }
}
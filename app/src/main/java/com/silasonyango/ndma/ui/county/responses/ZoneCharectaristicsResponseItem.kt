package com.silasonyango.ndma.ui.county.responses

import com.silasonyango.ndma.ui.county.model.LivelihoodZoneModel

data class ZoneCharectaristicsResponseItem(val zone: LivelihoodZoneModel, var zoneCharectaristics: MutableList<String>)
package com.ndma.livelihoodzones.ui.county.responses

import com.ndma.livelihoodzones.ui.county.model.LivelihoodZoneModel

data class ZoneCharectaristicsResponseItem(val zone: LivelihoodZoneModel, var zoneCharectaristics: MutableList<String>)
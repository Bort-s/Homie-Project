package com.example.homieapp.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.homieapp.R
import com.example.homieapp.model.Block
import com.example.homieapp.model.Guides
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GuidesViewModel : ViewModel() {
    private val _guides = MutableStateFlow(
        listOf(
            Guides(
                "Humedad Alta",
                1,
                "Humedad",
                listOf(
                    Block(
                        title = "Causas",
                        content = "La humedad alta en el hogar suele originarse por una combinación de factores estructurales y hábitos cotidianos. Entre las causas más comunes se encuentran la condensación, que ocurre cuando el vapor de agua choca con superficies frías (como ventanas o paredes mal aisladas), y las filtraciones externas debidas a grietas o problemas en el tejado. Asimismo, actividades básicas como cocinar, ducharse con agua caliente o secar la ropa en el interior sin la ventilación adecuada pueden elevar drásticamente los niveles de higrometría en espacios cerrados.",
                        icon = R.drawable.afraid_dommy,
                    ),
                    Block(
                        title = "Consecuencias",
                        content = "Las consecuencias de un ambiente excesivamente húmedo afectan tanto a la integridad de la vivienda como a la salud de sus habitantes. A nivel estructural, es habitual la aparición de manchas de moho, desprendimiento de pintura y el deterioro de muebles de madera o textiles. En cuanto al bienestar personal, la proliferación de ácaros y hongos en el aire puede agravar problemas respiratorios, provocar alergias y aumentar la sensación de fatiga, además de dificultar la regulación térmica del cuerpo, haciendo que el frío se sienta mucho más intenso en invierno.",
                        icon = R.drawable.happy_dommy
                    ),
                    Block(
                        title = "Soluciones",
                        content = "Para solucionar este problema, la medida más eficaz y sencilla es garantizar una ventilación cruzada diaria, abriendo ventanas durante al menos 10 o 15 minutos. En estancias críticas como baños y cocinas, la instalación de extractores ayuda a evacuar el vapor de forma inmediata. Si el problema persiste, el uso de deshumidificadores eléctricos permite controlar el porcentaje de agua en el aire de manera precisa, mientras que la aplicación de pinturas térmicas o la mejora del aislamiento en paredes exteriores previene de raíz la formación de condensación.",
                        icon = R.drawable.dead_dommy
                    )

                )
            ),
            Guides(
                "Humedad Baja",
                2,
                "Humedad",
                listOf(
                    Block(
                        title = "Causas",
                        content = "",
                        icon = R.drawable.dom_e_esperanzado
                    ),
                    Block(
                        title = "Consecuencias",
                        content = "",
                        icon = R.drawable.dom_e_humo
                    ),
                    Block(
                        title = "Soluciones",
                        content = "",
                        icon = R.drawable.dom_e_sabio
                    )

                )
            )
        )
    )
    val listGuides: StateFlow<List<Guides>> = _guides.asStateFlow()
}
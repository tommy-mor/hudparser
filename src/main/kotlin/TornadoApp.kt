import javafx.collections.FXCollections
import tornadofx.*
import javafx.scene.paint.Color

class MyView: View() {
    override val root = vbox {
        label("hud mixer tool")
        hbox {
            vbox {
                label("from")
                listview(listOf("Rayshud","goathud",
                        "m0rehud").observable())
            }
            vbox {
                label("import")
                listview(listOf("health bar","damage numbers",
                        "ammo").observable())
            }
            vbox {
                label("into")
                combobox<String> { items = FXCollections.observableArrayList("Austin",
                        "Dallas","Midland", "San Antonio","Fort Worth") }
            }
        }
        button("make transfer")
        listview(listOf("import HEALTH from RAYSHUD into BASEHUD").observable()) {
            this.setMaxSize(Double.MAX_VALUE,100.0)
        }
        button("create")
    }
    init {
    }
}

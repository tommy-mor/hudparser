import javafx.beans.Observable
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.collections.transformation.TransformationList
import javafx.event.EventHandler
import javafx.scene.control.SelectionMode
import tornadofx.*
import javafx.scene.paint.Color
import java.time.LocalDate
import java.time.Period
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.UnaryOperator
import java.util.stream.Stream

data class Transfer(val from: Hud, val to: Hud, val element: String) {

}

class MyView: View() {
    private var huds = mutableListOf(
            Hud("/Users/tommy/Downloads/rayshud-master")
    ).observable()
    var selectedHud: Hud? = null
    var selectedElement: String? = null
    private lateinit var baseHud: Hud
    override val root = vbox {
        label("hud mixer tool")
        hbox {
            vbox {
                label("from")
                //todo make list of huds save
                //todo make table view with checkmark for if it parsed correctly
                tableview(huds) {
                    readonlyColumn("Name", Hud::hudname)
                    readonlyColumn("Parsed", Hud::parsed)
                    setOnMouseClicked { selectedHud = selectedItem }
                }
                hbox {
                    button("+") {
                        action {
                            val file = chooseDirectory("Select HUD directory")
                            file?.let {
                                val hud = Hud(it.absolutePath)
                                huds.add(hud)
                            }
                        }
                    }
                    button("-") {
                        action {
                            huds.remove(selectedHud)
                        }
                    }
                }
            }

            vbox {
                label("import")
                listview(listOf("health bar", "damage numbers",
                        "ammo").observable()) {
                    setOnMouseClicked { selectedElement = selectedItem }
                }
            }
            vbox {
                label("into")
                combobox<Hud> {
                    items = huds
                }
            }
        }
        button("make transfer") {
            action {

            }
        }
        listview<Transfer>(mutableListOf<Transfer>(Transfer(huds[0], huds[0], "Ammo")).observable()) {
            this.setMaxSize(Double.MAX_VALUE, 100.0)
            this.isFocusTraversable = false
        }
        button("create")
    }


    init {
    }
}